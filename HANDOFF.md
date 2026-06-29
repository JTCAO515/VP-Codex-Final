# VisePanda — 交接文档

## 当前状态

- 完成阶段：阶段一 AI Butler Chat MVP 骨架。
- 当前分支：`main`
- 当前版本：`v0.1.5`
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
- Day-by-day itinerary cards ✅
- 每日一句摘要卡 + 侧边详情抽屉 ✅
- 桌面横屏一屏固定工作台 ✅
- 顶部五张任务/提醒卡 ✅
- mock AI provider ✅
- canvas patch reducer ✅
- Trips / Explore / Tools / Account 占位页 ✅
- `/api/chat` mock route ✅
- `/api/trips`、`/api/explore`、`/api/tools` placeholder routes ✅
- Vitest 单元/组件/API 测试 ✅
- Playwright 桌面/移动烟测 ✅

## 未完成/待办

- [ ] 接入真实 AI provider（保留 mock fallback）。
- [ ] 设计 Supabase schema 并接入 trips/chat/canvas persistence。
- [ ] 实现 Account 登录和同步。
- [ ] 将 Explore 从占位升级为城市、景点、美食、住宿探索。
- [ ] 设计并验证第三方 provider abstraction。
- [ ] 实现 Tools 第一批真实工具。
- [ ] 实现场景感知背景切换，例如北京对应长城/故宫水墨，上海对应外滩/江南园林水墨。
- [ ] 移动竖屏端细节适配（当前阶段先保证可用，后续再精修）。

## 已知问题

- npm audit 当前报告若干依赖安全提示；尚未使用 `npm audit fix --force`，避免破坏 Next/React 版本组合。
- 第一阶段 AI 结果是 deterministic mock，不代表真实模型质量。
- Explore、Trips、Tools、Account 当前只是占位。
- 桌面横屏端为当前优先体验；移动竖屏端后续需要针对抽屉和画布密度继续优化。
- OneDrive 目录偶尔会锁住 `.next` 构建缓存；如出现 `readlink` / `EBUSY`，停止 dev server 并安全删除 `.next` 后重跑。

## 下一步优先级

1. 继续打磨桌面横屏 Chat / Trip Canvas 的信息密度和抽屉体验。
2. 为移动竖屏端单独设计抽屉/画布适配。
3. 进入阶段二：真实 AI provider 接入，同时保留 mock fallback。
4. 设计 Supabase schema，再做登录/同步和 trip persistence。
5. 后续扩展 Explore provider abstraction 和真实工具页。

## 关键文件索引

- `app/chat/page.tsx` — Chat / AI Butler 页面入口。
- `components/chat/ButlerWorkspace.tsx` — 主工作台状态管理。
- `components/chat/ChatPanel.tsx` — 聊天面板。
- `components/canvas/TripCanvas.tsx` — live trip canvas 组合组件。
- `components/canvas/DayCard.tsx` — 单日行程卡片。
- `components/canvas/DayDetailDrawer.tsx` — 单日完整行程详情抽屉。
- `components/canvas/CanvasTaskStrip.tsx` — 画布顶部五张任务/提醒卡。
- `lib/mock-ai/mockButler.ts` — mock AI provider。
- `lib/canvas/applyCanvasPatch.ts` — canvas patch reducer。
- `lib/types/trip.ts` — 核心产品类型。
- `app/api/chat/route.ts` — mock chat API。
- `app/globals.css` — 当前视觉系统和响应式布局。
- `public/ink-landscape.png` — MVP 水墨背景资产。
- `tests/` — 单元、组件、API、e2e 测试。
- `docs/superpowers/specs/2026-06-29-vp-ai-butler-chat-design.md` — 第一阶段设计规格。
- `docs/superpowers/plans/2026-06-29-vp-ai-butler-chat.md` — 第一阶段实施计划。

## 本地验证记录

最近一次完成阶段一时通过：

```bash
npm run test
npm run build
npm run test:e2e
```

本轮 `v0.1.5` 需重新通过：

```bash
npm.cmd run test
npm.cmd run build
npm.cmd run test:e2e
```

PowerShell 环境可使用：

```bash
npm.cmd run test
npm.cmd run build
npm.cmd run test:e2e
```
