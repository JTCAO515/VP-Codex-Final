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

const DEEPSEEK_PROVIDER = "deepseek";
const DEFAULT_DEEPSEEK_BASE_URL = "https://api.deepseek.com";
const DEFAULT_DEEPSEEK_MODEL = "deepseek-v4-flash";

export async function POST(req: Request) {
  const { text, from = "en", to = "zh" } = await req.json();
  if (!text || typeof text !== "string") {
    return NextResponse.json({ ok: false, error: "missing_text" }, { status: 400 });
  }

  const model = process.env.QWEN_TRANSLATE_MODEL?.trim() || QWEN_MODELS.translate;
  const targetName = LANGUAGE_NAMES[String(to)] ?? "the requested target language";
  const sourceName = LANGUAGE_NAMES[String(from)] ?? "the source language";

  const prompt = buildTranslationPrompt(sourceName, targetName);

  const qwenResult = await translateWithQwen({ model, prompt, text });
  if (qwenResult.ok) {
    return NextResponse.json({
      ok: true,
      provider: ALIYUN_PROVIDER,
      model,
      from,
      to,
      ...qwenResult.body,
    });
  }

  const deepSeekResult = await translateWithDeepSeek({ prompt, text });
  if (deepSeekResult.ok) {
    return NextResponse.json({
      ok: true,
      provider: DEEPSEEK_PROVIDER,
      model: deepSeekResult.model,
      fallbackFrom: ALIYUN_PROVIDER,
      fallbackReason: qwenResult.error,
      from,
      to,
      ...deepSeekResult.body,
    });
  }

  return NextResponse.json(
    {
      ok: false,
      error: "translation_provider_unavailable",
      qwenError: qwenResult.error,
      deepSeekError: deepSeekResult.error,
    },
    { status: qwenResult.error === "not_configured" && deepSeekResult.error === "not_configured" ? 503 : 502 },
  );
}

function buildTranslationPrompt(sourceName: string, targetName: string) {
  return (
    `You are VisePanda's travel translator. Translate ${sourceName} to ${targetName}. ` +
    `Return only valid JSON: {"translation":"...","pinyin":"..."}; use an empty pinyin string unless the output is Chinese.`
  );
}

function normalizeTranslation(content: string) {
  const parsed = parseJsonObject(content);
  return {
    translation: typeof parsed.translation === "string" ? parsed.translation : content,
    pinyin: typeof parsed.pinyin === "string" ? parsed.pinyin : "",
  };
}

async function translateWithQwen(input: { model: string; prompt: string; text: string }) {
  try {
    const content = await callQwenChatCompletions({
      model: input.model,
      responseFormatJson: true,
      messages: [
        { role: "system", content: input.prompt },
        { role: "user", content: input.text },
      ],
    });

    return { ok: true as const, body: normalizeTranslation(content) };
  } catch (error) {
    return { ok: false as const, error: error instanceof Error ? error.message : "qwen_failed" };
  }
}

async function translateWithDeepSeek(input: { prompt: string; text: string }) {
  const apiKey = process.env.DEEPSEEK_API_KEY?.trim() || process.env.AI_API_KEY?.trim();
  if (!apiKey) return { ok: false as const, error: "not_configured" };

  const baseUrl = (process.env.DEEPSEEK_BASE_URL?.trim() || process.env.AI_BASE_URL?.trim() || DEFAULT_DEEPSEEK_BASE_URL).replace(/\/$/, "");
  const model = process.env.DEEPSEEK_TRANSLATE_MODEL?.trim() || process.env.DEEPSEEK_MODEL?.trim() || DEFAULT_DEEPSEEK_MODEL;

  try {
    const response = await fetch(`${baseUrl}/chat/completions`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${apiKey}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        model,
        messages: [
          { role: "system", content: input.prompt },
          { role: "user", content: input.text },
        ],
        response_format: { type: "json_object" },
        temperature: 0.1,
      }),
    });

    if (!response.ok) {
      return { ok: false as const, error: `upstream_http_${response.status}` };
    }

    const data = await response.json();
    const content = String(data.choices?.[0]?.message?.content ?? "").trim();
    if (!content) return { ok: false as const, error: "empty_response" };

    return { ok: true as const, model, body: normalizeTranslation(content) };
  } catch (error) {
    return { ok: false as const, error: error instanceof Error ? error.message : "deepseek_failed" };
  }
}
