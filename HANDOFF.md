# VisePanda — 交接文档

## 当前状态

- 完成阶段：阶段一 AI Butler Chat MVP 骨架；阶段二真实 AI provider + Supabase 登录 + guest draft 自动迁移已接入；阶段三 Trips 已接入真实 Supabase persistence 首个闭环，加入了 trip detail 页面和归档/分享链接流程（任务 3.5）；阶段四 Explore 已升级为静态 provider 驱动的骨架，完成 provider abstraction 设计（任务 4.1、4.2），并接入了 Add to Trip 流程（任务 4.4）；阶段五 Tools 已从占位页升级为静态 provider 驱动的 7 个分类骨架（任务 5.1），并接入了管家提醒深链流程（任务 5.2）；Account 已从独立页面改为头部图标 + 悬浮窗口，登录方式从 magic link 改为邮箱密码 + Google OAuth，登录后支持改名/改密码/登出（任务 2.5）。
- 当前分支：`claude/visepanda-phase-3-hym6z9`（按用户要求后续直接推送到 `main`）
- 当前版本：`v0.1.19`
- 重要：用户已创建真实 Supabase 项目、跑过 `0001_init_trip_schema.sql` migration，并在 Vercel 配置了三个 Supabase 环境变量。**`supabase/migrations/0002_trip_archive_and_share.sql` 需要用户在 Supabase SQL Editor 里手动追加执行，否则归档/分享相关的状态更新会因 RLS 缺失而失败。** **本轮（v0.1.16）登录方式从 magic link 改为邮箱密码 + Google 登录：Google 登录需要用户在 Supabase 项目的 Authentication → Providers 里启用 Google provider 并配置 OAuth Client ID/Secret，否则点击 Continue with Google 会报错；邮箱密码登录不需要额外配置即可使用。**
- 最新实现 commit：本轮提交后以 `git log -1 --oneline` 为准
- 当前远端：`https://github.com/JTCAO515/VP-Codex-Final.git`
- 部署地址：`https://go2china.space`

## 已完成的功能

- Next.js + React + TypeScript 项目骨架 ✅
- Vercel-ready route/API 结构 ✅
- Warm New Chinese 水墨背景视觉系统 ✅
- Chat / AI Butler 主工作台 ✅
- 左侧 Live Trip Canvas ✅
- 右侧持续聊天面板 ✅
- 无默认演示对话的真实 Chat 初始态 ✅
- 两列建议问题 + 每轮 2 个上下文 follow-up ✅
- Day-by-day summary cards ✅
- Day 时间线 + Morning / Afternoon / Evening 三段卡 ✅
- 可编辑每日详情抽屉 ✅
- 桌面横屏一屏固定工作台 ✅
- Canvas 顶部五张任务/提醒卡已移除 ✅
- DeepSeek V4 Flash provider ✅
- mock AI fallback ✅
- canvas patch reducer ✅
- Trips Dashboard 骨架 ✅
- Trips mock trip cards、状态筛选、概览指标、Continue in Chat ✅
- Explore / Tools / Account 占位页 ✅
- `/api/chat` DeepSeek + fallback route ✅
- `/api/trips`、`/api/explore`、`/api/tools` placeholder routes ✅
- Vitest 单元/组件/API 测试 ✅
- Playwright 桌面/移动烟测 ✅
- Supabase schema 设计：`users`、`trips`、`canvas_versions`、`messages` 表 + RLS policies ✅
- Supabase 登录（最初为 magic link，`v0.1.16` 改为邮箱密码 + Google OAuth，guest 模式始终可用）✅
- Chat 工作台 Save to Trips：保存当前 canvas 到 `trips` + `canvas_versions`，并同步聊天记录到 `messages` ✅
- Trips Dashboard：已登录且配置 Supabase 时读取真实行程列表；未登录/未配置时回落到 mock 行程 ✅
- 从 Trips 的 Continue in Chat 恢复真实保存的 canvas 到 Chat 工作台（`/chat?trip=<id>`） ✅
- Guest draft 自动持久化到 `localStorage` 并在刷新后还原；用户登录后自动同步草稿到 Supabase（任务 2.3） ✅
- Trip detail 页面（`/trips/[id]`）：已登录显示真实 Live Trip Canvas，未登录/示例行程显示摘要卡，未知 id 显示 not found（任务 3.4） ✅
- 归档与分享链接（任务 3.5）：Trip detail 页面支持 Mark as Ready / Archive / Restore from archive 状态切换；支持 Get share link 生成 token 并展示完整 URL，支持 Revoke share link 撤销；新增公开只读分享页 `/share/[token]`（`components/share/ShareView.tsx`），未登录访客可查看分享行程的 Canvas，看不到聊天记录 ✅
- Explore provider abstraction 与静态骨架（任务 4.1、4.2）：新增 `lib/explore/types.ts` 定义 `ExploreProvider` 接口和城市/景点/美食/住宿类型；`lib/explore/staticProvider.ts` 提供覆盖北京/上海/成都/西安的静态数据；`lib/explore/index.ts` 的 `getExploreProvider()` 是组件唯一允许调用的入口；`components/explore/ExploreBoard.tsx` 替换占位页，支持城市筛选按钮切换和景点/美食/住宿三列展示 ✅
- Account 图标 + 悬浮窗口重做（任务 2.5）：删除独立 `/account` 页面和 `AccountPanel.tsx`；新增 `components/account/AccountMenu.tsx`，渲染在 `AppShell` 头部、`NavTabs` 旁边；登录方式从 magic link 改为邮箱密码登录/注册（`signInWithPassword`/`signUpWithPassword`）和 Google 登录（`signInWithGoogle`）；已登录状态下悬浮窗口提供 Change name（`updateDisplayName`）、Change password（`updatePassword`）、Log out 三个操作 ✅
- Explore Add to Trip 流程（任务 4.4）：`ExploreBoard` 的每个景点/美食/住宿条目新增 Add to Trip 按钮，点击后跳转到 `/chat?add=<编码后的草稿消息>`；`ButlerWorkspace` 挂载时读取 `add` 参数，清空 URL 后调用既有的 `handleSend`，新内容和用户手动发消息一样经过 `/api/chat` → `CanvasPatch` → `applyCanvasPatch`，没有新增任何绕开 AI pipeline 的画布写入入口 ✅
- Tools 静态骨架（任务 5.1）：新增 `lib/tools/types.ts`（`ToolsProvider` 接口、`ToolCategory` 类型）、`lib/tools/staticProvider.ts`（签证入境/支付设置/翻译/汇率/地铁/eSIM-VPN/应急 7 个分类的静态参考清单）、`lib/tools/index.ts`（`getToolsProvider()` 工厂）；`components/tools/ToolsBoard.tsx` 替换占位页，左侧分类列表 + 右侧摘要和建议清单；Currency/Translate 文案明确说明实时汇率/翻译尚未接入 ✅
- 管家提醒深链 Tools（任务 5.2）：新增 `components/canvas/ButlerReminders.tsx`，渲染在 `TripCanvas` 行程时间线下方（不在画布顶部，不是固定 5 张任务卡）；按 `ButlerAlert.type` 映射到对应 Tools 分类 id，有映射的提醒渲染为跳转 `/tools?category=<id>` 的链接，没有映射的类型（`booking`、`weather`）只显示文字；`components/tools/ToolsBoard.tsx` 新增读取 `?category=` URL 参数并预选中对应分类 ✅

