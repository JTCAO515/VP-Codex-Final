# VisePanda Changelog

## v0.2.5 - 2026-07-02

**规划融合 + FIT travel desk readiness seed。** 本轮把远端 `v0.2.4` 交互深化规格与本地视觉规划/实现 seed 合并到同一条 `0.2.x` 主线,避免 `0.1.55` 本地 UI polish 与远端 `0.2.x` 规划分叉。

- 安全同步远端更新:检测到本地 `47eba46 feat(ui): polish fit travel desk` 与远端 `v0.2.1–v0.2.4` 分叉,先读取远端新文档与版本线,再合并而非重置覆盖。
- 吸收本地视觉规划/seed:Trip Canvas readiness 初版、summary/readiness/action rail、Chat first-run starter state、Home launcher polish 与响应式兜底纳入 `0.2.x` 主线。
- 修正后续版本路线:完整 Canvas 行动层不再占用 `v0.2.5`,顺延为 `v0.2.6`;Chat 体验重塑 + 内联工具卡顺延为 `v0.2.7`;设计系统收口 + Tools widgets 顺延为 `v0.2.8`。
- 更新 `PLAN.md`、`PRD.md`、`DESIGN.md`、`AGENTS.md`、`HANDOFF.md`、`VERSIONING.md`、`docs/planning/v0.2.4-interaction-deep-dive.md` 与 `docs/planning/handoff-prompt-for-coding-agent.md` 的路线编号。
- 保留核心约束:快捷动作仍走 AI 管道;readiness 目前是派生展示/seed,不是完整持久化 completion schema;mock/static fallback 不移除。

## v0.2.4 - 2026-07-02

**纯文档规划轮 —— 前端 UI 与用户交互深化规格 + 实现交接提示词。** 不改任何产品代码。

- 新增 `docs/planning/v0.2.4-interaction-deep-dive.md`(v0.2.3 路线图的下钻层,后三轮的交互验收标准):
  - **交互设计哲学五判据**:对话是手画布是脸 / 可点优于可打 / 100ms 反馈定律 / 一屏一个朱砂 / 动效只为"发生了什么"服务。
  - **Chat↔Canvas 联动可见性设计**(本轮核心命题):变更摘要卡(Change Digest,基于 `diffTripState` 纯函数的 day/alert 级 diff,点条目画布定位+金色脉冲)、桌面双向悬停联动(共享 highlightDayNumber)、patch 演出编排(600ms staged reveal + 自动滚动到首个变更卡)、撤销机制(预制 undo 意图 + 本地快照兜底)。
  - **组件级交互规格**:composer(Enter/Shift+Enter、自动长高、乐观清空、轮换占位)、MessageBlock 分层(serif headline → 60ms stagger 伪流式)、等待叙事(100ms/3s/8s 递进)、Day 卡结构与六态、时段块级微操作、完成度六段进度条、出发准备区复选(alert.done 可选字段)。
  - **视觉系统细则**:五级字体层级表、色彩使用规则(朱砂每屏唯一、金禁作文字、五档墨灰)、三层纸面无阴影、图标两档。
  - **毫秒级动效参数总表**(实现即抄表)+ 移动端手势(Chat sheet 70dvh/半收 30dvh、visualViewport 键盘适配)+ 无障碍底线(焦点管理、4.5:1 对比度)。
  - **三轮吸收方案(当时规划)**:v0.2.5 Canvas 行动层+画布交互 / v0.2.6 Chat 体验重塑+内联工具卡 / v0.2.7 设计系统收口+Tools 交互组件;该编号已在 v0.2.5 融合轮顺延为 v0.2.6/v0.2.7/v0.2.8。
- 新增 `docs/planning/handoff-prompt-for-coding-agent.md`:**自包含实现交接提示词**,可直接复制给其他 coding agent——含 60 秒项目背景、必读文档序、八条硬约束(AI 管道/fallback/key/视觉红线/动效/7 文档+双分支推送/并行防护/中文汇报)、三轮任务规格与操作者视角验收。
- 真流式 SSE、抽屉下滑手势、Day 卡拖拽排序:记录为三轮之后候选,本三轮不承诺。

## v0.2.3 - 2026-07-02

**纯文档规划轮 —— 整体项目规划 + 前端 UI 优化迭代路线。** 不改任何产品代码。

- 新增 `docs/planning/v0.2.3-ui-optimization-roadmap.md`,与既有三份蓝图(v0.1.52 交互蓝图 / v0.1.53 技术蓝图 / v0.1.55 布局规范)互补,构成完整设计契约:
  - **宏观**:定位到体验的四条推论(管家≠工具箱、一站式=不离开、FIT 买确定性、智能必须可见);10 项体验差距审计(G1 文字墙、G2 画布被动、G3 变化不可感、G4 无加载态、G5 完成度不可知、G6 工具与对话割裂、G7 样式无系统、G8 空错态未设计、G9 移动端未打磨、G10 五焦虑入口分散),并聚类为后三轮主题。
  - **微观**:逐界面 UI 优化清单——Canvas(完成度进度条/Day 卡快捷动作/patch 动画/出发准备区)、Chat(分块渲染/乐观 UI+骨架屏/内联工具卡/模型标签弱化)、Tools(widget 优先+清单折叠)、移动端专项、空/错状态。
  - **设计系统**:token 层整理、组件库首批(Button/Pill/Card/Sheet/Toast/ProgressMeter/MessageBlock/ToolCard/EmptyState)、动效与反馈准则(100ms 反馈、有含义的动效、reduced-motion)。
  - **后三轮执行承诺**:v0.2.4 Canvas 行动层+画布 UI;v0.2.5 对话体验重塑+内联工具卡(含 ask_factual <150ms 快通道);v0.2.6 设计系统收口+Tools 三件套 widget(汇率换算/签证问答/支付向导)。每轮含功能/UI/测试/边界四栏与验收标准。
- 项目记忆 `CLAUDE.md` 新增操作者指令:**思考、推理、回答、汇报一律中文**(代码与提交信息可保留英文)。
- 版本占位说明:原口头排期的三轮代码迭代因本规划轮占用 v0.2.3 顺延为 v0.2.4/v0.2.5/v0.2.6。

## v0.2.2 - 2026-07-01

**Chat core-loop fixes: speed, Chat↔Canvas sync, and auto-save.** Addresses three reported problems.

### Slow replies
- `lib/ai/orchestrator.ts` now **races all candidate providers in parallel** (`Promise.any`, first valid patch wins) instead of trying them one-by-one. Latency ≈ the fastest healthy model instead of the sum of failing ones.
- `lib/ai/providers/openaiCompatibleProvider.ts` gives every provider call an **18s abort timeout**, so a hung or misconfigured model fails fast instead of stalling the reply.
- The Amap tool-context prefetch is now time-bounded (6s) and best-effort, so a slow POI lookup never blocks the answer.

### Chat and Live Canvas not syncing
- Root cause: when a provider failed (e.g. a model-id mismatch), the orchestrator fell back to the mock butler, which only produced a full itinerary for "first time"/"5 days" messages — every other message returned no `days`, so `applyCanvasPatch` kept the old canvas.
- `lib/mock-ai/mockButler.ts` is now **destination-aware**: it extracts city names (EN + 中文) and a day/week count from the message and generates a matching multi-day skeleton itinerary, so the canvas reflects the chat even in fallback mode.
- `lib/ai/butlerPrompt.ts` system prompt now **requires live models to return the complete `days` array** (and set `tripSummary` title/duration/destinations) whenever the itinerary changes; days may be omitted only for pure factual questions.

