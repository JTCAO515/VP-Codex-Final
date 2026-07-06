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

### `patch.affectedDays`（Chat→Trips 反向链路契约，2026-07-07）

`CanvasPatch` 新增可选字段 `affectedDays?: number[]`：服务端在 `app/api/chat/route.ts` 的单一出口处，用请求带上来的 `trip.days`（改动前）和 `patch.days`（改动后）逐天 diff 算出的实际被新增/删除/改动过的 day 序号（`lib/canvas/applyCanvasPatch.ts` 的 `computeAffectedDays`），覆盖 butler-service 转发、factual tools、真实 LLM race、mock 兜底所有路径，客户端不需要自己 diff。

- `patch.days` 未提供（比如纯 `add_alerts` 回复）→ `affectedDays` 为 `[]`。
- 客户端只在 `affectedDays` 非空时才允许在 Chat 消息里展示"跳转到 Trips 对应天"的入口，纯文本回答或没有真正改动行程时不能出现这个入口。
- 如果某个 day 序号出现在 `affectedDays` 里但客户端本地 Trips 数据中已经不存在这一天（比如该天被同一个 patch 删除），跳转动作应该优雅降级为切到 Trips 首页而不是崩溃或报错。

### `patch.assistantResponse.exploreRefs`（Issue #50，Chat↔Explore 打通契约）

`AssistantResponse` 新增可选字段 `exploreRefs?: ExploreRef[]`：

```json
{
  "amapPoiId": "string — 高德原始 POI id（与 ButlerToolPoi.id / 移动端 AmapPoiJson.id 一致,不带 \"amap-\" 前缀）",
  "name": "string",
  "cityId": "string — 与 AMAP_CITY_MAP key 一致,如 beijing",
  "category": "\"attractions\" | \"food\" | \"stays\"",
  "subcategory": "string | undefined",
  "rating": "number | undefined",
  "pricePerPerson": "number | undefined"
}
```

**生成规则**（`lib/ai/toolContextWriteThrough.ts` 的 `buildExploreRefs`）：只对本次请求真实拿到的 `toolContext.pois`(见下方 `toolContext` 字段)按名称文本匹配 `assistantResponse` 的 `headline`/`body`/`highlights`,命中才写入 `exploreRefs`——**绝不让模型自由生成 id**,匹配不到时 `exploreRefs` 为空数组或字段整体缺失。移动端渲染规则:`exploreRefs` 为空/缺失时不渲染任何占位卡,不造假数据。

**移动端消费**：Butler 消息气泡下渲染 `exploreRefs` 为可点横滑小卡(名称+评分+人均);点击跳转 Explore 频道页并按 `cityId`/`category`/`amapPoiId` 定位。此契约落地后,Android/iOS 各自的客户端渲染在后续 Issue 中实现。

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

## GET /api/explore/amap — 高德 POI(v0.3.20 起支持大众点评式 Explore 重做,Issue #46)

**Query params**：
- `cityId`(必填,需在 `AMAP_CITY_MAP` 中:beijing/shanghai/chengdu/xian/guangzhou/hangzhou/suzhou/chongqing/nanjing)
- `type`(必填,需在 `AMAP_TYPE_MAP` 中——见下方完整分类表)
- `keyword`(可选,与分类自带的 keyword 组合,`|` 连接传给高德)
- `mode`(可选,`city`(默认)| `around`)
- `location`(mode=around 时必填,`"lng,lat"`,须在中国境内合理经纬度范围,否则 400)
- `radius`(可选,米,仅 around 模式生效,自动 clamp 到 500-50000)
- `sort`(可选,`weight`(默认)| `distance`,distance 仅 around 模式有意义)
- `page`(可选,默认 1,1-100)

**分类表(`AMAP_TYPE_MAP` 完整清单,语义化 key,不需要客户端自己拼高德码)**:

