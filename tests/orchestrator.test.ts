import { describe, expect, it, vi } from "vitest";
import { requestOrchestratedButlerPatch } from "@/lib/ai/orchestrator";
import { initialTripState } from "@/lib/mock-ai/mockButler";
import type { ChatCompletionProvider, ProviderCapability } from "@/lib/ai/providers/types";

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

  it("runs a parallel ensemble for high-stakes create_trip and prefers the primary", async () => {
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
    expect(result.strategy).toBe("ensemble");
    expect(result.mode).toBe("deepseek");
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
});
