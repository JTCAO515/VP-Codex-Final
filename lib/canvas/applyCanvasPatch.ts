import type { CanvasPatch, TripDay, TripState } from "@/lib/types/trip";

/**
 * Diffs the day-by-day itinerary the request was made with against the
 * patch's new days array (which, per applyCanvasPatch below, always
 * *replaces* the previous array wholesale when present — models don't emit
 * partial day updates). Returns the day numbers that were added, removed, or
 * had their content change, so clients can offer a "view updated day" link
 * without each platform re-implementing the same diff.
 */
export function computeAffectedDays(previousDays: TripDay[], nextDays: TripDay[] | undefined): number[] {
  if (!nextDays) return [];

  const previousByDay = new Map(previousDays.map((day) => [day.day, day]));
  const nextDayNumbers = new Set(nextDays.map((day) => day.day));
  const affected = new Set<number>();

  for (const day of nextDays) {
    const before = previousByDay.get(day.day);
    if (!before || JSON.stringify(before) !== JSON.stringify(day)) {
      affected.add(day.day);
    }
  }
  for (const day of previousDays) {
    if (!nextDayNumbers.has(day.day)) {
      affected.add(day.day);
    }
  }

  return Array.from(affected).sort((a, b) => a - b);
}

export function applyCanvasPatch(current: TripState, patch: CanvasPatch): TripState {
  const alertMap = new Map(current.alerts.map((alert) => [`${alert.type}:${alert.title}`, alert]));

  for (const alert of patch.butlerAlerts ?? []) {
    alertMap.set(`${alert.type}:${alert.title}`, alert);
  }

  return {
    summary: {
      ...current.summary,
      ...patch.tripSummary,
    },
    days: patch.days ?? current.days,
    alerts: Array.from(alertMap.values()),
    lastUpdatedReason: patch.reason,
  };
}
