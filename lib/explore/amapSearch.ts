import type { ExploreRichMeta } from "@/lib/explore/types";
import type { FetchLike } from "@/lib/ai/providers/types";

export const AMAP_CITY_MAP: Record<string, string> = {
  beijing: "北京",
  shanghai: "上海",
  chengdu: "成都",
  xian: "西安",
  guangzhou: "广州",
  hangzhou: "杭州",
  suzhou: "苏州",
  chongqing: "重庆",
  nanjing: "南京",
};

// Category config for Amap POI search. Every code below was verified against
// the real Amap API in Chengdu on 2026-07-05 (Issue #46) — the official code
// table is easy to misread (e.g. 050110 is Fujian cuisine, NOT hotpot; hotpot
// is 050117; bath/massage venues are 071400 under 生活服务, not 08xxxx).
// Subcategories with no precise code use a broad code + keyword refinement
// (bbq, specialty shopping, bath/spa variants) — also verified live.
export interface AmapCategoryConfig {
  /** Amap POI type codes, multiple joined with `|`. */
  types: string;
  /** Keyword refinement when a category has no precise type code. */
  keywords?: string;
}

export const AMAP_TYPE_MAP: Record<string, AmapCategoryConfig> = {
  // Top-level categories (Dianping-style Explore, spec §2.2). "stays" is the
  // legacy alias for hotels kept for pre-redesign mobile clients.
  food: { types: "050000" },
  attractions: { types: "110000" },
  hotels: { types: "100000" },
  stays: { types: "100000" },
  shopping: { types: "060000" },
  experiences: { types: "071400|080300|080500|050600" },

  // Food subcategories.
  "food.hotpot": { types: "050117" },
  "food.sichuan": { types: "050102" },
  "food.cantonese": { types: "050103" },
  "food.japanese": { types: "050202" },
  "food.bbq": { types: "050000", keywords: "烧烤" },
  "food.dessert": { types: "050900" },
  "food.fastfood": { types: "050300" },
  "food.cafe": { types: "050500" },

  // Attraction subcategories.
  "attractions.scenic": { types: "110200" },
  "attractions.park": { types: "110100" },
  "attractions.museum": { types: "140100" },
  "attractions.temple": { types: "110205" },

  // Hotel subcategories.
  "hotels.star": { types: "100100" },
  "hotels.economy": { types: "100105" },
  "hotels.hostel": { types: "100200" },

  // Shopping subcategories.
  "shopping.mall": { types: "060100" },
  "shopping.specialty": { types: "060000", keywords: "特产" },
  "shopping.supermarket": { types: "060400" },

  // Experience subcategories (bath/massage/spa share Amap code 071400 —
  // differentiated by keyword, verified to return distinct venue sets).
  "experiences.massage": { types: "071400" },
  "experiences.bath": { types: "071400", keywords: "洗浴汗蒸" },
  "experiences.spa": { types: "071400", keywords: "SPA" },
  "experiences.teahouse": { types: "050600" },
  "experiences.ktv": { types: "080302" },
};

export interface AmapPoi {
  id: string;
  name: string;
  type: string;
  address?: string | string[];
  adname?: string;
  biz_type?: string;
  tel?: string | string[];
  opentime_week?: string;
  business_area?: string;
  location?: string;
  biz_ext?: {
    rating?: string;
    cost?: string;
  } | [];
  photos?: Array<{ url?: string; title?: string }> | [];
}

interface SearchAmapPoisInput {
  cityId: string;
  type: string;
  keyword?: string;
  /**
   * "city" (default) searches the whole city by text; "around" searches near
   * `location` within `radius` meters — the data layer behind the
   * Dianping-style 附近 filter (Issue #46).
   */
  mode?: "city" | "around";
  /** "lng,lat" — required for mode "around". */
  location?: string;
  /** Meters, clamped to Amap's 500–50000 supported range. */
  radius?: number;
  /** "weight" (Amap relevance, default) or "distance" (around mode only). */
  sort?: "weight" | "distance";
  /** 1-based page for infinite scroll. */
  page?: number;
  env?: Record<string, string | undefined>;
  fetchImpl?: FetchLike;
}

export interface AmapSearchResult {
  pois: AmapPoi[];
  /** True when Amap reports more results beyond this page. */
  hasMore: boolean;
}

const PAGE_SIZE = 20;

