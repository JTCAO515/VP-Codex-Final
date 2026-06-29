import { describe, expect, it } from "vitest";
import { createMockButlerPatch, initialTripState } from "@/lib/mock-ai/mockButler";

describe("createMockButlerPatch", () => {
  it("creates a first-trip itinerary for first-time China prompts", () => {
    const patch = createMockButlerPatch(
      "I am visiting China for the first time for 5 days",
      initialTripState,
    );

    expect(patch.intent).toBe("create_trip");
    expect(patch.tripSummary?.durationDays).toBe(5);
    expect(patch.days?.some((day) => day.city === "Beijing")).toBe(true);
    expect(patch.days?.some((day) => day.city === "Shanghai")).toBe(true);
  });

  it("adds payment alerts when payment is mentioned", () => {
    const patch = createMockButlerPatch("Add payment reminders", initialTripState);

    expect(patch.butlerAlerts?.some((alert) => alert.type === "payment")).toBe(true);
  });

  it("relaxes the trip when the user asks for less tiring plans", () => {
    const patch = createMockButlerPatch("Make this less tiring and slower", initialTripState);

    expect(patch.tripSummary?.pace).toBe("Relaxed");
    expect(patch.reason).toContain("pace");
  });
});
