"use client";

import { Camera, Copy, Languages, Mic, RotateCcw, Upload, Volume2 } from "lucide-react";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import type { ChangeEvent } from "react";
import { speakWithQwen } from "@/components/translate/qwenSpeech";
import { useTranslation, type SupportedLocale } from "@/lib/i18n/I18nContext";

type Direction = "site-to-zh" | "zh-to-site";
type SourceMode = "text" | "image" | "voice";

const LOCALE_LABELS: Record<SupportedLocale, string> = {
  en: "English",
  es: "Spanish",
  ar: "Arabic",
  ja: "Japanese",
  ko: "Korean",
  fr: "French",
};

const QUICK_PHRASES = [
  { label: "Dining", source: "I do not eat spicy food.", zh: "我不吃辣。" },
  { label: "Taxi", source: "Please take me to this address.", zh: "请带我去这个地址。" },
  { label: "Hotel", source: "I have a reservation.", zh: "我有预订。" },
  { label: "Emergency", source: "I need help.", zh: "我需要帮助。" },
];

const TRAVEL_TERMS = [
  { en: "Forbidden City", zh: "故宫", note: "Book early" },
  { en: "The Bund", zh: "外滩", note: "Best at dusk" },
  { en: "No spice", zh: "不要辣", note: "Show at restaurants" },
  { en: "Invoice", zh: "发票", note: "Useful for hotels" },
];

function resizeImageToBase64(file: File, maxPx = 1200): Promise<{ base64: string; mimeType: string }> {
  return new Promise((resolve, reject) => {
    const img = new Image();
    const url = URL.createObjectURL(file);
    img.onload = () => {
      URL.revokeObjectURL(url);
      const scale = Math.min(1, maxPx / Math.max(img.width, img.height));
      const canvas = document.createElement("canvas");
      canvas.width = Math.round(img.width * scale);
      canvas.height = Math.round(img.height * scale);
      const ctx = canvas.getContext("2d");
      if (!ctx) {
        reject(new Error("canvas_ctx"));
        return;
      }
      ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
      const dataUrl = canvas.toDataURL("image/jpeg", 0.85);
      resolve({ base64: dataUrl.split(",")[1] ?? "", mimeType: "image/jpeg" });
    };
    img.onerror = reject;
    img.src = url;
  });
}

function blobToDataUrl(blob: Blob): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(String(reader.result));
    reader.onerror = reject;
    reader.readAsDataURL(blob);
  });
}

