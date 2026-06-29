import { NextResponse } from "next/server";

export async function GET() {
  return NextResponse.json({
    ok: true,
    status: "placeholder",
    message: "Explore providers for cities, attractions, dining, hotels, and experiences connect later.",
  });
}