### Auto-save
- `components/chat/ButlerWorkspace.tsx` now **auto-saves every chat** to Trips for signed-in users (silent "Saved to your Trips." note), and the manual **"Save to Trips" button was removed**. Guests keep the automatic localStorage draft. The sign-in sync and auto-save no longer double-write.

### Tests
- Updated `orchestrator`, `mockButler`, `chat-workspace`, and `chat-workspace-guest-sync` tests; added parallel-race and destination-skeleton coverage. Full suite 105 passing; production build succeeds.

## v0.2.1 - 2026-07-01

**Version-series reset (operator directive).** The product moves from the `0.1.x` line to the `0.2.x` line. `v0.1.55` was the last `0.1.x` iteration; `v0.2.1` is the new baseline, and every subsequent iteration increments `0.2.x`.

- `package.json` version → `0.2.1`.
- `VERSIONING.md` records the reset and updates the versioning rule to `0.2.x`.
- `CLAUDE.md` project memory records the new versioning rule so future sessions continue from `0.2.x`.
- No product code, behavior, provider, or schema change — version metadata only.

## v0.1.55 - 2026-07-01

**Documentation-only iteration — UX layout & frontend design specification.** Complements the existing v0.1.52 interaction blueprint (what/why) and v0.1.53 technical blueprint (plugin architecture) with the missing design/experience layer (how it looks, lays out, and feels) for the one-stop FIT travel-butler positioning.

- Added `docs/planning/ux-design-and-layout-spec.md` with three parts:
  - **Macro**: the single-surface spatial model (Canvas + Chat as home, not six tabs), a full information-architecture table, and the five-anxiety layout principle (every traveler anxiety must have a resolution path within one tap of the Canvas).
  - **Micro**: wireframe-level page composition and component-level interaction mechanics for Home, the Chat command center + Live Canvas (desktop two-column and mobile stacked), Day detail, Explore, Tools, Translate FAB, Trips, Account, and Admin — including structured-reply block rendering, canvas-patch animation, day quick-actions, and precise Add-to-Trip.
  - **Frontend design system**: formalized design tokens (Warm New Chinese palette, type/space/radius/motion scales), a reusable component library, per-surface visual hierarchy, motion/feedback, mobile-first specifics, and accessibility/i18n rules.
- Mapped each existing roadmap phase (Canvas Action Layer, Inline Tool Cards, TripBlock POI, Translate Everywhere, Tools Widgets, Account, Admin) to its governing design section so implementation has a concrete design contract.
- No product code changes. Synced this branch onto `origin/main` (v0.1.54) first to preserve the parallel session's work — no history was overwritten.

## v0.1.54 - 2026-07-01

**Interaction Shell I code implementation.** This release implements the first code slice planned after the v0.1.52/v0.1.53 strategy documents.

- Added three Home archetype starts for independent FIT travelers: First China 10 Days Essentials, Foodie China, and History & Nature. Each routes to `/chat?archetype=<id>`.
- Added shared archetype configuration in `lib/chat/archetypes.ts`, so Home links and Chat starter prompts use the same labels and Butler prompts.
- Chat now detects `?archetype=` on launch, clears the URL, and sends the matching archetype prompt through the existing Butler pipeline. No direct canvas mutation was added.
- Chat first-run suggestions now show three no-typing FIT starter chips instead of generic prompt buttons.
- The latest structured Butler `nextStep` now appears as a prominent primary action card in the Chat panel and can be clicked to continue the same AI pipeline.
- Trip Canvas now uses the trip title as its main h1 when available, replacing the generic "Live Trip Canvas" heading.
- Trip summary confidence labels now render as traveler-facing copy: Draft → Taking shape, Refined → Looking good, Ready / Ready to save → Travel-ready.
- Added tests for Home archetype routing, Chat first-run starts, `?archetype=` auto-send, primary `nextStep` action, and Canvas title/status wording.
- Updated planning docs so the active next implementation sequence begins at `v0.1.55` Canvas Action Layer after this `v0.1.54` code pass.

## v0.1.53 - 2026-07-01

**Documentation-only strategic planning pass.** No product runtime code changes. This iteration refines VisePanda's architectural design and product goals from a comprehensive traveler-first experience angle, detailing new features and linkages.

- **Offline-First Travel Vault**: Added requirements and designs for a local storage caching architecture. The vault caches the current active Trip Canvas, offline pocket notes, emergency contacts, local bilingual addresses, and essential translate phrasebooks. When the network is lost, the application seamlessly enters "Offline Desk" mode.
- **Cultural Context Interpreter**: Upgraded AI Butler requirements to provide cultural context for travelers (e.g., explaining why reservations must be booked 7 days in advance for certain museums, local holiday crowd advisories, and ticketing rules).
- **Intelligent Payment Card Routing**: Added a structural payments wizard requirement mapping foreign credit cards (Visa/Mastercard/Amex) to optimized WeChat Pay and Alipay setup procedures, identifying transaction limits, local ID verifications, and ATMs/cash backups.
- **Contextual Tool Promotion**: Outlined page linkage rules to promote relevant tools in real-time context (e.g., if a user is in Shanghai on Day 2, automatically float the Metro and Alipay guides, and prioritize menu translation).
- **Bilingual Export & Print Kit**: Added design specification to export itineraries as compact, clean bilingual (EN/ZH) cards, maps, and offline print sheets, specifically tailored for taxi drivers and hotel staff.
- Added design documents **ADR-060** through **ADR-063** to `DESIGN.md`.
- Updated `PRD.md`, `PLAN.md`, `AGENTS.md`, and `HANDOFF.md` to establish the new execution baseline.

## v0.1.52 - 2026-07-01

**Documentation-only strategic interaction iteration.** No product runtime code changes. This iteration defines the deeper product/UX direction for the next implementation rounds.

- Added `docs/planning/v0.1.52-product-interaction-blueprint.md` as the authoritative blueprint for product positioning, journey design, page roles, feature linkage, roadmap, UX writing, metrics, and implementation guardrails.
- Repositioned VisePanda as a China travel operating system for foreign visitors rather than only an AI itinerary generator.
- Defined the five anxieties the product must reduce: entry, payment, connectivity, language, and itinerary.
- Defined the core loop: intent/archetype → preference extraction → live tools/data → Trip Canvas source of truth → Chat explanation → small next-step controls → Trips readiness/continuity.
- Defined the journey model: Curious → Planning → Preparing → In China → Share/Get help.
- Reassigned page roles: Home starts archetypes; Chat is the command center; Canvas is the operational trip object; Trips handles continuity/readiness/sharing; Explore feeds Chat/Canvas; Tools resolves anxieties as contextual widgets/cards; Translate becomes an everywhere utility; Account handles trust/preference/consent/lead capture; Community supports inspiration and proof.
- Updated the original roadmap so the active sequence now treats `v0.1.54` as the completed Interaction Shell I code pass, followed by `v0.1.55` Canvas Action Layer and later tracks.
- Updated `PLAN.md`, `PRD.md`, `DESIGN.md`, `AGENTS.md`, `HANDOFF.md`, and `VERSIONING.md` to reflect the new planning baseline.

## v0.1.51 - 2026-07-01

**Three-iteration implementation batch:** Amap rich POI data, first Chat tool-context loop, and lightweight preference memory.

