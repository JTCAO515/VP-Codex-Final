// CanvasPatch migration, phase 1 (see docs/planning/canvaspatch-edit-intent-migration-plan.md).
//
// Instead of the model regenerating a whole TripDay[] to express "adjust the
// trip", it emits one small, enumerable edit intent; this file executes it
// deterministically in code. Structural correctness comes from these pure
// functions and the strict parser below, not from trusting the model's JSON
// shape — the exact failure mode that caused the real normalizeDays incident
// this migration is meant to close off.
//
// Only create_trip's full-array path (butlerPrompt.ts normalizeDays) is
// unaffected by this file; that path is intentionally left alone.

import type { TripBlock, TripDay } from "@/lib/types/trip";

export type TripEditIntent =
  | { op: "add_block"; day: number; block: TripBlock; position?: number }
  | { op: "remove_block"; day: number; blockIndex: number }
  | { op: "update_block"; day: number; blockIndex: number; patch: Partial<TripBlock> }
  // Phase 2/3 ops — type placeholders only, no executor yet (see migration
  // plan doc section 6). parseTripEditIntent rejects them until implemented.
  | { op: "move_block"; day: number; fromIndex: number; toIndex: number }
  | { op: "set_day_field"; day: number; field: "pace" | "note" | "stay" | "transport"; value: string }
  | { op: "add_day"; afterDay: number; content: TripDay }
  | { op: "remove_day"; day: number }
  | { op: "replace_day_blocks"; day: number; blocks: TripBlock[] };

/** Thrown for any structurally invalid or semantically inapplicable edit intent — callers must treat this as "the adjustment failed," not silently drop content. */
export class TripEditIntentError extends Error {}

const VALID_BLOCK_TIMES = new Set<TripBlock["time"]>(["Morning", "Afternoon", "Evening", "Flexible"]);

function requireString(value: unknown, field: string): string {
  if (typeof value !== "string" || !value.trim()) {
    throw new TripEditIntentError(`Missing or invalid "${field}".`);
  }
  return value;
}

function requireNumber(value: unknown, field: string): number {
  if (typeof value !== "number" || !Number.isFinite(value)) {
    throw new TripEditIntentError(`Missing or invalid "${field}".`);
  }
  return value;
}

function requireBlockTime(value: unknown, field: string): TripBlock["time"] {
  if (typeof value !== "string" || !VALID_BLOCK_TIMES.has(value as TripBlock["time"])) {
    throw new TripEditIntentError(`"${field}" must be one of Morning/Afternoon/Evening/Flexible.`);
  }
  return value as TripBlock["time"];
}

function parseTripBlock(raw: unknown): TripBlock {
  if (!raw || typeof raw !== "object") {
    throw new TripEditIntentError("block must be an object.");
  }
  const candidate = raw as Record<string, unknown>;
  return {
    time: requireBlockTime(candidate.time, "block.time"),
    title: requireString(candidate.title, "block.title"),
    description: typeof candidate.description === "string" ? candidate.description : "",
  };
}

function parseBlockPatch(raw: unknown): Partial<TripBlock> {
  if (!raw || typeof raw !== "object") {
    throw new TripEditIntentError("patch must be an object.");
  }
  const candidate = raw as Record<string, unknown>;
  const patch: Partial<TripBlock> = {};
  if (candidate.time !== undefined) patch.time = requireBlockTime(candidate.time, "patch.time");
  if (candidate.title !== undefined) patch.title = requireString(candidate.title, "patch.title");
  if (candidate.description !== undefined) {
    if (typeof candidate.description !== "string") {
      throw new TripEditIntentError('"patch.description" must be a string.');
    }
    patch.description = candidate.description;
  }
  if (Object.keys(patch).length === 0) {
    throw new TripEditIntentError("update_block patch did not include any recognized fields.");
  }
  return patch;
}

/**
 * Validates a model's raw JSON edit intent into a TripEditIntent. Throws
 * (rather than coercing with defaults) on anything malformed — an edit
 * intent that fails validation must fail the whole patch, never apply a
 * half-understood change to the traveler's itinerary.
 */
