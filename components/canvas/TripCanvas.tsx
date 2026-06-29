import { ButlerRail } from "@/components/canvas/ButlerRail";
import { DayCard } from "@/components/canvas/DayCard";
import { TripSummary } from "@/components/canvas/TripSummary";
import type { TripState } from "@/lib/types/trip";

export function TripCanvas({ trip }: { trip: TripState }) {
  return (
    <section className="trip-canvas" aria-label="Live trip canvas">
      <TripSummary trip={trip} />
      <div className="trip-canvas__body">
        <div className="trip-canvas__days">
          {trip.days.map((day) => (
            <DayCard day={day} key={`${day.day}-${day.city}`} />
          ))}
        </div>
        <ButlerRail alerts={trip.alerts} />
      </div>
    </section>
  );
}
