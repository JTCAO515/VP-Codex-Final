"use client";

import { useTripStore } from "@/lib/store";
import type { RailItem } from "@/lib/types";

const SEVERITY_TAG: Record<RailItem["severity"], string> = {
  info: "text-ink-umber/60",
  warning: "text-ink-gold",
  urgent: "text-ink-cinnabar",
};

const SEVERITY_LABEL: Record<RailItem["severity"], string> = {
  info: "Noted",
  warning: "In progress",
  urgent: "Action needed",
};

export function ButlerRails() {
  const rails = useTripStore((state) => state.rails);

  if (rails.length === 0) {
    return null;
  }

  return (
    <div className="flex gap-3 overflow-x-auto border-b border-ink-umber/15 px-4 py-3">
      {rails.map((rail) => (
        <div
          key={rail.id}
          className="min-w-[200px] flex-shrink-0 rounded-lg border border-ink-umber/15 bg-ink-paper px-3 py-2 shadow-sm"
        >
          <p className="text-sm font-semibold text-ink-umber">{rail.title}</p>
          <p className="mt-1 text-xs text-ink-umber/80">{rail.detail}</p>
          <p className={`mt-2 text-xs font-medium ${SEVERITY_TAG[rail.severity]}`}>
            {SEVERITY_LABEL[rail.severity]}
          </p>
        </div>
      ))}
    </div>
  );
}
