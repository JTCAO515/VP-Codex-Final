export interface ExploreCity {
  id: string;
  name: string;
  region: string;
  tagline: string;
  bestFor: string[];
}

export interface ExploreAttraction {
  id: string;
  cityId: string;
  name: string;
  category: string;
  description: string;
}

export interface ExploreFoodSpot {
  id: string;
  cityId: string;
  name: string;
  dish: string;
  description: string;
}

export interface ExploreStay {
  id: string;
  cityId: string;
  name: string;
  area: string;
  description: string;
}

export interface ExploreProvider {
  id: string;
  listCities(): Promise<ExploreCity[]>;
  listAttractions(cityId: string): Promise<ExploreAttraction[]>;
  listFoodSpots(cityId: string): Promise<ExploreFoodSpot[]>;
  listStays(cityId: string): Promise<ExploreStay[]>;
}
