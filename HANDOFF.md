# VisePanda — 交接文档

## 当前状态

- 完成阶段：阶段一 AI Butler Chat MVP 骨架。
- 当前分支：`main`
- 最新实现 commit：`fc2952e`
- 当前远端：`https://github.com/JTCAO515/VP-Codex-Final.git`
- 部署地址：尚未配置 Vercel；本地可运行。

## 已完成的功能

- Next.js + React + TypeScript 项目骨架 ✅
- Vercel-ready route/API 结构 ✅
- Warm New Chinese 水墨背景视觉系统 ✅
- Chat / AI Butler 主工作台 ✅
- 左侧 Live Trip Canvas ✅
- 右侧持续聊天面板 ✅
- Day-by-day itinerary cards ✅
- Butler Rails 管家提醒 ✅
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
- [ ] 配置 Vercel 项目并部署。

## 已知问题

- npm audit 当前报告若干依赖安全提示；尚未使用 `npm audit fix --force`，避免破坏 Next/React 版本组合。
- 第一阶段 AI 结果是 deterministic mock，不代表真实模型质量。
- Explore、Trips、Tools、Account 当前只是占位。
- 生产部署尚未执行。

## 下一步优先级

1. 先把当前 main push 到 GitHub，建立远端基线。
2. 配置 Vercel 项目并完成首次部署。
3. 进入阶段二：真实 AI provider 接入，同时保留 mock fallback。
4. 设计 Supabase schema，再做登录/同步和 trip persistence。
5. 在真实用户试用前，继续扩展移动端体验和视觉 polish。

## 关键文件索引

- `app/chat/page.tsx` — Chat / AI Butler 页面入口。
- `components/chat/ButlerWorkspace.tsx` — 主工作台状态管理。
- `components/chat/ChatPanel.tsx` — 聊天面板。
- `components/canvas/TripCanvas.tsx` — live trip canvas 组合组件。
- `components/canvas/DayCard.tsx` — 单日行程卡片。
- `components/canvas/ButlerRail.tsx` — 管家提醒栏。
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

PowerShell 环境可使用：

```bash
npm.cmd run test
npm.cmd run build
npm.cmd run test:e2e
```

