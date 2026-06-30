"use client";

import { useState } from "react";
import { TextTranslator } from "@/components/translate/TextTranslator";
import { OcrTranslator } from "@/components/translate/OcrTranslator";
import { PhraseBook } from "@/components/translate/PhraseBook";
import { VoiceTranslator } from "@/components/translate/VoiceTranslator";

type TranslatorTab = "text" | "scan" | "voice" | "phrases";

const TABS: Array<{ key: TranslatorTab; label: string }> = [
  { key: "text", label: "文字翻译 Text" },
  { key: "scan", label: "扫描翻译 Scan" },
  { key: "voice", label: "语音翻译 Voice" },
  { key: "phrases", label: "短语词典 Phrases" },
];

export function TranslatorPage() {
  const [activeTab, setActiveTab] = useState<TranslatorTab>("text");

  return (
    <section className="translator-page" aria-labelledby="translator-title">
      <header className="translator-page__header">
        <p className="section-kicker">翻译工具 / Translator</p>
        <h1 id="translator-title">中英翻译</h1>
        <p>文字翻译、图片扫描识别、语音识别与常用短语对照</p>
      </header>

      <div className="translator-tabs" role="tablist" aria-label="翻译功能选择">
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
