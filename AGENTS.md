# VisePanda — Agent 操作指南

## 项目概述

VisePanda 是一个面向外国人来中国旅行的 AI 管家产品。当前阶段的核心是 Chat / AI Butler 工作台：右侧持续聊天，左侧实时生成和更新 trip canvas。

## 核心技术栈

- 前端：Next.js App Router、React、TypeScript
- 后端：Next.js API Routes（第一阶段为 placeholder/mock）
- 数据库：Supabase（后续接入，当前仅预留 env）
- AI：mock provider（后续替换为真实 provider）
- 测试：Vitest、Testing Library、Playwright
- 部署：Vercel

## 工作流程

1. 先读 `PLAN.md`，了解当前阶段和任务顺序。
2. 再读 `PRD.md`，确认产品定位、MVP 范围和不做什么。
3. 需要理解架构时读 `DESIGN.md`。
4. 每次修改前先确认是否会扩大当前阶段范围。
5. 修改核心逻辑前先写测试，遵循 test-first。
6. 每完成一个功能后运行相关测试。
7. 每次迭代都必须同步更新 `PLAN.md`、`PRD.md`、`DESIGN.md`、`AGENTS.md`、`HANDOFF.md`。
8. 离开前更新 `HANDOFF.md`，记录状态、已知问题和下一步。

## 编码规范

- 命名：React 组件使用 PascalCase，函数和变量使用 camelCase。
- 类型：共享产品类型放在 `lib/types/`。
- 业务逻辑：mock AI 放在 `lib/mock-ai/`，canvas reducer 放在 `lib/canvas/`。
- UI 组件：Shell、Chat、Canvas、Placeholder 分目录维护。
- 注释：只在复杂逻辑处写短注释，不写重复解释。
- 样式：当前使用 `app/globals.css` 和稳定 class name；不要随意引入 UI 库。
- 测试：新增行为必须有 Vitest 或 Playwright 覆盖。

## 重要约束

- 不要把 Trips、Explore、Tools、Account 做成完整功能，除非 PLAN 进入对应阶段。
- 不要接入未验证的 Trip.com、Meituan、Amap API。
- 不要把真实 API key 写进仓库。
- 不要删除 mock provider；真实 AI 接入后也必须保留 fallback。
- 不要使用半透明玻璃聊天框。
- 不要在 Trip Canvas 主界面直接展开每日详情；主界面只保留每日一句总结，完整详情进入抽屉。
- 不要重新加入独立 Practical Reminder / Butler Rails 区块；管家提醒合并在顶部五张任务卡。
- 桌面横屏端优先保持一屏固定工作台；移动竖屏细节可以后续再精修。
- 不要让移动导航遮挡核心内容。
- 优先保证 Chat / AI Butler 主体验稳定。

## 常用命令

```bash
npm install
npm run dev
npm run test
npm run build
npm run test:e2e
```

Windows PowerShell 如果拦截 `npm.ps1`，使用：

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
- [ ] 未完成任务已记录。
- [ ] `npm run test` 已通过，或失败原因已记录。
- [ ] `npm run build` 已通过，或失败原因已记录。
- [ ] `npm run test:e2e` 已通过，或失败原因已记录。
- [ ] 没有把真实密钥、临时截图、`node_modules`、`.next` 提交进仓库。
