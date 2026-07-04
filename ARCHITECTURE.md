# ARCHITECTURE.md — VisePanda 系统架构

维护者：架构师 Claude Code session。任何架构级改动(新模块边界、新数据流、新端间契约)必须先记录到此文件 + `DESIGN.md` ADR，再开工。

## 1. Monorepo 布局

```
VP-Codex-Final/
├── app/                  Web 前端 + /api/* 后端(Next.js App Router)，权威后端
├── lib/                  Web 端业务逻辑(provider/orchestrator/repository)
├── android/              Android 原生 APK(Kotlin + Compose)— Android owner 独立负责
├── ios/                  iOS 原生 App(SwiftUI)— iOS owner 独立负责
├── supabase/migrations/  数据库 schema，按序号顺序执行
├── docs/planning/        规划类文档(纯文档迭代产出)
└── {PLAN,PRD,DESIGN,AGENTS,HANDOFF,CHANGELOG,VERSIONING}.md  共享文档
```

三端边界：**Web 是唯一后端**。Android/iOS 是纯客户端，通过 HTTP 调用 `app/api/*`，不得各自重新实现业务逻辑或直连数据库/第三方 API。

## 2. 数据流：CanvasPatch 管道(跨三端统一契约)

```
用户输入 → Chat UI → /api/chat → orchestrator(意图分类→provider选择→LLM/mock)
        → CanvasPatch { assistantMessage, assistantResponse?, patch, suggestions }
        → 客户端 applyCanvasPatch(本地 TripState 合并)
        → Trips/Day Detail 渲染
```

- 这是全项目最核心的不变量：**任何端**都不允许绕开这条链路直接拼装/合并 `TripState`/`TripDay`（例外：Explore 的 "Add to Trip" 一类的本地确定性追加，见下方 4.2，仍需在 ADR 中显式记录为例外）。
- Android 镜像实现：`data/model/ButlerModels.kt` + `CanvasPatchApplier.kt`。
- iOS 镜像实现：`Models/ButlerModels.swift` + `Data/CanvasPatchApplier.swift`。
- 三端字段必须保持 1:1（新增字段先改 `lib/types/trip.ts` 权威定义，再同步两端）。

## 3. 后端分层（Web，`app/api/*`）

| 路由 | 职责 | 详见 |
|---|---|---|
| `/api/chat` | Butler 对话入口，intent 分类 → provider 编排 → CanvasPatch | `lib/ai/orchestrator.ts` |
| `/api/trips` | Supabase trips/canvas_versions/messages persistence | `lib/supabase/tripsRepository.ts` |
| `/api/explore`, `/api/explore/amap` | POI 数据 provider（静态 fallback ← 高德） | `lib/explore/index.ts` |
| `/api/tools` | Tools 分类数据 + 实时汇率合并 | `lib/tools/index.ts` |
| `/api/exchange-rate` | 实时汇率单独端点 | 供 Web Tools 与 Android/iOS 复用 |
| `/api/translate/text`\|`ocr`\|`tts`\|`stt` | 翻译四件套，服务端代理外部 AI | `lib/aliyun/qwen.ts` |

完整字段级契约见 `API_SPEC.md`。

## 4. 客户端架构模式

### 4.1 Provider 抽象（Web 强制，Android/iOS 建议镜像）

数据源必须经过工厂函数（如 `getExploreProvider()`/`getToolsProvider()`），组件不得直接 import 具体 provider 或硬编码数据。每个 provider 必须实现 `getProviderStatus()`（mode/coverage/candidate/limitations），供内部/调试面板使用，不进入用户可见 UI（除非产品明确要求）。

### 4.2 本地确定性写入（例外于 CanvasPatch 管道）

以下几类操作被明确判定为"本地状态管理"而非"AI 决策"，允许绕开 CanvasPatch 直接写本地状态：

- 行程重命名（`renameActiveTrip`）
- 提醒完成勾选（`setAlertDone`）
- Explore "Add to Trip"（用户自选 POI，非 AI 生成：`addPoiToDay`）
- Day Detail 描述编辑/时段重排（`updateBlockDescription`/`moveBlock`）

新增此类"本地写入"前必须在 PR 描述中说明为什么它不该走 AI 管道，Reviewer 审核时重点核对是否被滥用来绕过 CanvasPatch。

### 4.3 离线优先

- Android/iOS 本地缓存层（Room / UserDefaults，未来可能升级 SQLite/SwiftData）保存当前 trip + messages，网络失败时展示上次缓存内容，而不是空白/崩溃。
- 静态 provider fallback（Explore/Tools 的 staticProvider）必须永久保留，不因为接入真实数据源而删除。
- 客户端反序列化边界必须做 null 归一化（历史 schema 缺字段时不能让"非空类型"在运行时是 null 后传播到下游——Android 的 `TripJson.decodeTrip()` 是参考实现）。

## 5. 密钥与安全边界

- 所有第三方/AI Key 只在 Web 后端环境变量读取（`lib/env/placeholders.ts` 是权威登记表），不进浏览器 bundle、不进 Android/iOS 客户端代码、不进任何 md 文档。
- Android/iOS 需要的唯一"密钥"是 `VISEPANDA_API_BASE_URL`（指向 Web 后端），无需各自持有 AI/第三方 key。
- `SUPABASE_SERVICE_ROLE_KEY` 只允许服务端 admin 代码使用。

## 6. 变更流程

1. 架构级改动先更新本文件 + `DESIGN.md` ADR。
2. 端到端契约变化（新字段/新路由）先更新 `API_SPEC.md`。
3. 移动端专属规范变化更新 `MOBILE_STANDARD.md`。
4. Issue 必须标注 Scope + Do not touch + Acceptance + 架构师预分配的版本号；PR 必须链接对应 Issue 并逐项填写 `.github/PULL_REQUEST_TEMPLATE.md`。
5. Reviewer（架构师会话）只审架构合规性，不改 PR 内容；不合规驳回并说明违反哪条规则。
6. 完整的多 Agent 协作规则（角色分工、六道防线、版本号所有权、冲突仲裁、发布线）见 `AGENTS.md` 末尾"多 Agent GitHub 协作规则"章节——该章节为协作权威。
