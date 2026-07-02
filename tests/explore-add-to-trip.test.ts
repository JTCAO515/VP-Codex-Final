import { describe, expect, it } from "vitest";
import { buildExploreAddToTripPayload, applyExplorePoiToPatch } from "@/lib/explore/addToTrip";
import type { ExploreAttraction, ExploreCity } from "@/lib/explore";
import { initialTripState } from "@/lib/mock-ai/mockButler";
import type { CanvasPatch } from "@/lib/types/trip";

describe("Explore add-to-trip payloads", () => {
  const city: ExploreCity = {
    id: "beijing",
    name: "Beijing",
    region: "North China",
    tagline: "Imperial history",
    bestFor: ["History"],
  };

  const attraction: ExploreAttraction = {
    id: "beijing-test-tea-house",
    cityId: "beijing",
    name: "Test Tea House",
    category: "Tea",
    description: "A quiet planning candidate.",
    tel: "010-12345678",
    openHours: "09:00-18:00",
    sourceLabel: "Amap",
    location: { lat: 39.9, lng: 116.4 },
  };

  it("builds a safe info-only POI payload for chat handoff", () => {
    const payload = buildExploreAddToTripPayload(attraction, city, "attraction");

    expect(payload).toMatchObject({
      id: "beijing-test-tea-house",
      name: "Test Tea House",
      cityName: "Beijing",
      phone: "010-12345678",
      openingHours: "09:00-18:00",
      sourceLabel: "Amap",
      coordinates: { lat: 39.9, lng: 116.4 },
    });
    expect(payload.mapUrl).toContain("uri.amap.com/marker");
    expect(payload.bookingCandidates?.[0]).toMatchObject({
      kind: "ticket",
      status: "info-only",
      provider: "Amap",
    });
  });

  it("adds an unmatched Explore POI as a flexible candidate block", () => {
    const payload = buildExploreAddToTripPayload(attraction, city, "attraction");
    const patch: CanvasPatch = {
      intent: "adjust_trip",
      assistantMessage: "I added that candidate.",
      reason: "User added an Explore POI.",
    };

    const nextPatch = applyExplorePoiToPatch(patch, initialTripState, payload);
    const beijingDay = nextPatch.days?.find((day) => day.city === "Beijing");
    const addedBlock = beijingDay?.blocks.find((block) => block.title === "Test Tea House");

    expect(addedBlock).toMatchObject({
      time: "Flexible",
      phone: "010-12345678",
      openingHours: "09:00-18:00",
      sourceLabel: "Amap",
    });
    expect(addedBlock?.bookingCandidates?.[0].status).toBe("info-only");
  });
});
