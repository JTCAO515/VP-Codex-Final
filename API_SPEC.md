# VisePanda — API 规范手册

> **架构真理源之一。** 所有 Agent 开工前必须读取。
> 定义全局唯一接口、结构体、字段、路由标准。
> 仅 Claude Code 有权修改本文档。端侧不得私自新增接口或修改字段。

---

## 1. 环境变量

所有 API Key 仅服务端读取，禁止传到浏览器端或提交仓库。

| 变量 | 用途 | 必需 |
|------|------|------|
| `DEEPSEEK_API_KEY` | Chat/翻译 fallback | 是（生产） |
| `DASHSCOPE_API_KEY` | 阿里云 Qwen 翻译/OCR/TTS/STT | 是（生产） |
| `EXCHANGE_RATE_API_KEY` | 实时汇率（exchangerate-api.com） | 否 |
| `AMAP_API_KEY` | 高德地图 Explore POI 搜索 | 否 |
| `AMAP_MAPS_KEY` | 高德地图 JS API（NEXT_PUBLIC） | 否 |
| `SUPABASE_URL` | Supabase 项目 URL | 否 |
| `SUPABASE_ANON_KEY` | Supabase 匿名公钥 | 否 |
| `SUPABASE_SERVICE_ROLE_KEY` | Supabase 服务端管理员 key（仅服务端） | 否 |

可选 override：
- `DASHSCOPE_COMPATIBLE_BASE_URL`
- `DASHSCOPE_BASE_URL`
- `QWEN_TRANSLATE_MODEL`
- `QWEN_OCR_MODEL`
- `QWEN_TTS_MODEL`
- `QWEN_STT_MODEL`

---

## 2. API 路由一览

| 路由 | 方法 | 用途 | 状态 |
|------|------|------|------|
| `/api/chat` | POST | AI 聊天核心入口 | 生产 |
| `/api/trips` | GET | Trips 持久化占位 | 占位 |
| `/api/explore` | GET | Explore 数据占位 | 占位 |
| `/api/explore/amap` | GET | 高德 POI 搜索 | 生产 |
| `/api/tools` | GET | Tools 数据占位 | 占位 |
| `/api/exchange-rate` | GET | 实时汇率 CNY | 生产 |
| `/api/translate/text` | POST | 文字翻译 | 生产 |
| `/api/translate/ocr` | POST | 图片 OCR 扫描翻译 | 生产 |
| `/api/translate/tts` | POST | 文字转语音 | 生产 |
| `/api/translate/stt` | POST | 语音转文字 | 生产 |

---

## 3. 核心接口详情

### 3.1 POST `/api/chat` — AI 聊天入口

**请求体：**
```typescript
{
  message: string;                          // 用户消息
  trip?: TripState;                         // 当前行程画布（可选，没有则新建）
  messages?: ChatMessage[];                 // 最近对话历史（可选）
  preferenceProfile?: UserPreferenceProfile; // 用户偏好画像（可选）
}
```

**成功响应（200）：**
```typescript
{
  ok: true;
  fallbackReason?: string;                  // 若使用了 fallback，说明原因
  mode: "mock" | "deepseek" | "glm";
  modelLabel: string;
  intent: ButlerIntent;                     // 识别的用户意图
  strategy: string;                         // AI 采用的回复策略
  providersTried: string[];                 // 尝试过的 provider 列表
  patch: CanvasPatch;                       // 画布变更（核心输出）
  suggestions: string[];                    // 建议的追问问题（2个）
  toolContext?: ButlerToolContext;           // 工具上下文（POI 数据等）
}
```

**错误响应：**
```typescript
{ ok: false, error: string }
```

---

### 3.2 POST `/api/translate/text` — 文字翻译

**请求体：**
```typescript
{
  text: string;         // 待翻译文本
  from?: string;        // 源语言代码，默认 "en"
  to?: string;          // 目标语言代码，默认 "zh"
}
```

**成功响应：**
```typescript
{
  ok: true;
  provider: "aliyun" | "deepseek";
  model: string;
  from: string;
  to: string;
  translatedText: string;
}
```

**错误：** 400 (missing_text) / 503 (not_configured)

---

### 3.3 POST `/api/translate/ocr` — 图片 OCR 扫描翻译

**请求体：**
```typescript
{
  imageBase64: string;     // Base64 编码的图片数据
  mimeType?: string;       // 图片类型，默认 "image/jpeg"
}
```

**成功响应：**
```typescript
{
  ok: true;
  provider: "aliyun";
  model: string;
  recognizedText: string;
  translatedText: string;
}
```

---

### 3.4 POST `/api/translate/tts` — 文字转语音

**请求体：**
```typescript
{
  text: string;               // 要朗读的文本
  language?: string;          // 语言，默认 "Chinese"
  voice?: string;             // 音色，默认 "Cherry"
  instructions?: string;      // 可选的发音指令
}
```

**成功响应：**
```typescript
{
  ok: true;
  provider: "aliyun";
  model: string;
  audioUrl: string;           // 可播放的音频 URL
}
```

---

### 3.5 POST `/api/translate/stt` — 语音转文字

