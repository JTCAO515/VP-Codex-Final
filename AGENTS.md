# VisePanda — Agent 操作指南

## 项目概述

VisePanda 是一个面向外国人来中国旅行的 AI 管家产品。当前主线是：右侧持续 Chat，左侧 Live Trip Canvas 实时刷新；Trip Canvas 会根据当前目的地自动切换水墨背景氛围；Trips 已从占位页升级为真实 Supabase persistence + 归档/分享流程，并有共享状态说明；Explore 已从占位页升级为静态 provider 驱动的城市/景点/美食/住宿骨架，预留真实第三方 provider 接入点，展示 provider readiness，并接入了 Add to Trip 流程（跳转 Chat 后走真实 AI pipeline 加入画布并重新平衡路线）；Account 已从独立页面改为头部图标 + 悬浮窗口，支持邮箱密码登录/注册和 Google 登录，登录后可改名/改密码/登出；Tools 已从占位页升级为静态 provider 驱动的签证入境/支付设置/翻译/汇率/地铁/eSIM-VPN/应急 7 个分类骨架，并支持 `/tools?category=<tool-category-id>` 分类深链、结构化分组、离线 pocket notes、API priority 和 provider readiness；Translate 已作为第五个主导航 Tab，支持文字翻译（DeepSeek）、OCR 扫描翻译（OCR.space）和常用短语/特殊词语词典（静态数据 + TTS）；`ButlerReminders` 已从 TripCanvas 移除（组件文件保留）。

## 核心技术栈

- 前端：Next.js App Router、React、TypeScript。
- 图标：lucide-react（顶部导航等常规 UI 图标优先使用）。
- AI：DeepSeek V4 Flash provider + mock fallback。
- 数据库：Supabase（`v0.1.11` 起接入真实登录和 trips/canvas_versions/messages persistence；`v0.1.12` 起 guest draft 登录后自动迁移；`v0.1.16` 起登录方式改为邮箱密码 + Google OAuth；未配置环境变量时优雅回落到 guest/mock 体验）。
- 部署：Vercel，生产域名 `go2china.space`。
- 测试：Vitest、Testing Library、Playwright。

## 工作流程

1. 读 `PLAN.md` 了解当前阶段。
2. 读 `PRD.md` 了解需求。
3. 读 `DESIGN.md` 了解架构和 ADR。
4. 每次修改前先确认是否会扩大当前阶段范围。
5. 修改核心逻辑前先写测试，遵循 test-first。
6. 每完成一个功能后运行相关测试。
7. 每次迭代都必须同步更新 `PLAN.md`、`PRD.md`、`DESIGN.md`、`AGENTS.md`、`HANDOFF.md`。
8. 每次迭代都必须更新版本号，默认格式 `0.1.x`，除非用户手动指定版本号。
9. 离开前更新 `HANDOFF.md`，记录状态、已知问题和下一步。

## 编码规范

- 命名：组件使用 PascalCase；函数和变量使用 camelCase；类型使用 PascalCase。
- 类型：核心数据结构优先放在 `lib/types/` 或对应 domain 的 `lib/*/` 中，避免组件里重复定义。
- AI 输出：必须通过 `CanvasPatch` 结构进入画布，不要让 UI 直接解析自然语言。
- Trips 数据：`v0.1.8` 使用 `lib/trips/mockTrips.ts`；接 Supabase 前不要伪造真实 persistence。
- 环境变量：缺 key 不应导致页面崩溃。
- 注释：只在复杂逻辑前写简短说明，避免空泛注释。
- 测试：核心逻辑、provider fallback、API route、组件交互必须覆盖。

## 重要约束

