"use client";

import { Calendar, ChevronRight, MapPin, Map as MapIcon, Pencil, Plus, Settings, Shuffle } from "lucide-react";
import { type ReactNode, useState } from "react";
import { calculateTripCompleteness } from "@/lib/trips/completeness";
import type { TripState } from "@/lib/types/trip";

const confidenceLabels: Record<string, string> = {
  Draft: "Taking shape",
  Refined: "Looking good",
  Ready: "Travel-ready",
  "Ready to save": "Travel-ready",
};

const confidenceTone: Record<string, string> = {
  Draft: "gold",
  Refined: "sage",
  Ready: "sage",
  "Ready to save": "sage",
};

interface TripSummaryProps {
  trip: TripState;
  actions?: ReactNode;
  onRenameTrip?: (nextTitle: string) => void;
  onAddDay?: () => void;
  onRebalanceRoute?: () => void;
}

export function TripSummary({ trip, actions, onRenameTrip, onAddDay, onRebalanceRoute }: TripSummaryProps) {
  const route = trip.summary.destinations.join(" -> ");
  const confidenceLabel = confidenceLabels[trip.summary.confidence] ?? trip.summary.confidence;
  const tone = confidenceTone[trip.summary.confidence] ?? "gold";
  const readiness = calculateTripCompleteness(trip);
  const firstGap = readiness.checks.find((check) => !check.complete);

  const [isEditingTitle, setIsEditingTitle] = useState(false);
  const [titleDraft, setTitleDraft] = useState(trip.summary.title);

  function startEditing() {
    setTitleDraft(trip.summary.title);
    setIsEditingTitle(true);
  }

  function commitEditing() {
    const trimmed = titleDraft.trim();
    if (trimmed && trimmed !== trip.summary.title) onRenameTrip?.(trimmed);
    setIsEditingTitle(false);
  }

  return (
    <header className="trip-summary">
      <div className="trip-summary__route-mark" aria-hidden="true">
        <span />
      </div>
      <div className="trip-summary__copy">
        <div className="trip-summary__title-row">
          {isEditingTitle ? (
            <input
              autoFocus
              className="trip-summary__title-input"
              onBlur={commitEditing}
              onChange={(event) => setTitleDraft(event.target.value)}
              onKeyDown={(event) => {
                if (event.key === "Enter") commitEditing();
                if (event.key === "Escape") setIsEditingTitle(false);
              }}
              value={titleDraft}
            />
          ) : (
            <p>{trip.summary.title}</p>
          )}
          {onRenameTrip && !isEditingTitle ? (
            <button aria-label="Rename trip" className="trip-summary__edit" onClick={startEditing} type="button">
              <Pencil size={13} strokeWidth={1.8} />
            </button>
          ) : null}
        </div>
        <h2>{route}</h2>
        <span>{trip.lastUpdatedReason}</span>
      </div>
      <div className="trip-summary__meta" aria-label="Trip summary">
        <span>{trip.summary.durationDays} days</span>
        <span>{trip.summary.pace} pace</span>
        <span>{trip.summary.travelerStyle}</span>
        <span>{confidenceLabel}</span>
      </div>
      <div className="trip-summary__status" data-tone={tone}>
        <div className="trip-summary__status-head">
          <span className="trip-summary__status-dot" aria-hidden="true" />
          <strong>{confidenceLabel}</strong>
        </div>
        <div className="trip-summary__status-bar">
          <span style={{ width: `${readiness.score}%` }} />
        </div>
        <span className="trip-summary__status-caption">{readiness.score}% complete</span>
        {firstGap ? (
          <button className="trip-summary__next-cell" type="button">
            <span>
              Next step<strong>Review {firstGap.label.toLowerCase()}</strong>
            </span>
            <ChevronRight aria-hidden="true" size={16} strokeWidth={1.8} />
          </button>
        ) : null}
      </div>
      <div className="trip-summary__chips" aria-label="Trip at a glance">
        <span>
          <MapPin aria-hidden="true" size={13} strokeWidth={1.8} />
          {route || "No destination yet"}
        </span>
        <span>
          <Calendar aria-hidden="true" size={13} strokeWidth={1.8} />
          {trip.summary.durationDays} days
        </span>
        <span className="trip-summary__chip--accent">{trip.summary.travelerStyle}</span>
      </div>
      <div className="trip-summary__action-row">
        <div className="trip-summary__action-cluster">
          <button disabled={!onAddDay} onClick={onAddDay} type="button">
            <Plus aria-hidden="true" size={14} strokeWidth={1.8} />
            Add day
          </button>
          <button disabled title="Coming soon" type="button">
            <MapIcon aria-hidden="true" size={14} strokeWidth={1.8} />
            View map
          </button>
        </div>
        <div className="trip-summary__action-cluster">
          <button disabled={!onRebalanceRoute} onClick={onRebalanceRoute} type="button">
            <Shuffle aria-hidden="true" size={14} strokeWidth={1.8} />
            Rebalance route
          </button>
          <button disabled title="Coming soon" type="button">
            <Settings aria-hidden="true" size={14} strokeWidth={1.8} />
            Trip settings
          </button>
        </div>
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
