import { type NextRequest, NextResponse } from "next/server";
import { AMAP_CITY_MAP, amapPoiRichMeta, searchAmapPoisPaged } from "@/lib/explore/amapSearch";
import {
  BAIDU_EXPERIENCE_TYPE_MAP,
  baiduPoiRichMeta,
  searchBaiduPoisPaged,
  type BaiduPoi,
} from "@/lib/explore/baiduSearch";

function normalized(value: string | undefined) {
  return (value ?? "").toLowerCase().replace(/\s+/g, "");
}

function withAmapRating(poi: BaiduPoi, amapRatings: Map<string, string>) {
  const amapRating = amapRatings.get(normalized(poi.name));
  if (!amapRating) return poi;
  return {
    ...poi,
    crossValidation: {
      amapRating,
      baiduRating: baiduPoiRichMeta(poi).rating,
      match: "name",
    },
  };
}

export async function GET(request: NextRequest) {
  if (!process.env.BAIDU_MAP_AK) {
    return NextResponse.json({ ok: false, error: "not_configured" }, { status: 503 });
  }

  const { searchParams } = request.nextUrl;
  const cityId = searchParams.get("cityId") ?? "";
  const type = searchParams.get("type") ?? "";
  const keyword = searchParams.get("keyword") ?? undefined;
  const pageRaw = searchParams.get("page");

  if (!AMAP_CITY_MAP[cityId] || !BAIDU_EXPERIENCE_TYPE_MAP[type]) {
    return NextResponse.json({ ok: false, error: "invalid_params" }, { status: 400 });
  }

  const page = pageRaw ? Number(pageRaw) : 1;
  if (!Number.isFinite(page) || page < 1 || page > 100) {
    return NextResponse.json({ ok: false, error: "invalid_page" }, { status: 400 });
  }

  const [baiduResult, amapResult] = await Promise.all([
    searchBaiduPoisPaged({ cityId, type, keyword, page }),
    process.env.AMAP_API_KEY
      ? searchAmapPoisPaged({ cityId, type, keyword, page }).catch(() => ({ pois: [], hasMore: false }))
      : Promise.resolve({ pois: [], hasMore: false }),
  ]);

  const amapRatings = new Map(
    amapResult.pois
      .map((poi) => [normalized(poi.name), amapPoiRichMeta(poi).rating] as const)
      .filter((entry): entry is readonly [string, string] => Boolean(entry[0] && entry[1])),
  );
  const pois = baiduResult.pois.map((poi) => withAmapRating(poi, amapRatings));

  return NextResponse.json({ ok: true, cityId, type, page, hasMore: baiduResult.hasMore, pois });
}
