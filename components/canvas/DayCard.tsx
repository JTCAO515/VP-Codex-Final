import type { TripDay } from "@/lib/types/trip";

interface DayCardProps {
  day: TripDay;
  isSelected: boolean;
  onSelect: () => void;
}

export function DayCard({ day, isSelected, onSelect }: DayCardProps) {
  const firstBlock = day.blocks[0];
  const summary = firstBlock
    ? `${firstBlock.title}: ${firstBlock.description}`
    : `${day.city} day planned at a ${day.pace.toLowerCase()} pace.`;

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
      <p className="day-card__summary">{summary}</p>
      <button
        aria-expanded={isSelected}
        aria-haspopup="dialog"
        aria-label={`View Day ${day.day} details`}
        className="day-card__button"
        onClick={onSelect}
        type="button"
      >
        View details
      </button>
    </article>
  );
}
