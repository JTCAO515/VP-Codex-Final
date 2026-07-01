// Multi-model Butler orchestrator (阶段十三 / ADR-044).
//
// Flow: classify intent -> pick candidate providers (specialist first, then a
// fallback chain) -> for high-stakes intents with 2+ providers run a small
// parallel ensemble, otherwise try the chain in order -> parse the patch ->
// on total failure fall back to the mock Butler. Cost is not a concern
// (ADR-043); redundancy and correctness win.
//
// This never removes the mock fallback: if no provider is configured, or every
// provider fails, the traveler still gets a working canvas patch.

import {
  buildSystemPrompt,
  buildUserPrompt,
  fallbackSuggestionsFor,
  parseButlerPatch,
} from "@/lib/ai/butlerPrompt";
import { classifyIntent, type ButlerIntent } from "@/lib/ai/intentClassifier";
import {
  BUTLER_PROVIDERS,
  getConfiguredProviders,
  isHighStakesIntent,
  selectProvidersForIntent,
} from "@/lib/ai/modelRegistry";
import { createMockButlerPatch } from "@/lib/mock-ai/mockButler";
import type { ChatCompletionProvider, FetchLike } from "@/lib/ai/providers/types";
import type { CanvasPatch, ChatMessage, TripState } from "@/lib/types/trip";

export interface OrchestratedButlerInput {
  message: string;
  currentTrip: TripState;
  recentMessages?: ChatMessage[];
  env?: Record<string, string | undefined>;
  fetchImpl?: FetchLike;
  providers?: ChatCompletionProvider[];
}

export interface OrchestratedButlerResult {
  mode: string; // provider id that produced the patch, or "mock"
  modelLabel: string; // human-readable label for status UI
  intent: ButlerIntent;
  strategy: "single" | "ensemble" | "mock";
  providersTried: string[];
  patch: CanvasPatch;
  suggestions: string[];
  fallbackReason?: string;
}

const MAX_TOKENS = 2200;

async function tryProvider(
  provider: ChatCompletionProvider,
  input: Required<Pick<OrchestratedButlerInput, "message" | "currentTrip" | "recentMessages">>,
  env: Record<string, string | undefined>,
  fetchImpl: FetchLike,
  fallbackSuggestions: string[],
): Promise<{ patch: CanvasPatch; suggestions: string[] }> {
  const result = await provider.complete(
    {
      messages: [
        { role: "system", content: buildSystemPrompt() },
        { role: "user", content: buildUserPrompt(input.message, input.currentTrip, input.recentMessages) },
      ],
      maxTokens: MAX_TOKENS,
      jsonMode: true,
    },
    { env, fetchImpl },
  );
  return parseButlerPatch(result.content, fallbackSuggestions);
}

function mockResult(
  message: string,
  currentTrip: TripState,
  intent: ButlerIntent,
  providersTried: string[],
  fallbackReason?: string,
): OrchestratedButlerResult {
  const patch = createMockButlerPatch(message, currentTrip);
  return {
    mode: "mock",
    modelLabel: "mock fallback",
    intent,
    strategy: "mock",
    providersTried,
    patch,
    suggestions: fallbackSuggestionsFor(message, currentTrip),
    fallbackReason,
  };
}

export async function requestOrchestratedButlerPatch(
  input: OrchestratedButlerInput,
): Promise<OrchestratedButlerResult> {
  const env = input.env ?? (process.env as Record<string, string | undefined>);
  const fetchImpl = input.fetchImpl ?? fetch;
  const recentMessages = input.recentMessages ?? [];
  const message = input.message.trim();
  const intent = classifyIntent(message);

  if (!message) {
    return mockResult(input.message, input.currentTrip, intent, [], "Empty message.");
  }

  const configured = getConfiguredProviders(env, input.providers ?? BUTLER_PROVIDERS);
  if (configured.length === 0) {
    return mockResult(message, input.currentTrip, intent, [], "No Chinese LLM provider is configured.");
  }

  const candidates = selectProvidersForIntent(intent, configured);
  const fallbackSuggestions = fallbackSuggestionsFor(message, input.currentTrip);
  const normalizedInput = { message, currentTrip: input.currentTrip, recentMessages };
  const tried: string[] = [];

  // High-stakes intents with 2+ providers: race the top two in parallel and
  // take the first valid patch (a simple ensemble). Remaining providers still
  // act as a fallback chain afterwards.
  if (isHighStakesIntent(intent) && candidates.length >= 2) {
    const [a, b] = candidates;
    tried.push(a.id, b.id);
    const settled = await Promise.allSettled([
      tryProvider(a, normalizedInput, env, fetchImpl, fallbackSuggestions),
      tryProvider(b, normalizedInput, env, fetchImpl, fallbackSuggestions),
    ]);
    // Prefer the primary (a) when both succeed.
    for (let i = 0; i < settled.length; i += 1) {
      const outcome = settled[i];
      if (outcome.status === "fulfilled") {
        const provider = i === 0 ? a : b;
        return {
          mode: provider.id,
          modelLabel: provider.label,
          intent,
          strategy: "ensemble",
          providersTried: [...tried],
          patch: outcome.value.patch,
          suggestions: outcome.value.suggestions,
        };
      }
    }
    // Both raced providers failed; continue the chain with the rest.
    for (let i = 2; i < candidates.length; i += 1) {
      const provider = candidates[i];
      tried.push(provider.id);
      try {
        const { patch, suggestions } = await tryProvider(provider, normalizedInput, env, fetchImpl, fallbackSuggestions);
        return {
          mode: provider.id,
          modelLabel: provider.label,
          intent,
          strategy: "single",
          providersTried: [...tried],
          patch,
          suggestions,
        };
      } catch {
        // try next
      }
    }
    return mockResult(message, input.currentTrip, intent, tried, "All configured providers failed.");
  }

  // Standard path: try the fallback chain in order.
  let lastError = "";
  for (const provider of candidates) {
    tried.push(provider.id);
    try {
      const { patch, suggestions } = await tryProvider(provider, normalizedInput, env, fetchImpl, fallbackSuggestions);
      return {
        mode: provider.id,
        modelLabel: provider.label,
        intent,
        strategy: "single",
        providersTried: [...tried],
        patch,
        suggestions,
      };
    } catch (error) {
      lastError = error instanceof Error ? error.message : "Unknown provider error.";
    }
  }

  return mockResult(message, input.currentTrip, intent, tried, lastError || "All configured providers failed.");
}
