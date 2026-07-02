import type { ButlerAlert, CanvasPatch, TripBlock, TripDay, TripState } from "@/lib/types/trip";

export const initialTripState: TripState = {
  summary: {
    title: "China Trip Draft",
    durationDays: 5,
    pace: "Balanced",
    travelerStyle: "First-time visitor",
    destinations: ["Beijing", "Shanghai"],
    confidence: "Draft",
  },
  days: [
    {
      day: 1,
      city: "Beijing",
      pace: "Balanced",
      blocks: [
        {
          time: "Morning",
          title: "Forbidden City (故宫)",
          description: "Start with the classic imperial axis and keep the morning focused.",
          address: "4 Jingshan Front Street, Dongcheng District, Beijing",
          chineseAddress: "北京市东城区景山前街4号",
          openingHours: "Usually daytime entry; timed tickets required",
          mapUrl: "https://uri.amap.com/search?keyword=%E6%95%85%E5%AE%AB",
          bookingUrl: "https://intl.dpm.org.cn/",
          bookingCandidates: [
            {
              id: "static-ticket-forbidden-city",
              kind: "ticket",
              label: "Forbidden City official ticket info",
              provider: "Official venue",
              status: "info-only",
              note: "Information link only. Confirm current availability, passport rules, and refund policy before paying.",
            },
          ],
          sourceLabel: "Static fallback",
          coordinates: { lat: 39.91635, lng: 116.39715 },
        },
        {
          time: "Afternoon",
          title: "Great Wall · Mutianyu (长城·慕田峪)",
          description: "Use a private car or guided transfer to reduce friction.",
          address: "Mutianyu Village, Huairou District, Beijing",
          chineseAddress: "北京市怀柔区慕田峪村",
          openingHours: "Daytime scenic-area hours; confirm before departure",
          mapUrl: "https://uri.amap.com/search?keyword=%E6%85%95%E7%94%B0%E5%B3%AA%E9%95%BF%E5%9F%8E",
          sourceLabel: "Static fallback",
          coordinates: { lat: 40.43191, lng: 116.57037 },
        },
        {
          time: "Evening",
          title: "Temple of Heaven (天坛)",
          description: "Keep the evening iconic but simple if energy is low.",
          address: "1 Tiantan East Road, Dongcheng District, Beijing",
          chineseAddress: "北京市东城区天坛东路1号",
          openingHours: "Park open into evening; halls close earlier",
          mapUrl: "https://uri.amap.com/search?keyword=%E5%A4%A9%E5%9D%9B",
          sourceLabel: "Static fallback",
          coordinates: { lat: 39.88216, lng: 116.40661 },
        },
      ],
      food: ["Hutong noodles", "Roast duck dinner"],
      stay: "Beijing city-center hotel",
      transport: "Airport transfer and private car for the Great Wall.",
      status: "new",
      note: "Keep the first day structured and book key tickets ahead.",
    },
    {
      day: 2,
      city: "Shanghai",
      pace: "Balanced",
      blocks: [
        {
          time: "Morning",
          title: "The Bund (外滩)",
          description: "Start with an easy riverfront orientation.",
          address: "Zhongshan East 1st Road, Huangpu District, Shanghai",
          chineseAddress: "上海市黄浦区中山东一路",
          openingHours: "Open public promenade",
          mapUrl: "https://uri.amap.com/search?keyword=%E5%A4%96%E6%BB%A9",
          sourceLabel: "Static fallback",
          coordinates: { lat: 31.23969, lng: 121.49976 },
        },
        {
          time: "Afternoon",
          title: "Yu Garden (豫园)",
          description: "Pair old Shanghai lanes with a classic garden stop.",
          address: "279 Yuyuan Old Street, Huangpu District, Shanghai",
          chineseAddress: "上海市黄浦区豫园老街279号",
          openingHours: "Ticketed garden hours vary by season",
          mapUrl: "https://uri.amap.com/search?keyword=%E8%B1%AB%E5%9B%AD",
          sourceLabel: "Static fallback",
          coordinates: { lat: 31.22723, lng: 121.49201 },
        },
        {
          time: "Evening",
          title: "Nanjing Road (南京路)",
          description: "Use a central evening walk with simple food options nearby.",
        },
      ],
      food: ["Xiaolongbao", "Shanghainese noodles"],
      stay: "Shanghai city-center hotel",
      transport: "Metro / high-speed rail",
      note: "Keep hotel areas central so the evening is easy.",
      status: "new",
    },
    {
      day: 3,
      city: "Beijing",
      pace: "Balanced",
      blocks: [
        {
          time: "Morning",
          title: "Summer Palace (颐和园)",
          description: "Use a slower scenic morning with lake views.",
        },
        {
          time: "Afternoon",
          title: "Hutong walk (胡同)",
          description: "Add a neighborhood walk and tea break.",
        },
        {
          time: "Evening",
          title: "Local dinner",
          description: "Pick a relaxed dinner close to the hotel.",
        },
      ],
      food: ["Tea house snacks", "Neighborhood dinner"],
      stay: "Beijing city-center hotel",
      transport: "Metro / short taxi rides",
      note: "This day balances culture with recovery time.",
      status: "new",
    },
  ],
  alerts: [
    {
      type: "payment",
      priority: "high",
      title: "Set up Alipay before arrival",
      body: "Payment setup prevents friction with taxis, restaurants, and small shops.",
      action: "Review payment setup",
    },
  ],
  lastUpdatedReason: "Initial VisePanda travel draft.",
};

