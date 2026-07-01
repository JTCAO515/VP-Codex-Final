"use client";

import { DayDetailDrawer } from "@/components/canvas/DayDetailDrawer";
import { DayCard } from "@/components/canvas/DayCard";
import { TripSummary } from "@/components/canvas/TripSummary";
import { getDestinationScene } from "@/lib/visual/destinationBackground";
import type { ReactNode } from "react";
import { useEffect, useMemo, useState } from "react";
import type { TripState } from "@/lib/types/trip";

export function TripCanvas({ trip, summaryActions }: { trip: TripState; summaryActions?: ReactNode }) {
  const [editableTrip, setEditableTrip] = useState(trip);
  const [selectedDayNumber, setSelectedDayNumber] = useState<number | null>(null);

  useEffect(() => {
    setEditableTrip(trip);
  }, [trip]);

  useEffect(() => {
    if (typeof document === "undefined") return;
    const scene = getDestinationScene(editableTrip.summary.destinations);
    document.body.dataset.destinationScene = scene.cssValue;
    document.body.dataset.destinationSceneLabel = scene.label;

    return () => {
      document.body.dataset.destinationScene = "default-ink";
      document.body.dataset.destinationSceneLabel = "China ink landscape";
    };
  }, [editableTrip.summary.destinations]);

  const selectedDay = useMemo(
    () => editableTrip.days.find((day) => day.day === selectedDayNumber),
    [selectedDayNumber, editableTrip.days],
  );

  return (
    <section className="trip-canvas" aria-label="Live trip canvas">
      <div className="trip-canvas__title">
        <h1>Live Trip Canvas</h1>
        <span aria-hidden="true">VP</span>
      </div>
      <TripSummary trip={editableTrip} actions={summaryActions} />
      <div className="trip-canvas__body">
        <div className="trip-canvas__days">
          {editableTrip.days.map((day) => (
            <DayCard
              day={day}
              isSelected={selectedDay?.day === day.day}
              key={`${day.day}-${day.city}`}
              onSelect={() => setSelectedDayNumber(day.day)}
            />
          ))}
        </div>
      </div>
      {selectedDay ? (
        <div className="day-drawer-shell" role="presentation">
          <DayDetailDrawer day={selectedDay} onClose={() => setSelectedDayNumber(null)} />
        </div>
      ) : null}
    </section>
  );
}