## 未完成/待办

- [ ] 真实 Supabase 项目已创建（用户已完成部署），需要提醒用户在 Supabase SQL Editor 里手动追加执行 `0002_trip_archive_and_share.sql`，否则归档/分享功能会因 RLS policy 缺失而报错。
- [ ] 需要提醒用户在 Supabase 项目的 Authentication → Providers 里启用并配置 Google OAuth provider，否则悬浮窗口里的 Continue with Google 会报错；邮箱密码登录不需要额外配置。
- [ ] 接入 verified/static provider 之外的真实第三方 Explore API（Amap/Trip.com/Meituan）（任务 4.3）。
- [ ] Tools 接入真实第三方数据源（实时汇率换算、机器翻译、签证规则查询等）。
- [ ] 补充移动端工具入口和离线可读内容（任务 5.3）。
- [ ] 实现场景感知背景切换，例如北京对应长城/故宫水墨，上海对应外滩/江南园林水墨。
- [ ] 移动端竖屏端细节适配。

## 已知问题

- DeepSeek 输出虽要求 JSON，但真实模型仍可能返回无效 patch 或无效 suggestions；当前会自动回落/规范化。
- 用户提供的 DeepSeek key 已出现在聊天上下文中，验证完成后建议在 DeepSeek 后台轮换。
- npm audit 当前可能报告若干依赖安全提示；尚未使用 `npm audit fix --force`，避免破坏 Next/React 版本组合。
- Trips 在未登录或未配置 Supabase 时仍为 mock data；登录且配置后才会显示真实保存的行程。
- Day 抽屉编辑仍是本地状态；只有点击 Save to Trips 才会把当时的 canvas 整体快照写入 Supabase，抽屉内的单次编辑不会自动保存。
- Tools 当前是静态参考清单骨架，没有接入真实汇率/翻译/签证规则数据源。
- `ButlerAlert` 里的 `booking`、`weather` 两种类型在 Tools 里没有对应分类，目前在管家提醒列表里只显示文字，不渲染深链；如果后续新增对应分类需要同步更新 `ButlerReminders.tsx` 里的映射。
- 桌面横屏端为当前优先体验；移动竖屏端后续需要针对抽屉、画布密度和 Trips 卡片继续优化。
- OneDrive 目录偶尔会锁住 `.next` 构建缓存；如出现 `readlink` / `EBUSY`，停止 dev server 并安全删除 `.next` 后重跑。
- `playwright.config.ts` 的 `webServer.command` 写的是 `npm.cmd run dev`（Windows 专用），在非 Windows 环境（例如本次远程沙箱）跑 `npm run test:e2e` 会因为找不到 `npm.cmd` 而无法启动开发服务器；本轮在远程沙箱里改为手动启动 `npm run dev` 并用 Playwright 直接连接验证 Account 悬浮窗口，未touch该配置文件，因为用户本地是 Windows 环境，`npm.cmd` 在那边是正确的。

