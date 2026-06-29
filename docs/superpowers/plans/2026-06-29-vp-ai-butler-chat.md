# VisePanda AI Butler Chat Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the first-stage VisePanda product skeleton focused on the AI Butler Chat workspace: live trip canvas on the left, persistent chat on the right, with placeholder tabs for Trips, Explore, Tools, and Account.

**Architecture:** Create a clean Next.js + React + TypeScript app for Vercel. Keep the first-stage experience local and deterministic through a mock AI provider that emits structured canvas patches. Separate shell, chat, canvas, mock AI, routing, and placeholder surfaces so real AI, Supabase, and third-party providers can be added later without rewriting the UI.

**Tech Stack:** Next.js App Router, React, TypeScript, CSS Modules or global CSS tokens, Vitest, Testing Library, Playwright, Vercel-ready environment placeholders.

## Global Constraints

- The first-stage default screen is Chat / AI Butler.
- Chat is the only fully designed first-stage experience.
- Trips, Explore, Tools, and Account are polished placeholders only.
- Main desktop workspace uses live trip canvas on the left and persistent chat on the right.
- Main mobile workspace is canvas-first with chat available from a fixed input or bottom panel.
- Visual direction is warm New Chinese with ink landscape background.
- Do not use translucent glass chat panels.
- MVP uses one high-quality warm ink background only; destination-aware background switching is reserved for a later iteration.
- Missing API keys must not break the app.
- No real AI, Supabase, Trip.com, Meituan, Amap, weather, translation, or speech integration in this phase.
- Keep dependencies minimal and compatible with Vercel.

---

## File Structure

Create these files:

- `package.json`: scripts and dependencies.
- `next.config.mjs`: Next.js config.
- `tsconfig.json`: TypeScript config.
- `.gitignore`: ignores dependencies, build output, local env.
- `.env.example`: future provider placeholders.
- `README.md`: local run, scope, env notes.
- `app/layout.tsx`: root document shell and metadata.
- `app/page.tsx`: redirects or renders the Chat workspace as default.
- `app/chat/page.tsx`: main AI Butler workspace route.
- `app/trips/page.tsx`: Trips placeholder.
- `app/explore/page.tsx`: Explore placeholder.
- `app/tools/page.tsx`: Tools placeholder.
- `app/account/page.tsx`: Account placeholder.
- `app/globals.css`: visual tokens, layout base, responsive behavior.
- `app/api/chat/route.ts`: mock chat API route.
- `app/api/trips/route.ts`: placeholder API response.
- `app/api/explore/route.ts`: placeholder API response.
- `app/api/tools/route.ts`: placeholder API response.
- `components/shell/AppShell.tsx`: shared app layout and navigation.
- `components/shell/NavTabs.tsx`: top/bottom nav.
- `components/chat/ButlerWorkspace.tsx`: stateful client workspace.
- `components/chat/ChatPanel.tsx`: chat transcript, prompt chips, composer.
- `components/canvas/TripCanvas.tsx`: left canvas composition.
- `components/canvas/TripSummary.tsx`: trip header.
- `components/canvas/DayCard.tsx`: day itinerary card.
- `components/canvas/ButlerRail.tsx`: practical reminders.
- `components/placeholders/PlaceholderPage.tsx`: reusable placeholder page.
- `lib/types/trip.ts`: shared trip, patch, alert, and chat types.
- `lib/mock-ai/mockButler.ts`: deterministic mock provider.
- `lib/canvas/applyCanvasPatch.ts`: patch application logic.
- `lib/env/placeholders.ts`: env placeholder registry.
- `tests/mockButler.test.ts`: mock parser tests.
- `tests/applyCanvasPatch.test.ts`: canvas reducer tests.
- `tests/env.test.ts`: missing-env safety test.
- `tests/chat-workspace.test.tsx`: workspace interaction test.
- `tests/placeholder-pages.test.tsx`: placeholder rendering test.
- `tests/e2e/chat-workspace.spec.ts`: desktop/mobile smoke test.
- `public/ink-landscape.svg` or `public/ink-landscape.webp`: warm Chinese ink background asset.

---

### Task 1: Bootstrap The Next.js App

**Files:**
- Create: `package.json`
- Create: `next.config.mjs`
- Create: `tsconfig.json`
- Create: `.gitignore`
- Create: `.env.example`
- Create: `README.md`
- Create: `app/layout.tsx`
- Create: `app/page.tsx`
- Create: `app/globals.css`

