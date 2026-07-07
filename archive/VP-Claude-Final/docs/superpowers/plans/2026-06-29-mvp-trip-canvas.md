# Trip Canvas + Butler Rails MVP Implementation Plan

> **SUPERSEDED 2026-06-29.** The user supplied a concrete reference mockup after this plan was approved but before any task was started. Scope grew to a real 5-route shell (Chat/Trips/Explore/Tools/Account) and the visual treatment changed from "dark mask on full-bleed mountain" to "flat bordered cards on a cream page with a decorative side ornament." Use `docs/superpowers/plans/2026-06-29-mvp-trip-canvas-v2.md` instead. This file is kept for history only — do not execute it.

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the Phase 1 MVP of VisePanda — a single-page AI travel butler workspace where a Day-by-day Trip Canvas (left) and a streaming chat (right) update live from the AI's structured replies, with Butler Rails reminders across the top, all rendered over a fixed warm ink-wash background with no semi-transparent dialog boxes.

**Architecture:** Next.js 14 App Router + TypeScript, single codebase. A Zustand store holds chat messages, Day cards, and Rail items, persisted to `localStorage`. The AI reply is plain conversational text followed by a fenced `json-trip-instructions` block; a pure-function parser extracts and applies it to the store. `/api/chat` proxies DeepSeek when `DEEPSEEK_API_KEY` is set, otherwise falls back to a deterministic keyword-based mock — both paths return the same plain-text streaming contract so the client never needs to know which one served the request.

**Tech Stack:** Next.js 14 (App Router), React 18, TypeScript, Tailwind CSS, Zustand, Vitest (unit tests for logic-only modules).

## Global Constraints

- Repo root for all paths below: `VP-Claude-Final/` (already `git init`'d, remote `origin` set to `JTCAO515/VP-Claude-Final`).
- No backend database. Trip state persists only via Zustand's `persist` middleware to `localStorage`.
- No authentication, no other tabs (Cities/Tools/Trips) — single page only, per `docs/superpowers/specs/2026-06-29-mvp-trip-canvas-design.md`.
- `DEEPSEEK_API_KEY` is read from `process.env` and is allowed to be absent — every code path must work with no key set (mock fallback).
- No semi-transparent card backgrounds anywhere in the UI. All text sits on a gradient dark-mask (`bg-gradient-to-*` from `black/NN` to `transparent`), never on a solid or translucent rectangle with a visible edge.
- TDD is required for logic-only modules: `lib/store.ts`, `lib/parse-instructions.ts`, `lib/mock-ai.ts`, `lib/streaming.ts`. Presentational/component tasks and the API route's integration wiring are verified by `npm run build` and manual browser/`curl` checks instead (per the design doc's reduced MVP testing scope) — do not invent component-render tests for these.
- No ESLint config, no E2E framework — out of scope for this plan.
- Commit after every task with `git add` limited to the files that task touches.

---

### Task 1: Project scaffold

**Files:**
- Create: `package.json`
- Create: `tsconfig.json`
- Create: `next.config.mjs`
- Create: `tailwind.config.ts`
- Create: `postcss.config.mjs`
- Create: `vitest.config.ts`
- Create: `next-env.d.ts`
- Create: `.gitignore`
- Create: `app/globals.css`
- Create: `app/layout.tsx`
- Create: `app/page.tsx`

**Interfaces:**
- Produces: Tailwind theme colors `ink.cream` (`#f0e6d2`), `ink.paper` (`#f7f0e2`), `ink.ochre` (`#9c7d54`), `ink.umber` (`#6e5634`), `ink.cinnabar` (`#a23728`), `ink.gold` (`#b8862c`) — every later component task uses these exact token names. CSS variables `--font-display` / `--font-body`, exposed as Tailwind `font-display` / `font-body`.

- [ ] **Step 1: Write `package.json`**

```json
{
  "name": "vp-claude-final",
  "version": "0.1.0",
  "private": true,
  "scripts": {
    "dev": "next dev",
    "build": "next build",
    "start": "next start",
    "test": "vitest run"
  },
  "dependencies": {
    "next": "^14.2.0",
    "react": "^18.3.0",
    "react-dom": "^18.3.0",
    "zustand": "^4.5.0"
  },
  "devDependencies": {
    "@types/node": "^20.12.0",
    "@types/react": "^18.3.0",
    "@types/react-dom": "^18.3.0",
    "autoprefixer": "^10.4.0",
    "postcss": "^8.4.0",
    "tailwindcss": "^3.4.0",
    "typescript": "^5.4.0",
    "vitest": "^1.6.0"
  }
}
```

- [ ] **Step 2: Write `tsconfig.json`**

```json
{
  "compilerOptions": {
    "target": "ES2017",
    "lib": ["dom", "dom.iterable", "esnext"],
    "allowJs": true,
    "skipLibCheck": true,
    "strict": true,
    "noEmit": true,
    "esModuleInterop": true,
    "module": "esnext",
    "moduleResolution": "bundler",
    "resolveJsonModule": true,
    "isolatedModules": true,
    "jsx": "preserve",
    "incremental": true,
    "plugins": [{ "name": "next" }],
    "paths": { "@/*": ["./*"] }
  },
  "include": ["next-env.d.ts", "**/*.ts", "**/*.tsx", ".next/types/**/*.ts"],
  "exclude": ["node_modules"]
}
```

- [ ] **Step 3: Write `next.config.mjs`**

```javascript
/** @type {import('next').NextConfig} */
const nextConfig = {};
export default nextConfig;
```

- [ ] **Step 4: Write `tailwind.config.ts`**

```typescript
import type { Config } from "tailwindcss";

const config: Config = {
  content: ["./app/**/*.{ts,tsx}", "./components/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        ink: {
          cream: "#f0e6d2",
          paper: "#f7f0e2",
          ochre: "#9c7d54",
          umber: "#6e5634",
          cinnabar: "#a23728",
          gold: "#b8862c",
        },
      },
      fontFamily: {
        display: ["var(--font-display)"],
        body: ["var(--font-body)"],
      },
    },
  },
  plugins: [],
};
export default config;
```

- [ ] **Step 5: Write `postcss.config.mjs`**

```javascript
const config = {
  plugins: {
    tailwindcss: {},
    autoprefixer: {},
  },
};
export default config;
```

- [ ] **Step 6: Write `vitest.config.ts`**

```typescript
import { defineConfig } from "vitest/config";
import path from "path";

export default defineConfig({
  test: {
    environment: "node",
    include: ["lib/**/*.test.ts"],
  },
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "."),
    },
  },
});
```

- [ ] **Step 7: Write `next-env.d.ts`**

```typescript
/// <reference types="next" />
/// <reference types="next/image-types/global" />
```

- [ ] **Step 8: Write `.gitignore`**

```
node_modules
.next
*.tsbuildinfo
.env*.local
```

- [ ] **Step 9: Write `app/globals.css`**

