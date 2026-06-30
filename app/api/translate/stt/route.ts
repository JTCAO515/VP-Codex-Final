import { NextResponse } from "next/server";
import { ALIYUN_PROVIDER, QWEN_MODELS, callQwenChatCompletions, normalizeDataUrl } from "@/lib/aliyun/qwen";

export async function POST(req: Request) {
  const { audioUrl, audioBase64, mimeType = "audio/mpeg", language = "zh" } = await req.json();
  const audioData =
    typeof audioUrl === "string" && audioUrl.trim()
      ? audioUrl.trim()
      : typeof audioBase64 === "string" && audioBase64.trim()
        ? normalizeDataUrl(audioBase64.trim(), mimeType)
        : "";

  if (!audioData) {
    return NextResponse.json({ ok: false, error: "missing_audio" }, { status: 400 });
  }

  const model = process.env.QWEN_STT_MODEL?.trim() || QWEN_MODELS.stt;

  try {
    const text = await callQwenChatCompletions({
      model,
      messages: [
        {
          role: "user",
          content: [
            {
              type: "input_audio",
              input_audio: { data: audioData },
            },
          ],
        },
      ],
      extraBody: {
        asr_options: {
          language,
          enable_itn: true,
        },
      },
    });

    return NextResponse.json({
      ok: true,
      provider: ALIYUN_PROVIDER,
      model,
      text: text.trim(),
      language,
    });
  } catch (error) {
    const message = error instanceof Error ? error.message : "stt_failed";
    const status = message === "not_configured" ? 503 : 502;
    return NextResponse.json({ ok: false, error: message }, { status });
  }
}
