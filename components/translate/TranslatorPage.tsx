"use client";

import { OcrTranslator } from "@/components/translate/OcrTranslator";
import { PhraseBook } from "@/components/translate/PhraseBook";
import { TextTranslator } from "@/components/translate/TextTranslator";
import { VoiceTranslator } from "@/components/translate/VoiceTranslator";
import { useTranslation } from "@/lib/i18n/I18nContext";

export function TranslatorPage() {
  const { t } = useTranslation();

  return (
    <section className="translator-page" aria-labelledby="translator-title">
      <header className="translator-page__header">
        <p className="section-kicker">{t.translate.kicker}</p>
        <h1 id="translator-title">{t.translate.heading}</h1>
        <p>{t.translate.subtitle}</p>
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