**Interfaces:**
- Produces: a Next.js app that can boot with `npm run dev`.
- Produces: environment placeholder names consumed later by `lib/env/placeholders.ts`.

- [ ] **Step 1: Create project metadata and scripts**

Create `package.json` with these scripts:

```json
{
  "name": "vp-codex-final",
  "version": "0.1.0",
  "private": true,
  "scripts": {
    "dev": "next dev",
    "build": "next build",
    "start": "next start",
    "lint": "next lint",
    "test": "vitest run",
    "test:watch": "vitest",
    "test:e2e": "playwright test"
  },
  "dependencies": {
    "@next/env": "^15.0.0",
    "next": "^15.0.0",
    "react": "^19.0.0",
    "react-dom": "^19.0.0"
  },
  "devDependencies": {
    "@playwright/test": "^1.45.0",
    "@testing-library/jest-dom": "^6.4.0",
    "@testing-library/react": "^16.0.0",
    "@types/node": "^22.0.0",
    "@types/react": "^19.0.0",
    "@types/react-dom": "^19.0.0",
    "eslint": "^9.0.0",
    "eslint-config-next": "^15.0.0",
    "typescript": "^5.5.0",
    "vitest": "^2.0.0"
  }
}
```

- [ ] **Step 2: Add Next and TypeScript configuration**

Create `next.config.mjs`:

```js
/** @type {import('next').NextConfig} */
const nextConfig = {
  poweredByHeader: false,
};

export default nextConfig;
```

Create `tsconfig.json` using Next.js defaults:

```json
{
  "compilerOptions": {
    "target": "ES2017",
    "lib": ["dom", "dom.iterable", "esnext"],
    "allowJs": false,
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

- [ ] **Step 3: Add ignore and env example files**

Create `.gitignore`:

```gitignore
node_modules
.next
out
coverage
.env
.env.local
.env.*.local
playwright-report
test-results
```

Create `.env.example`:

```bash
NEXT_PUBLIC_SUPABASE_URL=
NEXT_PUBLIC_SUPABASE_ANON_KEY=
SUPABASE_SERVICE_ROLE_KEY=
AI_API_KEY=
AI_BASE_URL=
AI_MODEL=
AMAP_API_KEY=
CTRIP_AID=
CTRIP_SID=
MEITUAN_UNION_API_KEY=
MEITUAN_UNION_API_SECRET=
```

- [ ] **Step 4: Add the root layout and default route**

Create `app/layout.tsx`:

```tsx
import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "VisePanda - AI China Travel Butler",
  description: "Plan a China trip with an AI butler and a live itinerary canvas.",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
```

Create `app/page.tsx`:

```tsx
import { redirect } from "next/navigation";

export default function HomePage() {
  redirect("/chat");
}
```

- [ ] **Step 5: Add base visual tokens**

Create `app/globals.css` with root tokens for warm paper, cinnabar, muted gold, ink brown, stone, radius, shadows, responsive layout, and reduced motion. Include only base styles in this task; component-specific styles come later.

- [ ] **Step 6: Install dependencies**

Run:

```bash
npm install
```

Expected: `package-lock.json` is created and install finishes without dependency errors.

- [ ] **Step 7: Verify boot**

Run:

```bash
npm run build
```

Expected: build reaches route compilation. If it fails because later components are not present yet, keep this step until after Task 2 creates the first renderable route.

- [ ] **Step 8: Commit**

```bash
git add package.json package-lock.json next.config.mjs tsconfig.json .gitignore .env.example README.md app
git commit -m "chore: bootstrap Next app"
```

---

### Task 2: Define Types, Environment Registry, And Tests

**Files:**
- Create: `lib/types/trip.ts`
- Create: `lib/env/placeholders.ts`
- Create: `tests/env.test.ts`
- Modify: `README.md`

**Interfaces:**
- Produces: `TripState`, `TripDay`, `TripBlock`, `ButlerAlert`, `CanvasPatch`, `ChatMessage`.
- Produces: `getEnvironmentStatus(): EnvironmentStatus[]`.
- Consumes: env names from `.env.example`.

- [ ] **Step 1: Add shared trip and chat types**

Create `lib/types/trip.ts` with these exported types:

```ts
export type Pace = "Light" | "Balanced" | "Relaxed" | "Packed";
export type AlertPriority = "high" | "medium" | "low";
export type AlertType =
  | "visa"
  | "payment"
  | "booking"
  | "transport"
  | "weather"
  | "language"
  | "risk"
  | "emergency";

