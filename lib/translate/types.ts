export type PhraseCategory = "greetings" | "dining" | "transport" | "shopping" | "emergency" | "hotel";
export type SpecialTermCategory = "attractions" | "dishes" | "signs";

export interface Phrase {
  id: string;
  english: string;
  chinese: string;
  pinyin: string;
  category: PhraseCategory;
  notes?: string;
}

export interface SpecialTerm {
  id: string;
  english: string;
  chinese: string;
  pinyin: string;
  termCategory: SpecialTermCategory;
  context?: string;
}
