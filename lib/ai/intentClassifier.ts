// Lightweight, local intent classifier (阶段十三 routing input; also seeds 阶段十二
// task 12.1). Regex + keyword only — no LLM call. Its purpose per ADR-043 is
// routing for QUALITY (pick the right specialist model), not cost savings.
//
// Deterministic and side-effect free so it is trivial to unit test.

export type ButlerIntent =
  | "create_trip"
  | "adjust_trip"
  | "add_location"
  | "add_poi"
  | "ask_recommendation"
  | "ask_factual"
  | "preference_signal"
  | "concern"
  | "logistics"
  | "unclear";

interface IntentRule {
  intent: ButlerIntent;
  patterns: RegExp[];
}

// Order matters: earlier rules win on ties. Most specific intents first.
const RULES: IntentRule[] = [
  {
    intent: "ask_factual",
    patterns: [
      /\b(visa|passport|entry|customs|transit)\b/,
      /\b(do i need|am i allowed|requirement|how (do|can) i pay|alipay|wechat pay)\b/,
      /\b(esim|sim card|vpn|exchange rate|currency|metro card|how much (is|does))\b/,
    ],
  },
  {
    intent: "concern",
    patterns: [
      /\b(is it safe|safety|dangerous|risk|scam(med)?|emergency|worried|afraid|hospital)\b/,
      // v0.3.17: acute-distress vocabulary — these must never fall through to
      // "unclear" or a generic recommendation path.
      /\b(robbed|stolen|theft|pickpocket|lost my (passport|wallet|phone)|injured|hurt|urgent help|call (the )?police|ambulance)\b/,
    ],
  },
  {
    intent: "logistics",
    patterns: [
      /\b(how (do|to) (i )?get from|train|high[- ]?speed rail|flight|transfer|between)\b.*\bto\b/,
      /\b(metro|subway|route|directions|how long (does|to)|distance)\b/,
    ],
  },
  {
    intent: "ask_recommendation",
    patterns: [
      /\b(best|top|recommend|where (should|can) i|what.s good|any good|must[- ]?(eat|see|try|visit))\b/,
      /\b(suggest|famous for|worth (visiting|seeing))\b/,
    ],
  },
  {
    intent: "preference_signal",
    patterns: [
      /\b(i (love|like|prefer|hate|don.t (eat|like|want)|can.t (eat|walk))|i.m (a )?(vegetarian|vegan|foodie))\b/,
      /\b(budget|student|luxury|cheap|allergic|no pork|no seafood|halal|with (kids|my family|toddlers))\b/,
      /\b(tired|exhausted|slow(er)? pace|too much walking)\b/,
    ],
  },
  {
    intent: "add_poi",
    patterns: [/\b(add|include|put in|insert)\b.*\b(to (my|the) (trip|day|itinerary|canvas))\b/, /\badd\b.+\bto my trip\b/],
  },
  {
    intent: "add_location",
    patterns: [/\b(add|include|also visit|squeeze in|stop in)\b.*\b(a (day|night)|city|to the route)\b/],
  },
  {
    intent: "adjust_trip",
    patterns: [
      /\b(make (it|this|the|day)|change|adjust|rebalance|swap|replace|less|more|lighten|shorten|extend)\b/,
      /\b(too (packed|busy|tiring|much)|slower|faster|easier)\b/,
    ],
  },
  {
    intent: "create_trip",
    patterns: [
      /\b(plan|build|create|design|draft|make me)\b.*\b(trip|itinerary|days?|route|vacation|holiday)\b/,
      /\b(first (time|trip)|\d+\s*(day|days|week|weeks)\b.*\b(china|beijing|shanghai|trip))\b/,
      /\b(i (want|would like) to (go|travel|visit))\b/,
    ],
  },
];

export function classifyIntent(message: string): ButlerIntent {
  const normalized = message.toLowerCase().trim();
  if (!normalized) return "unclear";

  for (const rule of RULES) {
    if (rule.patterns.some((pattern) => pattern.test(normalized))) {
      return rule.intent;
    }
  }
  return "unclear";
}
