import { describe, expect, it } from "vitest";
import { parseButlerPatch } from "@/lib/ai/butlerPrompt";

describe("parseButlerPatch days normalization", () => {
  it("keeps a well-formed flat blocks day intact", () => {
    const content = JSON.stringify({
      intent: "create_trip",
      assistantMessage: "Here is day 1.",
      reason: "First day plan.",
      suggestions: ["A?", "B?"],
      days: [
        {
          day: 1,
          city: "Beijing",
          pace: "Balanced",
          blocks: [{ time: "Morning", title: "Tiananmen Square", description: "Arrive early." }],
          food: ["Peking duck"],
          stay: "City center",
          transport: "Subway",
          note: "",
        },
      ],
    });

    const { patch } = parseButlerPatch(content, ["fallback1", "fallback2"]);

    expect(patch.days).toHaveLength(1);
    expect(patch.days?.[0].blocks).toHaveLength(1);
    expect(patch.days?.[0].blocks[0].title).toBe("Tiananmen Square");
  });

  it("recovers blocks from a legacy morning/afternoon/evening/activities shape instead of dropping the day", () => {
    const content = JSON.stringify({
      intent: "create_trip",
      assistantMessage: "Here is day 1.",
      reason: "First day plan.",
      suggestions: ["A?", "B?"],
      days: [
        {
          dayNumber: 1,
          date: "2025-04-01",
          morning: {
            title: "Imperial Morning",
            activities: [
              {
                time: "08:00",
                poi: { name: "Tiananmen Square", address: "Dongcheng District", sourceLabel: "Common knowledge", note: "Arrive early." },
                duration: "1 hour",
              },
            ],
          },
          afternoon: {
            title: "Park & Lunch",
            activities: [
              { time: "12:30", poi: { name: "Jingshan Park", address: "Xicheng District", note: "Great view." }, duration: "1 hour" },
            ],
          },
          evening: { title: "Hutong", activities: [] },
        },
      ],
    });

    const { patch } = parseButlerPatch(content, ["fallback1", "fallback2"]);

    expect(patch.days).toHaveLength(1);
    const day = patch.days?.[0];
    expect(day?.blocks).toHaveLength(2);
    expect(day?.blocks[0]).toMatchObject({ time: "Morning", title: "Tiananmen Square" });
    expect(day?.blocks[1]).toMatchObject({ time: "Afternoon", title: "Jingshan Park" });

    // Provider-specific scratch fields must not leak past the normalize boundary.
    expect(day).not.toHaveProperty("dayNumber");
    expect(day).not.toHaveProperty("morning");
    expect(day).not.toHaveProperty("afternoon");
    expect(day).not.toHaveProperty("evening");
  });

  it("prefers real blocks over legacy periods when both are present", () => {
    const content = JSON.stringify({
      intent: "create_trip",
      assistantMessage: "Here is day 1.",
      reason: "First day plan.",
      suggestions: ["A?", "B?"],
      days: [
        {
          day: 1,
          city: "Beijing",
          pace: "Balanced",
          blocks: [{ time: "Morning", title: "Real block", description: "" }],
          morning: { activities: [{ poi: { name: "Should be ignored" } }] },
          food: [],
          stay: "",
          transport: "",
          note: "",
        },
      ],
    });

    const { patch } = parseButlerPatch(content, ["fallback1", "fallback2"]);

    expect(patch.days?.[0].blocks).toHaveLength(1);
    expect(patch.days?.[0].blocks[0].title).toBe("Real block");
  });
});

