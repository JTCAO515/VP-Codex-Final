import { createMockButlerPatch } from "@/lib/mock-ai/mockButler";
import type { CanvasPatch, ChatMessage, TripState } from "@/lib/types/trip";

type FetchLike = typeof fetch;

interface RequestButlerPatchInput {
  currentTrip: TripState;
  env?: NodeJS.ProcessEnv | Record<string, string | undefined>;
  fetchImpl?: FetchLike;
  message: string;
  recentMessages?: ChatMessage[];
}

export interface ButlerPatchResult {
  mode: "deepseek" | "mock";
  patch: CanvasPatch;
  suggestions: string[];
  fallbackReason?: string;
}

const DEFAULT_BASE_URL = "https://api.deepseek.com";
const DEFAULT_MODEL = "deepseek-v4-flash";
const allowedIntents = new Set<CanvasPatch["intent"]>(["create_trip", "adjust_trip", "add_alerts"]);
const defaultSuggestions = [
  "Can you make the pace easier?",
  "What should we book first?",
  "Add more local food stops",
  "Keep hotels convenient",
];

function readConfig(env: RequestButlerPatchInput["env"] = process.env) {
  const apiKey = env.DEEPSEEK_API_KEY?.trim() || env.AI_API_KEY?.trim();
  const baseUrl = (env.DEEPSEEK_BASE_URL?.trim() || env.AI_BASE_URL?.trim() || DEFAULT_BASE_URL).replace(/\/$/, "");
  const model = env.DEEPSEEK_MODEL?.trim() || env.AI_MODEL?.trim() || DEFAULT_MODEL;

  return { apiKey, baseUrl, model };
}

function buildSystemPrompt() {
  return [
    "You are VisePanda, an AI China travel butler for foreign travelers.",
    "Return only valid json for a live itinerary canvas patch.",
    'Example json shape: {"intent":"adjust_trip","assistantMessage":"...","reason":"...","suggestions":["...","..."],"tripSummary":{"confidence":"Refined"},"days":[],"butlerAlerts":[]}.',
    "The json shape must be: intent, assistantMessage, reason, suggestions, optional tripSummary, optional days, optional butlerAlerts.",
    "Trip blocks may include optional operational POI fields when known: address, chineseAddress, phone, openingHours, mapUrl, bookingUrl, bookingCandidates, sourceLabel, and coordinates {lat,lng}. Only include them when sourced or safe static fallback; never invent official booking availability.",
    "Suggestions must be exactly two short next questions based on the user message, recent conversation, and current trip state.",
    "Keep the plan practical for China travel: routing, visas, payment, booking, transport, food, stay areas, and fatigue.",
    "Use concise English. Do not include markdown.",
  ].join(" ");
}

function buildUserPrompt(message: string, currentTrip: TripState, recentMessages: ChatMessage[] = []) {
  return JSON.stringify({
    userMessage: message,
    recentMessages: recentMessages.slice(-8),
    currentTrip,
    patchRules: {
      intent: ["create_trip", "adjust_trip", "add_alerts"],
      confidence: ["Draft", "Refined", "Ready to save"],
      pace: ["Light", "Balanced", "Relaxed", "Packed"],
      alertPriority: ["high", "medium", "low"],
      alertType: ["visa", "payment", "booking", "transport", "weather", "language", "risk", "emergency"],
      suggestions: "Return exactly two short context-aware question strings.",
      tripBlockPoiFields: "When a POI is used in a day block, keep safe operational fields if known: address, phone, openingHours, sourceLabel, coordinates, mapUrl, bookingUrl, bookingCandidates, and chineseAddress only when you have Chinese text. bookingCandidates are info-only and must not imply checkout or inventory.",
    },
  });
}

function normalizeSuggestions(value: unknown, fallback: string[]) {
  if (!Array.isArray(value)) return fallback.slice(0, 2);

  const suggestions = value.filter((item): item is string => typeof item === "string" && item.trim().length > 0).slice(0, 2);

  return suggestions.length === 2 ? suggestions : fallback.slice(0, 2);
}

