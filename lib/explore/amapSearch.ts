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

export const AMAP_TYPE_MAP: Record<string, string> = {
  attractions: "110000",
  food: "050000",
  stays: "100000",
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
  env?: Record<string, string | undefined>;
  fetchImpl?: FetchLike;
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

export async function searchAmapPois(input: SearchAmapPoisInput): Promise<AmapPoi[]> {
  const env = input.env ?? (process.env as Record<string, string | undefined>);
  const apiKey = env.AMAP_API_KEY?.trim();
  if (!apiKey) return [];

  const cityName = AMAP_CITY_MAP[input.cityId];
  const typeCode = AMAP_TYPE_MAP[input.type];
  if (!cityName || !typeCode) return [];

  const url = new URL("https://restapi.amap.com/v3/place/text");
  url.searchParams.set("key", apiKey);
  url.searchParams.set("city", cityName);
  url.searchParams.set("types", typeCode);
  url.searchParams.set("offset", "10");
  url.searchParams.set("page", "1");
  url.searchParams.set("extensions", "all");
  url.searchParams.set("output", "json");
  if (input.keyword?.trim()) {
    url.searchParams.set("keywords", input.keyword.trim());
  }

  const fetchImpl = input.fetchImpl ?? fetch;
  const res = await fetchImpl(url.toString(), { next: { revalidate: 3600 } });
  if (!res.ok) return [];

  const data = await res.json();
  if (data.status !== "1") return [];
  return Array.isArray(data.pois) ? (data.pois as AmapPoi[]) : [];
}
