import type { TripDay } from "@/lib/types/trip";

export type QuickActionKind = "lighten" | "add_food" | "swap_morning" | "add_rest";

export interface QuickActionDefinition {
  kind: QuickActionKind;
  label: string;
}

export const DAY_QUICK_ACTIONS: QuickActionDefinition[] = [
  { kind: "lighten", label: "Lighten" },
  { kind: "add_food", label: "Add food" },
  { kind: "swap_morning", label: "Swap morning" },
  { kind: "add_rest", label: "Add rest" },
];

/**
 * Builds the prefab natural-language message sent through the normal AI
 * pipeline for a Day-card quick action. The day number and city are always
 * included so the model (or mock fallback) never has to guess which day the
 * traveler means.
 */
export function buildQuickActionMessage(kind: QuickActionKind, day: TripDay): string {
  switch (kind) {
    case "lighten":
      return `Make Day ${day.day} in ${day.city} lighter — remove one activity and slow the pace.`;
    case "add_food":
      return `Add one more food stop to Day ${day.day} in ${day.city}.`;
    case "swap_morning":
      return `Replace the morning activity on Day ${day.day} in ${day.city} with a different option that fits my preferences.`;
    case "add_rest":
      return `Add a rest break to Day ${day.day} in ${day.city} — keep the day easier.`;
    default:
      return `Adjust Day ${day.day} in ${day.city}.`;
  }
}