**请求体：**
```typescript
{
  audioUrl?: string;           // 可直接访问的音频 URL
  audioBase64?: string;        // Base64 编码的音频数据
  mimeType?: string;           // 音频类型，默认 "audio/mpeg"
  language?: string;           // 语言，默认 "zh"
}
```

**成功响应：**
```typescript
{
  ok: true;
  provider: "aliyun";
  model: string;
  text: string;                // 识别出的文字
}
```

---

### 3.6 GET `/api/explore/amap` — 高德 POI 搜索

**查询参数：**
```
?city=beijing&category=attractions|food|stays
```

**成功响应：**
```typescript
{
  ok: true;
  provider: "amap";
  city: string;
  category: string;
  pois: AmapPoi[];             // POI 列表（空数组时回退 staticProvider）
}
```

---

### 3.7 GET `/api/exchange-rate` — 实时汇率

**成功响应：**
```typescript
{
  ok: true;
  base: "CNY";
  rates: Record<string, number>;  // {"USD": 0.14, "EUR": 0.13, ...}
  updatedAt: string;               // ISO 时间戳
}
```

---

## 4. 核心数据结构

### 4.1 `trip.ts` — 行程画布（核心模型）

```typescript
// 行程摘要
interface TripSummary {
  title: string;
  durationDays: number;
  pace: Pace;                        // "Light" | "Balanced" | "Relaxed" | "Packed"
  travelerStyle: string;
  destinations: string[];
  confidence: "Draft" | "Refined" | "Ready to save";
}

// 单日行程
interface TripDay {
  day: number;
  city: string;
  pace: Pace;
  blocks: TripBlock[];               // 时段块（早/中/晚/灵活）
  food: string[];
  stay: string;
  transport: string;
  note: string;
  status?: "new" | "revised" | "needs-confirmation";
}

// 时段块
interface TripBlock {
  time: "Morning" | "Afternoon" | "Evening" | "Flexible";
  title: string;
  description: string;
  highlights?: string[];
  photoUrl?: string;                 // 真实 POI 照片，不得伪造
  address?: string;
  chineseAddress?: string;
  phone?: string;
  openingHours?: string;
  mapUrl?: string;
  bookingUrl?: string;
  bookingCandidates?: BookingCandidate[];
  sourceLabel?: string;
  coordinates?: { lat: number; lng: number };
}

// 预订候选
interface BookingCandidate {
  id: string;
  kind: "hotel" | "ticket" | "transport" | "restaurant";
  label: string;
  provider: string;
  status: "info-only" | "planned";
  note: string;
  url?: string;
  priceHint?: string;
}

// 完整行程状态
interface TripState {
  summary: TripSummary;
  days: TripDay[];
  alerts: ButlerAlert[];
  lastUpdatedReason: string;
}

// AI→画布 变更补丁（核心接口）
interface CanvasPatch {
  intent: "create_trip" | "adjust_trip" | "add_alerts";
  assistantMessage: string;
  assistantResponse?: AssistantResponse;
  tripSummary?: Partial<TripSummary>;
  days?: TripDay[];
  butlerAlerts?: ButlerAlert[];
  reason: string;
}

// AI 回复格式化
interface AssistantResponse {
  headline: string;
  body: string;
  highlights: string[];
  watchOut?: string;
  nextStep: string;
  toolCards?: InlineToolCard[];
}

// AI 内联工具卡片
interface InlineToolCard {
  id: string;
  categoryId: string;
  title: string;
  summary: string;
  items: string[];
  nextAction: string;
  href?: string;
  tone?: "info" | "warning" | "success";
  sourceLabel?: string;
}

// 对话消息
interface ChatMessage {
  id: string;
  role: "user" | "assistant";
  content: string;
  response?: AssistantResponse;
  changeDigest?: ChangeDigestEntry[];
  createdAt?: string;
}

// 变更摘要条目
interface ChangeDigestEntry {
  kind: "added" | "revised" | "removed" | "alert";
  dayNumber?: number;
  label: string;
}

// Butler 提醒/告警
interface ButlerAlert {
  type: AlertType;              // "visa" | "payment" | "booking" | "transport" | "weather" | "language" | "risk" | "emergency"
  priority: "high" | "medium" | "low";
  title: string;
  body: string;
  action: string;
  done?: boolean;
}
```

---

### 4.2 `preferenceProfile.ts` — 用户偏好画像

```typescript
interface UserPreferenceProfile {
  pace?: "light" | "balanced" | "packed";
  budget?: "economy" | "mid" | "luxury";
  party?: "solo" | "couple" | "family_with_kids" | "group";
  dietaryRestrictions: string[];
  cuisinePreferences: string[];
  interests: string[];
  profileConfidence: "low" | "medium" | "high";
}
```

**提取方式：** 本地 keyword/regex 匹配，不调用 LLM。位于 `lib/ai/preferenceProfile.ts`。

---

### 4.3 `explore/types.ts` — 探索数据结构

