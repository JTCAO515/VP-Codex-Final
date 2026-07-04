import { beforeEach, describe, expect, it, vi } from "vitest";
import { requestOrchestratedButlerPatch, resetProviderHealthForTests } from "@/lib/ai/orchestrator";
import { initialTripState } from "@/lib/mock-ai/mockButler";
import type { ChatCompletionProvider, ProviderCapability } from "@/lib/ai/providers/types";

beforeEach(() => {
  resetProviderHealthForTests();
});

function validPatchJson(message: string) {
  return JSON.stringify({
    intent: "adjust_trip",
    assistantMessage: `Handled: ${message}`,
    assistantResponse: {
      headline: "Trip updated",
      body: `Handled: ${message}`,
      highlights: ["Pace stays balanced", "Transport stays practical"],
      watchOut: "Book popular sights early.",
      nextStep: "Review Day 1.",
    },
    reason: "Test provider response.",
    suggestions: ["One?", "Two?"],
    tripSummary: { confidence: "Refined" },
  });
}

function makeProvider(
  id: string,
  opts: { capabilities?: ProviderCapability[]; behavior?: "ok" | "throw"; content?: string } = {},
): ChatCompletionProvider {
  const behavior = opts.behavior ?? "ok";
  return {
    id,
    label: `Provider ${id}`,
    model: `${id}-model`,
    capabilities: opts.capabilities ?? ["reasoning"],
    isConfigured: () => true,
    complete: vi.fn(async ({ messages }) => {
      if (behavior === "throw") throw new Error(`${id} failed`);
      const userMsg = messages.find((m) => m.role === "user")?.content ?? "";
      return { content: opts.content ?? validPatchJson(userMsg), providerId: id, model: `${id}-model` };
    }),
  };
}