export interface TripBlock {
  time: "Morning" | "Afternoon" | "Evening" | "Flexible";
  title: string;
  description: string;
}

export interface TripDay {
  day: number;
  city: string;
  pace: Pace;
  blocks: TripBlock[];
  food: string[];
  stay: string;
  transport: string;
  note: string;
  status?: "new" | "revised" | "needs-confirmation";
}

export interface ButlerAlert {
  type: AlertType;
  priority: AlertPriority;
  title: string;
  body: string;
  action: string;
}

export interface TripSummary {
  title: string;
  durationDays: number;
  pace: Pace;
  travelerStyle: string;
  destinations: string[];
  confidence: "Draft" | "Refined" | "Ready to save";
}

export interface TripState {
  summary: TripSummary;
  days: TripDay[];
  alerts: ButlerAlert[];
  lastUpdatedReason: string;
}

export interface CanvasPatch {
  intent: "create_trip" | "adjust_trip" | "add_alerts";
  assistantMessage: string;
  tripSummary?: Partial<TripSummary>;
  days?: TripDay[];
  butlerAlerts?: ButlerAlert[];
  reason: string;
}

export interface ChatMessage {
  id: string;
  role: "user" | "assistant";
  content: string;
}
```

- [ ] **Step 2: Add env registry**

Create `lib/env/placeholders.ts`:

```ts
export interface EnvironmentStatus {
  key: string;
  configured: boolean;
  purpose: string;
}

const ENVIRONMENT_KEYS: Array<{ key: string; purpose: string }> = [
  { key: "NEXT_PUBLIC_SUPABASE_URL", purpose: "Future Supabase project URL" },
  { key: "NEXT_PUBLIC_SUPABASE_ANON_KEY", purpose: "Future Supabase browser key" },
  { key: "SUPABASE_SERVICE_ROLE_KEY", purpose: "Future server-side Supabase writes" },
  { key: "AI_API_KEY", purpose: "Future AI provider key" },
  { key: "AI_BASE_URL", purpose: "Future OpenAI-compatible endpoint" },
  { key: "AI_MODEL", purpose: "Future AI model name" },
  { key: "AMAP_API_KEY", purpose: "Future map and POI provider" },
  { key: "CTRIP_AID", purpose: "Future Trip.com affiliate link generation" },
  { key: "CTRIP_SID", purpose: "Future Trip.com sub-affiliate link generation" },
  { key: "MEITUAN_UNION_API_KEY", purpose: "Future Meituan Union API" },
  { key: "MEITUAN_UNION_API_SECRET", purpose: "Future Meituan Union signing secret" }
];

export function getEnvironmentStatus(env: NodeJS.ProcessEnv = process.env): EnvironmentStatus[] {
  return ENVIRONMENT_KEYS.map((item) => ({
    ...item,
    configured: Boolean(env[item.key]?.trim()),
  }));
}
```

- [ ] **Step 3: Write env safety test**

Create `tests/env.test.ts`:

```ts
import { describe, expect, it } from "vitest";
import { getEnvironmentStatus } from "@/lib/env/placeholders";

describe("getEnvironmentStatus", () => {
  it("reports missing keys without throwing", () => {
    const result = getEnvironmentStatus({});

    expect(result.length).toBeGreaterThan(5);
    expect(result.every((item) => item.configured === false)).toBe(true);
  });

  it("marks configured keys", () => {
    const result = getEnvironmentStatus({ AI_API_KEY: "test-key" });

    expect(result.find((item) => item.key === "AI_API_KEY")?.configured).toBe(true);
  });
});
```

- [ ] **Step 4: Run the focused test**

Run:

```bash
npm run test -- tests/env.test.ts
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add lib tests README.md
git commit -m "feat: add shared types and env registry"
```

---

### Task 3: Build Mock AI And Canvas Patch Logic

**Files:**
- Create: `lib/mock-ai/mockButler.ts`
- Create: `lib/canvas/applyCanvasPatch.ts`
- Create: `tests/mockButler.test.ts`
- Create: `tests/applyCanvasPatch.test.ts`

**Interfaces:**
- Consumes: `CanvasPatch`, `TripState` from `lib/types/trip.ts`.
- Produces: `initialTripState`.
- Produces: `createMockButlerPatch(message: string, current: TripState): CanvasPatch`.
- Produces: `applyCanvasPatch(current: TripState, patch: CanvasPatch): TripState`.

- [ ] **Step 1: Write failing mock AI tests**

Create `tests/mockButler.test.ts`:

```ts
import { describe, expect, it } from "vitest";
import { createMockButlerPatch, initialTripState } from "@/lib/mock-ai/mockButler";

