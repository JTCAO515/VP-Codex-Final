// CanvasPatch migration, phases 1-3 (see docs/planning/canvaspatch-edit-intent-migration-plan.md).
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

import type { Pace, TripBlock, TripDay } from "@/lib/types/trip";

export type TripEditIntent =
  | { op: "add_block"; day: number; block: TripBlock; position?: number }
  | { op: "remove_block"; day: number; blockIndex: number }
  | { op: "update_block"; day: number; blockIndex: number; patch: Partial<TripBlock> }
  | { op: "move_block"; day: number; fromIndex: number; toIndex: number }
  | { op: "set_day_field"; day: number; field: "pace" | "note" | "stay" | "transport"; value: string }
  // afterDay 0 means "insert as the new first day"; content.day is ignored —
  // the executor renumbers every day sequentially after inserting, so the
  // model never has to get downstream day numbers right.
  | { op: "add_day"; afterDay: number; content: Omit<TripDay, "day"> }
  | { op: "remove_day"; day: number }
  // Tier 2 escape hatch (migration plan doc section 3.2): bounded to ONE
  // day's blocks, not the whole trip — reuses the same tolerant/coercive
  // block validation as the create_trip normalizeDays path (phase A
  // hardening), not the strict Tier 1 parseTripBlock above, because this op
  // exists precisely for requests too open-ended for a precise Tier 1 op.
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

const VALID_PACE_VALUES = new Set<Pace>(["Light", "Balanced", "Relaxed", "Packed"]);
const DAY_STRING_FIELDS = new Set(["pace", "note", "stay", "transport"]);

function requireDayField(value: unknown): "pace" | "note" | "stay" | "transport" {
  if (typeof value !== "string" || !DAY_STRING_FIELDS.has(value)) {
    throw new TripEditIntentError('"field" must be one of pace/note/stay/transport.');
  }
  return value as "pace" | "note" | "stay" | "transport";
}

function requireDayFieldValue(field: "pace" | "note" | "stay" | "transport", value: unknown): string {
  if (field === "pace") {
    if (typeof value !== "string" || !VALID_PACE_VALUES.has(value as Pace)) {
      throw new TripEditIntentError('"value" for field "pace" must be one of Light/Balanced/Relaxed/Packed.');
    }
    return value;
  }
  if (typeof value !== "string") {
    throw new TripEditIntentError(`"value" for field "${field}" must be a string (it may be empty).`);
  }
  return value;
}

/** Loose/coercive block parsing for the Tier 2 replace_day_blocks escape hatch — mirrors butlerPrompt.ts's normalizeDays block coercion (defaults instead of throwing), not Tier 1's strict parseTripBlock. */
function coerceTripBlockForReplaceDayBlocks(raw: unknown): TripBlock {
  if (!raw || typeof raw !== "object") {
    throw new TripEditIntentError("Each block in replace_day_blocks.blocks must be an object.");
  }
  const candidate = raw as Record<string, unknown>;
  const time = typeof candidate.time === "string" && VALID_BLOCK_TIMES.has(candidate.time as TripBlock["time"])
    ? (candidate.time as TripBlock["time"])
    : "Flexible";
  const title = typeof candidate.title === "string" && candidate.title.trim() ? candidate.title : "Untitled stop";
  const description = typeof candidate.description === "string" ? candidate.description : "";
  return { time, title, description };
}

function parseDayContentForAddDay(raw: unknown): Omit<TripDay, "day"> {
  if (!raw || typeof raw !== "object") {
    throw new TripEditIntentError("add_day.content must be an object.");
  }
  const candidate = raw as Record<string, unknown>;
  if (!Array.isArray(candidate.blocks)) {
    throw new TripEditIntentError("add_day.content.blocks must be an array.");
  }
  return {
    city: requireString(candidate.city, "content.city"),
    pace: requireDayFieldValue("pace", candidate.pace) as Pace,
    blocks: candidate.blocks.map(parseTripBlock),
    food: Array.isArray(candidate.food) ? candidate.food.filter((f): f is string => typeof f === "string") : [],
    stay: typeof candidate.stay === "string" ? candidate.stay : "",
    transport: typeof candidate.transport === "string" ? candidate.transport : "",
    note: typeof candidate.note === "string" ? candidate.note : "",
  };
}