const firstTripDays: TripDay[] = [
  {
    day: 1,
    city: "Beijing",
    pace: "Balanced",
    blocks: [
        {
          time: "Morning",
          title: "Arrival and check-in",
          description: "Stay near Wangfujing (王府井) or Dongcheng for convenient metro access.",
          address: "Wangfujing, Dongcheng District, Beijing",
          chineseAddress: "北京市东城区王府井",
          openingHours: "Hotel check-in times vary",
          mapUrl: "https://uri.amap.com/search?keyword=%E7%8E%8B%E5%BA%9C%E4%BA%95",
          sourceLabel: "Static fallback",
        },
        {
          time: "Afternoon",
          title: "Temple of Heaven (天坛)",
          description: "Begin with a spacious, iconic site that is easier after a flight.",
          address: "1 Tiantan East Road, Dongcheng District, Beijing",
          chineseAddress: "北京市东城区天坛东路1号",
          openingHours: "Park open into evening; halls close earlier",
          mapUrl: "https://uri.amap.com/search?keyword=%E5%A4%A9%E5%9D%9B",
          sourceLabel: "Static fallback",
          coordinates: { lat: 39.88216, lng: 116.40661 },
        },
      {
        time: "Evening",
        title: "Easy hutong (胡同) dinner",
        description: "Choose a low-friction dinner close to the hotel.",
      },
    ],
    food: ["Hutong noodles", "Roast duck tasting"],
    stay: "Wangfujing or Dongcheng",
    transport: "Short taxi rides on arrival day; metro when rested.",
    note: "Keep the first day light and practical.",
    status: "new",
  },
  {
    day: 2,
    city: "Beijing",
    pace: "Balanced",
    blocks: [
        {
          time: "Morning",
          title: "Forbidden City (故宫)",
          description: "Book ahead and enter early to avoid the busiest flow.",
          address: "4 Jingshan Front Street, Dongcheng District, Beijing",
          chineseAddress: "北京市东城区景山前街4号",
          openingHours: "Usually daytime entry; timed tickets required",
          mapUrl: "https://uri.amap.com/search?keyword=%E6%95%85%E5%AE%AB",
          bookingUrl: "https://intl.dpm.org.cn/",
          bookingCandidates: [
            {
              id: "static-ticket-forbidden-city",
              kind: "ticket",
              label: "Forbidden City official ticket info",
              provider: "Official venue",
              status: "info-only",
              note: "Information link only. Confirm current availability, passport rules, and refund policy before paying.",
            },
          ],
          sourceLabel: "Static fallback",
          coordinates: { lat: 39.91635, lng: 116.39715 },
        },
      {
        time: "Afternoon",
        title: "Jingshan Park (景山公园) and hutongs (胡同)",
        description: "Pair one classic viewpoint with a slower neighborhood walk.",
      },
    ],
    food: ["Zhajiangmian", "Peking duck"],
    stay: "Dongcheng",
    transport: "Metro plus short rideshare hops.",
    note: "Reserve timed tickets before arrival.",
    status: "new",
  },
  {
    day: 3,
    city: "Shanghai",
    pace: "Balanced",
    blocks: [
      {
        time: "Morning",
        title: "High-speed train to Shanghai (高铁)",
        description: "Use the train if you want city-center arrival and fewer airport steps.",
      },
        {
          time: "Evening",
          title: "The Bund (外滩)",
          description: "Make the first Shanghai moment visually memorable but simple.",
          address: "Zhongshan East 1st Road, Huangpu District, Shanghai",
          chineseAddress: "上海市黄浦区中山东一路",
          openingHours: "Open public promenade",
          mapUrl: "https://uri.amap.com/search?keyword=%E5%A4%96%E6%BB%A9",
          sourceLabel: "Static fallback",
          coordinates: { lat: 31.23969, lng: 121.49976 },
        },
    ],
    food: ["Xiaolongbao", "Shanghainese noodles"],
    stay: "People's Square or Jing'an",
    transport: "Train arrival plus metro or taxi to hotel.",
    note: "Avoid overpacking the transfer day.",
    status: "new",
  },
];

