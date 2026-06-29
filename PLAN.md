# VisePanda — 执行计划

## 阶段一：AI Butler Chat MVP 骨架

- [x] 任务 1.1：建立 Next.js + React + TypeScript 项目骨架，适配 Vercel 部署。
- [x] 任务 1.2：定义 Trip Canvas、Butler Alert、Chat Message、Canvas Patch 等核心类型。
- [x] 任务 1.3：实现 mock AI butler pipeline，让用户消息可以生成结构化 canvas patch。
- [x] 任务 1.4：实现 Live Trip Canvas，包括 trip summary、day summary cards、顶部任务提醒卡。
- [x] 任务 1.5：实现 Chat / AI Butler 主工作台，桌面为左画布右聊天。
- [x] 任务 1.6：实现 Trips、Explore、Tools、Account 初始页面。
- [x] 任务 1.7：加入 warm New Chinese 水墨背景和纸卡视觉系统。
- [x] 任务 1.8：加入单元测试、组件测试、API 测试、Playwright 烟测。
- [x] 任务 1.9：将 Chat 工作台调整为桌面横屏一屏固定布局，页面本身不纵向滚动。
- [x] 任务 1.10：Trip Canvas 主界面只显示每日一句摘要，完整每日行程通过右侧抽屉查看。
- [x] 任务 1.11：移除独立 Practical Reminder 区块，将管家提醒合并到顶部五张任务卡。
- [x] 任务 1.12：移除 Chat 默认演示对话，建议问题改为两列，并在每次 AI 回复后生成 2 个上下文建议问题。
- [x] 任务 1.13：将 Live Trip Canvas 改为 Day 时间线，每天显示 Morning / Afternoon / Evening 三段，并移除顶部五张任务卡。
- [x] 任务 1.14：将每日详情抽屉升级为可编辑抽屉，支持本地修改日程块、酒店、交通和备注。

## 阶段二：AI Provider 与 Supabase 接入

- [x] 任务 2.1：接入 DeepSeek V4 Flash 真实 AI provider，保留 mock fallback。
- [x] 任务 2.2：设计 Supabase schema：users、trips、messages、canvas_versions。
- [x] 任务 2.3：实现 guest draft 到 logged-in synced trip 的迁移路径。
- [x] 任务 2.4：实现基础 auth（Supabase magic link），将 Account 占位页升级为真实登录/同步入口。
- [x] 任务 2.5：将 Account 从独立页面改为头部图标 + 悬浮窗口，登录方式从 magic link 改为邮箱密码 + Google OAuth，登录后支持改名/改密码/登出。

## 阶段三：Trips 行程库

- [x] 任务 3.1：将 Trips 从占位页升级为静态 saved trips dashboard 骨架。
- [x] 任务 3.2：加入 mock trip cards、状态筛选、概览指标和 Continue in Chat 入口。
- [x] 任务 3.3（首个闭环）：接入真实 Supabase trip persistence，支持从 Chat 保存当前 canvas、在 Trips 读取已登录用户的真实行程、并从 Trips 恢复到 Chat。仍需：未登录/未配置 Supabase 时使用 mock 数据兜底；trip detail 页面、归档/分享流程未实现。
- [x] 任务 3.4：实现 trip detail 页面和从 Trips 恢复 Chat Canvas 上下文。
- [x] 任务 3.5：实现分享状态、归档状态和基础协作/分享链接。

## 阶段四：Explore 与第三方 Provider

- [x] 任务 4.1：将 Explore 从占位页升级为城市、景点、美食、住宿入口。
- [x] 任务 4.2：设计 provider abstraction，避免 UI 直接依赖 Amap、Trip.com、Meituan。
- [ ] 任务 4.3：先接 verified/static provider，再接真实第三方 API。
- [x] 任务 4.4：把 Explore 结果加入 Chat 工作台的 Add to Trip / Add to Canvas 流程。

## 阶段五：Tools 与落地旅行能力

- [ ] 任务 5.1：实现签证、入境、支付设置、翻译、汇率、地铁、eSIM/VPN、应急工具页面。
- [ ] 任务 5.2：让顶部任务/提醒卡可以深链到对应工具。
- [ ] 任务 5.3：补充移动端工具入口和离线可读内容。

## 阶段六：场景化视觉与体验增强

- [ ] 任务 6.1：实现 destination-aware background switching。
- [ ] 任务 6.2：规划北京时切换为长城/故宫风格水墨背景。
- [ ] 任务 6.3：规划上海时切换为外滩/江南园林风格水墨背景。
- [ ] 任务 6.4：根据 active trip canvas destination state 平滑切换背景，避免做成手动主题选择器。

## 关键约束

