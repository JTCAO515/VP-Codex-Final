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
}

export interface AssistantResponse {
  headline: string;
  body: string;
  highlights: string[];
  watchOut?: string;
  nextStep: string;
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
}
