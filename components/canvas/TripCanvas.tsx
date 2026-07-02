"use client";

import { DayDetailDrawer } from "@/components/canvas/DayDetailDrawer";
import { DayCard } from "@/components/canvas/DayCard";
import { PrepChecklist } from "@/components/canvas/PrepChecklist";
import { TripSummary } from "@/components/canvas/TripSummary";
import type { QuickActionKind } from "@/lib/canvas/quickActions";
import { getDestinationScene } from "@/lib/visual/destinationBackground";
import type { ReactNode } from "react";
import { useEffect, useMemo, useRef, useState } from "react";
import type { ButlerAlert, TripBlock, TripDay, TripState } from "@/lib/types/trip";

export interface HighlightSignal {
  dayNumber: number;
  nonce: number;
}

export function TripCanvas({
  trip,
  summaryActions,
  onQuickAction,
  onToggleAlertDone,
  onRenameTrip,
  onAddDay,
  onRebalanceRoute,
  onScheduleCandidate,
  highlightSignal,
  busy,
}: {
  trip: TripState;
  summaryActions?: ReactNode;
  onQuickAction?: (message: string, kind: QuickActionKind) => void;
  onToggleAlertDone?: (alert: ButlerAlert) => void;
  onRenameTrip?: (nextTitle: string) => void;
  onAddDay?: () => void;
  onRebalanceRoute?: () => void;
  onScheduleCandidate?: (day: TripDay, block: TripBlock) => void;
  highlightSignal?: HighlightSignal | null;
  busy?: boolean;
}) {
  // Renders `trip` directly (no local buffered copy). The day drawer has been
  // read-only since v0.1.43, so there is no local-edit state that needs to
  // diverge from the prop — an earlier `editableTrip` copy synced via a
  // useEffect was vestigial from the pre-v0.1.43 editable-drawer era and
  // introduced an extra render pass, which could let the Change Digest card
  // (driven directly by ButlerWorkspace's `messages` state) appear one commit
  // ahead of the day cards actually reflecting the new trip. Rendering `trip`
  // directly closes that gap.
  const [selectedDayNumber, setSelectedDayNumber] = useState<number | null>(null);
  const dayRefs = useRef(new Map<number, HTMLElement | null>());
  const isFirstTripRender = useRef(true);

  useEffect(() => {
    if (typeof document === "undefined") return;
    const scene = getDestinationScene(trip.summary.destinations);
    document.body.dataset.destinationScene = scene.cssValue;
    document.body.dataset.destinationSceneLabel = scene.label;

    return () => {
      document.body.dataset.destinationScene = "default-ink";
      document.body.dataset.destinationSceneLabel = "China ink landscape";
    };
  }, [trip.summary.destinations]);

  // Patch演出: after a canvas update lands, auto-scroll to the first new or
  // revised day so the change is immediately visible (not just described in
  // the chat log). Skipped on the very first render (nothing "changed" yet).
  useEffect(() => {
    if (isFirstTripRender.current) {
      isFirstTripRender.current = false;
      return;
    }
    const firstChanged = trip.days.find((day) => day.status === "new" || day.status === "revised");
    if (!firstChanged) return;
    const element = dayRefs.current.get(firstChanged.day);
    if (typeof element?.scrollIntoView === "function") {
      element.scrollIntoView({ behavior: "smooth", block: "nearest" });
    }
  }, [trip]);

  // Change-Digest click: scroll to and re-pulse a specific day, even if the
  // same day is clicked twice in a row (nonce always changes).
  useEffect(() => {
    if (!highlightSignal) return;
    const element = dayRefs.current.get(highlightSignal.dayNumber);
    if (typeof element?.scrollIntoView === "function") {
      element.scrollIntoView({ behavior: "smooth", block: "nearest" });
    }
  }, [highlightSignal]);

  const selectedDay = useMemo(
    () => trip.days.find((day) => day.day === selectedDayNumber),
    [selectedDayNumber, trip.days],
  );
  const canvasTitle = trip.summary.title.trim() || "Live Trip Canvas";

  return (
    <section className="trip-canvas" aria-label="Live trip canvas">
      <div className="trip-canvas__title">
        <h1>{canvasTitle}</h1>
        <span aria-hidden="true">VP</span>
      </div>
      <TripSummary
        actions={summaryActions}
        onAddDay={onAddDay}
        onRebalanceRoute={onRebalanceRoute}
        onRenameTrip={onRenameTrip}
        trip={trip}
      />
      <div className="trip-canvas__body">
        <div className="trip-canvas__days">
          {trip.days.map((day) => (
            <DayCard
              busy={busy}
              day={day}
              highlightNonce={highlightSignal?.dayNumber === day.day ? highlightSignal.nonce : undefined}
              isSelected={selectedDay?.day === day.day}
              key={`${day.day}-${day.city}`}
              onQuickAction={onQuickAction}
              onSelect={() => setSelectedDayNumber(day.day)}
              registerRef={(dayNumber, element) => dayRefs.current.set(dayNumber, element)}
            />
          ))}
        </div>
      </div>
      {onToggleAlertDone ? <PrepChecklist alerts={trip.alerts} onToggle={onToggleAlertDone} /> : null}
      {selectedDay ? (
        <div className="day-drawer-shell" role="presentation">
          <DayDetailDrawer
            busy={busy}
            day={selectedDay}
            onClose={() => setSelectedDayNumber(null)}
            onScheduleCandidate={onScheduleCandidate}
          />
        </div>
      ) : null}
    </section>
  );
}
