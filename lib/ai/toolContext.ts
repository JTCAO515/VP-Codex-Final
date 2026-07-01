import { AMAP_CITY_MAP, amapPoiRichMeta, searchAmapPois } from "@/lib/explore/amapSearch";
import type { ButlerIntent } from "@/lib/ai/intentClassifier";
import type { FetchLike } from "@/lib/ai/providers/types";
import type { TripState } from "@/lib/types/trip";

export interface ButlerToolPoi {
  name: string;
  type: string;
  address?: string;
  rating?: string;
  pricePerPerson?: string;
  openHours?: string;
  businessArea?: string;
  sourceLabel: string;
}

export interface ButlerToolContext {
  source: "amap";
  cityId: string;
  category: "attractions" | "food" | "stays";
  pois: ButlerToolPoi[];
}

const CITY_ALIASES: Record<string, string> = {
  beijing: "beijing",
  北京: "beijing",
  shanghai: "shanghai",
  上海: "shanghai",
  chengdu: "chengdu",
  成都: "chengdu",
  xian: "xian",
  "xi'an": "xian",
  西安: "xian",
  guangzhou: "guangzhou",
  广州: "guangzhou",
  hangzhou: "hangzhou",
  杭州: "hangzhou",
  suzhou: "suzhou",
  苏州: "suzhou",
  chongqing: "chongqing",
  重庆: "chongqing",
  nanjing: "nanjing",
  南京: "nanjing",
};

function inferCityId(message: string, trip: TripState): string | null {
  const normalized = message.toLowerCase();
  for (const [alias, cityId] of Object.entries(CITY_ALIASES)) {
    if (normalized.includes(alias.toLowerCase())) return cityId;
  }
  for (const destination of trip.summary.destinations) {
    const id = CITY_ALIASES[destination.toLowerCase()];
    if (id) return id;
  }
  return null;
}

function inferCategory(message: string): ButlerToolContext["category"] {
  const normalized = message.toLowerCase();
  if (/\b(food|restaurant|eat|dining|dish|cuisine|hotpot|noodle|market)\b/.test(normalized)) return "food";
  if (/\b(hotel|stay|area|sleep|accommodation)\b/.test(normalized)) return "stays";
  return "attractions";
}

function shouldSearchPois(intent: ButlerIntent): boolean {
  return ["create_trip", "add_poi", "ask_recommendation", "logistics"].includes(intent);
}

function addressValue(value: string | string[] | undefined): string | undefined {
  if (Array.isArray(value)) return value.find(Boolean);
  return value || undefined;
}

export async function buildButlerToolContext(input: {
  message: string;
  currentTrip: TripState;
  intent: ButlerIntent;
  env: Record<string, string | undefined>;
  fetchImpl: FetchLike;
}): Promise<ButlerToolContext | undefined> {
  if (!shouldSearchPois(input.intent)) return undefined;

  const cityId = inferCityId(input.message, input.currentTrip);
  if (!cityId || !AMAP_CITY_MAP[cityId]) return undefined;

  const category = inferCategory(input.message);
  const pois = await searchAmapPois({
    cityId,
    type: category,
    env: input.env,
    fetchImpl: input.fetchImpl,
  });

  if (pois.length === 0) return undefined;

  return {
    source: "amap",
    cityId,
    category,
    pois: pois.slice(0, 5).map((poi) => {
      const meta = amapPoiRichMeta(poi);
      return {
        name: poi.name,
        type: poi.biz_type ?? poi.type,
        address: addressValue(poi.address) ?? poi.adname,
        rating: meta.rating,
        pricePerPerson: meta.pricePerPerson,
        openHours: meta.openHours,
        businessArea: meta.businessArea,
        sourceLabel: "Amap",
      };
    }),
  };
}