```css
@tailwind base;
@tailwind components;
@tailwind utilities;

:root {
  --font-display: "Caveat", cursive;
  --font-body: ui-sans-serif, system-ui, -apple-system, sans-serif;
}

html,
body {
  height: 100%;
}
```

- [ ] **Step 10: Write `app/layout.tsx`**

```tsx
import type { Metadata } from "next";
import { Caveat } from "next/font/google";
import "./globals.css";

const caveat = Caveat({ subsets: ["latin"], variable: "--font-display" });

export const metadata: Metadata = {
  title: "VisePanda",
  description: "AI travel butler for visiting China",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en" className={caveat.variable}>
      <body>{children}</body>
    </html>
  );
}
```

- [ ] **Step 11: Write `app/page.tsx` (placeholder, replaced in Task 11)**

```tsx
export default function Home() {
  return <main>VisePanda</main>;
}
```

- [ ] **Step 12: Install dependencies**

Run: `npm install`
Expected: installs without errors, creates `package-lock.json` and `node_modules/`.

- [ ] **Step 13: Verify the scaffold builds**

Run: `npm run build`
Expected: `Compiled successfully` and a `.next/` build output, no type errors.

- [ ] **Step 14: Commit**

```bash
git add package.json package-lock.json tsconfig.json next.config.mjs tailwind.config.ts postcss.config.mjs vitest.config.ts next-env.d.ts .gitignore app/globals.css app/layout.tsx app/page.tsx
git commit -m "Scaffold Next.js 14 + TypeScript + Tailwind + Zustand project"
```

---

### Task 2: Core types and Zustand store

**Files:**
- Create: `lib/types.ts`
- Create: `lib/store.ts`
- Test: `lib/store.test.ts`

**Interfaces:**
- Consumes: nothing (foundational).
- Produces: types `ChatMessage`, `DayCard`, `RailItem`, `RailCategory`, `DayInstruction`, `RailInstruction`, `TripInstructionBlock` from `lib/types.ts`. Store hook `useTripStore` with state `{ messages: ChatMessage[], days: DayCard[], rails: RailItem[] }` and actions `addMessage(message: ChatMessage): void`, `updateMessageContent(id: string, content: string): void`, `applyInstructions(block: TripInstructionBlock): void`, `reset(): void`. Every later task that touches trip state imports `useTripStore` from `@/lib/store` and these exact type names from `@/lib/types`.

- [ ] **Step 1: Write `lib/types.ts`**

```typescript
export type RailCategory =
  | "visa"
  | "payment"
  | "hotel"
  | "transport"
  | "weather"
  | "risk"
  | "language"
  | "emergency";

export interface RailItem {
  id: string;
  category: RailCategory;
  title: string;
  detail: string;
  severity: "info" | "warning" | "urgent";
}

export interface DayCard {
  day: number;
  city: string;
  attractions: string[];
  food: string[];
  hotel: string;
  transport: string;
  pace: "relaxed" | "moderate" | "packed";
  budgetNote: string;
}

export interface ChatMessage {
  id: string;
  role: "user" | "assistant";
  content: string;
}

export type CardAction = "upsert" | "delete";

export interface DayInstruction {
  day: number;
  action: CardAction;
  data?: Omit<DayCard, "day">;
}

export interface RailInstruction {
  id: string;
  action: CardAction;
  data?: Omit<RailItem, "id">;
}

export interface TripInstructionBlock {
  days?: DayInstruction[];
  rails?: RailInstruction[];
}
```

- [ ] **Step 2: Write the failing test for the store**

Create `lib/store.test.ts`:

```typescript
import { describe, it, expect, beforeEach } from "vitest";
import { useTripStore } from "./store";

function dayData(city: string) {
  return {
    city,
    attractions: [],
    food: [],
    hotel: "Hotel",
    transport: "Transport",
    pace: "moderate" as const,
    budgetNote: "n/a",
  };
}

describe("useTripStore.applyInstructions", () => {
  beforeEach(() => {
    useTripStore.getState().reset();
  });

  it("inserts a new day card on upsert when none exists", () => {
    useTripStore.getState().applyInstructions({
      days: [
        {
          day: 1,
          action: "upsert",
          data: {
            city: "Beijing",
            attractions: ["Forbidden City"],
            food: ["Peking duck"],
            hotel: "Beijing city-center hotel",
            transport: "Airport transfer",
            pace: "moderate",
            budgetNote: "~$100/day",
          },
        },
      ],
    });

    const days = useTripStore.getState().days;
    expect(days).toHaveLength(1);
    expect(days[0]).toMatchObject({ day: 1, city: "Beijing" });
  });

  it("replaces an existing day card on upsert with the same day number", () => {
    const store = useTripStore.getState();
    store.applyInstructions({
      days: [{ day: 1, action: "upsert", data: dayData("Beijing") }],
    });
    store.applyInstructions({
      days: [
        {
          day: 1,
          action: "upsert",
          data: { ...dayData("Beijing"), hotel: "Beijing hotel (near metro)", pace: "relaxed" },
        },
      ],
    });

    const days = useTripStore.getState().days;
    expect(days).toHaveLength(1);
    expect(days[0].pace).toBe("relaxed");
    expect(days[0].hotel).toContain("near metro");
  });

  it("removes a day card on delete", () => {
    const store = useTripStore.getState();
    store.applyInstructions({
      days: [{ day: 1, action: "upsert", data: dayData("Beijing") }],
    });
    store.applyInstructions({ days: [{ day: 1, action: "delete" }] });

    expect(useTripStore.getState().days).toHaveLength(0);
  });

  it("keeps day cards sorted by day number regardless of insertion order", () => {
    const store = useTripStore.getState();
    store.applyInstructions({
      days: [
        { day: 3, action: "upsert", data: dayData("Hangzhou") },
        { day: 1, action: "upsert", data: dayData("Beijing") },
        { day: 2, action: "upsert", data: dayData("Shanghai") },
      ],
    });

    expect(useTripStore.getState().days.map((d) => d.day)).toEqual([1, 2, 3]);
  });

  it("upserts and deletes rail items by id", () => {
    const store = useTripStore.getState();
    store.applyInstructions({
      rails: [
        {
          id: "visa-check",
          action: "upsert",
          data: {
            category: "visa",
            title: "Visa check",
            detail: "Confirm visa-free transit eligibility.",
            severity: "warning",
          },
        },
      ],
    });
    expect(useTripStore.getState().rails).toHaveLength(1);

    store.applyInstructions({ rails: [{ id: "visa-check", action: "delete" }] });
    expect(useTripStore.getState().rails).toHaveLength(0);
  });
});
```

- [ ] **Step 3: Run the test and verify it fails**

