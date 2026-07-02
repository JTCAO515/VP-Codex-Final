import type { TripBlock, TripDay } from "@/lib/types/trip";

interface DayDetailDrawerProps {
  day: TripDay;
  onClose: () => void;
  onScheduleCandidate?: (day: TripDay, block: TripBlock) => void;
  busy?: boolean;
}

const displayTimes: TripBlock["time"][] = ["Morning", "Afternoon", "Evening"];

function normalizeBlocks(day: TripDay) {
  const scheduledBlocks = displayTimes.map(
    (time) =>
      day.blocks.find((block) => block.time === time) ?? {
        time,
        title: "",
        description: "",
      },
  );

  return [...scheduledBlocks, ...day.blocks.filter((block) => block.time === "Flexible")];
}

function hasOperationalDetails(block: TripBlock) {
  return Boolean(
    block.address ||
      block.chineseAddress ||
      block.phone ||
      block.openingHours ||
      block.mapUrl ||
      block.bookingUrl ||
      block.bookingCandidates?.length ||
      block.sourceLabel ||
      block.coordinates,
  );
}

function taxiDestination(block: TripBlock) {
  return block.chineseAddress || block.address || block.title;
}

function coordinateLabel(block: TripBlock) {
  if (!block.coordinates) return "";
  return `${block.coordinates.lat.toFixed(5)}, ${block.coordinates.lng.toFixed(5)}`;
}

export function DayDetailDrawer({ day, onClose, onScheduleCandidate, busy }: DayDetailDrawerProps) {
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
              <span data-flexible={block.time === "Flexible" ? "true" : "false"}>
                {block.time === "Flexible" ? "Needs scheduling" : block.time}
              </span>
              <strong>{block.title}</strong>
              <p>{block.description}</p>
              {block.time === "Flexible" && block.title ? (
                <div className="day-drawer__candidate-actions" aria-label={`${block.title} scheduling actions`}>
                  <button
                    disabled={!onScheduleCandidate || busy}
                    onClick={() => onScheduleCandidate?.(day, block)}
                    title={onScheduleCandidate ? undefined : "Open in Chat to schedule"}
                    type="button"
                  >
                    Ask VisePanda to schedule
                  </button>
                  <span>Candidate from Explore; not locked into the day yet.</span>
                </div>
              ) : null}
              {hasOperationalDetails(block) ? (
                <div className="day-drawer__poi" aria-label={`${block.title} execution details`}>
                  <dl className="day-drawer__poi-grid">
                    {block.chineseAddress || block.address ? (
                      <div>
                        <dt>Address</dt>
                        <dd>
                          {block.chineseAddress ? <span>{block.chineseAddress}</span> : null}
                          {block.address ? <span>{block.address}</span> : null}
                        </dd>
                      </div>
                    ) : null}
                    {block.openingHours ? (
                      <div>
                        <dt>Hours</dt>
                        <dd>{block.openingHours}</dd>
                      </div>
                    ) : null}
                    {block.phone ? (
                      <div>
                        <dt>Phone</dt>
                        <dd>{block.phone}</dd>
                      </div>
                    ) : null}
                    {coordinateLabel(block) ? (
                      <div>
                        <dt>Coordinates</dt>
                        <dd>{coordinateLabel(block)}</dd>
                      </div>
                    ) : null}
                  </dl>
                  <div className="day-drawer__poi-actions" aria-label={`${block.title} action links`}>
                    {block.mapUrl ? (
                      <a href={block.mapUrl} rel="noreferrer" target="_blank">
                        Open map
                      </a>
                    ) : null}
                    {block.bookingUrl ? (
                      <a href={block.bookingUrl} rel="noreferrer" target="_blank">
                        Booking info
                      </a>
                    ) : null}
                    {block.sourceLabel ? <span>Source: {block.sourceLabel}</span> : null}
                  </div>
                  {block.bookingCandidates?.length ? (
                    <div className="day-drawer__booking-candidates" aria-label={`${block.title} booking candidates`}>
                      <span>Booking candidates</span>
                      {block.bookingCandidates.map((candidate) => (
                        <div key={candidate.id}>
                          <strong>{candidate.label}</strong>
                          <p>
                            {candidate.provider} · {candidate.status === "info-only" ? "Info only" : "Planned"} ·{" "}
                            {candidate.note}
                          </p>
                          {candidate.priceHint ? <small>Price hint: {candidate.priceHint}</small> : null}
                        </div>
                      ))}
                    </div>
                  ) : null}
                  <div className="day-drawer__taxi-card">
                    <span>Show taxi driver</span>
                    <strong>{taxiDestination(block)}</strong>
                    {block.chineseAddress ? <p>请带我去：{block.chineseAddress}</p> : null}
                  </div>
                </div>
              ) : null}
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
