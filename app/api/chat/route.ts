import { NextResponse } from "next/server";
import { requestOrchestratedButlerPatch, requestSkeletonCompletion } from "@/lib/ai/orchestrator";
import { tryButlerService } from "@/lib/ai/butlerServiceGateway";
import { computeAffectedDays } from "@/lib/canvas/applyCanvasPatch";
import { initialTripState } from "@/lib/mock-ai/mockButler";
import type { TripState } from "@/lib/types/trip";
import type { UserPreferenceProfile } from "@/lib/ai/preferenceProfile";

// Kimi's provider-level minTimeoutMs floor is 90s (modelRegistry.ts) — without
// this, Vercel's default serverless duration would kill the function before a
// real, successful Kimi completion returns, turning a working answer into a
// client-side timeout. (Hobby-tier deployments cap maxDuration at 60s
// regardless of this value; Kimi-as-last-resort would still be cut short
// there — this only takes full effect on Pro or higher.)
export const maxDuration = 120;

export async function POST(request: Request) {
  const body = await request.json().catch(() => ({}));
  const message = typeof body.message === "string" ? body.message : "";
  const currentTrip = isTripState(body.trip) ? body.trip : initialTripState;
  const recentMessages = Array.isArray(body.messages) ? body.messages : [];
  const preferenceProfile = isPreferenceProfile(body.preferenceProfile) ? body.preferenceProfile : undefined;
  // Trips staged generation round 2 (docs/planning/trips-staged-generation-migration-plan.md):
  // a skeleton the client already applied, sent back so the server can fill
  // in blocks. This bypasses butler-service forwarding and intent
  // classification entirely — it's not a normal chat turn.
  const completeSkeletonFor = isTripState(body.completeSkeletonFor) ? body.completeSkeletonFor : undefined;

  try {
    const result = completeSkeletonFor
      ? await requestSkeletonCompletion({ skeletonTrip: completeSkeletonFor })
      : // Butler 2.0 greylist switch: forwards to the external butler-service
        // when BUTLER_SERVICE_URL is set; inert otherwise. Any failure returns
        // null and the local orchestrator (and ultimately the mock Butler)
        // answers as before.
        (await tryButlerService({ message, currentTrip, recentMessages, preferenceProfile })) ??
        (await requestOrchestratedButlerPatch({ currentTrip, message, recentMessages, preferenceProfile }));

    // Single choke point covering every patch source (butler-service forward,
    // factual tools, real LLM race, mock fallback) — clients get affectedDays
    // without each one re-implementing the same before/after day diff. A
    // skeleton-completion round diffs against the skeleton it was given, not
    // the caller's main `currentTrip` (there isn't one in that request).
    const diffBase = completeSkeletonFor ?? currentTrip;
    const patch = {
      ...result.patch,
      affectedDays: computeAffectedDays(diffBase.days, result.patch.days),
    };

    return NextResponse.json({
      ok: true,
      fallbackReason: result.fallbackReason,
      mode: result.mode,
      modelLabel: result.modelLabel,
      intent: result.intent,
      strategy: result.strategy,
      providersTried: result.providersTried,
      patch,
      suggestions: result.suggestions,
      toolContext: result.toolContext,
    });
  } catch (error) {
    console.error("[Chat API Route] orchestrator failed:", error);
    return NextResponse.json({
      ok: false,
      error: "Connection failed. Please check your network and LLM key configurations.",
      details: error instanceof Error ? error.message : String(error),
    }, { status: 502 });
  }
}

function isTripState(value: unknown): value is TripState {
  if (!value || typeof value !== "object") return false;

  const candidate = value as Partial<TripState>;

  return Boolean(candidate.summary && Array.isArray(candidate.days) && Array.isArray(candidate.alerts));
}

function isPreferenceProfile(value: unknown): value is UserPreferenceProfile {
  if (!value || typeof value !== "object") return false;
  const candidate = value as Partial<UserPreferenceProfile>;
  return Array.isArray(candidate.dietaryRestrictions) && Array.isArray(candidate.cuisinePreferences) && Array.isArray(candidate.interests);
}
