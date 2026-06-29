import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";
import type { ChatMessage, DayCard, RailItem, TripInstructionBlock, TripSummary } from "./types";

const DEFAULT_SUMMARY: TripSummary = {
  route: [],
  startDate: null,
  endDate: null,
  travelers: 1,
  days: 0,
};

interface TripState {
  messages: ChatMessage[];
  days: DayCard[];
  rails: RailItem[];
  summary: TripSummary;
  addMessage: (message: ChatMessage) => void;
  updateMessageContent: (id: string, content: string) => void;
  applyInstructions: (block: TripInstructionBlock) => void;
  reset: () => void;
}

const noopStorage = {
  getItem: () => null,
  setItem: () => {},
  removeItem: () => {},
};

export const useTripStore = create<TripState>()(
  persist(
    (set) => ({
      messages: [],
      days: [],
      rails: [],
      summary: DEFAULT_SUMMARY,

      addMessage: (message) =>
        set((state) => ({ messages: [...state.messages, message] })),

      updateMessageContent: (id, content) =>
        set((state) => ({
          messages: state.messages.map((m) => (m.id === id ? { ...m, content } : m)),
        })),

      applyInstructions: (block) =>
        set((state) => {
          let days = state.days;
          let rails = state.rails;

          for (const instr of block.days ?? []) {
            if (instr.action === "delete") {
              days = days.filter((d) => d.day !== instr.day);
            } else if (instr.data) {
              const card: DayCard = { day: instr.day, ...instr.data };
              const idx = days.findIndex((d) => d.day === instr.day);
              days = idx >= 0 ? days.map((d, i) => (i === idx ? card : d)) : [...days, card];
            }
          }
          days = [...days].sort((a, b) => a.day - b.day);

          for (const instr of block.rails ?? []) {
            if (instr.action === "delete") {
              rails = rails.filter((r) => r.id !== instr.id);
            } else if (instr.data) {
              const item: RailItem = { id: instr.id, ...instr.data };
              const idx = rails.findIndex((r) => r.id === instr.id);
              rails = idx >= 0 ? rails.map((r, i) => (i === idx ? item : r)) : [...rails, item];
            }
          }

          const summary = block.summary ? { ...state.summary, ...block.summary } : state.summary;

          return { days, rails, summary };
        }),

      reset: () => set({ messages: [], days: [], rails: [], summary: DEFAULT_SUMMARY }),
    }),
    {
      name: "visepanda-trip-store",
      storage: createJSONStorage(() =>
        typeof window !== "undefined" ? window.localStorage : noopStorage
      ),
    }
  )
);