Run: `npx vitest run lib/store.test.ts`
Expected: FAIL — `Cannot find module './store'` (it doesn't exist yet).

- [ ] **Step 4: Write `lib/store.ts`**

```typescript
import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";
import type { ChatMessage, DayCard, RailItem, TripInstructionBlock } from "./types";

interface TripState {
  messages: ChatMessage[];
  days: DayCard[];
  rails: RailItem[];
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

          return { days, rails };
        }),

      reset: () => set({ messages: [], days: [], rails: [] }),
    }),
    {
      name: "visepanda-trip-store",
      storage: createJSONStorage(() =>
        typeof window !== "undefined" ? window.localStorage : noopStorage
      ),
    }
  )
);
```

- [ ] **Step 5: Run the test and verify it passes**

Run: `npx vitest run lib/store.test.ts`
Expected: PASS — all 5 tests green.

- [ ] **Step 6: Commit**

```bash
git add lib/types.ts lib/store.ts lib/store.test.ts
git commit -m "Add trip types and Zustand store with instruction reducers"
```

---

### Task 3: Instruction block parser

**Files:**
- Create: `lib/parse-instructions.ts`
- Test: `lib/parse-instructions.test.ts`

**Interfaces:**
- Consumes: `TripInstructionBlock` from `@/lib/types` (Task 2).
- Produces: `parseAssistantReply(raw: string): { chatText: string; instructions: TripInstructionBlock | null }`. Tasks 6 and 10 call this exact function with the exact raw assistant text (partial or complete) and rely on `chatText` always being safe to render even when `instructions` is `null`.

- [ ] **Step 1: Write the failing tests**

Create `lib/parse-instructions.test.ts`:

```typescript
import { describe, it, expect } from "vitest";
import { parseAssistantReply } from "./parse-instructions";

describe("parseAssistantReply", () => {
  it("returns the trimmed text unchanged when there is no instruction block", () => {
    const result = parseAssistantReply("  Hello, where would you like to go?  ");
    expect(result).toEqual({
      chatText: "Hello, where would you like to go?",
      instructions: null,
    });
  });

  it("extracts a valid instruction block and strips it from the chat text", () => {
    const raw = [
      "Here is your 1-day Beijing plan.",
      "",
      "```json-trip-instructions",
      JSON.stringify({ days: [{ day: 1, action: "upsert", data: { city: "Beijing" } }] }),
      "```",
    ].join("\n");

    const result = parseAssistantReply(raw);

    expect(result.chatText).toBe("Here is your 1-day Beijing plan.");
    expect(result.instructions).toEqual({
      days: [{ day: 1, action: "upsert", data: { city: "Beijing" } }],
      rails: undefined,
    });
  });

  it("falls back to null instructions when the JSON is malformed, but still strips the block", () => {
    const raw = ["Here is your plan.", "```json-trip-instructions", "{ this is not valid json", "```"].join("\n");

    const result = parseAssistantReply(raw);

    expect(result.chatText).toBe("Here is your plan.");
    expect(result.instructions).toBeNull();
  });

  it("falls back to null instructions when the parsed JSON is not an object", () => {
    const raw = ["Some text.", "```json-trip-instructions", "[1, 2, 3]", "```"].join("\n");

    const result = parseAssistantReply(raw);

    expect(result.chatText).toBe("Some text.");
    expect(result.instructions).toBeNull();
  });

  it("treats missing days/rails arrays as undefined rather than throwing", () => {
    const raw = ["No changes this turn.", "```json-trip-instructions", "{}", "```"].join("\n");

    const result = parseAssistantReply(raw);

    expect(result.instructions).toEqual({ days: undefined, rails: undefined });
  });
});
```

- [ ] **Step 2: Run the tests and verify they fail**

Run: `npx vitest run lib/parse-instructions.test.ts`
Expected: FAIL — `Cannot find module './parse-instructions'`.

- [ ] **Step 3: Write `lib/parse-instructions.ts`**

```typescript
import type { TripInstructionBlock } from "./types";

const INSTRUCTION_FENCE = /```json-trip-instructions\s*([\s\S]*?)```/;

export interface ParsedReply {
  chatText: string;
  instructions: TripInstructionBlock | null;
}

export function parseAssistantReply(raw: string): ParsedReply {
  const match = raw.match(INSTRUCTION_FENCE);
  if (!match) {
    return { chatText: raw.trim(), instructions: null };
  }

  const chatText = raw.replace(match[0], "").trim();
  const jsonText = match[1].trim();

  try {
    const parsed = JSON.parse(jsonText);
    if (typeof parsed !== "object" || parsed === null || Array.isArray(parsed)) {
      return { chatText, instructions: null };
    }
    const days = Array.isArray(parsed.days) ? parsed.days : undefined;
    const rails = Array.isArray(parsed.rails) ? parsed.rails : undefined;
    return { chatText, instructions: { days, rails } };
  } catch {
    return { chatText, instructions: null };
  }
}
```

- [ ] **Step 4: Run the tests and verify they pass**

Run: `npx vitest run lib/parse-instructions.test.ts`
Expected: PASS — all 5 tests green.

- [ ] **Step 5: Commit**

```bash
git add lib/parse-instructions.ts lib/parse-instructions.test.ts
git commit -m "Add fault-tolerant parser for trailing trip-instruction JSON blocks"
```

---

### Task 4: Deterministic mock AI engine

**Files:**
- Create: `lib/mock-ai.ts`
- Test: `lib/mock-ai.test.ts`

**Interfaces:**
- Consumes: `DayCard`, `DayInstruction`, `RailInstruction`, `TripInstructionBlock` from `@/lib/types` (Task 2).
- Produces: `generateMockReply(userText: string, existingDays: DayCard[]): { chatText: string; instructions: TripInstructionBlock }`. Task 6 calls this with the latest user message and the current `days` array from the request body.

- [ ] **Step 1: Write the failing tests**

Create `lib/mock-ai.test.ts`:

```typescript
import { describe, it, expect } from "vitest";
import { generateMockReply } from "./mock-ai";

describe("generateMockReply", () => {
  it("asks a clarifying question when no city, day count, or pace signal is present", () => {
    const result = generateMockReply("Hi there!", []);
    expect(result.instructions).toEqual({});
    expect(result.chatText).toMatch(/cities|days/i);
  });

  it("builds a multi-day, multi-city itinerary with butler rails from a first message", () => {
    const result = generateMockReply(
      "I'm coming from the US, first time in China, 5 days, want to go to Beijing and Shanghai.",
      []
    );

    expect(result.instructions.days).toHaveLength(5);
    expect(result.instructions.days?.[0].data?.city).toBe("Beijing");
    expect(result.instructions.days?.[1].data?.city).toBe("Shanghai");

    const railIds = result.instructions.rails?.map((r) => r.id) ?? [];
    expect(railIds).toContain("visa-check");
    expect(railIds).toContain("payment-setup");
    expect(railIds).toContain("intercity-transport");
  });

  it("lightens the pace and adjusts hotel notes when the user asks for less tiring days", () => {
    const existingDays = [
      {
        day: 1,
        city: "Beijing",
        attractions: ["Forbidden City", "Great Wall (Mutianyu)"],
        food: ["Peking duck"],
        hotel: "Beijing city-center hotel",
        transport: "Airport transfer",
        pace: "moderate" as const,
        budgetNote: "~$80-120/day estimated",
      },
    ];

    const result = generateMockReply(
      "I don't want to be too tired, hotels should be convenient.",
      existingDays
    );

    expect(result.instructions.days?.[0].data?.pace).toBe("relaxed");
    expect(result.instructions.days?.[0].data?.attractions).toHaveLength(1);
    expect(result.instructions.days?.[0].data?.hotel).toContain("metro");
  });
});
```

- [ ] **Step 2: Run the tests and verify they fail**

Run: `npx vitest run lib/mock-ai.test.ts`
Expected: FAIL — `Cannot find module './mock-ai'`.

- [ ] **Step 3: Write `lib/mock-ai.ts`**

```typescript
import type { DayCard, DayInstruction, RailInstruction, TripInstructionBlock } from "./types";

