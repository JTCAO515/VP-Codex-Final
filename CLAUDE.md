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

This project uses three AI Coding Agents working through GitHub. **Read these before starting work:**
1. `PROJECT_CONTEXT.md` — Project background and context
2. `ARCHITECTURE.md` — System architecture and data flow
3. `AGENTS.md` — Collaboration rules (this is the authoritative rules file)
4. `API_SPEC.md` — API interface definitions
5. `MOBILE_STANDARD.md` — Mobile app standards

**Workflow:**
- Tasks are assigned via **GitHub Issues** (Claude Code creates them)
- Work is done in **independent `agent/*` branches**
- Code is submitted via **Pull Requests** with a standardized template
- **Claude Code reviews** all PRs for architecture compliance

**Role hierarchy:**

| Agent | Role | What they do | What they don't do |
|-------|------|--------------|-------------------|
| **Claude Code** | Architect + Reviewer | Create Issues, maintain architecture docs, review PRs, arbitrate conflicts | Don't write end-side business code, don't directly edit PR content |
| **OpenAI Codex** | Lead Developer | Complex logic, backend APIs, bug fixes, refactors, iOS development | Don't modify DB schema or system prompts unilaterally |
| **Antigravity (agy)** | Frontend + QA | Android development, frontend UI, interaction/browser testing, visual checks, E2E tests | Don't set architecture standards, don't add endpoints or fields privately |

**Branch strategy:**
- `main` — production-ready, merges from `dev` only
- `dev` — development trunk, PR merge target
- `agent/<name>-<task>` — individual task branches

**Key rules:**
1. Single architecture authority — only Claude Code defines APIs, routes, DB schema, system prompts
2. Codex sets mobile cross-platform standards; Antigravity aligns unconditionally
3. No workarounds — blocked = file an Issue + @Claude Code
4. Small PRs, frequent merges — under 300 lines per PR
5. No PR without passing build + tests
6. No direct push to `main` or `dev`

- The authoritative product roadmap lives in `PLAN.md` (阶段一…阶段十八) and

- The authoritative product roadmap lives in `PLAN.md` (阶段一…阶段十八) and
  `go2china.space`. Supabase for auth/persistence (degrades to guest/mock).
- The authoritative product roadmap lives in `PLAN.md` (阶段一…阶段十八) and
  `docs/planning/v0.1.46-product-expansion.md`. Design decisions are ADRs in
  `DESIGN.md`.
- Do not put any model identifier or internal session id into commit messages,
  PR bodies, code comments, or pushed artifacts — chat replies only.