describe("createMockButlerPatch", () => {
  it("creates a first-trip itinerary for first-time China prompts", () => {
    const patch = createMockButlerPatch("I am visiting China for the first time for 5 days", initialTripState);

    expect(patch.intent).toBe("create_trip");
    expect(patch.tripSummary?.durationDays).toBe(5);
    expect(patch.days?.some((day) => day.city === "Beijing")).toBe(true);
    expect(patch.days?.some((day) => day.city === "Shanghai")).toBe(true);
  });

  it("adds payment alerts when payment is mentioned", () => {
    const patch = createMockButlerPatch("Add payment reminders", initialTripState);

    expect(patch.butlerAlerts?.some((alert) => alert.type === "payment")).toBe(true);
  });

  it("relaxes the trip when the user asks for less tiring plans", () => {
    const patch = createMockButlerPatch("Make this less tiring and slower", initialTripState);

    expect(patch.tripSummary?.pace).toBe("Relaxed");
    expect(patch.reason).toContain("pace");
  });
});
```

- [ ] **Step 2: Write failing canvas patch tests**

Create `tests/applyCanvasPatch.test.ts`:

```ts
import { describe, expect, it } from "vitest";
import { applyCanvasPatch } from "@/lib/canvas/applyCanvasPatch";
import { initialTripState } from "@/lib/mock-ai/mockButler";
import type { CanvasPatch } from "@/lib/types/trip";

describe("applyCanvasPatch", () => {
  it("merges summary fields and replaces supplied days", () => {
    const patch: CanvasPatch = {
      intent: "adjust_trip",
      assistantMessage: "Updated.",
      reason: "Changed pace.",
      tripSummary: { pace: "Relaxed", destinations: ["Beijing"] },
      days: [
        {
          day: 1,
          city: "Beijing",
          pace: "Relaxed",
          blocks: [{ time: "Morning", title: "Temple of Heaven", description: "Start gently." }],
          food: ["Noodles"],
          stay: "Dongcheng",
          transport: "Metro",
          note: "Light arrival day.",
          status: "revised"
        }
      ]
    };

    const next = applyCanvasPatch(initialTripState, patch);

    expect(next.summary.pace).toBe("Relaxed");
    expect(next.summary.destinations).toEqual(["Beijing"]);
    expect(next.days).toHaveLength(1);
    expect(next.lastUpdatedReason).toBe("Changed pace.");
  });

  it("deduplicates alerts by type and title", () => {
    const alert = {
      type: "payment" as const,
      priority: "high" as const,
      title: "Set up Alipay before arrival",
      body: "Prepare payment before taxis and meals.",
      action: "Review payment setup"
    };
    const next = applyCanvasPatch(
      { ...initialTripState, alerts: [alert] },
      { intent: "add_alerts", assistantMessage: "Added.", reason: "Payment.", butlerAlerts: [alert] }
    );

    expect(next.alerts).toHaveLength(1);
  });
});
```

- [ ] **Step 3: Implement `initialTripState` and mock provider**

Create `lib/mock-ai/mockButler.ts` with deterministic keyword matching for first-trip, slow pace, budget, food, hotel, visa, payment, translate, and emergency messages. Use the exact exported names from the interface section.

- [ ] **Step 4: Implement canvas patch application**

Create `lib/canvas/applyCanvasPatch.ts` so it:

- merges `tripSummary` into `current.summary`;
- replaces `days` only when `patch.days` is provided;
- appends new alerts while deduplicating by `${type}:${title}`;
- sets `lastUpdatedReason` to `patch.reason`.

- [ ] **Step 5: Run tests**

Run:

```bash
npm run test -- tests/mockButler.test.ts tests/applyCanvasPatch.test.ts
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add lib/mock-ai lib/canvas tests/mockButler.test.ts tests/applyCanvasPatch.test.ts
git commit -m "feat: add mock butler and canvas patch logic"
```

---

### Task 4: Build The App Shell And Placeholder Routes

**Files:**
- Create: `components/shell/AppShell.tsx`
- Create: `components/shell/NavTabs.tsx`
- Create: `components/placeholders/PlaceholderPage.tsx`
- Create: `app/trips/page.tsx`
- Create: `app/explore/page.tsx`
- Create: `app/tools/page.tsx`
- Create: `app/account/page.tsx`
- Create: `tests/placeholder-pages.test.tsx`
- Modify: `app/globals.css`

**Interfaces:**
- Produces: `AppShell({ children, activeTab })`.
- Produces: shared placeholders with a primary action to `/chat`.

- [ ] **Step 1: Write placeholder rendering tests**

Create `tests/placeholder-pages.test.tsx`:

```tsx
import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { PlaceholderPage } from "@/components/placeholders/PlaceholderPage";