- 不要把真实 API key 写进仓库。
- DeepSeek key 只允许通过 `DEEPSEEK_API_KEY` 等服务器端环境变量读取，不允许传到浏览器或写入文档。
- 不要删除 mock provider；真实 AI 接入后也必须保留 fallback。
- 不要使用半透明玻璃聊天框。
- 不要恢复 Canvas 顶部五个任务框：Visa / Payment / Booking / Less tiring / Food-focused 已被移除。
- 不要在 Trip Canvas 主界面直接展开长篇每日详情；主界面保留 Day 时间线和 Morning / Afternoon / Evening 摘要，完整详情和编辑进入抽屉。
- `/chat` 初始态不要放演示对话；真实 AI 接入后由用户第一句话开始对话。
- 建议问题属于聊天 UI 状态，不要写入 `CanvasPatch`；每次 AI 回复后刷新为 2 个上下文相关问题。
- 不要重新加入独立 Practical Reminder / Butler Rail 区块；`ButlerReminders` 组件（`v0.1.26`）文件保留但已在 `v0.1.28` 从 TripCanvas 移除；不允许做成恢复的顶部任务卡或固定 5 项布局。
- 桌面横屏端优先保持一屏固定工作台；移动竖屏细节可以后续再精修。
- 不要让移动导航遮挡核心内容。
- 顶部 Chat / Trips / Explore / Tools / Translate 导航使用 lucide-react 线性图标，不要退回字母占位；新增 Tab 时必须同步更新 `AppTab` 类型、`NavTabs` 的 tabs 数组，并在对应 `app/<page>/page.tsx` 传入 `activeTab="<key>"`。
- 翻译工具 (`/translate`) 是第五个主 Tab，三 Tab 布局（文字翻译/扫描翻译/短语词典）；新增翻译相关 API 时必须通过服务端代理路由（`/api/translate/*`），不允许在客户端直接传 API key；TTS 使用 `window.speechSynthesis`（无需后端），STT 规划中（`SpeechRecognition`，当前显示"Coming soon"）。
- 社区 (`/community`) 是第六个主 Tab（Globe 图标），三 Tab 布局（动态 Feed / 热门 Hot Spots / 照片 Photos）；当前使用 `lib/community/mockData.ts` 静态 mock 数据；Hot Spots 的 Add to Trip 必须走 `/chat?add=…` → `ButlerWorkspace.handleSend` → `/api/chat` 路径，不允许绕开 AI pipeline；后续接入 Supabase posts/photos/likes 表和高德/美团 API 时，只在 provider 层替换数据源，不改 `CommunityBoard` 组件结构。
- 桌面端 Chat / Trips / Explore / Tools / Translate 必须保持一屏锁定；展示不下的内容放进内部滚动容器，不要让 `body` 或页面级主区域纵向滚动。
- Trips/Chat 现在有真实 Supabase persistence 首个闭环（保存、读取、恢复），guest draft 登录后会自动迁移，且已有 trip detail 页面（`/trips/[id]`），支持归档/恢复状态切换和分享链接生成/撤销，分享链接对应只读公开页面 `/share/[token]`。
- Trips 的 draft / ready / shared / archived 状态说明和下一步动作集中在 `lib/trips/mockTrips.ts`，Dashboard 和 Trip Detail 都要复用，不要在组件里各写一套状态语义。
- 运行 `supabase/migrations/0002_trip_archive_and_share.sql` 之前，分享和归档相关数据库操作会因为 RLS policy 缺失而失败；该迁移必须在 0001 之后追加执行到真实 Supabase 项目。
- Explore 数据必须只通过 `lib/explore/index.ts` 的 `getExploreProvider()` 获取，不要在组件里直接 import `staticProvider.ts` 或硬编码城市数据。`v0.1.27` 起工厂返回 `createAmapExploreProvider()`（`lib/explore/amapProvider.ts`），该 provider 通过内部 `/api/explore/amap` 路由调用高德 POI 搜索，每个城市/分类请求返回空时回落到 staticProvider；后续接入 Trip.com/Meituan 时只新增模块并在工厂里切换，不改 `ExploreBoard` 组件。
- Explore provider 必须实现 `getProviderStatus()`；页面展示的 provider mode、coverage、candidate providers、next integration 和 limitations 应来自 provider 层，不要在 `ExploreBoard` 里硬编码。
- Explore 的 Add to Trip 必须通过「跳转 `/chat?add=<草稿消息>` 并由 `ButlerWorkspace` 调用既有 `handleSend` → `/api/chat` → `CanvasPatch` → `applyCanvasPatch`」流程加入画布，不允许在 `ExploreBoard` 或其他非 Chat 组件里直接拼装/合并 `TripDay`、`TripState` 或绕开 AI pipeline 写入画布；Add to Trip 文案应明确由 VisePanda 重新平衡路线。
- Tools 数据必须只通过 `lib/tools/index.ts` 的 `getToolsProvider()` 获取，不要在组件里直接 import `staticProvider.ts` 或硬编码分类数据。`v0.1.27` 起工厂返回 `createLiveToolsProvider()`（`lib/tools/liveToolsProvider.ts`），该 provider 通过内部 `/api/exchange-rate` 路由获取实时 CNY 汇率注入 currency 分类，API 不可达时回落到 staticProvider；翻译/签证规则/交通数据暂时仍为静态清单，后续接入时只新增实现 `ToolsProvider` 接口的模块并在工厂里切换，不改 `ToolsBoard` 组件。
- Tools `ToolCategory` 必须包含 `tips`、`sections`、`offlineTips`、`apiPriority`；组件只渲染这些字段，不要在 `ToolsBoard` 里新增分类专属硬编码文案。
- Tools provider 必须实现 `getProviderStatus()`；页面展示的 provider mode、coverage、candidate sources、next integration 和 limitations 应来自 provider 层，不要在 `ToolsBoard` 里硬编码。
- Tools 分类深链必须使用 `/tools?category=<tool-category-id>`；`ToolsBoard` 会读取该参数并在无效时回退默认分类；点击分类时用 `history.replaceState` 更新 URL。后续 Chat/Canvas/提醒入口需要跳 Tools 时复用这个参数，不要为了入口恢复 Canvas 顶部五个任务框。
- `components/tools/ToolsBoard.tsx` 必须保留对 `?category=<id>` URL 参数的读取和预选中逻辑，供 `ButlerReminders` 等深链入口使用；新增分类 id 时要同时更新 `lib/tools/staticProvider.ts` 和任何引用具体分类 id 的映射（例如 `ButlerReminders` 里的 `alertActionHrefMap`）。
- `ButlerReminders` 的 alert 类型到 action href 映射集中在 `components/canvas/ButlerReminders.tsx` 的 `alertActionHrefMap`；Tools-backed alerts 继续使用 `/tools?category=<id>`，`language` 必须直接链接 `/translate`；`booking`/`weather` 等无对应入口的类型渲染为纯文本 action，不生成链接；新增 alert 类型时在映射表里添加即可，不需要改 `TripCanvas` 或 `ToolsBoard`。
- 目的地背景切换由 `lib/visual/destinationBackground.ts` 根据 `TripSummary.destinations` 推导；不要在页面组件中散落城市匹配规则。当前使用 CSS 场景层叠加同一张 `ink-landscape.png`，后续若换真实城市背景图，也应保留该 helper 作为映射入口。
- 所有 Supabase 读写必须经过 `lib/supabase/tripsRepository.ts`，复用已确认的 `users`/`trips`/`canvas_versions`/`messages` 表结构，不要另起字段命名、拆分表，也不要绕开 repository 直接在组件里拼 Supabase 查询。
- Supabase 相关代码必须在未配置环境变量、未登录、网络失败时优雅降级（不崩溃，回落到 guest/mock 体验），参考 `lib/supabase/client.ts` 的 `isSupabaseConfigured` 模式。
- `SUPABASE_SERVICE_ROLE_KEY` 当前未在任何代码中使用；不要在浏览器端代码中引入它。
- 不要恢复独立的 `/account` 页面或 magic link 登录；账号登录/管理唯一入口是 `components/account/AccountMenu.tsx`（头部图标 + 悬浮窗口），登录方式只允许邮箱密码（`signInWithPassword`/`signUpWithPassword`）和 Google OAuth（`signInWithGoogle`），登录后的改名/改密码必须走 `lib/supabase/auth.ts` 的 `updateDisplayName`/`updatePassword`。

