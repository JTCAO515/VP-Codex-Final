import { afterEach, describe, expect, it, vi } from "vitest";

afterEach(() => {
  vi.unstubAllEnvs();
  vi.restoreAllMocks();
});

describe("Qwen translator API routes", () => {
  it("uses Qwen-MT Flash for text translation", async () => {
    vi.stubEnv("DASHSCOPE_API_KEY", "test-dashscope-key");
    const fetchMock = vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response(JSON.stringify({ choices: [{ message: { content: "你好" } }] }), { status: 200 }),
    );
    const { POST } = await import("@/app/api/translate/text/route");

    const response = await POST(new Request("http://localhost/api/translate/text", {
      method: "POST",
      body: JSON.stringify({ text: "Hello", from: "en", to: "zh" }),
    }));

    expect(response.status).toBe(200);
    expect(await response.json()).toMatchObject({
      ok: true,
      provider: "aliyun-bailian",
      model: "qwen-mt-flash",
      translation: "你好",
    });
    expect(fetchMock).toHaveBeenCalledWith(
      "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions",
      expect.objectContaining({
        headers: expect.objectContaining({ Authorization: "Bearer test-dashscope-key" }),
        body: expect.stringContaining('"model":"qwen-mt-flash"'),
      }),
    );
  });

  it("uses Qwen OCR instead of OCR.space", async () => {
    vi.stubEnv("DASHSCOPE_API_KEY", "test-dashscope-key");
    const fetchMock = vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response(JSON.stringify({ choices: [{ message: { content: "北京站" } }] }), { status: 200 }),
    );
    const { POST } = await import("@/app/api/translate/ocr/route");

    const response = await POST(new Request("http://localhost/api/translate/ocr", {
      method: "POST",
      body: JSON.stringify({ imageBase64: "abc123", mimeType: "image/jpeg" }),
    }));

    expect(response.status).toBe(200);
    expect(await response.json()).toMatchObject({
      ok: true,
      provider: "aliyun-bailian",
      model: "qwen3.5-ocr",
      text: "北京站",
    });
    expect(fetchMock).toHaveBeenCalledWith(
      "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions",
      expect.objectContaining({
        body: expect.stringContaining('"model":"qwen3.5-ocr"'),
      }),
    );
  });

  it("creates Qwen TTS audio URLs", async () => {
    vi.stubEnv("DASHSCOPE_API_KEY", "test-dashscope-key");
    const fetchMock = vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response(JSON.stringify({
        output: { audio: { url: "https://example.com/audio.wav", expires_at: 1770000000 } },
      }), { status: 200 }),
    );
    const { POST } = await import("@/app/api/translate/tts/route");

    const response = await POST(new Request("http://localhost/api/translate/tts", {
      method: "POST",
      body: JSON.stringify({ text: "你好", language: "Chinese" }),
    }));

    expect(response.status).toBe(200);
    expect(await response.json()).toMatchObject({
      ok: true,
      provider: "aliyun-bailian",
      model: "qwen3-tts-instruct-flash",
      audioUrl: "https://example.com/audio.wav",
    });
    expect(fetchMock).toHaveBeenCalledWith(
      "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation",
      expect.objectContaining({
        body: expect.stringContaining('"model":"qwen3-tts-instruct-flash"'),
      }),
    );
  });

  it("transcribes public audio URLs with Qwen ASR Flash", async () => {
    vi.stubEnv("DASHSCOPE_API_KEY", "test-dashscope-key");
    const fetchMock = vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response(JSON.stringify({ choices: [{ message: { content: "你好，我要去北京" } }] }), { status: 200 }),
    );
    const { POST } = await import("@/app/api/translate/stt/route");

    const response = await POST(new Request("http://localhost/api/translate/stt", {
      method: "POST",
      body: JSON.stringify({ audioUrl: "https://example.com/voice.mp3", language: "zh" }),
    }));

    expect(response.status).toBe(200);
    expect(await response.json()).toMatchObject({
      ok: true,
      provider: "aliyun-bailian",
      model: "qwen3-asr-flash",
      text: "你好，我要去北京",
    });
    expect(fetchMock).toHaveBeenCalledWith(
      "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions",
      expect.objectContaining({
        body: expect.stringContaining('"type":"input_audio"'),
      }),
    );
  });
});