export function UnifiedTranslator() {
  const { locale } = useTranslation();
  const [direction, setDirection] = useState<Direction>("site-to-zh");
  const [input, setInput] = useState("");
  const [translation, setTranslation] = useState("");
  const [pinyin, setPinyin] = useState("");
  const [detectedText, setDetectedText] = useState("");
  const [sourceMode, setSourceMode] = useState<SourceMode>("text");
  const [previewUrl, setPreviewUrl] = useState("");
  const [loading, setLoading] = useState("");
  const [recording, setRecording] = useState(false);
  const [error, setError] = useState("");
  const [copied, setCopied] = useState(false);
  const [cameraAvailable, setCameraAvailable] = useState(false);

  const uploadInputRef = useRef<HTMLInputElement>(null);
  const cameraInputRef = useRef<HTMLInputElement>(null);
  const recorderRef = useRef<MediaRecorder | null>(null);
  const chunksRef = useRef<BlobPart[]>([]);

  const siteLanguage = LOCALE_LABELS[locale];
  const from = direction === "site-to-zh" ? locale : "zh";
  const to = direction === "site-to-zh" ? "zh" : locale;
  const sourceLabel = direction === "site-to-zh" ? siteLanguage : "Chinese";
  const targetLabel = direction === "site-to-zh" ? "Chinese" : siteLanguage;
  const outputLanguage = targetLabel;

  useEffect(() => {
    const update = () => setCameraAvailable(window.matchMedia?.("(max-width: 760px)").matches ?? false);
    update();
    window.addEventListener("resize", update);
    return () => window.removeEventListener("resize", update);
  }, []);

  const directionCopy = useMemo(
    () => `${sourceLabel} -> ${targetLabel}`,
    [sourceLabel, targetLabel],
  );

  const resetOutput = useCallback(() => {
    setTranslation("");
    setPinyin("");
    setDetectedText("");
    setError("");
    setCopied(false);
  }, []);

  const translateText = useCallback(
    async (text: string, mode: SourceMode = "text") => {
      const trimmed = text.trim();
      if (!trimmed) return;
      setLoading("Translating with Qwen...");
      setError("");
      setSourceMode(mode);
      try {
        const res = await fetch("/api/translate/text", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ text: trimmed, from, to }),
        });
        const data = await res.json();
        if (!data.ok) throw new Error(data.error ?? "translation_failed");
        setTranslation(data.translation ?? "");
        setPinyin(data.pinyin ?? "");
        if (mode === "text") setDetectedText("");
      } catch {
        setError("Translation failed. Please try again.");
      } finally {
        setLoading("");
      }
    },
    [from, to],
  );

  async function handleCopy() {
    if (!translation) return;
    await navigator.clipboard.writeText(translation);
    setCopied(true);
    setTimeout(() => setCopied(false), 1800);
  }

  async function handleImageFile(file: File) {
    if (!file.type.startsWith("image/")) {
      setError("Please choose an image.");
      return;
    }
    resetOutput();
    setSourceMode("image");
    setPreviewUrl(URL.createObjectURL(file));
    setLoading("Reading image with Qwen...");
    try {
      const { base64, mimeType } = await resizeImageToBase64(file);
      const ocrRes = await fetch("/api/translate/ocr", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ imageBase64: base64, mimeType }),
      });
      const ocrData = await ocrRes.json();
      if (!ocrData.ok || !ocrData.text) throw new Error(ocrData.error ?? "ocr_empty");
      setDetectedText(ocrData.text);
      await translateText(ocrData.text, "image");
    } catch {
      setError("Image translation failed. Try a clearer photo or crop.");
      setLoading("");
    }
  }

  function handleImageChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    if (file) void handleImageFile(file);
    event.target.value = "";
  }

  async function transcribeAudio(payload: { audioBase64: string; mimeType: string }) {
    resetOutput();
    setSourceMode("voice");
    setLoading("Recognizing voice with Qwen...");
    try {
      const res = await fetch("/api/translate/stt", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ...payload, language: from }),
      });
      const data = await res.json();
      if (!data.ok || !data.text) throw new Error(data.error ?? "stt_failed");
      setDetectedText(data.text);
      await translateText(data.text, "voice");
    } catch {
      setError("Voice translation failed. Try a shorter, clearer recording.");
      setLoading("");
    }
  }

  async function startRecording() {
    if (!navigator.mediaDevices?.getUserMedia || typeof MediaRecorder === "undefined") {
      setError("Recording is not supported in this browser.");
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
      const dataUrl = await blobToDataUrl(blob);
      await transcribeAudio({ audioBase64: dataUrl, mimeType: blob.type || "audio/webm" });
    };
    recorderRef.current = recorder;
    recorder.start();
    setRecording(true);
  }

  function stopRecording() {
    recorderRef.current?.stop();
    setRecording(false);
  }

  function applyPhrase(text: string) {
    resetOutput();
    setInput(text);
    void translateText(text, "text");
  }

  return (
    <div className="unified-translator">
      <section className="unified-translator__main" aria-label="Unified translator workspace">
        <div className="unified-translator__toolbar">
          <div className="unified-translator__direction" aria-label="Translation direction">
            <Languages size={17} aria-hidden="true" />
            <button
              className={direction === "site-to-zh" ? "active" : ""}
              onClick={() => {
                setDirection("site-to-zh");
                resetOutput();
              }}
              type="button"
            >
              {siteLanguage} to Chinese
            </button>
            <button
              className={direction === "zh-to-site" ? "active" : ""}
              onClick={() => {
                setDirection("zh-to-site");
                resetOutput();
              }}
              type="button"
            >
              Chinese to {siteLanguage}
            </button>
          </div>
          <span className="unified-translator__provider">Text, image, voice, speech</span>
        </div>

        <div className="unified-translator__workspace">
          <div className="unified-translator__input">
            <label htmlFor="translator-input">{directionCopy}</label>
            <textarea
              id="translator-input"
              placeholder={`Type ${sourceLabel.toLowerCase()} travel text...`}
              value={input}
              onChange={(event) => {
                setInput(event.target.value);
                resetOutput();
              }}
              onKeyDown={(event) => {
                if (event.key === "Enter" && (event.ctrlKey || event.metaKey)) void translateText(input, "text");
              }}
            />
            <div className="unified-translator__actions">
              <button disabled={Boolean(loading) || !input.trim()} onClick={() => translateText(input, "text")} type="button">
                {loading && sourceMode === "text" ? "Translating..." : "Translate"}
              </button>
              <button onClick={() => uploadInputRef.current?.click()} type="button">
                <Upload size={16} aria-hidden="true" />
                Upload Image
              </button>
              <button
                disabled={!cameraAvailable}
                onClick={() => cameraInputRef.current?.click()}
                title={cameraAvailable ? "Use mobile camera" : "Take Photo is available on mobile browsers"}
                type="button"
              >
                <Camera size={16} aria-hidden="true" />
                Take Photo
              </button>
              <button className={recording ? "recording" : ""} onClick={recording ? stopRecording : startRecording} type="button">
                <Mic size={16} aria-hidden="true" />
                {recording ? "Stop" : "Record"}
              </button>
            </div>
            <input accept="image/*" hidden onChange={handleImageChange} ref={uploadInputRef} type="file" />
            <input accept="image/*" capture="environment" hidden onChange={handleImageChange} ref={cameraInputRef} type="file" />
          </div>

          <div className="unified-translator__output" aria-live="polite">
            <div className="unified-translator__output-head">
              <span>{targetLabel}</span>
              {translation && (
                <div>
                  <button onClick={() => speakWithQwen(translation, { language: outputLanguage }).catch(() => setError("TTS failed."))} type="button">
                    <Volume2 size={15} aria-hidden="true" />
                    Speak
                  </button>
                  <button onClick={handleCopy} type="button">
                    <Copy size={15} aria-hidden="true" />
                    {copied ? "Copied" : "Copy"}
                  </button>
                </div>
              )}
            </div>
            {loading && <p className="unified-translator__status">{loading}</p>}
            {error && <p className="unified-translator__error" role="alert">{error}</p>}
            {previewUrl && sourceMode === "image" && <img alt="Uploaded translation source" src={previewUrl} />}
            {detectedText && (
              <div className="unified-translator__detected">
                <span>{sourceMode === "image" ? "Recognized text" : "Transcript"}</span>
                <p>{detectedText}</p>
              </div>
            )}
            {translation ? (
              <div className="unified-translator__result">
                <p>{translation}</p>
                {pinyin && <small>{pinyin}</small>}
              </div>
            ) : (
              !loading && !error && <p className="unified-translator__empty">Translation results appear here.</p>
            )}
          </div>
        </div>
      </section>

      <aside className="unified-translator__side" aria-label="Common phrases and travel terms">
        <div className="unified-translator__mini-header">
          <h2>Phrases</h2>
          <button
            onClick={() => {
              setInput("");
              setPreviewUrl("");
              resetOutput();
            }}
            type="button"
          >
            <RotateCcw size={14} aria-hidden="true" />
            Clear
          </button>
        </div>
        <div className="unified-translator__phrase-list">
          {QUICK_PHRASES.map((phrase) => (
            <button
              key={phrase.label}
              onClick={() => applyPhrase(direction === "site-to-zh" ? phrase.source : phrase.zh)}
              type="button"
            >
              <span>{phrase.label}</span>
              <strong>{direction === "site-to-zh" ? phrase.source : phrase.zh}</strong>
            </button>
          ))}
        </div>
        <div className="unified-translator__terms">
          <h2>Special terms</h2>
          {TRAVEL_TERMS.map((term) => (
            <button key={term.en} onClick={() => applyPhrase(direction === "site-to-zh" ? term.en : term.zh)} type="button">
              <span>{term.en} / {term.zh}</span>
              <small>{term.note}</small>
            </button>
          ))}
        </div>
      </aside>
    </div>
  );
}