function createMockSuggestions(message: string, patch: CanvasPatch) {
  const normalized = message.toLowerCase();

  if (normalized.includes("visa") || normalized.includes("payment")) {
    return ["What documents should I prepare?", "How should I pay in China?"];
  }

  if (patch.days?.length) {
    return ["Can you make one day lighter?", "Which hotels are most convenient?"];
  }

  if (normalized.includes("food") || normalized.includes("restaurant")) {
    return ["Add one food market stop?", "Keep dinners near the hotel?"];
  }

  return defaultSuggestions.slice(0, 2);
}

function parseDeepSeekPatch(content: string, fallbackSuggestions: string[]): { patch: CanvasPatch; suggestions: string[] } {
  const parsed = JSON.parse(content) as Partial<CanvasPatch> & { suggestions?: unknown };

  if (!parsed.intent || !allowedIntents.has(parsed.intent)) {
    throw new Error("DeepSeek response did not include a valid intent.");
  }

  if (typeof parsed.assistantMessage !== "string" || !parsed.assistantMessage.trim()) {
    throw new Error("DeepSeek response did not include assistantMessage.");
  }

  if (typeof parsed.reason !== "string" || !parsed.reason.trim()) {
    throw new Error("DeepSeek response did not include reason.");
  }

  const patch = {
    intent: parsed.intent,
    assistantMessage: parsed.assistantMessage,
    reason: parsed.reason,
    tripSummary: parsed.tripSummary,
    days: Array.isArray(parsed.days) ? parsed.days : undefined,
    butlerAlerts: Array.isArray(parsed.butlerAlerts) ? parsed.butlerAlerts : undefined,
  };

  return {
    patch,
    suggestions: normalizeSuggestions(parsed.suggestions, fallbackSuggestions),
  };
}

function createFallback(message: string, currentTrip: TripState, fallbackReason?: string): ButlerPatchResult {
  const patch = createMockButlerPatch(message, currentTrip);

  return {
    fallbackReason,
    mode: "mock",
    patch,
    suggestions: createMockSuggestions(message, patch),
  };
}

export async function requestButlerPatch({
  currentTrip,
  env = process.env,
  fetchImpl = fetch,
  message,
  recentMessages = [],
}: RequestButlerPatchInput): Promise<ButlerPatchResult> {
  const config = readConfig(env);
  const trimmed = message.trim();

  const fallbackSuggestions = createMockSuggestions(trimmed, createMockButlerPatch(trimmed, currentTrip));

  if (!trimmed) {
    return createFallback(message, currentTrip, "Empty message.");
  }

  if (!config.apiKey) {
    return createFallback(trimmed, currentTrip, "DeepSeek API key is not configured.");
  }

  try {
    const response = await fetchImpl(`${config.baseUrl}/chat/completions`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${config.apiKey}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        model: config.model,
        messages: [
          { role: "system", content: buildSystemPrompt() },
          { role: "user", content: buildUserPrompt(trimmed, currentTrip, recentMessages) },
        ],
        max_tokens: 2200,
        response_format: { type: "json_object" },
        temperature: 0.4,
      }),
    });

    if (!response.ok) {
      return createFallback(trimmed, currentTrip, `DeepSeek returned HTTP ${response.status}.`);
    }

    const data = await response.json();
    const content = data?.choices?.[0]?.message?.content;

    if (typeof content !== "string") {
      return createFallback(trimmed, currentTrip, "DeepSeek response did not include message content.");
    }

    const parsed = parseDeepSeekPatch(content, fallbackSuggestions);

    return {
      mode: "deepseek",
      patch: parsed.patch,
      suggestions: parsed.suggestions,
    };
  } catch (error) {
    const message = error instanceof Error ? error.message : "Unknown DeepSeek provider error.";

    return createFallback(trimmed, currentTrip, message);
  }
}