## 常用命令

```bash
npm.cmd run test
npm.cmd run build
npm.cmd run test:e2e
```

## 交接清单

离开前确保：

- [ ] `PLAN.md` 状态已更新。
- [ ] `PRD.md` 已同步本轮产品行为。
- [ ] `DESIGN.md` 已同步本轮架构/交互决策。
- [ ] `AGENTS.md` 已同步新的操作规则或约束。
- [ ] `HANDOFF.md` 已更新。
- [ ] `CHANGELOG.md` 和 `VERSIONING.md` 已更新。
- [ ] `package.json` 和 `package-lock.json` 版本号已更新。
- [ ] 未完成任务已记录。
- [ ] `npm.cmd run test` 已通过，或失败原因已记录。
- [ ] `npm.cmd run build` 已通过，或失败原因已记录。
- [ ] `npm.cmd run test:e2e` 已通过，或失败原因已记录。
- [ ] 没有把真实密钥、临时截图、`node_modules`、`.next` 提交进仓库。
## v0.1.30 Agent Update - Translator Rules

- Translator external calls must go through server-side `/api/translate/*` routes. Do not call DashScope/Bailian directly from client components.
- Text translation uses Aliyun Bailian Qwen `qwen-mt-flash`; OCR uses `qwen3.5-ocr`; TTS uses `qwen3-tts-instruct-flash`; STT uses `qwen3-asr-flash`.
- Use `lib/aliyun/qwen.ts` for shared Qwen endpoint/key/model helpers instead of duplicating provider setup in routes.
- Server-side key variables: `DASHSCOPE_API_KEY` preferred, `ALIYUN_BAILIAN_API_KEY` accepted as alias. Optional overrides: `DASHSCOPE_COMPATIBLE_BASE_URL`, `DASHSCOPE_BASE_URL`, `QWEN_TRANSLATE_MODEL`, `QWEN_OCR_MODEL`, `QWEN_TTS_MODEL`, `QWEN_STT_MODEL`.
- Do not restore browser `speechSynthesis` as the primary Translator TTS path. It may only be considered as a future fallback if product requirements explicitly ask for offline speech.
- STT now supports recording/uploaded audio data URLs and public audio URLs. If future work adds durable audio upload, prefer Supabase Storage or OSS before calling `/api/translate/stt`.

