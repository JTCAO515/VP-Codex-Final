import { describe, expect, it, vi } from "vitest";
import { tryButlerService } from "@/lib/ai/butlerServiceGateway";
import { initialTripState } from "@/lib/mock-ai/mockButler";

const validServiceReply = {
  ok: true,
  mode: "butler-service",
  modelLabel: "Butler 2.0",
  intent: "adjust_trip",
  strategy: "single",
  providersTried: ["deepseek"],
  patch: {
    intent: "adjust_trip",
    assistantMessage: "Done.",
    reason: "test",
  },
  suggestions: ["A?", "B?"],
};

function input(env: Record<string, string | undefined>, fetchImpl: typeof fetch) {
  return {
    message: "Make it easier",
    currentTrip: initialTripState,
    recentMessages: [],
    env,
    fetchImpl,
  };
}

describe("tryButlerService", () => {
  it("is inert (returns null, no fetch) when BUTLER_SERVICE_URL is unset", async () => {
    const fetchImpl = vi.fn();
    const result = await tryButlerService(input({}, fetchImpl as unknown as typeof fetch));
    expect(result).toBeNull();
    expect(fetchImpl).not.toHaveBeenCalled();
  });

  it("forwards to the service and returns its valid result", async () => {
    const fetchImpl = vi.fn(async () => new Response(JSON.stringify(validServiceReply), { status: 200 }));
    const result = await tryButlerService(
      input({ BUTLER_SERVICE_URL: "https://butler.fly.dev" }, fetchImpl as unknown as typeof fetch),
    );
    expect(result?.mode).toBe("butler-service");
    expect(fetchImpl).toHaveBeenCalledWith("https://butler.fly.dev/butler/chat", expect.anything());
  });

  it("returns null on non-200 so the local orchestrator takes over", async () => {
    const fetchImpl = vi.fn(async () => new Response("oops", { status: 503 }));
    const result = await tryButlerService(
      input({ BUTLER_SERVICE_URL: "https://butler.fly.dev" }, fetchImpl as unknown as typeof fetch),
    );
    expect(result).toBeNull();
  });

  it("returns null on a malformed body", async () => {
    const fetchImpl = vi.fn(async () => new Response(JSON.stringify({ ok: true, nothing: "useful" }), { status: 200 }));
    const result = await tryButlerService(
      input({ BUTLER_SERVICE_URL: "https://butler.fly.dev" }, fetchImpl as unknown as typeof fetch),
    );
    expect(result).toBeNull();
  });

  it("returns null when the service throws (network failure)", async () => {
    const fetchImpl = vi.fn(async () => {
      throw new Error("connect refused");
    });
    const result = await tryButlerService(
      input({ BUTLER_SERVICE_URL: "https://butler.fly.dev" }, fetchImpl as unknown as typeof fetch),
    );
    expect(result).toBeNull();
  });
});
