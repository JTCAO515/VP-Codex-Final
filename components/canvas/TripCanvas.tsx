"use client";

import { CanvasTaskStrip } from "@/components/canvas/CanvasTaskStrip";
import { DayDetailDrawer } from "@/components/canvas/DayDetailDrawer";
import { DayCard } from "@/components/canvas/DayCard";
import { TripSummary } from "@/components/canvas/TripSummary";
import { useMemo, useState } from "react";
import type { TripState } from "@/lib/types/trip";

export function TripCanvas({ trip }: { trip: TripState }) {
  const [selectedDayNumber, setSelectedDayNumber] = useState<number | null>(null);
  const selectedDay = useMemo(
    () => trip.days.find((day) => day.day === selectedDayNumber),
    [selectedDayNumber, trip.days],
  );

  return (
    <section className="trip-canvas" aria-label="Live trip canvas">
      <div className="trip-canvas__title">
        <h1>Live Trip Canvas</h1>
        <span aria-hidden="true">VP</span>
      </div>
      <CanvasTaskStrip alerts={trip.alerts} />
      <TripSummary trip={trip} />
      <div className="trip-canvas__body">
        <div className="trip-canvas__days">
          {trip.days.map((day) => (
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
