import { describe, expect, it } from "vitest";
import { phrases, specialTerms, PHRASE_CATEGORY_LABELS, SPECIAL_TERM_CATEGORY_LABELS } from "@/lib/translate/phrases";

describe("phrases data", () => {
  it("exports phrases array with required fields", () => {
    expect(phrases.length).toBeGreaterThan(0);
    for (const phrase of phrases) {
      expect(phrase.id).toBeTruthy();
      expect(phrase.english).toBeTruthy();
      expect(phrase.chinese).toBeTruthy();
      expect(phrase.pinyin).toBeTruthy();
      expect(phrase.category).toBeTruthy();
    }
  });

  it("covers all declared categories", () => {
    const categories = new Set(phrases.map((p) => p.category));
    for (const cat of Object.keys(PHRASE_CATEGORY_LABELS)) {
      expect(categories.has(cat as never)).toBe(true);
    }
  });

  it("has at least 6 dining phrases", () => {
    const dining = phrases.filter((p) => p.category === "dining");
    expect(dining.length).toBeGreaterThanOrEqual(6);
  });
});

describe("specialTerms data", () => {
  it("exports specialTerms array with required fields", () => {
    expect(specialTerms.length).toBeGreaterThan(0);
    for (const term of specialTerms) {
      expect(term.id).toBeTruthy();
      expect(term.english).toBeTruthy();
      expect(term.chinese).toBeTruthy();
      expect(term.pinyin).toBeTruthy();
      expect(term.termCategory).toBeTruthy();
    }
  });

  it("covers all declared term categories", () => {
    const cats = new Set(specialTerms.map((t) => t.termCategory));
    for (const cat of Object.keys(SPECIAL_TERM_CATEGORY_LABELS)) {
      expect(cats.has(cat as never)).toBe(true);
    }
  });

  it("includes dishes with context notes", () => {
    const dishes = specialTerms.filter((t) => t.termCategory === "dishes");
    expect(dishes.length).toBeGreaterThanOrEqual(5);
    expect(dishes.every((d) => d.context)).toBe(true);
  });
});
