import { describe, expect, it } from "vitest";
import { applyCanvasPatch } from "@/lib/canvas/applyCanvasPatch";
import { initialTripState } from "@/lib/mock-ai/mockButler";
import type { CanvasPatch } from "@/lib/types/trip";

describe("applyCanvasPatch", () => {
  it("merges summary fields and replaces supplied days", () => {
    const patch: CanvasPatch = {
      intent: "adjust_trip",
      assistantMessage: "Updated.",
      reason: "Changed pace.",
      tripSummary: { pace: "Relaxed", destinations: ["Beijing"] },
      days: [
        {
          day: 1,
          city: "Beijing",
          pace: "Relaxed",
          blocks: [{ time: "Morning", title: "Temple of Heaven", description: "Start gently." }],
          food: ["Noodles"],
          stay: "Dongcheng",
          transport: "Metro",
          note: "Light arrival day.",
          status: "revised",
        },
      ],
    };

    const next = applyCanvasPatch(initialTripState, patch);

    expect(next.summary.pace).toBe("Relaxed");
    expect(next.summary.destinations).toEqual(["Beijing"]);
    expect(next.days).toHaveLength(1);
    expect(next.lastUpdatedReason).toBe("Changed pace.");
  });

  it("deduplicates alerts by type and title", () => {
    const alert = {
      type: "payment" as const,
      priority: "high" as const,
      title: "Set up Alipay before arrival",
      body: "Prepare payment before taxis and meals.",
      action: "Review payment setup",
    };
    const next = applyCanvasPatch(
      { ...initialTripState, alerts: [alert] },
      { intent: "add_alerts", assistantMessage: "Added.", reason: "Payment.", butlerAlerts: [alert] },
    );

    expect(next.alerts).toHaveLength(1);
  });
});
