import { describe, expect, it } from "vitest";
import { getExploreProvider } from "@/lib/explore";

describe("static explore provider", () => {
  it("lists cities and city-scoped data", async () => {
    const provider = getExploreProvider();

    const cities = await provider.listCities();
    expect(cities.length).toBeGreaterThan(0);
    expect(cities.map((city) => city.id)).toContain("beijing");

    const attractions = await provider.listAttractions("beijing");
    expect(attractions.length).toBeGreaterThan(0);
    expect(attractions.every((attraction) => attraction.cityId === "beijing")).toBe(true);

    const foodSpots = await provider.listFoodSpots("chengdu");
    expect(foodSpots.length).toBeGreaterThan(0);
    expect(foodSpots.every((spot) => spot.cityId === "chengdu")).toBe(true);

    const stays = await provider.listStays("xian");
    expect(stays.length).toBeGreaterThan(0);
    expect(stays.every((stay) => stay.cityId === "xian")).toBe(true);
  });

  it("returns an empty list for an unknown city", async () => {
    const provider = getExploreProvider();
    expect(await provider.listAttractions("atlantis")).toEqual([]);
  });
});
