import type { TripState } from "@/lib/types/trip";

export function TripSummary({ trip }: { trip: TripState }) {
  return (
    <header className="trip-summary">
      <div>
        <p className="section-kicker">Live Trip Canvas</p>
        <h1>{trip.summary.title}</h1>
      </div>
      <div className="trip-summary__meta" aria-label="Trip summary">
        <span>{trip.summary.durationDays} days</span>
        <span>{trip.summary.destinations.join(" + ")}</span>
        <span>{trip.summary.pace} pace</span>
        <span>{trip.summary.travelerStyle}</span>
      </div>
      <p className="trip-summary__confidence">{trip.summary.confidence}</p>
      <p className="trip-summary__reason">{trip.lastUpdatedReason}</p>
    </header>
  );
}
