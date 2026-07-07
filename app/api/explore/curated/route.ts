import { type NextRequest, NextResponse } from "next/server";
import { AMAP_CITY_MAP, AMAP_TYPE_MAP } from "@/lib/explore/amapSearch";
import { listCuratedPois } from "@/lib/explore/curatedPois";

// VisePanda Editorial precinct (Issue #49) — pure curated data, no live Amap
// call. Backs the Explore home page's "editor's picks" strip and the UGC-mock
// feed (spec §1.1): both render honest editorial content instead of invented
// user posts.
export async function GET(request: NextRequest) {
  const { searchParams } = request.nextUrl;
  const cityId = searchParams.get("cityId") ?? "";
  const category = searchParams.get("category") ?? undefined;

  if (!AMAP_CITY_MAP[cityId]) {
    return NextResponse.json({ ok: false, error: "invalid_params" }, { status: 400 });
  }
  if (category && !AMAP_TYPE_MAP[category]) {
    return NextResponse.json({ ok: false, error: "invalid_params" }, { status: 400 });
  }

  const entries = await listCuratedPois({ cityId, category });
  return NextResponse.json({ ok: true, cityId, category: category ?? null, entries });
}
