"use client";

import { OcrTranslator } from "@/components/translate/OcrTranslator";
import { PhraseBook } from "@/components/translate/PhraseBook";
import { TextTranslator } from "@/components/translate/TextTranslator";
import { VoiceTranslator } from "@/components/translate/VoiceTranslator";

export function TranslatorPage() {
  return (
    <section className="translator-page" aria-labelledby="translator-title">
      <header className="translator-page__header">
        <p className="section-kicker">Translator</p>
        <h1 id="translator-title">China travel translator</h1>
        <p>Text, image scan, voice, and phrases — all in one view.</p>
      </header>

      <div className="translator-grid">
        <div className="translator-grid__panel">
          <TextTranslator />
        </div>
        <div className="translator-grid__panel">
          <OcrTranslator />
        </div>
        <div className="translator-grid__panel">
          <VoiceTranslator />
        </div>
        <div className="translator-grid__panel">
          <PhraseBook />
        </div>
      </div>
    </section>
  );
}
