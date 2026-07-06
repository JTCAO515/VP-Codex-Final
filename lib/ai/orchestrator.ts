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
  type ButlerPromptMode,
} from "@/lib/ai/butlerPrompt";
import { classifyIntent, type ButlerIntent } from "@/lib/ai/intentClassifier";
import { BUTLER_PROVIDERS, getConfiguredProviders, selectProvidersForIntent } from "@/lib/ai/modelRegistry";
import { buildButlerToolContext, type ButlerToolContext } from "@/lib/ai/toolContext";
import { applyToolContextToPatch } from "@/lib/ai/toolContextWriteThrough";
import type { UserPreferenceProfile } from "@/lib/ai/preferenceProfile";

import { buildFactualToolResponse } from "@/lib/tools/factualToolCards";
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
  strategy: "parallel" | "single" | "mock" | "tool";
  providersTried: string[];
  patch: CanvasPatch;
  suggestions: string[];
  fallbackReason?: string;
  toolContext?: ButlerToolContext;
}

const TOOL_CONTEXT_TIMEOUT_MS = 6000;

// v0.3.17 per-intent budgets. Root cause fixed here: itinerary intents demand
// the COMPLETE days array (system prompt contract), which for a 3-day trip
// regularly exceeded the old flat 2200-token ceiling — the completion was cut
// mid-string and JSON.parse threw ("Unterminated string…" observed in
// production), silently sinking healthy providers into the mock fallback.
// Non-itinerary replies stay small and fast.
const ITINERARY_INTENTS: ReadonlySet<ButlerIntent> = new Set<ButlerIntent>([
  "create_trip",
  "adjust_trip",
  "add_location",
  "add_poi",
]);

function budgetForIntent(intent: ButlerIntent): { maxTokens: number; timeoutMs: number } {
  return ITINERARY_INTENTS.has(intent)
    ? { maxTokens: 4096, timeoutMs: 25000 }
    : { maxTokens: 1400, timeoutMs: 15000 };
}

// v0.3.17 in-memory circuit breaker. A provider that failed twice in a row is
// skipped for a cooldown window instead of being raced (and timing out) on
// every single request — production showed all three configured providers
// unhealthy (truncation / 18s timeout / HTTP 429) with zero systemic memory of
// it. Serverless caveat, recorded honestly: state lives per warm instance and
// resets on cold start, which is acceptable — the goal is not perfect global
// health tracking but avoiding repeated known-bad calls within an instance's
// lifetime. If every candidate is tripped, the breaker is ignored entirely so
// it can never lock the chat into mock-only mode.
const BREAKER_THRESHOLD = 2;
const BREAKER_COOLDOWN_MS = 120_000;
const providerHealth = new Map<string, { consecutiveFailures: number; skipUntil: number }>();

function isTripped(providerId: string, now: number): boolean {
  const entry = providerHealth.get(providerId);
  return Boolean(entry && entry.consecutiveFailures >= BREAKER_THRESHOLD && now < entry.skipUntil);
}

function recordProviderFailure(providerId: string, now: number): void {
  const entry = providerHealth.get(providerId) ?? { consecutiveFailures: 0, skipUntil: 0 };
  entry.consecutiveFailures += 1;
  entry.skipUntil = now + BREAKER_COOLDOWN_MS;
  providerHealth.set(providerId, entry);
}

function recordProviderSuccess(providerId: string): void {
  providerHealth.delete(providerId);
}

