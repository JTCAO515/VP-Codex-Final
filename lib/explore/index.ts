import { createAmapExploreProvider } from "@/lib/explore/amapProvider";
import type { ExploreProvider } from "@/lib/explore/types";

export function getExploreProvider(): ExploreProvider {
  return createAmapExploreProvider();
}

export type {
  ExploreAttraction,
  ExploreCity,
  ExploreFoodSpot,
  ExploreProvider,
  ExploreProviderStatus,
  ExploreStay,
} from "@/lib/explore/types";