function includesAny(message: string, words: string[]) {
  return words.some((word) => message.includes(word));
}

function paymentAlert(): ButlerAlert {
  return {
    type: "payment",
    priority: "high",
    title: "Set up Alipay before arrival",
    body: "Payment setup prevents friction with taxis, restaurants, and small shops.",
    action: "Review payment setup",
  };
}

function visaAlert(): ButlerAlert {
  return {
    type: "visa",
    priority: "high",
    title: "Check entry rules before booking",
    body: "Visa-free and transit rules depend on nationality, city pair, and trip length.",
    action: "Review visa and entry checklist",
  };
}

function languageAlert(): ButlerAlert {
  return {
    type: "language",
    priority: "medium",
    title: "Prepare translation for taxis and dining",
    body: "Save hotel addresses and common food phrases in Chinese before you land.",
    action: "Open translation tools",
  };
}

function emergencyAlert(): ButlerAlert {
  return {
    type: "emergency",
    priority: "medium",
    title: "Save emergency contacts offline",
    body: "Keep embassy, hotel, passport, and insurance details available without roaming.",
    action: "Prepare emergency card",
  };
}

// City detection + skeleton generation so the fallback butler always produces a
// canvas that reflects what the traveler asked for (fixes Chat<->Canvas "not
// syncing" when live models are unavailable). Real models still handle nuance;
// this is the graceful floor.
const CITY_LOOKUP: Record<string, string> = {
  beijing: "Beijing", 北京: "Beijing",
  shanghai: "Shanghai", 上海: "Shanghai",
  chengdu: "Chengdu", 成都: "Chengdu",
  "xi'an": "Xi'an", xian: "Xi'an", 西安: "Xi'an",
  guangzhou: "Guangzhou", 广州: "Guangzhou",
  hangzhou: "Hangzhou", 杭州: "Hangzhou",
  suzhou: "Suzhou", 苏州: "Suzhou",
  chongqing: "Chongqing", 重庆: "Chongqing",
  nanjing: "Nanjing", 南京: "Nanjing",
  guilin: "Guilin", 桂林: "Guilin",
  lijiang: "Lijiang", 丽江: "Lijiang",
  yunnan: "Yunnan", 云南: "Yunnan",
  shenzhen: "Shenzhen", 深圳: "Shenzhen",
  "hong kong": "Hong Kong", 香港: "Hong Kong",
};

const CITY_HIGHLIGHTS: Record<string, string[]> = {
  Beijing: ["Forbidden City (故宫)", "Great Wall · Mutianyu (长城·慕田峪)", "Temple of Heaven (天坛)", "Summer Palace (颐和园)", "Hutong walk (胡同)"],
  Shanghai: ["The Bund (外滩)", "Yu Garden (豫园)", "Nanjing Road (南京路)", "French Concession (法租界)", "Shanghai Tower (上海中心)"],
  Chengdu: ["Giant Panda Base (大熊猫基地)", "Jinli Ancient Street (锦里)", "People's Park teahouse (人民公园)", "Wuhou Shrine (武侯祠)", "Kuanzhai Alley (宽窄巷子)"],
  "Xi'an": ["Terracotta Army (兵马俑)", "City Wall (西安城墙)", "Muslim Quarter (回民街)", "Big Wild Goose Pagoda (大雁塔)", "Bell Tower (钟楼)"],
  Guangzhou: ["Canton Tower (广州塔)", "Shamian Island (沙面)", "Yuexiu Park (越秀公园)", "Beijing Road (北京路)", "Dim sum tea house"],
  Hangzhou: ["West Lake (西湖)", "Lingyin Temple (灵隐寺)", "Longjing tea village (龙井)", "Hefang Street (河坊街)", "Grand Canal (大运河)"],
  Suzhou: ["Humble Administrator's Garden (拙政园)", "Pingjiang Road (平江路)", "Tiger Hill (虎丘)", "Silk Museum (丝绸博物馆)", "Shantang Street (山塘街)"],
  Chongqing: ["Hongya Cave (洪崖洞)", "Yangtze cable car (长江索道)", "Ciqikou old town (磁器口)", "Liziba monorail (李子坝)", "Hotpot dinner (火锅)"],
  Guilin: ["Li River cruise (漓江)", "Reed Flute Cave (芦笛岩)", "Elephant Trunk Hill (象鼻山)", "Yangshuo West Street (阳朔西街)", "Longji rice terraces (龙脊梯田)"],
};