const KNOWN_CITIES = ["Beijing", "Shanghai", "Chengdu", "Xi'an", "Guangzhou", "Hangzhou"];

const CITY_HIGHLIGHTS: Record<string, { attractions: string[]; food: string[] }> = {
  Beijing: {
    attractions: ["Forbidden City", "Great Wall (Mutianyu)", "Temple of Heaven"],
    food: ["Peking duck", "Zhajiangmian noodles"],
  },
  Shanghai: {
    attractions: ["The Bund", "Yu Garden", "Nanjing Road"],
    food: ["Xiaolongbao", "Shanghai-style braised pork"],
  },
  Chengdu: {
    attractions: ["Giant Panda Base", "Jinli Ancient Street"],
    food: ["Hotpot", "Mapo tofu"],
  },
  "Xi'an": {
    attractions: ["Terracotta Army", "Ancient City Wall"],
    food: ["Roujiamo", "Biangbiang noodles"],
  },
  Guangzhou: {
    attractions: ["Canton Tower", "Shamian Island"],
    food: ["Dim sum", "Cantonese roast goose"],
  },
  Hangzhou: {
    attractions: ["West Lake", "Lingyin Temple"],
    food: ["Longjing tea", "Beggar's chicken"],
  },
};

function extractCities(text: string): string[] {
  return KNOWN_CITIES.filter((city) => text.toLowerCase().includes(city.toLowerCase()));
}

function extractDayCount(text: string): number | null {
  const match = text.match(/(\d+)\s*-?\s*day/i);
  return match ? parseInt(match[1], 10) : null;
}

function detectRelaxedPace(text: string): boolean {
  return /tired|relax|slow|easy pace|not too much/i.test(text);
}

export interface MockReply {
  chatText: string;
  instructions: TripInstructionBlock;
}

export function generateMockReply(userText: string, existingDays: DayCard[]): MockReply {
  const cities = extractCities(userText);
  const dayCount = extractDayCount(userText);
  const relaxed = detectRelaxedPace(userText);

  if (cities.length === 0 && dayCount === null && !relaxed) {
    return {
      chatText:
        "Tell me which cities you'd like to visit and how many days you have, and I'll start building your itinerary.",
      instructions: {},
    };
  }

  if (relaxed && existingDays.length > 0) {
    const days: DayInstruction[] = existingDays.map((d) => ({
      day: d.day,
      action: "upsert",
      data: {
        city: d.city,
        attractions: d.attractions.slice(0, 1),
        food: d.food,
        hotel: `${d.hotel} (near metro, English-friendly front desk)`,
        transport: d.transport,
        pace: "relaxed",
        budgetNote: d.budgetNote,
      },
    }));
    return {
      chatText: "Got it — I've lightened the pace and prioritized hotels near the metro with English-friendly service.",
      instructions: { days },
    };
  }

  const totalDays = dayCount ?? 5;
  const targetCities = cities.length > 0 ? cities : ["Beijing"];
  const days: DayInstruction[] = [];
  for (let day = 1; day <= totalDays; day++) {
    const city = targetCities[(day - 1) % targetCities.length];
    const highlight = CITY_HIGHLIGHTS[city] ?? {
      attractions: ["City center walk"],
      food: ["Local specialties"],
    };
    days.push({
      day,
      action: "upsert",
      data: {
        city,
        attractions: highlight.attractions.slice(0, 2),
        food: highlight.food.slice(0, 1),
        hotel: `${city} city-center hotel`,
        transport: day === 1 ? "Airport transfer" : "Metro / high-speed rail",
        pace: "moderate",
        budgetNote: "~$80-120/day estimated",
      },
    });
  }

  const rails: RailInstruction[] = [
    {
      id: "visa-check",
      action: "upsert",
      data: {
        category: "visa",
        title: "Visa check",
        detail: "Confirm visa-free transit eligibility or apply for a tourist visa before departure.",
        severity: "warning",
      },
    },
    {
      id: "payment-setup",
      action: "upsert",
      data: {
        category: "payment",
        title: "Payment setup",
        detail: "Link an international card to Alipay/WeChat Pay before you arrive.",
        severity: "info",
      },
    },
  ];

  if (targetCities.length > 1) {
    rails.push({
      id: "intercity-transport",
      action: "upsert",
      data: {
        category: "transport",
        title: "Intercity transport",
        detail: `Book high-speed rail or flights between ${targetCities.join(" and ")} in advance.`,
        severity: "info",
      },
    });
  }

  return {
    chatText: `I've put together a ${totalDays}-day route across ${targetCities.join(
      ", "
    )}. Check the canvas on the left — let me know if you want a slower pace or different cities.`,
    instructions: { days, rails },
  };
}
```

- [ ] **Step 4: Run the tests and verify they pass**

Run: `npx vitest run lib/mock-ai.test.ts`
Expected: PASS — all 3 tests green.

- [ ] **Step 5: Commit**

```bash
git add lib/mock-ai.ts lib/mock-ai.test.ts
git commit -m "Add deterministic keyword-based mock AI engine"
```

---

### Task 5: Streaming helpers

**Files:**
- Create: `lib/streaming.ts`
- Test: `lib/streaming.test.ts`

**Interfaces:**
- Consumes: nothing beyond Web Streams APIs (available in the Next.js Node runtime).
- Produces: `textToChunkStream(text: string, delayMs?: number): ReadableStream<Uint8Array>` and `deepSeekSseToTextStream(upstream: ReadableStream<Uint8Array>): ReadableStream<Uint8Array>`. Task 6 uses both to give the mock path and the real DeepSeek path the same plain-text streaming contract.

- [ ] **Step 1: Write the failing tests**

Create `lib/streaming.test.ts`:

```typescript
import { describe, it, expect } from "vitest";
import { textToChunkStream, deepSeekSseToTextStream } from "./streaming";

async function readAllText(stream: ReadableStream<Uint8Array>): Promise<string> {
  const reader = stream.getReader();
  const decoder = new TextDecoder();
  let result = "";
  while (true) {
    const { done, value } = await reader.read();
    if (done) break;
    result += decoder.decode(value, { stream: true });
  }
  return result;
}

