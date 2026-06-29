import type {
  DayActivityBlock,
  DayCard,
  DayInstruction,
  RailInstruction,
  TripInstructionBlock,
} from "./types";

const KNOWN_CITIES = ["Beijing", "Shanghai", "Chengdu", "Xi'an", "Guangzhou", "Hangzhou"];

const CITY_HIGHLIGHTS: Record<string, { attractions: string[]; food: string[] }> = {
  Beijing: {
    attractions: ["Forbidden City", "Great Wall (Mutianyu)", "Temple of Heaven"],
    food: ["Peking duck", "Zhajiangmian noodles"],
  },
  Shanghai: {
    attractions: ["The Bund", "Yu Garden", "Nanjing Road"],
    food: ["Xiaolongbao", "Shanghai-style braised pork"],
  },
  Chengdu: {
    attractions: ["Giant Panda Base", "Jinli Ancient Street"],
    food: ["Hotpot", "Mapo tofu"],
  },
  "Xi'an": {
    attractions: ["Terracotta Army", "Ancient City Wall"],
    food: ["Roujiamo", "Biangbiang noodles"],
  },
  Guangzhou: {
    attractions: ["Canton Tower", "Shamian Island"],
    food: ["Dim sum", "Cantonese roast goose"],
  },
  Hangzhou: {
    attractions: ["West Lake", "Lingyin Temple"],
    food: ["Longjing tea", "Beggar's chicken"],
  },
};

const PERIODS: DayPeriodList = ["morning", "afternoon", "evening"];
type DayPeriodList = DayActivityBlock["period"][];

function buildActivities(city: string, count: number): DayActivityBlock[] {
  const highlight = CITY_HIGHLIGHTS[city] ?? { attractions: ["City center walk"], food: ["Local specialties"] };
  return PERIODS.slice(0, count).map((period, i) => ({
    period,
    title: highlight.attractions[i % highlight.attractions.length],
    imageHint: highlight.attractions[i % highlight.attractions.length],
  }));
}

function extractCities(text: string): string[] {
  return KNOWN_CITIES.filter((city) => text.toLowerCase().includes(city.toLowerCase()));
}

function extractDayCount(text: string): number | null {
  const match = text.match(/(\d+)\s*-?\s*day/i);
  return match ? parseInt(match[1], 10) : null;
}

function detectRelaxedPace(text: string): boolean {
  return /tired|relax|slow|easy pace|not too much/i.test(text);
}

export interface MockReply {
  chatText: string;
  instructions: TripInstructionBlock;
}

export function generateMockReply(userText: string, existingDays: DayCard[]): MockReply {
  const cities = extractCities(userText);
  const dayCount = extractDayCount(userText);
  const relaxed = detectRelaxedPace(userText);

  if (cities.length === 0 && dayCount === null && !relaxed) {
    return {
      chatText:
        "Tell me which cities you'd like to visit and how many days you have, and I'll start building your itinerary.",
      instructions: {},
    };
  }

  if (relaxed && existingDays.length > 0) {
    const days: DayInstruction[] = existingDays.map((d) => ({
      day: d.day,
      action: "upsert",
      data: {
        city: d.city,
        activities: buildActivities(d.city, Math.max(1, d.activities.length - 1)),
        food: d.food,
        hotel: `${d.hotel} (near metro, English-friendly front desk)`,
        transport: d.transport,
        pace: "relaxed",
        budgetNote: d.budgetNote,
      },
    }));
    return {
      chatText: "Got it — I've lightened the pace and prioritized hotels near the metro with English-friendly service.",
      instructions: { days },
    };
  }

  const totalDays = dayCount ?? 5;
  const targetCities = cities.length > 0 ? cities : ["Beijing"];
  const days: DayInstruction[] = [];
  for (let day = 1; day <= totalDays; day++) {
    const city = targetCities[(day - 1) % targetCities.length];
    const highlight = CITY_HIGHLIGHTS[city] ?? { attractions: ["City center walk"], food: ["Local specialties"] };
    days.push({
      day,
      action: "upsert",
      data: {
        city,
        activities: buildActivities(city, 3),
        food: highlight.food.slice(0, 1),
        hotel: `${city} city-center hotel`,
        transport: day === 1 ? "Airport transfer" : "Metro / high-speed rail",
        pace: "moderate",
        budgetNote: "~$80-120/day estimated",
      },
    });
  }

  const rails: RailInstruction[] = [
    {
      id: "visa-check",
      action: "upsert",
      data: {
        category: "visa",
        title: "Visa check",
        detail: "Confirm visa-free transit eligibility or apply for a tourist visa before departure.",
        severity: "warning",
      },
    },
    {
      id: "payment-setup",
      action: "upsert",
      data: {
        category: "payment",
        title: "Payment setup",
        detail: "Link an international card to Alipay/WeChat Pay before you arrive.",
        severity: "info",
      },
    },
  ];

  if (targetCities.length > 1) {
    rails.push({
      id: "intercity-transport",
      action: "upsert",
      data: {
        category: "transport",
        title: "Intercity transport",
        detail: `Book high-speed rail or flights between ${targetCities.join(" and ")} in advance.`,
        severity: "info",
      },
    });
  }

  return {
    chatText: `I've put together a ${totalDays}-day route across ${targetCities.join(
      ", "
    )}. Check the canvas on the left — let me know if you want a slower pace or different cities.`,
    instructions: {
      days,
      rails,
      summary: { route: targetCities, days: totalDays },
    },
  };
}