function highlightsFor(city: string): string[] {
  return (
    CITY_HIGHLIGHTS[city] ?? [
      `${city} historic center`,
      `${city} signature landmark`,
      `${city} local market & street food`,
      `${city} park or riverside walk`,
      `${city} evening night view`,
    ]
  );
}

function extractCities(normalized: string): string[] {
  const found: string[] = [];
  for (const [alias, display] of Object.entries(CITY_LOOKUP)) {
    if (normalized.includes(alias) && !found.includes(display)) found.push(display);
  }
  return found;
}

function extractDayCount(normalized: string): number {
  const weeks = normalized.match(/(\d+)\s*(weeks?|周)/);
  if (weeks) return Math.min(21, parseInt(weeks[1], 10) * 7);
  const days = normalized.match(/(\d+)\s*(days?|nights?|天|晚)/);
  if (days) return Math.min(21, parseInt(days[1], 10));
  return 0;
}

function buildSkeletonDay(dayNum: number, city: string, cityDayIndex: number): TripDay {
  const hl = highlightsFor(city);
  const pick = (offset: number) => hl[(cityDayIndex * 3 + offset) % hl.length];
  const blockWithPoi = (time: TripBlock["time"], title: string, description: string): TripBlock => ({
    time,
    title,
    description,
    address: `${title}, ${city}`,
    chineseAddress: title.includes("（") || title.includes("(") ? title : undefined,
    openingHours: "Confirm current hours before departure",
    mapUrl: `https://uri.amap.com/search?keyword=${encodeURIComponent(title)}`,
    sourceLabel: "Static fallback",
  });
  return {
    day: dayNum,
    city,
    pace: "Balanced",
    blocks: [
      blockWithPoi("Morning", pick(0), `Start your ${city} day at an easy pace.`),
      blockWithPoi("Afternoon", pick(1), `Keep exploring ${city} with manageable walking.`),
      blockWithPoi("Evening", pick(2), `Wind down with dinner and an easy ${city} evening.`),
    ],
    food: [`${city} local specialty`, `${city} street snack`],
    stay: `${city} central, transit-friendly area`,
    transport: cityDayIndex === 0 ? `Arrive in ${city}; metro or taxi to the hotel.` : `Metro and short rides within ${city}.`,
    note: `Reserve popular ${city} sights ahead where booking is required.`,
    status: "new",
  };
}

function buildSkeletonDays(cities: string[], totalDays: number): TripDay[] {
  const days: TripDay[] = [];
  const perCity = Math.max(1, Math.floor(totalDays / cities.length));
  let dayNum = 1;
  let remaining = totalDays;
  cities.forEach((city, index) => {
    const count = index === cities.length - 1 ? remaining : Math.min(perCity, remaining);
    for (let i = 0; i < count; i += 1) {
      days.push(buildSkeletonDay(dayNum++, city, i));
      remaining -= 1;
    }
  });
  let lastIndex = days.filter((d) => d.city === cities[cities.length - 1]).length;
  while (remaining > 0) {
    days.push(buildSkeletonDay(dayNum++, cities[cities.length - 1], lastIndex++));
    remaining -= 1;
  }
  return days;
}

