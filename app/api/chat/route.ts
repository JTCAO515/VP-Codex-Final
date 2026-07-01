import { NextResponse } from "next/server";
import { requestOrchestratedButlerPatch } from "@/lib/ai/orchestrator";
import { initialTripState } from "@/lib/mock-ai/mockButler";
import type { TripState } from "@/lib/types/trip";
import type { UserPreferenceProfile } from "@/lib/ai/preferenceProfile";

export async function POST(request: Request) {
  const body = await request.json().catch(() => ({}));
  const message = typeof body.message === "string" ? body.message : "";
  const currentTrip = isTripState(body.trip) ? body.trip : initialTripState;
  const recentMessages = Array.isArray(body.messages) ? body.messages : [];
  const preferenceProfile = isPreferenceProfile(body.preferenceProfile) ? body.preferenceProfile : undefined;
  const result = await requestOrchestratedButlerPatch({ currentTrip, message, recentMessages, preferenceProfile });

  return NextResponse.json({
    ok: true,
    fallbackReason: result.fallbackReason,
    mode: result.mode,
    modelLabel: result.modelLabel,
    intent: result.intent,
    strategy: result.strategy,
    providersTried: result.providersTried,
    patch: result.patch,
    suggestions: result.suggestions,
    toolContext: result.toolContext,
  });
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
