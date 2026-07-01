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
}

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
        max_tokens: options.maxTokens ?? 2200,
        temperature: options.temperature ?? 0.4,
      };
      if (options.jsonMode) {
        body.response_format = { type: "json_object" };
      }

      const response = await ctx.fetchImpl(`${baseUrl}/chat/completions`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${apiKey}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(body),
        signal: options.signal,
      });

      if (!response.ok) {
        throw new Error(`${config.label}: HTTP ${response.status}.`);
      }

      const data = await response.json();
      const content = data?.choices?.[0]?.message?.content;
      if (typeof content !== "string" || !content.trim()) {
        throw new Error(`${config.label}: response did not include message content.`);
      }

      return { content, providerId: config.id, model: resolvedModel };
    },
  };
}
