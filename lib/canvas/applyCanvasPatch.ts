import type { CanvasPatch, TripState } from "@/lib/types/trip";

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
