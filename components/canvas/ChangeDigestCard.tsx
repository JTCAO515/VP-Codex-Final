"use client";

import type { ChangeDigestEntry } from "@/lib/types/trip";

const KIND_ICON: Record<ChangeDigestEntry["kind"], string> = {
  added: "+",
  revised: "~",
  removed: "-",
  alert: "!",
};

interface ChangeDigestCardProps {
  entries: ChangeDigestEntry[];
  onSelectDay?: (dayNumber: number) => void;
  onUndo?: () => void;
  undoDisabled?: boolean;
}

export function ChangeDigestCard({ entries, onSelectDay, onUndo, undoDisabled }: ChangeDigestCardProps) {
  if (entries.length === 0) return null;

  return (
    <div className="change-digest-card" aria-label="This update's changes">
      <div className="change-digest-card__head">
        <span>This update</span>
        {onUndo ? (
          <button disabled={undoDisabled} onClick={onUndo} type="button">
            Undo
          </button>
        ) : null}
      </div>
      <ul>
        {entries.map((entry) => (
          <li key={`${entry.kind}-${entry.dayNumber ?? "alert"}-${entry.label}`}>
            {entry.dayNumber && onSelectDay ? (
              <button onClick={() => onSelectDay(entry.dayNumber as number)} type="button">
                <span aria-hidden="true">{KIND_ICON[entry.kind]}</span>
                {entry.label}
              </button>
            ) : (
              <span>
                <span aria-hidden="true">{KIND_ICON[entry.kind]}</span>
                {entry.label}
              </span>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}
