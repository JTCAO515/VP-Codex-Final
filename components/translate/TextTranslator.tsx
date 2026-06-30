"use client";

import { useCallback, useState } from "react";
import { speakWithQwen } from "@/components/translate/qwenSpeech";

type Direction = "en-zh" | "zh-en";

export function TextTranslator() {
  const [direction, setDirection] = useState<Direction>("en-zh");
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
      const [from, to] = direction === "en-zh" ? ["en", "zh"] : ["zh", "en"];
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
      setError("Translation failed. Please try again.");
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

  const outputLanguage = direction === "en-zh" ? "Chinese" : "English";

  function setNextDirection(nextDirection: Direction) {
    setDirection(nextDirection);
    setTranslation("");
    setPinyin("");
  }

  return (
    <div className="text-translator">
      <div className="text-translator__direction">
        <button className={direction === "en-zh" ? "active" : ""} onClick={() => setNextDirection("en-zh")} type="button">
          English to Chinese
        </button>
        <button className={direction === "zh-en" ? "active" : ""} onClick={() => setNextDirection("zh-en")} type="button">
          Chinese to English
        </button>
      </div>

      <div className="text-translator__input-area">
        <textarea
          className="text-translator__input"
          placeholder={direction === "en-zh" ? "Enter English travel text..." : "Enter Chinese travel text..."}
          rows={4}
          value={input}
          onChange={(event) => setInput(event.target.value)}
          onKeyDown={(event) => {
            if (event.key === "Enter" && (event.ctrlKey || event.metaKey)) void translate();
          }}
        />
        <button
          className="text-translator__btn-translate"
          disabled={loading || !input.trim()}
          onClick={translate}
          type="button"
        >
          {loading ? "Translating..." : "Translate"}
        </button>
      </div>

      {error && (
        <p className="text-translator__error" role="alert">
          {error}
        </p>
      )}

      {translation && (
        <div className="text-translator__output">
          <p className="text-translator__result">{translation}</p>
          {pinyin && <p className="text-translator__pinyin">{pinyin}</p>}
          <div className="text-translator__actions">
            <button
              onClick={() =>
                speakWithQwen(translation, { language: outputLanguage }).catch(() =>
                  setError("TTS failed. Please try again later."),
                )
              }
              type="button"
            >
              Speak
            </button>
            <button onClick={handleCopy} type="button">
              {copied ? "Copied" : "Copy"}
            </button>
          </div>
        </div>
      )}

      <p className="text-translator__hint">Ctrl+Enter to translate.</p>
    </div>
  );
}
