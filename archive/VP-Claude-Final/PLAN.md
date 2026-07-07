# VisePanda Active Plan

Last updated: 2026-06-29
Current phase: Phase 1 (MVP) — Trip Canvas + Butler Rails
Status: design + implementation plan approved; build not started

## Current Objective

Ship a real multi-route app shell (Chat / Trips / Explore / Tools / Account in the top nav) where Chat is the AI travel butler workspace: conversation on the right drives a live, structured day-by-day itinerary canvas (with a Trip Summary card) on the left, with proactive operational reminders (Butler Rails) across the top — fully demoable with zero API keys. Trips/Explore/Tools/Account are real routes rendering a shared placeholder, not yet feature pages.

**Scope revised 2026-06-29**: the user supplied a concrete reference mockup that (a) expanded Phase 1 from a single page to a real 5-route shell, and (b) replaced the original "dark mask on full-bleed mountain" visual treatment with light, bordered cards on a flat cream page with the ink-wash art moved to a decorative side margin. See `DESIGN.md` rev. 2 for the current visual spec — it supersedes the visual sections of `docs/superpowers/specs/2026-06-29-mvp-trip-canvas-design.md`.

## Current Baseline

Nothing built yet in this repository. What exists:

- `docs/superpowers/specs/2026-06-29-mvp-trip-canvas-design.md` — approved design (scope, tech stack, layout, visual treatment, AI data flow, error handling, test scope).
- `docs/superpowers/plans/2026-06-29-mvp-trip-canvas.md` — approved 12-task implementation plan with full code for every task.
- `PRD.md`, `DESIGN.md`, `AGENTS.md`, `HANDOFF.md` (this set of docs).

## Phase Plan

### Phase 1: App Shell + Trip Canvas + Butler Rails

Status: planned (plan v2), build not started.

- Next.js 14 + TypeScript + Tailwind + Zustand scaffold.
- Shared `TopNav` + `SideOrnament` shell across 5 real routes: `/` (Chat), `/trips`, `/explore`, `/tools`, `/account`. Only Chat is fully built; the other four render a shared placeholder.
- Core types + store with upsert/delete reducers for Day cards, Rail items, and the Trip Summary object.
- Fault-tolerant parser for the AI's trailing `json-trip-instructions` block (now also carrying an optional `summary` field).
- Deterministic mock AI engine (keyword-based) so the product works with no `DEEPSEEK_API_KEY`.
- `/api/chat` route: DeepSeek streaming when a key is present, mock fallback otherwise, identical plain-text contract either way.
- Bordered/flat-card visual treatment per `DESIGN.md` rev. 2 (no dark mask, no glass blur) for Butler Rails, Trip Summary card, Day timeline + cards, chat panel.
- Quick-reply chips (real, send canned text) and attachment/image icons (visual placeholder only) in the chat input row.
- Desktop split layout (canvas | chat, divided by a hairline line) and mobile chat-primary layout with a full-screen canvas sheet.

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
- No semi-transparent/glass-blur card or dialog backgrounds anywhere — flat, bordered, same-tone-family cards only (see `DESIGN.md` rev. 2).
- Every external dependency (AI, future maps/booking/translation) must degrade gracefully with no key configured.
- TDD is required for logic-only modules (store, parser, mock AI, streaming helpers); presentational components are verified by build + manual walkthrough, not unit tests — do not add component-render tests without a reason that isn't already covered by manual verification.
- Phase 1 now includes the 5-route shell (Chat/Trips/Explore/Tools/Account), but only Chat gets real functionality — don't build out feature logic for the other four routes without revisiting this plan with the user first; a shared placeholder page is sufficient.
- Do not add login/database for real — the other four routes are placeholders, not an excuse to start building auth/persistence ahead of Phase 3.
- Do not commit real API keys or secrets; `.env.example` documents the only required variable name (`DEEPSEEK_API_KEY`), left blank.
