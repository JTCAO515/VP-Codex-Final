import { describe, expect, it } from "vitest";
import { DAY_QUICK_ACTIONS, buildQuickActionMessage } from "@/lib/canvas/quickActions";
import { initialTripState } from "@/lib/mock-ai/mockButler";

const day = initialTripState.days[1]; // day 2, Shanghai

describe("buildQuickActionMessage", () => {
  it("always includes the day number and city so the model never has to guess", () => {
    for (const action of DAY_QUICK_ACTIONS) {
      const message = buildQuickActionMessage(action.kind, day);
      expect(message).toContain(`Day ${day.day}`);
      expect(message).toContain(day.city);
    }
  });

  it("lighten produces a message that routes to the mock butler's relaxed-pace fallback", () => {
    const message = buildQuickActionMessage("lighten", day).toLowerCase();
    expect(message).toMatch(/slow|relaxed|less tiring/);
  });

  it("add_food produces a message that routes to the mock butler's food fallback", () => {
    const message = buildQuickActionMessage("add_food", day).toLowerCase();
    expect(message).toContain("food");
  });
});
