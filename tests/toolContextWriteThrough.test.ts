import { describe, expect, it } from "vitest";
import { applyToolContextToPatch } from "@/lib/ai/toolContextWriteThrough";
import type { ButlerToolContext } from "@/lib/ai/toolContext";
import type { CanvasPatch } from "@/lib/types/trip";

describe("applyToolContextToPatch", () => {
  it("copies matching live POI execution fields into TripBlocks without replacing existing text", () => {
    const patch: CanvasPatch = {
      intent: "adjust_trip",
      assistantMessage: "Updated with a live POI.",
      reason: "Used Amap context.",
      days: [
        {
          day: 1,
          city: "Shanghai",
          pace: "Balanced",
          blocks: [
            {
              time: "Morning",
              title: "The Bund (外滩)",
              description: "Start on the riverfront.",
            },
          ],
          food: ["Xiaolongbao"],
          stay: "People's Square",
          transport: "Metro",
          note: "Keep it easy.",
        },
      ],
    };
    const toolContext: ButlerToolContext = {
      source: "amap",
      cityId: "shanghai",
      category: "attractions",
      pois: [
        {
          id: "BUND",
          name: "The Bund",
          type: "Scenic area",
          address: "Zhongshan East 1st Road, Huangpu District, Shanghai",
          phone: "021-00000000",
          openHours: "Open public promenade",
          mapUrl: "https://uri.amap.com/marker?position=121.49976,31.23969&name=The%20Bund",
          sourceLabel: "Amap",
          coordinates: { lat: 31.23969, lng: 121.49976 },
          bookingCandidates: [
            {
              id: "amap-ticket-BUND",
              kind: "ticket",
              label: "The Bund",
              provider: "Amap",
              status: "info-only",
              note: "No ticket inventory checked.",
            },
          ],
        },
      ],
    };

    const enriched = applyToolContextToPatch(patch, toolContext);
    const block = enriched.days?.[0]?.blocks[0];

    expect(block?.description).toBe("Start on the riverfront.");
    expect(block?.address).toContain("Zhongshan East");
    expect(block?.phone).toBe("021-00000000");
    expect(block?.openingHours).toBe("Open public promenade");
    expect(block?.coordinates).toEqual({ lat: 31.23969, lng: 121.49976 });
    expect(block?.bookingCandidates?.[0]).toMatchObject({ status: "info-only", kind: "ticket" });
  });
});
