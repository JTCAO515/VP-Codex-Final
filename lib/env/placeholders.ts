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