- **v0.1.49 Amap rich POI:** `/api/explore/amap` now requests `extensions=all` and supports Nanjing. A shared `lib/explore/amapSearch.ts` normalizes rating, price, phone, opening hours, photos, business area, location, and source metadata.
- **Explore rich cards:** `ExploreAttraction`, `ExploreFoodSpot`, and `ExploreStay` now carry optional rich metadata. `ExploreBoard` conditionally renders ratings, price level, approximate per-person price, opening hours, phone, source, business area, and thumbnails when Amap provides them, while static fallback cards stay unchanged.
- **v0.1.50 Chat tool context:** the Butler orchestrator now builds a bounded Amap POI context for relevant intents (`create_trip`, `add_poi`, `ask_recommendation`, `logistics`) and injects real POI candidates into the model prompt before planning. If Amap is unavailable or unconfigured, the context is omitted and the normal provider/mock chain continues.
- **v0.1.51 Preference memory:** added `UserPreferenceProfile` extraction for pace, budget, party type, dietary restrictions, cuisine preferences, interests, and confidence. Guest profiles persist in localStorage and are sent to `/api/chat`; Chat shows compact remembered-preference chips.
- **Compatibility:** all new POI and preference fields are optional. Existing mock/static data, saved trips, and plain `assistantMessage` flows remain valid.
- **Tests:** added preference-profile coverage and updated affected Chat/Explore tests.

## v0.1.48 - 2026-07-01

**Chat quality activation for the configured provider keys.** DeepSeek, Qwen, Zhipu, and Moonshot are now aligned to the user's Vercel configuration, and Butler replies can render as structured travel guidance instead of a single plain paragraph.

- Updated the multi-LLM registry defaults to the configured production model choices: DeepSeek v4 flash, Qwen 3.6 Flash, Zhipu GLM5, and Moonshot Kimi 2.5. Env overrides remain available through the existing `*_CHAT_MODEL` variables.
- Extended `CanvasPatch` and `ChatMessage` with optional `assistantResponse` / `response` data: `{ headline, body, highlights, watchOut, nextStep }`.
- Updated `lib/ai/butlerPrompt.ts` so live providers are asked to return the structured response object while still populating `assistantMessage` for backwards compatibility.
- Added parser fallback behavior so older/plain provider JSON is still accepted and converted into a minimal structured response.
- Updated `ChatPanel` to render structured VisePanda replies with a headline, highlights, optional watch-out line, and next-step line while preserving plain text rendering for historical messages and mock fallback.
- Added tests for the new model defaults and structured-response parsing compatibility.

## v0.1.47 - 2026-07-01

**First code iteration of the multi-model track (阶段十三).** The Butler now answers through a real multi-LLM orchestrator instead of a single hardcoded DeepSeek call, following the quality-over-cost principle (ADR-043). All keys are server-side only; the mock fallback is preserved.

- **Provider abstraction** (`lib/ai/providers/types.ts`, `openaiCompatibleProvider.ts`): a `ChatCompletionProvider` interface plus one OpenAI-compatible implementation that covers every Chinese LLM (they all expose an OpenAI-shaped `/chat/completions`).
- **Model registry** (`lib/ai/modelRegistry.ts`): six providers — DeepSeek (reasoning), Qwen/Aliyun Bailian (chinese/vision), Zhipu GLM (reasoning/judge/long-context), Moonshot Kimi (long-context), Baidu ERNIE (china-facts), MiniMax (judge). Each declares capabilities and a server-side key env with aliases; base URL and model are overridable per deployment.
- **Intent classifier** (`lib/ai/intentClassifier.ts`): a fast local regex/keyword classifier (10 intents) whose purpose is quality routing (pick the right specialist), not cost savings.
- **Orchestrator** (`lib/ai/orchestrator.ts`): classify → select candidate providers (specialist first, then a fallback chain) → for high-stakes intents (`create_trip`, `ask_factual`) with 2+ providers run a small parallel ensemble preferring the primary → parse the patch → on total failure fall back to the mock Butler. Returns `mode`, `modelLabel`, `intent`, `strategy`, and `providersTried`.
- **Chat route** (`app/api/chat/route.ts`): now calls `requestOrchestratedButlerPatch` and returns the richer metadata. With zero keys configured it behaves exactly as before (mock); it upgrades automatically as each provider key is added — no code change required.
- **Workspace status** (`components/chat/ButlerWorkspace.tsx`): shows the actual model label (e.g. "Qwen Plus (Aliyun Bailian)") that produced the canvas update, instead of only "DeepSeek/mock".
- **Env documentation** (`lib/env/placeholders.ts`): 16 new keys documented — `ZHIPU_API_KEY`, `MOONSHOT_API_KEY`, `ERNIE_API_KEY`, `MINIMAX_API_KEY`, plus each provider's `*_BASE_URL` / `*_CHAT_MODEL` overrides and `QWEN_CHAT_MODEL`. All server-side only.
- **Mock inventory** (`docs/planning/mock-inventory.md`): a complete list of every mock / placeholder / local-simulation point (22 items) with its real-replacement plan, status, and target phase — per the request to fill in previously-skipped steps with real tools/data/workflows while keeping fallbacks.
- **Project memory** (`CLAUDE.md`): VPCC runs only on explicit request (not automatically); every iteration updates all md docs in detail (especially HANDOFF/PLAN/PRD) and pushes; manual steps get beginner tutorials; security constraints restated. `.claude/commands/vpcc.md` annotated accordingly.
- **Tests**: added `tests/orchestrator.test.ts`, `tests/intentClassifier.test.ts`, `tests/modelRegistry.test.ts` (16 new tests). Full suite 95 passing; the legacy `deepseekButler.test.ts` stays green (that path is retained for back-compat). Production build succeeds.
- **ADR-050** added (`DESIGN.md`): provider-agnostic orchestration on top of OpenAI-compatible endpoints.

## v0.1.46 - 2026-07-01

**Documentation-only iteration — no code changes.** A strategic product-expansion plan covering seven new tracks. Full deep-dive in `docs/planning/v0.1.46-product-expansion.md`; summaries and decisions recorded across the seven workflow docs.

- **Guiding principle changed** (`PRD.md`, `DESIGN.md`): optimize for user experience and answer quality first — **token cost is explicitly not a constraint**. The v0.1.45 "route factual messages away from the LLM to save cost" rationale is reframed: the intent classifier now exists for quality/correctness (route to the right specialist model / verified source), not cost savings. Larger models, multi-model ensembles, and refine-and-verify loops are all in scope.
- **Multi-model Chinese LLM orchestration defined** (§1, ADR-044): a provider-agnostic orchestrator over DeepSeek, Qwen (Aliyun Bailian), Zhipu GLM, Moonshot Kimi, and Baidu ERNIE, with intent-based routing, parallel ensemble + judge for high-stakes answers, a refine-and-verify loop, and a graceful fallback chain that still ends at the mock Butler.
- **Native iOS + Android apps planned** (§2, ADR-045): React Native + Expo recommended (genuinely native, reuses the TypeScript domain layer and API routes) over dual Swift/Kotlin; offline-first travel cache, native camera/mic/push, and a separate China distribution track requiring ICP 备案 / 软著. Plan only — not executed.
- **Tools functional upgrade planned** (§3, ADR-046): the six static Tools become interactive — visa eligibility/transit checker, payment setup wizard, full currency converter, Amap-backed metro route planner, actionable eSIM/VPN options, and one-tap Emergency (call buttons, GPS embassy locator, phrase TTS, share-location).
- **Professional Account UI + lead capture planned** (§4, ADR-047): a dedicated `/account` center (professional, formal, trust-signaling) plus progressive-profiling lead capture (留资) with a prioritized field list — contact (incl. WeChat), trip qualification (nationality/dates/party/budget/cities/purpose), and enrichment with explicit consent.
- **Admin backend + LLM customer briefs planned** (§5, ADR-048): a role-gated `/admin` area where staff see leads and chat conversations, with a multi-model pipeline that distills each customer's data + conversation into a structured `CustomerBrief` (summary, trip intent, budget signal, readiness-to-book score, key preferences, open questions, objections, suggested next action).
- **Supabase schema additions planned** (§6): `0004_leads_and_admin.sql` — `leads`, `lead_events`, `customer_briefs`, `profiles`, and an admin `role`; RLS for admin-wide read; `SUPABASE_SERVICE_ROLE_KEY` server-side only.
- **Frontend & visual optimization track planned** (§7, ADR-049): design tokens + a reusable component library, motion/feedback, empty/error states, accessibility, responsive/tablet polish, performance, and a brand illustration system.
- **Consolidated build sequence recorded** (`PLAN.md` 阶段十三–十八): multi-LLM → Tools functional → Account+leads → admin backend → design system → native apps, with parallelizable legal/ops lead-time work started early.
- **New env keys reserved (server-side only):** `ZHIPU_API_KEY`, `MOONSHOT_API_KEY`, `ERNIE_API_KEY`/`BAIDU_*`, `MINIMAX_API_KEY`. Standing security constraints reaffirmed (mock fallback preserved; service-role key never in browser; only `NEXT_PUBLIC_AMAP_MAPS_KEY` is public).

