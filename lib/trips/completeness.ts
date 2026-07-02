import type { TripState } from "@/lib/types/trip";

export type CompletenessDimensionId = "route" | "stay" | "food" | "transport" | "payment" | "visa";

export interface CompletenessCheck {
  id: CompletenessDimensionId;
  label: string;
  complete: boolean;
}

export interface CompletenessResult {
  checks: CompletenessCheck[];
  score: number;
}

function everyDayHas(trip: TripState, predicate: (day: TripState["days"][number]) => boolean): boolean {
  return trip.days.length > 0 && trip.days.every(predicate);
}

/** True when there is no outstanding (not-done) alert of the given type. Absence of the alert is vacuously complete. */
function noOutstandingAlert(trip: TripState, type: TripState["alerts"][number]["type"]): boolean {
  return trip.alerts.filter((alert) => alert.type === type).every((alert) => alert.done);
}

/**
 * Six-dimension trip readiness score (route/stay/food/transport/payment/visa),
 * a pure function so it is trivial to unit test and reuse in both the
 * TripSummary progress meter and the "Before you fly" prep checklist.
 */
export function calculateTripCompleteness(trip: TripState): CompletenessResult {
  const checks: CompletenessCheck[] = [
    { id: "route", label: "Route", complete: trip.summary.destinations.length > 0 && trip.days.length > 0 },
    { id: "stay", label: "Stay area", complete: everyDayHas(trip, (day) => Boolean(day.stay?.trim())) },
    { id: "food", label: "Food", complete: everyDayHas(trip, (day) => day.food.length > 0) },
    { id: "transport", label: "Transport", complete: everyDayHas(trip, (day) => Boolean(day.transport?.trim())) },
    { id: "payment", label: "Payment", complete: noOutstandingAlert(trip, "payment") },
    { id: "visa", label: "Visa", complete: noOutstandingAlert(trip, "visa") },
  ];

  const completeCount = checks.filter((check) => check.complete).length;

  return {
    checks,
    score: Math.round((completeCount / checks.length) * 100),
  };
}
