"use client";

import type { ButlerAlert } from "@/lib/types/trip";

interface PrepChecklistProps {
  alerts: ButlerAlert[];
  onToggle: (alert: ButlerAlert) => void;
}

/** "Before you fly" — aggregates the five-anxiety alerts into one checkable prep list on the canvas. */
export function PrepChecklist({ alerts, onToggle }: PrepChecklistProps) {
  if (alerts.length === 0) return null;

  return (
    <section className="prep-checklist" aria-label="Before you fly">
      <h3>Before you fly</h3>
      <ul>
        {alerts.map((alert) => (
          <li data-done={alert.done ? "true" : "false"} key={`${alert.type}:${alert.title}`}>
            <label>
              <input checked={Boolean(alert.done)} onChange={() => onToggle(alert)} type="checkbox" />
              <span>
                <strong>{alert.title}</strong>
                <span>{alert.body}</span>
              </span>
            </label>
          </li>
        ))}
      </ul>
    </section>
  );
}
