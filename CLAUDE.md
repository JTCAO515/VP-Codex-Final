# VisePanda — Project Memory

Standing rules for every session on this project. Read this before acting.

## Workflow rules

- **VPCC is NOT automatic.** Do NOT run the VPCC workflow/ceremony (the Step 0
  pre-work diff report, code sync, read-7-docs, confirm-phase steps) on your own.
  Only run VPCC when the user explicitly asks for it (e.g. says "执行vpcc",
  "run vpcc", or types `/vpcc`). For normal iteration requests, just do the work.
- **Every iteration must update ALL markdown docs in detail** — especially
  `HANDOFF.md`, `PLAN.md`, and `PRD.md`, plus `DESIGN.md`, `AGENTS.md`,
  `CHANGELOG.md`, and `VERSIONING.md`. Bump `package.json` version each iteration.
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

## Product context

- Next.js 15 App Router + React 19 + TypeScript on Vercel; production domain
  `go2china.space`. Supabase for auth/persistence (degrades to guest/mock).
- The authoritative product roadmap lives in `PLAN.md` (阶段一…阶段十八) and
  `docs/planning/v0.1.46-product-expansion.md`. Design decisions are ADRs in
  `DESIGN.md`.
- Do not put any model identifier or internal session id into commit messages,
  PR bodies, code comments, or pushed artifacts — chat replies only.
