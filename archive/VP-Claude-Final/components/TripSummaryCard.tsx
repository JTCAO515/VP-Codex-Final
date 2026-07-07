"use client";

import { useTripStore } from "@/lib/store";

export function TripSummaryCard({ onEdit }: { onEdit: () => void }) {
  const summary = useTripStore((state) => state.summary);

  if (summary.route.length === 0) {
    return null;
  }

  const dateRange =
    summary.startDate && summary.endDate ? `${summary.startDate} – ${summary.endDate}` : "Dates not set";

  return (
    <div className="mb-4 flex gap-3 rounded-lg border border-ink-umber/15 bg-ink-paper p-3 shadow-sm">
      <div className="flex h-16 w-16 flex-shrink-0 items-center justify-center rounded bg-ink-cream text-xs text-ink-umber/50">
        Map
      </div>
      <div className="flex-1">
        <div className="flex items-center justify-between">
          <p className="font-display text-lg text-ink-umber">{summary.route.join(" → ")}</p>
          <button onClick={onEdit} className="text-xs text-ink-cinnabar underline">
            Edit
          </button>
        </div>
        <p className="text-xs text-ink-umber/70">
          {dateRange} · {summary.travelers} traveler{summary.travelers === 1 ? "" : "s"} · {summary.days} days
        </p>
      </div>
    </div>
  );
}
