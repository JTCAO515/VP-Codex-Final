import { createStaticExploreProvider } from "@/lib/explore/staticProvider";
import { amapPoiRichMeta, type AmapPoi } from "@/lib/explore/amapSearch";
import type {
  ExploreAttraction,
  ExploreCity,
  ExploreFoodSpot,
  ExploreProvider,
  ExploreProviderStatus,
  ExploreStay,
} from "@/lib/explore/types";

async function fetchAmapPois(cityId: string, type: string): Promise<AmapPoi[]> {
  try {
    const res = await fetch(
      `/api/explore/amap?cityId=${encodeURIComponent(cityId)}&type=${encodeURIComponent(type)}`
    );
    if (!res.ok) return [];
    const data = await res.json();
    return data.ok ? (data.pois as AmapPoi[]) : [];
  } catch {
    return [];
  }
}

function primaryType(poi: AmapPoi): string {
  return poi.biz_type ?? poi.type.split(";")[0] ?? "";
}

function textValue(value: string | string[] | undefined, fallback = "") {
  if (Array.isArray(value)) return value.find(Boolean) ?? fallback;
  return value || fallback;
}

function poiToAttraction(poi: AmapPoi, cityId: string): ExploreAttraction {
  return {
    id: `amap-${poi.id}`,
    cityId,
    name: poi.name,
    category: primaryType(poi),
    description: textValue(poi.address, poi.adname ?? cityId),
    ...amapPoiRichMeta(poi),
  };
}

function poiToFoodSpot(poi: AmapPoi, cityId: string): ExploreFoodSpot {
  return {
    id: `amap-${poi.id}`,
    cityId,
    name: poi.name,
    dish: primaryType(poi),
    description: textValue(poi.address, poi.adname ?? cityId),
    ...amapPoiRichMeta(poi),
  };
}

function poiToStay(poi: AmapPoi, cityId: string): ExploreStay {
  return {
    id: `amap-${poi.id}`,
    cityId,
    name: poi.name,
    area: poi.adname ?? cityId,
    description: textValue(poi.address, cityId),
    ...amapPoiRichMeta(poi),
  };
}

export function createAmapExploreProvider(): ExploreProvider {
  const staticProvider = createStaticExploreProvider();

  return {
    id: "amap",

    async getProviderStatus(): Promise<ExploreProviderStatus> {
      // Health-check: if live API returns data, report live mode; otherwise report static
      const testPois = await fetchAmapPois("beijing", "attractions");
      if (testPois.length === 0) {
        return staticProvider.getProviderStatus();
      }
      return {
        id: "amap",
        label: "Amap live POI provider",
        mode: "live",
        coverage: "National coverage via Amap POI search. Falls back to 8-city static data if API is unavailable.",
        candidates: ["Trip.com", "Meituan", "Tripadvisor"],
        nextIntegration: "Add hotel booking and review data via Trip.com API.",
        limitations: [
          "POI results are not editorially curated; quality depends on Amap search ranking.",
          "Amap rich fields are shown only when the upstream result includes them.",
          "No live ticket prices or booking integration yet.",
        ],
      };
    },

    async listCities(): Promise<ExploreCity[]> {
      return staticProvider.listCities();
    },

    async listAttractions(cityId: string): Promise<ExploreAttraction[]> {
      const pois = await fetchAmapPois(cityId, "attractions");
      if (pois.length === 0) return staticProvider.listAttractions(cityId);
      return pois.map((poi) => poiToAttraction(poi, cityId));
    },

    async listFoodSpots(cityId: string): Promise<ExploreFoodSpot[]> {
      const pois = await fetchAmapPois(cityId, "food");
      if (pois.length === 0) return staticProvider.listFoodSpots(cityId);
      return pois.map((poi) => poiToFoodSpot(poi, cityId));
    },

    async listStays(cityId: string): Promise<ExploreStay[]> {
      const pois = await fetchAmapPois(cityId, "stays");
      if (pois.length === 0) return staticProvider.listStays(cityId);
      return pois.map((poi) => poiToStay(poi, cityId));
    },
  };
}
