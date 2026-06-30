"use client";

import { useRef, useState } from "react";
import type { DragEvent } from "react";
import { speakWithQwen } from "@/components/translate/qwenSpeech";

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
      resolve({ base64: dataUrl.split(",")[1], mimeType: "image/jpeg" });
    };
    img.onerror = reject;
    img.src = url;
  });
}

export function OcrTranslator() {
  const [preview, setPreview] = useState<string | null>(null);
  const [ocrText, setOcrText] = useState("");
  const [translation, setTranslation] = useState("");
  const [pinyin, setPinyin] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const fileRef = useRef<HTMLInputElement>(null);

  async function handleFile(file: File) {
    if (!file.type.startsWith("image/")) {
      setError("Please upload an image file.");
      return;
    }
    setError("");
    setOcrText("");
    setTranslation("");
    setPinyin("");
    setLoading(true);

    try {
      const objectUrl = URL.createObjectURL(file);
      setPreview(objectUrl);

      const { base64, mimeType } = await resizeImageToBase64(file);

      const ocrRes = await fetch("/api/translate/ocr", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ imageBase64: base64, mimeType }),
      });
      const ocrData = await ocrRes.json();
      if (!ocrData.ok || !ocrData.text) throw new Error(ocrData.error ?? "ocr_empty");

      setOcrText(ocrData.text);

      const transRes = await fetch("/api/translate/text", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ text: ocrData.text, from: "zh", to: "en" }),
      });
      const transData = await transRes.json();
      if (!transData.ok) throw new Error(transData.error ?? "translate_failed");

      setTranslation(transData.translation ?? "");
      setPinyin(transData.pinyin ?? "");
    } catch (errorValue) {
      const message = errorValue instanceof Error ? errorValue.message : "unknown";
      setError(message === "ocr_empty" ? "No text found in this image." : "Processing failed. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  function handleDrop(event: DragEvent) {
    event.preventDefault();
    const file = event.dataTransfer.files[0];
    if (file) void handleFile(file);
  }

  function resetScan() {
    setPreview(null);
    setOcrText("");
    setTranslation("");
    setPinyin("");
    setError("");
  }

  return (
    <div className="ocr-translator">
      <div className="ocr-translator__drop-zone" onDragOver={(event) => event.preventDefault()} onDrop={handleDrop}>
        {preview ? (
          <img alt="Scanned travel text" className="ocr-translator__preview" src={preview} />
        ) : (
          <p>Drag a menu, sign, or label image here.</p>
        )}
      </div>

      <div className="ocr-translator__controls">
        <input
          accept="image/*"
          capture="environment"
          onChange={(event) => {
            const file = event.target.files?.[0];
            if (file) void handleFile(file);
          }}
          ref={fileRef}
          style={{ display: "none" }}
          type="file"
        />
        <button onClick={() => fileRef.current?.click()} type="button">
          Choose image
        </button>
        {preview && !loading && (
          <button onClick={resetScan} type="button">
            Reset
          </button>
        )}
      </div>

      {loading && (
        <p className="ocr-translator__loading" role="status">
          Reading and translating with Qwen...
        </p>
      )}
      {error && (
        <p className="ocr-translator__error" role="alert">
          {error}
        </p>
      )}

      {ocrText && (
        <div className="ocr-translator__results">
          <section>
            <h3>Recognized text</h3>
            <p>{ocrText}</p>
          </section>
          {translation && (
            <section>
              <h3>English translation</h3>
              <p>{translation}</p>
              {pinyin && <p className="ocr-translator__pinyin">{pinyin}</p>}
              <button
                onClick={() =>
                  speakWithQwen(ocrText, { language: "Chinese" }).catch(() =>
                    setError("TTS failed. Please try again later."),
                  )
                }
                type="button"
              >
                Speak Chinese
              </button>
            </section>
          )}
        </div>
      )}

      <p className="ocr-translator__hint">Built for menus, signs, tickets, and labels.</p>
    </div>
  );
}
