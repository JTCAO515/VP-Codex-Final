"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { TripCanvas } from "@/components/canvas/TripCanvas";
import { savedTrips, tripStatusDescriptions, tripStatusLabels, tripStatusNextActions } from "@/lib/trips/mockTrips";
import {
  createShareLink,
  loadTripWithCanvas,
  revokeShareLink,
  updateTripStatus,
} from "@/lib/supabase/tripsRepository";
import { useSupabaseSession } from "@/lib/supabase/useSupabaseSession";
import type { TripRow } from "@/lib/supabase/schema";
import type { TripState } from "@/lib/types/trip";

type LoadState = "loading" | "found" | "not-found" | "error";

export function TripDetail({ tripId }: { tripId: string }) {
  const { configured, loading, session } = useSupabaseSession();
  const [remoteTrip, setRemoteTrip] = useState<TripRow | null>(null);
  const [canvas, setCanvas] = useState<TripState | null>(null);
  const [state, setState] = useState<LoadState>("loading");
  const [shareUrl, setShareUrl] = useState<string | null>(null);
  const [actionBusy, setActionBusy] = useState(false);
  const [actionMessage, setActionMessage] = useState<string | null>(null);

  useEffect(() => {
    if (!configured || loading || !session) {
      setState(loading ? "loading" : "not-found");
      return;
    }

    let active = true;
    loadTripWithCanvas(tripId)
      .then((remote) => {
        if (!active) return;
        if (!remote) {
          setState("not-found");
          return;
        }
        setRemoteTrip(remote.trip);
        setCanvas(remote.canvas);
        if (remote.trip.share_token && typeof window !== "undefined") {
          setShareUrl(`${window.location.origin}/share/${remote.trip.share_token}`);
        }
        setState("found");
      })
      .catch(() => {
        if (active) setState("error");
      });

    return () => {
      active = false;
    };
  }, [configured, loading, session, tripId]);

  async function handleSetStatus(status: TripRow["status"]) {
    if (!remoteTrip) return;
    setActionBusy(true);
    try {
      await updateTripStatus(remoteTrip.id, status);
      setRemoteTrip({ ...remoteTrip, status });
      setActionMessage(status === "archived" ? "Trip archived." : `Trip marked as ${status}.`);
    } catch {
      setActionMessage("Could not update this trip. Please try again.");
    } finally {
      setActionBusy(false);
    }
  }

  async function handleCreateShareLink() {
    if (!remoteTrip) return;
    setActionBusy(true);
    try {
      const token = await createShareLink(remoteTrip.id);
      setRemoteTrip({ ...remoteTrip, status: "shared", share_token: token });
      setShareUrl(`${window.location.origin}/share/${token}`);
      setActionMessage("Share link created.");
    } catch {
      setActionMessage("Could not create a share link. Please try again.");
    } finally {
      setActionBusy(false);
    }
  }

  async function handleRevokeShareLink() {
    if (!remoteTrip) return;
    setActionBusy(true);
    try {
      await revokeShareLink(remoteTrip.id);
      setRemoteTrip({ ...remoteTrip, share_token: null });
      setShareUrl(null);
      setActionMessage("Share link revoked.");
    } catch {
      setActionMessage("Could not revoke the share link. Please try again.");
    } finally {
      setActionBusy(false);
    }
  }

  const mockTrip = savedTrips.find((trip) => trip.id === tripId);

  if (state === "loading") {
    return (
      <section className="trip-detail" aria-labelledby="trip-detail-title">
        <p className="section-kicker">Trips</p>
        <h1 id="trip-detail-title">Loading trip...</h1>
      </section>
    );
  }

  if (state === "found" && remoteTrip) {
    return (
      <section className="trip-detail" aria-labelledby="trip-detail-title">
        <div className="trip-detail__header">
          <div>
            <p className="section-kicker">Trips</p>
            <h1 id="trip-detail-title">{remoteTrip.title}</h1>
            <p className="trip-detail__status">
              {tripStatusLabels[remoteTrip.status]} - Updated {new Date(remoteTrip.updated_at).toLocaleDateString()}
            </p>
          </div>
          <div className="trip-detail__actions">
            <Link className="primary-link" href={`/chat?trip=${remoteTrip.id}`}>
              Continue in Chat
            </Link>
            <Link className="primary-link" href="/trips">
              Back to Trips
            </Link>
          </div>
        </div>
        <div className="trip-detail__status-actions">
          {remoteTrip.status !== "archived" && (
            <button disabled={actionBusy} onClick={() => handleSetStatus("ready")} type="button">
              Mark as Ready
            </button>
          )}
          {remoteTrip.status !== "archived" ? (
            <button disabled={actionBusy} onClick={() => handleSetStatus("archived")} type="button">
              Archive
            </button>
          ) : (
            <button disabled={actionBusy} onClick={() => handleSetStatus("draft")} type="button">
              Restore from archive
            </button>
          )}
          {remoteTrip.share_token ? (
            <button disabled={actionBusy} onClick={handleRevokeShareLink} type="button">
              Revoke share link
            </button>
          ) : (
            <button disabled={actionBusy} onClick={handleCreateShareLink} type="button">
              Get share link
            </button>
          )}
        </div>
        <aside className="trip-detail__status-guide" aria-label="Current trip status">
          <strong>{tripStatusLabels[remoteTrip.status]}</strong>
          <p>{tripStatusDescriptions[remoteTrip.status]}</p>
          <span>{tripStatusNextActions[remoteTrip.status]}</span>
        </aside>
        {shareUrl && (
          <p className="trip-detail__share-link" role="status">
            Share link: <code>{shareUrl}</code>
          </p>
        )}
        {actionMessage && (
          <p aria-live="polite" className="trip-detail__action-message" role="status">
            {actionMessage}
          </p>
        )}
        {canvas ? (
          <div className="trip-detail__canvas">
            <TripCanvas trip={canvas} />
          </div>
        ) : (
          <p className="trip-detail__notice">
            This trip does not have a saved canvas yet. Open it in Chat and use Save to Trips to create one.
          </p>
        )}
      </section>
    );
  }

  if (mockTrip) {
    return (
      <section className="trip-detail" aria-labelledby="trip-detail-title">
        <div className="trip-detail__header">
          <div>
            <p className="section-kicker">Trips</p>
            <h1 id="trip-detail-title">{mockTrip.title}</h1>
            <p className="trip-detail__status">{tripStatusLabels[mockTrip.status]} - {mockTrip.updatedAt}</p>
          </div>
          <div className="trip-detail__actions">
            <Link className="primary-link" href="/chat">
              Continue in Chat
            </Link>
            <Link className="primary-link" href="/trips">
              Back to Trips
            </Link>
          </div>
        </div>
        <p className="trip-detail__notice">
          This is an example trip. Sign in from Account and save a real trip from Chat to see its full live canvas
          here.
        </p>
        <aside className="trip-detail__status-guide" aria-label="Current trip status">
          <strong>{tripStatusLabels[mockTrip.status]}</strong>
          <p>{tripStatusDescriptions[mockTrip.status]}</p>
          <span>{tripStatusNextActions[mockTrip.status]}</span>
        </aside>
        <dl className="trip-card__meta" aria-label={`${mockTrip.title} summary`}>
          <div>
            <dt>Route</dt>
            <dd>{mockTrip.route}</dd>
          </div>
          <div>
            <dt>Dates</dt>
            <dd>{mockTrip.dates}</dd>
          </div>
          <div>
            <dt>Length</dt>
            <dd>{mockTrip.durationDays} days</dd>
          </div>
          <div>
            <dt>Travelers</dt>
            <dd>{mockTrip.travelers}</dd>
          </div>
        </dl>
        <ul className="trip-card__footer" aria-label={`${mockTrip.title} highlights`}>
          {mockTrip.highlights.map((highlight) => (
            <li key={highlight}>{highlight}</li>
          ))}
        </ul>
      </section>
    );
  }

  return (
    <section className="trip-detail" aria-labelledby="trip-detail-title">
      <p className="section-kicker">Trips</p>
      <h1 id="trip-detail-title">Trip not found</h1>
      <p className="trip-detail__notice">
        We could not find that trip. It may have been removed, or you may need to sign in to view it.
      </p>
      <Link className="primary-link" href="/trips">
        Back to Trips
      </Link>
    </section>
  );
}