describe("PlaceholderPage", () => {
  it("renders a polished placeholder with a return action", () => {
    render(
      <PlaceholderPage
        eyebrow="Explore"
        title="Explore is coming next."
        description="Cities, attractions, dining, hotels, and local experiences will connect here."
        items={["Cities", "Attractions", "Food", "Hotels", "Local experiences"]}
      />
    );

    expect(screen.getByText("Explore is coming next.")).toBeInTheDocument();
    expect(screen.getByText("Local experiences")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /return to chat/i })).toHaveAttribute("href", "/chat");
  });
});
```

- [ ] **Step 2: Create shell navigation**

Implement `components/shell/NavTabs.tsx` with links for Chat, Trips, Explore, Tools, and Account. Active state is controlled by the `activeTab` prop.

- [ ] **Step 3: Create app shell**

Implement `components/shell/AppShell.tsx` to render brand, nav, and page content. Keep shell styling in CSS classes defined in `app/globals.css`.

- [ ] **Step 4: Create placeholder component**

Implement `components/placeholders/PlaceholderPage.tsx` with eyebrow, title, description, item list, and return-to-chat link.

- [ ] **Step 5: Create placeholder pages**

Create pages with these exact meanings:

- `app/trips/page.tsx`: saved trips and canvas snapshots coming next.
- `app/explore/page.tsx`: cities, attractions, food, hotels, local experiences; third-party providers connect later.
- `app/tools/page.tsx`: translate, payment setup, visa and entry, currency, metro, eSIM/VPN, emergency.
- `app/account/page.tsx`: sign in and sync coming later; guest mode available.

- [ ] **Step 6: Run placeholder tests**

Run:

```bash
npm run test -- tests/placeholder-pages.test.tsx
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add app components tests/placeholder-pages.test.tsx
git commit -m "feat: add shell navigation and placeholder tabs"
```

---

### Task 5: Build The Trip Canvas Components

**Files:**
- Create: `components/canvas/TripCanvas.tsx`
- Create: `components/canvas/TripSummary.tsx`
- Create: `components/canvas/DayCard.tsx`
- Create: `components/canvas/ButlerRail.tsx`
- Modify: `app/globals.css`

**Interfaces:**
- Consumes: `TripState`, `TripDay`, `ButlerAlert`.
- Produces: presentational canvas components without owning chat state.

- [ ] **Step 1: Add component behavior expectations**

Before implementation, confirm each component has one job:

- `TripCanvas`: composes summary, day cards, and rails.
- `TripSummary`: renders title, destinations, duration, pace, confidence, and last update reason.
- `DayCard`: renders one day with time blocks, food, stay, transport, and note.
- `ButlerRail`: groups alerts by priority and renders practical reminders.

- [ ] **Step 2: Implement TripSummary**

Render summary fields using stable class names:

- `.trip-summary`
- `.trip-summary__meta`
- `.trip-summary__confidence`

- [ ] **Step 3: Implement DayCard**

Render each day as a solid paper-like card. Include status class modifiers for `new`, `revised`, and `needs-confirmation`.

- [ ] **Step 4: Implement ButlerRail**

Render alerts with priority labels. High priority appears first.

- [ ] **Step 5: Implement TripCanvas**

Compose the full canvas and include an accessible region label such as `aria-label="Live trip canvas"`.

- [ ] **Step 6: Style canvas**

Add CSS for:

- desktop left canvas width;
- paper cards;
- warm ink colors;
- cinnabar accents;
- muted gold priority labels;
- responsive single-column mobile layout.

- [ ] **Step 7: Run build**

Run:

```bash
npm run build
```

Expected: PASS.

- [ ] **Step 8: Commit**

```bash
git add components/canvas app/globals.css
git commit -m "feat: add live trip canvas components"
```

---

### Task 6: Build The Chat Panel And Stateful Butler Workspace

**Files:**
- Create: `components/chat/ButlerWorkspace.tsx`
- Create: `components/chat/ChatPanel.tsx`
- Create: `tests/chat-workspace.test.tsx`
- Create: `app/chat/page.tsx`
- Modify: `app/globals.css`

**Interfaces:**
- Consumes: `initialTripState`, `createMockButlerPatch`, `applyCanvasPatch`.
- Produces: client-side workspace state for messages and canvas.

- [ ] **Step 1: Write workspace interaction test**

Create `tests/chat-workspace.test.tsx`:

```tsx
import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ButlerWorkspace } from "@/components/chat/ButlerWorkspace";

