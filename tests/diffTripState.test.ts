import { describe, expect, it } from "vitest";
import { diffTripState } from "@/lib/canvas/diffTripState";
import { initialTripState } from "@/lib/mock-ai/mockButler";
import type { TripState } from "@/lib/types/trip";

describe("diffTripState", () => {
  it("returns an empty array when nothing changed", () => {
    expect(diffTripState(initialTripState, initialTripState)).toEqual([]);
  });

  it("reports an added day", () => {
    const next: TripState = {
      ...initialTripState,
      days: [
        ...initialTripState.days,
        {
          day: 4,
          city: "Suzhou",
          pace: "Balanced",
          blocks: [{ time: "Morning", title: "Humble Administrator's Garden", description: "…" }],
          food: ["Noodles"],
          stay: "Old town",
          transport: "Train",
          note: "",
          status: "new",
        },
      ],
    };

    const entries = diffTripState(initialTripState, next);
    expect(entries).toHaveLength(1);
    expect(entries[0]).toMatchObject({ kind: "added", dayNumber: 4 });
    expect(entries[0].label).toContain("Day 4");
  });

  it("reports a revised day when its content changes", () => {
    const next: TripState = {
      ...initialTripState,
      days: initialTripState.days.map((day) =>
        day.day === 1 ? { ...day, stay: "A different hotel area", status: "revised" as const } : day,
      ),
    };

    const entries = diffTripState(initialTripState, next);
    expect(entries).toHaveLength(1);
    expect(entries[0]).toMatchObject({ kind: "revised", dayNumber: 1 });
  });

  it("reports a removed day", () => {
    const next: TripState = { ...initialTripState, days: initialTripState.days.slice(0, 2) };

    const entries = diffTripState(initialTripState, next);
    expect(entries).toHaveLength(1);
    expect(entries[0]).toMatchObject({ kind: "removed", dayNumber: 3 });
  });

  it("reports a new alert", () => {
    const next: TripState = {
      ...initialTripState,
      alerts: [
        ...initialTripState.alerts,
        { type: "visa", priority: "high", title: "Check entry rules", body: "…", action: "Review visa" },
      ],
    };

    const entries = diffTripState(initialTripState, next);
    expect(entries).toHaveLength(1);
    expect(entries[0].kind).toBe("alert");
    expect(entries[0].label).toContain("Check entry rules");
  });

  it("sorts entries by day number", () => {
    const next: TripState = {
      ...initialTripState,
      days: initialTripState.days.map((day) =>
        day.day === 3 || day.day === 1 ? { ...day, note: "changed", status: "revised" as const } : day,
      ),
    };

    const entries = diffTripState(initialTripState, next);
    expect(entries.map((e) => e.dayNumber)).toEqual([1, 3]);
  });
});
