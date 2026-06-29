# VisePanda — 交接文档

## 当前状态

- 完成阶段：阶段一 AI Butler Chat MVP 骨架；阶段二真实 AI provider + Supabase magic link 登录已接入；阶段三 Trips 已接入真实 Supabase persistence 首个闭环。
- 当前分支：`claude/visepanda-phase-3-hym6z9`（按用户要求后续直接推送到 `main`）
- 当前版本：`v0.1.11`
- 重要：本仓库尚未连接真实 Supabase 项目，Vercel 环境变量目前只有 DeepSeek key；Supabase 相关功能在用户完成项目创建和环境变量配置前不会生效（但也不会崩溃）。
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
- Supabase magic link 登录（Account 页面，guest 模式始终可用）✅
- Chat 工作台 Save to Trips：保存当前 canvas 到 `trips` + `canvas_versions`，并同步聊天记录到 `messages` ✅
- Trips Dashboard：已登录且配置 Supabase 时读取真实行程列表；未登录/未配置时回落到 mock 行程 ✅
- 从 Trips 的 Continue in Chat 恢复真实保存的 canvas 到 Chat 工作台（`/chat?trip=<id>`） ✅

## 未完成/待办

- [ ] 真实 Supabase 项目尚未创建；需要用户在 supabase.com 创建项目、跑 `supabase/migrations/0001_init_trip_schema.sql`、把三个环境变量加到 Vercel。
- [ ] 实现 guest draft 到 logged-in synced trip 的迁移路径（任务 2.3）。
- [ ] 增加 trip detail 页面和分享/归档链接。
- [ ] 将 Explore 从占位升级为城市、景点、美食、住宿探索。
- [ ] 设计并验证第三方 provider abstraction。
- [ ] 实现 Tools 第一批真实工具。
- [ ] 实现场景感知背景切换，例如北京对应长城/故宫水墨，上海对应外滩/江南园林水墨。
- [ ] 移动端竖屏端细节适配。

## 已知问题

- DeepSeek 输出虽要求 JSON，但真实模型仍可能返回无效 patch 或无效 suggestions；当前会自动回落/规范化。
- 用户提供的 DeepSeek key 已出现在聊天上下文中，验证完成后建议在 DeepSeek 后台轮换。
- npm audit 当前可能报告若干依赖安全提示；尚未使用 `npm audit fix --force`，避免破坏 Next/React 版本组合。
- Trips 在未登录或未配置 Supabase 时仍为 mock data；登录且配置后才会显示真实保存的行程。
- Day 抽屉编辑仍是本地状态；只有点击 Save to Trips 才会把当时的 canvas 整体快照写入 Supabase，抽屉内的单次编辑不会自动保存。
- 真实 Supabase 项目还没创建,Vercel 环境变量目前只有 DeepSeek key；用户需要按 README/对话中给出的步骤创建项目、跑 migration、配置 Vercel 环境变量后功能才会生效。
- Explore、Tools、Account 当前仍是占位。
- 桌面横屏端为当前优先体验；移动竖屏端后续需要针对抽屉、画布密度和 Trips 卡片继续优化。
- OneDrive 目录偶尔会锁住 `.next` 构建缓存；如出现 `readlink` / `EBUSY`，停止 dev server 并安全删除 `.next` 后重跑。

## 下一步优先级

1. 用户在 supabase.com 创建真实项目、跑 migration、把环境变量加到 Vercel（步骤见对话记录/README）。
2. 实现任务 2.3：guest draft 到 logged-in synced trip 的迁移路径。
3. 扩展 trip detail 页面和分享/归档状态。
4. 后续再推进 Explore provider abstraction 和真实工具页。

## 关键文件索引

- `app/chat/page.tsx` — Chat / AI Butler 页面入口。
- `app/trips/page.tsx` — Trips Dashboard 页面入口。
- `app/api/chat/route.ts` — DeepSeek + fallback chat API。
- `components/chat/ButlerWorkspace.tsx` — 主工作台状态管理和 API 调用。
- `components/chat/ChatPanel.tsx` — 聊天面板。
- `components/canvas/TripCanvas.tsx` — live trip canvas 组合组件。
- `components/canvas/DayCard.tsx` — 单日行程摘要卡片。
- `components/canvas/DayDetailDrawer.tsx` — 单日完整行程详情抽屉。
- `components/canvas/CanvasTaskStrip.tsx` — 画布顶部五张任务/提醒卡。
- `components/trips/TripsDashboard.tsx` — Trips 行程库 dashboard。
- `lib/trips/mockTrips.ts` — Trips 当前静态 mock data。
- `lib/ai/deepseekButler.ts` — DeepSeek V4 Flash provider 与 mock fallback。
- `lib/mock-ai/mockButler.ts` — mock AI fallback provider。
- `lib/canvas/applyCanvasPatch.ts` — canvas patch reducer。
- `lib/types/trip.ts` — 核心产品类型。
- `lib/supabase/schema.ts` — Supabase 表结构的 TypeScript 契约。
- `lib/supabase/client.ts` — 浏览器 Supabase 客户端 + `isSupabaseConfigured`。
- `lib/supabase/auth.ts` — magic link 登录/登出/session 读取。
- `lib/supabase/useSupabaseSession.ts` — React hook，给组件提供 `{ configured, loading, session }`。
- `lib/supabase/tripsRepository.ts` — trips/canvas_versions/messages 的唯一读写入口。
- `components/account/AccountPanel.tsx` — Account 登录 UI。
- `supabase/migrations/0001_init_trip_schema.sql` — Supabase schema SQL 迁移（需要在真实项目里手动跑一次）。
- `app/globals.css` — 当前视觉系统和响应式布局。
- `public/ink-landscape.png` — MVP 水墨背景资产。
- `tests/trips-dashboard.test.tsx` — Trips Dashboard 组件测试。
- `tests/` — 单元、组件、API、e2e 测试。

## 本地验证记录

本轮 `v0.1.11` 需要通过：

```bash
npm.cmd run test
npm.cmd run build
npm.cmd run test:e2e
```
