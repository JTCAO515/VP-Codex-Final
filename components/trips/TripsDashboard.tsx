"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import {
  savedTrips,
  tripStatusDescriptions,
  tripStatusLabels,
  tripStatusNextActions,
  type SavedTrip,
  type SavedTripStatus,
} from "@/lib/trips/mockTrips";
import { listTripsForOwner } from "@/lib/supabase/tripsRepository";
import { useSupabaseSession } from "@/lib/supabase/useSupabaseSession";
import type { TripRow } from "@/lib/supabase/schema";
import { useTranslation } from "@/lib/i18n/I18nContext";

type TripFilter = SavedTripStatus | "all";

const filters: TripFilter[] = ["all", "draft", "ready", "shared", "archived"];

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
  const { t } = useTranslation();

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

  function getStatusCopy(status: SavedTripStatus): string {
    if (status === "ready") return t.trips.statusReady;
    if (status === "shared") return t.trips.statusShared;
    if (status === "archived") return t.trips.statusArchived;
    return t.trips.statusDraft;
  }

  const filterLabels: Record<TripFilter, string> = {
    all: t.trips.filterAll,
    draft: t.trips.filterDraft,
    ready: t.trips.filterReady,
    shared: t.trips.filterShared,
    archived: t.trips.filterArchived,
  };

  return (
    <section className="trips-dashboard" aria-labelledby="trips-title">
      <header className="trips-dashboard__header">
        <div>
          <p className="section-kicker">{t.trips.kicker}</p>
          <h1 id="trips-title">{t.trips.heading}</h1>
          <p>{t.trips.subtitle}</p>
        </div>
        <Link className="trips-dashboard__primary" href="/chat">
          {t.trips.planInChat}
        </Link>
      </header>

      <div className="trips-dashboard__summary" aria-label="Trip library summary">
        <article>
          <span>{visibleTrips.length}</span>
          <p>{t.trips.summaryTrips}</p>
        </article>
        <article>
          <span>{totalDays}</span>
          <p>{t.trips.summaryDays}</p>
        </article>
        <article>
          <span>{totalAlerts}</span>
          <p>{t.trips.summaryTasks}</p>
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
            {filterLabels[filter]}
          </button>
        ))}
      </div>

      <div className="trip-library" aria-label="Saved trips">
        <div className="trip-status-guide" aria-label="Trip status guide">
          {(["draft", "ready", "shared", "archived"] as SavedTripStatus[]).map((status) => (
            <article data-status={status} key={status}>
              <strong>{status === "ready" ? t.trips.guideReady : tripStatusLabels[status]}</strong>
              <p>{tripStatusDescriptions[status]}</p>
              <span>{tripStatusNextActions[status]}</span>
            </article>
          ))}
        </div>
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
                <Link href={`/trips/${trip.id}`}>{t.trips.cardViewDetails}</Link>
                <Link href={isSignedIn ? `/chat?trip=${trip.id}` : "/chat"}>
                  {t.trips.cardContinueChat}
                </Link>
              </div>
            </div>
            <dl className="trip-card__meta" aria-label={`${trip.title} summary`}>
              <div>
                <dt>{t.trips.cardMetaRoute}</dt>
                <dd>{trip.route}</dd>
              </div>
              <div>
                <dt>{t.trips.cardMetaDates}</dt>
                <dd>{trip.dates}</dd>
              </div>
              <div>
                <dt>{t.trips.cardMetaLength}</dt>
                <dd>{trip.durationDays} {t.trips.cardDays}</dd>
              </div>
              <div>
                <dt>{t.trips.cardMetaTravelers}</dt>
                <dd>{trip.travelers}</dd>
              </div>
            </dl>
            <div className="trip-card__footer">
              <ul aria-label={`${trip.title} highlights`}>
                {trip.highlights.map((highlight) => (
                  <li key={highlight}>{highlight}</li>
                ))}
              </ul>
              <span>{trip.alertCount} {t.trips.cardTasks}</span>
            </div>
          </article>
        ))}
        {isSignedIn && visibleTrips.length === 0 && (
          <p className="trips-dashboard__empty">{t.trips.empty}</p>
        )}
      </div>
    </section>
  );
}
