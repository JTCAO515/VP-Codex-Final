// Generic provider for OpenAI-compatible chat-completions endpoints.
// DeepSeek, Qwen (DashScope compatible-mode), Zhipu GLM, Moonshot Kimi,
// Baidu Qianfan (ERNIE), and MiniMax all expose an OpenAI-shaped
// POST {baseUrl}/chat/completions with `Authorization: Bearer <key>`, so a
// single implementation covers every provider. Provider-specific defaults
// (base URL, model, key env name) come from the model registry.

import type {
  ChatCompletionOptions,
  ChatCompletionProvider,
  ChatCompletionResult,
  ProviderCapability,
  ProviderContext,
} from "@/lib/ai/providers/types";

export interface OpenAiCompatibleConfig {
  id: string;
  label: string;
  capabilities: ProviderCapability[];
  /** Primary env var holding the API key. */
  apiKeyEnv: string;
  /** Optional fallback env var names for the key (aliases). */
  apiKeyEnvAliases?: string[];
  /** Default base URL (no trailing slash, no /chat/completions suffix). */
  defaultBaseUrl: string;
  /** Optional env var to override the base URL. */
  baseUrlEnv?: string;
  /** Default model id. */
  defaultModel: string;
  /** Optional env var to override the model. */
  modelEnv?: string;
  /**
   * Vendor-specific extra body fields merged into every request. v0.3.18: used
   * to disable reasoning/thinking on hybrid-reasoning models (Qwen
   * `enable_thinking:false`, Zhipu `thinking.type:disabled`) — with thinking
   * on, itinerary-sized JSON completions burned the whole time budget on
   * reasoning tokens (25s timeouts observed with empty content). Structured
   * patch generation wants fast, direct output; the system prompt already
   * carries the planning rules.
   */
  extraBody?: Record<string, unknown>;
  /**
   * Vendor-enforced floor on the request timeout, regardless of what the
   * orchestrator's per-intent budget requests. v0.3.19: Moonshot Kimi-K2.6 is
   * a genuine reasoning model — verified with a real key (2026-07-05) that no
   * combination of `thinking:{type:"disabled"}` / `enable_thinking:false` /
   * `reasoning_effort:"minimal"` actually suppresses its reasoning pass (some
   * are silently ignored, one changes the model's accepted temperature and
   * 400s). A real itinerary-sized completion took 33.5s end-to-end. Racing it
   * against faster providers is fine (a healthy faster answer still wins),
   * but it must not be timed out early when it's the only/last candidate.
   */
  minTimeoutMs?: number;
  /**
   * Vendor-enforced floor on max_tokens, regardless of the orchestrator's
   * per-intent token budget. v0.3.19: Moonshot Kimi-K2.6's reasoning pass
   * cannot be disabled and its reasoning_content shares the same max_tokens
   * ceiling as content — verified with a real key (2026-07-05) that the
   * itinerary budget (4096) occasionally left zero room for content after a
   * long reasoning pass on the real (much longer) system prompt, producing
   * "response did not include message content." A real request with
   * max_tokens=8192 completed with room to spare (1511 completion tokens,
   * 1304 of them reasoning). Floor it there instead of fighting the model.
   */
  minMaxTokens?: number;
}

// Hard ceiling on a single provider call. A slow or misconfigured model must
// fail fast so the orchestrator can race/fall back instead of hanging the chat.
const DEFAULT_TIMEOUT_MS = 18000;

function readKey(config: OpenAiCompatibleConfig, env: Record<string, string | undefined>): string | undefined {
  const names = [config.apiKeyEnv, ...(config.apiKeyEnvAliases ?? [])];
  for (const name of names) {
    const value = env[name]?.trim();
    if (value) return value;
  }
  return undefined;
}

export function createOpenAiCompatibleProvider(config: OpenAiCompatibleConfig): ChatCompletionProvider {
  const model = config.defaultModel;

  return {
    id: config.id,
    label: config.label,
    model,
    capabilities: config.capabilities,

    isConfigured(env) {
      return Boolean(readKey(config, env));
    },

    async complete(options: ChatCompletionOptions, ctx: ProviderContext): Promise<ChatCompletionResult> {
      const apiKey = readKey(config, ctx.env);
      if (!apiKey) {
        throw new Error(`${config.label}: API key (${config.apiKeyEnv}) is not configured.`);
      }

      const baseUrl = (
        (config.baseUrlEnv ? ctx.env[config.baseUrlEnv]?.trim() : undefined) || config.defaultBaseUrl
      ).replace(/\/$/, "");
      const resolvedModel =
        (config.modelEnv ? ctx.env[config.modelEnv]?.trim() : undefined) || config.defaultModel;

      const body: Record<string, unknown> = {
        model: resolvedModel,
        messages: options.messages,
        max_tokens: Math.max(options.maxTokens ?? 2200, config.minMaxTokens ?? 0),
        // jsonMode implies a structured contract — keep sampling tight so the
        // shape stays stable; free-text replies keep the slightly warmer 0.4.
        temperature: options.temperature ?? (options.jsonMode ? 0.3 : 0.4),
        ...config.extraBody,
      };
      if (options.jsonMode) {
        body.response_format = { type: "json_object" };
      }

      const timeoutMs = Math.max(options.timeoutMs ?? DEFAULT_TIMEOUT_MS, config.minTimeoutMs ?? 0);
      const controller = new AbortController();
      const timer = setTimeout(() => controller.abort(), timeoutMs);
      if (options.signal) {
        if (options.signal.aborted) controller.abort();
        else options.signal.addEventListener("abort", () => controller.abort(), { once: true });
      }

      let response: Response;
      try {
        response = await ctx.fetchImpl(`${baseUrl}/chat/completions`, {
          method: "POST",
          headers: {
            Authorization: `Bearer ${apiKey}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify(body),
          signal: controller.signal,
        });
      } catch (error) {
        if (controller.signal.aborted) {
          throw new Error(`${config.label}: timed out after ${timeoutMs}ms.`);
        }
        throw error instanceof Error ? error : new Error(`${config.label}: request failed.`);
      } finally {
        clearTimeout(timer);
      }

      if (!response.ok) {
        throw new Error(`${config.label}: HTTP ${response.status}.`);
      }

      const data = await response.json();
      const choice = data?.choices?.[0];
      const content = choice?.message?.content;
      if (typeof content !== "string" || !content.trim()) {
        throw new Error(`${config.label}: response did not include message content.`);
      }

      const finishReason = typeof choice?.finish_reason === "string" ? choice.finish_reason : undefined;
      return { content, providerId: config.id, model: resolvedModel, finishReason };
    },
  };
}
