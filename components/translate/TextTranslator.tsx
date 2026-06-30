"use client";

import { useState, useCallback } from "react";
import { speakWithQwen } from "@/components/translate/qwenSpeech";

type Direction = "en→zh" | "zh→en";

export function TextTranslator() {
  const [direction, setDirection] = useState<Direction>("en→zh");
  const [input, setInput] = useState("");
  const [translation, setTranslation] = useState("");
  const [pinyin, setPinyin] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [copied, setCopied] = useState(false);

  const translate = useCallback(async () => {
    const text = input.trim();
    if (!text) return;
    setLoading(true);
    setError("");
    setTranslation("");
    setPinyin("");
    try {
      const [from, to] = direction === "en→zh" ? ["en", "zh"] : ["zh", "en"];
      const res = await fetch("/api/translate/text", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ text, from, to }),
      });
      const data = await res.json();
      if (!data.ok) throw new Error(data.error ?? "translation_failed");
      setTranslation(data.translation ?? "");
      setPinyin(data.pinyin ?? "");
    } catch {
      setError("翻译失败，请重试。Translation failed, please try again.");
    } finally {
      setLoading(false);
    }
  }, [input, direction]);

  async function handleCopy() {
    if (!translation) return;
    await navigator.clipboard.writeText(translation);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  }

  const outputLanguage = direction === "en→zh" ? "Chinese" : "English";

  return (
    <div className="text-translator">
      <div className="text-translator__direction">
        <button
          className={direction === "en→zh" ? "active" : ""}
          onClick={() => { setDirection("en→zh"); setTranslation(""); setPinyin(""); }}
          type="button"
        >
          English → 中文
        </button>
        <button
          className={direction === "zh→en" ? "active" : ""}
          onClick={() => { setDirection("zh→en"); setTranslation(""); setPinyin(""); }}
          type="button"
        >
          中文 → English
        </button>
      </div>

      <div className="text-translator__input-area">
        <textarea
          className="text-translator__input"
          placeholder={direction === "en→zh" ? "输入英文..." : "输入中文..."}
          rows={4}
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => { if (e.key === "Enter" && (e.ctrlKey || e.metaKey)) translate(); }}
        />
        <button
          className="text-translator__btn-translate"
          disabled={loading || !input.trim()}
          onClick={translate}
          type="button"
        >
          {loading ? "翻译中..." : "翻译 Translate"}
        </button>
      </div>

      {error && <p className="text-translator__error" role="alert">{error}</p>}

      {translation && (
        <div className="text-translator__output">
          <p className="text-translator__result">{translation}</p>
          {pinyin && <p className="text-translator__pinyin">{pinyin}</p>}
          <div className="text-translator__actions">
            <button
              onClick={() => speakWithQwen(translation, { language: outputLanguage }).catch(() => setError("朗读失败，请稍后再试 / TTS failed"))}
              type="button"
            >
              🔊 朗读 Speak
            </button>
            <button onClick={handleCopy} type="button">
              {copied ? "✓ 已复制" : "复制 Copy"}
            </button>
          </div>
        </div>
      )}

      <p className="text-translator__hint">Ctrl+Enter 快速翻译 / Ctrl+Enter to translate</p>
    </div>
  );
}
