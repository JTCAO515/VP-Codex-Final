import type { TripDay } from "@/lib/types/trip";

export function DayDetailDrawer({ day }: { day: TripDay }) {
  return (
    <aside className="day-drawer" aria-label={`Day ${day.day} itinerary details`}>
      <div className="day-drawer__head">
        <p>Day {day.day}</p>
        <h2>{day.city}</h2>
        <span>{day.pace} pace</span>
      </div>
      <div className="day-drawer__blocks">
        {day.blocks.map((block) => (
          <section className="day-drawer__block" key={`${day.day}-${block.time}-${block.title}`}>
            <span>{block.time}</span>
            <strong>{block.title}</strong>
            <p>{block.description}</p>
          </section>
        ))}
      </div>
      <dl className="day-drawer__details">
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
      <p className="day-drawer__note">{day.note}</p>
    </aside>
  );
}
