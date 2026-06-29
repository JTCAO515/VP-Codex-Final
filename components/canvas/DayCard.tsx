import type { TripDay } from "@/lib/types/trip";

export function DayCard({ day }: { day: TripDay }) {
  return (
    <article className="day-card" data-status={day.status ?? "stable"}>
      <div className="day-card__head">
        <p>Day {day.day}</p>
        <h2>{day.city}</h2>
        <span>{day.pace}</span>
      </div>
      <div className="day-card__blocks">
        {day.blocks.map((block) => (
          <section className="day-block" key={`${day.day}-${block.time}-${block.title}`}>
            <span>{block.time}</span>
            <strong>{block.title}</strong>
            <p>{block.description}</p>
          </section>
        ))}
      </div>
      <dl className="day-card__details">
        <div>
          <dt>Food</dt>
          <dd>{day.food.join(", ")}</dd>
        </div>
        <div>
          <dt>Stay</dt>
          <dd>{day.stay}</dd>
        </div>
        <div>
          <dt>Transport</dt>
          <dd>{day.transport}</dd>
        </div>
      </dl>
      <p className="day-card__note">{day.note}</p>
    </article>
  );
}
