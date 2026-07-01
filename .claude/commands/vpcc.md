# VPCC — VisePanda Codex Cycle

Standard iteration workflow for the VisePanda project.

## Steps

### Step 0: Pre-Work Diff Report
Before touching any code or documents, produce a detailed text report comparing remote main with local state. Run all of the following and present results in plain text:

```bash
# 1. Which branch are we on and what is the remote tracking branch?
git branch -vv

# 2. Commits on local that are NOT on remote main
git log origin/main..HEAD --oneline

# 3. Commits on remote main that are NOT local (i.e., remote is ahead)
git log HEAD..origin/main --oneline

# 4. Files changed (staged + unstaged) vs current HEAD
git status --short

# 5. Full unified diff of any uncommitted changes
git diff HEAD

# 6. Local package.json version vs remote package.json version
grep '"version"' package.json
git show origin/main:package.json | grep '"version"'

# 7. Summary of the 7 workflow docs: local HEAD commit that last touched each
git log --oneline -1 -- VERSIONING.md CHANGELOG.md HANDOFF.md PLAN.md PRD.md DESIGN.md AGENTS.md
```

Report format — write a plain-text summary covering:
- **Remote vs local commit gap**: how many commits ahead/behind each side is
- **Uncommitted changes**: list every modified/untracked file with a one-line description of what differs
- **Version gap**: local `package.json` version vs remote `package.json` version
- **Doc freshness**: which of the 7 docs have local changes not yet pushed
- **Conclusion**: what state we are starting from and any risks before proceeding

### Step 1: Sync Code
After the diff report, pull the latest remote state before starting any work.

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
4. **Commit + push to feature branch**: `git add` specific files (no `-A` for secrets), commit with a descriptive message, push feature branch with `git push -u origin claude/visepanda-phase-3-hym6z9`.
5. **Force push to main**: after the feature branch push succeeds, always push the same commit to the GitHub repo main branch:
   ```bash
   git push origin HEAD:main
   ```
   Verify the push succeeded by checking `git log origin/main --oneline -3`. If it fails, retry up to 3 times with 5-second waits between attempts.
6. **Security**: never write real API keys into any file. All keys (`DEEPSEEK_API_KEY`, `DASHSCOPE_API_KEY`, `EXCHANGE_RATE_API_KEY`, `AMAP_API_KEY`, `NEXT_PUBLIC_SUPABASE_URL`, `NEXT_PUBLIC_SUPABASE_ANON_KEY`) must only be read from server-side environment variables.
7. **No mock removal**: keep all mock/fallback providers even after real providers are added.

## End-of-Iteration Push Protocol

Every iteration must end with this exact sequence:

```bash
# 1. Stage specific files (never git add -A)
git add <list of changed files>

# 2. Commit
git commit -m "<version>: <description>"

# 3. Push to feature branch
git push -u origin claude/visepanda-phase-3-hym6z9

# 4. Push to main (mandatory, not optional)
git push origin HEAD:main

# 5. Confirm both succeeded
git log origin/main --oneline -3
git log origin/claude/visepanda-phase-3-hym6z9 --oneline -3
```

Report the final SHA and both remote refs after completing the push sequence.

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