## 下一步优先级

1. 推进 Tools 任务 5.3（移动端工具入口、离线可读内容）。
2. 评估 Explore 真实第三方 provider 接入（任务 4.3，等待可用凭据/合作方）。
3. 评估阶段六场景感知背景切换（任务 6.1–6.4）。

## 关键文件索引

- `app/chat/page.tsx` — Chat / AI Butler 页面入口。
- `app/trips/page.tsx` — Trips Dashboard 页面入口。
- `app/trips/[id]/page.tsx`、`components/trips/TripDetail.tsx` — Trip detail 页面，含归档状态切换和分享链接管理。
- `app/share/[token]/page.tsx`、`components/share/ShareView.tsx` — 公开只读分享页。
- `app/api/chat/route.ts` — DeepSeek + fallback chat API。
- `components/chat/ButlerWorkspace.tsx` — 主工作台状态管理和 API 调用，挂载时读取 `?trip=`（恢复保存的画布）和 `?add=`（自动发送 Explore 的 Add to Trip 草稿消息）两个 URL 参数。
- `components/chat/ChatPanel.tsx` — 聊天面板。
- `components/canvas/TripCanvas.tsx` — live trip canvas 组合组件。
- `components/canvas/DayCard.tsx` — 单日行程摘要卡片。
- `components/canvas/DayDetailDrawer.tsx` — 单日完整行程详情抽屉。
- `components/canvas/CanvasTaskStrip.tsx` — 画布顶部五张任务/提醒卡（已废弃，保留文件但不被任何组件引用）。
- `components/canvas/ButlerReminders.tsx` — 行程时间线下方的轻量管家提醒列表，按 `ButlerAlert.type` 深链到对应 Tools 分类（`/tools?category=<id>`）。
- `components/trips/TripsDashboard.tsx` — Trips 行程库 dashboard。
- `lib/trips/mockTrips.ts` — Trips 当前静态 mock data。
- `lib/ai/deepseekButler.ts` — DeepSeek V4 Flash provider 与 mock fallback。
- `lib/mock-ai/mockButler.ts` — mock AI fallback provider。
- `lib/canvas/applyCanvasPatch.ts` — canvas patch reducer。
- `lib/types/trip.ts` — 核心产品类型。
- `lib/supabase/schema.ts` — Supabase 表结构的 TypeScript 契约。
- `lib/supabase/client.ts` — 浏览器 Supabase 客户端 + `isSupabaseConfigured`。
- `lib/supabase/auth.ts` — 邮箱密码登录/注册、Google OAuth、改名、改密码、登出/session 读取。
- `lib/supabase/useSupabaseSession.ts` — React hook，给组件提供 `{ configured, loading, session }`。
- `lib/supabase/tripsRepository.ts` — trips/canvas_versions/messages 的唯一读写入口。
- `supabase/migrations/0001_init_trip_schema.sql` — Supabase schema SQL 迁移（需要在真实项目里手动跑一次）。
- `supabase/migrations/0002_trip_archive_and_share.sql` — 新增 `archived` 状态和分享链接公开只读 RLS policy（需要在 0001 之后手动追加执行）。
- `lib/explore/types.ts` — Explore 域类型和 `ExploreProvider` 接口。
- `lib/explore/staticProvider.ts` — 静态 Explore provider 实现（北京/上海/成都/西安）。
- `lib/explore/index.ts` — `getExploreProvider()` 工厂，组件唯一允许调用的入口。
- `app/explore/page.tsx`、`components/explore/ExploreBoard.tsx` — Explore 页面入口和城市/景点/美食/住宿看板组件，每个条目带 Add to Trip 按钮（跳转 `/chat?add=<草稿消息>`）。
- `components/account/AccountMenu.tsx` — Account 头部图标 + 悬浮窗口，登录/注册/Google 登录/改名/改密码/登出的唯一入口。
- `lib/tools/types.ts` — Tools 域类型和 `ToolsProvider` 接口。
- `lib/tools/staticProvider.ts` — 静态 Tools provider 实现（签证入境/支付设置/翻译/汇率/地铁/eSIM-VPN/应急 7 个分类）。
- `lib/tools/index.ts` — `getToolsProvider()` 工厂，组件唯一允许调用的入口。
- `app/tools/page.tsx`、`components/tools/ToolsBoard.tsx` — Tools 页面入口和分类列表/详情看板组件；挂载时读取 `?category=` URL 参数预选中对应分类。
- `app/globals.css` — 当前视觉系统和响应式布局。
- `public/ink-landscape.png` — MVP 水墨背景资产。
- `tests/trips-dashboard.test.tsx` — Trips Dashboard 组件测试。
- `tests/` — 单元、组件、API、e2e 测试。

## 本地验证记录

本轮 `v0.1.19` 需要通过：

```bash
npm.cmd run test
npm.cmd run build
npm.cmd run test:e2e
```
