import { createStaticExploreProvider } from "@/lib/explore/staticProvider";
import type { ExploreProvider } from "@/lib/explore/types";

export function getExploreProvider(): ExploreProvider {
  return createStaticExploreProvider();
}

export type {
  ExploreAttraction,
  ExploreCity,
  ExploreFoodSpot,
  ExploreProvider,
  ExploreProviderStatus,
  ExploreStay,
} from "@/lib/explore/types";
