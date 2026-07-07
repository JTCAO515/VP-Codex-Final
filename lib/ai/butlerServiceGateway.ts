// Gateway to the external butler-service (Butler 2.0 Phase A — see
// docs/planning/butler-2.0-chatbot-design.md and GitHub Issue #8).
//
// Rollout contract: when BUTLER_SERVICE_URL is unset (the default), this module
// is inert and /api/chat behaves exactly as before. When set, the request is
// forwarded to the JVM service; any failure (network, timeout, non-200,
// malformed body) falls back to the local TS orchestrator so the traveler
// always gets a reply. This is the greylist switch that lets butler-service
// ship dark and be enabled per-deployment with a single env var.

import type { ChatMessage, TripState } from "@/lib/types/trip";
import type { UserPreferenceProfile } from "@/lib/ai/preferenceProfile";
import type { OrchestratedButlerResult } from "@/lib/ai/orchestrator";
import type { FetchLike } from "@/lib/ai/providers/types";

const GATEWAY_TIMEOUT_MS = 26000; // slightly above the orchestrator's largest per-intent budget

export interface ButlerServiceForwardInput {
  message: string;
  currentTrip: TripState;
  recentMessages: ChatMessage[];
  preferenceProfile?: UserPreferenceProfile;
  env?: Record<string, string | undefined>;
  fetchImpl?: FetchLike;
}

/** The service must reply with the same shape /api/chat returns (API_SPEC.md). */
function isOrchestratedResult(value: unknown): value is OrchestratedButlerResult {
  if (!value || typeof value !== "object") return false;
  const candidate = value as Partial<OrchestratedButlerResult> & { patch?: { assistantMessage?: unknown } };
  return (
    typeof candidate.mode === "string" &&
    typeof candidate.modelLabel === "string" &&
    Boolean(candidate.patch) &&
    typeof candidate.patch?.assistantMessage === "string" &&
    Array.isArray(candidate.suggestions)
  );
}

/**
 * Forward the chat turn to butler-service. Returns null whenever the service
 * is not configured or anything goes wrong — the caller then runs the local
 * orchestrator as usual. Never throws.
 */
export async function tryButlerService(
  input: ButlerServiceForwardInput,
): Promise<OrchestratedButlerResult | null> {
  const env = input.env ?? (process.env as Record<string, string | undefined>);
  const baseUrl = env.BUTLER_SERVICE_URL?.trim();
  if (!baseUrl) return null;

  const fetchImpl = input.fetchImpl ?? fetch;
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), GATEWAY_TIMEOUT_MS);

  try {
    const response = await fetchImpl(`${baseUrl.replace(/\/$/, "")}/butler/chat`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        message: input.message,
        trip: input.currentTrip,
        messages: input.recentMessages,
        preferenceProfile: input.preferenceProfile,
      }),
      signal: controller.signal,
    });
    if (!response.ok) return null;

    const data = await response.json();
    const result = data && typeof data === "object" && "ok" in data ? (data as { ok: unknown }) : null;
    if (!result || result.ok !== true) return null;
    if (!isOrchestratedResult(data)) return null;
    return data as OrchestratedButlerResult;
  } catch {
    return null; // network error, timeout, bad JSON — all degrade silently
  } finally {
    clearTimeout(timer);
  }
}
