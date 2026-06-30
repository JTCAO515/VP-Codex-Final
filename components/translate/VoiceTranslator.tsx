"use client";

import { useRef, useState } from "react";
import { speakWithQwen } from "@/components/translate/qwenSpeech";

type VoiceDirection = "zh-en" | "en-zh";

function blobToDataUrl(blob: Blob): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(String(reader.result));
    reader.onerror = reject;
    reader.readAsDataURL(blob);
  });
}

export function VoiceTranslator() {
  const [direction, setDirection] = useState<VoiceDirection>("zh-en");
  const [audioUrlInput, setAudioUrlInput] = useState("");
  const [previewUrl, setPreviewUrl] = useState("");
  const [transcript, setTranscript] = useState("");
  const [translation, setTranslation] = useState("");
  const [pinyin, setPinyin] = useState("");
  const [recording, setRecording] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const recorderRef = useRef<MediaRecorder | null>(null);
  const chunksRef = useRef<BlobPart[]>([]);
  const fileRef = useRef<HTMLInputElement>(null);

  const sourceLang = direction === "zh-en" ? "zh" : "en";
  const targetLang = direction === "zh-en" ? "en" : "zh";
  const outputLanguage = direction === "zh-en" ? "English" : "Chinese";

  async function translateText(text: string) {
    const res = await fetch("/api/translate/text", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ text, from: sourceLang, to: targetLang }),
    });
    const data = await res.json();
    if (!data.ok) throw new Error(data.error ?? "translate_failed");
    setTranslation(data.translation ?? "");
    setPinyin(data.pinyin ?? "");
  }

  async function transcribe(payload: { audioUrl?: string; audioBase64?: string; mimeType?: string }) {
    setLoading(true);
    setError("");
    setTranscript("");
    setTranslation("");
    setPinyin("");
    try {
      const res = await fetch("/api/translate/stt", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ...payload, language: sourceLang }),
      });
      const data = await res.json();
      if (!data.ok || !data.text) throw new Error(data.error ?? "stt_failed");
      setTranscript(data.text);
      await translateText(data.text);
    } catch {
      setError("Speech recognition failed. Try a clearer recording or upload.");
    } finally {
      setLoading(false);
    }
  }

  async function handleFile(file: File) {
    setPreviewUrl(URL.createObjectURL(file));
    const dataUrl = await blobToDataUrl(file);
    await transcribe({ audioBase64: dataUrl, mimeType: file.type || "audio/mpeg" });
  }

  async function startRecording() {
    if (!navigator.mediaDevices?.getUserMedia || typeof MediaRecorder === "undefined") {
      setError("Recording is not supported in this browser. Upload an audio file instead.");
      return;
    }
    setError("");
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    const recorder = new MediaRecorder(stream);
    chunksRef.current = [];
    recorder.ondataavailable = (event) => {
      if (event.data.size > 0) chunksRef.current.push(event.data);
    };
    recorder.onstop = async () => {
      stream.getTracks().forEach((track) => track.stop());
      const blob = new Blob(chunksRef.current, { type: recorder.mimeType || "audio/webm" });
      setPreviewUrl(URL.createObjectURL(blob));
      const dataUrl = await blobToDataUrl(blob);
      await transcribe({ audioBase64: dataUrl, mimeType: blob.type || "audio/webm" });
    };
    recorderRef.current = recorder;
    recorder.start();
    setRecording(true);
  }

  function stopRecording() {
    recorderRef.current?.stop();
    setRecording(false);
  }

  return (
    <div className="voice-translator">
      <section className="voice-translator__panel" aria-labelledby="voice-translator-title">
        <div>
          <p className="section-kicker">Qwen STT / TTS</p>
          <h2 id="voice-translator-title">Voice translation</h2>
          <p>Speak, upload audio, or paste a public audio URL. VisePanda transcribes it with Qwen and translates it.</p>
        </div>

        <div className="voice-translator__direction">
          <button className={direction === "zh-en" ? "active" : ""} onClick={() => setDirection("zh-en")} type="button">
            Chinese voice to English
          </button>
          <button className={direction === "en-zh" ? "active" : ""} onClick={() => setDirection("en-zh")} type="button">
            English voice to Chinese
          </button>
        </div>

        <div className="voice-translator__controls">
          <button onClick={recording ? stopRecording : startRecording} type="button">
            {recording ? "Stop recording" : "Record voice"}
          </button>
          <button onClick={() => fileRef.current?.click()} type="button">
            Upload audio
          </button>
          <input
            accept="audio/*"
            onChange={(event) => {
              const file = event.target.files?.[0];
              if (file) void handleFile(file);
            }}
            ref={fileRef}
            style={{ display: "none" }}
            type="file"
          />
        </div>

        <label className="voice-translator__url">
          <span>Audio file URL</span>
          <div>
            <input
              placeholder="https://example.com/voice.mp3"
              value={audioUrlInput}
              onChange={(event) => setAudioUrlInput(event.target.value)}
            />
            <button
              disabled={!audioUrlInput.trim() || loading}
              onClick={() => transcribe({ audioUrl: audioUrlInput.trim() })}
              type="button"
            >
              Transcribe URL
            </button>
          </div>
        </label>

        {previewUrl && <audio className="voice-translator__audio" controls src={previewUrl} />}
        {loading && (
          <p className="voice-translator__status" role="status">
            Recognizing with Qwen...
          </p>
        )}
        {error && (
          <p className="voice-translator__error" role="alert">
            {error}
          </p>
        )}
      </section>

      {(transcript || translation) && (
        <section className="voice-translator__result" aria-label="Voice translation result">
          {transcript && (
            <div>
              <h3>Transcript</h3>
              <p>{transcript}</p>
            </div>
          )}
          {translation && (
            <div>
              <h3>Translation</h3>
              <p>{translation}</p>
              {pinyin && <p className="voice-translator__pinyin">{pinyin}</p>}
              <button
                onClick={() =>
                  speakWithQwen(translation, { language: outputLanguage }).catch(() =>
                    setError("TTS failed. Please try again later."),
                  )
                }
                type="button"
              >
                Speak translation
              </button>
            </div>
          )}
        </section>
      )}
    </div>
  );
}
