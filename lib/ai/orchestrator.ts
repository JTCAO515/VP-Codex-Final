// Multi-model Butler orchestrator (阶段十三 / ADR-044, latency-fixed in v0.2.2).
//
// Flow: classify intent -> pick candidate providers (specialist first) -> race
// them IN PARALLEL and take the first valid patch -> on total failure fall back
// to the mock Butler. Cost is not a concern (ADR-043), so racing every capable
// provider in parallel gives the lowest latency and the best resilience: one
// slow or misconfigured model can no longer stall the chat, because a faster
// healthy model answers first and hung calls are bounded by a per-provider
// timeout (see openaiCompatibleProvider).
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
import { BUTLER_PROVIDERS, getConfiguredProviders, selectProvidersForIntent } from "@/lib/ai/modelRegistry";
import { buildButlerToolContext, type ButlerToolContext } from "@/lib/ai/toolContext";
import type { UserPreferenceProfile } from "@/lib/ai/preferenceProfile";
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
  preferenceProfile?: UserPreferenceProfile;
}

export interface OrchestratedButlerResult {
  mode: string; // provider id that produced the patch, or "mock"
  modelLabel: string; // human-readable label for status UI
  intent: ButlerIntent;
  strategy: "parallel" | "single" | "mock";
  providersTried: string[];
  patch: CanvasPatch;
  suggestions: string[];
  fallbackReason?: string;
  toolContext?: ButlerToolContext;
}

const MAX_TOKENS = 2200;
const TOOL_CONTEXT_TIMEOUT_MS = 6000;

async function withTimeout<T>(promise: Promise<T>, ms: number, fallback: T): Promise<T> {
  let timer: ReturnType<typeof setTimeout> | undefined;
  const timeout = new Promise<T>((resolve) => {
    timer = setTimeout(() => resolve(fallback), ms);
  });
  try {
    return await Promise.race([promise, timeout]);
  } finally {
    if (timer) clearTimeout(timer);
  }
}

async function tryProvider(
  provider: ChatCompletionProvider,
  input: Required<Pick<OrchestratedButlerInput, "message" | "currentTrip" | "recentMessages">>,
  env: Record<string, string | undefined>,
  fetchImpl: FetchLike,
  fallbackSuggestions: string[],
  context: { preferenceProfile?: UserPreferenceProfile; toolContext?: ButlerToolContext },
): Promise<{ provider: ChatCompletionProvider; patch: CanvasPatch; suggestions: string[] }> {
  const result = await provider.complete(
    {
      messages: [
        { role: "system", content: buildSystemPrompt() },
        { role: "user", content: buildUserPrompt(input.message, input.currentTrip, input.recentMessages, context) },
      ],
      maxTokens: MAX_TOKENS,
      jsonMode: true,
    },
    { env, fetchImpl },
  );
  const parsed = parseButlerPatch(result.content, fallbackSuggestions);
  return { provider, patch: parsed.patch, suggestions: parsed.suggestions };
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

  // Tool-context prefetch is best-effort and time-bounded so a slow Amap call
  // never stalls the reply.
  const toolContext = await withTimeout(
    buildButlerToolContext({ message, currentTrip: input.currentTrip, intent, env, fetchImpl }),
    TOOL_CONTEXT_TIMEOUT_MS,
    undefined,
  );
  const providerContext = { preferenceProfile: input.preferenceProfile, toolContext };
  const tried = candidates.map((provider) => provider.id);

  // Race all candidates in parallel; first valid patch wins.
  try {
    const winner = await Promise.any(
      candidates.map((provider) =>
        tryProvider(provider, normalizedInput, env, fetchImpl, fallbackSuggestions, providerContext),
      ),
    );
    return {
      mode: winner.provider.id,
      modelLabel: winner.provider.label,
      intent,
      strategy: candidates.length > 1 ? "parallel" : "single",
      providersTried: tried,
      patch: winner.patch,
      suggestions: winner.suggestions,
      toolContext,
    };
  } catch (error) {
    const reason =
      error instanceof AggregateError
        ? error.errors.map((e) => (e instanceof Error ? e.message : String(e))).filter(Boolean).join("; ")
        : error instanceof Error
          ? error.message
          : "All configured providers failed.";
    return mockResult(message, input.currentTrip, intent, tried, reason || "All configured providers failed.");
  }
}