- 技术选型：Next.js App Router、React、TypeScript、Vercel、Supabase 预留。
- AI 约束：DeepSeek V4 Flash 只在服务端 API route 调用；真实 key 不进入浏览器、不写入仓库。
- Fallback 约束：缺少 `DEEPSEEK_API_KEY`、API 失败或模型输出不合法时必须回落到 mock provider。
- 当前重点：Chat / AI Butler 已完成 MVP 骨架；Trips 行程库已接入真实 Supabase persistence 首个闭环、归档/分享流程；guest draft 已能自动迁移到登录账号；Explore 已升级为静态 provider 驱动的城市/景点/美食/住宿骨架，并接入了 Add to Trip 流程；Account 已从独立页面改为头部图标 + 悬浮窗口，支持邮箱密码登录/注册、Google 登录，登录后可改名/改密码/登出。
- Explore provider 约束：`lib/explore/types.ts` 定义 `ExploreProvider` 接口；`lib/explore/staticProvider.ts` 是当前唯一实现（静态数据，覆盖 Beijing/Shanghai/Chengdu/Xi'an）；`lib/explore/index.ts` 的 `getExploreProvider()` 是组件唯一允许调用的入口，后续切换真实 Amap/Trip.com/Meituan provider 时只替换这里，不应改动 `components/explore/ExploreBoard.tsx` 的渲染逻辑。
- Explore Add to Trip 约束（`v0.1.17`）：`ExploreBoard` 的每个景点/美食/住宿条目都有一个 "Add to Trip" 按钮；点击后跳转到 `/chat?add=<编码后的草稿消息>`，由 `ButlerWorkspace` 在挂载时读取 `add` 参数并通过既有的 `handleSend` → `/api/chat` → `CanvasPatch` → `applyCanvasPatch` 流程发送，不允许在 Explore 组件里直接拼装 `TripDay`/`TripState` 或绕开 AI pipeline 写入画布。
- Trips 当前限制：`v0.1.14` 已支持 trip detail 页面、归档/恢复状态切换、生成与撤销分享链接、以及只读公开分享页 `/share/[token]`；分享页不展示 chat 历史。
- Account 约束：`v0.1.16` 起不再有独立 `/account` 页面；`components/account/AccountMenu.tsx` 是登录/账号管理唯一入口，渲染在 `AppShell` 头部；登录方式为邮箱密码（`signInWithPassword`/`signUpWithPassword`）和 Google OAuth（`signInWithGoogle`），不再提供 magic link；登录后的改名/改密码通过 `updateDisplayName`/`updatePassword`（均封装在 `lib/supabase/auth.ts`）完成。
- Supabase schema 约束：`supabase/migrations/0001_init_trip_schema.sql`、`supabase/migrations/0002_trip_archive_and_share.sql` 和 `lib/supabase/schema.ts` 是当前 schema 契约；`lib/supabase/tripsRepository.ts` 是唯一允许的 persistence 入口，不要绕开它直接拼 Supabase 查询。需要在 Supabase SQL Editor 中按顺序运行这两个迁移文件。
- Supabase 部署约束：本仓库目前没有真实 Supabase 项目；`NEXT_PUBLIC_SUPABASE_URL`、`NEXT_PUBLIC_SUPABASE_ANON_KEY`、`SUPABASE_SERVICE_ROLE_KEY` 未配置前，所有 Supabase 相关代码必须保持优雅降级（不崩溃、自动回落到 mock/guest 体验）。
- 视觉约束：warm New Chinese、水墨背景、实底纸卡；不要半透明玻璃聊天框。
- 桌面布局约束：当前阶段优先电脑横屏端，一屏工作台和内部滚动优先；移动竖屏端后续精修。
- Canvas 约束：不要恢复顶部 Visa / Payment / Booking / Less tiring / Food-focused 五个任务框；每日主卡必须直接呈现 Morning / Afternoon / Evening。
- 文档约束：每一次迭代都必须同步更新 `PLAN.md`、`PRD.md`、`DESIGN.md`、`AGENTS.md`、`HANDOFF.md`。
- 测试约束：最终完成前必须跑 `npm run test`、`npm run build`、`npm run test:e2e`。

## 里程碑

- M1：AI Butler Chat MVP 骨架完成（2026-06-29，已完成）。
- M2：真实 AI provider 完成（2026-06-29，已完成）；Supabase schema 待排期。
- M3：Trips Dashboard 骨架完成（2026-06-29，v0.1.8）。
- M3.5：Live Trip Canvas 三段式时间线和可编辑每日抽屉完成（2026-06-29，v0.1.9）。
- M3.6：Supabase schema 设计完成（2026-06-29，v0.1.10）。
- M4：Supabase magic link 登录 + Trips/Chat 真实 persistence 首个闭环完成（2026-06-29，v0.1.11）；trip detail、归档、分享待排期。
- M4.5：guest draft 到 logged-in synced trip 自动迁移路径完成（2026-06-29，v0.1.12）。
- M4.6：Trip detail 页面完成，支持从 Trips 查看真实/示例行程详情并恢复到 Chat（2026-06-29，v0.1.13）。
- M4.7：分享状态、归档状态和只读分享链接完成（2026-06-29，v0.1.14）。
- M5：Explore provider abstraction 和静态城市/景点/美食/住宿骨架完成（2026-06-29，v0.1.15）。
- M5.5：Account 从独立页面改为头部图标 + 悬浮窗口，邮箱密码登录 + Google 登录，登录后可改名/改密码/登出完成（2026-06-29，v0.1.16）。
- M5.6：Explore Add to Trip / Add to Canvas 流程完成（2026-06-29，v0.1.17）。
- M6：Tools 第一批真实工具完成（待排期）。
- M7：目的地感知背景切换完成（待排期）。

## 风险

- 已知风险 1：真实 AI 输出需要严格结构化，否则 canvas patch 可能不稳定。
- 已知风险 2：DeepSeek key、限流、网络和模型输出质量会影响实时体验，因此必须保留 mock fallback。
- 已知风险 3：Supabase schema 一旦过早固定，后续 Trips/Canvas 迭代会受限。
- 已知风险 4：未配置 Supabase 或未登录时 Trips 仍展示 mock data；文案需要持续避免让用户误以为这些是真实保存的行程。
- 已知风险 5：第三方 API 能力边界必须先验证，不能伪造未确认的 Trip.com、Meituan、Amap 能力。
- 已知风险 6：水墨背景如果过重，会影响文字可读性和移动端性能。
- 待验证假设 1：用户会更喜欢“右侧持续聊天 + 左侧实时画布”而不是传统单列聊天。
- 待验证假设 2：Trips 行程库先以 dashboard 管理草稿/已确认/已分享状态，能自然承接 Chat 生成的行程。
