import { NextResponse } from "next/server";
import { requestOrchestratedButlerPatch } from "@/lib/ai/orchestrator";
import { initialTripState } from "@/lib/mock-ai/mockButler";
import type { TripState } from "@/lib/types/trip";

export async function POST(request: Request) {
  const body = await request.json().catch(() => ({}));
  const message = typeof body.message === "string" ? body.message : "";
  const currentTrip = isTripState(body.trip) ? body.trip : initialTripState;
  const recentMessages = Array.isArray(body.messages) ? body.messages : [];
  const result = await requestOrchestratedButlerPatch({ currentTrip, message, recentMessages });

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
  });
}

function isTripState(value: unknown): value is TripState {
  if (!value || typeof value !== "object") return false;

  const candidate = value as Partial<TripState>;

  return Boolean(candidate.summary && Array.isArray(candidate.days) && Array.isArray(candidate.alerts));
}
