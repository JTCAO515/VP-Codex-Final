import { NextResponse } from "next/server";

export async function GET() {
  return NextResponse.json({
    ok: true,
    status: "placeholder",
    message: "Trips persistence is reserved for a later Supabase-backed phase.",
  });
}
