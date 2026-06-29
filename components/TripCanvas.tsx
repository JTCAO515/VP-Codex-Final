"use client";

import { useTripStore } from "@/lib/store";
import { DayCard } from "./DayCard";
import { TripSummaryCard } from "./TripSummaryCard";

export function TripCanvas({ onEditSummary }: { onEditSummary: () => void }) {
  const days = useTripStore((state) => state.days);

  if (days.length === 0) {
    return (
      <div className="flex h-full items-center justify-center px-6 text-center text-ink-umber/70">
        <p className="text-sm">
          Tell me where you&apos;re headed and how many days you have — your itinerary will appear here.
        </p>
      </div>
    );
  }

  return (
    <div className="h-full overflow-y-auto px-4 py-4">
      <p className="mb-3 font-display text-2xl text-ink-umber">Live Trip Canvas</p>
      <TripSummaryCard onEdit={onEditSummary} />
      <div className="border-l-2 border-ink-cinnabar/25">
        {days.map((day) => (
          <DayCard key={day.day} day={day} />
        ))}
      </div>
    </div>
  );
}