describe("ButlerWorkspace", () => {
  it("updates the canvas after a user asks for a first China trip", async () => {
    render(<ButlerWorkspace />);

    fireEvent.change(screen.getByLabelText(/ask visepanda/i), {
      target: { value: "I am visiting China for the first time for 5 days" },
    });
    fireEvent.click(screen.getByRole("button", { name: /send/i }));

    expect(await screen.findByText(/Beijing/i)).toBeInTheDocument();
    expect(await screen.findByText(/Shanghai/i)).toBeInTheDocument();
    expect(await screen.findByText(/VisePanda updated the canvas/i)).toBeInTheDocument();
  });
});
```

- [ ] **Step 2: Implement ChatPanel**

`ChatPanel` accepts:

```ts
interface ChatPanelProps {
  messages: ChatMessage[];
  onSend: (message: string) => void;
  busy?: boolean;
}
```

It renders prompt chips, transcript, labeled textarea/input, and a Send button.

- [ ] **Step 3: Implement ButlerWorkspace**

Mark `ButlerWorkspace` as a client component. It owns:

- `TripState`
- `ChatMessage[]`
- input submission handler
- mock patch application
- accessible status text: `VisePanda updated the canvas: ${patch.reason}`

- [ ] **Step 4: Create Chat route**

Create `app/chat/page.tsx` using `AppShell` and `ButlerWorkspace`.

- [ ] **Step 5: Style the split workspace**

Add CSS for:

- desktop 65/35 split;
- right-side persistent chat;
- solid non-glass chat surface;
- direct background presence;
- mobile canvas-first flow;
- fixed mobile Ask input behavior or bottom chat area.

- [ ] **Step 6: Run focused test**

Run:

```bash
npm run test -- tests/chat-workspace.test.tsx
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add app/chat components/chat app/globals.css tests/chat-workspace.test.tsx
git commit -m "feat: add AI butler chat workspace"
```

---

### Task 7: Add API Route Placeholders

**Files:**
- Create: `app/api/chat/route.ts`
- Create: `app/api/trips/route.ts`
- Create: `app/api/explore/route.ts`
- Create: `app/api/tools/route.ts`

**Interfaces:**
- `/api/chat` returns mock assistant message and canvas patch.
- `/api/trips`, `/api/explore`, `/api/tools` return `{ ok: true, status: "placeholder" }` style JSON.

- [ ] **Step 1: Implement `/api/chat`**

Accept:

```ts
{
  "message": "string"
}
```

Return:

```ts
{
  "ok": true,
  "mode": "mock",
  "patch": CanvasPatch
}
```

Use `createMockButlerPatch` with `initialTripState` for first-stage server parity.

- [ ] **Step 2: Implement placeholder APIs**

For Trips, Explore, and Tools, return clear JSON saying the route is reserved for later provider work and requires no keys in phase one.

- [ ] **Step 3: Verify routes in build**

Run:

```bash
npm run build
```

Expected: all app routes and API routes compile.

- [ ] **Step 4: Commit**

```bash
git add app/api
git commit -m "feat: add first-stage API placeholders"
```

---

### Task 8: Add Ink Landscape Asset And Final Visual Polish

**Files:**
- Create: `public/ink-landscape.svg` or `public/ink-landscape.webp`
- Modify: `app/globals.css`
- Modify: `README.md`

**Interfaces:**
- Consumes: app shell and workspace classes.
- Produces: warm New Chinese background without translucent glass panels.

- [ ] **Step 1: Add background asset**

Create or generate one optimized warm ink landscape background asset in `public/`. It should be calm, mostly light, with negative space where the app text sits. Do not use a dark, blurred, or stock-like image.

Do not implement destination-aware background switching in this task. Future iterations may switch backgrounds based on active canvas destinations, such as Great Wall or Forbidden City ink imagery for Beijing and Bund or Jiangnan garden ink imagery for Shanghai.

- [ ] **Step 2: Apply background**

Set the app background using the asset, warm base colors, and subtle readability controls. Avoid translucent glass panels. Use solid paper-like surfaces for itinerary cards and chat.

- [ ] **Step 3: Check color palette**

Scan `app/globals.css` and confirm the palette is not dominated by only one hue. It should combine warm paper, cinnabar, muted gold, ink brown, and stone.

- [ ] **Step 4: Update README visual note**

Document that the first-stage visual direction is warm New Chinese and that the background asset is part of the product identity.

- [ ] **Step 5: Run build**

Run:

```bash
npm run build
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add public app/globals.css README.md
git commit -m "style: add warm ink visual system"
```

---

### Task 9: Add Browser Smoke Tests And Verify Desktop/Mobile

**Files:**
- Create: `playwright.config.ts`
- Create: `tests/e2e/chat-workspace.spec.ts`
- Modify: `package.json`
- Modify: `README.md`

**Interfaces:**
- Consumes: running Next app.
- Produces: automated smoke coverage for the core workspace.

- [ ] **Step 1: Add Playwright config**

Create `playwright.config.ts` with:

- test directory `tests/e2e`;
- base URL `http://127.0.0.1:3000`;
- desktop Chromium project at 1440x900;
- mobile Chromium project at 390x844.

