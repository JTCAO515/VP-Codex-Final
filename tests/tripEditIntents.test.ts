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

  it("throws on a not-yet-implemented phase 2/3 op", () => {
    expect(() => parseTripEditIntent({ op: "move_block", day: 1, fromIndex: 0, toIndex: 1 })).toThrow(TripEditIntentError);
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

  it("throws for an op without an executor yet", () => {
    expect(() => applyTripEditIntent(days(), { op: "remove_day", day: 2 })).toThrow(TripEditIntentError);
  });
});
