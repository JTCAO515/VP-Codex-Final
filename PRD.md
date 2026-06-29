# VisePanda PRD

Last updated: 2026-06-29
Repository: `JTCAO515/VP-Claude-Final`
Status: Phase 1 (MVP) — design and implementation plan approved, build not started

## Product Definition

VisePanda is an AI travel butler for English-speaking foreign visitors to China. It replaces form-filling trip planning with conversation: the user talks to the AI, and a structured itinerary builds itself in front of them, while the AI proactively surfaces the practical things a first-time visitor would forget (visa, payment setup, transport between cities, weather, risk, translation, emergencies).

This is a from-scratch rebuild. Three prior parallel attempts (`VP-Hermes-Web`, `VP-Codex-Web`, `VP-Claude-Web`) explored this space and converged on the same target user and a similar "stdlib Python + vanilla JS" stack; none is reused directly here. `VP-Claude-Web` reached the highest feature completeness (real Ctrip/Meituan/Amap/Qwen3 integrations) and is the most useful prior-art reference for later phases, but this repo starts clean with a different stack (see DESIGN.md / AGENTS.md) chosen specifically for the Phase 1 interaction model.

## Target Users

- First-time, independent foreign visitors to China (primary).
- Foreign residents in China planning weekend/short trips.
- English speakers planning a China trip for someone else.

Shared traits: English as the working language, 25-55, mobile-first, no fluency in Chinese apps (WeChat/Alipay/Meituan/Ctrip), expects the product to bridge to those real services rather than reinvent them.

## Core Value Proposition

"Talk to a travel butler, watch your itinerary build itself." The AI is not a search box or a generic chatbot — every turn of the conversation can update a visible, structured day-by-day plan, and the AI volunteers operational reminders (Butler Rails) the user didn't ask for but needs.

## Phase Roadmap

### Phase 1 — Trip Canvas + Butler Rails (current)

Scope: a single workspace page. Day-by-day itinerary canvas (left, desktop) driven live by a persistent chat (right), with a Butler Rails reminder bar (visa/payment/hotel/transport/weather/risk/language/emergency) across the top. No login, no database — trip state lives in the browser. AI calls are optional (placeholder key, deterministic mock fallback). Full design: `docs/superpowers/specs/2026-06-29-mvp-trip-canvas-design.md`. Full build plan: `docs/superpowers/plans/2026-06-29-mvp-trip-canvas.md`.

**Success criteria for Phase 1:**
- A user can describe a multi-city, multi-day trip in plain English and see correct Day cards appear without manual form entry.
- A follow-up preference ("don't want to be too tired") visibly updates the existing plan rather than replacing it with something unrelated.
- The whole loop works with zero API keys configured (mock mode), so the experience can be demoed/reviewed before any real integration exists.
- No part of the UI uses a semi-transparent dialog/card background — text sits on the ink-wash background via dark-mask treatment only.

### Phase 2 — Real AI + Background Art (planned, not yet scoped)

- Replace the mock AI fallback's primacy with a production DeepSeek key as the default path.
- Replace the placeholder SVG ink-wash background with commissioned/generated art, and make it switch per active city/scene (explicitly deferred from Phase 1 by user decision on 2026-06-29).

### Phase 3 — Supporting Surfaces (planned, not yet scoped)

- City library, travel tools, saved trips list, account/auth — the tabs intentionally left out of Phase 1.
- Real third-party integrations (hotel booking, maps, translation) replacing any remaining placeholders.

## Explicitly Out of Scope (Phase 1)

- Login/auth, any backend database, real third-party API keys, additional tabs beyond the single workspace, dynamic/per-city backgrounds, E2E test framework. See `docs/superpowers/specs/2026-06-29-mvp-trip-canvas-design.md` for the full exclusion list and rationale.
