import type { TripBlock, TripDay } from "@/lib/types/trip";

export type QuickActionKind =
  | "add_activity"
  | "find_food"
  | "get_tickets"
  | "lighten"
  | "swap_morning"
  | "add_rest";

export interface QuickActionDefinition {
  kind: QuickActionKind;
  label: string;
}

/** Always-visible primary actions on a Day card, matching the traveler-facing action row. */
export const DAY_PRIMARY_ACTIONS: QuickActionDefinition[] = [
  { kind: "add_activity", label: "Add to day" },
  { kind: "find_food", label: "Find food nearby" },
  { kind: "get_tickets", label: "Get tickets" },
];

/** Secondary pace/structure actions, tucked behind the "…" overflow menu. */
export const DAY_SECONDARY_ACTIONS: QuickActionDefinition[] = [
  { kind: "lighten", label: "Lighten this day" },
  { kind: "swap_morning", label: "Swap morning" },
  { kind: "add_rest", label: "Add rest" },
];

export const DAY_QUICK_ACTIONS: QuickActionDefinition[] = [...DAY_PRIMARY_ACTIONS, ...DAY_SECONDARY_ACTIONS];

/**
 * Builds the prefab natural-language message sent through the normal AI
 * pipeline for a Day-card quick action. The day number and city are always
 * included so the model (or mock fallback) never has to guess which day the
 * traveler means.
 */
export function buildQuickActionMessage(kind: QuickActionKind, day: TripDay): string {
  switch (kind) {
    case "add_activity":
      return `Add one more activity to Day ${day.day} in ${day.city}.`;
    case "find_food":
      return `Suggest good local food near the Day ${day.day} plan in ${day.city}.`;
    case "get_tickets":
      return `What tickets or bookings do I need for Day ${day.day} in ${day.city}, and how do I get them?`;
    case "lighten":
      return `Make Day ${day.day} in ${day.city} lighter — remove one activity and slow the pace.`;
    case "swap_morning":
      return `Replace the morning activity on Day ${day.day} in ${day.city} with a different option that fits my preferences.`;
    case "add_rest":
      return `Add a rest break to Day ${day.day} in ${day.city} — keep the day easier.`;
    default:
      return `Adjust Day ${day.day} in ${day.city}.`;
  }
}

export function buildScheduleCandidateMessage(day: TripDay, block: TripBlock): string {
  return `Schedule ${block.title} into Day ${day.day} in ${day.city}. Keep the route practical, choose the best time slot, and explain what changed.`;
}