## v0.1.45 - 2026-07-01

**Documentation-only iteration — no code changes.** This is a macro product-planning pass that distills three research threads (Amap/Meituan data enrichment, Chat×Explore×Trips fusion, and chat-efficiency/UX overhaul) into one coherent seven-iteration roadmap.

- **Product positioning sharpened** (`PRD.md`): VisePanda is reframed around the five concrete fears a Western traveler has before a China trip — visa eligibility, payment, connectivity, language, and itinerary anxiety — and every feature is mapped to the fear it resolves. Chat becomes the single thread that holds the other surfaces together.
- **Chat Intelligence pipeline defined** (`PRD.md`, `DESIGN.md`): a five-stage `Input → Classify → Route → Handle → Normalize` pipeline replaces the current "every message is a full LLM call" model. A 10-intent taxonomy (`create_trip`, `adjust_trip`, `add_poi`, `ask_factual`, `ask_recommendation`, `preference_signal`, `concern`, `logistics`, `add_location`, `unclear`) routes factual and preference messages to non-LLM handlers, cutting cost and latency for an estimated 30–40% of traffic.
- **Response normalization schema defined**: a structured `{headline, body, highlights, watchOut, nextStep}` contract replaces the single free-text `assistantMessage` blob so every Butler reply is scannable and consistent.
- **Preference Profile + intent distillation defined**: a `UserPreferenceProfile` extracted silently from natural conversation (no interrogation form), with a strict "one clarifying question per turn, only when a gap would produce a wrong itinerary" rule.
- **Data-fusion architecture defined** (`DESIGN.md`): a tool-calling Butler that calls Amap/Dianping live during planning (`search_pois`, `get_poi_detail`, `search_dianping`), an `ExploreRichMeta` model for ratings/price/hours/photos, a backwards-compatible rich `TripBlock`, and a two-key Amap split (server-side POI key vs. public JS-map key).
- **Seven-iteration roadmap added** (`PLAN.md` 阶段十二): v0.1.46 Chat Intelligence Layer → v0.1.47 Preference Profile → v0.1.48 Amap Enrichment → v0.1.49 Tool-Calling Butler → v0.1.50 Onboarding + Canvas Quick-Actions → v0.1.51 Navigation Restructure → v0.1.52 Dianping/Meituan + Map.
- **UX audit added** (`PRD.md`): user-journey walkthroughs (first-run, ask-visa, refine-day, translate-menu), a redundancy-removal list (provider-status jargon, duplicate labels, developer-facing confidence copy), and a navigation restructure from 6 flat tabs to 4 tabs + a floating Translate action.
- **Meituan/Dianping API application guide recorded** (`HANDOFF.md`): step-by-step registration path on 大众点评开放平台, credential requirements, realistic review timelines, and the recommendation to start the application now because of the multi-week queue.
- **ADR-037 through ADR-042 added** (`DESIGN.md`): intent routing, preference profile, tool-calling loop, rich data model, two-key Amap split, and navigation restructure decisions.
- **Security constraints reaffirmed**: all keys (`DEEPSEEK_API_KEY`, `DASHSCOPE_API_KEY`, `AMAP_API_KEY`, future `DIANPING_APP_KEY`/`DIANPING_APP_SECRET`) stay server-side only; mock/static fallback is never removed; `NEXT_PUBLIC_AMAP_MAPS_KEY` is the only new public key and is domain-whitelisted.

## v0.1.44 - 2026-07-01

- Moved the 6-tab navigation to a **fixed bottom bar** on mobile (`position: fixed; bottom: 0`), so thumbs can reach tabs without stretching to the top of the screen. The header now shows only the brand mark, language switcher, and account icon on mobile.
- Converted the day detail drawer from a narrow right-side panel (`min(430px, 34vw)` = ~133px on a 390px phone) into a **full-width bottom sheet** (80dvh, slides up from the bottom with rounded top corners and a drag-handle hint).
- Constrained the **account menu popover** to `calc(100vw - 28px)` on mobile, preventing it from overflowing the left edge of narrow screens.
- Made **explore city filter pills** scroll horizontally on mobile instead of wrapping into multiple rows.
- Forced **explore POI columns** (Attractions / Food / Stays) to a single column on mobile instead of the auto-fit multi-column grid.
- **Trip detail header** now stacks vertically on mobile (title above action links) instead of trying to fit both on one row.
- **Trip summary actions** (compact controls inside the Live Trip Canvas on Trip Detail) switch to left-aligned and wrap freely on mobile.
- Enlarged **tool card** touch targets on mobile (`min-height: 52px`).
- Added `padding-bottom: calc(64px + env(safe-area-inset-bottom))` to `.app-shell` so page content is never hidden behind the fixed bottom nav on any screen, including notch devices.
- Removed the circular border from nav icons inside the bottom bar; added a 2px active-state line at the top of the active tab for visual feedback.
- Shrunk brand mark logo and title text slightly on mobile to free header height.

## v0.1.43 - 2026-07-01

- Fixed `/api/translate/text` so text translation no longer fails outright when the Qwen/DashScope route is unavailable; the route now falls back to the existing DeepSeek server-side provider before returning a provider-unavailable error.
- Updated the unified Translator UI to show a clearer configuration message when neither Qwen nor DeepSeek translation providers are available.
- Changed Trip Canvas day cards from `Edit` to `View details`; the day drawer is now read-only and shows itinerary details instead of editable form fields.
- Reworked real Trip Detail pages with saved canvases so Continue/Trips/status/archive/share controls live as compact buttons/status copy inside the Live Trip Canvas summary card instead of taking over the top of the page.
- Added tests for Qwen-to-DeepSeek translation fallback, read-only day details, and compact Trip Detail canvas actions.

## v0.1.42 - 2026-07-01

- Simplified `/translate` from four separate cards into one locale-aware translator workspace.
- Translation direction now follows the active website language and Chinese, supporting English, Spanish, Arabic, Japanese, Korean, French, and Chinese through the existing Qwen text route.
- Image translation now exposes two clear actions: Upload Image and Take Photo. Take Photo is visible but disabled on desktop because it is intended for mobile camera capture.
- Voice translation is reduced to one Record button; audio upload and public audio URL controls were removed from the page UI.
- Common phrases and special terms now sit in a horizontal support rail below the two equal source/output text panels.
- Reduced heavy translucent panel styling on Translator so the ink landscape background remains visible, using hairline dividers and very low-opacity paper backing instead.
- Replaced the Account avatar picker artwork with six new panda PNG assets while keeping the existing six avatar IDs stable for localStorage and community compatibility.
- Desktop landscape `/translate` is locked to one viewport with internal overflow only; 1440x900 verification showed no body/html page scroll.
- Updated `/api/translate/text` so non-English target locales are prompted correctly instead of defaulting every non-Chinese target to English.

