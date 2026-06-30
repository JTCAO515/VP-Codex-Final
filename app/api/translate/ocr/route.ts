import { NextResponse } from "next/server";

export async function POST(req: Request) {
  const { imageBase64, mimeType = "image/jpeg" } = await req.json();
  if (!imageBase64 || typeof imageBase64 !== "string") {
    return NextResponse.json({ ok: false, error: "missing_image" }, { status: 400 });
  }

  const apiKey = process.env.OCR_SPACE_API_KEY ?? "helloworld";

  const formData = new FormData();
  formData.append("base64Image", `data:${mimeType};base64,${imageBase64}`);
  formData.append("language", "chs");
  formData.append("OCREngine", "2");
  formData.append("isCreateSearchablePdf", "false");
  formData.append("isSearchablePdfHideTextLayer", "false");

  const res = await fetch("https://api.ocr.space/parse/image", {
    method: "POST",
    headers: { apikey: apiKey },
    body: formData,
  });

  if (!res.ok) {
    return NextResponse.json({ ok: false, error: "ocr_upstream_error" }, { status: 502 });
  }

  const data = await res.json();
  if (data.IsErroredOnProcessing) {
    return NextResponse.json({ ok: false, error: data.ErrorMessage?.[0] ?? "ocr_failed" }, { status: 422 });
  }

  const text = data.ParsedResults?.[0]?.ParsedText ?? "";
  return NextResponse.json({ ok: true, text: text.trim() });
}
