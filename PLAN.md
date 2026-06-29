# VisePanda Active Plan

Last updated: 2026-06-29
Current phase: Phase 1 (MVP) — Trip Canvas + Butler Rails
Status: design + implementation plan approved; build not started

## Current Objective

Ship a single-page AI travel butler workspace where conversation on the right drives a live, structured day-by-day itinerary canvas on the left, with proactive operational reminders (Butler Rails) across the top — fully demoable with zero API keys.

## Current Baseline

Nothing built yet in this repository. What exists:

- `docs/superpowers/specs/2026-06-29-mvp-trip-canvas-design.md` — approved design (scope, tech stack, layout, visual treatment, AI data flow, error handling, test scope).
- `docs/superpowers/plans/2026-06-29-mvp-trip-canvas.md` — approved 12-task implementation plan with full code for every task.
- `PRD.md`, `DESIGN.md`, `AGENTS.md`, `HANDOFF.md` (this set of docs).

## Phase Plan

### Phase 1: Trip Canvas + Butler Rails

Status: planned, build not started.

- Next.js 14 + TypeScript + Tailwind + Zustand scaffold.
- Core types + store with upsert/delete reducers for Day cards and Rail items.
- Fault-tolerant parser for the AI's trailing `json-trip-instructions` block.
- Deterministic mock AI engine (keyword-based) so the product works with no `DEEPSEEK_API_KEY`.
- `/api/chat` route: DeepSeek streaming when a key is present, mock fallback otherwise, identical plain-text contract either way.
- Ink-wash background (placeholder SVG, see DESIGN.md), Butler Rails bar, Trip Canvas + Day cards, chat panel — all using the dark-mask-on-background treatment, no card borders.
- Desktop split layout (canvas | chat) and mobile chat-primary layout with a full-screen canvas sheet.

### Phase 2: Real AI + Dynamic Background

Status: not yet scoped into tasks.

- Make a real DeepSeek key the default; treat mock as a dev/offline fallback only, not the primary demo path.
- Replace the placeholder ink-wash SVG with real art and make the background change per active city/scene — explicitly approved direction, deferred from Phase 1 only because Phase 1 has no art asset pipeline yet.

### Phase 3: Supporting Surfaces

Status: not yet scoped into tasks.

- City library, tools, saved trips list, account/auth as additional surfaces alongside the Phase 1 workspace.
- Replace any remaining placeholder integrations (hotels, maps, translation) with real ones.

## Near-Term Rules

- Stack is Next.js 14 + TypeScript + Tailwind + Zustand for this rebuild — do not reintroduce the old stdlib-Python/vanilla-JS pattern without an explicit decision.
- No semi-transparent card/dialog backgrounds anywhere — dark-mask-on-background only (see DESIGN.md).
- Every external dependency (AI, future maps/booking/translation) must degrade gracefully with no key configured.
- TDD is required for logic-only modules (store, parser, mock AI, streaming helpers); presentational components are verified by build + manual walkthrough, not unit tests — do not add component-render tests without a reason that isn't already covered by manual verification.
- Keep Phase 1 to the single workspace page. Do not add tabs/auth/database ahead of Phase 3 without revisiting this plan with the user first.
- Do not commit real API keys or secrets; `.env.example` documents the only required variable name (`DEEPSEEK_API_KEY`), left blank.
