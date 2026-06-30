export interface EnvironmentStatus {
  key: string;
  configured: boolean;
  purpose: string;
}

const ENVIRONMENT_KEYS: Array<{ key: string; purpose: string }> = [
  { key: "NEXT_PUBLIC_SUPABASE_URL", purpose: "Future Supabase project URL" },
  { key: "NEXT_PUBLIC_SUPABASE_ANON_KEY", purpose: "Future Supabase browser key" },
  { key: "SUPABASE_SERVICE_ROLE_KEY", purpose: "Future server-side Supabase writes" },
  { key: "DEEPSEEK_API_KEY", purpose: "DeepSeek provider key for V4 Flash" },
  { key: "DEEPSEEK_BASE_URL", purpose: "Optional DeepSeek-compatible endpoint override" },
  { key: "DEEPSEEK_MODEL", purpose: "Optional DeepSeek model override, defaults to deepseek-v4-flash" },
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
