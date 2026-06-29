import type { AlertType, ButlerAlert } from "@/lib/types/trip";

const baseTasks = [
  {
    icon: "V",
    type: "visa",
    title: "Visa",
    body: "Check entry rules and apply early.",
    status: "Not started",
    tone: "red",
  },
  {
    icon: "P",
    type: "payment",
    title: "Payment",
    body: "Set up mobile payments for a smooth trip.",
    status: "In progress",
    tone: "gold",
  },
  {
    icon: "B",
    type: "booking",
    title: "Booking",
    body: "Attractions and transport book ahead.",
    status: "2 to confirm",
    tone: "gold",
  },
  {
    icon: "L",
    type: "transport",
    title: "Less tiring",
    body: "Balanced pace with smart routing.",
    status: "Optimizing",
    tone: "sage",
  },
  {
    icon: "F",
    type: "food",
    title: "Food-focused",
    body: "Local flavors and must-try picks.",
    status: "Curated",
    tone: "red",
  },
];

const alertTaskMap: Partial<Record<AlertType, (typeof baseTasks)[number]["type"]>> = {
  emergency: "booking",
  language: "transport",
  payment: "payment",
  risk: "booking",
  transport: "transport",
  visa: "visa",
  weather: "transport",
};

function getTaskAlert(taskType: string, alerts: ButlerAlert[]) {
  return alerts.find((alert) => alertTaskMap[alert.type] === taskType);
}

export function CanvasTaskStrip({ alerts = [] }: { alerts?: ButlerAlert[] }) {
  return (
    <div className="canvas-task-strip" aria-label="Butler planning tasks">
      {baseTasks.map((task) => {
        const alert = getTaskAlert(task.type, alerts);
        const body = alert ? alert.title : task.body;
        const status = alert ? alert.action : task.status;

        return (
          <article className="canvas-task" data-priority={alert?.priority} data-tone={task.tone} key={task.title}>
            <span className="canvas-task__icon" aria-hidden="true">
              {task.icon}
            </span>
            <div>
              <h3>{task.title}</h3>
              <p>{body}</p>
            </div>
            <strong>{status}</strong>
          </article>
        );
      })}
    </div>
  );
}
