export interface ExploreCity {
  id: string;
  name: string;
  region: string;
  tagline: string;
  bestFor: string[];
}

/**
 * Client-derived-only judgment fields for "can a first-time foreign visitor
 * go here" (Issue #119/#122). Never returned by the backend today — this
 * type exists so Web UI code can consume the same shape iOS derives
 * client-side (ios/VisePandaIOS/Models/ExploreModels.swift's
 * TravelerFitDeriver), without inventing its own field names. All fields
 * optional; omit rather than guess when a signal isn't there.
 */
export interface TravelerFit {
  firstTimerFit?: boolean;
  paymentFriendliness?: string;
  languageDifficulty?: string;
  routeFit?: string;
  rainyDayFit?: boolean;
  nightFit?: boolean;
  crowdRisk?: string;
  luggageFit?: boolean;
  watchOut?: string;
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
  /** Not populated by any Web/API code today — see TravelerFit's doc comment. */
  travelerFit?: TravelerFit;
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
