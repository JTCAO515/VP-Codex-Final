export interface ExploreCity {
  id: string;
  name: string;
  region: string;
  tagline: string;
  bestFor: string[];
}

export interface ExploreRichMeta {
  rating?: string;
  pricePerPerson?: string;
  priceLevel?: "¥" | "¥¥" | "¥¥¥";
  tel?: string;
  openHours?: string;
  photoUrl?: string;
  businessArea?: string;
  sourceLabel?: string;
  location?: {
    lat: number;
    lng: number;
  };
}

export interface ExploreAttraction extends ExploreRichMeta {
  id: string;
  cityId: string;
  name: string;
  category: string;
  description: string;
}

export interface ExploreFoodSpot extends ExploreRichMeta {
  id: string;
  cityId: string;
  name: string;
  dish: string;
  description: string;
}

export interface ExploreStay extends ExploreRichMeta {
  id: string;
  cityId: string;
  name: string;
  area: string;
  description: string;
}

export interface ExploreProviderStatus {
  id: string;
  label: string;
  mode: "static" | "live";
  coverage: string;
  candidates: string[];
  nextIntegration: string;
  limitations: string[];
}

export interface ExploreProvider {
  id: string;
  getProviderStatus(): Promise<ExploreProviderStatus>;
  listCities(): Promise<ExploreCity[]>;
  listAttractions(cityId: string): Promise<ExploreAttraction[]>;
  listFoodSpots(cityId: string): Promise<ExploreFoodSpot[]>;
  listStays(cityId: string): Promise<ExploreStay[]>;
}
