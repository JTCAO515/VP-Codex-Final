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
