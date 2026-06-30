"use client";

import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import en, { type Translations } from "./translations/en";

export type SupportedLocale = "en" | "es" | "ar" | "ja" | "ko" | "fr";

const SUPPORTED_LOCALES: SupportedLocale[] = ["en", "es", "ar", "ja", "ko", "fr"];
const STORAGE_KEY = "visepanda:locale";
const RTL_LOCALES = new Set<SupportedLocale>(["ar"]);

interface I18nContextValue {
  locale: SupportedLocale;
  setLocale: (locale: SupportedLocale) => void;
  t: Translations;
}

const I18nContext = createContext<I18nContextValue>({
  locale: "en",
  setLocale: () => {},
  t: en,
});

async function loadBundle(locale: SupportedLocale): Promise<Translations> {
  if (locale === "en") return en;
  const mod = await import(`./translations/${locale}`);
  return mod.default as Translations;
}

export function I18nProvider({ children }: { children: ReactNode }) {
  const [locale, setLocaleState] = useState<SupportedLocale>("en");
  const [t, setT] = useState<Translations>(en);

  useEffect(() => {
    const stored = localStorage.getItem(STORAGE_KEY) as SupportedLocale | null;
    const initial: SupportedLocale =
      stored && SUPPORTED_LOCALES.includes(stored) ? stored : "en";
    if (initial !== "en") applyLocale(initial);
  }, []);

  function applyLocale(next: SupportedLocale) {
    setLocaleState(next);
    loadBundle(next).then(setT);
    localStorage.setItem(STORAGE_KEY, next);
    document.documentElement.lang = next;
    document.documentElement.dir = RTL_LOCALES.has(next) ? "rtl" : "ltr";
  }

  return (
    <I18nContext.Provider value={{ locale, setLocale: applyLocale, t }}>
      {children}
    </I18nContext.Provider>
  );
}

export function useTranslation(): I18nContextValue {
  return useContext(I18nContext);
}
