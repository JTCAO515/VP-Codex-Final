"use client";

import { useTranslation, type SupportedLocale } from "@/lib/i18n/I18nContext";

const LOCALE_LABELS: Record<SupportedLocale, string> = {
  en: "EN",
  es: "ES",
  ar: "AR",
  ja: "JA",
  ko: "KO",
  fr: "FR",
};

export function LanguageSwitcher() {
  const { locale, setLocale, t } = useTranslation();

  return (
    <select
      aria-label="Language"
      className="language-switcher"
      value={locale}
      onChange={(e) => setLocale(e.target.value as SupportedLocale)}
    >
      {(Object.entries(LOCALE_LABELS) as [SupportedLocale, string][]).map(([code, label]) => (
        <option key={code} title={t.lang[code]} value={code}>
          {label}
        </option>
      ))}
    </select>
  );
}
