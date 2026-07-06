import { describe, expect, it } from "vitest";
import { applyTripEditIntent, parseTripEditIntent, TripEditIntentError } from "@/lib/canvas/tripEditIntents";
import type { TripDay } from "@/lib/types/trip";

function days(): TripDay[] {
  return [
    {
      day: 1,
      city: "Beijing",
      pace: "Balanced",
      blocks: [
        { time: "Morning", title: "Tiananmen Square", description: "Arrive early." },
        { time: "Afternoon", title: "Forbidden City", description: "Full palace tour." },
      ],
      food: [],
      stay: "",
      transport: "",
      note: "",
    },
    {
      day: 2,
      city: "Beijing",
      pace: "Balanced",
      blocks: [{ time: "Morning", title: "Great Wall", description: "Mutianyu section." }],
      food: [],
      stay: "",
      transport: "",
      note: "",
    },
  ];
}

describe("parseTripEditIntent", () => {
  it("parses a valid add_block intent", () => {
    const intent = parseTripEditIntent({
      op: "add_block",
      day: 1,
      block: { time: "Evening", title: "Wangfujing Street", description: "Evening food street." },
    });
    expect(intent).toMatchObject({ op: "add_block", day: 1 });
  });

  it("parses a valid remove_block intent", () => {
    const intent = parseTripEditIntent({ op: "remove_block", day: 1, blockIndex: 0 });
    expect(intent).toEqual({ op: "remove_block", day: 1, blockIndex: 0 });
  });

  it("parses a valid update_block intent", () => {
    const intent = parseTripEditIntent({ op: "update_block", day: 1, blockIndex: 0, patch: { title: "Renamed" } });
    expect(intent).toEqual({ op: "update_block", day: 1, blockIndex: 0, patch: { title: "Renamed" } });
  });

  it("throws on an unknown op", () => {
    expect(() => parseTripEditIntent({ op: "teleport_block", day: 1 })).toThrow(TripEditIntentError);
  });

  it("parses a valid move_block intent", () => {
    const intent = parseTripEditIntent({ op: "move_block", day: 1, fromIndex: 0, toIndex: 1 });
    expect(intent).toEqual({ op: "move_block", day: 1, fromIndex: 0, toIndex: 1 });
  });

  it("parses a valid set_day_field intent for pace", () => {
    const intent = parseTripEditIntent({ op: "set_day_field", day: 1, field: "pace", value: "Relaxed" });
    expect(intent).toEqual({ op: "set_day_field", day: 1, field: "pace", value: "Relaxed" });
  });

  it("throws on set_day_field with an invalid pace value", () => {
    expect(() => parseTripEditIntent({ op: "set_day_field", day: 1, field: "pace", value: "Chaotic" })).toThrow(
      TripEditIntentError,
    );
  });

  it("throws on set_day_field with an unrecognized field", () => {
    expect(() => parseTripEditIntent({ op: "set_day_field", day: 1, field: "city", value: "Shanghai" })).toThrow(
      TripEditIntentError,
    );
  });

  it("parses a valid add_day intent", () => {
    const intent = parseTripEditIntent({
      op: "add_day",
      afterDay: 1,
      content: {
        city: "Suzhou",
        pace: "Light",
        blocks: [{ time: "Morning", title: "Humble Administrator's Garden", description: "" }],
        food: ["Suzhou noodles"],
        stay: "",
        transport: "",
        note: "",
      },
    });
    expect(intent).toMatchObject({ op: "add_day", afterDay: 1 });
  });

  it("throws on add_day with a missing content.city", () => {
    expect(() =>
      parseTripEditIntent({ op: "add_day", afterDay: 1, content: { pace: "Light", blocks: [] } }),
    ).toThrow(TripEditIntentError);
  });

  it("parses a valid remove_day intent", () => {
    const intent = parseTripEditIntent({ op: "remove_day", day: 2 });
    expect(intent).toEqual({ op: "remove_day", day: 2 });
  });

  it("parses a valid replace_day_blocks intent, coercing a malformed block instead of throwing", () => {
    const intent = parseTripEditIntent({
      op: "replace_day_blocks",
      day: 1,
      blocks: [{ time: "Nonsense", title: "", description: "kept" }],
    });
    expect(intent).toEqual({
      op: "replace_day_blocks",
      day: 1,
      blocks: [{ time: "Flexible", title: "Untitled stop", description: "kept" }],
    });
  });

  it("throws on replace_day_blocks when blocks is not an array", () => {
    expect(() => parseTripEditIntent({ op: "replace_day_blocks", day: 1, blocks: "nope" })).toThrow(TripEditIntentError);
  });

  it("throws when add_block's block has an invalid time", () => {
    expect(() =>
      parseTripEditIntent({ op: "add_block", day: 1, block: { time: "Midnight", title: "X", description: "" } }),
    ).toThrow(TripEditIntentError);
  });

  it("throws when add_block's block is missing a title", () => {
    expect(() => parseTripEditIntent({ op: "add_block", day: 1, block: { time: "Morning", description: "" } })).toThrow(
      TripEditIntentError,
    );
  });

  it("throws when update_block's patch has no recognized fields", () => {
    expect(() => parseTripEditIntent({ op: "update_block", day: 1, blockIndex: 0, patch: {} })).toThrow(TripEditIntentError);
  });

  it("throws when the raw value isn't an object", () => {
    expect(() => parseTripEditIntent(null)).toThrow(TripEditIntentError);
    expect(() => parseTripEditIntent("add_block")).toThrow(TripEditIntentError);
  });
});

