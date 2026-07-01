export interface EnvironmentStatus {
  key: string;
  configured: boolean;
  purpose: string;
}

const ENVIRONMENT_KEYS: Array<{ key: string; purpose: string }> = [
  { key: "NEXT_PUBLIC_SUPABASE_URL", purpose: "Future Supabase project URL" },
  { key: "NEXT_PUBLIC_SUPABASE_ANON_KEY", purpose: "Future Supabase browser key" },
  { key: "SUPABASE_SERVICE_ROLE_KEY", purpose: "Future server-side Supabase writes" },
  { key: "DEEPSEEK_API_KEY", purpose: "DeepSeek provider key for V4 Flash (Butler reasoning)" },
  { key: "DEEPSEEK_BASE_URL", purpose: "Optional DeepSeek-compatible endpoint override" },
  { key: "DEEPSEEK_MODEL", purpose: "Optional DeepSeek model override, defaults to deepseek-v4-flash" },
  { key: "QWEN_CHAT_MODEL", purpose: "Optional Butler chat model for Qwen/DashScope, defaults to qwen3.6-flash" },
  { key: "ZHIPU_API_KEY", purpose: "Zhipu GLM provider key for Butler orchestration (server-side only)" },
  { key: "ZHIPU_BASE_URL", purpose: "Optional Zhipu endpoint override" },
  { key: "ZHIPU_CHAT_MODEL", purpose: "Optional Zhipu model override, defaults to glm-5" },
  { key: "MOONSHOT_API_KEY", purpose: "Moonshot Kimi provider key for Butler orchestration (server-side only)" },
  { key: "MOONSHOT_BASE_URL", purpose: "Optional Moonshot endpoint override" },
  { key: "MOONSHOT_CHAT_MODEL", purpose: "Optional Moonshot model override, defaults to kimi-2.5" },
  { key: "ERNIE_API_KEY", purpose: "Baidu ERNIE (Qianfan) provider key for Butler orchestration (server-side only)" },
  { key: "ERNIE_BASE_URL", purpose: "Optional Baidu Qianfan endpoint override" },
  { key: "ERNIE_CHAT_MODEL", purpose: "Optional ERNIE model override, defaults to ernie-4.5-turbo-128k" },
  { key: "MINIMAX_API_KEY", purpose: "MiniMax provider key for Butler orchestration (server-side only)" },
  { key: "MINIMAX_BASE_URL", purpose: "Optional MiniMax endpoint override" },
  { key: "MINIMAX_CHAT_MODEL", purpose: "Optional MiniMax model override, defaults to abab6.5s-chat" },
  { key: "DASHSCOPE_API_KEY", purpose: "Aliyun Bailian DashScope API key for Qwen Translator text/OCR/TTS/STT" },
  { key: "ALIYUN_BAILIAN_API_KEY", purpose: "Optional alias for DASHSCOPE_API_KEY" },
  { key: "DASHSCOPE_COMPATIBLE_BASE_URL", purpose: "Optional Aliyun Bailian OpenAI-compatible endpoint override" },
  { key: "DASHSCOPE_BASE_URL", purpose: "Optional Aliyun Bailian DashScope HTTP endpoint override for TTS" },
  { key: "QWEN_TRANSLATE_MODEL", purpose: "Optional Translator text model override, defaults to qwen-mt-flash" },
  { key: "QWEN_OCR_MODEL", purpose: "Optional Translator OCR model override, defaults to qwen3.5-ocr" },
  { key: "QWEN_TTS_MODEL", purpose: "Optional Translator TTS model override, defaults to qwen3-tts-instruct-flash" },
  { key: "QWEN_STT_MODEL", purpose: "Optional Translator STT model override, defaults to qwen3-asr-flash" },
  { key: "AMAP_API_KEY", purpose: "Future map and POI provider" },
  { key: "CTRIP_AID", purpose: "Future Trip.com affiliate link generation" },
  { key: "CTRIP_SID", purpose: "Future Trip.com sub-affiliate link generation" },
  { key: "MEITUAN_UNION_API_KEY", purpose: "Future Meituan Union API" },
  { key: "MEITUAN_UNION_API_SECRET", purpose: "Future Meituan Union signing secret" },
];

export function getEnvironmentStatus(env: NodeJS.ProcessEnv = process.env): EnvironmentStatus[] {
  return ENVIRONMENT_KEYS.map((item) => ({
    ...item,
    configured: Boolean(env[item.key]?.trim()),
  }));
}
