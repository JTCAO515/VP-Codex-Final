import { describe, expect, it, vi } from "vitest";
import { curatedPoiIndex, listCuratedPois } from "@/lib/explore/curatedPois";

const ENV = {
  NEXT_PUBLIC_SUPABASE_URL: "https://example.supabase.co",
  NEXT_PUBLIC_SUPABASE_ANON_KEY: "anon-key",
};

function row(overrides: Partial<Record<string, unknown>> = {}) {
  return {
    city_id: "chengdu",
    category: "food",
    amap_poi_id: "B0FFGM6CG9",
    name: "陈麻婆豆腐",
    name_en: "Chen Mapo Tofu",
    editorial_summary: "The birthplace of mapo tofu.",
    tags: ["spicy", "time-honored"],
    list_badges: ["black-pearl-2026"],
    photo_url: null,
    rank: 1,
    source: "official_list",
    source_url: "https://example.com/list",
    ...overrides,
  };
}

describe("listCuratedPois", () => {
  it("queries PostgREST with city/category filters and rank ordering", async () => {
    const fetchImpl = vi.fn(async () => ({ ok: true, json: async () => [row()] }) as Response);
    const rows = await listCuratedPois({ cityId: "chengdu", category: "food", env: ENV, fetchImpl });

    const url = new URL(fetchImpl.mock.calls[0][0] as string);
    expect(url.origin + url.pathname).toBe("https://example.supabase.co/rest/v1/curated_pois");
    expect(url.searchParams.get("city_id")).toBe("eq.chengdu");
    expect(url.searchParams.get("category")).toBe("eq.food");
    expect(url.searchParams.get("order")).toBe("rank.asc");
    expect(rows).toHaveLength(1);
    expect(rows[0].name_en).toBe("Chen Mapo Tofu");
  });

  it("omits the category filter when not provided", async () => {
    const fetchImpl = vi.fn(async () => ({ ok: true, json: async () => [] }) as Response);
    await listCuratedPois({ cityId: "chengdu", env: ENV, fetchImpl });
    const url = new URL(fetchImpl.mock.calls[0][0] as string);
    expect(url.searchParams.has("category")).toBe(false);
  });

  it("returns empty when Supabase isn't configured or the request fails", async () => {
    const fetchImpl = vi.fn(async () => ({ ok: true, json: async () => [row()] }) as Response);
    expect(await listCuratedPois({ cityId: "chengdu", env: {}, fetchImpl })).toEqual([]);

    const failing = vi.fn(async () => ({ ok: false, json: async () => [] }) as Response);
    expect(await listCuratedPois({ cityId: "chengdu", env: ENV, fetchImpl: failing })).toEqual([]);
  });
});

describe("curatedPoiIndex", () => {
  it("indexes rows by amap_poi_id for O(1) merge lookups", async () => {
    const fetchImpl = vi.fn(async () => ({ ok: true, json: async () => [row(), row({ amap_poi_id: "OTHER", name: "别的店" })] }) as Response);
    const index = await curatedPoiIndex({ cityId: "chengdu", env: ENV, fetchImpl });
    expect(index.size).toBe(2);
    expect(index.get("B0FFGM6CG9")?.name).toBe("陈麻婆豆腐");
    expect(index.has("nonexistent")).toBe(false);
  });
});