/** "lng,lat" within plausible China bounds; rejects anything else. */
export function isValidLngLat(value: string | undefined): value is string {
  if (!value) return false;
  const [lngRaw, latRaw, extra] = value.split(",");
  if (extra !== undefined) return false;
  const lng = Number(lngRaw);
  const lat = Number(latRaw);
  return Number.isFinite(lng) && Number.isFinite(lat) && lng >= 70 && lng <= 140 && lat >= 15 && lat <= 55;
}

function scalar(value: string | string[] | undefined): string | undefined {
  if (Array.isArray(value)) return value.find(Boolean);
  return value || undefined;
}

function priceLevel(cost?: string): ExploreRichMeta["priceLevel"] {
  const amount = Number(cost);
  if (!Number.isFinite(amount) || amount <= 0) return undefined;
  if (amount < 80) return "¥";
  if (amount < 220) return "¥¥";
  return "¥¥¥";
}

export function amapPoiRichMeta(poi: AmapPoi): ExploreRichMeta {
  const bizExt = poi.biz_ext && !Array.isArray(poi.biz_ext) ? poi.biz_ext : undefined;
  const photoUrl = Array.isArray(poi.photos) ? poi.photos.find((photo) => photo.url)?.url : undefined;
  const [lngRaw, latRaw] = poi.location?.split(",") ?? [];
  const lng = Number(lngRaw);
  const lat = Number(latRaw);

  return {
    rating: bizExt?.rating,
    pricePerPerson: bizExt?.cost,
    priceLevel: priceLevel(bizExt?.cost),
    tel: scalar(poi.tel),
    openHours: poi.opentime_week,
    photoUrl,
    businessArea: poi.business_area,
    sourceLabel: "Amap",
    location: Number.isFinite(lat) && Number.isFinite(lng) ? { lat, lng } : undefined,
  };
}

export async function searchAmapPoisPaged(input: SearchAmapPoisInput): Promise<AmapSearchResult> {
  const env = input.env ?? (process.env as Record<string, string | undefined>);
  const apiKey = env.AMAP_API_KEY?.trim();
  if (!apiKey) return { pois: [], hasMore: false };

  const cityName = AMAP_CITY_MAP[input.cityId];
  const category = AMAP_TYPE_MAP[input.type];
  if (!cityName || !category) return { pois: [], hasMore: false };

  const around = input.mode === "around" && isValidLngLat(input.location);
  const page = Math.max(1, Math.floor(input.page ?? 1));

  const url = new URL(
    around ? "https://restapi.amap.com/v3/place/around" : "https://restapi.amap.com/v3/place/text",
  );
  url.searchParams.set("key", apiKey);
  url.searchParams.set("types", category.types);
  url.searchParams.set("offset", String(PAGE_SIZE));
  url.searchParams.set("page", String(page));
  url.searchParams.set("extensions", "all");
  url.searchParams.set("output", "json");

  if (around) {
    url.searchParams.set("location", input.location!);
    const radius = Math.min(50000, Math.max(500, Math.floor(input.radius ?? 3000)));
    url.searchParams.set("radius", String(radius));
    // Around search still constrains to the city so a location near a city
    // border can't leak results from an unsupported city.
    url.searchParams.set("city", cityName);
    url.searchParams.set("sortrule", input.sort === "distance" ? "distance" : "weight");
  } else {
    url.searchParams.set("city", cityName);
  }

  // Category keyword (e.g. bbq → 烧烤) and the traveler's own keyword compose;
  // Amap treats multiple keywords joined with | as OR-ish refinement.
  const keywords = [category.keywords, input.keyword?.trim()].filter(Boolean).join("|");
  if (keywords) url.searchParams.set("keywords", keywords);

  const fetchImpl = input.fetchImpl ?? fetch;
  const res = await fetchImpl(url.toString(), { next: { revalidate: 3600 } });
  if (!res.ok) return { pois: [], hasMore: false };

  const data = await res.json();
  if (data.status !== "1") return { pois: [], hasMore: false };
  const pois = Array.isArray(data.pois) ? (data.pois as AmapPoi[]) : [];
  const total = Number(data.count);
  const hasMore = Number.isFinite(total) ? page * PAGE_SIZE < total : pois.length === PAGE_SIZE;
  return { pois, hasMore };
}

/** Legacy single-page helper — pre-#46 signature kept for existing callers (toolContext). */
export async function searchAmapPois(input: SearchAmapPoisInput): Promise<AmapPoi[]> {
  const result = await searchAmapPoisPaged(input);
  return result.pois;
}
