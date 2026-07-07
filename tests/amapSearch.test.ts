import { describe, expect, it, vi } from "vitest";
import {
  AMAP_TYPE_MAP,
  isValidLngLat,
  searchAmapPois,
  searchAmapPoisPaged,
} from "@/lib/explore/amapSearch";

const ENV = { AMAP_API_KEY: "test-key" };

function amapOk(pois: unknown[], count = pois.length) {
  return {
    ok: true,
    json: async () => ({ status: "1", count: String(count), pois }),
  } as Response;
}

function poi(name: string) {
  return { id: `id-${name}`, name, type: "餐饮服务" };
}

describe("AMAP_TYPE_MAP (Issue #46)", () => {
  it("keeps the legacy top-level keys and adds the two new Dianping categories", () => {
    for (const key of ["food", "attractions", "stays", "hotels", "shopping", "experiences"]) {
      expect(AMAP_TYPE_MAP[key]?.types, key).toBeTruthy();
    }
    // stays is the legacy alias of hotels — must stay in lockstep.
    expect(AMAP_TYPE_MAP.stays.types).toBe(AMAP_TYPE_MAP.hotels.types);
  });

  it("has a config for every subcategory in the redesign spec §2.2", () => {
    const expected = [
      "food.hotpot", "food.sichuan", "food.cantonese", "food.japanese",
      "food.bbq", "food.dessert", "food.fastfood", "food.cafe",
      "attractions.scenic", "attractions.park", "attractions.museum", "attractions.temple",
      "hotels.star", "hotels.economy", "hotels.hostel",
      "shopping.mall", "shopping.specialty", "shopping.supermarket",
      "experiences.massage", "experiences.bath", "experiences.spa",
      "experiences.teahouse", "experiences.ktv",
    ];
    for (const key of expected) {
      expect(AMAP_TYPE_MAP[key]?.types, key).toBeTruthy();
    }
  });
});

describe("isValidLngLat", () => {
  it("accepts a plausible China coordinate and rejects garbage", () => {
    expect(isValidLngLat("104.06,30.57")).toBe(true);
    expect(isValidLngLat("30.57")).toBe(false);
    expect(isValidLngLat("104.06,30.57,9")).toBe(false);
    expect(isValidLngLat("abc,def")).toBe(false);
    expect(isValidLngLat("-73.98,40.75")).toBe(false); // New York — outside China bounds
    expect(isValidLngLat(undefined)).toBe(false);
  });
});

describe("searchAmapPoisPaged", () => {
  it("city mode hits place/text with the city name and reports hasMore from count", async () => {
    const fetchImpl = vi.fn(async () => amapOk([poi("a")], 45));
    const result = await searchAmapPoisPaged({
      cityId: "chengdu", type: "food", env: ENV, fetchImpl, page: 2,
    });

    const url = new URL(fetchImpl.mock.calls[0][0] as string);
    expect(url.pathname).toBe("/v3/place/text");
    expect(url.searchParams.get("city")).toBe("成都");
    expect(url.searchParams.get("types")).toBe("050000");
    expect(url.searchParams.get("page")).toBe("2");
    expect(result.hasMore).toBe(true); // 2*20 < 45
  });

  it("around mode hits place/around with location/radius/sortrule and clamps radius", async () => {
    const fetchImpl = vi.fn(async () => amapOk([poi("a")], 5));
    await searchAmapPoisPaged({
      cityId: "chengdu", type: "food", env: ENV, fetchImpl,
      mode: "around", location: "104.06,30.57", radius: 999999, sort: "distance",
    });

    const url = new URL(fetchImpl.mock.calls[0][0] as string);
    expect(url.pathname).toBe("/v3/place/around");
    expect(url.searchParams.get("location")).toBe("104.06,30.57");
    expect(url.searchParams.get("radius")).toBe("50000"); // clamped to Amap max
    expect(url.searchParams.get("sortrule")).toBe("distance");
    expect(url.searchParams.get("city")).toBe("成都"); // still city-constrained
  });

  it("around mode without a valid location falls back to city text search", async () => {
    const fetchImpl = vi.fn(async () => amapOk([]));
    await searchAmapPoisPaged({
      cityId: "chengdu", type: "food", env: ENV, fetchImpl, mode: "around", location: "junk",
    });
    const url = new URL(fetchImpl.mock.calls[0][0] as string);
    expect(url.pathname).toBe("/v3/place/text");
  });

  it("composes the category keyword with the traveler keyword", async () => {
    const fetchImpl = vi.fn(async () => amapOk([]));
    await searchAmapPoisPaged({
      cityId: "chengdu", type: "food.bbq", keyword: "羊肉串", env: ENV, fetchImpl,
    });
    const url = new URL(fetchImpl.mock.calls[0][0] as string);
    expect(url.searchParams.get("types")).toBe("050000");
    expect(url.searchParams.get("keywords")).toBe("烧烤|羊肉串");
  });

  it("returns empty without an API key or for an unknown city/type", async () => {
    const fetchImpl = vi.fn(async () => amapOk([poi("a")]));
    expect(
      (await searchAmapPoisPaged({ cityId: "chengdu", type: "food", env: {}, fetchImpl })).pois,
    ).toEqual([]);
    expect(
      (await searchAmapPoisPaged({ cityId: "atlantis", type: "food", env: ENV, fetchImpl })).pois,
    ).toEqual([]);
    expect(
      (await searchAmapPoisPaged({ cityId: "chengdu", type: "nope", env: ENV, fetchImpl })).pois,
    ).toEqual([]);
  });

  it("legacy searchAmapPois keeps returning a bare array (toolContext contract)", async () => {
    const fetchImpl = vi.fn(async () => amapOk([poi("a"), poi("b")]));
    const pois = await searchAmapPois({ cityId: "beijing", type: "attractions", env: ENV, fetchImpl });
    expect(pois).toHaveLength(2);
    expect(pois[0].name).toBe("a");
  });
});
