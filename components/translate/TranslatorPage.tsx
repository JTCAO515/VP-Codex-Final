"use client";

import { UnifiedTranslator } from "@/components/translate/UnifiedTranslator";
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

      <UnifiedTranslator />
    </section>
  );
}