describe("parseButlerPatch adjust_trip edit-intent path", () => {
  const currentDays = [
    {
      day: 1,
      city: "Beijing",
      pace: "Balanced" as const,
      blocks: [{ time: "Morning" as const, title: "Tiananmen Square", description: "Arrive early." }],
      food: [],
      stay: "",
      transport: "",
      note: "",
    },
  ];

  it("executes add_block instead of expecting a full days array", () => {
    const content = JSON.stringify({
      intent: "adjust_trip",
      assistantMessage: "Added the Summer Palace to day 1.",
      reason: "User asked to add a stop.",
      suggestions: ["A?", "B?"],
      editIntent: {
        op: "add_block",
        day: 1,
        block: { time: "Afternoon", title: "Summer Palace", description: "A calm afternoon stop." },
      },
    });

    const { patch } = parseButlerPatch(content, ["fallback1", "fallback2"], "adjust_trip", currentDays);

    expect(patch.days).toHaveLength(1);
    expect(patch.days?.[0].blocks).toHaveLength(2);
    expect(patch.days?.[0].blocks[1].title).toBe("Summer Palace");
  });

  it("executes remove_block", () => {
    const content = JSON.stringify({
      intent: "adjust_trip",
      assistantMessage: "Removed Tiananmen Square from day 1.",
      reason: "User asked to remove a stop.",
      suggestions: ["A?", "B?"],
      editIntent: { op: "remove_block", day: 1, blockIndex: 0 },
    });

    const { patch } = parseButlerPatch(content, ["fallback1", "fallback2"], "adjust_trip", currentDays);

    expect(patch.days?.[0].blocks).toHaveLength(0);
  });

  it("executes update_block", () => {
    const content = JSON.stringify({
      intent: "adjust_trip",
      assistantMessage: "Moved the visit to the afternoon.",
      reason: "User asked to shift the time.",
      suggestions: ["A?", "B?"],
      editIntent: { op: "update_block", day: 1, blockIndex: 0, patch: { time: "Afternoon" } },
    });

    const { patch } = parseButlerPatch(content, ["fallback1", "fallback2"], "adjust_trip", currentDays);

    expect(patch.days?.[0].blocks[0]).toMatchObject({ time: "Afternoon", title: "Tiananmen Square" });
  });

  it("treats a missing editIntent as 'no itinerary change', same as the full-array path treats a missing days field", () => {
    const content = JSON.stringify({
      intent: "adjust_trip",
      assistantMessage: "Sure, here's an answer to your question.",
      reason: "This reply didn't change the itinerary.",
      suggestions: ["A?", "B?"],
    });

    const { patch } = parseButlerPatch(content, ["fallback1", "fallback2"], "adjust_trip", currentDays);

    expect(patch.days).toBeUndefined();
  });

  it("throws when editIntent is present but malformed, rather than silently dropping content", () => {
    const content = JSON.stringify({
      intent: "adjust_trip",
      assistantMessage: "Something changed.",
      reason: "editIntent is garbage.",
      suggestions: ["A?", "B?"],
      editIntent: { op: "add_block", day: 1 },
    });

    expect(() => parseButlerPatch(content, ["fallback1", "fallback2"], "adjust_trip", currentDays)).toThrow();
  });

  it("throws on an out-of-range blockIndex instead of applying a bad edit", () => {
    const content = JSON.stringify({
      intent: "adjust_trip",
      assistantMessage: "Removed a stop.",
      reason: "Index does not exist.",
      suggestions: ["A?", "B?"],
      editIntent: { op: "remove_block", day: 1, blockIndex: 5 },
    });

    expect(() => parseButlerPatch(content, ["fallback1", "fallback2"], "adjust_trip", currentDays)).toThrow();
  });

  it("does not use the edit-intent path for create_trip even with the same days shown", () => {
    const content = JSON.stringify({
      intent: "create_trip",
      assistantMessage: "Here is a fresh 1-day plan.",
      reason: "User asked to plan a new trip.",
      suggestions: ["A?", "B?"],
      days: [
        {
          day: 1,
          city: "Shanghai",
          pace: "Balanced",
          blocks: [{ time: "Morning", title: "The Bund", description: "Start with the skyline." }],
          food: [],
          stay: "",
          transport: "",
          note: "",
        },
      ],
    });

    // Same "adjust_trip" ButlerIntent classification passed through, but the
    // model's own CanvasPatch.intent is create_trip — the full-array path
    // must still run since usesEditIntentPath only gates the prompt/parse
    // choice, not this test's assertion that create_trip content survives.
    const { patch } = parseButlerPatch(content, ["fallback1", "fallback2"], "create_trip", currentDays);

    expect(patch.days?.[0].city).toBe("Shanghai");
  });
});