## v0.1.31 Agent Update - Community, Membership, and Avatar Rules

- Community MVP is local-first: do not imply posts, comments, photo cards, likes, or saves are synced across devices until Supabase community persistence is added.
- Preserve `/community` as a one-page desktop workspace with internal scrolling.
- Community Hot Spots Add to Trip must continue to route through `/chat?add=` and the existing AI pipeline.
- Membership levels are defined in `lib/community/membership.ts`; do not duplicate tier names or benefits inside components.
- Panda avatars are defined in `lib/account/avatars.ts` and stored under `public/avatars/`. Account avatar selection uses `visepanda:selected-avatar` in `localStorage`.
- Do not implement real avatar upload or photo file upload until the project adds Supabase Storage buckets, upload validation, image moderation, and profile/media persistence.

## v0.1.32 Agent Update - Tools UI Rules

- `/tools` must not include a Translate category. Translation belongs to the dedicated `/translate` tab.
- Tools category cards are name-only. Do not add summaries, descriptions, badges, or provider labels inside the closed cards unless the product direction changes.
- Tools details are hidden by default. Only open a drawer after a category click or a valid `/tools?category=<tool-category-id>` URL param.
- Clicking the active Tools card should close the drawer and remove the `category` query param.
- Do not render provider status, coverage copy, next-integration copy, candidate API-source strings, or category `apiPriority` copy in the traveler-facing Tools UI.
- `getProviderStatus()` and `apiPriority` may remain in the provider/data layer for internal planning, but user-facing Tools copy should stay practical and non-technical.
- The retained `ButlerReminders` helper must route `language` alerts to `/translate`, not `/tools?category=translate`.
- When changing Tools categories, update `tests/tools-provider.test.ts`, `tests/tools-board.test.tsx`, and any deep-link mappings such as `ButlerReminders`.

## v0.1.33 Agent Update - Visual Layout Rules

- Preserve the v0.1.33 desktop-first visual-system layer in `app/globals.css` unless a later design pass replaces it intentionally.
- Desktop landscape pages should stay one viewport tall; use internal scroll regions for Chat logs, day cards, Trips lists, Explore columns, Tools drawers, Translator bodies, and Community content.
- Keep page headers compact. Avoid large hero-style titles inside product workspaces.
- Chat prompt suggestions must stay two compact rows; do not let prompt buttons stretch into tall cards.
- Chat composer, Save to Trips, and workspace status must remain separated and non-overlapping.
- Use solid paper-style inputs and fine ink dividers; do not introduce glassmorphism panels.
- If visible mojibake appears in primary labels, clean it during the same iteration that touches that page.
- For design-only work, avoid changing provider routes, Supabase persistence, or AI model behavior.

## v0.1.42 Agent Update - Unified Translator Rules

- `/translate` should stay as one unified workspace, not four visible cards or tabs.
- The desktop layout should use two equal top text panels and a horizontal phrase/term rail below.
- Traveler-facing direction must be active site language ↔ Chinese. Keep `/api/translate/text` locale prompts aligned with all supported locales.
- Desktop landscape `/translate` must be one viewport tall with no body/page scroll. Use internal overflow for long recognized text, translations, phrases, and terms.
- Keep the visual surface clean and background-forward. Avoid heavy translucent cards; use hairline dividers and very low-opacity paper backing only where text needs contrast.
- Keep image entry as Upload Image and Take Photo. Take Photo may be disabled on desktop but should remain available for mobile browsers.
- Voice UI should remain one Record button unless the user explicitly asks to restore audio upload or URL transcription controls.
- Preserve existing avatar IDs in `lib/account/avatars.ts` unless there is a migration plan, because community mock data and localStorage may reference them.

## v0.1.44 Agent Update - Mobile Layout Rules

