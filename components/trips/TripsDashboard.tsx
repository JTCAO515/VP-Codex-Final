"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { savedTrips, tripStatusLabels, type SavedTrip, type SavedTripStatus } from "@/lib/trips/mockTrips";
import { listTripsForOwner } from "@/lib/supabase/tripsRepository";
import { useSupabaseSession } from "@/lib/supabase/useSupabaseSession";
import type { TripRow } from "@/lib/supabase/schema";

type TripFilter = SavedTripStatus | "all";

const filters: TripFilter[] = ["all", "draft", "ready", "shared"];

function getStatusCopy(status: SavedTripStatus) {
  if (status === "ready") return "Ready to review";
  if (status === "shared") return "Shared draft";
  return "Draft in progress";
}

function tripRowToSavedTrip(row: TripRow): SavedTrip {
  return {
    id: row.id,
    title: row.title,
    route: "",
    dates: "",
    durationDays: 0,
    travelers: "",
    status: row.status,
    updatedAt: `Updated ${new Date(row.updated_at).toLocaleDateString()}`,
    alertCount: 0,
    summary: "Saved from your Chat workspace.",
    highlights: [],
  };
}

export function TripsDashboard() {
  const { configured, loading, session } = useSupabaseSession();
  const [activeFilter, setActiveFilter] = useState<TripFilter>("all");
  const [remoteTrips, setRemoteTrips] = useState<SavedTrip[] | null>(null);

  useEffect(() => {
    if (!configured || loading || !session) {
      setRemoteTrips(null);
      return;
    }

    listTripsForOwner(session.user.id)
      .then((rows) => setRemoteTrips(rows.map(tripRowToSavedTrip)))
      .catch(() => setRemoteTrips([]));
  }, [configured, loading, session]);

  const isSignedIn = configured && !loading && Boolean(session);
  const trips = isSignedIn ? remoteTrips ?? [] : savedTrips;

  const visibleTrips = useMemo(
    () => trips.filter((trip) => activeFilter === "all" || trip.status === activeFilter),
    [trips, activeFilter],
  );
  const totalDays = visibleTrips.reduce((sum, trip) => sum + trip.durationDays, 0);
  const totalAlerts = visibleTrips.reduce((sum, trip) => sum + trip.alertCount, 0);

  return (
    <section className="trips-dashboard" aria-labelledby="trips-title">
      <header className="trips-dashboard__header">
        <div>
          <p className="section-kicker">Trips</p>
          <h1 id="trips-title">Your trips</h1>
          <p>Saved canvases, active drafts, and share-ready China itineraries will live here.</p>
        </div>
        <Link className="trips-dashboard__primary" href="/chat">
          Plan in Chat
        </Link>
      </header>

      <div className="trips-dashboard__summary" aria-label="Trip library summary">
        <article>
          <span>{visibleTrips.length}</span>
          <p>Trips</p>
        </article>
        <article>
          <span>{totalDays}</span>
          <p>Days planned</p>
        </article>
        <article>
          <span>{totalAlerts}</span>
          <p>Butler tasks</p>
        </article>
      </div>

      <div className="trip-filters" aria-label="Trip status filters">
        {filters.map((filter) => (
          <button
            aria-pressed={activeFilter === filter}
            data-active={activeFilter === filter ? "true" : "false"}
            key={filter}
            onClick={() => setActiveFilter(filter)}
            type="button"
          >
            {tripStatusLabels[filter]}
          </button>
        ))}
      </div>

      <div className="trip-library" aria-label="Saved trips">
        {visibleTrips.map((trip) => (
          <article className="trip-card" data-status={trip.status} key={trip.id}>
            <div className="trip-card__status">
              <span>{getStatusCopy(trip.status)}</span>
              <strong>{trip.updatedAt}</strong>
            </div>
            <div className="trip-card__main">
              <div>
                <h2>{trip.title}</h2>
                <p>{trip.summary}</p>
              </div>
              <div className="trip-card__links">
                <Link href={`/trips/${trip.id}`}>View details</Link>
                <Link href={isSignedIn ? `/chat?trip=${trip.id}` : "/chat"}>Continue in Chat</Link>
              </div>
            </div>
            <dl className="trip-card__meta" aria-label={`${trip.title} summary`}>
              <div>
                <dt>Route</dt>
                <dd>{trip.route}</dd>
              </div>
              <div>
                <dt>Dates</dt>
                <dd>{trip.dates}</dd>
              </div>
              <div>
                <dt>Length</dt>
                <dd>{trip.durationDays} days</dd>
              </div>
              <div>
                <dt>Travelers</dt>
                <dd>{trip.travelers}</dd>
              </div>
            </dl>
            <div className="trip-card__footer">
              <ul aria-label={`${trip.title} highlights`}>
                {trip.highlights.map((highlight) => (
                  <li key={highlight}>{highlight}</li>
                ))}
              </ul>
              <span>{trip.alertCount} butler tasks</span>
            </div>
          </article>
        ))}
        {isSignedIn && visibleTrips.length === 0 && (
          <p className="trips-dashboard__empty">No saved trips yet. Use Save to Trips from the Chat workspace.</p>
        )}
      </div>
    </section>
  );
}
