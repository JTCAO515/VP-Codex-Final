// Shared prompt building + response parsing for the Butler orchestrator.
// Kept separate from lib/ai/deepseekButler.ts (the legacy single-provider path)
// so the orchestrator can drive any provider with identical prompts and parsing.

import { createMockButlerPatch } from "@/lib/mock-ai/mockButler";
import type { ButlerToolContext } from "@/lib/ai/toolContext";
import type { UserPreferenceProfile } from "@/lib/ai/preferenceProfile";
import type { AssistantResponse, CanvasPatch, ChatMessage, InlineToolCard, TripState } from "@/lib/types/trip";

const allowedIntents = new Set<CanvasPatch["intent"]>(["create_trip", "adjust_trip", "add_alerts"]);

export const defaultSuggestions = [
  "Can you make the pace easier?",
  "What should we book first?",
  "Add more local food stops",
  "Keep hotels convenient",
];

export function buildSystemPrompt(): string {
  return [
    "You are VisePanda, an AI China travel butler for foreign travelers.",
    "Return only valid json for a live itinerary canvas patch.",
    'Example json shape: {"intent":"adjust_trip","assistantMessage":"...","assistantResponse":{"headline":"...","body":"...","highlights":["..."],"watchOut":"...","nextStep":"..."},"reason":"...","suggestions":["...","..."],"tripSummary":{"confidence":"Refined"},"days":[],"butlerAlerts":[]}.',
    "The json shape must be: intent, assistantMessage, assistantResponse, reason, suggestions, optional tripSummary, optional days, optional butlerAlerts.",
    "IMPORTANT: whenever the itinerary changes (intent create_trip or adjust_trip) you MUST return the COMPLETE updated days array (every day, morning/afternoon/evening blocks, food, stay, transport, note) — never a partial delta and never omit days — and set tripSummary.title, tripSummary.durationDays, and tripSummary.destinations so the live canvas reflects the plan.",
    "Trip blocks may include optional operational POI fields when known: address, chineseAddress, phone, openingHours, mapUrl, bookingUrl, bookingCandidates, sourceLabel, and coordinates {lat,lng}. Only include them when sourced from provided context or common static fallback; never invent official booking availability.",
    "Only omit days when the user's message does not change the itinerary at all (for example a pure factual question).",
    "assistantResponse must have a short headline, one concise body paragraph, 2-4 practical highlights, an optional watchOut, and one nextStep.",
    "Keep assistantMessage populated as a readable plain-text fallback that combines the same meaning as assistantResponse.",
    "Suggestions must be exactly two short next questions based on the user message, recent conversation, and current trip state.",
    "Keep the plan practical for China travel: routing, visas, payment, booking, transport, food, stay areas, and fatigue.",
    "Use concise English. Do not include markdown.",
  ].join(" ");
}

export function buildUserPrompt(
  message: string,
  currentTrip: TripState,
  recentMessages: ChatMessage[] = [],
  context: { preferenceProfile?: UserPreferenceProfile; toolContext?: ButlerToolContext } = {},
): string {
  return JSON.stringify({
    userMessage: message,
    recentMessages: recentMessages.slice(-8),
    currentTrip,
    preferenceProfile: context.preferenceProfile,
    liveToolContext: context.toolContext,
    patchRules: {
      intent: ["create_trip", "adjust_trip", "add_alerts"],
      confidence: ["Draft", "Refined", "Ready to save"],
      pace: ["Light", "Balanced", "Relaxed", "Packed"],
      alertPriority: ["high", "medium", "low"],
      alertType: ["visa", "payment", "booking", "transport", "weather", "language", "risk", "emergency"],
      suggestions: "Return exactly two short context-aware question strings.",
      liveToolContext: "If liveToolContext is present, prefer those real POIs over invented place names and mention important rating/price/open-hour caveats in watchOut.",
      tripBlockPoiFields: "When a live POI is used in a day block, copy safe operational fields into that block when present: address, phone, openingHours, sourceLabel, coordinates, mapUrl, bookingUrl, bookingCandidates. Use chineseAddress only when the context provides Chinese text. bookingCandidates are info-only and must not imply checkout or inventory.",
      preferenceProfile: "Respect stored traveler preferences without interrogating the user; ask at most one clarifying question only if missing information would make the plan clearly wrong.",
    },
  });
}

