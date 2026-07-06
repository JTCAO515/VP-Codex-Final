export type Pace = "Light" | "Balanced" | "Relaxed" | "Packed";
export type AlertPriority = "high" | "medium" | "low";
export type AlertType =
  | "visa"
  | "payment"
  | "booking"
  | "transport"
  | "weather"
  | "language"
  | "risk"
  | "emergency";

export interface TripBlock {
  time: "Morning" | "Afternoon" | "Evening" | "Flexible";
  title: string;
  description: string;
  /** Optional short checklist sub-items for this block. Absent for most AI/mock output today — the UI falls back to rendering `description` as a single bullet. */
  highlights?: string[];
  /** Optional real photo URL (e.g. from a future POI provider). Never fabricated client-side. */
  photoUrl?: string;
  /**
   * Optional operational POI fields for day-detail execution. These are
   * additive so older saved trips and provider responses remain valid.
   */
  address?: string;
  chineseAddress?: string;
  phone?: string;
  openingHours?: string;
  mapUrl?: string;
  bookingUrl?: string;
  bookingCandidates?: BookingCandidate[];
  sourceLabel?: string;
  coordinates?: {
    lat: number;
    lng: number;
  };
}

export interface BookingCandidate {
  id: string;
  kind: "hotel" | "ticket" | "transport" | "restaurant";
  label: string;
  provider: string;
  status: "info-only" | "planned";
  note: string;
  url?: string;
  priceHint?: string;
}

export interface TripDay {
  day: number;
  city: string;
  pace: Pace;
  blocks: TripBlock[];
  food: string[];
  stay: string;
  transport: string;
  note: string;
  status?: "new" | "revised" | "needs-confirmation";
}

export interface ButlerAlert {
  type: AlertType;
  priority: AlertPriority;
  title: string;
  body: string;
  action: string;
  /**
   * Operational checklist state (e.g. "Before you fly" prep list), not
   * itinerary content. Toggling this is a local UI action and does not need
   * to route through the AI pipeline — see AGENTS.md v0.2.7 note.
   */
  done?: boolean;
}

export interface TripSummary {
  title: string;
  durationDays: number;
  pace: Pace;
  travelerStyle: string;
  destinations: string[];
  confidence: "Draft" | "Refined" | "Ready to save";
}

export interface TripState {
  summary: TripSummary;
  days: TripDay[];
  alerts: ButlerAlert[];
  lastUpdatedReason: string;
}

export interface CanvasPatch {
  intent: "create_trip" | "adjust_trip" | "add_alerts";
  assistantMessage: string;
  assistantResponse?: AssistantResponse;
  tripSummary?: Partial<TripSummary>;
  days?: TripDay[];
  butlerAlerts?: ButlerAlert[];
  reason: string;
  /**
   * Day numbers this patch actually added, removed, or changed the content
   * of, computed server-side by diffing against the trip state the request
   * was made with (see computeAffectedDays in lib/canvas/applyCanvasPatch.ts).
   * Empty/absent means this patch didn't touch the day-by-day itinerary
   * (e.g. an add_alerts-only reply) — clients use this to decide whether to
   * offer a "view updated day" link back to Trips, so a plain-text answer
   * never claims to have changed the itinerary.
   */
  affectedDays?: number[];
}

export interface AssistantResponse {
  headline: string;
  body: string;
  highlights: string[];
  watchOut?: string;
  nextStep: string;
  toolCards?: InlineToolCard[];
  /**
   * Real Explore POIs the Butler's answer text refers to (Issue #50,
   * Chat↔Explore bridge). Populated server-side by matching names in
   * headline/body/highlights against the request's real toolContext POIs —
   * never model-generated, so a ref always points at something that exists.
   * Empty/absent when no real POI was mentioned; clients must not render a
   * placeholder card in that case.
   */
  exploreRefs?: ExploreRef[];
}

/** One real Explore POI referenced by an AssistantResponse. See exploreRefs. */
export interface ExploreRef {
  /** Raw Amap POI id (matches ButlerToolPoi.id / mobile AmapPoiJson.id before the "amap-" prefix). */
  amapPoiId: string;
  name: string;
  cityId: string;
  category: "attractions" | "food" | "stays";
  subcategory?: string;
  rating?: number;
  pricePerPerson?: number;
}

export interface InlineToolCard {
  id: string;
  categoryId: string;
  title: string;
  summary: string;
  items: string[];
  nextAction: string;
  href?: string;
  tone?: "info" | "warning" | "success";
  sourceLabel?: string;
}

/** One line item in the post-patch "Change Digest" card (see diffTripState). */
export interface ChangeDigestEntry {
  kind: "added" | "revised" | "removed" | "alert";
  dayNumber?: number;
  label: string;
}

export interface ChatMessage {
  id: string;
  role: "user" | "assistant";
  content: string;
  response?: AssistantResponse;
  /** Present only on assistant messages that actually changed the canvas. */
  changeDigest?: ChangeDigestEntry[];
  /** ISO timestamp set at creation time. Optional so older saved/loaded messages without it still render. */
  createdAt?: string;
  isError?: boolean;
}
