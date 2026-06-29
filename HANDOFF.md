# VisePanda — 交接文档

## 当前状态

- 完成阶段：阶段一 AI Butler Chat MVP 骨架；阶段二真实 AI provider 已接入；阶段三 Trips Dashboard 骨架已完成。
- 当前分支：`main`
- 当前版本：`v0.1.8`
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
- 每日一句摘要卡 + 侧边详情抽屉 ✅
- 桌面横屏一屏固定工作台 ✅
- 顶部五张任务/提醒卡 ✅
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

## 未完成/待办

- [ ] 设计 Supabase schema 并接入 trips/chat/canvas persistence。
- [ ] 实现 Account 登录和同步。
- [ ] 让 Trips 支持真实保存、读取、更新、恢复到 Chat Canvas。
- [ ] 增加 trip detail 页面和分享链接。
- [ ] 将 Explore 从占位升级为城市、景点、美食、住宿探索。
- [ ] 设计并验证第三方 provider abstraction。
- [ ] 实现 Tools 第一批真实工具。
- [ ] 实现场景感知背景切换，例如北京对应长城/故宫水墨，上海对应外滩/江南园林水墨。
- [ ] 移动端竖屏端细节适配。

## 已知问题

- DeepSeek 输出虽要求 JSON，但真实模型仍可能返回无效 patch 或无效 suggestions；当前会自动回落/规范化。
- 用户提供的 DeepSeek key 已出现在聊天上下文中，验证完成后建议在 DeepSeek 后台轮换。
- npm audit 当前可能报告若干依赖安全提示；尚未使用 `npm audit fix --force`，避免破坏 Next/React 版本组合。
- Trips 当前为 mock data，不具备真实保存或跨设备同步能力。
- Explore、Tools、Account 当前仍是占位。
- 桌面横屏端为当前优先体验；移动竖屏端后续需要针对抽屉、画布密度和 Trips 卡片继续优化。
- OneDrive 目录偶尔会锁住 `.next` 构建缓存；如出现 `readlink` / `EBUSY`，停止 dev server 并安全删除 `.next` 后重跑。

## 下一步优先级

1. 为 Trips 设计 Supabase schema：trip metadata、canvas snapshot、messages、share status。
2. 实现从 Chat 保存当前 Trip Canvas 到 Trips 的最小闭环。
3. 实现从 Trips 点击某个行程恢复到 Chat 的上下文。
4. 再扩展 trip detail 页面和分享/归档状态。
5. 后续再推进 Explore provider abstraction 和真实工具页。

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
- `app/globals.css` — 当前视觉系统和响应式布局。
- `public/ink-landscape.png` — MVP 水墨背景资产。
- `tests/trips-dashboard.test.tsx` — Trips Dashboard 组件测试。
- `tests/` — 单元、组件、API、e2e 测试。

## 本地验证记录

本轮 `v0.1.8` 需要通过：

```bash
npm.cmd run test
npm.cmd run build
npm.cmd run test:e2e
```