## v0.1.41 - 2026-07-01

- Fixed "Saving failed" error on Save to Trips in Chat.
- Root cause: `trips.owner_id` FK references `public.users(id)`, but Supabase Auth users only exist in `auth.users` — no `public.users` row → FK violation on every insert.
- Fix 1 (DB): migration `0003_fix_auth_user_sync.sql` adds a trigger that auto-upserts a `public.users` row whenever a new `auth.users` entry is created, plus INSERT/UPDATE RLS policies so existing users can upsert their own row.
- Fix 2 (code): `saveTripCanvas` now upserts the caller's `public.users` row (using `ownerEmail` from the session) before inserting the trip, covering users who signed up before the trigger was applied.
- Fix 3 (code): `appendMessage` failures are now non-fatal (logged as warnings, don't block the save success message).
- Added `console.error` logging in the save catch block for easier diagnosis of future failures.

## v0.1.40 - 2026-07-01

- Tightened the standalone landing home page into a single-viewport desktop layout.
- Reduced hero, header, CTA, and feature-card spacing so the home page reads faster without pushing key content below the fold.
- Made the six feature cards more compact while preserving the route entry points for Chat, Trips, Explore, Tools, Translate, and Community.
- Preserved the v0.1.38 home-page structure, brand mark behavior, LanguageSwitcher, and Account menu entry.

## v0.1.39 - 2026-07-01

- Replaced background image with new golden-line Chinese landscape (mountains, pagodas, traditional buildings on warm cream paper).
- Reduced background gradient overlay opacity across all body and destination-scene variants so the new line-art image shows clearly (was ~0.58, now ~0.16–0.18).
- Day drawer panel overlay also lightened to match.

## v0.1.38 - 2026-06-30

- New landing home page (`/`) replaces the redirect-to-chat default — brand hero with "Start Planning →" CTA and 6 feature cards (Chat, Trips, Explore, Tools, Translate, Community).
- Home page is standalone (no AppShell/NavTabs); has its own minimal top bar with LanguageSwitcher and AccountMenu.
- Brand-mark in AppShell now links to `/` instead of `/chat`.
- Compact interior page headers: section kicker/h1/subtitle made tighter across Trips, Explore, Tools, Community, and Translator via CSS overrides.
- Feature cards on home page each show icon, translated nav label, and one-line description; responsive 3→2→1 column grid.

## v0.1.37 - 2026-06-30

- Added multi-language (i18n) system supporting English, Spanish, Arabic, Japanese, Korean, and French.
- Language preference stored in localStorage (`visepanda:locale`); persists across sessions.
- Custom React context (`I18nProvider`) and `useTranslation()` hook — no external library required.
- Locale bundles dynamically imported per language (only the active locale is loaded).
- Arabic (`ar`) sets `document.documentElement.dir="rtl"` automatically; all other locales use LTR.
- `[dir="rtl"]` CSS overrides for Arabic: card accent borders, day notes, tool modal tips, explore add-note, and phrase-book notes flip to the right side.
- `LanguageSwitcher` component added to the app header (left of the account menu) — compact EN/ES/AR/JA/KO/FR selector.
- Navigation labels (Chat, Trips, Explore, Tools, Translate, Community) translated in all 6 locales.
- Trips Dashboard: heading, filter pills, card labels, status copy, empty state all translated.
- Tools Board: section heading, "Open checklist →", badge labels, "Close", "Offline pocket notes" all translated.
- Explore Board: section heading, column headers (Attractions/Food/Stays), "Add to Trip" translated.
- Community Board: section heading, tab labels (Feed/Hot Spots/Photos) translated.
- Translator Page: section heading and subtitle translated.

## v0.1.36 - 2026-06-30

- Mobile layout overhaul: all pages now scroll naturally on phones (390px viewport).
- Nav bar becomes a 6-icon bottom strip on mobile, fitting all tabs including Community.
- Translator switches to single-column stack on mobile (fixed CSS cascade bug where desktop 2×2 grid overrode the mobile rule).
- Day block thumbnail placeholder hidden on mobile (title already shown in block text).
- Trip status guide hidden on mobile to bring trip cards immediately into view; trip meta table hidden to reduce card height.
- Trip filter pills horizontally scrollable on mobile (no more wrapping).
- Community membership strip horizontally scrollable on mobile (full tier names visible).
- Community board body and all page containers flow with natural height on mobile (no more overflow:hidden clipping).
- Trips summary stats remain 3-column on mobile.
- Added late-cascade `@media (max-width: 760px)` block at end of globals.css to override warm visual system styles that weren't guarded by a min-width breakpoint.

## v0.1.35 - 2026-06-30

- English-only UI pass: all Chinese-only UI labels replaced with English across Tools badges, Community HotSpots filters and actions, Community Photos default emoji, mock trip highlights, and mock butler itinerary attraction names.
- All attraction and place names in mock data now use "English Name (中文名)" bilingual format (e.g. "Forbidden City (故宫)", "The Bund (外滩)").
- Tool card badges changed to English: Required, Pre-trip, Live, Transit, Connectivity, Emergency.

## v0.1.34 - 2026-06-30

- Redesigned Tools page with 6 modal-overlay card dialogs: each tool now appears as a themed card (icon, accent color) in a 3×2 grid; clicking opens a floating modal with tips, checklist sections, and offline notes. ESC and backdrop click close the modal. URL deep links (`?category=<id>`) preserved.
- Fixed Trips dashboard filter button visibility: ALL/DRAFT/READY/SHARED/ARCHIVED filter pills are now always visible above the scrollable trip library, with the status guide moved inside the scroll area. Grid reduced from 5 rows to 4.
- Redesigned Translator as a single-page 2×2 grid: Text (top-left), OCR (top-right), Voice (bottom-left), Phrases (bottom-right) are all visible simultaneously — no tab navigation. Each panel scrolls independently.
- Updated CSS throughout: `.tools-grid` (3×2 card grid), `.tool-card` with per-card accent theming, `.tool-modal-overlay` + `.tool-modal`, `.translator-grid` (2×2 panel grid), `.translator-grid__panel`, and responsive overrides for mobile.
- Updated `tests/tools-board.test.tsx` to mock the static provider directly and updated `tests/translator-page.test.tsx` for the single-page layout.

## v0.1.33 - 2026-06-30

- Added a desktop-first visual layout refresh across Chat, Trips, Explore, Tools, Translate, Community, and Account.
- Tightened the global shell, header, navigation, page titles, card rhythm, and one-page desktop workspace behavior.
- Added a Warm New Chinese visual-system override with warmer paper tones, ink-line dividers, smaller serif headings, compact cards, and unified paper-style inputs.
- Rebalanced Chat so the Live Trip Canvas title is smaller, the right prompt chips stay in two compact rows, the composer/save/status controls no longer overlap, and the page stays locked to one viewport.
- Improved Trips, Explore, Tools, Translate, and Community content areas so headers and filters take less vertical space while long content scrolls inside each page.
- Cleaned visible mojibake from Translator and Community page-level labels, and normalized Translator Text/OCR/Voice controls to stable English labels without changing the Qwen API flow.
- Added `docs/superpowers/specs/2026-06-30-visual-layout-refresh-design.md` to record the approved visual refresh direction.

## v0.1.32 - 2026-06-30

- Removed the old Translate category from `/tools`; translation now remains a dedicated `/translate` main navigation page.
- Converted Tools categories into compact name-only cards. Details stay hidden until a category card is opened, and URL deep links such as `/tools?category=currency` still open the matching drawer.
- Removed Tools provider implementation/status metadata from the page UI, including live-provider labels, coverage copy, next-integration copy, and candidate API-source strings.
- Removed the visible Tools API-priority block from category drawers so users see travel checklists rather than implementation planning text.
- Updated the retained `ButlerReminders` helper so `language` alerts route to `/translate` instead of the removed `/tools?category=translate` category.
- Updated Tools tests to cover the six-category provider, card-first drawer interaction, Translate removal, and hidden provider metadata.

## v0.1.31 - 2026-06-30

- Upgraded the Community page from static mock display to a local interactive MVP.
- Feed now supports local post publishing, type/city filters, likes, saves, comments, read-more detail, and localStorage persistence.
- Photos now supports local photo-card publishing and local likes; real image upload remains planned for Supabase Storage.
- Added a five-level membership system: Bamboo Guest, Panda Explorer, Silk Road Insider, Dragon Pass, and VisePanda Concierge.
- Community authors now show panda avatars and membership badges.
- Added six bundled panda avatar SVG assets and a reusable avatar registry in `lib/account/avatars.ts`.
- Account trigger now displays the selected panda avatar, and the Account popover lets users choose a panda avatar stored in localStorage.
- Added tests for Community MVP interactions and Account avatar selection.

## v0.1.30 - 2026-06-30

- Upgraded the Translator stack to **Aliyun Bailian Qwen** across text translation, OCR, TTS, and STT.
- `/api/translate/text` now uses `qwen-mt-flash` via the DashScope OpenAI-compatible chat completions endpoint, returning translation and optional pinyin JSON.
- `/api/translate/ocr` now uses `qwen3.5-ocr` via the DashScope OpenAI-compatible multimodal endpoint; OCR.space is no longer used.
- Added `/api/translate/tts`, using `qwen3-tts-instruct-flash` through the DashScope multimodal generation endpoint and returning a temporary Qwen audio URL.
- Added `/api/translate/stt`, using `qwen3-asr-flash` with `input_audio` through the DashScope OpenAI-compatible chat completions endpoint.
- Added a **Voice** tab to `/translate` for recording, uploading audio, or pasting a public audio URL; recognized speech is automatically sent through the text translation route.
- Replaced browser `speechSynthesis` in Text, OCR, and Phrase Book with server-side Qwen TTS requests so API keys remain server-only.
- Added `lib/aliyun/qwen.ts` as the shared Bailian/Qwen helper and updated environment placeholders for `DASHSCOPE_API_KEY`, endpoint overrides, and optional model overrides.
- Added tests for the Qwen translator API routes and the Voice tab; full Vitest suite now covers 30 files and 69 tests.

## v0.1.29 - 2026-06-30

- Added **Community page** (`/community`) as the 6th main navigation tab (Globe icon), implementing the Phase 11 framework.
- Three-tab layout: **动态 Feed** (shared trip posts and tips), **热门 Hot Spots** (city-level attraction/food/hidden-gem rankings), **照片 Photos** (photo wall grid).
- `CommunityFeed`: 6 mock posts (trips + tips) with author avatars, city tags, excerpts, hashtags, like/comment counts, and a "Share My Trip" CTA placeholder.
- `CommunityHotSpots`: 12 community-rated hot spots across 5 cities (Beijing, Shanghai, Chengdu, Xi'an, Hangzhou) with star ratings, review counts, traveler tips, and "Add to Trip" buttons (routes to `/chat?add=…` via existing AI pipeline).
- `CommunityPhotos`: 8 mock photo cards with emoji covers, location labels, captions, and like counts; upload CTA placeholder for future Supabase Storage integration.
- Static data in `lib/community/types.ts` and `lib/community/mockData.ts`; no real API yet — community Supabase tables (`posts`, `photos`, `likes`) and high-de/美团 API integration planned for Phase 11.
- Added CSS classes for `.community-board`, `.community-tabs`, `.community-feed`, `.community-post-card`, `.community-photos`, `.community-photo-card`, `.community-hotspots`, `.community-hotspot-card`.
- Updated `tests/nav-tabs.test.tsx` to 6 tabs/SVGs. All 64 tests pass.

## v0.1.28 - 2026-06-30

- Added a full-featured **Translator page** (`/translate`) as the fifth main navigation tab (Languages icon).
- **Text translation** (`components/translate/TextTranslator.tsx`): bidirectional EN↔ZH text translation via `/api/translate/text` (DeepSeek server proxy, `DEEPSEEK_API_KEY`); returns translation + pinyin; supports Web Speech API TTS (zh-CN, rate 0.85) and clipboard copy; Ctrl+Enter shortcut.
- **OCR scan translation** (`components/translate/OcrTranslator.tsx`): upload or camera-capture image → client-side Canvas API resize (max 1200 px) → `/api/translate/ocr` (OCR.space, `OCR_SPACE_API_KEY` or free "helloworld" key, language `chs`, engine 2) → auto-translates recognized Chinese text to English; TTS for Chinese output; drag-and-drop zone.
- **Phrase book** (`components/translate/PhraseBook.tsx`): 44 static phrases across 6 categories (Greetings, Dining, Transport, Shopping, Emergency, Hotel) and 28 special terms across 3 categories (Attractions, Dishes, Signs); each item shows Chinese, pinyin, English, optional notes/context, and a TTS speak button.
- New server routes: `app/api/translate/text/route.ts` and `app/api/translate/ocr/route.ts`; both keys are server-only, never exposed to the browser.
- Static phrase data in `lib/translate/phrases.ts` and `lib/translate/types.ts`.
- `ToolsBoard` Translate category gains a data-driven CTA link (`/translate`) via new optional `cta?: { label; href }` field on `ToolCategory`.
- Removed `ButlerReminders` rendering from `TripCanvas` (component file kept); updated canvas tests accordingly.
- Added CSS classes for `.translator-page`, `.translator-tabs`, `.text-translator`, `.ocr-translator`, `.phrase-book`, `.tools-category-cta`.
- Community page planning added to PLAN.md (Phase 11), PRD.md, DESIGN.md, AGENTS.md, and HANDOFF.md — no implementation yet.
- Added `tests/translate-api.test.ts` and `tests/phrase-book.test.tsx`; updated `tests/nav-tabs.test.tsx` to 5 tabs/SVGs and `tests/canvas-components.test.tsx` to remove ButlerReminders assertions. All 64 tests pass.

## v0.1.27 - 2026-06-30

- Connected real-time exchange-rate data via ExchangeRate-API: new `/api/exchange-rate` server-side route fetches CNY-base rates hourly using `EXCHANGE_RATE_API_KEY` (env var, never exposed to browser); new `lib/tools/liveToolsProvider.ts` wraps the static provider and injects live rate rows into the `currency` category when the API is reachable, falling back to static text when it is not.
- Connected Amap live POI data for Explore: new `/api/explore/amap` server-side route fetches tourist attractions (`types=110000`), restaurants (`050000`), and hotels (`100000`) per city using `AMAP_API_KEY`; new `lib/explore/amapProvider.ts` wraps the static provider and falls back city-by-city to static data when the API is unreachable.
- `lib/tools/index.ts` now returns `createLiveToolsProvider()` instead of `createStaticToolsProvider()`; `lib/explore/index.ts` now returns `createAmapExploreProvider()` instead of `createStaticExploreProvider()`. Static providers are fully preserved as fallbacks.
- All 53 existing tests continue to pass because live API calls fail gracefully (try/catch → null/empty) in the Vitest environment, leaving static data as the result.

## v0.1.26 - 2026-06-30

- Added `ButlerReminders` — a lightweight alert list rendered below the day timeline in `TripCanvas` (not at the top, not the removed five-card grid).
- Each `ButlerAlert` maps by `type` to a Tools category id (`visa`→`visa-and-entry`, `payment`→`payment-setup`, `language`→`translate`, `transport`→`metro`, `risk`/`emergency`→`emergency`) and renders as an `<a href="/tools?category=<id>">` link; unmapped types (`booking`, `weather`) render as plain text.
- `CanvasTaskStrip.tsx` remains unused — not restored, per the existing constraint against the removed top task-card grid.
- Added `tests/butler-reminders.test.tsx` covering mapped/unmapped alert rendering and updated `tests/canvas-components.test.tsx` since reminders are now intentionally rendered with working deep links.

## v0.1.25 - 2026-06-30

- Added Tools provider readiness metadata so the Tools layer documents static mode, coverage, candidate live data sources, limitations, and the first integration priority.
- `ToolsBoard` now renders the provider status alongside category content without hardcoding provider-specific copy in the component.
- Added provider status tests for the Tools abstraction.

## v0.1.24 - 2026-06-30

- Added Explore provider readiness metadata for static mode, current coverage, candidate data providers, limitations, and the next integration target.
- `ExploreBoard` now shows the provider status so users and future agents can see that the page is static today and prepared for POI/place-detail provider validation.
- Added provider status tests for the Explore abstraction.

## v0.1.23 - 2026-06-30

- Added destination-aware background scene selection for the Trip Canvas route.
- `TripCanvas` now syncs the active destination scene to the document body, and CSS switches the ink-wash atmosphere for Beijing, Shanghai/Jiangnan, Hangzhou/Suzhou, Chongqing, or the default China ink landscape.
- Added tests for the destination-to-scene mapper.

## v0.1.22 - 2026-06-30

- Implemented the Tools practicalization pass: every static Tools category now has structured sections, offline pocket notes, and an API priority note.
- `lib/tools/types.ts` and `lib/tools/staticProvider.ts` now model `sections`, `offlineTips`, and `apiPriority` so future real providers can fill the same fields without changing `ToolsBoard`.
- `components/tools/ToolsBoard.tsx` renders category tips, grouped checklist sections, offline-readable notes, and the next API integration priority.
- Expanded Tools tests to require structured sections, offline content, and API priority metadata.

## v0.1.21 - 2026-06-30

- Expanded Explore static data with Guangzhou, Hangzhou, Suzhou, and Chongqing across city summaries, attractions, food, and stays.
- Updated Explore Add to Trip messaging so every item sends the user back to Chat with a request for VisePanda to rebalance the route around the selected place.
- Added a visible note explaining that Add to Trip reopens Chat and updates the canvas through the AI planning pipeline.
- Expanded Explore provider and board tests for the new cities and rebalanced Add to Trip message.

## v0.1.20 - 2026-06-30

- Added a Trips status guide that explains Draft, Ready, Shared, and Archived states in plain language.
- Trip Detail now shows the current status meaning and next recommended action for both real Supabase-backed trips and example trips.
- Shared status copy now lives in `lib/trips/mockTrips.ts` so dashboard cards and detail pages use the same language.
- Expanded Trips Dashboard and Trip Detail tests for the status guide behavior.

## v0.1.19 - 2026-06-30

- Implemented task 5.2: Tools category deep links.
- `components/tools/ToolsBoard.tsx` now reads `/tools?category=<tool-category-id>` on mount and opens the matching category, falling back to the default category for invalid values.
- Category clicks now update the URL with `history.replaceState`, making selected Tools categories copyable and reusable from future Chat/Canvas reminder entry points.
- Added a subtle active background highlight for the selected Tools category.
- Expanded `tests/tools-board.test.tsx` to cover URL-selected categories and invalid-category fallback.
- Replaced the letter placeholders in the top navigation with Lucide icons for Chat, Trips, Explore, and Tools.
- Tightened desktop page density across Chat, Trips, Explore, and Tools: smaller headers, slimmer summary cards, tighter spacing, and internal scroll containers so the page itself stays locked to one viewport.
- Added `tests/nav-tabs.test.tsx` to cover the icon-backed primary navigation.

## v0.1.18 - 2026-06-29

- Implemented task 5.1: upgraded `/tools` from a placeholder into a real static-provider-driven skeleton covering 7 categories — Visa and entry, Payment setup, Translate, Currency, Metro, eSIM/VPN, and Emergency.
- Added `lib/tools/types.ts` (`ToolsProvider` interface, `ToolCategory` type), `lib/tools/staticProvider.ts` (static reference checklists per category), and `lib/tools/index.ts` (`getToolsProvider()` factory — the only entry point components may call).
- Added `components/tools/ToolsBoard.tsx` and replaced the Tools placeholder page with it: a category list and a detail panel showing that category's summary and tips.
- Currency/Translate copy explicitly states that live exchange-rate conversion and machine translation aren't wired up yet, to avoid implying real-time data.
- Added `tests/tools-provider.test.ts` and `tests/tools-board.test.tsx` covering the static provider's category list and the board's category-switch interaction.

## v0.1.17 - 2026-06-29

- Implemented task 4.4: Explore "Add to Trip" flow.
- `components/explore/ExploreBoard.tsx`: every attraction/food/stay item now has an "Add to Trip" button that navigates to `/chat?add=<encoded draft message>` (e.g. "Add Forbidden City in Beijing to my trip.").
- `components/chat/ButlerWorkspace.tsx`: added a one-time mount effect that reads the `add` URL param, clears it via `history.replaceState`, and calls the existing `handleSend` so the new content always goes through `/api/chat` → `CanvasPatch` → `applyCanvasPatch`, never a direct UI-side canvas write.
- Added `.explore-add-button` styles in `app/globals.css`.
- Added a navigation test in `tests/explore-board.test.tsx` and an auto-send test in `tests/chat-workspace.test.tsx`.

## v0.1.16 - 2026-06-29

- Implemented task 2.5: replaced the standalone `/account` page with a header icon + popover, and switched login from magic link to email/password and Google OAuth.
- Removed `app/account/page.tsx` and `components/account/AccountPanel.tsx`; removed `"account"` from `NavTabs`'s `AppTab` union and tab list.
- Added `components/account/AccountMenu.tsx`: a header icon button that toggles a popover. Shows guest messaging when Supabase isn't configured, a sign-in/sign-up email+password form plus a "Continue with Google" button when signed out, and Change name / Change password / Log out actions when signed in. Mounted in `AppShell`'s header next to `NavTabs`.
- `lib/supabase/auth.ts`: removed `signInWithMagicLink`; added `signInWithPassword`, `signUpWithPassword`, `signInWithGoogle`, `updateDisplayName`, `updatePassword`.
- Added `.account-menu*` styles in `app/globals.css` for the trigger button and popover.
- Added `tests/account-menu-guest.test.tsx`, `tests/account-menu-signin.test.tsx`, `tests/account-menu-signedin.test.tsx` covering the unconfigured, signed-out, and signed-in states; removed `tests/account-panel.test.tsx`.

## v0.1.15 - 2026-06-29

- Implemented tasks 4.1 and 4.2: Explore skeleton and provider abstraction.
- Added `lib/explore/types.ts`: `ExploreCity`, `ExploreAttraction`, `ExploreFoodSpot`, `ExploreStay` domain types and the `ExploreProvider` interface.
- Added `lib/explore/staticProvider.ts`: `createStaticExploreProvider()` with static data covering Beijing, Shanghai, Chengdu, and Xi'an.
- Added `lib/explore/index.ts`: `getExploreProvider()` factory — the only entry point components are allowed to call; swapping in a real Amap/Trip.com/Meituan provider later only requires changing this file.
- Added `components/explore/ExploreBoard.tsx` and replaced the Explore placeholder page with it: city filter buttons, a city summary card, and an Attractions/Food/Stays column layout that reloads when the active city changes.
- Added `tests/explore-provider.test.ts` and `tests/explore-board.test.tsx` covering the static provider's filtering behavior and the board's city-switch interaction.

## v0.1.14 - 2026-06-29

- Implemented task 3.5: trip archive state and share links.
- Added `supabase/migrations/0002_trip_archive_and_share.sql`: extends the `trips.status` check constraint to allow `archived`, and adds RLS policies so anyone can read a `trips`/`canvas_versions` row once `share_token` is set.
- Added `updateTripStatus`, `createShareLink`, `revokeShareLink`, and `loadSharedTrip` to `lib/supabase/tripsRepository.ts`.
- `TripDetail` now exposes Mark as Ready / Archive / Restore from archive buttons, plus Get share link / Revoke share link actions with a live status message and the full share URL.
- Added `app/share/[token]/page.tsx` and `components/share/ShareView.tsx`: a public, unauthenticated, read-only page that renders a shared trip's saved canvas without exposing chat history.
- `lib/trips/mockTrips.ts` gained an `archived` status and a fourth example trip; `TripsDashboard` filters now include "Archived".
- Added `tests/trip-detail-actions.test.tsx` and `tests/share-view.test.tsx` covering the new archive/share flows and the public share page.

## v0.1.13 - 2026-06-29

- Implemented task 3.4: trip detail page (`/trips/[id]`).
- Added `components/trips/TripDetail.tsx`: shows the real saved canvas (via `TripCanvas`) for signed-in Supabase-backed trips, falls back to an example-trip summary for mock trips, and shows a not-found notice otherwise.
- Added a "View details" link on each Trips Dashboard card alongside the existing "Continue in Chat" link.
- Added `tests/trip-detail.test.tsx` covering the mock-trip and not-found paths.

## v0.1.12 - 2026-06-29

- Implemented task 2.3: guest draft to logged-in synced trip migration path.
- `ButlerWorkspace` now persists an in-progress guest (not signed in, not yet saved) trip draft to `localStorage` under `visepanda:guest-draft` and restores it on remount.
- When a guest signs in via magic link while a local draft exists, the draft is now automatically saved to Supabase (`saveTripCanvas` + `appendMessage`) without the user needing to click "Save to Trips" again.
- The local draft is cleared once a trip is associated with a signed-in session (either restored from Supabase or freshly saved).
- Added `tests/chat-workspace-guest-sync.test.tsx` covering the auto-save-on-sign-in flow, and a guest-draft persistence/restore test in `tests/chat-workspace.test.tsx`.

## v0.1.11 - 2026-06-29

- Added a Supabase browser client and a `isSupabaseConfigured` guard so missing project keys never crash the app.
- Added magic-link sign-in/sign-out (`lib/supabase/auth.ts`) and a `useSupabaseSession` hook.
- Replaced the Account placeholder with `AccountPanel`: email magic-link form when Supabase is configured, guest-mode messaging when it is not.
- Added `lib/supabase/tripsRepository.ts` with `saveTripCanvas`, `listTripsForOwner`, `loadTripWithCanvas`, and `appendMessage`, all RLS-scoped to the signed-in user.
- Added a "Save to Trips" action in the Chat workspace that writes the current canvas to `trips` + `canvas_versions` and syncs chat history to `messages`.
- Trips Dashboard now loads real saved trips for signed-in users when Supabase is configured, and falls back to the existing mock trips otherwise.
- "Continue in Chat" now passes the saved trip id so the Chat workspace can restore that canvas via `/chat?trip=<id>`.
- No live Supabase project is connected yet; all of this activates once `NEXT_PUBLIC_SUPABASE_URL`, `NEXT_PUBLIC_SUPABASE_ANON_KEY`, and the migration are in place.

## v0.1.10 - 2026-06-29

- Designed the Supabase schema for `users`, `trips`, `canvas_versions`, and `messages` (task 2.2).
- Added `supabase/migrations/0001_init_trip_schema.sql` with table definitions, indexes, foreign keys, and row-level security policies scoped to trip owners.
- Added `lib/supabase/schema.ts` with TypeScript row types matching the migration, reusing `TripState`, `ChatMessage`, and `SavedTripStatus`.
- No live Supabase project is connected yet; this is the schema contract that task 3.3 persistence work will implement against.

## v0.1.9 - 2026-06-29

- Redesigned Live Trip Canvas day cards into a vertical Day 1 / Day 2 / Day 3 timeline.
- Added Morning / Afternoon / Evening blocks directly inside each day card.
- Removed the five top butler task cards from the canvas surface.
- Upgraded the day detail drawer into a local editor for city, day blocks, hotel, transport, and notes.
- Added component coverage for the new editable canvas workflow.

## v0.1.8 - 2026-06-29

- Upgraded `/trips` from a placeholder page into a saved trips dashboard skeleton.
- Added three mock trip cards with route, date, length, traveler, status, highlights, butler task count, and summary copy.
- Added All / Draft / Ready / Shared filters and summary metrics for the currently visible trips.
- Added Continue in Chat links so saved-trip work can return to the AI Butler flow.
- Added Trips Dashboard component tests.

## v0.1.7 - 2026-06-29

- Removed the default demo conversation from the chat page now that live AI is connected.
- Changed suggested prompts to a stable two-column layout instead of a clipped horizontal row.
- Added two context-aware follow-up questions to `/api/chat` responses.
- Updated the chat panel so suggestions refresh after each AI answer.

## v0.1.6 - 2026-06-29

- Added a DeepSeek V4 Flash provider for `/api/chat`.
- Routed chat submissions through the server API so provider keys stay server-side.
- Kept deterministic mock fallback for missing keys, API failures, or invalid model output.
- Updated environment placeholders to use `DEEPSEEK_API_KEY`, `DEEPSEEK_BASE_URL`, and `DEEPSEEK_MODEL`.

## v0.1.5 - 2026-06-29

- Removed day itinerary details from the main Trip Canvas surface.
- Kept each day card to a one-sentence daily summary with a details action.
- Changed the day detail view into a closed-by-default side drawer.
- Fixed the desktop landscape workspace to one viewport with internal scrolling areas.
- Removed the standalone Practical Reminder rail and merged butler reminders into the top task cards.

## v0.1.4 - 2026-06-29

- Replaced the temporary `VP` header mark with the panda icon from the supplied brand manual.
- Kept the current warm New Chinese interface direction instead of applying the full brand manual system.

## v0.1.3 - 2026-06-29

- Reduced the Live Trip Canvas heading so it takes less desktop workspace.
- Changed day cards into one-line itinerary summaries for faster scanning.
- Added a click-through day detail drawer for daily blocks, food, stay, transport, and notes.
- Kept this iteration focused on desktop landscape layout; mobile portrait refinement is deferred.

## v0.1.2 - 2026-06-29

- Restyled the Chat workspace toward the approved open ink-painting concept.
- Removed the large glass-like chat container and shifted the right side to an open conversation rail.
- Added a thin vertical divider between the live trip canvas and the chat rail.
- Added the canvas task strip for visa, payment, booking, pace, and food-focused butler work.
- Updated the trip summary and day cards to feel more integrated with the paper background.
- Recorded the production domain as `go2china.space`.

## v0.1.1 - 2026-06-29

- First working AI Butler Chat MVP skeleton.
- Added the two-column Chat + Live Trip Canvas workspace.
- Added mock canvas patching, trip cards, butler alerts, placeholder tabs, tests, and Vercel-ready structure.

## Versioning Rule

- Default iteration format is `0.1.x`.
- Each product iteration must update `package.json` and this changelog.
- Use a custom version only when the user explicitly provides one.
