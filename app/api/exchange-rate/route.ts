import { NextResponse } from "next/server";

const TARGET_CURRENCIES = ["USD", "EUR", "GBP", "JPY", "KRW", "HKD", "TWD", "AUD", "CAD", "SGD"];

export async function GET() {
  const apiKey = process.env.EXCHANGE_RATE_API_KEY;
  if (!apiKey) {
    return NextResponse.json({ ok: false, error: "not_configured" }, { status: 503 });
  }

  const res = await fetch(`https://v6.exchangerate-api.com/v6/${apiKey}/latest/CNY`, {
    next: { revalidate: 3600 },
  });

  if (!res.ok) {
    return NextResponse.json({ ok: false, error: "upstream_error" }, { status: 502 });
  }

  const data = await res.json();
  if (data.result !== "success") {
    return NextResponse.json({ ok: false, error: data["error-type"] ?? "api_error" }, { status: 502 });
  }

  const rates: Record<string, number> = {};
  for (const code of TARGET_CURRENCIES) {
    const rate = data.conversion_rates?.[code];
    if (typeof rate === "number") rates[code] = rate;
  }

  return NextResponse.json({
    ok: true,
    base: "CNY",
    rates,
    updatedAt: data.time_last_update_utc as string,
  });
}
