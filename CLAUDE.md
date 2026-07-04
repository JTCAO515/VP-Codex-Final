# VisePanda — Project Memory

Standing rules for every session on this project. Read this before acting.

## Language rule (operator directive)

- **All Claude Code thinking, reasoning chains, replies, and reports to the
  operator must be in Chinese (中文).** Code, identifiers, and commit messages
  may stay in English, but every chat-facing explanation, plan, and report is
  written in Chinese.

## Workflow rules

- **VPCC is NOT automatic.** Do NOT run the VPCC workflow/ceremony (the Step 0
  pre-work diff report, code sync, read-7-docs, confirm-phase steps) on your own.
  Only run VPCC when the user explicitly asks for it (e.g. says "执行vpcc",
  "run vpcc", or types `/vpcc`). For normal iteration requests, just do the work.
- **Every iteration must update ALL markdown docs in detail** — especially
  `HANDOFF.md`, `PLAN.md`, and `PRD.md`, plus `DESIGN.md`, `AGENTS.md`,
  `CHANGELOG.md`, and `VERSIONING.md`. Bump `package.json` version each iteration.
- **Version series is `0.2.x`** (reset from `0.1.x` at operator request as of
  `v0.2.1`; `v0.1.55` was the last `0.1.x`). Increment the `0.2.x` patch by 1 each
  iteration unless the operator specifies a different version.
- **After every iteration, commit and push to the feature branch
  `claude/visepanda-phase-3-hym6z9` AND force-push the same commit to
  `origin/main`** (`git push origin HEAD:main`).
- **Detailed beginner tutorials for manual steps.** The user is a non-technical
  ("技术小白") operator. Any step the user must perform by hand — registering an
  API key, adding a Vercel environment variable, running a Supabase migration,
  enrolling in an app store, filing 备案/软著 — must come with a numbered,
  click-by-click tutorial (where to click, what to paste, what to expect).

## Security constraints (never violate)

- Never write real API keys/secrets into the repo or any doc.
- All provider keys are server-side only: `DEEPSEEK_API_KEY`, `DASHSCOPE_API_KEY`,
  `EXCHANGE_RATE_API_KEY`, `AMAP_API_KEY`, and future `ZHIPU_API_KEY`,
  `MOONSHOT_API_KEY`, `ERNIE_API_KEY`, `MINIMAX_API_KEY`,
  `DIANPING_APP_KEY`/`DIANPING_APP_SECRET`.
- `SUPABASE_SERVICE_ROLE_KEY` must never appear in any browser/client bundle
  (server-side admin code only).
- Only `NEXT_PUBLIC_AMAP_MAPS_KEY` may be public (display-only, domain-whitelisted).
- Never remove the mock/static fallback. Real integrations must always degrade
  gracefully when a key is missing or an upstream fails.
- Do not commit `.env`, credentials, `node_modules`, or `.next`.

## Agent role (VisePanda multi-Agent collaboration)

This project uses three AI Coding Agents. **Read these three documents before starting work:**
1. `AGENTS.md` — Collaboration rules, division of labor, communication protocols, and workflows
2. `API_SPEC.md` — Global API interface, struct, field, and route standards
3. `MOBILE_STANDARD.md` — Cross-platform network, cache, encryption, error codes, storage, and business process standards

**Role hierarchy:**

| Layer | Agent | Scope | Branch |
|-------|-------|-------|--------|
| **Architecture** | **Claude Code** | Global architecture commander + backend owner: API route design, DB schema, system prompt/constraint words, knowledge base, cross-agent progress monitoring, `main` merge approval | `claude/` → `main` |
| **Mobile Standards** | **OpenAI Codex** | iOS lead + mobile liaison: full iOS (SwiftUI/UIKit); defines cross-platform standards (network layer, caching, error codes, encryption, login); Antigravity aligns unconditionally | `codex/ios-development` → `main` |
| **Implementation** | **Antigravity (agy)** | Android dedicated dev: full Android (Kotlin + Jetpack Compose), system permissions, APK builds; aligns to `API_SPEC.md` + `MOBILE_STANDARD.md` | `agy/android` → `main` |

**Three iron rules:**
1. Single architecture authority — only Claude Code defines APIs, routes, DB schema, and system prompts. End-sides must NOT add/modify endpoints, fields, or schemas privately.
2. Cross-platform consistency — Codex sets the standard for shared mobile logic; Antigravity aligns unconditionally. When iOS and Android differ, Codex's design wins.
3. No silent workarounds — any missing interface or field mismatch must be reported upward. No hard-coding compatibility patches.

**Four standardized communication protocols — see `AGENTS.md` for full format definitions:**
- 【Architecture Task Ticket】 — Claude Code only (task dispatch)
- 【Progress Report】 — Codex/Antigravity only (status updates)
- 【Architecture Conflict Report】 — everyone (blocking issues)
- 【Merge Request】 — end-side only (PR to `main`)

**Merge workflow:** End-side completes dev → submits 【Merge Request】 → Claude Code audits architecture compliance only → approve or reject. No direct push to `main`.

- The authoritative product roadmap lives in `PLAN.md` (阶段一…阶段十八) and
  `go2china.space`. Supabase for auth/persistence (degrades to guest/mock).
- The authoritative product roadmap lives in `PLAN.md` (阶段一…阶段十八) and
  `docs/planning/v0.1.46-product-expansion.md`. Design decisions are ADRs in
  `DESIGN.md`.
- Do not put any model identifier or internal session id into commit messages,
  PR bodies, code comments, or pushed artifacts — chat replies only.
