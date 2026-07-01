import type { ReactNode } from "react";
import type { TripState } from "@/lib/types/trip";

export function TripSummary({ trip, actions }: { trip: TripState; actions?: ReactNode }) {
  const route = trip.summary.destinations.join(" -> ");

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
        <span>{trip.summary.confidence}</span>
      </div>
      {actions ? <div className="trip-summary__actions">{actions}</div> : null}
    </header>
  );
}