- [ ] **Step 2: Add e2e smoke test**

Create `tests/e2e/chat-workspace.spec.ts`:

```ts
import { expect, test } from "@playwright/test";

test("chat workspace updates the canvas", async ({ page }) => {
  await page.goto("/chat");
  await expect(page.getByRole("heading", { name: /VisePanda/i })).toBeVisible();
  await page.getByLabel(/ask visepanda/i).fill("I am visiting China for the first time for 5 days");
  await page.getByRole("button", { name: /send/i }).click();
  await expect(page.getByText(/Beijing/i)).toBeVisible();
  await expect(page.getByText(/Shanghai/i)).toBeVisible();
  await expect(page.getByText(/updated the canvas/i)).toBeVisible();
});
```

- [ ] **Step 3: Run unit tests**

Run:

```bash
npm run test
```

Expected: PASS.

- [ ] **Step 4: Run production build**

Run:

```bash
npm run build
```

Expected: PASS.

- [ ] **Step 5: Run the app and e2e tests**

Start the dev server:

```bash
npm run dev
```

In another terminal, run:

```bash
npm run test:e2e
```

Expected: desktop and mobile projects pass.

- [ ] **Step 6: Manual visual inspection**

Open `http://127.0.0.1:3000/chat` and verify:

- desktop shows left canvas and right chat;
- mobile has no horizontal overflow around 390px;
- text remains readable over the background;
- chat is not a translucent glass box;
- placeholder tabs are reachable.

- [ ] **Step 7: Commit**

```bash
git add playwright.config.ts tests/e2e package.json package-lock.json README.md
git commit -m "test: add workspace browser smoke coverage"
```

---

### Task 10: Final Readiness Pass

**Files:**
- Modify only files required by verification findings.
- No new product scope.

**Interfaces:**
- Consumes: all previous tasks.
- Produces: a stable first-stage skeleton ready for user review.

- [ ] **Step 1: Run all checks**

Run:

```bash
npm run test
npm run build
npm run test:e2e
```

Expected: PASS for all commands.

- [ ] **Step 2: Check repo cleanliness**

Run:

```bash
git status --short
```

Expected: no uncommitted files except intentional final fixes.

- [ ] **Step 3: Review scope**

Confirm these are still true:

- Chat / AI Butler is the only complete first-stage experience.
- Trips, Explore, Tools, and Account remain placeholders.
- No real third-party integration was added.
- Missing env vars do not break local build.
- Main workspace is canvas-left/chat-right on desktop.
- Main workspace is canvas-first on mobile.

- [ ] **Step 4: Commit final fixes if needed**

If verification required changes:

```bash
git add <changed-files>
git commit -m "fix: polish first-stage butler skeleton"
```

- [ ] **Step 5: Provide handoff summary**

Summarize:

- what was built;
- what commands passed;
- what remains for phase two;
- local URL if the dev server is still running.
