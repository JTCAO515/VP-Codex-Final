# PROJECT_CONTEXT.md — VisePanda 项目背景

## 产品定位

VisePanda：面向外国人来华旅行的 AI 管家产品。核心循环：Chat 驱动一切，AI 输出通过 `CanvasPatch` 结构化写入 Trip 画布，Trips/Explore/Tools 围绕这个循环提供延续性、发现、执行工具。目标是"中国旅行操作系统"，不只是行程生成器。

## 当前主线

- **Web**（`app/`, Next.js App Router + React + TypeScript）：功能最完整，版本序列 `0.2.x` 已冻结在 `v0.2.17`，之后只做维护性改动（`AGENTS.md` 有明确记录哪些改动碰过 Web）。
- **Android**（`android/`，Kotlin + Jetpack Compose + Material 3）：`v0.3.x` 系列，原生 APK 主线，Claude Code 独立负责。
- **iOS**（`ios/`，SwiftUI）：Codex 独立负责，已有 5-Tab 骨架（Chat/Trips/Explore/Tools/Me）+ mock fallback + `/api/chat` 调用，非空壳。

三端共享同一个 monorepo 和同一套后端 `/api/*` 路由；产品行为以 Web 端已实现内容为准，Android/iOS 是移植，不各自发明新产品逻辑。

## 团队协作模式(本轮起生效)

- **架构师 + Reviewer**（本 Claude Code session）：创建 Issue、维护 `ARCHITECTURE.md`/`API_SPEC.md`/`MOBILE_STANDARD.md`、审核所有 PR 架构合规性、仲裁冲突。不写端侧业务代码，不直接改 PR 内容。
- **Codex**：iOS 开发，`agent/*` 分支开 PR。
- **Antigravity**：Android 开发，`agent/*` 分支开 PR。
- 协作全程通过 GitHub 完成（Issue → PR → Review → 合并入 `dev`），不追求三方实时连通。

## 核心技术栈（Web，后端权威来源）

- Next.js App Router / React / TypeScript，图标 lucide-react。
- AI：多模型编排（DeepSeek/Qwen/Zhipu/Moonshot/ERNIE/MiniMax），`lib/ai/orchestrator.ts` 统一入口，任何时候都必须以 mock Butler fallback 兜底。
- 数据库：Supabase（未配置时优雅降级到 guest/mock，不崩溃）。
- 部署：Vercel，生产域名 `go2china.space`。

## 硬约束（贯穿三端）

- 所有第三方/AI API Key 只能服务端读取，不进浏览器、不进仓库、不进文档。
- 翻译类外部调用必须走 `/api/translate/*` 服务端代理。
- 所有 AI 输出必须通过 `CanvasPatch` 契约进入画布/行程状态，任何端都不允许 UI 直接拼装/解析自然语言写状态。
- 必须保留 mock/static fallback，替换 mock 为真实实现时旧 fallback 不能删除。
- Supabase 未配置或未登录时降级到 guest/mock 体验，不崩溃。

## 详见

- 产品需求：`PRD.md`
- 架构与 ADR：`DESIGN.md` / `ARCHITECTURE.md`
- 执行计划：`PLAN.md`
- 多 Agent 协作细则：`AGENTS.md`
- 版本历史：`CHANGELOG.md` / `VERSIONING.md`
- 交接状态：`HANDOFF.md`
