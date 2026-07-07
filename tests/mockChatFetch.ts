import { vi } from "vitest";
import { createMockButlerPatch, initialTripState } from "@/lib/mock-ai/mockButler";
import type { TripState } from "@/lib/types/trip";

export function mockSuccessfulChatFetch() {
  vi.stubGlobal(
    "fetch",
    vi.fn(async (_input: RequestInfo | URL, init?: RequestInit) => {
      const body = JSON.parse(String(init?.body ?? "{}")) as { message?: string; trip?: TripState };
      const patch = createMockButlerPatch(body.message ?? "", body.trip ?? initialTripState);

      return new Response(
        JSON.stringify({
          ok: true,
          mode: "test",
          modelLabel: "Test Butler",
          patch,
          suggestions: ["Refine the route", "Add payment setup"],
        }),
        { status: 200, headers: { "content-type": "application/json" } },
      );
    }),
  );
}

export function mockFailedChatFetch() {
  vi.stubGlobal(
    "fetch",
    vi.fn(async () =>
      new Response(JSON.stringify({ ok: false, error: "Connection failed" }), {
        status: 502,
        headers: { "content-type": "application/json" },
      }),
    ),
  );
}