export function createMockButlerPatch(message: string, current: TripState): CanvasPatch {
  const normalized = message.toLowerCase();
  const alerts: ButlerAlert[] = [];

  if (includesAny(normalized, ["payment", "alipay", "wechat pay", "card"])) alerts.push(paymentAlert());
  if (includesAny(normalized, ["visa", "entry", "passport", "transit"])) alerts.push(visaAlert());
  if (includesAny(normalized, ["translate", "translation", "language", "chinese"])) alerts.push(languageAlert());
  if (includesAny(normalized, ["emergency", "sos", "hospital", "passport lost"])) alerts.push(emergencyAlert());

  if (includesAny(normalized, ["first time", "first china trip", "5 days"])) {
    return {
      intent: "create_trip",
      assistantMessage:
        "I drafted a first China trip with Beijing for cultural grounding and Shanghai for a smooth modern contrast.",
      reason: "Created a first-time China itinerary across Beijing and Shanghai.",
      tripSummary: {
        title: "First China Trip",
        durationDays: 5,
        pace: "Balanced",
        travelerStyle: "First-time visitor",
        destinations: ["Beijing", "Shanghai"],
        confidence: "Draft",
      },
      days: firstTripDays,
      butlerAlerts: alerts.length ? alerts : [paymentAlert(), visaAlert()],
    };
  }

  // Destination-aware create: any message naming a city with a planning cue or a
  // new destination produces a matching itinerary so the canvas reflects the chat.
  const cities = extractCities(normalized);
  const requestedDays = extractDayCount(normalized);
  const createIntent = includesAny(normalized, [
    "plan", "create", "build", "design", "draft", "itinerary", "trip", "travel",
    "go to", "visit", "holiday", "vacation", "days", "day", "week",
    "规划", "计划", "行程", "天", "旅游", "路线", "去",
  ]);
  const destinationsLower = current.summary.destinations.map((d) => d.toLowerCase());
  const hasNewDestination = cities.some((city) => !destinationsLower.includes(city.toLowerCase()));

  if (cities.length > 0 && createIntent && (requestedDays > 0 || hasNewDestination)) {
    const totalDays = requestedDays > 0 ? requestedDays : Math.min(10, Math.max(3, cities.length * 2));
    return {
      intent: "create_trip",
      assistantMessage: `I drafted a ${totalDays}-day ${cities.join(" + ")} itinerary you can refine from here.`,
      reason: `Created a ${totalDays}-day itinerary for ${cities.join(", ")}.`,
      tripSummary: {
        title: `${cities.join(" + ")} Trip`,
        durationDays: totalDays,
        pace: "Balanced",
        travelerStyle: current.summary.travelerStyle,
        destinations: cities,
        confidence: "Draft",
      },
      days: buildSkeletonDays(cities, totalDays),
      butlerAlerts: alerts.length ? alerts : [paymentAlert(), visaAlert()],
    };
  }

  if (includesAny(normalized, ["less tiring", "slow", "slower", "relaxed"])) {
    return {
      intent: "adjust_trip",
      assistantMessage: "I slowed the pace and kept the daily plan easier to recover from.",
      reason: "Adjusted pace to Relaxed with fewer daily moves.",
      tripSummary: { pace: "Relaxed", confidence: "Refined" },
      days: current.days.map((day) => ({
        ...day,
        pace: "Relaxed",
        blocks: day.blocks.slice(0, 2),
        note: "This day is intentionally lighter to reduce fatigue.",
        status: "revised",
      })),
      butlerAlerts: alerts,
    };
  }

  if (includesAny(normalized, ["budget", "cheap", "lower cost", "save money"])) {
    return {
      intent: "adjust_trip",
      assistantMessage: "I shifted the plan toward metro access, casual meals, and practical hotel areas.",
      reason: "Adjusted budget assumptions toward lower-cost choices.",
      tripSummary: { confidence: "Refined" },
      days: current.days.map((day) => ({
        ...day,
        food: ["Casual local noodles", "Food court or neighborhood restaurant"],
        transport: "Prefer metro routes and short rides only when needed.",
        note: "Budget version: keep hotels near transit and avoid unnecessary transfers.",
        status: "revised",
      })),
      butlerAlerts: alerts,
    };
  }

  if (includesAny(normalized, ["food", "dining", "eat", "restaurant"])) {
    return {
      intent: "adjust_trip",
      assistantMessage: "I added more food-focused stops without making the route too dense.",
      reason: "Added dining emphasis to the canvas.",
      days: current.days.map((day) => ({
        ...day,
        food: [...day.food, day.city === "Shanghai" ? "Xiaolongbao tasting" : "Regional snack stop"],
        status: "revised",
      })),
      butlerAlerts: alerts,
    };
  }

  if (includesAny(normalized, ["hotel", "stay", "convenient"])) {
    return {
      intent: "adjust_trip",
      assistantMessage: "I moved hotel guidance toward convenient, transit-friendly areas.",
      reason: "Updated hotel area guidance.",
      days: current.days.map((day) => ({
        ...day,
        stay: day.city === "Shanghai" ? "Jing'an or People's Square" : "Dongcheng or Wangfujing",
        status: "revised",
      })),
      butlerAlerts: alerts,
    };
  }

  return {
    intent: alerts.length ? "add_alerts" : "adjust_trip",
    assistantMessage: alerts.length
      ? "I added the relevant practical reminders to the canvas."
      : "I kept the current route and noted this as planning context.",
    reason: alerts.length ? "Added practical butler reminders." : "Added context without changing the route.",
    tripSummary: { confidence: alerts.length ? "Refined" : current.summary.confidence },
    butlerAlerts: alerts,
  };
}
