// Registry of Chinese LLM providers for the Butler orchestrator (阶段十三).
//
// Guiding principle (v0.1.46 / ADR-043): optimize for answer quality, not token
// cost. The registry lets the orchestrator route each request to the strongest
// available specialist and run multi-model ensembles for high-stakes answers.
//
// Every provider is OpenAI-compatible, so all six share one implementation and
// differ only by base URL / model / key env. Base URLs and default models can be
// overridden per-deployment via the *_BASE_URL / *_MODEL env vars documented in
// lib/env/placeholders.ts. All keys are server-side only.

import { createOpenAiCompatibleProvider } from "@/lib/ai/providers/openaiCompatibleProvider";
import type { ButlerIntent } from "@/lib/ai/intentClassifier";
import type { ChatCompletionProvider, ProviderCapability } from "@/lib/ai/providers/types";

export const BUTLER_PROVIDERS: ChatCompletionProvider[] = [
  createOpenAiCompatibleProvider({
    id: "deepseek",
    label: "DeepSeek v4 flash",
    capabilities: ["reasoning"],
    apiKeyEnv: "DEEPSEEK_API_KEY",
    apiKeyEnvAliases: ["AI_API_KEY"],
    defaultBaseUrl: "https://api.deepseek.com",
    baseUrlEnv: "DEEPSEEK_BASE_URL",
    defaultModel: "deepseek-v4-flash",
    modelEnv: "DEEPSEEK_MODEL",
  }),
  createOpenAiCompatibleProvider({
    id: "qwen",
    label: "Qwen 3.6 Flash (Aliyun Bailian)",
    capabilities: ["chinese", "reasoning", "vision"],
    apiKeyEnv: "DASHSCOPE_API_KEY",
    apiKeyEnvAliases: ["ALIYUN_BAILIAN_API_KEY"],
    defaultBaseUrl: "https://dashscope.aliyuncs.com/compatible-mode/v1",
    baseUrlEnv: "DASHSCOPE_COMPATIBLE_BASE_URL",
    defaultModel: "qwen3.6-flash",
    modelEnv: "QWEN_CHAT_MODEL",
  }),
  createOpenAiCompatibleProvider({
    id: "zhipu",
    label: "Zhipu GLM5",
    capabilities: ["reasoning", "judge", "longContext"],
    apiKeyEnv: "ZHIPU_API_KEY",
    apiKeyEnvAliases: ["GLM_API_KEY"],
    defaultBaseUrl: "https://open.bigmodel.cn/api/paas/v4",
    baseUrlEnv: "ZHIPU_BASE_URL",
    defaultModel: "glm-5.1",
    modelEnv: "ZHIPU_CHAT_MODEL",
  }),
  createOpenAiCompatibleProvider({
    id: "moonshot",
    label: "Moonshot Kimi 2.5",
    capabilities: ["longContext", "reasoning"],
    apiKeyEnv: "MOONSHOT_API_KEY",
    apiKeyEnvAliases: ["KIMI_API_KEY"],
    defaultBaseUrl: "https://api.moonshot.cn/v1",
    baseUrlEnv: "MOONSHOT_BASE_URL",
    defaultModel: "kimi-2.5",
    modelEnv: "MOONSHOT_CHAT_MODEL",
  }),
  createOpenAiCompatibleProvider({
    id: "ernie",
    label: "Baidu ERNIE",
    capabilities: ["chinaFacts", "chinese"],
    apiKeyEnv: "ERNIE_API_KEY",
    apiKeyEnvAliases: ["QIANFAN_API_KEY", "BAIDU_API_KEY"],
    defaultBaseUrl: "https://qianfan.baidubce.com/v2",
    baseUrlEnv: "ERNIE_BASE_URL",
    defaultModel: "ernie-4.5-turbo-128k",
    modelEnv: "ERNIE_CHAT_MODEL",
  }),
  createOpenAiCompatibleProvider({
    id: "minimax",
    label: "MiniMax",
    capabilities: ["judge", "reasoning"],
    apiKeyEnv: "MINIMAX_API_KEY",
    defaultBaseUrl: "https://api.minimax.chat/v1",
    baseUrlEnv: "MINIMAX_BASE_URL",
    defaultModel: "abab6.5s-chat",
    modelEnv: "MINIMAX_CHAT_MODEL",
  }),
];

/** Providers whose API key is present in the given env, in registry order. */
export function getConfiguredProviders(
  env: Record<string, string | undefined>,
  providers: ChatCompletionProvider[] = BUTLER_PROVIDERS,
): ChatCompletionProvider[] {
  return providers.filter((provider) => provider.isConfigured(env));
}

// Which capability each intent most wants, in priority order. The router picks
// the first configured provider that has an earlier-listed capability; if none
// match, it falls through to any configured provider (registry order).
const INTENT_CAPABILITY_PRIORITY: Record<ButlerIntent, ProviderCapability[]> = {
  create_trip: ["reasoning", "chinese", "longContext"],
  adjust_trip: ["reasoning", "chinese"],
  add_location: ["reasoning", "chinese"],
  add_poi: ["chinese", "reasoning"],
  ask_recommendation: ["chinese", "chinaFacts", "reasoning"],
  ask_factual: ["chinaFacts", "chinese", "reasoning"],
  concern: ["chinaFacts", "reasoning"],
  logistics: ["reasoning", "chinese"],
  preference_signal: ["reasoning"],
  unclear: ["reasoning", "chinese"],
};

/**
 * Ordered candidate providers for an intent: best specialist first, then the
 * rest of the configured providers as a fallback chain. Deterministic and
 * side-effect free so it is easy to unit test.
 */
export function selectProvidersForIntent(
  intent: ButlerIntent,
  configured: ChatCompletionProvider[],
): ChatCompletionProvider[] {
  const priority = INTENT_CAPABILITY_PRIORITY[intent] ?? ["reasoning"];
  const ranked: ChatCompletionProvider[] = [];
  const seen = new Set<string>();

  for (const capability of priority) {
    for (const provider of configured) {
      if (!seen.has(provider.id) && provider.capabilities.includes(capability)) {
        ranked.push(provider);
        seen.add(provider.id);
      }
    }
  }
  // Append any remaining configured providers as a last-resort fallback.
  for (const provider of configured) {
    if (!seen.has(provider.id)) {
      ranked.push(provider);
      seen.add(provider.id);
    }
  }
  return ranked;
}

// High-stakes intents run a small parallel ensemble (when 2+ providers exist)
// so a slow or wrong primary does not sink the answer. Cost is not a concern.
const HIGH_STAKES_INTENTS: ReadonlySet<ButlerIntent> = new Set<ButlerIntent>([
  "create_trip",
  "ask_factual",
]);

export function isHighStakesIntent(intent: ButlerIntent): boolean {
  return HIGH_STAKES_INTENTS.has(intent);
}