describe("applyTripEditIntent", () => {
  it("add_block appends by default", () => {
    const next = applyTripEditIntent(days(), {
      op: "add_block",
      day: 1,
      block: { time: "Evening", title: "Wangfujing Street", description: "" },
    });
    expect(next[0].blocks).toHaveLength(3);
    expect(next[0].blocks[2].title).toBe("Wangfujing Street");
    // Other days are untouched.
    expect(next[1]).toEqual(days()[1]);
  });

  it("add_block inserts at a specific position", () => {
    const next = applyTripEditIntent(days(), {
      op: "add_block",
      day: 1,
      position: 0,
      block: { time: "Morning", title: "Sunrise viewpoint", description: "" },
    });
    expect(next[0].blocks[0].title).toBe("Sunrise viewpoint");
    expect(next[0].blocks).toHaveLength(3);
  });

  it("add_block throws for a day that doesn't exist", () => {
    expect(() =>
      applyTripEditIntent(days(), { op: "add_block", day: 99, block: { time: "Morning", title: "X", description: "" } }),
    ).toThrow(TripEditIntentError);
  });

  it("add_block throws when position is out of range", () => {
    expect(() =>
      applyTripEditIntent(days(), {
        op: "add_block",
        day: 1,
        position: 99,
        block: { time: "Morning", title: "X", description: "" },
      }),
    ).toThrow(TripEditIntentError);
  });

  it("remove_block removes the targeted block only", () => {
    const next = applyTripEditIntent(days(), { op: "remove_block", day: 1, blockIndex: 0 });
    expect(next[0].blocks).toHaveLength(1);
    expect(next[0].blocks[0].title).toBe("Forbidden City");
  });

  it("remove_block throws on an out-of-range index", () => {
    expect(() => applyTripEditIntent(days(), { op: "remove_block", day: 1, blockIndex: 5 })).toThrow(TripEditIntentError);
  });

  it("update_block merges the patch into the existing block, keeping other fields", () => {
    const next = applyTripEditIntent(days(), {
      op: "update_block",
      day: 1,
      blockIndex: 1,
      patch: { time: "Evening" },
    });
    expect(next[0].blocks[1]).toMatchObject({ time: "Evening", title: "Forbidden City", description: "Full palace tour." });
  });

  it("update_block throws on an out-of-range index", () => {
    expect(() => applyTripEditIntent(days(), { op: "update_block", day: 1, blockIndex: 5, patch: { title: "X" } })).toThrow(
      TripEditIntentError,
    );
  });

  it("move_block reorders within a day, leaving other days untouched", () => {
    const next = applyTripEditIntent(days(), { op: "move_block", day: 1, fromIndex: 0, toIndex: 1 });
    expect(next[0].blocks.map((b) => b.title)).toEqual(["Forbidden City", "Tiananmen Square"]);
    expect(next[1]).toEqual(days()[1]);
  });

  it("move_block throws on out-of-range indices", () => {
    expect(() => applyTripEditIntent(days(), { op: "move_block", day: 1, fromIndex: 5, toIndex: 0 })).toThrow(
      TripEditIntentError,
    );
    expect(() => applyTripEditIntent(days(), { op: "move_block", day: 1, fromIndex: 0, toIndex: 5 })).toThrow(
      TripEditIntentError,
    );
  });

  it("set_day_field updates only the targeted field on the targeted day", () => {
    const next = applyTripEditIntent(days(), { op: "set_day_field", day: 2, field: "pace", value: "Relaxed" });
    expect(next[1].pace).toBe("Relaxed");
    expect(next[0]).toEqual(days()[0]);
  });

  it("set_day_field throws for a day that doesn't exist", () => {
    expect(() => applyTripEditIntent(days(), { op: "set_day_field", day: 99, field: "note", value: "x" })).toThrow(
      TripEditIntentError,
    );
  });

  it("add_day inserts after the given day and renumbers all days sequentially", () => {
    const next = applyTripEditIntent(days(), {
      op: "add_day",
      afterDay: 1,
      content: {
        city: "Suzhou",
        pace: "Light",
        blocks: [{ time: "Morning", title: "Humble Administrator's Garden", description: "" }],
        food: [],
        stay: "",
        transport: "",
        note: "",
      },
    });
    expect(next).toHaveLength(3);
    expect(next.map((d) => d.day)).toEqual([1, 2, 3]);
    expect(next[1].city).toBe("Suzhou");
    expect(next[2].city).toBe("Beijing");
  });

  it("add_day with afterDay 0 prepends as the new first day", () => {
    const next = applyTripEditIntent(days(), {
      op: "add_day",
      afterDay: 0,
      content: { city: "Arrival day", pace: "Light", blocks: [], food: [], stay: "", transport: "", note: "" },
    });
    expect(next.map((d) => d.day)).toEqual([1, 2, 3]);
    expect(next[0].city).toBe("Arrival day");
  });

  it("add_day throws when afterDay is out of range", () => {
    expect(() =>
      applyTripEditIntent(days(), {
        op: "add_day",
        afterDay: 99,
        content: { city: "X", pace: "Light", blocks: [], food: [], stay: "", transport: "", note: "" },
      }),
    ).toThrow(TripEditIntentError);
  });

  it("remove_day deletes the day and renumbers the remaining days sequentially", () => {
    const next = applyTripEditIntent(days(), { op: "remove_day", day: 1 });
    expect(next).toHaveLength(1);
    expect(next[0]).toMatchObject({ day: 1, city: "Beijing", blocks: [{ title: "Great Wall" }] });
  });

  it("remove_day throws when removing the last remaining day", () => {
    const oneDay = [days()[0]];
    expect(() => applyTripEditIntent(oneDay, { op: "remove_day", day: 1 })).toThrow(TripEditIntentError);
  });

  it("remove_day throws for a day that doesn't exist", () => {
    expect(() => applyTripEditIntent(days(), { op: "remove_day", day: 99 })).toThrow(TripEditIntentError);
  });

  it("replace_day_blocks overwrites only the targeted day's blocks", () => {
    const next = applyTripEditIntent(days(), {
      op: "replace_day_blocks",
      day: 2,
      blocks: [{ time: "Evening", title: "Night market", description: "" }],
    });
    expect(next[1].blocks).toEqual([{ time: "Evening", title: "Night market", description: "" }]);
    expect(next[0]).toEqual(days()[0]);
  });

  it("replace_day_blocks throws for a day that doesn't exist", () => {
    expect(() => applyTripEditIntent(days(), { op: "replace_day_blocks", day: 99, blocks: [] })).toThrow(
      TripEditIntentError,
    );
  });
});
