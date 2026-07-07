export const ALIYUN_PROVIDER = "aliyun-bailian";

export const QWEN_MODELS = {
  translate: "qwen3.7-plus",
  ocr: "qwen3.7-plus",
  tts: "qwen3-tts-instruct-flash",
  stt: "qwen3-asr-flash",
} as const;

const DEFAULT_COMPATIBLE_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1";
const DEFAULT_DASHSCOPE_BASE_URL = "https://dashscope.aliyuncs.com/api/v1";

type ChatMessageContent =
  | string
  | Array<
      | { type: "text"; text: string }
      | { type: "image_url"; image_url: { url: string }; min_pixels?: number; max_pixels?: number }
      | { type: "input_audio"; input_audio: { data: string } }
    >;

type ChatMessage = {
  role: "system" | "user" | "assistant";
  content: ChatMessageContent;
};

export function getQwenApiKey() {
  return process.env.DASHSCOPE_API_KEY?.trim() || process.env.ALIYUN_BAILIAN_API_KEY?.trim() || "";
}

export function getQwenCompatibleUrl(path: string) {
  const baseUrl = (process.env.DASHSCOPE_COMPATIBLE_BASE_URL?.trim() || DEFAULT_COMPATIBLE_BASE_URL).replace(/\/$/, "");
  return `${baseUrl}${path}`;
}

export function getDashScopeUrl(path: string) {
  const baseUrl = (process.env.DASHSCOPE_BASE_URL?.trim() || DEFAULT_DASHSCOPE_BASE_URL).replace(/\/$/, "");
  return `${baseUrl}${path}`;
}

export async function callQwenChatCompletions(input: {
  model: string;
  messages: ChatMessage[];
  temperature?: number;
  responseFormatJson?: boolean;
  extraBody?: Record<string, unknown>;
}) {
  const apiKey = getQwenApiKey();
  if (!apiKey) throw new Error("not_configured");

  const body: Record<string, unknown> = {
    model: input.model,
    messages: input.messages,
    temperature: input.temperature ?? 0.1,
    stream: false,
    ...input.extraBody,
  };
  if (input.responseFormatJson) body.response_format = { type: "json_object" };

  const res = await fetch(getQwenCompatibleUrl("/chat/completions"), {
    method: "POST",
    headers: { "Content-Type": "application/json", Authorization: `Bearer ${apiKey}` },
    body: JSON.stringify(body),
  });

  if (!res.ok) {
    throw new Error("upstream_error");
  }

  const data = await res.json();
  return String(data.choices?.[0]?.message?.content ?? "").trim();
}

export function parseJsonObject(content: string): Record<string, unknown> {
  try {
    const parsed = JSON.parse(content);
    return parsed && typeof parsed === "object" && !Array.isArray(parsed) ? parsed : {};
  } catch {
    const match = content.match(/\{[\s\S]*\}/);
    if (!match) return {};
    try {
      const parsed = JSON.parse(match[0]);
      return parsed && typeof parsed === "object" && !Array.isArray(parsed) ? parsed : {};
    } catch {
      return {};
    }
  }
}

export function normalizeDataUrl(data: string, mimeType: string) {
  return data.startsWith("data:") ? data : `data:${mimeType};base64,${data}`;
}
