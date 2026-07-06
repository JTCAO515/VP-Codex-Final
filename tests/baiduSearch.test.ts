import { describe, expect, it, vi } from "vitest";
import type { FetchLike } from "@/lib/ai/providers/types";
import { BAIDU_EXPERIENCE_TYPE_MAP, baiduPoiRichMeta, searchBaiduPoisPaged } from "@/lib/explore/baiduSearch";

const ENV = { BAIDU_MAP_AK: "test-ak" };

function baiduOk(results: unknown[], total = results.length) {
  return {
    ok: true,
    json: async () => ({ status: 0, total, results }),
  } as Response;
}

describe("BAIDU_EXPERIENCE_TYPE_MAP", () => {
  it("only covers the experience categories planned for Baidu Phase 2", () => {
    expect(BAIDU_EXPERIENCE_TYPE_MAP["experiences.massage"]).toBe("按摩");
    expect(BAIDU_EXPERIENCE_TYPE_MAP["experiences.bath"]).toBe("足疗");
    expect(BAIDU_EXPERIENCE_TYPE_MAP["experiences.spa"]).toBe("SPA");
    expect(BAIDU_EXPERIENCE_TYPE_MAP["experiences.teahouse"]).toBe("茶馆");
    expect(BAIDU_EXPERIENCE_TYPE_MAP.food).toBeUndefined();
  });
});

describe("searchBaiduPoisPaged", () => {
  it("hits Place API v2 with ak auth and 0-based page_num", async () => {
    const fetchImpl = vi.fn<FetchLike>(async () => baiduOk([{ uid: "u1", name: "川式足道" }], 45));
    const result = await searchBaiduPoisPaged({
      cityId: "chengdu", type: "experiences.bath", keyword: "春熙路", page: 2, env: ENV, fetchImpl,
    });

    const url = new URL(fetchImpl.mock.calls[0][0] as string);
    expect(url.href.startsWith("https://api.map.baidu.com/place/v2/search")).toBe(true);
    expect(url.searchParams.get("ak")).toBe("test-ak");
    expect(url.searchParams.get("query")).toBe("足疗 春熙路");
    expect(url.searchParams.get("region")).toBe("成都");
    expect(url.searchParams.get("city_limit")).toBe("true");
    expect(url.searchParams.get("scope")).toBe("2");
    expect(url.searchParams.get("page_size")).toBe("20");
    expect(url.searchParams.get("page_num")).toBe("1");
    expect(result.hasMore).toBe(true);
  });

  it("returns empty without an API key or for non-experience categories", async () => {
    const fetchImpl = vi.fn<FetchLike>(async () => baiduOk([{ uid: "u1", name: "x" }]));
    expect((await searchBaiduPoisPaged({ cityId: "chengdu", type: "experiences.spa", env: {}, fetchImpl })).pois).toEqual([]);
    expect((await searchBaiduPoisPaged({ cityId: "chengdu", type: "food", env: ENV, fetchImpl })).pois).toEqual([]);
  });

  it("maps rich meta fields used by Explore cards", () => {
    expect(
      baiduPoiRichMeta({
        uid: "u1",
        name: "川式足道",
        telephone: "028-123456",
        area: "锦江区",
        location: { lat: 30.65, lng: 104.08 },
        detail_info: { overall_rating: "4.6", price: "128", image: "https://img.example/a.jpg", shop_hours: "10:00-22:00" },
      }),
    ).toEqual({
      rating: "4.6",
      pricePerPerson: "128",
      priceLevel: "¥¥",
      tel: "028-123456",
      openHours: "10:00-22:00",
      photoUrl: "https://img.example/a.jpg",
      businessArea: "锦江区",
      sourceLabel: "Baidu",
      location: { lat: 30.65, lng: 104.08 },
    });
  });
});