describe("parseButlerPatch staged generation (create_trip skeleton + completion)", () => {
  it("tags a create_trip response as generationStage 'skeleton' and force-clears blocks even if the model ignored the instruction", () => {
    const content = JSON.stringify({
      intent: "create_trip",
      assistantMessage: "Here is your 2-day Beijing trip.",
      reason: "First pass.",
      suggestions: ["A?", "B?"],
      tripSummary: { title: "Beijing", durationDays: 2, destinations: ["Beijing"], confidence: "Draft" },
      days: [
        {
          day: 1,
          city: "Beijing",
          pace: "Balanced",
          blocks: [{ time: "Morning", title: "Should be stripped", description: "" }],
          food: ["Peking duck"],
          stay: "City center",
          transport: "Subway",
          note: "",
        },
      ],
    });

    const { patch } = parseButlerPatch(content, ["fallback1", "fallback2"], "create_trip");

    expect(patch.generationStage).toBe("skeleton");
    expect(patch.days?.[0].blocks).toEqual([]);
    expect(patch.days?.[0].city).toBe("Beijing");
  });

  it("does not set generationStage for adjust_trip or add_alerts", () => {
    const adjustContent = JSON.stringify({
      intent: "adjust_trip",
      assistantMessage: "Sure, no itinerary change.",
      reason: "Plain answer.",
      suggestions: ["A?", "B?"],
    });
    const { patch: adjustPatch } = parseButlerPatch(adjustContent, ["fallback1", "fallback2"], "adjust_trip", []);
    expect(adjustPatch.generationStage).toBeUndefined();

    const alertsContent = JSON.stringify({
      intent: "add_alerts",
      assistantMessage: "Added a visa reminder.",
      reason: "Alert only.",
      suggestions: ["A?", "B?"],
    });
    const { patch: alertsPatch } = parseButlerPatch(alertsContent, ["fallback1", "fallback2"], "unclear");
    expect(alertsPatch.generationStage).toBeUndefined();
  });

  const skeletonDays = [
    {
      day: 1,
      city: "Beijing",
      pace: "Balanced" as const,
      blocks: [],
      food: ["Peking duck"],
      stay: "City center",
      transport: "Subway",
      note: "",
    },
    {
      day: 2,
      city: "Suzhou",
      pace: "Light" as const,
      blocks: [],
      food: [],
      stay: "",
      transport: "",
      note: "",
    },
  ];

  it("accepts a completion response that fills in blocks without touching the skeleton's day/city/pace", () => {
    const content = JSON.stringify({
      intent: "create_trip",
      assistantMessage: "Details are in.",
      reason: "Round 2.",
      suggestions: ["A?", "B?"],
      days: [
        { ...skeletonDays[0], blocks: [{ time: "Morning", title: "Tiananmen Square", description: "Arrive early." }] },
        { ...skeletonDays[1], blocks: [{ time: "Morning", title: "Humble Administrator's Garden", description: "" }] },
      ],
    });

    const { patch } = parseButlerPatch(content, ["fallback1", "fallback2"], "create_trip", skeletonDays, "skeletonCompletion");

    expect(patch.generationStage).toBe("complete");
    expect(patch.days?.[0].blocks).toHaveLength(1);
    expect(patch.days?.[1].city).toBe("Suzhou");
  });

  it("throws when a completion response changes the day count", () => {
    const content = JSON.stringify({
      intent: "create_trip",
      assistantMessage: "Details are in.",
      reason: "Round 2.",
      suggestions: ["A?", "B?"],
      days: [{ ...skeletonDays[0], blocks: [{ time: "Morning", title: "X", description: "" }] }],
    });

    expect(() =>
      parseButlerPatch(content, ["fallback1", "fallback2"], "create_trip", skeletonDays, "skeletonCompletion"),
    ).toThrow();
  });

  it("throws when a completion response changes a day's city", () => {
    const content = JSON.stringify({
      intent: "create_trip",
      assistantMessage: "Details are in.",
      reason: "Round 2.",
      suggestions: ["A?", "B?"],
      days: [
        { ...skeletonDays[0], city: "Shanghai", blocks: [{ time: "Morning", title: "X", description: "" }] },
        { ...skeletonDays[1], blocks: [{ time: "Morning", title: "Y", description: "" }] },
      ],
    });

    expect(() =>
      parseButlerPatch(content, ["fallback1", "fallback2"], "create_trip", skeletonDays, "skeletonCompletion"),
    ).toThrow();
  });

  it("throws when a completion response changes a day's pace", () => {
    const content = JSON.stringify({
      intent: "create_trip",
      assistantMessage: "Details are in.",
      reason: "Round 2.",
      suggestions: ["A?", "B?"],
      days: [
        { ...skeletonDays[0], pace: "Packed", blocks: [{ time: "Morning", title: "X", description: "" }] },
        { ...skeletonDays[1], blocks: [{ time: "Morning", title: "Y", description: "" }] },
      ],
    });

    expect(() =>
      parseButlerPatch(content, ["fallback1", "fallback2"], "create_trip", skeletonDays, "skeletonCompletion"),
    ).toThrow();
  });
});
