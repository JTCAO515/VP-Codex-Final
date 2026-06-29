# VisePanda Handoff

Last updated: 2026-06-29 (rev. 2 — scope and visual direction revised after a reference mockup)
Current phase: Phase 1 (MVP) — App shell + Trip Canvas + Butler Rails
Status: **design and implementation plan v2 approved by user; zero code written yet**

## What's Done

- Read all three prior parallel repos (`VP-Hermes-Web`, `VP-Codex-Web`, `VP-Claude-Web`) and extracted product/tech/design lessons — see PRD.md for the summary.
- Round 1 brainstorming (via the visual companion for layout/visual-style decisions) locked in an initial Phase 1 design — single workspace page, dark-mask-on-mountain visual treatment. Written up in `docs/superpowers/specs/2026-06-29-mvp-trip-canvas-design.md` and `docs/superpowers/plans/2026-06-29-mvp-trip-canvas.md` (now **superseded**, kept for history).
- **Round 2 revision (same day, before any code was written):** the user supplied a concrete reference mockup that changed two things:
  - **Scope grew**: Phase 1 now includes a real, navigable 5-route shell (Chat/Trips/Explore/Tools/Account in a shared top nav), not a single page. Only Chat is a full feature page; the other four render a shared "coming soon" placeholder.
  - **Visual treatment changed**: replaced "dark gradient mask, text directly on a full-bleed mountain painting" with "flat, opaque, bordered cards (`bg-ink-paper` on `bg-ink-cream`) on a calm page, ink-wash art confined to a decorative side margin (`SideOrnament`), division by hairline border lines instead of background contrast." Dark text on light paper, not light text on a dark mask.
  - Also added: a Trip Summary card (route/dates/travelers/days, AI-driven via a new `summary` field on the instruction block), real quick-reply chips in chat, and visual-only attachment/image icon placeholders.
  - `DESIGN.md` was rewritten in place (rev. 2) to reflect this; `PRD.md`/`PLAN.md`/`AGENTS.md` scope sections were updated to match; a new `docs/superpowers/plans/2026-06-29-mvp-trip-canvas-v2.md` supersedes the original plan with the updated 12-task breakdown (types/store with `TripSummary`, parser/mock-AI with `summary` support, app-shell task for TopNav/SideOrnament/placeholder routes, restyled rails/canvas/day-card/chat components).
- Initialized the git repo locally (`VP-Claude-Final/`), remote `origin` set to `https://github.com/JTCAO515/VP-Claude-Final.git`. **Nothing has been pushed yet** — all commits are local only.

## What's Pending

- **Nothing in `app/`, `components/`, `lib/`, or `hooks/` exists yet.** Execute `docs/superpowers/plans/2026-06-29-mvp-trip-canvas-v2.md`'s 12 tasks (scaffold → types/store → parser → mock AI → streaming → API route → app shell/routes → rails → summary+day cards/canvas → chat → workspace layout → verification). Do not execute the v1 plan file — it's marked superseded at the top.
- User has not yet chosen between subagent-driven execution and inline executing-plans execution for the build.
- Repo has not been pushed to GitHub — local commits only, on top of an empty remote.

## Known Risks / Things to Watch

- The rev. 2 visual system trades the rev. 1 legibility risk (text on a busy painted background) for a different one: cards are *intentionally* low-contrast against the page (`ink-paper` vs `ink-cream` is a very small color difference) so they "merge" rather than float — if that reads as "no visual hierarchy at all" once real content is in place, the fix is to lean harder on the hairline borders/shadow-sm, not to reintroduce a mask or add background-color contrast back in, since that was explicitly rejected.
- `SideOrnament` is `hidden` below the `md` breakpoint and `app/layout.tsx`'s `<main>` only gets `md:pl-40` — on mobile there's no side margin to reserve, which is correct, but double-check no element assumes the ornament's gutter exists below 768px.
- The AI dual-output mechanism (chat text + trailing `json-trip-instructions` block, now including `summary`) depends on the model reliably following the system prompt. The mock path is fully deterministic; the real DeepSeek path isn't — expected to occasionally omit/malform the block, which the parser handles by failing closed (chat-only update, no crash).
- Per-city dynamic side-ornament art and real map/photo assets are approved future directions, not Phase 1 scope — don't build them speculatively without a follow-up design pass.
- Don't be surprised by another concrete-mockup-driven revision before code lands — this is the second time it's happened today. If the user shares another reference image, treat it the same way: reconcile against `DESIGN.md`/the v2 plan, ask only about genuine scope questions (like the nav-tabs one), and update docs before any code is affected.

## Next Step

Get the user's execution-mode choice (subagent-driven vs. inline) and start Task 1 of `docs/superpowers/plans/2026-06-29-mvp-trip-canvas-v2.md`.
