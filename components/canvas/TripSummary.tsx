import type { ReactNode } from "react";
import type { TripState } from "@/lib/types/trip";

const confidenceLabels: Record<string, string> = {
  Draft: "Taking shape",
  Refined: "Looking good",
  Ready: "Travel-ready",
  "Ready to save": "Travel-ready",
};

function getTripReadiness(trip: TripState) {
  const checks = [
    { label: "Route", complete: trip.summary.destinations.length > 0 },
    { label: "Daily plan", complete: trip.days.length > 0 && trip.days.every((day) => day.blocks.length >= 3) },
    { label: "Stay area", complete: trip.days.length > 0 && trip.days.every((day) => Boolean(day.stay?.trim())) },
    { label: "Transport", complete: trip.days.length > 0 && trip.days.every((day) => Boolean(day.transport?.trim())) },
    { label: "Travel-ready", complete: ["Refined", "Ready", "Ready to save"].includes(trip.summary.confidence) },
  ];
  const completeCount = checks.filter((check) => check.complete).length;

  return {
    checks,
    score: Math.round((completeCount / checks.length) * 100),
  };
}

export function TripSummary({ trip, actions }: { trip: TripState; actions?: ReactNode }) {
  const route = trip.summary.destinations.join(" -> ");
  const confidenceLabel = confidenceLabels[trip.summary.confidence] ?? trip.summary.confidence;
  const readiness = getTripReadiness(trip);

  return (
    <header className="trip-summary">
      <div className="trip-summary__route-mark" aria-hidden="true">
        <span />
      </div>
      <div className="trip-summary__copy">
        <p>{trip.summary.title}</p>
        <h2>{route}</h2>
        <span>{trip.lastUpdatedReason}</span>
      </div>
      <div className="trip-summary__meta" aria-label="Trip summary">
        <span>{trip.summary.durationDays} days</span>
        <span>{trip.summary.pace} pace</span>
        <span>{trip.summary.travelerStyle}</span>
        <span>{confidenceLabel}</span>
      </div>
      <div className="trip-summary__readiness" aria-label={`Trip readiness ${readiness.score}%`}>
        <div>
          <span>Trip readiness</span>
          <strong>{readiness.score}%</strong>
        </div>
        <meter min={0} max={100} value={readiness.score}>
          {readiness.score}%
        </meter>
        <ul aria-label="Readiness checklist">
          {readiness.checks.map((check) => (
            <li data-complete={check.complete ? "true" : "false"} key={check.label}>
              {check.label}
            </li>
          ))}
        </ul>
      </div>
      {actions ? <div className="trip-summary__actions">{actions}</div> : null}
    </header>
  );
}
