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
