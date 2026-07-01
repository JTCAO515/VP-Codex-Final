# VPCC — VisePanda Codex Cycle

Standard iteration workflow for the VisePanda project.

## Steps

### Step 1: Sync Code
Pull the latest remote state before starting any work.

```bash
git fetch origin main && git reset --hard origin/main
```

Confirm the current HEAD commit and version:

```bash
git log --oneline -3
grep '"version"' package.json
```

### Step 2: Read Documents
Read all 7 workflow documents in order to understand the current project state:

1. `VERSIONING.md` — current version and release history
2. `CHANGELOG.md` — detailed change log per version
3. `HANDOFF.md` — current status, known issues, next steps
4. `PLAN.md` — phase tasks and milestones
5. `PRD.md` — product requirements and acceptance criteria
6. `DESIGN.md` — architecture decisions (ADRs) and code structure
7. `AGENTS.md` — agent rules, constraints, and coding standards

### Step 3: Confirm Current Phase
After reading the docs, report:
- Current version (from VERSIONING.md / package.json)
- Current phase and completed milestones (from PLAN.md)
- Last completed feature (from CHANGELOG.md)
- Any known issues or next recommended iteration (from HANDOFF.md)

---

## Hard Rules (enforce on every iteration)

1. **Version**: increment `0.1.x` patch by 1 each iteration unless the user specifies a different version.
2. **Doc sync**: after every code change, update all 7 documents before committing.
3. **Tests**: run `npm run test` and `npm run build` before committing; record failures if they occur.
4. **Commit**: `git add` specific files (no `-A` for secrets), commit with a descriptive message, then `git push -u origin <branch>`.
5. **Security**: never write real API keys into any file. All keys (`DEEPSEEK_API_KEY`, `DASHSCOPE_API_KEY`, `EXCHANGE_RATE_API_KEY`, `AMAP_API_KEY`, `NEXT_PUBLIC_SUPABASE_URL`, `NEXT_PUBLIC_SUPABASE_ANON_KEY`) must only be read from server-side environment variables.
6. **No mock removal**: keep all mock/fallback providers even after real providers are added.
7. **Branch**: always develop on `claude/visepanda-phase-3-hym6z9`; push to remote with `git push -u origin <branch>`.

## Version Update Checklist

After each iteration, update these files:
- [ ] `package.json` — bump `version` field
- [ ] `package-lock.json` — auto-updated by npm
- [ ] `VERSIONING.md` — add release note for new version
- [ ] `CHANGELOG.md` — add full changelog entry at top
- [ ] `HANDOFF.md` — add handoff update section for new version
- [ ] `PLAN.md` — add addendum with completed tasks
- [ ] `PRD.md` — add requirement update if product behavior changed
- [ ] `DESIGN.md` — add design update and ADR if architecture changed
- [ ] `AGENTS.md` — add agent update if new rules or constraints apply
