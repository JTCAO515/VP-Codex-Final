# API_SPEC.md — VisePanda 后端接口规范

权威后端只有一个：Web 端 `app/api/*`（Next.js Route Handlers）。Android/iOS 通过 `VISEPANDA_API_BASE_URL` HTTP 调用，不得各自实现同名逻辑。字段名两端必须与此文档一致；改动路由前先改此文件。

约定：所有响应含 `ok: boolean`；失败时额外带 `error`（机器可读 code）和可选 `message`（人类可读，仅供日志/调试，不建议直接展示给用户）。

## POST /api/chat — Butler 对话

**Request body**
```json
{
  "message": "string, required, 非空",
  "trip": "TripState, required — 见 lib/types/trip.ts",
  "messages": "ChatMessage[], optional，最近对话历史",
  "preferenceProfile": "UserPreferenceProfile, optional"
}
```
- `trip` 缺失或不是合法 `TripState`(至少含 `summary`/`days: []`/`alerts: []`) → 400 `trip_required`。
- `message` 空 → 400 `message_required`。

**Response（成功）**
```json
{
  "ok": true,
  "mode": "string — 例如 mock/live/tools 等 orchestrator 策略标签",
  "modelLabel": "string",
  "intent": "string",
  "strategy": "string",
  "providersTried": "string[]",
  "patch": "CanvasPatch — { assistantMessage, assistantResponse?, ... }，客户端用它 merge TripState",
  "suggestions": "string[] — 建议问题，UI 状态，不写入 CanvasPatch",
  "toolContext": "object | undefined"
}
```

**Response（失败）**：503 `{ ok: false, error: "butler_unavailable", message }`。

**客户端契约**：收到 `patch` 后必须走 `applyCanvasPatch`（或镜像实现）合并本地 `TripState`，不允许自己拼装。Android 参考 `CanvasPatchApplier.kt`，iOS 参考 `CanvasPatchApplier.swift`。

## GET /api/tools — Tools 分类

**Response**
```json
{
  "ok": true,
  "provider": "string — provider id",
  "status": "ProviderStatus — mode/coverage/candidateProviders/limitations，内部/调试用，不展示给用户",
  "categories": "ToolCategory[] — 每项含 tips/sections/offlineTips/apiPriority"
}
```
- `currency` 分类的汇率字段由 provider 内部合并 `/api/exchange-rate` 结果，失败时回退 provider 自带的固定汇率（不是这个路由本身失败）。

## GET /api/exchange-rate — 实时汇率

**Response（成功）**
```json
{ "ok": true, "base": "CNY", "rates": { "USD": 0.14, "...": 0 }, "updatedAt": "string" }
```
**Response（失败）**：
- 503 `not_configured`（`EXCHANGE_RATE_API_KEY` 未配置）
- 502 `upstream_error` / 具体 `error-type`

**客户端规则**：调用失败时必须显示诚实的"实时汇率不可用"提示，禁止伪造/编造汇率数字（Android 参考 `LiveToolsRepository.fetchLiveRates()` 的 `runCatching` 优雅降级模式）。

## GET /api/explore/amap — 高德 POI

**Query params**：`cityId`（必填，需在 `AMAP_CITY_MAP` 中）、`type`（必填，需在 `AMAP_TYPE_MAP` 中）、`keyword`（可选）。

**Response（成功）**
```json
{ "ok": true, "cityId": "string", "type": "string", "pois": "AmapPoi[]" }
```
**Response（失败）**：503 `not_configured`（无 `AMAP_API_KEY`）/ 400 `invalid_params` / 502 `upstream_error`。

**客户端规则**：任一失败都必须回落到静态/mock POI 列表，不能空白页或崩溃。

## GET /api/explore — Explore provider 聚合入口

聚合 static ← amap 链式回退，字段与 `getExploreProvider()` 一致；Android/iOS mock-first 阶段可暂不接此路由，但接入时必须遵守相同的链式回退语义，不允许跳过 static fallback。

## POST /api/trips — Supabase 行程持久化

Web 端 `lib/supabase/tripsRepository.ts` 的薄封装，Supabase 未配置/未登录时优雅降级。Android/iOS 当前使用本地 Room/UserDefaults 缓存，尚未接入此路由 —— 接入前须先在 `ARCHITECTURE.md`/本文件补充字段级契约，不得先斩后奏。

## POST /api/translate/text | ocr | tts | stt — 翻译四件套

服务端代理 Aliyun Bailian Qwen（`qwen-mt-flash`/`qwen3.5-ocr`/`qwen3-tts-instruct-flash`/`qwen3-asr-flash`），Key 只在服务端读取。客户端（含 Android/iOS）禁止直连 DashScope/Bailian。Android/iOS 已接入文本翻译（v0.3.15 / iOS v0.1），OCR/STT 尚未接入，接入时按下方字段对齐，不新起翻译服务。

### POST /api/translate/ocr — 拍照/图片翻译

**Request**：`{ imageBase64: string, mimeType?: string(默认 "image/jpeg") }`
**Response（成功）**：`{ ok: true, provider: string, model: string, text: string }` —— `text` 是从图片提取的原始文字（中英文皆可能），**不是翻译结果**；客户端拿到 `text` 后再调用 `/api/translate/text` 走一次翻译，两步拼起来才是完整体验。
**Response（失败）**：503 `not_configured` / 502 其它错误。

**客户端规则（强制）**：
- 图片必须先本地压缩再上传：最长边不超过 1200px（参照 Web 端 Canvas API 压缩逻辑），不允许直接上传原图（体积/延迟都不可接受）。
- 拍照权限：只在用户点击"拍照翻译"入口时申请，不预先请求；拒绝后显示诚实提示（"需要相机权限才能拍照翻译，可在系统设置里开启"），不崩溃、不重复弹权限框。

### POST /api/translate/stt — 语音转文字

**Request**：`{ audioBase64?: string, audioUrl?: string, mimeType?: string(默认 "audio/mpeg"), language?: string(默认 "zh") }` —— 二选一提供音频来源，`audioBase64` 用于本地录音场景。
**Response（成功）**：`{ ok: true, provider: string, model: string, text: string, language: string }` —— `text` 是识别出的原文，同样**不是翻译结果**，需要的话再调 `/api/translate/text`。
**Response（失败）**：503 `not_configured` / 502 其它错误。

**客户端规则（强制）**：
- 麦克风权限：只在用户点击"语音翻译"录音按钮时申请，拒绝后显示诚实提示，不崩溃。
- 录音时长建议客户端侧设合理上限（如 30-60s），避免生成超大 base64 payload。

## 通用规则

1. 新增/修改路由字段：先改本文件，再改代码，PR 里必须引用本文件对应章节。
2. 任何路由都不允许把第三方 Key 通过响应体透出给客户端。
3. 客户端对所有路由的失败分支都必须有非崩溃、诚实披露的降级 UI（不是空白页，不是假数据）。
