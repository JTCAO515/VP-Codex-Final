"use client";

import { ButlerRail } from "@/components/canvas/ButlerRail";
import { CanvasTaskStrip } from "@/components/canvas/CanvasTaskStrip";
import { DayDetailDrawer } from "@/components/canvas/DayDetailDrawer";
import { DayCard } from "@/components/canvas/DayCard";
import { TripSummary } from "@/components/canvas/TripSummary";
import { useMemo, useState } from "react";
import type { TripState } from "@/lib/types/trip";

export function TripCanvas({ trip }: { trip: TripState }) {
  const [selectedDayNumber, setSelectedDayNumber] = useState(trip.days[0]?.day ?? 1);
  const selectedDay = useMemo(
    () => trip.days.find((day) => day.day === selectedDayNumber) ?? trip.days[0],
    [selectedDayNumber, trip.days],
  );

  return (
    <section className="trip-canvas" aria-label="Live trip canvas">
      <div className="trip-canvas__title">
        <h1>Live Trip Canvas</h1>
        <span aria-hidden="true">VP</span>
      </div>
      <CanvasTaskStrip />
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
        <div className="trip-canvas__side">
          {selectedDay ? <DayDetailDrawer day={selectedDay} /> : null}
          <ButlerRail alerts={trip.alerts} />
        </div>
      </div>
    </section>
  );
}
