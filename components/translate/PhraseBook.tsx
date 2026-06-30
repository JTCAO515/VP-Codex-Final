"use client";

import { useState } from "react";
import { phrases, specialTerms, PHRASE_CATEGORY_LABELS, SPECIAL_TERM_CATEGORY_LABELS } from "@/lib/translate/phrases";
import type { PhraseCategory, SpecialTermCategory } from "@/lib/translate/types";

function speak(text: string) {
  if (typeof window === "undefined" || !window.speechSynthesis) return;
  window.speechSynthesis.cancel();
  const utterance = new SpeechSynthesisUtterance(text);
  utterance.lang = "zh-CN";
  utterance.rate = 0.85;
  window.speechSynthesis.speak(utterance);
}

type BookTab = "phrases" | "terms";
type PhraseTab = PhraseCategory;
type TermTab = SpecialTermCategory;

export function PhraseBook() {
  const [bookTab, setBookTab] = useState<BookTab>("phrases");
  const [phraseTab, setPhraseTab] = useState<PhraseTab>("greetings");
  const [termTab, setTermTab] = useState<TermTab>("attractions");

  const visiblePhrases = phrases.filter((p) => p.category === phraseTab);
  const visibleTerms = specialTerms.filter((t) => t.termCategory === termTab);

  return (
    <div className="phrase-book">
      <div className="phrase-book__book-tabs">
        <button
          className={bookTab === "phrases" ? "active" : ""}
          onClick={() => setBookTab("phrases")}
          type="button"
        >
          常用短语 Phrases
        </button>
        <button
          className={bookTab === "terms" ? "active" : ""}
          onClick={() => setBookTab("terms")}
          type="button"
        >
          特殊词语 Special Terms
        </button>
      </div>

      {bookTab === "phrases" && (
        <>
          <div className="phrase-book__category-tabs" role="tablist" aria-label="短语分类">
            {(Object.keys(PHRASE_CATEGORY_LABELS) as PhraseCategory[]).map((cat) => (
              <button
                aria-selected={cat === phraseTab}
                className={cat === phraseTab ? "active" : ""}
                key={cat}
                onClick={() => setPhraseTab(cat)}
                role="tab"
                type="button"
              >
                {PHRASE_CATEGORY_LABELS[cat]}
              </button>
            ))}
          </div>

          <ul className="phrase-book__list" aria-label={PHRASE_CATEGORY_LABELS[phraseTab]}>
            {visiblePhrases.map((phrase) => (
              <li className="phrase-book__item" key={phrase.id}>
                <div className="phrase-book__item-main">
                  <span className="phrase-book__chinese">{phrase.chinese}</span>
                  <span className="phrase-book__pinyin">{phrase.pinyin}</span>
                  <span className="phrase-book__english">{phrase.english}</span>
                </div>
                {phrase.notes && <p className="phrase-book__notes">{phrase.notes}</p>}
                <button
                  aria-label={`朗读 ${phrase.chinese}`}
                  className="phrase-book__speak"
                  onClick={() => speak(phrase.chinese)}
                  type="button"
                >
                  🔊
                </button>
              </li>
            ))}
          </ul>
        </>
      )}

      {bookTab === "terms" && (
        <>
          <div className="phrase-book__category-tabs" role="tablist" aria-label="词语分类">
            {(Object.keys(SPECIAL_TERM_CATEGORY_LABELS) as SpecialTermCategory[]).map((cat) => (
              <button
                aria-selected={cat === termTab}
                className={cat === termTab ? "active" : ""}
                key={cat}
                onClick={() => setTermTab(cat)}
                role="tab"
                type="button"
              >
                {SPECIAL_TERM_CATEGORY_LABELS[cat]}
              </button>
            ))}
          </div>

          <ul className="phrase-book__list" aria-label={SPECIAL_TERM_CATEGORY_LABELS[termTab]}>
            {visibleTerms.map((term) => (
              <li className="phrase-book__item" key={term.id}>
                <div className="phrase-book__item-main">
                  <span className="phrase-book__chinese">{term.chinese}</span>
                  <span className="phrase-book__pinyin">{term.pinyin}</span>
                  <span className="phrase-book__english">{term.english}</span>
                </div>
                {term.context && <p className="phrase-book__notes">{term.context}</p>}
                <button
                  aria-label={`朗读 ${term.chinese}`}
                  className="phrase-book__speak"
                  onClick={() => speak(term.chinese)}
                  type="button"
                >
                  🔊
                </button>
              </li>
            ))}
          </ul>
        </>
      )}
    </div>
  );
}
