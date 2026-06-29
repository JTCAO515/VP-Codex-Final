export type RailCategory =
  | "visa"
  | "payment"
  | "hotel"
  | "transport"
  | "weather"
  | "risk"
  | "language"
  | "emergency";

export interface RailItem {
  id: string;
  category: RailCategory;
  title: string;
  detail: string;
  severity: "info" | "warning" | "urgent";
}

export type DayPeriod = "morning" | "afternoon" | "evening";

export interface DayActivityBlock {
  period: DayPeriod;
  title: string;
  imageHint: string;
}

export interface DayCard {
  day: number;
  city: string;
  activities: DayActivityBlock[];
  food: string[];
  hotel: string;
  transport: string;
  pace: "relaxed" | "moderate" | "packed";
  budgetNote: string;
}

export interface TripSummary {
  route: string[];
  startDate: string | null;
  endDate: string | null;
  travelers: number;
  days: number;
}

export interface ChatMessage {
  id: string;
  role: "user" | "assistant";
  content: string;
}

export type CardAction = "upsert" | "delete";

export interface DayInstruction {
  day: number;
  action: CardAction;
  data?: Omit<DayCard, "day">;
}

export interface RailInstruction {
  id: string;
  action: CardAction;
  data?: Omit<RailItem, "id">;
}

export interface TripInstructionBlock {
  days?: DayInstruction[];
  rails?: RailInstruction[];
  summary?: Partial<TripSummary>;
}
