// Shared prompt building + response parsing for the Butler orchestrator.
// Kept separate from lib/ai/deepseekButler.ts (the legacy single-provider path)
// so the orchestrator can drive any provider with identical prompts and parsing.

import { createMockButlerPatch } from "@/lib/mock-ai/mockButler";
import { safeParseLlmJson } from "@/lib/ai/jsonRepair";
import type { ButlerToolContext } from "@/lib/ai/toolContext";
import type { UserPreferenceProfile } from "@/lib/ai/preferenceProfile";
import type { AssistantResponse, CanvasPatch, ChatMessage, InlineToolCard, TripDay, TripState } from "@/lib/types/trip";

const allowedIntents = new Set<CanvasPatch["intent"]>(["create_trip", "adjust_trip", "add_alerts"]);

export const defaultSuggestions = [
  "Can you make the pace easier?",
  "What should we book first?",
  "Add more local food stops",
  "Keep hotels convenient",
];

export function buildSystemPrompt(): string {
  return [
    // Persona — a knowledgeable local friend, not a call-center script.
    "You are VisePanda, a warm, practical AI travel butler for foreign travelers in China — like a knowledgeable local friend: professional, specific, honest, never fluffy or salesy.",
    // Constitution — hard rules that outrank everything below.
    "HARD RULES:",
    "(1) Never invent China facts (opening hours, prices, visa rules). When unsure, say so briefly and name an official source to verify.",
    "(2) Bookings are info-only: never claim you can book, pay, reserve, or hold inventory.",
    "(3) Ask at most ONE clarifying question per reply, and only when missing information would make the plan clearly wrong; otherwise make a sensible assumption and state it.",
    "(4) Write assistantMessage/assistantResponse/suggestions in the same language as the user's latest message; JSON keys and enum values always stay in English.",
    "(5) If the message signals danger, injury, theft, or a lost passport, lead with immediate practical safety steps (110 police / 120 ambulance / embassy) before anything else.",
    "(6) When the user corrects you, accept the correction plainly and adjust — never argue or repeat the mistake.",
    // Output contract.
    "Return only valid json for a live itinerary canvas patch.",
    'Example json shape: {"intent":"adjust_trip","assistantMessage":"...","assistantResponse":{"headline":"...","body":"...","highlights":["..."],"watchOut":"...","nextStep":"..."},"reason":"...","suggestions":["...","..."],"tripSummary":{"confidence":"Refined"},"days":[{"day":1,"city":"Beijing","pace":"Balanced","blocks":[{"time":"Morning","title":"Tiananmen Square","description":"..."},{"time":"Afternoon","title":"...","description":"..."}],"food":["..."],"stay":"...","transport":"...","note":"..."}],"butlerAlerts":[]}.',
    "The json shape must be: intent, assistantMessage, assistantResponse, reason, suggestions, optional tripSummary, optional days, optional butlerAlerts.",
    "IMPORTANT: each day object MUST have a flat `blocks` array (day, city, pace, blocks, food, stay, transport, note) — never group activities under separate morning/afternoon/evening objects, and never nest an `activities` array. Every block is one flat object: {time: one of Morning/Afternoon/Evening/Flexible, title, description}. Whenever the itinerary changes (intent create_trip or adjust_trip) you MUST return the COMPLETE updated days array — never a partial delta and never omit days — and set tripSummary.title, tripSummary.durationDays, and tripSummary.destinations so the live canvas reflects the plan.",
    "Trip blocks may include optional operational POI fields when known: address, chineseAddress, phone, openingHours, mapUrl, bookingUrl, bookingCandidates, sourceLabel, and coordinates {lat,lng}. Only include them when sourced from provided context or common static fallback; never invent official booking availability.",
    "Only omit days when the user's message does not change the itinerary at all (for example a pure factual question).",
    "assistantResponse must have a short headline, one concise body paragraph, 2-4 practical highlights, an optional watchOut, and one concrete, tappable nextStep.",
    "Keep assistantMessage populated as a readable plain-text fallback that combines the same meaning as assistantResponse.",
    "Suggestions must be exactly two short follow-up questions the traveler would naturally tap next — base them on the biggest gaps in the current trip (missing days, no hotel area chosen, undone high-priority alerts) or on the topic just discussed. Never repeat a question that was already answered in recentMessages.",
    "Match reply length to the ask: factual questions get 2-3 tight sentences; itinerary work may use the full structure.",
    "Keep the plan practical for China travel: routing, visas, payment, booking, transport, food, stay areas, and fatigue.",
    "Be concise. Do not include markdown.",
  ].join(" ");
}

/**
 * Prompt-side slimming: photoUrl is a display-only field that can carry very
 * long CDN URLs. Stripping it cuts real tokens from the request on multi-day
 * trips without losing anything the model needs for planning.
 */
