import { describe, expect, it } from "vitest";
import { buildFactualToolResponse } from "@/lib/tools/factualToolCards";
import { initialTripState } from "@/lib/mock-ai/mockButler";

describe("buildFactualToolResponse", () => {
  it("builds an inline payment tool card from static Tools knowledge", async () => {
    const result = await buildFactualToolResponse({
      message: "How do I pay with Alipay or WeChat Pay in China?",
      currentTrip: initialTripState,
      intent: "ask_factual",
    });

    expect(result?.categoryId).toBe("payment-setup");
    expect(result?.patch.assistantResponse?.toolCards?.[0]).toMatchObject({
      categoryId: "payment-setup",
      title: "Payment setup",
      href: "/tools?category=payment-setup",
      sourceLabel: "VisePanda Tools",
    });
    expect(result?.patch.days).toBeUndefined();
  });

  it("does not answer non-factual itinerary requests", async () => {
    const result = await buildFactualToolResponse({
      message: "Make Day 2 lighter",
      currentTrip: initialTripState,
      intent: "adjust_trip",
    });

    expect(result).toBeUndefined();
  });
});
