"use client";

import { useState } from "react";
import { OcrTranslator } from "@/components/translate/OcrTranslator";
import { PhraseBook } from "@/components/translate/PhraseBook";
import { TextTranslator } from "@/components/translate/TextTranslator";
import { VoiceTranslator } from "@/components/translate/VoiceTranslator";

type TranslatorTab = "text" | "scan" | "voice" | "phrases";

const TABS: Array<{ key: TranslatorTab; label: string }> = [
  { key: "text", label: "Text" },
  { key: "scan", label: "Scan" },
  { key: "voice", label: "Voice" },
  { key: "phrases", label: "Phrases" },
];

export function TranslatorPage() {
  const [activeTab, setActiveTab] = useState<TranslatorTab>("text");

  return (
    <section className="translator-page" aria-labelledby="translator-title">
      <header className="translator-page__header">
        <p className="section-kicker">Translator</p>
        <h1 id="translator-title">China travel translator</h1>
        <p>Text, image scan, voice recognition, Qwen TTS, and practical phrase support.</p>
      </header>

      <div className="translator-tabs" role="tablist" aria-label="Translation tools">
        {TABS.map((tab) => (
          <button
            aria-selected={tab.key === activeTab}
            className={`translator-tabs__btn${tab.key === activeTab ? " active" : ""}`}
            key={tab.key}
            onClick={() => setActiveTab(tab.key)}
            role="tab"
            type="button"
          >
            {tab.label}
          </button>
        ))}
      </div>

      <div className="translator-page__body">
        {activeTab === "text" && <TextTranslator />}
        {activeTab === "scan" && <OcrTranslator />}
        {activeTab === "voice" && <VoiceTranslator />}
        {activeTab === "phrases" && <PhraseBook />}
      </div>
    </section>
  );
}
