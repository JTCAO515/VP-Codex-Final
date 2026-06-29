import type { ButlerAlert, CanvasPatch, TripDay, TripState } from "@/lib/types/trip";

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
          title: "Forbidden City",
          description: "Start with the classic imperial axis and keep the morning focused.",
        },
        {
          time: "Afternoon",
          title: "Great Wall (Mutianyu)",
          description: "Use a private car or guided transfer to reduce friction.",
        },
        {
          time: "Evening",
          title: "Temple of Heaven",
          description: "Keep the evening iconic but simple if energy is low.",
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
          title: "The Bund",
          description: "Start with an easy riverfront orientation.",
        },
        {
          time: "Afternoon",
          title: "Yu Garden",
          description: "Pair old Shanghai lanes with a classic garden stop.",
        },
        {
          time: "Evening",
          title: "Nanjing Road",
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
          title: "Summer Palace",
          description: "Use a slower scenic morning with lake views.",
        },
        {
          time: "Afternoon",
          title: "Hutong walk",
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
        description: "Stay near Wangfujing or Dongcheng for convenient metro access.",
      },
      {
        time: "Afternoon",
        title: "Temple of Heaven",
        description: "Begin with a spacious, iconic site that is easier after a flight.",
      },
      {
        time: "Evening",
        title: "Easy hutong dinner",
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
        title: "Forbidden City",
        description: "Book ahead and enter early to avoid the busiest flow.",
      },
      {
        time: "Afternoon",
        title: "Jingshan and hutongs",
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
        title: "High-speed train to Shanghai",
        description: "Use the train if you want city-center arrival and fewer airport steps.",
      },
      {
        time: "Evening",
        title: "The Bund",
        description: "Make the first Shanghai moment visually memorable but simple.",
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
