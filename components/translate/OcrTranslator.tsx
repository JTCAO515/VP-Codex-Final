"use client";

import { useState, useRef } from "react";

function speak(text: string) {
  if (typeof window === "undefined" || !window.speechSynthesis) return;
  window.speechSynthesis.cancel();
  const utterance = new SpeechSynthesisUtterance(text);
  utterance.lang = "zh-CN";
  utterance.rate = 0.85;
  window.speechSynthesis.speak(utterance);
}

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
      if (!ctx) { reject(new Error("canvas_ctx")); return; }
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
      setError("请上传图片文件 / Please upload an image file");
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
    } catch (e) {
      const msg = e instanceof Error ? e.message : "unknown";
      setError(msg === "ocr_empty" ? "未能识别图片中的文字 / No text found in image" : "处理失败，请重试 / Processing failed");
    } finally {
      setLoading(false);
    }
  }

  function handleDrop(e: React.DragEvent) {
    e.preventDefault();
    const file = e.dataTransfer.files[0];
    if (file) handleFile(file);
  }

  return (
    <div className="ocr-translator">
      <div
        className="ocr-translator__drop-zone"
        onDragOver={(e) => e.preventDefault()}
        onDrop={handleDrop}
      >
        {preview ? (
          <img alt="扫描图片" className="ocr-translator__preview" src={preview} />
        ) : (
          <p>拖放图片至此 / Drag & drop image here</p>
        )}
      </div>

      <div className="ocr-translator__controls">
        <input
          accept="image/*"
          capture="environment"
          onChange={(e) => { const f = e.target.files?.[0]; if (f) handleFile(f); }}
          ref={fileRef}
          style={{ display: "none" }}
          type="file"
        />
        <button onClick={() => fileRef.current?.click()} type="button">
          📷 拍照 / 选择图片
        </button>
        {preview && !loading && (
          <button onClick={() => { setPreview(null); setOcrText(""); setTranslation(""); setPinyin(""); setError(""); }} type="button">
            重置 Reset
          </button>
        )}
      </div>

      {loading && <p className="ocr-translator__loading" role="status">识别并翻译中... Processing...</p>}
      {error && <p className="ocr-translator__error" role="alert">{error}</p>}

      {ocrText && (
        <div className="ocr-translator__results">
          <section>
            <h3>识别文字 Recognized text</h3>
            <p>{ocrText}</p>
          </section>
          {translation && (
            <section>
              <h3>英文翻译 English translation</h3>
              <p>{translation}</p>
              {pinyin && <p className="ocr-translator__pinyin">{pinyin}</p>}
              <button onClick={() => speak(ocrText)} type="button">🔊 朗读中文 Speak Chinese</button>
            </section>
          )}
        </div>
      )}

      <p className="ocr-translator__hint">适用于菜单、路牌、标识的扫描翻译 / For menus, signs, and labels</p>
    </div>
  );
}
