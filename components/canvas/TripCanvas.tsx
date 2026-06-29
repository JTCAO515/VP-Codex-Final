"use client";

import { DayDetailDrawer } from "@/components/canvas/DayDetailDrawer";
import { DayCard } from "@/components/canvas/DayCard";
import { TripSummary } from "@/components/canvas/TripSummary";
import { useEffect, useMemo, useState } from "react";
import type { TripDay, TripState } from "@/lib/types/trip";

export function TripCanvas({ trip }: { trip: TripState }) {
  const [editableTrip, setEditableTrip] = useState(trip);
  const [selectedDayNumber, setSelectedDayNumber] = useState<number | null>(null);

  useEffect(() => {
    setEditableTrip(trip);
  }, [trip]);

  const selectedDay = useMemo(
    () => editableTrip.days.find((day) => day.day === selectedDayNumber),
    [selectedDayNumber, editableTrip.days],
  );

  function handleSaveDay(updatedDay: TripDay) {
    setEditableTrip((currentTrip) => ({
      ...currentTrip,
      days: currentTrip.days.map((day) => (day.day === updatedDay.day ? updatedDay : day)),
      lastUpdatedReason: `Day ${updatedDay.day} was edited in the itinerary drawer.`,
    }));
    setSelectedDayNumber(null);
  }

  return (
    <section className="trip-canvas" aria-label="Live trip canvas">
      <div className="trip-canvas__title">
        <h1>Live Trip Canvas</h1>
        <span aria-hidden="true">VP</span>
      </div>
      <TripSummary trip={editableTrip} />
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
          <DayDetailDrawer day={selectedDay} onClose={() => setSelectedDayNumber(null)} onSave={handleSaveDay} />
        </div>
      ) : null}
    </section>
  );
}
