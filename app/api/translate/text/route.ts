import { NextResponse } from "next/server";
import { ALIYUN_PROVIDER, QWEN_MODELS, callQwenChatCompletions, parseJsonObject } from "@/lib/aliyun/qwen";

const LANGUAGE_NAMES: Record<string, string> = {
  ar: "Arabic",
  en: "English",
  es: "Spanish",
  fr: "French",
  ja: "Japanese",
  ko: "Korean",
  zh: "Simplified Chinese",
};

export async function POST(req: Request) {
  const { text, from = "en", to = "zh" } = await req.json();
  if (!text || typeof text !== "string") {
    return NextResponse.json({ ok: false, error: "missing_text" }, { status: 400 });
  }

  const model = process.env.QWEN_TRANSLATE_MODEL?.trim() || QWEN_MODELS.translate;
  const targetName = LANGUAGE_NAMES[String(to)] ?? "the requested target language";
  const sourceName = LANGUAGE_NAMES[String(from)] ?? "the source language";

  try {
    const content = await callQwenChatCompletions({
      model,
      responseFormatJson: true,
      messages: [
        {
          role: "system",
          content:
            `You are VisePanda's travel translator. Translate ${sourceName} to ${targetName}. ` +
            `Return only valid JSON: {"translation":"...","pinyin":"..."}; use an empty pinyin string unless the output is Chinese.`,
        },
        { role: "user", content: text },
      ],
    });
    const parsed = parseJsonObject(content);
    const translation = typeof parsed.translation === "string" ? parsed.translation : content;
    const pinyin = typeof parsed.pinyin === "string" ? parsed.pinyin : "";

    return NextResponse.json({
      ok: true,
      provider: ALIYUN_PROVIDER,
      model,
      from,
      to,
      translation,
      pinyin,
    });
  } catch (error) {
    const message = error instanceof Error ? error.message : "translation_failed";
    const status = message === "not_configured" ? 503 : 502;
    return NextResponse.json({ ok: false, error: message }, { status });
  }
}
