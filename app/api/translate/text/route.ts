import { NextResponse } from "next/server";

export async function POST(req: Request) {
  const { text, from = "en", to = "zh" } = await req.json();
  if (!text || typeof text !== "string") {
    return NextResponse.json({ ok: false, error: "missing_text" }, { status: 400 });
  }

  const apiKey = process.env.DEEPSEEK_API_KEY;
  if (!apiKey) {
    return NextResponse.json({ ok: false, error: "not_configured" }, { status: 503 });
  }

  const systemPrompt =
    to === "zh"
      ? `You are a professional English-to-Chinese translator. Translate the user's text into Simplified Chinese. Also provide pinyin romanization. Respond ONLY with valid JSON in this format: {"translation":"...","pinyin":"..."}`
      : `You are a professional Chinese-to-English translator. Translate the user's text into natural English. Respond ONLY with valid JSON in this format: {"translation":"..."}`;

  const res = await fetch("https://api.deepseek.com/chat/completions", {
    method: "POST",
    headers: { "Content-Type": "application/json", Authorization: `Bearer ${apiKey}` },
    body: JSON.stringify({
      model: "deepseek-chat",
      temperature: 0.1,
      response_format: { type: "json_object" },
      messages: [
        { role: "system", content: systemPrompt },
        { role: "user", content: text },
      ],
    }),
  });

  if (!res.ok) {
    return NextResponse.json({ ok: false, error: "upstream_error" }, { status: 502 });
  }

  const data = await res.json();
  const content = data.choices?.[0]?.message?.content ?? "{}";
  const parsed = JSON.parse(content);

  return NextResponse.json({ ok: true, from, to, ...parsed });
}
