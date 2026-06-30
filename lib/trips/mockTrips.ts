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
    highlights: ["Forbidden City (故宫)", "Great Wall (长城)", "The Bund (外滩)"],
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
    highlights: ["Hotpot (火锅)", "People's Park (人民公园)", "Kuanzhai Alley (宽窄巷子)"],
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
    highlights: ["Jing'an (静安)", "The Bund (外滩)", "Yu Garden (豫园)"],
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
    highlights: ["Green Lake (翠湖)", "Old Town (昆明古城)", "Stone Forest day trip (石林)"],
  },
];

export const tripStatusLabels: Record<SavedTripStatus | "all", string> = {
  all: "All",
  draft: "Draft",
  ready: "Ready",
  shared: "Shared",
  archived: "Archived",
};

export const tripStatusDescriptions: Record<SavedTripStatus, string> = {
  draft: "Drafts are still flexible and should continue in Chat before review.",
  ready: "Ready means this itinerary is prepared for review before sharing or traveling.",
  shared: "Shared plans already have a public read-only link or are ready to send.",
  archived: "Archived trips are kept for reference and hidden from active planning work.",
};

export const tripStatusNextActions: Record<SavedTripStatus, string> = {
  draft: "Continue editing in Chat",
  ready: "Review details or create a share link",
  shared: "Open details to manage the share link",
  archived: "Restore from archive if planning resumes",
};
