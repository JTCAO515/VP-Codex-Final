import { describe, expect, it } from "vitest";
import { calculateTripCompleteness } from "@/lib/trips/completeness";
import { initialTripState } from "@/lib/mock-ai/mockButler";
import type { TripState } from "@/lib/types/trip";

describe("calculateTripCompleteness", () => {
  it("scores 5 of 6 dimensions complete for the mock initial trip (payment alert unresolved)", () => {
    const result = calculateTripCompleteness(initialTripState);

    expect(result.score).toBe(83);
    expect(result.checks.find((c) => c.id === "route")?.complete).toBe(true);
    expect(result.checks.find((c) => c.id === "stay")?.complete).toBe(true);
    expect(result.checks.find((c) => c.id === "food")?.complete).toBe(true);
    expect(result.checks.find((c) => c.id === "transport")?.complete).toBe(true);
    expect(result.checks.find((c) => c.id === "payment")?.complete).toBe(false);
    expect(result.checks.find((c) => c.id === "visa")?.complete).toBe(true);
  });

  it("scores 100 once the outstanding payment alert is marked done", () => {
    const trip: TripState = {
      ...initialTripState,
      alerts: initialTripState.alerts.map((alert) => ({ ...alert, done: true })),
    };

    expect(calculateTripCompleteness(trip).score).toBe(100);
  });

  it("scores an empty draft trip low, with only the vacuous payment/visa dimensions complete", () => {
    // No days means route/stay/food/transport are all incomplete; payment and
    // visa have no outstanding alert at all (nothing to resolve yet), which is
    // vacuously complete by design — see calculateTripCompleteness docstring.
    const trip: TripState = {
      summary: { title: "", durationDays: 0, pace: "Balanced", travelerStyle: "", destinations: [], confidence: "Draft" },
      days: [],
      alerts: [],
      lastUpdatedReason: "",
    };

    const result = calculateTripCompleteness(trip);
    expect(result.score).toBe(33);
    expect(result.checks.find((c) => c.id === "route")?.complete).toBe(false);
    expect(result.checks.find((c) => c.id === "stay")?.complete).toBe(false);
    expect(result.checks.find((c) => c.id === "food")?.complete).toBe(false);
    expect(result.checks.find((c) => c.id === "transport")?.complete).toBe(false);
    expect(result.checks.find((c) => c.id === "payment")?.complete).toBe(true);
    expect(result.checks.find((c) => c.id === "visa")?.complete).toBe(true);
  });

  it("treats an unresolved visa alert as an incomplete visa dimension", () => {
    const trip: TripState = {
      ...initialTripState,
      alerts: [
        ...initialTripState.alerts,
        { type: "visa", priority: "high", title: "Check entry rules", body: "…", action: "Review visa" },
      ],
    };

    expect(calculateTripCompleteness(trip).checks.find((c) => c.id === "visa")?.complete).toBe(false);
  });
});
