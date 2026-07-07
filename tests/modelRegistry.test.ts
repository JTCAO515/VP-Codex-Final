import { describe, expect, it } from "vitest";
import {
  BUTLER_PROVIDERS,
  getConfiguredProviders,
  isHighStakesIntent,
  selectProvidersForIntent,
} from "@/lib/ai/modelRegistry";

describe("model registry", () => {
  it("registers the six Chinese LLM providers", () => {
    const ids = BUTLER_PROVIDERS.map((p) => p.id);
    expect(ids).toEqual(["deepseek", "qwen", "zhipu", "moonshot", "ernie", "minimax"]);
  });

  it("uses the configured 2026 model defaults for the active China providers", () => {
    expect(BUTLER_PROVIDERS.find((p) => p.id === "deepseek")?.model).toBe("deepseek-v4-flash");
    expect(BUTLER_PROVIDERS.find((p) => p.id === "qwen")?.model).toBe("qwen3.6-flash");
    expect(BUTLER_PROVIDERS.find((p) => p.id === "zhipu")?.model).toBe("glm-5.2");
    expect(BUTLER_PROVIDERS.find((p) => p.id === "moonshot")?.model).toBe("kimi-k2.6");
  });

  it("reports a provider configured only when its key env is present", () => {
    const deepseek = BUTLER_PROVIDERS.find((p) => p.id === "deepseek")!;
    expect(deepseek.isConfigured({})).toBe(false);
    expect(deepseek.isConfigured({ DEEPSEEK_API_KEY: "k" })).toBe(true);
    // alias support
    expect(deepseek.isConfigured({ AI_API_KEY: "k" })).toBe(true);
  });

  it("returns only configured providers in registry order", () => {
    const configured = getConfiguredProviders({ DASHSCOPE_API_KEY: "k", DEEPSEEK_API_KEY: "k" });
    expect(configured.map((p) => p.id)).toEqual(["deepseek", "qwen"]);
  });

  it("routes factual intents to a chinaFacts specialist first", () => {
    const configured = getConfiguredProviders({
      DEEPSEEK_API_KEY: "k",
      ERNIE_API_KEY: "k",
    });
    const ranked = selectProvidersForIntent("ask_factual", configured);
    expect(ranked[0].id).toBe("ernie"); // chinaFacts capability wins for ask_factual
  });

  it("routes recommendation intents to a chinese specialist first", () => {
    const configured = getConfiguredProviders({ DEEPSEEK_API_KEY: "k", DASHSCOPE_API_KEY: "k" });
    const ranked = selectProvidersForIntent("ask_recommendation", configured);
    expect(ranked[0].id).toBe("qwen"); // chinese capability wins for ask_recommendation
  });

  it("flags high-stakes intents", () => {
    expect(isHighStakesIntent("create_trip")).toBe(true);
    expect(isHighStakesIntent("ask_factual")).toBe(true);
    expect(isHighStakesIntent("adjust_trip")).toBe(false);
  });
});
