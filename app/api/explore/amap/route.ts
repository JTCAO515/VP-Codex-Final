import { type NextRequest, NextResponse } from "next/server";
import { AMAP_CITY_MAP, AMAP_TYPE_MAP, isValidLngLat, searchAmapPoisPaged } from "@/lib/explore/amapSearch";
import { curatedPoiIndex } from "@/lib/explore/curatedPois";

// Dianping-style Explore data layer (Issue #46). Backward compatible: the
// pre-redesign call shape (cityId + type + keyword) behaves exactly as before;
// mode/location/radius/sort/page are additive. Response gains `hasMore`.
export async function GET(request: NextRequest) {
  const apiKey = process.env.AMAP_API_KEY;
  if (!apiKey) {
    return NextResponse.json({ ok: false, error: "not_configured" }, { status: 503 });
  }

  const { searchParams } = request.nextUrl;
  const cityId = searchParams.get("cityId") ?? "";
  const type = searchParams.get("type") ?? "";
  const keyword = searchParams.get("keyword") ?? undefined;
  const mode = searchParams.get("mode") === "around" ? "around" : "city";
  const location = searchParams.get("location") ?? undefined;
  const radiusRaw = searchParams.get("radius");
  const sort = searchParams.get("sort") === "distance" ? "distance" : "weight";
  const pageRaw = searchParams.get("page");

  if (!AMAP_CITY_MAP[cityId] || !AMAP_TYPE_MAP[type]) {
    return NextResponse.json({ ok: false, error: "invalid_params" }, { status: 400 });
  }
  if (mode === "around" && !isValidLngLat(location)) {
    return NextResponse.json({ ok: false, error: "invalid_location" }, { status: 400 });
  }

  const page = pageRaw ? Number(pageRaw) : 1;
  if (!Number.isFinite(page) || page < 1 || page > 100) {
    return NextResponse.json({ ok: false, error: "invalid_page" }, { status: 400 });
  }

  const [result, curatedIndex] = await Promise.all([
    searchAmapPoisPaged({ cityId, type, keyword, mode, location, radius: radiusRaw ? Number(radiusRaw) : undefined, sort, page }),
    curatedPoiIndex({ cityId }).catch(() => new Map()),
  ]);

  // Editorial overlay (Issue #49): attach VisePanda's curated summary/tags/
  // badges to any POI that's also in the knowledge base. Optional superset
  // field — absent entirely when curated_pois has no matching row (RLS
  // unavailable, table empty, or genuinely no curated data for this POI yet).
  const pois = result.pois.map((poi) => {
    const curated = curatedIndex.get(poi.id);
    if (!curated) return poi;
    return {
      ...poi,
      editorial: {
        summary: curated.editorial_summary,
        tags: curated.tags,
        badges: curated.list_badges,
        badge: "VisePanda Editorial",
      },
    };
  });

  return NextResponse.json({ ok: true, cityId, type, page, hasMore: result.hasMore, pois });
}