describe("requestOrchestratedButlerPatch", () => {
  it("routes to a configured provider and returns its parsed patch", async () => {
    const provider = makeProvider("deepseek");
    const result = await requestOrchestratedButlerPatch({
      message: "Make the Beijing day calmer",
      currentTrip: initialTripState,
      env: { DEEPSEEK_API_KEY: "k" },
      fetchImpl: vi.fn(),
      providers: [provider],
    });

    expect(result.mode).toBe("deepseek");
    expect(result.strategy).toBe("single");
    expect(result.intent).toBe("adjust_trip");
    expect(result.patch.tripSummary?.confidence).toBe("Refined");
    expect(result.patch.assistantResponse?.headline).toBe("Trip updated");
    expect(result.patch.assistantResponse?.highlights).toHaveLength(2);
    expect(result.suggestions).toEqual(["One?", "Two?"]);
  });

  it("keeps old provider responses compatible by deriving a structured fallback", async () => {
    const provider = makeProvider("deepseek", {
      content: JSON.stringify({
        intent: "adjust_trip",
        assistantMessage: "Legacy plain response.",
        reason: "Legacy response still parsed.",
        suggestions: ["First?", "Second?"],
      }),
    });
    const result = await requestOrchestratedButlerPatch({
      message: "Make the Beijing day calmer",
      currentTrip: initialTripState,
      env: { DEEPSEEK_API_KEY: "k" },
      fetchImpl: vi.fn(),
      providers: [provider],
    });

    expect(result.patch.assistantResponse).toEqual({
      headline: "Legacy plain response.",
      body: "",
      highlights: [],
      nextStep: "First?",
    });
  });

  it("falls through the chain when the first provider fails", async () => {
    const first = makeProvider("deepseek", { behavior: "throw" });
    const second = makeProvider("qwen", { capabilities: ["reasoning"] });
    const result = await requestOrchestratedButlerPatch({
      message: "Make it less tiring",
      currentTrip: initialTripState,
      env: { DEEPSEEK_API_KEY: "k", DASHSCOPE_API_KEY: "k" },
      fetchImpl: vi.fn(),
      providers: [first, second],
    });

    expect(result.mode).toBe("qwen");
    expect(result.providersTried).toEqual(["deepseek", "qwen"]);
  });

  it("races multiple providers in parallel and returns a valid winner", async () => {
    const primary = makeProvider("deepseek");
    const secondary = makeProvider("qwen");
    const result = await requestOrchestratedButlerPatch({
      message: "Plan me a 5 day trip in China",
      currentTrip: initialTripState,
      env: { DEEPSEEK_API_KEY: "k", DASHSCOPE_API_KEY: "k" },
      fetchImpl: vi.fn(),
      providers: [primary, secondary],
    });

    expect(result.intent).toBe("create_trip");
    expect(result.strategy).toBe("parallel");
    expect(["deepseek", "qwen"]).toContain(result.mode);
    expect(result.providersTried).toEqual(["deepseek", "qwen"]);
  });

  it("returns the healthy provider even when a faster one rejects (parallel race)", async () => {
    const failing = makeProvider("deepseek", { behavior: "throw" });
    const healthy = makeProvider("qwen");
    const result = await requestOrchestratedButlerPatch({
      message: "Plan me a 5 day trip in China",
      currentTrip: initialTripState,
      env: { DEEPSEEK_API_KEY: "k", DASHSCOPE_API_KEY: "k" },
      fetchImpl: vi.fn(),
      providers: [failing, healthy],
    });

    expect(result.mode).toBe("qwen");
    expect(result.strategy).toBe("parallel");
  });

  it("falls back to the mock butler when no provider is configured", async () => {
    const result = await requestOrchestratedButlerPatch({
      message: "first time in China for 5 days",
      currentTrip: initialTripState,
      env: {},
      fetchImpl: vi.fn(),
    });

    expect(result.mode).toBe("mock");
    expect(result.strategy).toBe("mock");
    expect(result.patch.days?.some((day) => day.city === "Beijing")).toBe(true);
    expect(result.suggestions).toHaveLength(2);
    expect(result.fallbackReason).toBeTruthy();
  });

  it("answers factual travel-tool questions without calling an LLM provider", async () => {
    const provider = makeProvider("deepseek");
    const result = await requestOrchestratedButlerPatch({
      message: "Do I need a visa or transit permit for China?",
      currentTrip: initialTripState,
      env: { DEEPSEEK_API_KEY: "k" },
      fetchImpl: vi.fn(),
      providers: [provider],
    });

    expect(result.mode).toBe("tools");
    expect(result.strategy).toBe("tool");
    expect(result.modelLabel).toBe("VisePanda Tools");
    expect(result.patch.assistantResponse?.toolCards?.[0].categoryId).toBe("visa-and-entry");
    expect(provider.complete).not.toHaveBeenCalled();
  });

  it("falls back to the mock butler when every provider throws", async () => {
    const result = await requestOrchestratedButlerPatch({
      message: "Make the plan easier",
      currentTrip: initialTripState,
      env: { DEEPSEEK_API_KEY: "k" },
      fetchImpl: vi.fn(),
      providers: [makeProvider("deepseek", { behavior: "throw" })],
    });

    expect(result.mode).toBe("mock");
    expect(result.fallbackReason).toContain("deepseek failed");
  });

  it("normalizes days missing blocks so write-through never crashes (v0.3.18)", async () => {
    const patchWithHoleyDays = JSON.stringify({
      intent: "create_trip",
      assistantMessage: "Planned!",
      reason: "model omitted blocks on day 2",
      suggestions: ["A?", "B?"],
      days: [
        { day: 1, city: "Beijing", blocks: [{ time: "Morning", title: "Forbidden City", description: "Go early" }] },
        { day: 2, city: "Beijing" }, // no blocks at all — crashed production before
      ],
    });
    const result = await requestOrchestratedButlerPatch({
      message: "Plan me a 2 day trip in Beijing",
      currentTrip: initialTripState,
      env: { DEEPSEEK_API_KEY: "k" },
      fetchImpl: vi.fn(),
      providers: [makeProvider("deepseek", { content: patchWithHoleyDays })],
    });

    expect(result.mode).toBe("deepseek");
    expect(result.patch.days).toHaveLength(2);
    expect(result.patch.days?.[1].blocks).toEqual([]);
  });

  it("recovers a patch from JSON truncated at max_tokens (v0.3.17)", async () => {
    const truncated =
      '{"intent":"adjust_trip","assistantMessage":"Made Day 1 lighter","reason":"pace","suggestions":["A?","B?"],"tripSummary":{"confidence":"Refined"},"days":[{"day":1,"city":"Beijing","note":"Cut mid stri';
    const provider = makeProvider("deepseek", { content: truncated });
    const result = await requestOrchestratedButlerPatch({
      message: "Make the Beijing day calmer",
      currentTrip: initialTripState,
      env: { DEEPSEEK_API_KEY: "k" },
      fetchImpl: vi.fn(),
      providers: [provider],
    });

    expect(result.mode).toBe("deepseek");
    expect(result.patch.assistantMessage).toBe("Made Day 1 lighter");
  });

  it("rejects a create_trip patch without a days array (quality gate, v0.3.17)", async () => {
    const lazyPatch = JSON.stringify({
      intent: "create_trip",
      assistantMessage: "I planned your trip!",
      reason: "claims to have planned but returned no days",
      suggestions: ["A?", "B?"],
    });
    const result = await requestOrchestratedButlerPatch({
      message: "Plan me a 5 day trip in China",
      currentTrip: initialTripState,
      env: { DEEPSEEK_API_KEY: "k" },
      fetchImpl: vi.fn(),
      providers: [makeProvider("deepseek", { content: lazyPatch })],
    });

    expect(result.mode).toBe("mock");
    expect(result.fallbackReason).toContain("days");
  });

  it("skips a provider tripped by the circuit breaker on the next request (v0.3.17)", async () => {
    const failing = makeProvider("deepseek", { behavior: "throw" });
    const healthy = makeProvider("qwen");
    const env = { DEEPSEEK_API_KEY: "k", DASHSCOPE_API_KEY: "k" };

    // Two consecutive failures trip the breaker for deepseek.
    for (let i = 0; i < 2; i++) {
      await requestOrchestratedButlerPatch({
        message: "Make it less tiring",
        currentTrip: initialTripState,
        env,
        fetchImpl: vi.fn(),
        providers: [failing, healthy],
      });
    }

    const result = await requestOrchestratedButlerPatch({
      message: "Make it less tiring",
      currentTrip: initialTripState,
      env,
      fetchImpl: vi.fn(),
      providers: [failing, healthy],
    });

    expect(result.mode).toBe("qwen");
    expect(result.providersTried).toEqual(["qwen"]);
  });

  it("ignores the breaker when every candidate is tripped (never mock-locked)", async () => {
    const failing = makeProvider("deepseek", { behavior: "throw" });
    const env = { DEEPSEEK_API_KEY: "k" };

    for (let i = 0; i < 3; i++) {
      await requestOrchestratedButlerPatch({
        message: "Make it less tiring",
        currentTrip: initialTripState,
        env,
        fetchImpl: vi.fn(),
        providers: [failing],
      });
    }

    // Even fully tripped, the sole provider is still attempted (then mock).
    const result = await requestOrchestratedButlerPatch({
      message: "Make it less tiring",
      currentTrip: initialTripState,
      env,
      fetchImpl: vi.fn(),
      providers: [failing],
    });
    expect(result.providersTried).toEqual(["deepseek"]);
    expect(result.mode).toBe("mock");
  });
});
