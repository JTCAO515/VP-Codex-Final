import { NextResponse } from "next/server";
import { ALIYUN_PROVIDER, QWEN_MODELS, getDashScopeUrl, getQwenApiKey } from "@/lib/aliyun/qwen";

export async function POST(req: Request) {
  const { text, language = "Chinese", voice = "Cherry", instructions } = await req.json();
  if (!text || typeof text !== "string") {
    return NextResponse.json({ ok: false, error: "missing_text" }, { status: 400 });
  }

  const apiKey = getQwenApiKey();
  if (!apiKey) {
    return NextResponse.json({ ok: false, error: "not_configured" }, { status: 503 });
  }

  const model = process.env.QWEN_TTS_MODEL?.trim() || QWEN_MODELS.tts;
  const input: Record<string, unknown> = {
    text,
    voice,
    language_type: language,
  };
  if (typeof instructions === "string" && instructions.trim()) {
    input.instructions = instructions.trim();
    input.optimize_instructions = true;
  }

  const res = await fetch(getDashScopeUrl("/services/aigc/multimodal-generation/generation"), {
    method: "POST",
    headers: { "Content-Type": "application/json", Authorization: `Bearer ${apiKey}` },
    body: JSON.stringify({ model, input }),
  });

  if (!res.ok) {
    return NextResponse.json({ ok: false, error: "tts_upstream_error" }, { status: 502 });
  }

  const data = await res.json();
  const audioUrl =
    data.output?.audio?.url ??
    data.output?.audios?.[0]?.url ??
    data.output?.choices?.[0]?.message?.content?.[0]?.audio?.url ??
    "";

  if (!audioUrl) {
    return NextResponse.json({ ok: false, error: "tts_audio_missing" }, { status: 502 });
  }

  return NextResponse.json({
    ok: true,
    provider: ALIYUN_PROVIDER,
    model,
    audioUrl,
    expiresAt: data.output?.audio?.expires_at ?? data.output?.audios?.[0]?.expires_at ?? null,
  });
}