export function parseTripEditIntent(raw: unknown): TripEditIntent {
  if (!raw || typeof raw !== "object") {
    throw new TripEditIntentError("editIntent must be an object.");
  }
  const candidate = raw as Record<string, unknown>;

  switch (candidate.op) {
    case "add_block":
      return {
        op: "add_block",
        day: requireNumber(candidate.day, "day"),
        block: parseTripBlock(candidate.block),
        position: candidate.position === undefined ? undefined : requireNumber(candidate.position, "position"),
      };
    case "remove_block":
      return {
        op: "remove_block",
        day: requireNumber(candidate.day, "day"),
        blockIndex: requireNumber(candidate.blockIndex, "blockIndex"),
      };
    case "update_block":
      return {
        op: "update_block",
        day: requireNumber(candidate.day, "day"),
        blockIndex: requireNumber(candidate.blockIndex, "blockIndex"),
        patch: parseBlockPatch(candidate.patch),
      };
    default:
      throw new TripEditIntentError(`Unknown or not-yet-supported edit op: ${String(candidate.op)}`);
  }
}

function findDayIndex(days: TripDay[], dayNumber: number): number {
  const index = days.findIndex((day) => day.day === dayNumber);
  if (index === -1) {
    throw new TripEditIntentError(`Day ${dayNumber} does not exist in the current trip.`);
  }
  return index;
}

function withDayBlocks(days: TripDay[], dayIndex: number, blocks: TripBlock[]): TripDay[] {
  return days.map((day, index) => (index === dayIndex ? { ...day, blocks } : day));
}

function applyAddBlock(days: TripDay[], intent: Extract<TripEditIntent, { op: "add_block" }>): TripDay[] {
  const dayIndex = findDayIndex(days, intent.day);
  const blocks = [...days[dayIndex].blocks];
  const position = intent.position ?? blocks.length;
  if (position < 0 || position > blocks.length) {
    throw new TripEditIntentError(`add_block position ${position} is out of range for day ${intent.day}.`);
  }
  blocks.splice(position, 0, intent.block);
  return withDayBlocks(days, dayIndex, blocks);
}

function applyRemoveBlock(days: TripDay[], intent: Extract<TripEditIntent, { op: "remove_block" }>): TripDay[] {
  const dayIndex = findDayIndex(days, intent.day);
  const blocks = days[dayIndex].blocks;
  if (intent.blockIndex < 0 || intent.blockIndex >= blocks.length) {
    throw new TripEditIntentError(`remove_block index ${intent.blockIndex} is out of range for day ${intent.day}.`);
  }
  return withDayBlocks(days, dayIndex, blocks.filter((_, index) => index !== intent.blockIndex));
}

function applyUpdateBlock(days: TripDay[], intent: Extract<TripEditIntent, { op: "update_block" }>): TripDay[] {
  const dayIndex = findDayIndex(days, intent.day);
  const blocks = days[dayIndex].blocks;
  if (intent.blockIndex < 0 || intent.blockIndex >= blocks.length) {
    throw new TripEditIntentError(`update_block index ${intent.blockIndex} is out of range for day ${intent.day}.`);
  }
  const nextBlocks = blocks.map((block, index) => (index === intent.blockIndex ? { ...block, ...intent.patch } : block));
  return withDayBlocks(days, dayIndex, nextBlocks);
}

/** Executes a validated TripEditIntent against the current days, returning the new full array — callers put this straight into CanvasPatch.days, same shape as the create_trip path. */
export function applyTripEditIntent(days: TripDay[], intent: TripEditIntent): TripDay[] {
  switch (intent.op) {
    case "add_block":
      return applyAddBlock(days, intent);
    case "remove_block":
      return applyRemoveBlock(days, intent);
    case "update_block":
      return applyUpdateBlock(days, intent);
    default:
      throw new TripEditIntentError(`Edit op "${intent.op}" is not implemented yet.`);
  }
}
