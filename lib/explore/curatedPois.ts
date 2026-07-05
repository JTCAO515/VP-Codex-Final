import type { FetchLike } from "@/lib/ai/providers/types";

// VisePanda Editorial overlay (Issue #49) — read-only PostgREST access to
// public.curated_pois. Uses the anon key (table has a public-read RLS
// policy, see supabase/migrations/0006_curated_pois.sql) via a plain fetch,
// matching the style of lib/explore/amapSearch.ts rather than pulling in the
// full supabase-js client for a single read query.

export interface CuratedPoi {
  city_id: string;
  category: string;
  amap_poi_id: string;
  name: string;
  name_en: string | null;
  editorial_summary: string | null;
  tags: string[];
  list_badges: string[];
  photo_url: string | null;
  rank: number;
  source: "wikivoyage" | "official_list" | "llm_seed";
  source_url: string | null;
}

interface CuratedPoisInput {
  cityId: string;
  category?: string;
  env?: Record<string, string | undefined>;
  fetchImpl?: FetchLike;
}

function restBase(env: Record<string, string | undefined>): string | undefined {
  const url = env.NEXT_PUBLIC_SUPABASE_URL?.trim();
  return url ? `${url.replace(/\/+$/, "")}/rest/v1` : undefined;
}

/** All curated entries for a city (optionally scoped to one category), ranked. */
export async function listCuratedPois(input: CuratedPoisInput): Promise<CuratedPoi[]> {
  const env = input.env ?? (process.env as Record<string, string | undefined>);
  const anonKey = env.NEXT_PUBLIC_SUPABASE_ANON_KEY?.trim();
  const base = restBase(env);
  if (!anonKey || !base) return [];

  const url = new URL(`${base}/curated_pois`);
  url.searchParams.set("city_id", `eq.${input.cityId}`);
  if (input.category) url.searchParams.set("category", `eq.${input.category}`);
  url.searchParams.set("order", "rank.asc");
  url.searchParams.set("select", "*");

  const fetchImpl = input.fetchImpl ?? fetch;
  const res = await fetchImpl(url.toString(), {
    headers: { apikey: anonKey, Authorization: `Bearer ${anonKey}` },
    next: { revalidate: 3600 },
  });
  if (!res.ok) return [];

  const rows = await res.json();
  return Array.isArray(rows) ? (rows as CuratedPoi[]) : [];
}

/** Map amap_poi_id → curated entry, for merging into live Amap search results. */
export async function curatedPoiIndex(input: CuratedPoisInput): Promise<Map<string, CuratedPoi>> {
  const rows = await listCuratedPois(input);
  return new Map(rows.map((row) => [row.amap_poi_id, row]));
}
