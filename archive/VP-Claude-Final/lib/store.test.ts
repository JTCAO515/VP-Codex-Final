import { describe, it, expect, beforeEach } from "vitest";
import { useTripStore } from "./store";

function dayData(city: string) {
  return {
    city,
    activities: [],
    food: [],
    hotel: "Hotel",
    transport: "Transport",
    pace: "moderate" as const,
    budgetNote: "n/a",
  };
}

describe("useTripStore.applyInstructions", () => {
  beforeEach(() => {
    useTripStore.getState().reset();
  });

  it("inserts a new day card on upsert when none exists", () => {
    useTripStore.getState().applyInstructions({
      days: [{ day: 1, action: "upsert", data: dayData("Beijing") }],
    });

    const days = useTripStore.getState().days;
    expect(days).toHaveLength(1);
    expect(days[0]).toMatchObject({ day: 1, city: "Beijing" });
  });

  it("replaces an existing day card on upsert with the same day number", () => {
    const store = useTripStore.getState();
    store.applyInstructions({ days: [{ day: 1, action: "upsert", data: dayData("Beijing") }] });
    store.applyInstructions({
      days: [
        {
          day: 1,
          action: "upsert",
          data: { ...dayData("Beijing"), hotel: "Beijing hotel (near metro)", pace: "relaxed" },
        },
      ],
    });

    const days = useTripStore.getState().days;
    expect(days).toHaveLength(1);
    expect(days[0].pace).toBe("relaxed");
    expect(days[0].hotel).toContain("near metro");
  });

  it("removes a day card on delete", () => {
    const store = useTripStore.getState();
    store.applyInstructions({ days: [{ day: 1, action: "upsert", data: dayData("Beijing") }] });
    store.applyInstructions({ days: [{ day: 1, action: "delete" }] });

    expect(useTripStore.getState().days).toHaveLength(0);
  });

  it("keeps day cards sorted by day number regardless of insertion order", () => {
    const store = useTripStore.getState();
    store.applyInstructions({
      days: [
        { day: 3, action: "upsert", data: dayData("Hangzhou") },
        { day: 1, action: "upsert", data: dayData("Beijing") },
        { day: 2, action: "upsert", data: dayData("Shanghai") },
      ],
    });

    expect(useTripStore.getState().days.map((d) => d.day)).toEqual([1, 2, 3]);
  });

  it("upserts and deletes rail items by id", () => {
    const store = useTripStore.getState();
    store.applyInstructions({
      rails: [
        {
          id: "visa-check",
          action: "upsert",
          data: {
            category: "visa",
            title: "Visa check",
            detail: "Confirm visa-free transit eligibility.",
            severity: "warning",
          },
        },
      ],
    });
    expect(useTripStore.getState().rails).toHaveLength(1);

    store.applyInstructions({ rails: [{ id: "visa-check", action: "delete" }] });
    expect(useTripStore.getState().rails).toHaveLength(0);
  });

  it("merges partial summary updates without clobbering untouched fields", () => {
    const store = useTripStore.getState();
    store.applyInstructions({ summary: { route: ["Beijing", "Shanghai"], days: 5 } });
    store.applyInstructions({ summary: { travelers: 2 } });

    expect(useTripStore.getState().summary).toEqual({
      route: ["Beijing", "Shanghai"],
      startDate: null,
      endDate: null,
      travelers: 2,
      days: 5,
    });
  });

  it("reset clears messages, days, rails, and summary back to defaults", () => {
    const store = useTripStore.getState();
    store.addMessage({ id: "1", role: "user", content: "hi" });
    store.applyInstructions({
      days: [{ day: 1, action: "upsert", data: dayData("Beijing") }],
      summary: { route: ["Beijing"], days: 1 },
    });

    store.reset();

    const state = useTripStore.getState();
    expect(state.messages).toHaveLength(0);
    expect(state.days).toHaveLength(0);
    expect(state.summary).toEqual({ route: [], startDate: null, endDate: null, travelers: 1, days: 0 });
  });
});
