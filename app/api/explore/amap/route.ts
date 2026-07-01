import { type NextRequest, NextResponse } from "next/server";
import { AMAP_CITY_MAP, AMAP_TYPE_MAP, searchAmapPois } from "@/lib/explore/amapSearch";

export async function GET(request: NextRequest) {
  const apiKey = process.env.AMAP_API_KEY;
  if (!apiKey) {
    return NextResponse.json({ ok: false, error: "not_configured" }, { status: 503 });
  }

  const { searchParams } = request.nextUrl;
  const cityId = searchParams.get("cityId") ?? "";
  const type = searchParams.get("type") ?? "";
  const keyword = searchParams.get("keyword") ?? undefined;

  if (!AMAP_CITY_MAP[cityId] || !AMAP_TYPE_MAP[type]) {
    return NextResponse.json({ ok: false, error: "invalid_params" }, { status: 400 });
  }

  const pois = await searchAmapPois({ cityId, type, keyword });

  return NextResponse.json({ ok: true, cityId, type, pois });
}
