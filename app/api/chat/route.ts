import { NextResponse } from "next/server";
import { createMockButlerPatch, initialTripState } from "@/lib/mock-ai/mockButler";

export async function POST(request: Request) {
  const body = await request.json().catch(() => ({}));
  const message = typeof body.message === "string" ? body.message : "";
  const patch = createMockButlerPatch(message, initialTripState);

  return NextResponse.json({
    ok: true,
    mode: "mock",
    patch,
  });
}