- The 6-tab navigation is `position: fixed; bottom: 0` on mobile (`max-width: 760px`). Do not add inline `style` or component-level classes that conflict with this. If NavTabs layout needs to change, update the CSS rule in `app/globals.css`.
- `.day-drawer-shell` on mobile is a full-width bottom sheet. Do not reintroduce `width: min(430px, 34vw)` or any right-side positioning for the drawer on mobile; it must remain a bottom sheet until a user decision changes the pattern.
- Do not add `padding-bottom` overrides to `.app-shell` from component code; the fixed-nav bottom padding is controlled by the CSS cascade in `globals.css` to avoid conflicts.
- When adding new page sections or modals on mobile, account for the fixed 56–64px bottom nav height. Use `env(safe-area-inset-bottom, 0px)` for notch devices.
- Mobile nav active-state indicator is a 2px top border on the active tab (not the bottom border used on desktop). If changing the active indicator, update both the desktop `::after` rule and the mobile override.

## v0.1.43 Agent Update - Repair Rules

- `/api/translate/text` should keep Qwen/DashScope as the first provider but must preserve the DeepSeek fallback path unless the user explicitly removes it.
- If changing Translator provider behavior, keep all provider keys server-side and update `tests/qwen-translate-api.test.ts`.
- Trip Canvas day drawers are read-only detail views. Do not reintroduce day-edit inputs or a Save Day button unless the user explicitly asks to restore editing.
- Day-card CTA copy should remain `View details`, not `Edit`.
- Real Trip Detail pages with saved canvases should keep status/actions/share controls compact inside the Live Trip Canvas summary card; avoid rebuilding a large top action/status block above the itinerary.

## v0.1.45 Agent Update - Intelligent Chat Pipeline & Data-Fusion Rules

v0.1.45 is a documentation-only planning iteration. The rules below govern the implementation iterations it plans (v0.1.46–v0.1.52, tracked as 阶段十二/tasks 12.1–12.33 in PLAN.md). When you pick up any of those iterations, follow these rules.

**General**

- v0.1.45 itself must not change any code. If asked to "do v0.1.45", only update the seven docs.
- Implement the roadmap in the planned order (v0.1.46 → v0.1.52). Each iteration should be independently shippable and keep the app working end-to-end.
- Never remove the mock/static fallback. Every new provider (Amap enrichment, Dianping, tool calling) must degrade gracefully to the existing static provider and mock Butler when a key is missing or an upstream fails.

**Chat pipeline (v0.1.46)**

- The intent classifier must be local (regex + keyword), fast, and must not call an LLM. Put it in a dedicated module (e.g. `lib/ai/intentClassifier.ts`), not inline in the route.
- `ask_factual` and `preference_signal` messages must be handled without a full Butler LLM call. Answer `ask_factual` from the existing `lib/tools` static data; handle `preference_signal` as a profile update + one-line ack.
- All canvas writes must still flow through the existing `handleSend → /api/chat → CanvasPatch → applyCanvasPatch` pipeline. Do not let any handler assemble `TripDay`/`TripState` directly or bypass `applyCanvasPatch`.
- When adding the `{headline, body, highlights, watchOut, nextStep}` schema, keep `CanvasPatch.assistantMessage` populated (e.g. from `headline`+`body`) for backwards compatibility with any consumer that reads it, and update `parseDeepSeekPatch` + tests.

**Preference profile (v0.1.47)**

- Store the profile via a new provider/repository entry point, not scattered across components. Logged-in → Supabase (new `profiles` table + migration run in order); guest → localStorage. Keep the Supabase path gracefully degrading when Supabase is not configured.
- Extract preferences silently. Do not add a form/questionnaire UI. Respect the one-clarifying-question-per-turn rule.

**Data enrichment & tool calling (v0.1.48–v0.1.49, v0.1.52)**

- `ExploreRichMeta` and the new `TripBlock` fields must be optional. Do not make existing mock data or the read-only day drawer depend on them.
- Keep the provider abstraction intact: components call `getExploreProvider()` / the chat route, never a data source directly. Amap enrichment lives in `amapProvider.ts` + `/api/explore/amap`; Dianping lives behind a new `/api/explore/dianping` + `meituanProvider.ts`, chained static ← amap ← dianping.
- All data-source keys stay server-side: `AMAP_API_KEY`, future `DIANPING_APP_KEY`/`DIANPING_APP_SECRET`. The only public key permitted is `NEXT_PUBLIC_AMAP_MAPS_KEY` (display-only, domain-whitelisted) for the map widget.
- The tool loop in `/api/chat` must be bounded (max ~3 rounds) and reuse existing Amap route logic for `search_pois`. Keep `response_format: json_object`. If DeepSeek V4 Flash lacks function calling, use `deepseek-chat` (V3) for the tool loop and keep Flash for simple adjustments.