/**
 * Validates a model's raw JSON edit intent into a TripEditIntent. Throws
 * (rather than coercing with defaults) on anything malformed — an edit
 * intent that fails validation must fail the whole patch, never apply a
 * half-understood change to the traveler's itinerary. The one deliberate
 * exception is replace_day_blocks' blocks (see coerceTripBlockForReplaceDayBlocks).
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
    case "move_block":
      return {
        op: "move_block",
        day: requireNumber(candidate.day, "day"),
        fromIndex: requireNumber(candidate.fromIndex, "fromIndex"),
        toIndex: requireNumber(candidate.toIndex, "toIndex"),
      };
    case "set_day_field": {
      const field = requireDayField(candidate.field);
      return {
        op: "set_day_field",
        day: requireNumber(candidate.day, "day"),
        field,
        value: requireDayFieldValue(field, candidate.value),
      };
    }
    case "add_day":
      return {
        op: "add_day",
        afterDay: requireNumber(candidate.afterDay, "afterDay"),
        content: parseDayContentForAddDay(candidate.content),
      };
    case "remove_day":
      return {
        op: "remove_day",
        day: requireNumber(candidate.day, "day"),
      };
    case "replace_day_blocks": {
      if (!Array.isArray(candidate.blocks)) {
        throw new TripEditIntentError("replace_day_blocks.blocks must be an array.");
      }
      return {
        op: "replace_day_blocks",
        day: requireNumber(candidate.day, "day"),
        blocks: candidate.blocks.map(coerceTripBlockForReplaceDayBlocks),
      };
    }
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

function applyMoveBlock(days: TripDay[], intent: Extract<TripEditIntent, { op: "move_block" }>): TripDay[] {
  const dayIndex = findDayIndex(days, intent.day);
  const blocks = days[dayIndex].blocks;
  if (intent.fromIndex < 0 || intent.fromIndex >= blocks.length) {
    throw new TripEditIntentError(`move_block fromIndex ${intent.fromIndex} is out of range for day ${intent.day}.`);
  }
  if (intent.toIndex < 0 || intent.toIndex >= blocks.length) {
    throw new TripEditIntentError(`move_block toIndex ${intent.toIndex} is out of range for day ${intent.day}.`);
  }
  const nextBlocks = [...blocks];
  const [moved] = nextBlocks.splice(intent.fromIndex, 1);
  nextBlocks.splice(intent.toIndex, 0, moved);
  return withDayBlocks(days, dayIndex, nextBlocks);
}

function applySetDayField(days: TripDay[], intent: Extract<TripEditIntent, { op: "set_day_field" }>): TripDay[] {
  const dayIndex = findDayIndex(days, intent.day);
  return days.map((day, index) => (index === dayIndex ? { ...day, [intent.field]: intent.value } : day));
}

function renumberDays(days: TripDay[]): TripDay[] {
  return days.map((day, index) => ({ ...day, day: index + 1 }));
}

function applyAddDay(days: TripDay[], intent: Extract<TripEditIntent, { op: "add_day" }>): TripDay[] {
  if (intent.afterDay < 0 || intent.afterDay > days.length) {
    throw new TripEditIntentError(`add_day afterDay ${intent.afterDay} is out of range for a ${days.length}-day trip.`);
  }
  const newDay: TripDay = { ...intent.content, day: 0 };
  const next = [...days];
  next.splice(intent.afterDay, 0, newDay);
  return renumberDays(next);
}

function applyRemoveDay(days: TripDay[], intent: Extract<TripEditIntent, { op: "remove_day" }>): TripDay[] {
  const dayIndex = findDayIndex(days, intent.day);
  if (days.length === 1) {
    throw new TripEditIntentError("remove_day cannot remove the last remaining day of a trip.");
  }
  const next = days.filter((_, index) => index !== dayIndex);
  return renumberDays(next);
}

function applyReplaceDayBlocks(days: TripDay[], intent: Extract<TripEditIntent, { op: "replace_day_blocks" }>): TripDay[] {
  const dayIndex = findDayIndex(days, intent.day);
  return withDayBlocks(days, dayIndex, intent.blocks);
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
    case "move_block":
      return applyMoveBlock(days, intent);
    case "set_day_field":
      return applySetDayField(days, intent);
    case "add_day":
      return applyAddDay(days, intent);
    case "remove_day":
      return applyRemoveDay(days, intent);
    case "replace_day_blocks":
      return applyReplaceDayBlocks(days, intent);
    default:
      throw new TripEditIntentError(`Edit op "${(intent as { op: string }).op}" is not implemented yet.`);
  }
}
