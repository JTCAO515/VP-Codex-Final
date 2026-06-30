export interface Translations {
  nav: {
    chat: string;
    trips: string;
    explore: string;
    tools: string;
    translate: string;
    community: string;
  };
  trips: {
    kicker: string;
    heading: string;
    subtitle: string;
    planInChat: string;
    summaryTrips: string;
    summaryDays: string;
    summaryTasks: string;
    filterAll: string;
    filterDraft: string;
    filterReady: string;
    filterShared: string;
    filterArchived: string;
    cardViewDetails: string;
    cardContinueChat: string;
    cardMetaRoute: string;
    cardMetaDates: string;
    cardMetaLength: string;
    cardMetaTravelers: string;
    cardDays: string;
    cardTasks: string;
    statusReady: string;
    statusShared: string;
    statusArchived: string;
    statusDraft: string;
    guideReady: string;
    empty: string;
  };
  tools: {
    kicker: string;
    heading: string;
    subtitle: string;
    openChecklist: string;
    close: string;
    offlineHeading: string;
    badgeRequired: string;
    badgePreTrip: string;
    badgeLive: string;
    badgeTransit: string;
    badgeConnectivity: string;
    badgeEmergency: string;
  };
  community: {
    kicker: string;
    heading: string;
    subtitle: string;
    tabFeed: string;
    tabHotSpots: string;
    tabPhotos: string;
  };
  translate: {
    kicker: string;
    heading: string;
    subtitle: string;
  };
  explore: {
    kicker: string;
    heading: string;
    subtitle: string;
    attractions: string;
    food: string;
    stays: string;
    addToTrip: string;
  };
  lang: {
    en: string;
    es: string;
    ar: string;
    ja: string;
    ko: string;
    fr: string;
  };
}

const en: Translations = {
  nav: {
    chat: "Chat",
    trips: "Trips",
    explore: "Explore",
    tools: "Tools",
    translate: "Translate",
    community: "Community",
  },
  trips: {
    kicker: "Trips",
    heading: "Your trips",
    subtitle: "Saved canvases, active drafts, and share-ready China itineraries will live here.",
    planInChat: "Plan in Chat",
    summaryTrips: "Trips",
    summaryDays: "Days planned",
    summaryTasks: "Butler tasks",
    filterAll: "All",
    filterDraft: "Draft",
    filterReady: "Ready",
    filterShared: "Shared",
    filterArchived: "Archived",
    cardViewDetails: "View details",
    cardContinueChat: "Continue in Chat",
    cardMetaRoute: "Route",
    cardMetaDates: "Dates",
    cardMetaLength: "Length",
    cardMetaTravelers: "Travelers",
    cardDays: "days",
    cardTasks: "butler tasks",
    statusReady: "Ready to review",
    statusShared: "Shared draft",
    statusArchived: "Archived",
    statusDraft: "Draft in progress",
    guideReady: "Ready plans",
    empty: "No saved trips yet. Use Save to Trips from the Chat workspace.",
  },
  tools: {
    kicker: "Tools",
    heading: "On-the-ground tools",
    subtitle: "Six essential toolkits for every step of your China journey.",
    openChecklist: "Open checklist →",
    close: "Close",
    offlineHeading: "📵 Offline pocket notes",
    badgeRequired: "Required",
    badgePreTrip: "Pre-trip",
    badgeLive: "Live",
    badgeTransit: "Transit",
    badgeConnectivity: "Connectivity",
    badgeEmergency: "Emergency",
  },
  community: {
    kicker: "Community",
    heading: "Traveler community",
    subtitle: "Share trips, photos, practical tips, and trusted China discoveries.",
    tabFeed: "Feed",
    tabHotSpots: "Hot Spots",
    tabPhotos: "Photos",
  },
  translate: {
    kicker: "Translator",
    heading: "China travel translator",
    subtitle: "Text, image scan, voice, and phrases — all in one view.",
  },
  explore: {
    kicker: "Explore",
    heading: "Cities, attractions, food, and stays",
    subtitle: "Curated starting points for your China trip. Real provider data will connect here next.",
    attractions: "Attractions",
    food: "Food",
    stays: "Stays",
    addToTrip: "Add to Trip",
  },
  lang: {
    en: "English",
    es: "Español",
    ar: "العربية",
    ja: "日本語",
    ko: "한국어",
    fr: "Français",
  },
};

export default en;
