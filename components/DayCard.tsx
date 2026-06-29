import type { DayCard as DayCardType, DayPeriod } from "@/lib/types";

const PERIOD_LABEL: Record<DayPeriod, string> = {
  morning: "Morning",
  afternoon: "Afternoon",
  evening: "Evening",
};

export function DayCard({ day }: { day: DayCardType }) {
  return (
    <div className="relative mb-6 ml-10">
      <div className="absolute -left-10 top-0 flex h-10 w-10 items-center justify-center rounded-full bg-ink-cinnabar text-xs font-semibold text-ink-paper">
        Day {day.day}
      </div>
      <div className="rounded-lg border border-ink-umber/15 bg-ink-paper p-4 shadow-sm">
        <p className="font-display text-xl text-ink-umber">{day.city}</p>
        <div className="mt-2 grid grid-cols-1 gap-2 sm:grid-cols-3">
          {day.activities.map((activity) => (
            <div key={activity.period} className="rounded border border-ink-umber/10 bg-ink-cream p-2">
              <div className="flex h-12 items-center justify-center rounded bg-ink-ochre/20 text-center text-[10px] text-ink-umber/60">
                {activity.imageHint}
              </div>
              <p className="mt-1 text-[11px] font-medium uppercase text-ink-umber/50">
                {PERIOD_LABEL[activity.period]}
              </p>
              <p className="text-sm text-ink-umber">{activity.title}</p>
            </div>
          ))}
        </div>
        <div className="mt-3 flex flex-wrap items-center gap-3 text-xs text-ink-umber/70">
          <span>Hotel: {day.hotel}</span>
          <span>Transport: {day.transport}</span>
          <span>Pace: {day.pace}</span>
          <span>{day.budgetNote}</span>
          <span title="Coming soon" className="cursor-not-allowed underline">
            Map
          </span>
          <span title="Coming soon" className="cursor-not-allowed underline">
            Notes
          </span>
        </div>
      </div>
    </div>
  );
}
