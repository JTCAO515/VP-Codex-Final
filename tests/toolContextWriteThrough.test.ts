import { describe, expect, it } from "vitest";
import { applyToolContextToPatch, buildExploreRefs } from "@/lib/ai/toolContextWriteThrough";
import type { ButlerToolContext } from "@/lib/ai/toolContext";
import type { AssistantResponse, CanvasPatch } from "@/lib/types/trip";

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

describe("buildExploreRefs", () => {
  const toolContext: ButlerToolContext = {
    source: "amap",
    cityId: "beijing",
    category: "food",
    pois: [
      {
        id: "B0L015FUKT",
        name: "老盛兴汤包馆",
        type: "Restaurant",
        rating: "4.6",
        pricePerPerson: "45",
        sourceLabel: "Amap",
      },
      {
        id: "OTHERPOI",
        name: "Never Mentioned Restaurant",
        type: "Restaurant",
        sourceLabel: "Amap",
      },
    ],
  };

  it("only includes real toolContext POIs actually named in the answer text", () => {
    const assistantResponse: AssistantResponse = {
      headline: "Try 老盛兴汤包馆 tonight",
      body: "It's a short walk from your hotel and known for soup dumplings.",
      highlights: ["Great value at ¥45/person"],
      nextStep: "Add it to your day 2 dinner slot.",
    };

    const refs = buildExploreRefs(assistantResponse, toolContext);

    expect(refs).toHaveLength(1);
    expect(refs[0]).toMatchObject({
      amapPoiId: "B0L015FUKT",
      name: "老盛兴汤包馆",
      cityId: "beijing",
      category: "food",
      rating: 4.6,
      pricePerPerson: 45,
    });
  });

  it("returns nothing when no real POI is named — never invents a placeholder ref", () => {
    const assistantResponse: AssistantResponse = {
      headline: "Here are some dinner ideas",
      body: "There are many good options near the Bund.",
      highlights: [],
      nextStep: "Tell me your budget.",
    };

    expect(buildExploreRefs(assistantResponse, toolContext)).toEqual([]);
  });

  it("returns nothing when toolContext is absent, even if the text happens to name a place", () => {
    const assistantResponse: AssistantResponse = {
      headline: "Try 老盛兴汤包馆 tonight",
      body: "",
      highlights: [],
      nextStep: "",
    };

    expect(buildExploreRefs(assistantResponse, undefined)).toEqual([]);
  });

  it("plumbs exploreRefs through applyToolContextToPatch onto assistantResponse", () => {
    const patch: CanvasPatch = {
      intent: "adjust_trip",
      assistantMessage: "ok",
      reason: "test",
      assistantResponse: {
        headline: "Try 老盛兴汤包馆 tonight",
        body: "",
        highlights: [],
        nextStep: "",
      },
    };

    const enriched = applyToolContextToPatch(patch, toolContext);

    expect(enriched.assistantResponse?.exploreRefs).toHaveLength(1);
    expect(enriched.assistantResponse?.exploreRefs?.[0].amapPoiId).toBe("B0L015FUKT");
  });
});
