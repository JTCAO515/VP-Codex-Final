"use client";

import { Clock3, MoonStar, MoreHorizontal, Sun, Sunrise } from "lucide-react";
import { useEffect, useRef, useState } from "react";
import type { TripBlock, TripDay } from "@/lib/types/trip";
import { calculateDayCompleteness } from "@/lib/trips/completeness";
import {
  DAY_PRIMARY_ACTIONS,
  DAY_SECONDARY_ACTIONS,
  buildQuickActionMessage,
  type QuickActionKind,
} from "@/lib/canvas/quickActions";
import { useReplayableAnimation } from "@/lib/canvas/useReplayableAnimation";

interface DayCardProps {
  day: TripDay;
  isSelected: boolean;
  onSelect: () => void;
  onQuickAction?: (message: string, kind: QuickActionKind) => void;
  /** Bumped by the parent (any change, including repeats) to replay the pulse animation, e.g. when a Change Digest entry for this day is clicked. */
  highlightNonce?: number;
  busy?: boolean;
  registerRef?: (day: number, element: HTMLElement | null) => void;
}

const timeSlots: TripBlock["time"][] = ["Morning", "Afternoon", "Evening", "Flexible"];

const TIME_ICON: Record<string, typeof Sunrise> = {
  Morning: Sunrise,
  Afternoon: Sun,
  Evening: MoonStar,
  Flexible: Clock3,
};

function getBlockForTime(day: TripDay, time: TripBlock["time"]) {
  return (
    day.blocks.find((block) => block.time === time) ?? {
      time,
      title: `${day.city} open time`,
      description: "Keep this block flexible while VisePanda refines the route.",
    }
  );
}

export function DayCard({ day, isSelected, onSelect, onQuickAction, highlightNonce, busy, registerRef }: DayCardProps) {
  const blocks = timeSlots.map((time) => getBlockForTime(day, time));
  const [pulseTrigger, setPulseTrigger] = useState(0);
  const [showMore, setShowMore] = useState(false);
  const previousContentKeyRef = useRef<string>("");
  const dayCompleteness = calculateDayCompleteness(day);

  // Replay the gold pulse whenever this day is freshly marked "revised" by an
  // AI patch (identity/content-based, so two consecutive revisions both pulse).
  useEffect(() => {
    const contentKey = JSON.stringify({ status: day.status, blocks: day.blocks, stay: day.stay, transport: day.transport });
    if (day.status === "revised" && contentKey !== previousContentKeyRef.current) {
      setPulseTrigger((n) => n + 1);
    }
    previousContentKeyRef.current = contentKey;
  }, [day]);

  // Replay the pulse when the parent asks for a Change-Digest-click highlight.
  useEffect(() => {
    if (highlightNonce) setPulseTrigger((n) => n + 1);
  }, [highlightNonce]);

  const pulseRef = useReplayableAnimation<HTMLElement>(pulseTrigger, "day-card--pulse");

  function runAction(kind: QuickActionKind) {
    onQuickAction?.(buildQuickActionMessage(kind, day), kind);
    setShowMore(false);
  }

  return (
    <article
      className="day-card"
      data-selected={isSelected ? "true" : "false"}
      data-status={day.status ?? "stable"}
      ref={(element) => {
        pulseRef.current = element;
        registerRef?.(day.day, element);
      }}
    >
      <div className="day-card__marker" aria-hidden="true">
        <span>Day</span>
        <strong>{day.day}</strong>
      </div>
      <div className="day-card__head">
        <h2>{day.city}</h2>
        <div className="day-card__head-meta">
          <span>{day.pace}</span>
          <span className="day-card__completeness" data-complete={dayCompleteness >= 100 ? "true" : "false"}>
            {dayCompleteness}%
          </span>
        </div>
      </div>
      <div className="day-card__blocks" aria-label={`Day ${day.day} schedule`}>
        {blocks.map((block) => {
          const TimeIcon = TIME_ICON[block.time] ?? Sun;
          const items = block.highlights?.length ? block.highlights : block.description ? [block.description] : [];
          return (
            <div className="day-block" key={`${day.day}-${block.time}`}>
              {block.photoUrl ? (
                <img alt="" className="day-block__photo" src={block.photoUrl} />
              ) : (
                <div aria-hidden="true" className="day-block__photo day-block__photo--placeholder">
                  <TimeIcon size={20} strokeWidth={1.6} />
                </div>
              )}
              <div className="day-block__body">
                <span className="day-block__time">
                  <TimeIcon aria-hidden="true" size={13} strokeWidth={1.8} />
                  {block.time}
                </span>
                <strong>{block.title}</strong>
                {items.length > 0 ? (
                  <ul>
                    {items.map((item) => (
                      <li key={item}>{item}</li>
                    ))}
                  </ul>
                ) : null}
              </div>
            </div>
          );
        })}
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
      <div className="day-card__actions" aria-label={`Day ${day.day} actions`}>
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
        {onQuickAction ? (
          <div className="day-card__quick-actions" aria-label={`Day ${day.day} quick actions`}>
            {DAY_PRIMARY_ACTIONS.map((action) => (
              <button disabled={busy} key={action.kind} onClick={() => runAction(action.kind)} type="button">
                {action.label}
              </button>
            ))}
            <div className="day-card__overflow">
              <button
                aria-expanded={showMore}
                aria-label={`More actions for Day ${day.day}`}
                onClick={() => setShowMore((v) => !v)}
                type="button"
              >
                <MoreHorizontal aria-hidden="true" size={16} strokeWidth={1.8} />
              </button>
              {showMore ? (
                <div className="day-card__overflow-menu" role="menu">
                  {DAY_SECONDARY_ACTIONS.map((action) => (
                    <button
                      disabled={busy}
                      key={action.kind}
                      onClick={() => runAction(action.kind)}
                      role="menuitem"
                      type="button"
                    >
                      {action.label}
                    </button>
                  ))}
                </div>
              ) : null}
            </div>
          </div>
        ) : null}
      </div>
    </article>
  );
}
