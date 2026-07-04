# VisePanda — 系统架构

> 所有 Agent 开工前必读。了解系统结构、数据流、模块边界。
> 详细 API 定义见 `API_SPEC.md`，详细 ADR 见 `DESIGN.md`。

## 架构概览

```
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│   Web App   │  │   iOS App   │  │  Android App│
│ (Next.js)   │  │  (SwiftUI)  │  │ (Kotlin)    │
├─────────────┤  ├─────────────┤  ├─────────────┤
│  Chat       │  │  Chat       │  │  Chat       │
│  Trips      │  │  Trips      │  │  Trips      │
│  Explore    │  │  Explore    │  │  Explore    │
│  Tools      │  │  Tools      │  │  Tools      │
│  Translate  │  │  Translate  │  │  Translate  │
│  Community  │  │  Account    │  │  Account    │
└──────┬──────┘  └──────┬──────┘  └──────┬──────┘
       │                │                │
       └────────────────┼────────────────┘
                        │
              ┌─────────▼─────────┐
              │   Next.js API     │
              │   (Server-side)   │
              │                   │
              │  /api/chat        │
              │  /api/translate/* │
              │  /api/explore/amap│
              │  /api/exchange-   │
              │    rate           │
              └─────────┬─────────┘
                        │
          ┌─────────────┼─────────────┐
          │             │             │
   ┌──────▼──────┐ ┌───▼───┐ ┌──────▼──────┐
   │  AI Models  │ │Supabase│ │3rd Party    │
   │  DeepSeek   │ │ Post-  │ │ AMAP        │
   │  Qwen       │ │ greSQL │ │ Exchange-   │
   │  GLM        │ │       │ │ Rate API    │
   │  Kimi       │ │       │ │ OCR.space   │
   └─────────────┘ └───────┘ └─────────────┘
```

## 数据流：Chat（核心管道）

用户消息 → 页面 → POST /api/chat → AI Provider → CanvasPatch → 画布更新

```
User types "Plan 5 days in Beijing"
  → app/chat/page.tsx
    → components/chat/ButlerWorkspace.tsx
      → POST /api/chat { message, trip?, messages?, preferenceProfile? }
        → lib/ai/orchestrator.ts (意图分类 + 路由)
          → lib/ai/deepseekButler.ts (DeepSeek 调用)
            → 返回 { patch: CanvasPatch, suggestions: string[] }
        → applyCanvasPatch(patch) → 更新 TripState
      → UI 刷新 Chat + Trip Canvas
```

**关键约束：**
- 所有画布变更必须经过 POST /api/chat → CanvasPatch 管道
- Explore / Tools / Community 中任何"Add to Trip"按钮都必须跳转 `/chat?add=` 走 AI pipeline
- 端侧不得直接拼装 TripDay 或写入画布

## 数据流：翻译

```
Text Translation:
  app/translate/page.tsx
    → POST /api/translate/text { text, from, to }
      → 首选 Qwen DashScope → fallback DeepSeek → 返回翻译文本

OCR Translation:
  app/translate/page.tsx (拍照/上传)
    → 客户端调整尺寸 (Canvas API, max 1200px)
    → POST /api/translate/ocr { imageBase64, mimeType }
      → Qwen OCR API → 返回识别文本 → 自动送入翻译流程 → 返回翻译

TTS:
  app/translate/page.tsx → POST /api/translate/tts { text, voice }
    → Qwen TTS API → 返回 audioUrl → 播放

STT:
  app/translate/page.tsx (录音)
    → POST /api/translate/stt { audioBase64 }
    → Qwen STT → 返回识别文本 → 自动送入翻译流程
```

## 数据流：Explore

```
用户浏览城市/分类
  → lib/explore/index.ts → getExploreProvider()
    → 生产: lib/explore/amapProvider.ts → GET /api/explore/amap?city=X&category=Y
    → 降级: lib/explore/staticProvider.ts (内建静态数据)
  → 用户点击"Add to Trip"
    → 跳转 /chat?add={<草稿消息>} → 走 AI pipeline
```

## 数据流：Tools

```
用户浏览工具分类
  → lib/tools/index.ts → getToolsProvider()
    → 生产: lib/tools/liveToolsProvider.ts → GET /api/exchange-rate (汇率实时)
    → 基础: lib/tools/staticProvider.ts (7个分类静态内容)
  → 分类可通过 URL 深链: /tools?category=<tool-category-id>
```

## 数据持久化

```
未登录 / Supabase 未配置:
  localStorage (Web) / DataStore (Android) / UserDefaults (iOS)
  - trip drafts
  - chat history
  - preference profile
  - selected avatar

已登录 + Supabase 已配置:
  PostgreSQL (Supabase)
  - public.users (id, email, display_name)
  - public.trips (id, owner_id, title, status, share_token, canvas_version_id)
  - public.canvas_versions (id, trip_id, canvas_jsonb, reason)
  - public.messages (id, trip_id, role, content)
  - 全部行级安全 (RLS)，按用户隔离
```

## 模块边界

| 模块 | 目录 | 职责 |
|------|------|------|
| Chat + 画布 | `components/chat/`, `components/canvas/` | AI 对话、行程画布展示与编辑 |
| Trips | `components/trips/`, `app/trips/` | 行程列表、详情、归档/分享 |
| Explore | `components/explore/`, `lib/explore/` | 城市数据、POI 浏览 |
| Tools | `components/tools/`, `lib/tools/` | 实用工具分类 |
| Translate | `components/translate/`, `app/translate/` | 翻译三 Tab |
| Community | `components/community/`, `lib/community/` | 社区动态、热区、照片 |
| Account | `components/account/`, `lib/supabase/auth.ts` | 登录、个人中心 |
| AI | `lib/ai/` | Provider 抽象、意图分类、Orchestrator |
| 类型 | `lib/types/` | 核心数据类型 |
| API 路由 | `app/api/` | 所有服务端接口 |
| 数据层 | `lib/supabase/` | Supabase 客户端、Repository、Schema |

## 不可违背的原则

1. **API Key 永不进浏览器** — 所有外部 API 调用必须通过 Next.js 服务端路由
2. **mock fallback 永不删除** — 真实接入后也必须保留降级路径
3. **画布变更不走捷径** — 任何修改 TripState 的路径都必须经过 AI pipeline
4. **探针不可达时降级而非中断** — 用户不应因为 API Key 缺失或上游失败而看到白屏
5. **Web 是功能参照** — 移动端优先参考 Web 实现的功能逻辑，而非 UI 样式
