import { describe, expect, it } from "vitest";
import { applyCanvasPatch, computeAffectedDays } from "@/lib/canvas/applyCanvasPatch";
import { initialTripState } from "@/lib/mock-ai/mockButler";
import type { CanvasPatch, TripDay } from "@/lib/types/trip";

function day(overrides: Partial<TripDay> & { day: number }): TripDay {
  return {
    city: "Beijing",
    pace: "Balanced",
    blocks: [{ time: "Morning", title: "Placeholder", description: "" }],
    food: [],
    stay: "",
    transport: "",
    note: "",
    ...overrides,
  };
}

describe("applyCanvasPatch", () => {
  it("merges summary fields and replaces supplied days", () => {
    const patch: CanvasPatch = {
      intent: "adjust_trip",
      assistantMessage: "Updated.",
      reason: "Changed pace.",
      tripSummary: { pace: "Relaxed", destinations: ["Beijing"] },
      days: [
        {
          day: 1,
          city: "Beijing",
          pace: "Relaxed",
          blocks: [{ time: "Morning", title: "Temple of Heaven", description: "Start gently." }],
          food: ["Noodles"],
          stay: "Dongcheng",
          transport: "Metro",
          note: "Light arrival day.",
          status: "revised",
        },
      ],
    };

    const next = applyCanvasPatch(initialTripState, patch);

    expect(next.summary.pace).toBe("Relaxed");
    expect(next.summary.destinations).toEqual(["Beijing"]);
    expect(next.days).toHaveLength(1);
    expect(next.lastUpdatedReason).toBe("Changed pace.");
  });

  it("deduplicates alerts by type and title", () => {
    const alert = {
      type: "payment" as const,
      priority: "high" as const,
      title: "Set up Alipay before arrival",
      body: "Prepare payment before taxis and meals.",
      action: "Review payment setup",
    };
    const next = applyCanvasPatch(
      { ...initialTripState, alerts: [alert] },
      { intent: "add_alerts", assistantMessage: "Added.", reason: "Payment.", butlerAlerts: [alert] },
    );

    expect(next.alerts).toHaveLength(1);
  });
});

describe("computeAffectedDays", () => {
  it("returns an empty array when the patch didn't touch days", () => {
    expect(computeAffectedDays([day({ day: 1 })], undefined)).toEqual([]);
  });

  it("flags a day whose content changed", () => {
    const before = [day({ day: 1, note: "Original" }), day({ day: 2, note: "Untouched" })];
    const after = [day({ day: 1, note: "Revised" }), day({ day: 2, note: "Untouched" })];

    expect(computeAffectedDays(before, after)).toEqual([1]);
  });

  it("flags a newly added day", () => {
    const before = [day({ day: 1 })];
    const after = [day({ day: 1 }), day({ day: 2 })];

    expect(computeAffectedDays(before, after)).toEqual([2]);
  });

  it("flags a removed day", () => {
    const before = [day({ day: 1 }), day({ day: 2 })];
    const after = [day({ day: 1 })];

    expect(computeAffectedDays(before, after)).toEqual([2]);
  });

  it("returns nothing when every day is identical", () => {
    const before = [day({ day: 1 }), day({ day: 2 })];
    const after = [day({ day: 1 }), day({ day: 2 })];

    expect(computeAffectedDays(before, after)).toEqual([]);
  });
});
