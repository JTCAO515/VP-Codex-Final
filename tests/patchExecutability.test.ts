import { describe, expect, it } from "vitest";
import { evaluatePatchExecutability } from "@/lib/ai/patchExecutability";
import type { CanvasPatch, TripDay } from "@/lib/types/trip";

function day(overrides: Partial<TripDay> = {}): TripDay {
  return {
    day: 1,
    city: "Beijing",
    pace: "Balanced",
    blocks: [{ time: "Morning", title: "Tiananmen Square", description: "Arrive early." }],
    food: [],
    stay: "",
    transport: "",
    note: "",
    ...overrides,
  };
}

function basePatch(overrides: Partial<CanvasPatch> = {}): CanvasPatch {
  return {
    intent: "create_trip",
    assistantMessage: "Here is your trip.",
    reason: "Test.",
    ...overrides,
  };
}

describe("evaluatePatchExecutability", () => {
  it("scores a complete create_trip patch as 100 with no issues", () => {
    const result = evaluatePatchExecutability(basePatch({ days: [day()] }));
    expect(result).toEqual({ score: 100, issues: [] });
  });

  it("flags a create_trip patch with no days", () => {
    const result = evaluatePatchExecutability(basePatch({ days: undefined }));
    expect(result.score).toBeLessThan(100);
    expect(result.issues).toContain("create_trip patch has no days.");
  });

  it("flags a day missing a city", () => {
    const result = evaluatePatchExecutability(basePatch({ days: [day({ city: "" })] }));
    expect(result.issues).toContain("Day 1 is missing a city.");
  });

  it("flags a day with no blocks when not a skeleton", () => {
    const result = evaluatePatchExecutability(basePatch({ days: [day({ blocks: [] })] }));
    expect(result.issues).toContain("Day 1 has no blocks.");
  });

  it("does not flag empty blocks when generationStage is skeleton", () => {
    const result = evaluatePatchExecutability(
      basePatch({ days: [day({ blocks: [] })], generationStage: "skeleton" }),
    );
    expect(result.issues).toEqual([]);
    expect(result.score).toBe(100);
  });

  it("flags a block missing a title or description", () => {
    const result = evaluatePatchExecutability(
      basePatch({ days: [day({ blocks: [{ time: "Morning", title: "", description: "" }] })] }),
    );
    expect(result.issues).toContain("Day 1 block 0 is missing a title.");
    expect(result.issues).toContain("Day 1 block 0 is missing a description.");
  });

  it("does not penalize a patch with no days when intent isn't create_trip", () => {
    const result = evaluatePatchExecutability(basePatch({ intent: "add_alerts", days: undefined }));
    expect(result).toEqual({ score: 100, issues: [] });
  });

  it("checks days content when present even for non-create_trip intents", () => {
    const result = evaluatePatchExecutability(
      basePatch({ intent: "adjust_trip", days: [day({ city: "" })] }),
    );
    expect(result.issues).toContain("Day 1 is missing a city.");
  });

  it("flags a missing headline/nextStep when assistantResponse is present", () => {
    const result = evaluatePatchExecutability(
      basePatch({
        days: [day()],
        assistantResponse: { headline: "", body: "", highlights: [], nextStep: "" },
      }),
    );
    expect(result.issues).toContain("assistantResponse is missing a headline.");
    expect(result.issues).toContain("assistantResponse is missing a nextStep.");
  });

  it("does not require watchOut on assistantResponse", () => {
    const result = evaluatePatchExecutability(
      basePatch({
        days: [day()],
        assistantResponse: { headline: "Trip ready", body: "...", highlights: [], nextStep: "Review day 1." },
      }),
    );
    expect(result.issues).toEqual([]);
  });
});
