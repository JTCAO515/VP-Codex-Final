import { type NextRequest, NextResponse } from "next/server";

const CITY_MAP: Record<string, string> = {
  beijing: "北京",
  shanghai: "上海",
  chengdu: "成都",
  xian: "西安",
  guangzhou: "广州",
  hangzhou: "杭州",
  suzhou: "苏州",
  chongqing: "重庆",
};

const TYPE_MAP: Record<string, string> = {
  attractions: "110000",
  food: "050000",
  stays: "100000",
};

export async function GET(request: NextRequest) {
  const apiKey = process.env.AMAP_API_KEY;
  if (!apiKey) {
    return NextResponse.json({ ok: false, error: "not_configured" }, { status: 503 });
  }

  const { searchParams } = request.nextUrl;
  const cityId = searchParams.get("cityId") ?? "";
  const type = searchParams.get("type") ?? "";

  const cityName = CITY_MAP[cityId];
  const typeCode = TYPE_MAP[type];

  if (!cityName || !typeCode) {
    return NextResponse.json({ ok: false, error: "invalid_params" }, { status: 400 });
  }

  const url = new URL("https://restapi.amap.com/v3/place/text");
  url.searchParams.set("key", apiKey);
  url.searchParams.set("city", cityName);
  url.searchParams.set("types", typeCode);
  url.searchParams.set("offset", "10");
  url.searchParams.set("page", "1");
  url.searchParams.set("output", "json");

  const res = await fetch(url.toString(), { next: { revalidate: 3600 } });

  if (!res.ok) {
    return NextResponse.json({ ok: false, error: "upstream_error" }, { status: 502 });
  }

  const data = await res.json();
  if (data.status !== "1") {
    return NextResponse.json({ ok: false, error: data.info ?? "amap_error" }, { status: 502 });
  }

  return NextResponse.json({ ok: true, cityId, type, pois: data.pois ?? [] });
}
