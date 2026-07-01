import type { TripBlock, TripDay } from "@/lib/types/trip";

interface DayDetailDrawerProps {
  day: TripDay;
  onClose: () => void;
}

const displayTimes: TripBlock["time"][] = ["Morning", "Afternoon", "Evening"];

function normalizeBlocks(day: TripDay) {
  return displayTimes.map(
    (time) =>
      day.blocks.find((block) => block.time === time) ?? {
        time,
        title: "",
        description: "",
      },
  );
}

export function DayDetailDrawer({ day, onClose }: DayDetailDrawerProps) {
  const blocks = normalizeBlocks(day);

  return (
    <aside className="day-drawer" aria-label={`Day ${day.day} itinerary details`}>
      <div className="day-drawer__head">
        <div>
          <p>Day {day.day}</p>
          <h2>{day.city}</h2>
          <span>{day.pace} pace</span>
        </div>
        <button aria-label="Close day details" onClick={onClose} type="button">
          Close
        </button>
      </div>
      <div className="day-drawer__form">
        <div className="day-drawer__blocks">
          {blocks.map((block) => (
            <section className="day-drawer__block" key={`${day.day}-${block.time}`}>
              <span>{block.time}</span>
              <strong>{block.title}</strong>
              <p>{block.description}</p>
            </section>
          ))}
        </div>
        <dl className="day-drawer__details">
          <div>
            <dt>Hotel</dt>
            <dd>{day.stay}</dd>
          </div>
          <div>
            <dt>Transport</dt>
            <dd>{day.transport}</dd>
          </div>
        </dl>
        <div className="day-drawer__note">
          <span>Notes</span>
          <p>{day.note}</p>
        </div>
      </div>
    </aside>
  );
}
