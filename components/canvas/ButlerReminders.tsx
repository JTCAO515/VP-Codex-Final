import type { AlertType, ButlerAlert } from "@/lib/types/trip";

const alertToolCategoryMap: Partial<Record<AlertType, string>> = {
  visa: "visa-and-entry",
  payment: "payment-setup",
  language: "translate",
  transport: "metro",
  risk: "emergency",
  emergency: "emergency",
};

export function ButlerReminders({ alerts }: { alerts: ButlerAlert[] }) {
  if (alerts.length === 0) return null;

  return (
    <section className="butler-reminders" aria-label="Butler reminders">
      <h3>Butler reminders</h3>
      <ul>
        {alerts.map((alert, index) => {
          const categoryId = alertToolCategoryMap[alert.type];

          return (
            <li data-priority={alert.priority} key={`${alert.type}-${index}`}>
              <span>{alert.title}</span>
              {categoryId ? (
                <a href={`/tools?category=${categoryId}`}>{alert.action}</a>
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
