import type { AlertPriority, ButlerAlert } from "@/lib/types/trip";

const priorityRank: Record<AlertPriority, number> = {
  high: 0,
  medium: 1,
  low: 2,
};

export function ButlerRail({ alerts }: { alerts: ButlerAlert[] }) {
  const sortedAlerts = [...alerts].sort((a, b) => priorityRank[a.priority] - priorityRank[b.priority]);

  return (
    <aside className="butler-rail" aria-label="Butler reminders">
      <div className="butler-rail__head">
        <p className="section-kicker">Butler Rails</p>
        <h2>Practical reminders</h2>
      </div>
      <div className="butler-rail__list">
        {sortedAlerts.map((alert) => (
          <article className="butler-alert" data-priority={alert.priority} key={`${alert.type}-${alert.title}`}>
            <span>{alert.priority}</span>
            <h3>{alert.title}</h3>
            <p>{alert.body}</p>
            <strong>{alert.action}</strong>
          </article>
        ))}
      </div>
    </aside>
  );
}
