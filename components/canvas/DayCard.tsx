"use client";

import { useEffect, useRef, useState } from "react";
import type { TripBlock, TripDay } from "@/lib/types/trip";
import { DAY_QUICK_ACTIONS, buildQuickActionMessage, type QuickActionKind } from "@/lib/canvas/quickActions";
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

export function DayCard({ day, isSelected, onSelect, onQuickAction, highlightNonce, busy, registerRef }: DayCardProps) {
  const blocks = timeSlots.map((time) => getBlockForTime(day, time));
  const [pulseTrigger, setPulseTrigger] = useState(0);
  const previousContentKeyRef = useRef<string>("");

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
      {onQuickAction ? (
        <div className="day-card__quick-actions" aria-label={`Day ${day.day} quick actions`}>
          {DAY_QUICK_ACTIONS.map((action) => (
            <button
              disabled={busy}
              key={action.kind}
              onClick={() => onQuickAction(buildQuickActionMessage(action.kind, day), action.kind)}
              type="button"
            >
              {action.label}
            </button>
          ))}
        </div>
      ) : null}
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
