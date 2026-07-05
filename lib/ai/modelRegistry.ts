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
    // deepseek-v4-flash is also a hybrid-reasoning model — same shape as
    // Qwen/GLM (v0.3.18): thinking on by default, verified via real key
    // (2026-07-05) that a small max_tokens burns the whole budget on
    // reasoning_content and returns empty content with finish_reason:"length".
    // The existing per-intent budgets (1400/4096) are large enough that this
    // was never a production outage, but disabling thinking (same
    // thinking:{type:"disabled"} shape as Zhipu) is strictly faster/cheaper
    // with no quality loss for structured CanvasPatch generation.
    extraBody: { thinking: { type: "disabled" } },
  }),
  createOpenAiCompatibleProvider({
    id: "qwen",
    label: "Qwen (Aliyun Bailian)",
    capabilities: ["chinese", "reasoning", "vision"],
    apiKeyEnv: "DASHSCOPE_API_KEY",
    apiKeyEnvAliases: ["ALIYUN_BAILIAN_API_KEY"],
    defaultBaseUrl: "https://dashscope.aliyuncs.com/compatible-mode/v1",
    baseUrlEnv: "DASHSCOPE_COMPATIBLE_BASE_URL",
    defaultModel: "qwen3.6-flash",
    modelEnv: "QWEN_CHAT_MODEL",
    // Hybrid-reasoning Qwen models default to thinking mode, which burns the
    // whole latency budget on reasoning tokens for itinerary-sized JSON
    // (observed: 25s timeout with empty content). Butler patches want fast,
    // direct output — measured 1.9s vs timeout with this off (v0.3.18).
    extraBody: { enable_thinking: false },
  }),
  createOpenAiCompatibleProvider({
    id: "zhipu",
    label: "Zhipu GLM",
    capabilities: ["reasoning", "judge", "longContext"],
    apiKeyEnv: "ZHIPU_API_KEY",
    apiKeyEnvAliases: ["GLM_API_KEY"],
    defaultBaseUrl: "https://open.bigmodel.cn/api/paas/v4",
    baseUrlEnv: "ZHIPU_BASE_URL",
    defaultModel: "glm-5.2",
    modelEnv: "ZHIPU_CHAT_MODEL",
    // Same rationale as qwen: GLM-5.x reasons by default; disabled = 6.6s
    // direct JSON vs 25s timeout (v0.3.18).
    extraBody: { thinking: { type: "disabled" } },
  }),
  createOpenAiCompatibleProvider({
    id: "moonshot",
    label: "Moonshot Kimi",
    capabilities: ["longContext", "reasoning"],
    apiKeyEnv: "MOONSHOT_API_KEY",
    apiKeyEnvAliases: ["KIMI_API_KEY"],
    defaultBaseUrl: "https://api.moonshot.cn/v1",
    baseUrlEnv: "MOONSHOT_BASE_URL",
    defaultModel: "kimi-k2.6",
    modelEnv: "MOONSHOT_CHAT_MODEL",
    // kimi-k2.x rejects any temperature except 1 with HTTP 400 ("invalid
    // temperature: only 1 is allowed for this model", measured v0.3.18).
    // extraBody is spread after the default temperature so this wins.
    extraBody: { temperature: 1 },
    // kimi-k2.6 is a genuine reasoning model — verified with a real key
    // (2026-07-05) that none of the usual disable-thinking parameters
    // actually suppress its reasoning pass, and its reasoning_content shares
    // the same max_tokens ceiling as content. On the actual (long) butler
    // system prompt, measured real completions ranged from 61.5s up to
    // timing out past 75s across repeated runs — the reasoning pass length is
    // highly variable and scales unpredictably with prompt complexity, not a
    // single fixed constant. Rather than chase this indefinitely, the floor
    // is set generously (90s) and this variance is accepted as a known
    // characteristic: Kimi is always raced behind 3 faster providers
    // (DeepSeek/Qwen/Zhipu, 7-27s), so this latency is normally invisible; it
    // only surfaces if Kimi becomes the last surviving candidate, in which
    // case a slow real answer (or an honest timeout per ADR-120) still beats
    // a fake instant mock.
    minTimeoutMs: 90000,
    minMaxTokens: 8192,
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
