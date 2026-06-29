import { useEffect, useState, type FormEvent } from "react";
import type { TripBlock, TripDay } from "@/lib/types/trip";

interface DayDetailDrawerProps {
  day: TripDay;
  onClose: () => void;
  onSave: (day: TripDay) => void;
}

const editableTimes: TripBlock["time"][] = ["Morning", "Afternoon", "Evening"];

function normalizeBlocks(day: TripDay) {
  return editableTimes.map(
    (time) =>
      day.blocks.find((block) => block.time === time) ?? {
        time,
        title: "",
        description: "",
      },
  );
}

export function DayDetailDrawer({ day, onClose, onSave }: DayDetailDrawerProps) {
  const [draftDay, setDraftDay] = useState(day);

  useEffect(() => {
    setDraftDay(day);
  }, [day]);

  const blocks = normalizeBlocks(draftDay);

  function updateBlock(time: TripBlock["time"], field: "title" | "description", value: string) {
    setDraftDay((currentDay) => {
      const nextBlocks = normalizeBlocks(currentDay).map((block) =>
        block.time === time ? { ...block, [field]: value } : block,
      );

      return {
        ...currentDay,
        blocks: nextBlocks,
      };
    });
  }

  function updateField(field: keyof Pick<TripDay, "city" | "stay" | "transport" | "note">, value: string) {
    setDraftDay((currentDay) => ({
      ...currentDay,
      [field]: value,
    }));
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    onSave({
      ...draftDay,
      blocks,
      status: "revised",
    });
  }

  return (
    <aside className="day-drawer" aria-label={`Day ${day.day} itinerary details`}>
      <div className="day-drawer__head">
        <div>
          <p>Day {day.day}</p>
          <h2>{draftDay.city}</h2>
          <span>{draftDay.pace} pace</span>
        </div>
        <button aria-label="Close day details" onClick={onClose} type="button">
          Close
        </button>
      </div>
      <form className="day-drawer__form" onSubmit={handleSubmit}>
        <label>
          City
          <input value={draftDay.city} onChange={(event) => updateField("city", event.target.value)} />
        </label>
        <div className="day-drawer__blocks">
          {blocks.map((block) => (
            <section className="day-drawer__block" key={`${day.day}-${block.time}`}>
              <span>{block.time}</span>
              <label>
                {block.time} title
                <input value={block.title} onChange={(event) => updateBlock(block.time, "title", event.target.value)} />
              </label>
              <label>
                {block.time} detail
                <textarea
                  value={block.description}
                  onChange={(event) => updateBlock(block.time, "description", event.target.value)}
                />
              </label>
            </section>
          ))}
        </div>
        <div className="day-drawer__details">
          <label>
            Hotel
            <input value={draftDay.stay} onChange={(event) => updateField("stay", event.target.value)} />
          </label>
          <label>
            Transport
            <input value={draftDay.transport} onChange={(event) => updateField("transport", event.target.value)} />
          </label>
          <label>
            Notes
            <textarea value={draftDay.note} onChange={(event) => updateField("note", event.target.value)} />
          </label>
        </div>
        <button className="day-drawer__save" type="submit">
          Save Day {day.day} changes
        </button>
      </form>
    </aside>
  );
}
