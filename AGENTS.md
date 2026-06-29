# VisePanda — Agent 操作指南

## 项目概述

VisePanda 是一个面向外国人来中国旅行的 AI 管家产品。当前主线是：右侧持续 Chat，左侧 Live Trip Canvas 实时刷新；Trips 已从占位页升级为 saved trips dashboard 骨架，后续会承接真实保存、恢复和分享。

## 核心技术栈

- 前端：Next.js App Router、React、TypeScript。
- AI：DeepSeek V4 Flash provider + mock fallback。
- 数据库：Supabase（`v0.1.11` 起接入真实 magic link 登录和 trips/canvas_versions/messages persistence；`v0.1.12` 起 guest draft 登录后自动迁移；未配置环境变量时优雅回落到 guest/mock 体验）。
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
- 不要重新加入独立 Practical Reminder / Butler Rail 区块；管家提醒后续应进入 Tools 或更轻量的上下文提示，不占据 Canvas 顶部。
- 桌面横屏端优先保持一屏固定工作台；移动竖屏细节可以后续再精修。
- 不要让移动导航遮挡核心内容。
- Trips/Chat 现在有真实 Supabase persistence 首个闭环（保存、读取、恢复），guest draft 登录后会自动迁移，且已有 trip detail 页面（`/trips/[id]`）；但仍没有归档、分享流程。
- 所有 Supabase 读写必须经过 `lib/supabase/tripsRepository.ts`，复用已确认的 `users`/`trips`/`canvas_versions`/`messages` 表结构，不要另起字段命名、拆分表，也不要绕开 repository 直接在组件里拼 Supabase 查询。
- Supabase 相关代码必须在未配置环境变量、未登录、网络失败时优雅降级（不崩溃，回落到 guest/mock 体验），参考 `lib/supabase/client.ts` 的 `isSupabaseConfigured` 模式。
- `SUPABASE_SERVICE_ROLE_KEY` 当前未在任何代码中使用；不要在浏览器端代码中引入它。

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
