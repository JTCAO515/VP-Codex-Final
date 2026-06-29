import { NextResponse } from "next/server";

export async function GET() {
  return NextResponse.json({
    ok: true,
    status: "placeholder",
    message: "Tools for translation, payment, visa, currency, metro, eSIM, and emergency help connect later.",
  });
}