function stringChunksToStream(chunks: string[]): ReadableStream<Uint8Array> {
  const encoder = new TextEncoder();
  let index = 0;
  return new ReadableStream<Uint8Array>({
    pull(controller) {
      if (index >= chunks.length) {
        controller.close();
        return;
      }
      controller.enqueue(encoder.encode(chunks[index]));
      index += 1;
    },
  });
}

describe("textToChunkStream", () => {
  it("reconstructs the original text when all chunks are read", async () => {
    const original = "Here is your 5-day itinerary across Beijing and Shanghai.";
    const text = await readAllText(textToChunkStream(original, 0));
    expect(text).toBe(original);
  });
});

describe("deepSeekSseToTextStream", () => {
  it("extracts delta content across multiple SSE events", async () => {
    const sse = [
      'data: {"choices":[{"delta":{"content":"Hello"}}]}\n\n',
      'data: {"choices":[{"delta":{"content":", world"}}]}\n\n',
      "data: [DONE]\n\n",
    ];
    const text = await readAllText(deepSeekSseToTextStream(stringChunksToStream(sse)));
    expect(text).toBe("Hello, world");
  });

  it("ignores malformed SSE lines instead of throwing", async () => {
    const sse = ["data: not json\n\n", 'data: {"choices":[{"delta":{"content":"ok"}}]}\n\n'];
    const text = await readAllText(deepSeekSseToTextStream(stringChunksToStream(sse)));
    expect(text).toBe("ok");
  });

  it("handles an SSE event split across two underlying chunks", async () => {
    const sse = ['data: {"choices":[{"delta"', ':{"content":"split"}}]}\n\n'];
    const text = await readAllText(deepSeekSseToTextStream(stringChunksToStream(sse)));
    expect(text).toBe("split");
  });
});
```

- [ ] **Step 2: Run the tests and verify they fail**

Run: `npx vitest run lib/streaming.test.ts`
Expected: FAIL — `Cannot find module './streaming'`.

- [ ] **Step 3: Write `lib/streaming.ts`**

```typescript
export function textToChunkStream(text: string, delayMs = 25): ReadableStream<Uint8Array> {
  const words = text.split(/(\s+)/).filter((w) => w.length > 0);
  const encoder = new TextEncoder();
  let index = 0;

  return new ReadableStream<Uint8Array>({
    async pull(controller) {
      if (index >= words.length) {
        controller.close();
        return;
      }
      controller.enqueue(encoder.encode(words[index]));
      index += 1;
      if (delayMs > 0) {
        await new Promise((resolve) => setTimeout(resolve, delayMs));
      }
    },
  });
}

export function deepSeekSseToTextStream(
  upstream: ReadableStream<Uint8Array>
): ReadableStream<Uint8Array> {
  const reader = upstream.getReader();
  const decoder = new TextDecoder();
  const encoder = new TextEncoder();
  let buffer = "";

  return new ReadableStream<Uint8Array>({
    async pull(controller) {
      const { done, value } = await reader.read();
      if (done) {
        controller.close();
        return;
      }
      buffer += decoder.decode(value, { stream: true });
      const lines = buffer.split("\n");
      buffer = lines.pop() ?? "";

      for (const line of lines) {
        const trimmed = line.trim();
        if (!trimmed.startsWith("data:")) continue;
        const payload = trimmed.slice("data:".length).trim();
        if (payload === "[DONE]") continue;
        try {
          const json = JSON.parse(payload);
          const delta = json.choices?.[0]?.delta?.content;
          if (typeof delta === "string" && delta.length > 0) {
            controller.enqueue(encoder.encode(delta));
          }
        } catch {
          // Skip malformed SSE lines rather than failing the stream.
        }
      }
    },
  });
}
```

- [ ] **Step 4: Run the tests and verify they pass**

Run: `npx vitest run lib/streaming.test.ts`
Expected: PASS — all 4 tests green.

- [ ] **Step 5: Commit**

```bash
git add lib/streaming.ts lib/streaming.test.ts
git commit -m "Add streaming helpers shared by mock and DeepSeek chat paths"
```

---

### Task 6: Chat API route

**Files:**
- Create: `app/api/chat/route.ts`
- Create: `.env.example`

**Interfaces:**
- Consumes: `generateMockReply` from `@/lib/mock-ai` (Task 4), `deepSeekSseToTextStream` + `textToChunkStream` from `@/lib/streaming` (Task 5), `ChatMessage` + `DayCard` from `@/lib/types` (Task 2).
- Produces: `POST /api/chat` accepting JSON body `{ messages: ChatMessage[]; days: DayCard[] }` and returning a `200` response whose body is a plain-text (`text/plain; charset=utf-8`) stream — conversational text optionally followed by a ```` ```json-trip-instructions ```` fenced block. Task 10's `sendUserMessage` reads this exact response shape.

- [ ] **Step 1: Write `.env.example`**

```
DEEPSEEK_API_KEY=
```

- [ ] **Step 2: Write `app/api/chat/route.ts`**

```typescript
import { NextRequest } from "next/server";
import { generateMockReply } from "@/lib/mock-ai";
import { deepSeekSseToTextStream, textToChunkStream } from "@/lib/streaming";
import type { ChatMessage, DayCard } from "@/lib/types";

export const runtime = "nodejs";

interface ChatRequestBody {
  messages: ChatMessage[];
  days: DayCard[];
}

function buildSystemPrompt(): string {
  return [
    "You are VisePanda, an AI travel butler for foreign tourists visiting China.",
    "Always answer the traveler's question in plain conversational text first.",
    "Then, only if this turn should change the itinerary, append a fenced code block",
    'tagged exactly ```json-trip-instructions``` containing a single JSON object',
    'with optional "days" and "rails" arrays describing upserts/deletes to the itinerary canvas.',
    "Each day instruction has shape {day, action: 'upsert'|'delete', data?: {city, attractions, food, hotel, transport, pace, budgetNote}}.",
    "Each rail instruction has shape {id, action: 'upsert'|'delete', data?: {category, title, detail, severity}}.",
    "Omit the code block entirely when nothing about the itinerary should change this turn.",
  ].join(" ");
}

function mockResponseStream(lastUserMessage: string, days: DayCard[]): ReadableStream<Uint8Array> {
  const mock = generateMockReply(lastUserMessage, days);
  const fenced = `${mock.chatText}\n\n\`\`\`json-trip-instructions\n${JSON.stringify(mock.instructions)}\n\`\`\``;
  return textToChunkStream(fenced);
}