/** Test hook — clears breaker state so unit tests stay isolated. */
export function resetProviderHealthForTests(): void {
  providerHealth.clear();
}

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
  intent: ButlerIntent,
  env: Record<string, string | undefined>,
  fetchImpl: FetchLike,
  fallbackSuggestions: string[],
  context: { preferenceProfile?: UserPreferenceProfile; toolContext?: ButlerToolContext },
  budget: { maxTokens: number; timeoutMs: number },
  raceSignal: AbortSignal,
  mode: ButlerPromptMode = "normal",
): Promise<{ provider: ChatCompletionProvider; patch: CanvasPatch; suggestions: string[] }> {
  try {
    const result = await provider.complete(
      {
        messages: [
          { role: "system", content: buildSystemPrompt(intent, mode) },
          { role: "user", content: buildUserPrompt(input.message, input.currentTrip, input.recentMessages, context) },
        ],
        maxTokens: budget.maxTokens,
        timeoutMs: budget.timeoutMs,
        jsonMode: true,
        signal: raceSignal,
      },
      { env, fetchImpl },
    );
    const parsed = parseButlerPatch(result.content, fallbackSuggestions, intent, input.currentTrip.days, mode);
    recordProviderSuccess(provider.id);
    return { provider, patch: parsed.patch, suggestions: parsed.suggestions };
  } catch (error) {
    // A candidate cancelled because a faster provider already won the race
    // (see raceController.abort() below) is not unhealthy — checking
    // raceSignal.aborted here (rather than at call time) is what makes this
    // safe: it is only true once a winner has actually been established, so
    // a genuine failure that happens beforehand still trips the breaker
    // normally. Without this guard, every race would count its slower-but-
    // healthy losers as failures and could spuriously trip the circuit
    // breaker on providers that never actually failed.
    if (!raceSignal.aborted) {
      recordProviderFailure(provider.id, Date.now());
    }
    throw error;
  }
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
    throw new Error("Empty message.");
  }

  const factualToolResponse = await buildFactualToolResponse({ message, currentTrip: input.currentTrip, intent });
  if (factualToolResponse) {
    return {
      mode: "tools",
      modelLabel: "VisePanda Tools",
      intent,
      strategy: "tool",
      providersTried: [],
      patch: factualToolResponse.patch,
      suggestions: factualToolResponse.suggestions,
    };
  }

  const configured = getConfiguredProviders(env, input.providers ?? BUTLER_PROVIDERS);
  if (configured.length === 0) {
    throw new Error("No Chinese LLM provider is configured.");
  }

  const ranked = selectProvidersForIntent(intent, configured);

  // Circuit breaker: skip providers that recently failed repeatedly — unless
  // that would leave nobody, in which case ignore the breaker entirely.
  const now = Date.now();
  const healthy = ranked.filter((provider) => !isTripped(provider.id, now));
  const candidates = healthy.length > 0 ? healthy : ranked;

  const budget = budgetForIntent(intent);
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

  // Race all candidates in parallel; first valid patch wins. The losers are
  // aborted once a winner is found — they were previously left running to
  // their own timeout, silently burning real API quota on every single chat
  // message for no benefit once an answer had already been chosen.
  const raceController = new AbortController();
  try {
    const winner = await Promise.any(
      candidates.map((provider) =>
        tryProvider(provider, normalizedInput, intent, env, fetchImpl, fallbackSuggestions, providerContext, budget, raceController.signal),
      ),
    );
    raceController.abort();
    return {
      mode: winner.provider.id,
      modelLabel: winner.provider.label,
      intent,
      strategy: candidates.length > 1 ? "parallel" : "single",
      providersTried: tried,
      patch: applyToolContextToPatch(winner.patch, toolContext),
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
    throw new Error(reason || "All configured providers failed.");
  }
}

export interface SkeletonCompletionInput {
  skeletonTrip: TripState;
  env?: Record<string, string | undefined>;
  fetchImpl?: FetchLike;
  providers?: ChatCompletionProvider[];
}

/**
 * Trips staged generation round 2 (docs/planning/trips-staged-generation-migration-plan.md).
 * Called by /api/chat when the request body has completeSkeletonFor set —
 * there's no user message to classify, so intent is fixed to create_trip
 * and tool-context prefetch is skipped (no natural-language message to
 * derive search queries from). On total provider failure this throws
 * (same as requestOrchestratedButlerPatch) rather than falling back to the
 * mock Butler: a generic mock itinerary for the skeleton's specific
 * city/pace would be misleading in a way that leaving the skeleton as-is
 * and offering a retry is not.
 */
export async function requestSkeletonCompletion(
  input: SkeletonCompletionInput,
): Promise<OrchestratedButlerResult> {
  const env = input.env ?? (process.env as Record<string, string | undefined>);
  const fetchImpl = input.fetchImpl ?? fetch;
  const intent: ButlerIntent = "create_trip";

  const configured = getConfiguredProviders(env, input.providers ?? BUTLER_PROVIDERS);
  if (configured.length === 0) {
    throw new Error("No Chinese LLM provider is configured.");
  }

  const ranked = selectProvidersForIntent(intent, configured);
  const now = Date.now();
  const healthy = ranked.filter((provider) => !isTripped(provider.id, now));
  const candidates = healthy.length > 0 ? healthy : ranked;

  const budget = budgetForIntent(intent);
  const fallbackSuggestions = fallbackSuggestionsFor("", input.skeletonTrip);
  const normalizedInput = { message: "", currentTrip: input.skeletonTrip, recentMessages: [] };
  const providerContext = {};
  const tried = candidates.map((provider) => provider.id);

  const raceController = new AbortController();
  try {
    const winner = await Promise.any(
      candidates.map((provider) =>
        tryProvider(
          provider,
          normalizedInput,
          intent,
          env,
          fetchImpl,
          fallbackSuggestions,
          providerContext,
          budget,
          raceController.signal,
          "skeletonCompletion",
        ),
      ),
    );
    raceController.abort();
    return {
      mode: winner.provider.id,
      modelLabel: winner.provider.label,
      intent,
      strategy: candidates.length > 1 ? "parallel" : "single",
      providersTried: tried,
      patch: winner.patch,
      suggestions: winner.suggestions,
    };
  } catch (error) {
    const reason =
      error instanceof AggregateError
        ? error.errors.map((e) => (e instanceof Error ? e.message : String(e))).filter(Boolean).join("; ")
        : error instanceof Error
          ? error.message
          : "All configured providers failed to complete the skeleton.";
    throw new Error(reason || "All configured providers failed to complete the skeleton.");
  }
}
