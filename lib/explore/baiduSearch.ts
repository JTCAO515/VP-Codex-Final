import type { FetchLike } from "@/lib/ai/providers/types";
import { AMAP_CITY_MAP } from "@/lib/explore/amapSearch";
import type { ExploreRichMeta } from "@/lib/explore/types";

export const BAIDU_EXPERIENCE_TYPE_MAP: Record<string, string> = {
  experiences: "按摩 足疗 SPA 茶馆",
  "experiences.massage": "按摩",
  "experiences.bath": "足疗",
  "experiences.spa": "SPA",
  "experiences.teahouse": "茶馆",
};

export interface BaiduPoi {
  uid: string;
  name: string;
  address?: string;
  province?: string;
  city?: string;
  area?: string;
  telephone?: string;
  location?: {
    lat?: number;
    lng?: number;
  };
  detail_info?: {
    tag?: string;
    type?: string;
    overall_rating?: string;
    price?: string;
    image?: string;
    shop_hours?: string;
  };
}

interface SearchBaiduPoisInput {
  cityId: string;
  type: string;
  keyword?: string;
  page?: number;
  env?: Record<string, string | undefined>;
  fetchImpl?: FetchLike;
}

export interface BaiduSearchResult {
  pois: BaiduPoi[];
  hasMore: boolean;
}

const PAGE_SIZE = 20;

function priceLevel(price?: string): ExploreRichMeta["priceLevel"] {
  const amount = Number(price);
  if (!Number.isFinite(amount) || amount <= 0) return undefined;
  if (amount < 80) return "¥";
  if (amount < 220) return "¥¥";
  return "¥¥¥";
}

export function baiduPoiRichMeta(poi: BaiduPoi): ExploreRichMeta {
  const lat = Number(poi.location?.lat);
  const lng = Number(poi.location?.lng);
  return {
    rating: poi.detail_info?.overall_rating,
    pricePerPerson: poi.detail_info?.price,
    priceLevel: priceLevel(poi.detail_info?.price),
    tel: poi.telephone,
    openHours: poi.detail_info?.shop_hours,
    photoUrl: poi.detail_info?.image,
    businessArea: poi.area,
    sourceLabel: "Baidu",
    location: Number.isFinite(lat) && Number.isFinite(lng) ? { lat, lng } : undefined,
  };
}

export async function searchBaiduPoisPaged(input: SearchBaiduPoisInput): Promise<BaiduSearchResult> {
  const env = input.env ?? (process.env as Record<string, string | undefined>);
  const apiKey = env.BAIDU_MAP_AK?.trim();
  if (!apiKey) return { pois: [], hasMore: false };

  const cityName = AMAP_CITY_MAP[input.cityId];
  const categoryKeyword = BAIDU_EXPERIENCE_TYPE_MAP[input.type];
  if (!cityName || !categoryKeyword) return { pois: [], hasMore: false };

  const page = Math.max(1, Math.floor(input.page ?? 1));
  const url = new URL("https://api.map.baidu.com/place/v2/search");
  url.searchParams.set("ak", apiKey);
  url.searchParams.set("query", [categoryKeyword, input.keyword?.trim()].filter(Boolean).join(" "));
  url.searchParams.set("region", cityName);
  url.searchParams.set("city_limit", "true");
  url.searchParams.set("scope", "2");
  url.searchParams.set("output", "json");
  url.searchParams.set("page_size", String(PAGE_SIZE));
  url.searchParams.set("page_num", String(page - 1));

  const fetchImpl = input.fetchImpl ?? fetch;
  const res = await fetchImpl(url.toString(), { next: { revalidate: 3600 } });
  if (!res.ok) return { pois: [], hasMore: false };

  const data = await res.json();
  if (data.status !== 0) return { pois: [], hasMore: false };

  const pois = Array.isArray(data.results) ? (data.results as BaiduPoi[]) : [];
  const total = Number(data.total);
  const seen = (page - 1) * PAGE_SIZE + pois.length;
  const hasMore = Number.isFinite(total) ? seen < total : pois.length === PAGE_SIZE;
  return { pois, hasMore };
}
