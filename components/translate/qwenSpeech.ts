export async function speakWithQwen(text: string, options: { language?: string; voice?: string } = {}) {
  const phrase = text.trim();
  if (!phrase || typeof window === "undefined") return;

  const res = await fetch("/api/translate/tts", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      text: phrase,
      language: options.language ?? "Chinese",
      voice: options.voice ?? "Cherry",
    }),
  });
  const data = await res.json();
  if (!data.ok || !data.audioUrl) {
    throw new Error(data.error ?? "tts_failed");
  }

  const audio = new Audio(data.audioUrl);
  await audio.play();
}
