# VisePanda Handoff

Last updated: 2026-06-29
Current phase: Phase 1 (MVP) — Trip Canvas + Butler Rails
Status: **design and implementation plan approved by user; zero code written yet**

## What's Done

- Read all three prior parallel repos (`VP-Hermes-Web`, `VP-Codex-Web`, `VP-Claude-Web`) and extracted product/tech/design lessons — see PRD.md for the summary.
- Ran a full brainstorming session with the user (via the visual companion for layout/visual-style decisions) and locked in every Phase 1 decision:
  - Scope: single workspace page only (Trip Canvas + Butler Rails), no auth/db/other tabs.
  - Layout: Butler Rails as a full-width top bar; desktop split (canvas left, chat right); mobile chat-primary with a full-screen canvas sheet.
  - Visual treatment: no semi-transparent dialog boxes anywhere — text sits directly on the background via a gradient dark-mask, never a bordered/solid card.
  - Background: fixed (not yet per-city-dynamic) warm "medium ink wash" — confirmed direction is to make it dynamic per city/scene in a later phase once real art exists.
  - AI mechanism: single AI call returns conversational text + a trailing fenced `json-trip-instructions` JSON block describing Day/Rail upserts/deletes; parse failures silently no-op instead of erroring.
  - Stack: Next.js 14 + TypeScript + Tailwind + Zustand (decided by Claude per user delegation — chosen over continuing the old stdlib-Python/vanilla-JS pattern because Phase 1's core interaction is state-management-heavy, not information-display-heavy).
- Wrote and committed:
  - `docs/superpowers/specs/2026-06-29-mvp-trip-canvas-design.md` — full design doc, user-approved.
  - `docs/superpowers/plans/2026-06-29-mvp-trip-canvas.md` — full 12-task implementation plan with complete code for every step, user has seen the plan summary and chosen an execution mode is the next decision point.
  - `PRD.md`, `PLAN.md`, `DESIGN.md`, `AGENTS.md`, this file.
- Initialized the git repo locally (`VP-Claude-Final/`), remote `origin` set to `https://github.com/JTCAO515/VP-Claude-Final.git`. **Nothing has been pushed yet** — all commits are local only.

## What's Pending

- **Nothing in `app/`, `components/`, `lib/`, or `hooks/` exists yet.** The implementation plan's 12 tasks (scaffold → types/store → parser → mock AI → streaming → API route → background → rails → canvas → chat → layout → verification) have not been started.
- User has not yet chosen between subagent-driven execution and inline executing-plans execution for the build.
- Repo has not been pushed to GitHub — local commits only, on top of an empty remote.

## Known Risks / Things to Watch

- The "no dialog box" visual rule (dark-mask text directly on background) is unusual and was a deliberate, explicit user choice over two more conventional alternatives (opaque paper-card, fully-blank-negative-space). If legibility issues show up once real content is in place, revisit DESIGN.md's "no dialog box" section with the user before reverting to a card-based pattern unilaterally.
- The AI dual-output mechanism (chat text + trailing JSON block) depends on the model reliably following the fenced-block instruction in the system prompt (`app/api/chat/route.ts`'s `buildSystemPrompt`). The mock path is fully deterministic and doesn't have this risk; the real DeepSeek path does — if instruction blocks are missing/malformed often once a real key is wired in, that's expected by design (parser fails closed, chat-only update) but worth monitoring for frequency.
- Per-city dynamic background is an approved future direction, not Phase 1 scope — don't build it speculatively into `BackgroundLayer.tsx` without a follow-up design pass (art source/pipeline isn't decided yet).

## Next Step

Get the user's execution-mode choice (subagent-driven vs. inline) and start Task 1 of `docs/superpowers/plans/2026-06-29-mvp-trip-canvas.md`.
