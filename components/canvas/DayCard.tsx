import type { TripBlock, TripDay } from "@/lib/types/trip";

interface DayCardProps {
  day: TripDay;
  isSelected: boolean;
  onSelect: () => void;
}

const timeSlots: TripBlock["time"][] = ["Morning", "Afternoon", "Evening"];

function getBlockForTime(day: TripDay, time: TripBlock["time"]) {
  return (
    day.blocks.find((block) => block.time === time) ?? {
      time,
      title: `${day.city} open time`,
      description: "Keep this block flexible while VisePanda refines the route.",
    }
  );
}

export function DayCard({ day, isSelected, onSelect }: DayCardProps) {
  const blocks = timeSlots.map((time) => getBlockForTime(day, time));

  return (
    <article className="day-card" data-selected={isSelected ? "true" : "false"} data-status={day.status ?? "stable"}>
      <div className="day-card__marker" aria-hidden="true">
        <span>Day</span>
        <strong>{day.day}</strong>
      </div>
      <div className="day-card__head">
        <h2>{day.city}</h2>
        <span>{day.pace}</span>
      </div>
      <div className="day-card__blocks" aria-label={`Day ${day.day} schedule`}>
        {blocks.map((block) => (
          <div className="day-block" key={`${day.day}-${block.time}`}>
            <div className="day-block__image" aria-hidden="true">
              {block.title}
            </div>
            <span>{block.time}</span>
            <strong>{block.title}</strong>
          </div>
        ))}
      </div>
      <div className="day-card__meta">
        <span>Hotel: {day.stay}</span>
        <span>Transport: {day.transport}</span>
        <span>Pace: {day.pace.toLowerCase()}</span>
        <span>~$80-120/day estimated</span>
        <button
          aria-expanded={isSelected}
          aria-haspopup="dialog"
          aria-label={`Open Day ${day.day} map notes`}
          className="day-card__link"
          onClick={onSelect}
          type="button"
        >
          Map
        </button>
        <button aria-label={`Open Day ${day.day} notes`} className="day-card__link" onClick={onSelect} type="button">
          Notes
        </button>
      </div>
      <button
        aria-expanded={isSelected}
        aria-haspopup="dialog"
        aria-label={`View details for Day ${day.day}`}
        className="day-card__button"
        onClick={onSelect}
        type="button"
      >
        View details
      </button>
    </article>
  );
}
