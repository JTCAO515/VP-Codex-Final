// Shared provider abstraction for the multi-model Chinese LLM orchestrator (阶段十三).
// Every Chinese LLM (DeepSeek, Qwen, GLM, Kimi, ERNIE, MiniMax) is wrapped behind
// this single interface so the orchestrator stays provider-agnostic. All keys are
// read from server-side env only; nothing here runs in the browser.

export type ProviderCapability =
  | "reasoning" // itinerary planning, structured JSON
  | "chinese" // Chinese POI / menu / place comprehension
  | "longContext" // full trip + full chat history summarization
  | "vision" // image understanding
  | "chinaFacts" // China-specific rules (visa, transit, regulations)
  | "judge"; // reconcile/verify other models' answers

export interface ChatCompletionMessage {
  role: "system" | "user" | "assistant";
  content: string;
}

export interface ChatCompletionOptions {
  messages: ChatCompletionMessage[];
  maxTokens?: number;
  temperature?: number;
  jsonMode?: boolean;
  signal?: AbortSignal;
  /** Per-call hard ceiling; defaults to the provider's own DEFAULT_TIMEOUT_MS. */
  timeoutMs?: number;
}

export interface ChatCompletionResult {
  content: string;
  providerId: string;
  model: string;
  /** Upstream finish_reason when reported — "length" signals a truncated completion. */
  finishReason?: string;
}

export type FetchLike = typeof fetch;

export interface ChatCompletionProvider {
  id: string;
  label: string;
  model: string;
  capabilities: ProviderCapability[];
  /** True only when this provider's API key is present in the given env. */
  isConfigured(env: Record<string, string | undefined>): boolean;
  complete(options: ChatCompletionOptions, ctx: ProviderContext): Promise<ChatCompletionResult>;
}

export interface ProviderContext {
  env: Record<string, string | undefined>;
  fetchImpl: FetchLike;
}