export async function POST(req: NextRequest) {
  const body = (await req.json()) as ChatRequestBody;
  const lastUserMessage = body.messages[body.messages.length - 1]?.content ?? "";
  const apiKey = process.env.DEEPSEEK_API_KEY;

  if (!apiKey) {
    return new Response(mockResponseStream(lastUserMessage, body.days), {
      headers: { "Content-Type": "text/plain; charset=utf-8" },
    });
  }

  const upstream = await fetch("https://api.deepseek.com/chat/completions", {
    method: "POST",
    headers: {
      Authorization: `Bearer ${apiKey}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      model: "deepseek-chat",
      stream: true,
      messages: [
        { role: "system", content: buildSystemPrompt() },
        ...body.messages.map((m) => ({ role: m.role, content: m.content })),
      ],
    }),
  });

  if (!upstream.ok || !upstream.body) {
    return new Response(mockResponseStream(lastUserMessage, body.days), {
      headers: { "Content-Type": "text/plain; charset=utf-8" },
    });
  }

  return new Response(deepSeekSseToTextStream(upstream.body), {
    headers: { "Content-Type": "text/plain; charset=utf-8" },
  });
}
```

- [ ] **Step 3: Manually verify the mock path (no API key set)**

Run: `npm run dev` (leave running in one terminal)

In another terminal, run:

```bash
curl -N -X POST http://localhost:3000/api/chat \
  -H "Content-Type: application/json" \
  -d '{"messages":[{"id":"1","role":"user","content":"5 days, Beijing and Shanghai"}],"days":[]}'
```

Expected: streamed plain text ending with a ```` ```json-trip-instructions ```` fenced block containing 5 day entries alternating Beijing/Shanghai, plus `visa-check`, `payment-setup`, and `intercity-transport` rail entries (since `DEEPSEEK_API_KEY` is unset, this exercises the mock path from Task 4).

- [ ] **Step 4: Commit**

```bash
git add app/api/chat/route.ts .env.example
git commit -m "Add /api/chat route with DeepSeek streaming and mock fallback"
```

---

### Task 7: Ink-wash background layer

**Files:**
- Create: `components/BackgroundLayer.tsx`

**Interfaces:**
- Consumes: Tailwind tokens from Task 1 (`ink.cream`).
- Produces: `<BackgroundLayer />` — a fixed, full-viewport background with `-z-10`. Task 11's `Workspace` renders this once at the root, behind everything else.

- [ ] **Step 1: Write `components/BackgroundLayer.tsx`**

```tsx
export function BackgroundLayer() {
  return (
    <div
      aria-hidden
      className="fixed inset-0 -z-10 bg-gradient-to-b from-ink-cream via-[#e3d3b0] to-[#cbb487]"
    >
      <svg className="absolute bottom-0 h-2/3 w-full" viewBox="0 0 400 160" preserveAspectRatio="none">
        <path d="M0,130 Q70,70 140,110 T280,90 T400,120 L400,160 L0,160 Z" fill="#9c7d54" opacity="0.65" />
        <path d="M0,145 Q90,110 180,135 T400,140 L400,160 L0,160 Z" fill="#6e5634" opacity="0.6" />
        <circle cx="330" cy="45" r="22" fill="#c9522f" opacity="0.5" />
      </svg>
    </div>
  );
}
```

This is a code-faithful port of the "medium ink wash" mockup approved in brainstorming (option B: warm gradient, distant mountain silhouettes, sun/seal accent, ~60% negative space). It is a placeholder for a real commissioned illustration — swapping it for a `<img>`/`background-image` later requires no changes to any consumer, since `BackgroundLayer` has no props.

- [ ] **Step 2: Manually verify**

Run: `npm run dev`, visit `http://localhost:3000`. Expected: no visible output change yet (nothing renders it until Task 11), but confirm `npm run build` still succeeds with the new component present and unused-export warnings (if any) don't fail the build.

Run: `npm run build`
Expected: `Compiled successfully`.

- [ ] **Step 3: Commit**

```bash
git add components/BackgroundLayer.tsx
git commit -m "Add ink-wash background layer component"
```

---

### Task 8: Butler Rails bar

**Files:**
- Create: `components/ButlerRails.tsx`

**Interfaces:**
- Consumes: `useTripStore` from `@/lib/store` (Task 2), reads `state.rails: RailItem[]`.
- Produces: `<ButlerRails />` — renders `null` when there are no rails, otherwise a horizontally-scrolling row of dark-mask reminder cards. Task 11's `Workspace` renders this once, above the canvas/chat split.

- [ ] **Step 1: Write `components/ButlerRails.tsx`**

```tsx
"use client";

import { useTripStore } from "@/lib/store";
import type { RailItem } from "@/lib/types";

const SEVERITY_GRADIENT: Record<RailItem["severity"], string> = {
  info: "from-black/30",
  warning: "from-black/45",
  urgent: "from-black/60",
};

export function ButlerRails() {
  const rails = useTripStore((state) => state.rails);

  if (rails.length === 0) {
    return null;
  }

  return (
    <div className="flex gap-3 overflow-x-auto px-4 py-3">
      {rails.map((rail) => (
        <div
          key={rail.id}
          className={`min-w-[220px] flex-shrink-0 rounded bg-gradient-to-r ${SEVERITY_GRADIENT[rail.severity]} to-transparent px-3 py-2`}
        >
          <p className="font-display text-lg text-ink-paper">{rail.title}</p>
          <p className="font-body text-sm text-ink-paper/90">{rail.detail}</p>
        </div>
      ))}
    </div>
  );
}
```

- [ ] **Step 2: Manually verify**

Run: `npm run build`
Expected: `Compiled successfully`. (Visual verification happens in Task 12 once the page is fully wired.)

- [ ] **Step 3: Commit**

```bash
git add components/ButlerRails.tsx
git commit -m "Add Butler Rails reminder bar component"
```

---

### Task 9: Trip Canvas and Day cards

**Files:**
- Create: `components/DayCard.tsx`
- Create: `components/TripCanvas.tsx`

**Interfaces:**
- Consumes: `useTripStore` from `@/lib/store` (Task 2), `DayCard` type from `@/lib/types` (Task 2).
- Produces: `<DayCard day={DayCard} />` and `<TripCanvas />` (reads `state.days` itself, no props). Task 11's `Workspace` and Task 11's `MobileCanvasSheet` both render `<TripCanvas />` directly.

- [ ] **Step 1: Write `components/DayCard.tsx`**

```tsx
import type { DayCard as DayCardType } from "@/lib/types";

export function DayCard({ day }: { day: DayCardType }) {
  return (
    <div className="relative mb-4 rounded bg-gradient-to-b from-black/40 via-black/20 to-transparent p-4">
      <p className="font-display text-2xl text-ink-paper">
        Day {day.day} · {day.city}
      </p>
      <p className="mt-1 text-sm text-ink-paper/90">
        <span className="font-semibold">Attractions:</span> {day.attractions.join(", ") || "—"}
      </p>
      <p className="mt-1 text-sm text-ink-paper/90">
        <span className="font-semibold">Food:</span> {day.food.join(", ") || "—"}
      </p>
      <p className="mt-1 text-sm text-ink-paper/90">
        <span className="font-semibold">Hotel:</span> {day.hotel}
      </p>
      <p className="mt-1 text-sm text-ink-paper/90">
        <span className="font-semibold">Transport:</span> {day.transport}
      </p>
      <p className="mt-1 text-sm text-ink-paper/90">
        <span className="font-semibold">Pace:</span> {day.pace}
      </p>
      <p className="mt-1 text-xs text-ink-paper/70">{day.budgetNote}</p>
    </div>
  );
}
```

- [ ] **Step 2: Write `components/TripCanvas.tsx`**

```tsx
"use client";

import { useTripStore } from "@/lib/store";
import { DayCard } from "./DayCard";

export function TripCanvas() {
  const days = useTripStore((state) => state.days);

  if (days.length === 0) {
    return (
      <div className="flex h-full items-center justify-center px-6 text-center text-ink-paper/80">
        <p className="font-body text-sm">
          Tell me where you&apos;re headed and how many days you have — your itinerary will appear here.
        </p>
      </div>
    );
  }

  return (
    <div className="h-full overflow-y-auto px-4 py-4">
      {days.map((day) => (
        <DayCard key={day.day} day={day} />
      ))}
    </div>
  );
}
```

- [ ] **Step 3: Manually verify**

Run: `npm run build`
Expected: `Compiled successfully`.

- [ ] **Step 4: Commit**

```bash
git add components/DayCard.tsx components/TripCanvas.tsx
git commit -m "Add Trip Canvas and Day card components"
```

---

### Task 10: Chat panel and message sending

**Files:**
- Create: `components/ChatMessage.tsx`
- Create: `lib/send-message.ts`
- Create: `components/ChatPanel.tsx`

**Interfaces:**
- Consumes: `useTripStore` from `@/lib/store` (Task 2), `parseAssistantReply` from `@/lib/parse-instructions` (Task 3), `ChatMessage` type from `@/lib/types` (Task 2).
- Produces: `sendUserMessage(content: string): Promise<void>` (posts to `/api/chat` from Task 6, streams the response into the store) and `<ChatPanel />` (no props — reads `state.messages` itself). Task 11's `Workspace` and `MobileCanvasSheet`-adjacent layout both render `<ChatPanel />` directly.

- [ ] **Step 1: Write `components/ChatMessage.tsx`**

```tsx
import type { ChatMessage as ChatMessageType } from "@/lib/types";

export function ChatMessage({ message }: { message: ChatMessageType }) {
  const isUser = message.role === "user";
  return (
    <div
      className={`mb-3 max-w-[85%] rounded px-3 py-2 ${
        isUser
          ? "ml-auto bg-gradient-to-l from-black/25 to-transparent text-right"
          : "mr-auto bg-gradient-to-r from-black/40 to-transparent"
      }`}
    >
      <p className="whitespace-pre-wrap text-sm text-ink-paper">{message.content}</p>
    </div>
  );
}
```

- [ ] **Step 2: Write `lib/send-message.ts`**

```typescript
import { parseAssistantReply } from "./parse-instructions";
import { useTripStore } from "./store";
import type { ChatMessage } from "./types";

export async function sendUserMessage(content: string): Promise<void> {
  const store = useTripStore.getState();
  const userMessage: ChatMessage = { id: crypto.randomUUID(), role: "user", content };
  const assistantMessage: ChatMessage = { id: crypto.randomUUID(), role: "assistant", content: "" };

  store.addMessage(userMessage);
  store.addMessage(assistantMessage);

  const response = await fetch("/api/chat", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      messages: [...store.messages, userMessage],
      days: store.days,
    }),
  });

  if (!response.body) {
    return;
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let fullText = "";

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;
    fullText += decoder.decode(value, { stream: true });
    const { chatText } = parseAssistantReply(fullText);
    useTripStore.getState().updateMessageContent(assistantMessage.id, chatText);
  }

  const { chatText, instructions } = parseAssistantReply(fullText);
  useTripStore.getState().updateMessageContent(assistantMessage.id, chatText);
  if (instructions) {
    useTripStore.getState().applyInstructions(instructions);
  }
}
```

Note: during streaming, `fullText` is partial and never contains the closing ```` ``` ```` fence yet, so `parseAssistantReply` reliably falls through to its no-match branch and returns the growing raw text as `chatText` — this is intentional and is what makes the chat feel like it is streaming.

- [ ] **Step 3: Write `components/ChatPanel.tsx`**

```tsx
"use client";

import { useState } from "react";
import { useTripStore } from "@/lib/store";
import { sendUserMessage } from "@/lib/send-message";
import { ChatMessage } from "./ChatMessage";

export function ChatPanel() {
  const messages = useTripStore((state) => state.messages);
  const [input, setInput] = useState("");
  const [sending, setSending] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    const content = input.trim();
    if (!content || sending) return;
    setInput("");
    setSending(true);
    try {
      await sendUserMessage(content);
    } finally {
      setSending(false);
    }
  }

  return (
    <div className="flex h-full flex-col">
      <div className="flex-1 overflow-y-auto px-4 py-4">
        {messages.length === 0 ? (
          <p className="text-sm text-ink-paper/80">
            Tell me where you&apos;re from, where you want to go, and how many days you have.
          </p>
        ) : (
          messages.map((message) => <ChatMessage key={message.id} message={message} />)
        )}
      </div>
      <form onSubmit={handleSubmit} className="flex gap-2 px-4 py-3">
        <input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Ask your travel butler..."
          className="flex-1 rounded bg-black/20 px-3 py-2 text-sm text-ink-paper placeholder:text-ink-paper/60 focus:outline-none"
        />
        <button
          type="submit"
          disabled={sending}
          className="rounded bg-ink-cinnabar px-4 py-2 text-sm font-semibold text-ink-paper disabled:opacity-50"
        >
          Send
        </button>
      </form>
    </div>
  );
}
```

- [ ] **Step 4: Manually verify**

Run: `npm run build`
Expected: `Compiled successfully`.

- [ ] **Step 5: Commit**

```bash
git add components/ChatMessage.tsx lib/send-message.ts components/ChatPanel.tsx
git commit -m "Add chat panel with streaming send/receive wired to the trip store"
```

---

### Task 11: Workspace layout (desktop split + mobile) and page wiring

**Files:**
- Create: `hooks/useIsMobile.ts`
- Create: `components/MobileCanvasSheet.tsx`
- Create: `components/Workspace.tsx`
- Modify: `app/page.tsx`

**Interfaces:**
- Consumes: `BackgroundLayer` (Task 7), `ButlerRails` (Task 8), `TripCanvas` (Task 9), `ChatPanel` (Task 10).
- Produces: `useIsMobile(): boolean` and `<Workspace />`, the single component rendered by `app/page.tsx`.

- [ ] **Step 1: Write `hooks/useIsMobile.ts`**

```typescript
"use client";

import { useEffect, useState } from "react";

const MOBILE_BREAKPOINT_PX = 768;

export function useIsMobile(): boolean {
  const [isMobile, setIsMobile] = useState(false);

  useEffect(() => {
    const query = window.matchMedia(`(max-width: ${MOBILE_BREAKPOINT_PX - 1}px)`);
    const update = () => setIsMobile(query.matches);
    update();
    query.addEventListener("change", update);
    return () => query.removeEventListener("change", update);
  }, []);

  return isMobile;
}
```

- [ ] **Step 2: Write `components/MobileCanvasSheet.tsx`**

```tsx
"use client";

import { TripCanvas } from "./TripCanvas";

export function MobileCanvasSheet({ open, onClose }: { open: boolean; onClose: () => void }) {
  if (!open) return null;

  return (
    <div className="fixed inset-0 z-20 bg-ink-cream">
      <div className="flex items-center justify-between px-4 py-3">
        <p className="font-display text-xl text-ink-umber">Your itinerary</p>
        <button onClick={onClose} className="text-sm text-ink-umber underline">
          Back to chat
        </button>
      </div>
      <div className="h-[calc(100%-56px)]">
        <TripCanvas />
      </div>
    </div>
  );
}
```

- [ ] **Step 3: Write `components/Workspace.tsx`**

```tsx
"use client";

import { useState } from "react";
import { useIsMobile } from "@/hooks/useIsMobile";
import { BackgroundLayer } from "./BackgroundLayer";
import { ButlerRails } from "./ButlerRails";
import { TripCanvas } from "./TripCanvas";
import { ChatPanel } from "./ChatPanel";
import { MobileCanvasSheet } from "./MobileCanvasSheet";

export function Workspace() {
  const isMobile = useIsMobile();
  const [canvasOpen, setCanvasOpen] = useState(false);

  return (
    <div className="relative h-screen w-screen overflow-hidden">
      <BackgroundLayer />
      <div className="relative flex h-full flex-col">
        <ButlerRails />
        {isMobile ? (
          <div className="relative flex-1">
            <ChatPanel />
            <button
              onClick={() => setCanvasOpen(true)}
              className="absolute bottom-4 right-4 rounded-full bg-ink-cinnabar px-4 py-2 text-sm font-semibold text-ink-paper shadow-lg"
            >
              View itinerary
            </button>
            <MobileCanvasSheet open={canvasOpen} onClose={() => setCanvasOpen(false)} />
          </div>
        ) : (
          <div className="flex flex-1">
            <div className="flex-[1.4] border-r border-ink-paper/10">
              <TripCanvas />
            </div>
            <div className="flex-1">
              <ChatPanel />
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
```

- [ ] **Step 4: Replace `app/page.tsx`**

```tsx
import { Workspace } from "@/components/Workspace";

export default function Home() {
  return <Workspace />;
}
```

- [ ] **Step 5: Manually verify**

Run: `npm run build`
Expected: `Compiled successfully`.

Run: `npm run dev`, open `http://localhost:3000`.
Expected: full-viewport ink-wash background, empty chat panel on the right (or full width on narrow viewports) with the "Tell me where you're from..." prompt, empty Trip Canvas state on the left ("Tell me where you're headed...") on desktop widths, no visible card borders.

- [ ] **Step 6: Commit**

```bash
git add hooks/useIsMobile.ts components/MobileCanvasSheet.tsx components/Workspace.tsx app/page.tsx
git commit -m "Wire desktop split and mobile stacked layouts into the Workspace"
```

---

### Task 12: End-to-end manual verification and README

**Files:**
- Modify: `README.md`

**Interfaces:**
- Consumes: the fully wired app from Tasks 1–11. No new interfaces produced — this task is verification + documentation only.

- [ ] **Step 1: Write `README.md`**

```markdown
# VisePanda — Phase 1 MVP

AI travel butler workspace: a streaming chat (right) drives a live Day-by-day
Trip Canvas (left) and a row of Butler Rails reminders (top), rendered over a
fixed warm ink-wash background. See
`docs/superpowers/specs/2026-06-29-mvp-trip-canvas-design.md` for the full design.

## Running locally

```bash
npm install
npm run dev
```

Visit `http://localhost:3000`. No environment variables are required — without
`DEEPSEEK_API_KEY` set, `/api/chat` serves deterministic mock replies so the
full interaction loop works out of the box. Copy `.env.example` to `.env.local`
and set `DEEPSEEK_API_KEY` to use real DeepSeek streaming instead.

## Tests

```bash
npm run test
```

Runs Vitest against the logic-only modules: the trip store reducers, the
trip-instruction parser, the mock AI engine, and the streaming helpers.

## Manual verification script

1. Start the app with no `DEEPSEEK_API_KEY` set.
2. Send: `I'm coming from the US, first time in China, 5 days, want to go to
   Beijing and Shanghai.`
   Expect: 5 Day cards appear on the canvas alternating Beijing/Shanghai, and
   Butler Rails for visa, payment setup, and intercity transport appear at the
   top.
3. Send: `I don't want to be too tired, hotels should be convenient.`
   Expect: existing Day cards update in place — fewer attractions per day,
   hotel notes mention being near the metro — without the canvas resetting or
   duplicating days.
4. Resize the browser below 768px width (or use devtools device toolbar).
   Expect: the layout switches to a full-screen chat with a "View itinerary"
   button bottom-right that opens a full-screen Trip Canvas sheet with a "Back
   to chat" control.
5. Reload the page.
   Expect: messages and Day cards persist (loaded from `localStorage`).
```

- [ ] **Step 2: Run the full test suite**

Run: `npm run test`
Expected: PASS — all tests from Tasks 2–5 (store, parser, mock AI, streaming) green.

- [ ] **Step 3: Run the production build**

Run: `npm run build`
Expected: `Compiled successfully`.

- [ ] **Step 4: Walk through the manual verification script from `README.md` steps 2–5 above in a running `npm run dev` session**

Expected: every step behaves as described. If any step fails, fix the relevant component/module from Tasks 7–11 before proceeding — do not mark this task done with a failing manual check.

- [ ] **Step 5: Commit**

```bash
git add README.md
git commit -m "Add README with run instructions and manual verification script"
```

---

## Self-Review Notes

- **Spec coverage:** Scope/range (Task 1, 11), tech stack (Task 1), desktop layout with top rails + left canvas + right chat (Task 11), mobile chat-primary with sheet (Task 11), dark-mask text-on-background treatment with no card borders (Tasks 7–10), AI dual-output mechanism with fenced instruction block (Tasks 3, 6, 10), mock fallback (Tasks 4, 6), fault-tolerant parsing (Task 3), persistence to localStorage (Task 2) — all covered.
- **Placeholder scan:** No TBD/TODO; every code block is complete and runnable as written.
- **Type consistency:** `DayCard`, `RailItem`, `DayInstruction`, `RailInstruction`, `TripInstructionBlock`, `ChatMessage` are defined once in Task 2 and referenced identically (same field names/casing) in every later task's code.
- **Scope:** single subsystem (one page), no decomposition needed.