**UX & navigation (v0.1.50–v0.1.51)**

- Onboarding archetypes must pre-seed a draft the Butler frames as a suggestion, still routed through the AI pipeline — do not hardcode a finished itinerary.
- Day-card quick-actions send pre-formed Butler intent strings through `handleSend`; they must not mutate the canvas directly.
- When restructuring nav, the Translate FAB and inline tool cards must reuse existing Translator/Tools components; do not fork their logic.
- Follow the established late-cascade CSS override pattern in `app/globals.css` for any mobile layout work.

**Docs**

- Every one of these iterations must update PLAN/PRD/DESIGN/AGENTS/HANDOFF/CHANGELOG/VERSIONING and force-push per the VPCC end-of-iteration protocol.

## v0.1.46 Agent Update - Product Expansion Rules

v0.1.46 is a documentation-only planning iteration. The deep-dive is `docs/planning/v0.1.46-product-expansion.md`. The rules below govern the seven expansion tracks (阶段十三–十八 in PLAN.md). If asked to "do v0.1.46", only update the docs — no code.

**Guiding principle**

- Optimize for UX and answer quality; do not optimize for token/compute cost. Larger models, multi-model ensembles, and refine-and-verify loops are allowed and encouraged where they improve correctness. The only limits are acceptable latency and never fabricating China-specific facts.
- This reframes (does not delete) the v0.1.45 intent classifier: keep it, but use it to route for quality/correctness, not cost savings.

**Multi-LLM orchestration (阶段十三)**

- Put provider descriptions in `lib/ai/modelRegistry.ts` and selection logic in `lib/ai/orchestrator.ts`. Do not scatter model choices across routes/components.
- Wrap every provider behind a common `ChatCompletionProvider` interface. Reuse one OpenAI-compatible client for DeepSeek/Zhipu/Moonshot/MiniMax; keep the existing `lib/aliyun/qwen.ts` for Qwen; add a dedicated ERNIE access-token helper.
- All new keys are server-side only: `ZHIPU_API_KEY`, `MOONSHOT_API_KEY`, `ERNIE_API_KEY`/`BAIDU_*`, `MINIMAX_API_KEY`. Never expose to the browser or write into the repo/docs.
- Every orchestration path must end in the mock Butler fallback. Do not remove it.

**Native apps (阶段十四)**

- Plan only for now. When building: extract a shared core package (`lib/types`, `lib/i18n`, provider interfaces, orchestrator client) rather than forking logic; the existing Next.js API routes are the mobile backend — do not duplicate business logic in the app.
- Default to React Native + Expo unless the user explicitly chooses dual native. Keep Supabase auth/RLS shared.
- Treat China distribution (备案/软著/MIIT) as a legal/ops workstream, not an engineering afterthought.

**Tools functional upgrade (阶段十五)**

- Extend `ToolCategory` with an optional `interactive` descriptor; keep static content as the graceful-degradation fallback. Do not remove the static checklists.
- Keep the `ToolsProvider` abstraction; components call `getToolsProvider()`. New third-party calls go through server routes (`/api/tools/transit`, existing `/api/exchange-rate`) with keys server-side.
- Interactive widgets live in `components/tools/widgets/*`, one per tool, so tools evolve independently.

**Account + lead capture (阶段十六)**

- Add a dedicated `/account` page but keep the existing `AccountMenu` popover working. Reuse `lib/supabase/auth.ts`; do not fork auth.
- Lead capture is progressive — never a single wall-of-forms. Store in the `leads` table via a repository entry point (mirror `tripsRepository` patterns), not ad-hoc queries. Consent must be explicit, timestamped, and source-tagged.
- Do not collect passport/document data without opt-in and encryption; keep core planning usable without any lead form.

**Admin backend (阶段十七)**

- `/admin` is role-gated and must never appear in traveler navigation. Gate every admin API route with a server-side Supabase session + role check.
- `SUPABASE_SERVICE_ROLE_KEY` may be used only in server-side admin code — never in any client bundle. This is a hard constraint.
- The `CustomerBrief` is generated by the orchestrator (long-context model to read the conversation, reasoner to score/act), cached in `customer_briefs`, and regenerated on new data. Always keep raw data available beside the brief. Log PII access.

**Design system (阶段十八)**

- Introduce tokens + a reusable component library incrementally; migrate pages to it rather than rewriting all at once. Preserve the Warm New Chinese palette and the late-cascade mobile CSS pattern.
- Do not regress existing tests or provider flows during visual work.