```typescript
interface ExploreCity { id: string; name: string; chineseName: string; ... }
interface ExploreAttraction extends ExploreRichMeta { ... }
interface ExploreFoodSpot extends ExploreRichMeta { ... }
interface ExploreStay extends ExploreRichMeta { ... }
interface ExploreProvider {
  getCity(cityId: string): Promise<ExploreCity | null>;
  getAttractions(cityId: string): Promise<ExploreAttraction[]>;
  getFoodSpots(cityId: string): Promise<ExploreFoodSpot[]>;
  getStays(cityId: string): Promise<ExploreStay[]>;
  getProviderStatus(): ExploreProviderStatus;
}
```

---

### 4.4 `tools/types.ts` — 工具数据结构

```typescript
interface ToolCategory {
  id: string;
  name: string;
  icon: string;
  description: string;
  tips: string[];
  sections: { title: string; items: { label: string; detail: string }[] }[];
  offlineTips: { title: string; body: string }[];
  apiPriority: string;
}
interface ToolsProvider {
  getCategories(): Promise<ToolCategory[]>;
  getProviderStatus(): ToolsProviderStatus;
}
```

---

### 4.5 `community/types.ts` — 社区数据结构

```typescript
type CommunityPostType = "trip" | "photo" | "tip" | "question";
type MemberTierId = "bamboo-guest" | "panda-explorer" | "silk-road-insider" | "dragon-pass" | "visepanda-concierge";
interface CommunityPost { id: string; type: CommunityPostType; author: CommunityAuthor; title: string; body: string; ... }
interface CityHotSpot { id: string; cityId: string; name: string; category: "attraction" | "food" | "hidden"; rating: number; ... }
interface MemberTier { id: MemberTierId; name: string; benefits: string[]; ... }
```

---

### 4.6 `ai/` — AI Provider 抽象层

```typescript
interface ChatCompletionProvider {
  createChatCompletion(options: ChatCompletionOptions): Promise<ChatCompletionResult>;
}
type ButlerIntent = "plan_trip" | "adjust_trip" | "ask_factual" | "preference_signal" | "general_chat" | "unknown";
interface ButlerToolContext { source: "amap"; cityId: string; category: "attractions" | "food" | "stays"; pois: ButlerToolPoi[]; }
```

---

## 5. Supabase 数据库 Schema

### 5.1 表结构

**`users`**
| 字段 | 类型 | 约束 |
|------|------|------|
| id | uuid | PK, references auth.users |
| email | text | UNIQUE NOT NULL |
| display_name | text | nullable |
| created_at | timestamptz | DEFAULT now() |

**`trips`**
| 字段 | 类型 | 约束 |
|------|------|------|
| id | uuid | PK, DEFAULT gen_random_uuid() |
| owner_id | uuid | FK → users.id, NOT NULL |
| title | text | NOT NULL |
| status | text | CHECK('draft','ready','shared','archived'), DEFAULT 'draft' |
| share_token | text | UNIQUE, nullable |
| current_canvas_version_id | uuid | FK → canvas_versions.id, nullable |
| created_at | timestamptz | DEFAULT now() |
| updated_at | timestamptz | DEFAULT now() |

**`canvas_versions`**
| 字段 | 类型 | 约束 |
|------|------|------|
| id | uuid | PK, DEFAULT gen_random_uuid() |
| trip_id | uuid | FK → trips.id, NOT NULL |
| canvas | jsonb | NOT NULL (TripState) |
| last_updated_reason | text | DEFAULT '' |
| created_at | timestamptz | DEFAULT now() |

**`messages`**
| 字段 | 类型 | 约束 |
|------|------|------|
| id | uuid | PK, DEFAULT gen_random_uuid() |
| trip_id | uuid | FK → trips.id, NOT NULL |
| role | text | CHECK('user','assistant'), NOT NULL |
| content | text | NOT NULL |
| created_at | timestamptz | DEFAULT now() |

### 5.2 RLS 策略

所有表启用 Row Level Security，按用户隔离。读写须通过 `lib/supabase/tripsRepository.ts`，不得直接在组件中拼查询。

### 5.3 降级规则

未配置环境变量、未登录、网络失败时 → **优雅回退 guest/mock 体验**，不崩溃。参考 `lib/supabase/client.ts` 的 `isSupabaseConfigured` 模式。

---

## 6. 架构约束

1. 所有翻译相关外部调用必须经过服务端代理路由 `/api/translate/*`，**禁止从客户端直接传 API Key**
2. 所有 AI 输出必须通过 `CanvasPatch` 结构进入画布，**禁止 UI 直接解析自然语言**
3. 所有 Supabase 读写必须经过 `lib/supabase/tripsRepository.ts`
4. Explore 数据必须只通过 `lib/explore/index.ts` 的 `getExploreProvider()` 获取
5. Tools 数据必须只通过 `lib/tools/index.ts` 的 `getToolsProvider()` 获取
6. 所有 provider 必须实现 `getProviderStatus()` 用于页面展示
7. 真实 AI 接入后也**必须保留 mock/static fallback**
8. 所有未列在此文档中的接口，需 Claude Code 审批后方可添加

---

*初始版本生成于 2026-07-04，基于 AGENTS.md v0.3.x + 代码库现有路由提取。*
*后续由 Claude Code 维护更新。*
