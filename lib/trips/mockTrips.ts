export type SavedTripStatus = "draft" | "ready" | "shared" | "archived";

export interface SavedTrip {
  id: string;
  title: string;
  route: string;
  dates: string;
  durationDays: number;
  travelers: string;
  status: SavedTripStatus;
  updatedAt: string;
  alertCount: number;
  summary: string;
  highlights: string[];
}

export const savedTrips: SavedTrip[] = [
  {
    id: "first-china-beijing-shanghai",
    title: "Beijing -> Shanghai First China Trip",
    route: "Beijing -> Shanghai",
    dates: "May 20 - May 25",
    durationDays: 6,
    travelers: "2 travelers",
    status: "ready",
    updatedAt: "Updated today",
    alertCount: 3,
    summary: "A balanced first China itinerary with cultural anchors, food stops, and convenient hotel areas.",
    highlights: ["Forbidden City", "Great Wall", "The Bund"],
  },
  {
    id: "chengdu-food-weekend",
    title: "Chengdu Food Weekend",
    route: "Chengdu",
    dates: "Jun 14 - Jun 16",
    durationDays: 3,
    travelers: "Solo traveler",
    status: "draft",
    updatedAt: "Updated yesterday",
    alertCount: 2,
    summary: "A slower weekend draft centered on Sichuan food, teahouses, pandas, and low-friction rides.",
    highlights: ["Hotpot", "People's Park", "Kuanzhai Alley"],
  },
  {
    id: "shanghai-business-culture",
    title: "Shanghai Business + Culture",
    route: "Shanghai",
    dates: "Jul 3 - Jul 7",
    durationDays: 5,
    travelers: "Business guest",
    status: "shared",
    updatedAt: "Updated 3 days ago",
    alertCount: 1,
    summary: "A compact plan that fits meetings, river views, French Concession walks, and client dinners.",
    highlights: ["Jing'an", "The Bund", "Yu Garden"],
  },
  {
    id: "kunming-quiet-archive",
    title: "Kunming Quiet Getaway",
    route: "Kunming",
    dates: "Mar 2 - Mar 5",
    durationDays: 4,
    travelers: "Solo traveler",
    status: "archived",
    updatedAt: "Updated 2 months ago",
    alertCount: 0,
    summary: "An archived slow-travel draft for Kunming's lakes and old town, kept for reference.",
    highlights: ["Green Lake", "Old Town", "Stone Forest day trip"],
  },
];

export const tripStatusLabels: Record<SavedTripStatus | "all", string> = {
  all: "All",
  draft: "Draft",
  ready: "Ready",
  shared: "Shared",
  archived: "Archived",
};
