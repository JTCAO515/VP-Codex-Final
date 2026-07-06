import type { AlertType, ButlerAlert } from "@/lib/types/trip";

const alertActionHrefMap: Partial<Record<AlertType, string>> = {
  visa: "/tools?category=visa-and-entry",
  payment: "/tools?category=payment-setup",
  language: "/translate",
  transport: "/tools?category=metro",
  risk: "/tools?category=emergency",
  emergency: "/tools?category=emergency",
};

export function ButlerReminders({ alerts }: { alerts: ButlerAlert[] }) {
  if (alerts.length === 0) return null;

  return (
    <section className="butler-reminders" aria-label="Copilot reminders">
      <h3>Copilot reminders</h3>
      <ul>
        {alerts.map((alert, index) => {
          const actionHref = alertActionHrefMap[alert.type];

          return (
            <li data-priority={alert.priority} key={`${alert.type}-${index}`}>
              <span>{alert.title}</span>
              {actionHref ? (
                <a href={actionHref}>{alert.action}</a>
              ) : (
                <span className="butler-reminders__action">{alert.action}</span>
              )}
            </li>
          );
        })}
      </ul>
    </section>
  );
}
