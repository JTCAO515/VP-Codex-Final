const tasks = [
  {
    icon: "V",
    title: "Visa",
    body: "Check entry rules and apply early.",
    status: "Not started",
    tone: "red",
  },
  {
    icon: "P",
    title: "Payment",
    body: "Set up mobile payments for a smooth trip.",
    status: "In progress",
    tone: "gold",
  },
  {
    icon: "B",
    title: "Booking",
    body: "Attractions and transport book ahead.",
    status: "2 to confirm",
    tone: "gold",
  },
  {
    icon: "L",
    title: "Less tiring",
    body: "Balanced pace with smart routing.",
    status: "Optimizing",
    tone: "sage",
  },
  {
    icon: "F",
    title: "Food-focused",
    body: "Local flavors and must-try picks.",
    status: "Curated",
    tone: "red",
  },
];

export function CanvasTaskStrip() {
  return (
    <div className="canvas-task-strip" aria-label="Butler planning tasks">
      {tasks.map((task) => (
        <article className="canvas-task" data-tone={task.tone} key={task.title}>
          <span className="canvas-task__icon" aria-hidden="true">
            {task.icon}
          </span>
          <div>
            <h3>{task.title}</h3>
            <p>{task.body}</p>
          </div>
          <strong>{task.status}</strong>
        </article>
      ))}
    </div>
  );
}
