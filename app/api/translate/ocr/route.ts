import { NextResponse } from "next/server";
import { ALIYUN_PROVIDER, QWEN_MODELS, callQwenChatCompletions, normalizeDataUrl } from "@/lib/aliyun/qwen";

export async function POST(req: Request) {
  const { imageBase64, mimeType = "image/jpeg" } = await req.json();
  if (!imageBase64 || typeof imageBase64 !== "string") {
    return NextResponse.json({ ok: false, error: "missing_image" }, { status: 400 });
  }

  const model = process.env.QWEN_OCR_MODEL?.trim() || QWEN_MODELS.ocr;

  try {
    const text = await callQwenChatCompletions({
      model,
      messages: [
        {
          role: "user",
          content: [
            {
              type: "image_url",
              image_url: { url: normalizeDataUrl(imageBase64, mimeType) },
              min_pixels: 32 * 32 * 3,
              max_pixels: 32 * 32 * 8192,
            },
            {
              type: "text",
              text:
                "Extract only the visible text from this travel image, menu, sign, ticket, or label. " +
                "Preserve Chinese and English text. Do not add explanations.",
            },
          ],
        },
      ],
    });

    return NextResponse.json({
      ok: true,
      provider: ALIYUN_PROVIDER,
      model,
      text: text.trim(),
    });
  } catch (error) {
    const message = error instanceof Error ? error.message : "ocr_failed";
    const status = message === "not_configured" ? 503 : 502;
    return NextResponse.json({ ok: false, error: message }, { status });
  }
}
