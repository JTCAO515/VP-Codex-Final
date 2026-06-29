# VisePanda Agent Instructions

VisePanda is an AI travel butler for English-speaking foreign visitors to China. This repository (`JTCAO515/VP-Claude-Final`) is a from-scratch Phase 1 rebuild — do not port code from `VP-Hermes-Web`, `VP-Codex-Web`, or `VP-Claude-Web`; those are prior-art reference only (see PRD.md).

## Start Here

Read these before making broad changes:

1. `HANDOFF.md` — current state, what's done, what's pending
2. `PRD.md` — product definition, target users, phase scope
3. `PLAN.md` — active phase plan and near-term rules
4. `DESIGN.md` — visual system, especially the "no dialog box" rule
5. `docs/superpowers/specs/2026-06-29-mvp-trip-canvas-design.md` — the approved Phase 1 design with full rationale
6. `docs/superpowers/plans/2026-06-29-mvp-trip-canvas.md` — the approved Phase 1 task-by-task build plan

## Current Engineering Rules

- Stack is Next.js 14 (App Router) + TypeScript + Tailwind + Zustand. Do not introduce a separate backend language/runtime without revisiting this decision with the user.
- No backend database in Phase 1 — trip state is Zustand + `localStorage` only.
- Every external dependency (AI today; maps/booking/translation later) must degrade gracefully with no API key configured. Never make a feature hard-fail because a key is missing.
- No semi-transparent card/dialog backgrounds — see DESIGN.md's "no dialog box" rule before adding any new text-bearing UI.
- TDD only for logic-only modules (`lib/store.ts`, `lib/parse-instructions.ts`, `lib/mock-ai.ts`, `lib/streaming.ts`). Don't add component-render tests for presentational components — those are verified by `npm run build` + manual walkthrough per the design doc's reduced MVP test scope.
- Do not commit real API keys, tokens, or secrets. `.env.example` lists required variable names only, left blank.
- Keep Phase 1 to the single workspace page (no tabs, no auth) unless the user explicitly moves the project into Phase 3.

## Useful Checks

```bash
npm run test      # vitest — store, parser, mock AI, streaming
npm run build     # next build — type-checks and builds the whole app
npm run dev        # local dev server, http://localhost:3000
```

Manual verification script for the full interaction loop lives in `README.md` once Task 12 of the implementation plan is complete.

## Historical Notes

`VP-Hermes-Web`, `VP-Codex-Web`, and `VP-Claude-Web` (all under `JTCAO515`) document three earlier parallel attempts at this product, on a Python-stdlib + vanilla-JS stack. They are archive context for product/content decisions (e.g. `VP-Claude-Web`'s real Ctrip/Meituan/Amap integrations are useful reference for Phase 3), not a codebase to extend.