**Schema**

- New tables land in `supabase/migrations/0004_leads_and_admin.sql` (run in order in the SQL Editor): `leads`, `lead_events`, `customer_briefs`, `profiles`, plus a user `role`. Keep all Supabase code gracefully degrading when Supabase is unconfigured.

## v0.1.47 Agent Update - Multi-LLM Orchestrator Rules (implemented)

The multi-LLM Butler orchestrator now exists. When touching AI/chat code:

- **Entry point:** the chat route calls `requestOrchestratedButlerPatch` (`lib/ai/orchestrator.ts`). Do not reintroduce a direct single-provider call in the route. `lib/ai/deepseekButler.ts` is legacy/back-compat only — keep it and its test, but do not build new features on it.
- **Add a provider = add a registry entry.** To add or change a Chinese LLM, edit `BUTLER_PROVIDERS` in `lib/ai/modelRegistry.ts` (id, label, capabilities, key env + aliases, base URL/override, model/override). Only write a new `ChatCompletionProvider` implementation if the provider is not OpenAI-compatible.
- **Keys are server-side only.** Never read an LLM key in client code or bundle it. Every new key must be documented in `lib/env/placeholders.ts`. Current provider keys: `DEEPSEEK_API_KEY`, `DASHSCOPE_API_KEY`, `ZHIPU_API_KEY`, `MOONSHOT_API_KEY`, `ERNIE_API_KEY`, `MINIMAX_API_KEY` (+ documented aliases and `*_BASE_URL`/`*_CHAT_MODEL` overrides).
- **Never remove the mock fallback.** The orchestrator must always end at `createMockButlerPatch` when no provider is configured or all fail. Keep this true for every future change.
- **Routing is by capability, not hardcoded model names.** Change routing via `INTENT_CAPABILITY_PRIORITY` / `selectProvidersForIntent`, and high-stakes membership via `HIGH_STAKES_INTENTS`. Keep these pure and unit-tested (`tests/modelRegistry.test.ts`).
- **Intent classifier is local and deterministic.** Extend `lib/ai/intentClassifier.ts` with regex/keyword only — no network calls. Update `tests/intentClassifier.test.ts` when adding intents.
- **Prompt/parsing lives in `lib/ai/butlerPrompt.ts`.** Reuse `buildSystemPrompt`/`buildUserPrompt`/`parseButlerPatch` so all providers behave identically. When you add the `{headline,body,highlights,watchOut,nextStep}` schema (task 13.5), extend the parser here and keep `assistantMessage` populated for back-compat.
- **Response shape:** the route returns `mode`, `modelLabel`, `intent`, `strategy`, `providersTried`, `patch`, `suggestions`, `fallbackReason`. `ButlerWorkspace` reads `modelLabel` for status — keep it populated.

## Mock/placeholder replacement discipline

- `docs/planning/mock-inventory.md` is the running list of every mock/placeholder/local-sim point and its real-replacement plan. When you make a mock real, update its row (status 🔴/🟡/🟢) and the "What changed" section in that file during the same iteration.
- "Replace" always means real primary + mock kept as graceful fallback. Never delete a fallback to "finish" an item.

## v0.1.48 Agent Update - Configured Models and Structured Reply Rules

- The active configured Butler defaults are DeepSeek v4 flash (`deepseek-v4-flash`), Qwen 3.6 Flash (`qwen3.6-flash`), Zhipu GLM5 (`glm-5`), and Moonshot Kimi 2.5 (`kimi-2.5`). Keep these defaults unless the user changes provider/model direction; use env overrides for deployment-specific corrections.
- `CanvasPatch.assistantMessage` is still required. Do not remove it, because saved chat history, legacy providers, and mock fallback depend on a plain-text message.
- `CanvasPatch.assistantResponse` is optional but preferred for live providers. It must follow `{ headline, body, highlights, watchOut, nextStep }`.
- `ChatMessage.response` mirrors the structured assistant response for UI rendering. Historical messages may not have it; `ChatPanel` must keep rendering plain `content` safely.
- When changing prompt/parser behavior, update `tests/orchestrator.test.ts` and keep old provider JSON compatible.

## v0.1.49-v0.1.51 Agent Update - Rich POI, Tool Context, Preference Memory

