"use client";

import { useEffect, useState } from "react";
import { TripCanvas } from "@/components/canvas/TripCanvas";
import { loadSharedTrip } from "@/lib/supabase/tripsRepository";
import type { TripRow } from "@/lib/supabase/schema";
import type { TripState } from "@/lib/types/trip";

type LoadState = "loading" | "found" | "not-found";

export function ShareView({ shareToken }: { shareToken: string }) {
  const [trip, setTrip] = useState<TripRow | null>(null);
  const [canvas, setCanvas] = useState<TripState | null>(null);
  const [state, setState] = useState<LoadState>("loading");

  useEffect(() => {
    let active = true;
    loadSharedTrip(shareToken)
      .then((remote) => {
        if (!active) return;
        if (!remote) {
          setState("not-found");
          return;
        }
        setTrip(remote.trip);
        setCanvas(remote.canvas);
        setState("found");
      })
      .catch(() => {
        if (active) setState("not-found");
      });

    return () => {
      active = false;
    };
  }, [shareToken]);

  if (state === "loading") {
    return (
      <section className="share-view" aria-labelledby="share-view-title">
        <p className="section-kicker">Shared Trip</p>
        <h1 id="share-view-title">Loading trip...</h1>
      </section>
    );
  }

  if (state === "found" && trip) {
    return (
      <section className="share-view" aria-labelledby="share-view-title">
        <p className="section-kicker">Shared Trip</p>
        <h1 id="share-view-title">{trip.title}</h1>
        <p className="share-view__notice">This is a read-only view of a shared VisePanda trip canvas.</p>
        {canvas ? (
          <div className="share-view__canvas">
            <TripCanvas trip={canvas} />
          </div>
        ) : (
          <p className="trip-detail__notice">This trip does not have a saved canvas yet.</p>
        )}
      </section>
    );
  }

  return (
    <section className="share-view" aria-labelledby="share-view-title">
      <p className="section-kicker">Shared Trip</p>
      <h1 id="share-view-title">Link not available</h1>
      <p className="share-view__notice">
        This share link is invalid or has been revoked by its owner.
      </p>
    </section>
  );
}
