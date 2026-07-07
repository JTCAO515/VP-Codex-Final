import { describe, it, expect } from "vitest";
import { generateMockReply } from "./mock-ai";

describe("generateMockReply", () => {
  it("asks a clarifying question when no city, day count, or pace signal is present", () => {
    const result = generateMockReply("Hi there!", []);
    expect(result.instructions).toEqual({});
    expect(result.chatText).toMatch(/cities|days/i);
  });

  it("builds a multi-day, multi-city itinerary with butler rails and a trip summary from a first message", () => {
    const result = generateMockReply(
      "I'm coming from the US, first time in China, 5 days, want to go to Beijing and Shanghai.",
      []
    );

    expect(result.instructions.days).toHaveLength(5);
    expect(result.instructions.days?.[0].data?.city).toBe("Beijing");
    expect(result.instructions.days?.[1].data?.city).toBe("Shanghai");
    expect(result.instructions.days?.[0].data?.activities).toHaveLength(3);

    const railIds = result.instructions.rails?.map((r) => r.id) ?? [];
    expect(railIds).toContain("visa-check");
    expect(railIds).toContain("payment-setup");
    expect(railIds).toContain("intercity-transport");

    expect(result.instructions.summary).toEqual({ route: ["Beijing", "Shanghai"], days: 5 });
  });

  it("lightens the pace and adjusts hotel notes when the user asks for less tiring days", () => {
    const existingDays = [
      {
        day: 1,
        city: "Beijing",
        activities: [
          { period: "morning" as const, title: "Forbidden City", imageHint: "Forbidden City" },
          { period: "afternoon" as const, title: "Great Wall (Mutianyu)", imageHint: "Great Wall (Mutianyu)" },
          { period: "evening" as const, title: "Temple of Heaven", imageHint: "Temple of Heaven" },
        ],
        food: ["Peking duck"],
        hotel: "Beijing city-center hotel",
        transport: "Airport transfer",
        pace: "moderate" as const,
        budgetNote: "~$80-120/day estimated",
      },
    ];

    const result = generateMockReply(
      "I don't want to be too tired, hotels should be convenient.",
      existingDays
    );

    expect(result.instructions.days?.[0].data?.pace).toBe("relaxed");
    expect(result.instructions.days?.[0].data?.activities.length).toBeLessThan(3);
    expect(result.instructions.days?.[0].data?.hotel).toContain("metro");
  });
});