function slimTripForPrompt(trip: TripState): TripState {
  return {
    ...trip,
    days: trip.days.map((day) => ({
      ...day,
      blocks: day.blocks.map((block) => {
        const { photoUrl: _photoUrl, ...rest } = block;
        return rest;
      }),
    })),
  };
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
    currentTrip: slimTripForPrompt(currentTrip),
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

/**
 * Some models ignore the "flat blocks array" instruction and instead group
 * activities under morning/afternoon/evening period objects (each holding an
 * `activities` array of `{time, poi:{name,address,note,sourceLabel}, duration}`
 * entries) — a shape closer to what these models saw more often in training.
 * Without this fallback, `normalizeDays` would see no `blocks` field and
 * silently produce an empty array, discarding an entire day the model did
 * generate correctly in substance, just under the wrong key names.
 */
function extractBlocksFromLegacyPeriods(raw: Record<string, unknown>): Record<string, unknown>[] {
  const periods: { key: string; time: string }[] = [
    { key: "morning", time: "Morning" },
    { key: "afternoon", time: "Afternoon" },
    { key: "evening", time: "Evening" },
  ];
  const blocks: Record<string, unknown>[] = [];
  for (const { key, time } of periods) {
    const period = raw[key];
    if (!period || typeof period !== "object") continue;
    const activities = (period as Record<string, unknown>).activities;
    if (!Array.isArray(activities)) continue;
    for (const activity of activities) {
      if (!activity || typeof activity !== "object") continue;
      const entry = activity as Record<string, unknown>;
      const poi = entry.poi && typeof entry.poi === "object" ? (entry.poi as Record<string, unknown>) : {};
      const title = stringValue(poi.name) || stringValue(entry.title);
      if (!title) continue;
      const descriptionParts = [stringValue(poi.note), stringValue(entry.duration) && `Duration: ${stringValue(entry.duration)}`].filter(Boolean);
      blocks.push({
        time,
        title,
        description: descriptionParts.join(". "),
        address: stringValue(poi.address) || undefined,
        sourceLabel: stringValue(poi.sourceLabel) || undefined,
      });
    }
  }
  return blocks;
}

/**
 * Normalize LLM-produced days at the parse boundary (v0.3.18). Real model
 * output can omit any field the TypeScript type claims is required — a day
 * without `blocks` crashed `applyToolContextToPatch` in production
 * ("Cannot read properties of undefined (reading 'map')"), throwing away an
 * otherwise-winning patch. Same philosophy as the Android TripJson
 * normalizeNulls fix: nulls/holes must never leave the decode layer.
 *
 * v0.3.19: also recovers content when the model used the legacy
 * morning/afternoon/evening/activities shape instead of `blocks` (see
 * extractBlocksFromLegacyPeriods) — real generated itinerary content was
 * being silently dropped to an empty day rather than a decode error, which
 * is worse: it looks like the request succeeded but the canvas stays empty.
 * Only the canonical TripDay fields are returned (no `...raw` passthrough),
 * so provider-specific scratch fields (dayNumber, date, morning, etc.) never
 * reach the client.
 */
function normalizeDays(value: unknown): TripDay[] | undefined {
  if (!Array.isArray(value)) return undefined;
  const days = value
    .filter((item): item is Record<string, unknown> => Boolean(item) && typeof item === "object")
    .map((raw, index) => {
      const rawBlocks = Array.isArray(raw.blocks) && raw.blocks.length > 0 ? raw.blocks : extractBlocksFromLegacyPeriods(raw);
      const blocks = rawBlocks
        .filter((block): block is Record<string, unknown> => Boolean(block) && typeof block === "object")
        .map((block) => ({
          ...block,
          time: typeof block.time === "string" && block.time ? block.time : "Flexible",
          title: stringValue(block.title) || "Untitled stop",
          description: stringValue(block.description),
        }));
      return {
        day: typeof raw.day === "number" ? raw.day : index + 1,
        city: stringValue(raw.city),
        pace: typeof raw.pace === "string" && raw.pace ? raw.pace : "Balanced",
        blocks,
        food: Array.isArray(raw.food) ? raw.food.filter((f): f is string => typeof f === "string") : [],
        stay: stringValue(raw.stay),
        transport: stringValue(raw.transport),
        note: stringValue(raw.note),
      } as unknown as TripDay;
    });
  return days;
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

/**
 * Parse a model's raw JSON string into a validated CanvasPatch. Throws on invalid.
 *
 * v0.3.17: parsing is now tolerant of truncated/fenced output (safeParseLlmJson)
 * — a completion cut off at max_tokens no longer sinks the whole answer — and a
 * quality gate rejects create_trip patches without a complete days array, so a
 * reply that *says* it planned a trip but didn't actually patch the canvas
 * loses the race to a provider that did the work.
 */
export function parseButlerPatch(
  content: string,
  fallbackSuggestions: string[],
): { patch: CanvasPatch; suggestions: string[] } {
  const parsed = safeParseLlmJson(content) as Partial<CanvasPatch> & { suggestions?: unknown };

  if (!parsed.intent || !allowedIntents.has(parsed.intent)) {
    throw new Error("Butler response did not include a valid intent.");
  }
  if (typeof parsed.assistantMessage !== "string" || !parsed.assistantMessage.trim()) {
    throw new Error("Butler response did not include assistantMessage.");
  }
  if (typeof parsed.reason !== "string" || !parsed.reason.trim()) {
    throw new Error("Butler response did not include reason.");
  }
  if (parsed.intent === "create_trip" && (!Array.isArray(parsed.days) || parsed.days.length === 0)) {
    throw new Error("create_trip patch is missing the complete days array.");
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
    days: normalizeDays(parsed.days),
    butlerAlerts: Array.isArray(parsed.butlerAlerts) ? parsed.butlerAlerts : undefined,
  };

  return { patch, suggestions };
}

/**
 * Fallback suggestions for a given message + trip. v0.3.17: gap-driven first —
 * the biggest real hole in the current trip beats a canned template, so even
 * mock-mode replies steer the traveler toward the next useful step.
 */
export function fallbackSuggestionsFor(message: string, currentTrip: TripState): string[] {
  const gaps: string[] = [];
  if (!currentTrip.days.length) {
    gaps.push("Plan my first days in China");
  } else {
    if (currentTrip.days.some((day) => !day.stay?.trim())) {
      gaps.push("Which hotel area should I pick?");
    }
    if (currentTrip.alerts.some((alert) => !alert.done && alert.priority === "high")) {
      gaps.push("What should I prepare first?");
    }
  }
  const base = createMockSuggestions(message, createMockButlerPatch(message, currentTrip));
  return [...gaps, ...base].slice(0, 2);
}
