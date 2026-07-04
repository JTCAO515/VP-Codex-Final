# VisePanda — Project Context

> 所有 Agent 开工前必读。了解这个项目是什么、为什么存在、技术选型、关键决策。

## 一句话

VisePanda 是一个面向外国人来中国旅行的 AI 管家，解决西方游客来华前的五大恐惧：签证、支付、联网、语言、行程规划。

## 目标用户

来中国旅行的国际游客（非中文母语者）。从"好奇"到"规划"到"准备"到"在中国"到"分享"，全旅程覆盖。

## 核心技术选型

| 层 | 技术 |
|------|------|
| Web 前端 | Next.js 15 App Router + React 19 + TypeScript |
| 后端 | Next.js API Routes（服务端，不暴露 Key） |
| 数据库 | Supabase（PostgreSQL，行级安全） |
| AI Provider | DeepSeek V4 Flash（主）、Qwen DashScope（翻译/OCR/TTS/STT）、智谱 GLM5、月之暗面 Kimi |
| 地图 | 高德 AMAP（POI 搜索 REST API + JS API 地图显示） |
| 部署 | Vercel，生产域名 `go2china.space` |
| 移动端 iOS | SwiftUI + UIKit（全新启动） |
| 移动端 Android | Kotlin + Jetpack Compose + Material 3（v0.3.x 进行中） |

## 核心产品逻辑

产品围绕一个核心概念构建：**AI 管家（Butler）+ 行程画布（Trip Canvas）**。

1. 用户在 **Chat** 中与 AI 对话，AI 输出结构化回复并修改行程画布
2. **Trip Canvas** 是"唯一真相源"，以天为单位展示行程（Morning/Afternoon/Evening）
3. **Explore** / **Tools** / **Translate** 是围绕行程的支持系统
4. 所有数据流经 AI pipeline，端侧不得绕过

## 关键架构决策（ADR）

- AI 输出必须通过 `CanvasPatch` 结构进入画布，UI 不直接解析自然语言
- 所有翻译 API 调用经过服务端代理，浏览器不传 API Key
- 所有 Provider 必须保留 mock/static fallback，上游不可用时优雅降级
- Supabase 未配置时以 guest/mock 模式运行
- 移动端不直接拼 Supabase 查询，通过 `/api/` 路由走服务端代理
- 桌面横屏优先一屏固定布局，移动端底部导航

## 版本命名

- Web 端：`0.1.x`（已完成）→ `0.2.x`（过渡中）→ `0.3.x`（当前）
- 移动端 Android：`0.3.x`（与 Web 对齐）
- 移动端 iOS：`0.3.x`（新启动，与 Web 对齐）

## 仓库结构

```
VP-Codex-Final/
├── app/              ← Next.js App Router（Web 前端 + API 路由）
├── components/       ← React 组件
├── lib/              ← 核心逻辑（provider 抽象、AI 管线、Supabase 封装）
├── android/          ← Android 原生代码（Kotlin + Compose）
├── ios/              ← iOS 原生代码（待创建）
├── supabase/         ← 数据库迁移 SQL
├── docs/             ← 规划文档
├── tests/            ← 测试
├── AGENTS.md         ← 多 Agent 规则（必读）
├── PLAN.md           ← 执行计划
├── PRD.md            ← 产品需求
├── DESIGN.md         ← 架构决策记录
├── API_SPEC.md       ← 接口规范（必读）
└── MOBILE_STANDARD.md ← 移动端规范（必读）
```
