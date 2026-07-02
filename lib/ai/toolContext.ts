import { AMAP_CITY_MAP, amapPoiRichMeta, searchAmapPois, type AmapPoi } from "@/lib/explore/amapSearch";
import type { ButlerIntent } from "@/lib/ai/intentClassifier";
import type { FetchLike } from "@/lib/ai/providers/types";
import type { TripState } from "@/lib/types/trip";
import type { BookingCandidate } from "@/lib/types/trip";

export interface ButlerToolPoi {
  id: string;
  name: string;
  type: string;
  address?: string;
  phone?: string;
  rating?: string;
  pricePerPerson?: string;
  openHours?: string;
  businessArea?: string;
  mapUrl?: string;
  sourceLabel: string;
  coordinates?: {
    lat: number;
    lng: number;
  };
  bookingCandidates?: BookingCandidate[];
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

function mapUrlForPoi(poi: { name: string; location?: string }): string {
  const [lngRaw, latRaw] = poi.location?.split(",") ?? [];
  if (lngRaw && latRaw) {
    return `https://uri.amap.com/marker?position=${encodeURIComponent(`${lngRaw},${latRaw}`)}&name=${encodeURIComponent(poi.name)}`;
  }
  return `https://uri.amap.com/search?keyword=${encodeURIComponent(poi.name)}`;
}

function bookingCandidatesForPoi(category: ButlerToolContext["category"], poi: AmapPoi, meta: ReturnType<typeof amapPoiRichMeta>): BookingCandidate[] {
  if (category === "stays") {
    return [
      {
        id: `amap-stay-${poi.id}`,
        kind: "hotel",
        label: poi.name,
        provider: "Amap",
        status: "info-only",
        note: "Place candidate only. VisePanda has not checked live room inventory, refund rules, or payment.",
        priceHint: meta.pricePerPerson,
      },
    ];
  }
  if (category === "attractions") {
    return [
      {
        id: `amap-ticket-${poi.id}`,
        kind: "ticket",
        label: poi.name,
        provider: "Amap",
        status: "info-only",
        note: "Attraction candidate only. Confirm current tickets and opening rules with the official venue.",
        priceHint: meta.pricePerPerson,
      },
    ];
  }
  return [
    {
      id: `amap-food-${poi.id}`,
      kind: "restaurant",
      label: poi.name,
      provider: "Amap",
      status: "info-only",
      note: "Restaurant candidate only. Confirm hours, queues, and reservation needs before going.",
      priceHint: meta.pricePerPerson,
    },
  ];
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
        id: poi.id,
        name: poi.name,
        type: poi.biz_type ?? poi.type,
        address: addressValue(poi.address) ?? poi.adname,
        phone: meta.tel,
        rating: meta.rating,
        pricePerPerson: meta.pricePerPerson,
        openHours: meta.openHours,
        businessArea: meta.businessArea,
        mapUrl: mapUrlForPoi(poi),
        sourceLabel: "Amap",
        coordinates: meta.location,
        bookingCandidates: bookingCandidatesForPoi(category, poi, meta),
      };
    }),
  };
}