- Amap rich POI normalization lives in `lib/explore/amapSearch.ts`. Do not duplicate rating/price/photo/open-hours parsing inside components.
- `/api/explore/amap` must keep `AMAP_API_KEY` server-side and use `extensions=all`; browser code only calls the internal route.
- Explore rich metadata is optional. Static provider rows may leave all rich fields undefined, and UI must remain clean.
- Chat tool context currently uses bounded Amap POI prefetch (`lib/ai/toolContext.ts`) rather than a full function-calling loop. Keep it bounded and gracefully omitted when Amap is unavailable.
- `UserPreferenceProfile` is extracted silently in `lib/ai/preferenceProfile.ts`. Do not add a questionnaire UI unless the user explicitly asks.
- Guest preference memory uses localStorage. Do not claim cross-device preference sync until Supabase `profiles` migration is implemented.
- Keep the one-question rule in prompts: Butler may ask at most one clarifying question and only when missing information would make the trip clearly wrong.

## v0.1.52 Agent Update - Product Interaction Blueprint Rules

v0.1.52 is a documentation-only strategic interaction iteration. Deep-dive: `docs/planning/v0.1.52-product-interaction-blueprint.md`. Future product/UX work should follow these rules unless the user explicitly changes direction.

**Product spine**

- Treat VisePanda as a China travel operating system for foreign visitors, not just an itinerary generator.
- Chat is the command center; Trip Canvas is the source of truth; Trips is continuity/readiness/sharing. Explore, Tools, Translate, Account, and Community should support that loop instead of competing with it.
- Before adding a new standalone page or large panel, check whether the same value can appear as a contextual Chat card, Canvas action, Trip detail control, or Translate floating utility.

**Journey-first planning**

- For every new feature, identify the journey stage it serves: Curious, Planning, Preparing, In China, or Share/Get help.
- Also identify which traveler anxiety it reduces: entry, payment, connectivity, language, or itinerary.
- Do not prioritize a feature only because it is technically convenient; prioritize it when it moves the traveler to the next safe, practical step.

**Surface roles**

- Home should start trips through archetypes and route returning/high-intent users toward Chat or active Trips.
- Chat should expose first-run starts, structured `nextStep` actions, inline tool cards, and subtle preference memory.
- Trip Canvas should show the trip title, completeness, day quick actions, and operational day detail. Quick actions must still send Butler intents through the AI pipeline; they must not mutate canvas directly.
- Trips detail should keep itinerary primary and actions compact inside the trip/canvas context.
- Explore should feed Chat/Canvas and support "In your trip", Add to Day, Replace morning, or Use as dinner precision.
- Tools should become widgets and contextual cards, but static fallback content must remain.
- Translate should become an everywhere utility and return users to their prior context.
- Account should become a trust/preference/consent center; do not collect sensitive document fields without explicit opt-in.

**Traveler-facing language**

- Replace developer-facing labels with traveler-facing labels: Draft → Taking shape, Refined → Looking good, Ready to save → Travel-ready.
- Avoid provider/API/model jargon in traveler UI. Use internal docs/logs/admin/debug surfaces for implementation status.
- Every AI reply or guided flow should end with one concrete next action.
- Every personal or sensitive question must explain why it matters to the trip.

**Recommended order**
 
- Next implementation should start with `v0.1.53` Interaction Shell I, then proceed through Canvas Action Layer, Inline Tool Cards, TripBlock POI Embedding, Translate Everywhere, Tools Widgets, Account Center, and Admin/Customer Brief work as recorded in `PLAN.md`.
 
## v0.1.53 Agent Update - Offline Vault, Context Interpretation, Payment Cards, Contextual Tool Promotion, and Bilingual Handoff

- **Offline-First Vault Integrity**: When editing client state or adding new local storage dependencies, ensure all critical data models (`TripState`, `UserPreferenceProfile`, offline phrasebooks, emergency cards) are cached locally and fall back gracefully in offline mode. Keep the offline checking code unified.
- **Cultural and Operational Interpretation**: AI Butler prompts must remain grounded in China travel constraints. Do not let the Butler output names without validating booking constraints (e.g. passport reservation requirements for museums, train ticket release schedules, holiday crowd advisories).
- **Payment Wizard Logic**: When implementing payments setup, ensure the credit card routing matches the specific card rules: e.g., Alipay transaction limits (3% fee on purchases above 200 CNY), linking credit cards, WeChat Pay limits, and locating international cash networks.
- **Contextual Tool Rendering**: Components on the active workspace must watch the current day/city route. Ensure tool priority changes dynamically (e.g., floating metro and Alipay cards for Shanghai, currency convert calculators for shopping).
- **Bilingual Handoff Guidelines**: Ensure every POI and day block exposes a "Show Taxi Driver" trigger, rendering Chinese name and address in large-font bilingual text cards for drivers. Itinerary export should produce clean, compact EN/ZH print sheets.