| 大类 | 二级 key | 大类 | 二级 key |
|---|---|---|---|
| `food` | `food.hotpot`/`food.sichuan`/`food.cantonese`/`food.japanese`/`food.bbq`/`food.dessert`/`food.fastfood`/`food.cafe` | `shopping` | `shopping.mall`/`shopping.specialty`/`shopping.supermarket` |
| `attractions` | `attractions.scenic`/`attractions.park`/`attractions.museum`/`attractions.temple` | `experiences` | `experiences.massage`/`experiences.bath`/`experiences.spa`/`experiences.teahouse`/`experiences.ktv` |
| `hotels`(`stays` 为兼容别名) | `hotels.star`/`hotels.economy`/`hotels.hostel` | | |

**Response(成功)**
```json
{ "ok": true, "cityId": "string", "type": "string", "page": 1, "hasMore": true, "pois": "AmapPoi[]" }
```
每个 `poi` 若命中 `curated_pois` 知识库(Issue #49),会附加可选 `editorial` 字段(老客户端忽略即可,超集扩展):
```json
{ "editorial": { "summary": "string", "tags": ["string"], "badges": ["string"], "badge": "VisePanda Editorial" } }
```

**Response(失败)**:503 `not_configured`(无 `AMAP_API_KEY`)/ 400 `invalid_params` / `invalid_location`(mode=around 但 location 不合法)/ `invalid_page` / 502 `upstream_error`。

## GET /api/explore/baidu — 百度 POI(体验品类 Phase 2 占位接入)

独立百度地图 Place API v2 数据源,先只覆盖 `experiences*` 体验品类,不改变 `/api/explore/amap` 既有行为。服务端读取 `BAIDU_MAP_AK`; 当前未提交真实 AK/真实调用结论,部署真实 key 后再补 PR 描述里的实测响应与覆盖价值判断。

**Query params**:
- `cityId`(必填,同 `AMAP_CITY_MAP`)
- `type`(必填,仅 `experiences`/`experiences.massage`/`experiences.bath`/`experiences.spa`/`experiences.teahouse`)
- `keyword`(可选,与百度体验品类关键词组合)
- `page`(可选,默认 1,1-100; 服务端转换为百度 `page_num` 0-based)

**Response(成功)**
```json
{ "ok": true, "cityId": "string", "type": "string", "page": 1, "hasMore": true, "pois": "BaiduPoi[]" }
```
`BaiduPoi` 保留百度原始字段(`uid`/`name`/`address`/`telephone`/`location`/`detail_info`)。若同名命中高德同页结果且 `AMAP_API_KEY` 已配置,会附加可选 `crossValidation`:
```json
{ "crossValidation": { "amapRating": "4.5", "baiduRating": "4.6", "match": "name" } }
```

**Response(失败)**:503 `not_configured`(无 `BAIDU_MAP_AK`)/ 400 `invalid_params` / `invalid_page`。

## GET /api/explore/curated — VisePanda 编辑精选(Issue #49)

纯知识库数据,不打真实高德请求。用于 Explore 首页的"编辑精选"置顶区和 UGC mock feed(规格 §1.1)。

**Query params**:`cityId`(必填)、`category`(可选,需在 `AMAP_TYPE_MAP` 中)。

**Response(成功)**
```json
{ "ok": true, "cityId": "string", "category": "string|null", "entries": "CuratedPoi[]" }
```
`CuratedPoi` 字段:`city_id`/`category`/`amap_poi_id`/`name`/`name_en`/`editorial_summary`/`tags[]`/`list_badges[]`/`photo_url`/`rank`/`source`(`wikivoyage`|`official_list`|`llm_seed`)/`source_url`。

**Response(失败)**:400 `invalid_params`。表为空或 Supabase 未配置时返回 `entries: []`,不是错误。

**客户端规则**:任一失败都必须回落到静态/mock POI 列表,不能空白页或崩溃。老参数组合(`cityId`+`type`,不带 mode/location)行为与升级前完全一致,向后兼容。

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