export function normalizeSuggestions(value: unknown, fallback: string[]): string[] {
  if (!Array.isArray(value)) return fallback.slice(0, 2);
  const suggestions = value
    .filter((item): item is string => typeof item === "string" && item.trim().length > 0)
    .slice(0, 2);
  return suggestions.length === 2 ? suggestions : fallback.slice(0, 2);
}

export function createMockSuggestions(message: string, patch: CanvasPatch): string[] {
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

function stringValue(value: unknown): string {
  return typeof value === "string" ? value.trim() : "";
}

function stringArrayValue(value: unknown): string[] {
  if (!Array.isArray(value)) return [];
  return value.filter((item): item is string => typeof item === "string" && item.trim().length > 0).map((item) => item.trim());
}

function normalizeToolCards(value: unknown): InlineToolCard[] | undefined {
  if (!Array.isArray(value)) return undefined;
  const cards = value
    .map((item): InlineToolCard | null => {
      if (!item || typeof item !== "object") return null;
      const candidate = item as Partial<InlineToolCard>;
      const id = stringValue(candidate.id);
      const categoryId = stringValue(candidate.categoryId);
      const title = stringValue(candidate.title);
      const summary = stringValue(candidate.summary);
      const nextAction = stringValue(candidate.nextAction);
      const items = stringArrayValue(candidate.items).slice(0, 5);
      if (!id || !categoryId || !title || !summary || !nextAction || items.length === 0) return null;
      const href = stringValue(candidate.href);
      const sourceLabel = stringValue(candidate.sourceLabel);
      const tone = candidate.tone === "warning" || candidate.tone === "success" || candidate.tone === "info" ? candidate.tone : undefined;
      const card: InlineToolCard = {
        id,
        categoryId,
        title,
        summary,
        items,
        nextAction,
        href: href || undefined,
        tone,
        sourceLabel: sourceLabel || undefined,
      };
      return card;
    })
    .filter((item): item is InlineToolCard => Boolean(item));
  return cards.length ? cards.slice(0, 3) : undefined;
}

function normalizeAssistantResponse(
  value: unknown,
  assistantMessage: string,
  suggestions: string[],
): AssistantResponse {
  if (value && typeof value === "object") {
    const candidate = value as Partial<AssistantResponse>;
    const headline = stringValue(candidate.headline);
    const body = stringValue(candidate.body);
    const highlights = stringArrayValue(candidate.highlights).slice(0, 4);
    const watchOut = stringValue(candidate.watchOut);
    const nextStep = stringValue(candidate.nextStep);
    const toolCards = normalizeToolCards(candidate.toolCards);

    if (headline && body && nextStep) {
      return {
        headline,
        body,
        highlights,
        watchOut: watchOut || undefined,
        nextStep,
        toolCards,
      };
    }
  }

  return {
    headline: assistantMessage,
    body: "",
    highlights: [],
    nextStep: suggestions[0] ?? "Continue refining this trip.",
  };
}

/** Parse a model's raw JSON string into a validated CanvasPatch. Throws on invalid. */
export function parseButlerPatch(
  content: string,
  fallbackSuggestions: string[],
): { patch: CanvasPatch; suggestions: string[] } {
  const parsed = JSON.parse(content) as Partial<CanvasPatch> & { suggestions?: unknown };

  if (!parsed.intent || !allowedIntents.has(parsed.intent)) {
    throw new Error("Butler response did not include a valid intent.");
  }
  if (typeof parsed.assistantMessage !== "string" || !parsed.assistantMessage.trim()) {
    throw new Error("Butler response did not include assistantMessage.");
  }
  if (typeof parsed.reason !== "string" || !parsed.reason.trim()) {
    throw new Error("Butler response did not include reason.");
  }

  const suggestions = normalizeSuggestions(parsed.suggestions, fallbackSuggestions);
  const assistantResponse = normalizeAssistantResponse(
    parsed.assistantResponse,
    parsed.assistantMessage,
    suggestions,
  );

  const patch: CanvasPatch = {
    intent: parsed.intent,
    assistantMessage: parsed.assistantMessage,
    assistantResponse,
    reason: parsed.reason,
    tripSummary: parsed.tripSummary,
    days: Array.isArray(parsed.days) ? parsed.days : undefined,
    butlerAlerts: Array.isArray(parsed.butlerAlerts) ? parsed.butlerAlerts : undefined,
  };

  return { patch, suggestions };
}

/** Fallback suggestions derived from the mock butler for a given message + trip. */
export function fallbackSuggestionsFor(message: string, currentTrip: TripState): string[] {
  return createMockSuggestions(message, createMockButlerPatch(message, currentTrip));
}
