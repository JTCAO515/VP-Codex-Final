# VisePanda Android App — 产品开发设计文档

> **版本:** v1.0 | **日期:** 2026-06-17
> **作者:** 资生产品经理
> **用途:** 完整的产品开发设计说明，交付给开发团队从零构建
> **框架参考:** AI+ Coding SOP 指南（6 阶段标准流程）

---

## 目录

1. [产品定义与战略](#1-产品定义与战略)
2. [用户与市场分析](#2-用户与市场分析)
3. [产品需求规格 (PRD)](#3-产品需求规格-prd)
4. [设计方向与体验指南](#4-设计方向与体验指南)
5. [技术架构指引](#5-技术架构指引)
6. [迭代路线图](#6-迭代路线图)
7. [测试策略](#7-测试策略)
8. [交付标准与工程归档](#8-交付标准与工程归档)
9. [附录：关键决策记录](#9-附录关键决策记录)

---

## 1. 产品定义与战略

### 1.1 一句话定义

**VisePanda 是一个面向来华国际旅行者的 AI 中国旅行顾问 App。**

不是"又一个旅行工具"——而是一个有品牌信任感的 AI 旅行伙伴。从灵感启发到行程落地，在一条产品线内完成。

### 1.2 核心价值主张

| 维度 | 描述 |
|------|------|
| **目标用户** | 计划或正在中国旅行的国际游客（英语母语优先，25-45岁） |
| **用户状态** | 对中国有兴趣但信息有限，被语言/文化/支付壁垒困扰 |
| **核心价值** | AI 一键生成个性化中国行程，城市选择→路线规划→实用工具全程覆盖 |
| **差异化** | 专精中国市场（非通用全球）+ 东方奢雅品牌调性（非工具堆砌） |
| **情感承诺** | **"一个懂中国的旅行伙伴"**——不是信息罗列，是规划能力的延伸 |

### 1.3 竞争格局

| 竞品 | 类型 | 优势 | 劣势 | VisePanda 机会 |
|------|------|------|------|---------------|
| TripAdvisor | 旅行社区 | 内容丰富，用户量大 | 通用型，中国数据弱；广告多 | 专精中国，无广告，品牌克制 |
| TripIt | 行程管理 | 行程组织强 | 无灵感/探索功能，UI 老旧 | AI 生成+灵感+管理一体化 |
| 小红书 | UGC 内容 | 中国内容最丰富 | 中文为主，非结构化 | 英文+结构化 AI 输出 |
| 携程国际版 | OTA | 预订能力强，中文数据好 | 工具感强，品牌感弱，设计过时 | 东方奢雅品牌化+AI 旅行顾问 |
| ChatGPT Travel | 通用 AI | 理解能力强，交互好 | 无中国专精数据，不能保存行程 | 中国垂直+行程资产化 |
| Wanderlog | AI 旅行 | 已有 AI 行程功能 | 全球通用，中国数据弱；设计平淡 | 中国垂直+东方品牌设计 |

### 1.4 战略取舍

| ✅ 要做 | ❌ 不做 |
|---------|---------|
| AI 驱动的中国旅行规划 | 全球旅行（保持专注） |
| 深色东方奢雅品牌设计 | 通用 Material 主题 |
| 英文原生内容 | 多语言（首版不覆盖） |
| 行程灵感到落地一体化 | 纯工具/信息罗列 |
| 轻量无广告体验 | 付费墙/订阅付费模式 |

### 1.5 核心能力与护城河

| 能力 | 自建/采购 | 说明 |
|------|-----------|------|
| AI 对话引擎 | 自建（SSE API） | 后端提供的流式对话，中国旅行专精语料 |
| 目的地数据结构化 | 自建 | 28+ 城市的结构化数据（Must-see/Must-eat/Stay/Tips） |
| 旅行工具内容 | 自建 | Visa/SIM/Payment/Etiquette 等场景化指南 |
| 地图可视化 | 开源（osmdroid） | 零 API Key，离线可用 |
| 品牌设计系统 | 自建 | 东方奢雅暗色主题，与竞品形成视觉壁垒 |

**护城河：** 中国旅行垂直数据的结构化积累 + 东方奢雅品牌认知。竞争对手可以复制"AI 行程生成"，但无法快速建立中国专精的目的地内容库和品牌信任感。

---

## 2. 用户与市场分析

### 2.1 目标用户画像

| 属性 | 典型用户 A（规划者） | 典型用户 B（探索者） |
|------|---------------------|---------------------|
| 年龄 | 28-40 | 25-35 |
| 旅行动机 | 年假深度游（7-14天） | 短途周末探索（3-5天） |
| 中国经验 | 首次/二次来华 | 在华常驻/出差间隙 |
| 痛点的核心 | "信息太多，不知道从哪开始" | "想发现新的东西，但攻略太同质化" |
| 产品使用场景 | 出发前 2-4 周，密集规划 | 临出发前/旅行中，即兴探索 |
| 最在乎 | 行程完整可执行 + 预算透明 | 个性化推荐 + 发现感 |

### 2.2 用户典型旅程（7 步）

```
发现        下载        首次打开       浏览         规划          管理        复访
┌────┐     ┌────┐     ┌──────┐     ┌──────┐    ┌──────┐    ┌──────┐   ┌──────┐
│App │     │ 安 │     │ 首 │        │ 刷 │      │ AI  │      │ 保 │    │ 打 │
│Store│ ──> │ 装 │ ──> │ 次 │ ──>    │ 城 │ ──>  │ 对话│ ──>  │ 存 │──> │ 开 │
│搜到 │     │ 打 │     │ 打 │        │ 市 │      │ 生 │      │ 旅 │    │ 看 │
│     │     │ 开 │     │ 开 │        │    │      │ 成 │      │ 程 │    │ 已 │
└────┘     └────┘     └──────┘     └──────┘    └──────┘    └──────┘   └──────┘
```

### 2.3 情感曲线设计

| 阶段 | 用户状态 | 设计目标 | 关键体验点 |
|------|---------|---------|-----------|
| 首次打开 | 好奇但陌生 | 建立信任，传达价值 | Hero 大图+金色标识+一句话"Your AI China Travel Companion" |
| 浏览城市 | 兴趣激发 | 美图吸引，轻操作 | 全宽卡片流，左右滑动浏览 |
| 进入城市详情 | 决策预备 | 说服 + 降低启动门槛 | 对称布局+CTA 显眼 |
| AI 对话 | 期待但犹豫 | 降低犹豫，给予安全感 | 预设问题卡片（点选即发） |
| 流式输出 | 验证预期 | 明确进度，建立可信 | token 逐字渲染 + 卡片渐入 |
| 保存行程 | 成就感 | 满足完成欲 | 保存成功动效 |
| 再次打开 | 信赖沉淀 | 减少步骤，直达价值 | 首页显示最近行程 |

---

## 3. 产品需求规格 (PRD)

### 3.1 功能全景（按优先级）

| 优先级 | 模块 | 功能 | 说明 |
|--------|------|------|------|
| **P0** | 首页 | Hero 品牌区 | 品牌标识 + 标语 + 主 CTA"Plan Your Trip" |
| **P0** | 首页 | 精选城市卡片 | LazyRow 展示 3-4 个推荐城市，点击跳转详情 |
| **P0** | 首页 | AI 规划入口 | "Chat with Panda"卡片入口 |
| **P0** | 首页 | Bottom Navigation | 4 Tab 导航（Home/Explore/Chat/Trips） |
| **P0** | Explore | 城市卡片网格 | 28+ 城市可滑动浏览，包含图片+城市名+标签 |
| **P0** | Explore | 地图视图 | osmdroid 加载，城市标记，点击弹出信息卡 |
| **P0** | Chat | AI 对话 | SSE 流式输出，纯文本+结构化行程+图片 |
| **P0** | Chat | 预设 Prompt | Welcome chips 引导对话 |
| **P0** | Chat | 自动保存行程 | AI 回复含行程信息时自动保存至本地 |
| **P0** | City Detail | 城市详情页 | 头图+信息分区（Must-see/Eat/Stay/Tips） |
| **P0** | City Detail | CTA 跳转 | "Plan my trip to {City}" 跳转 Chat 带城市上下文 |
| **P0** | Trips | 行程列表 | 保存的行程卡片列表，按时间倒序 |
| **P0** | Trips | 空态引导 | 无行程时显示引导插画+CTA |
| **P1** | Chat | Itinerary Block | 结构化的行程卡片渲染（金色边框+缩略图） |
| **P1** | Chat | Recommendation Card | 横向推荐卡片，带阴影层级 |
| **P1** | Chat | 图片内嵌 | AI 回复中的图片渲染 |
| **P1** | Home | 主题灵感区 | "First Time in China"/"Food Journey"/"Culture" |
| **P1** | Home | Tools 快捷入口 | 图标矩阵跳转工具页 |
| **P1** | Explore | 视图切换器 | 卡片流/地图视图切换动画 |
| **P1** | Explore | 搜索/筛选 | 按城市名/标签搜索 |
| **P2** | Tools | 支付与手机指南 | Payment & Mobile Guide |
| **P2** | Tools | 签证与入境 | Visa & Entry Info |
| **P2** | Tools | 网络通信 | SIM & Internet Guide |
| **P2** | Tools | 应急信息 | Emergency Contacts |
| **P2** | Tools | 礼仪指南 | Etiquette Guide |
| **P2** | Tools | 常用中文 | Useful Chinese Phrases |
| **P2** | Chat | FAQ 折叠 | AI 回复中 FAQ 区域可折叠 |
| **P2** | Trips | 删除行程 | 长按/滑动删除确认 |

### 3.2 非功能需求

| 类别 | 要求 | 验收标准 |
|------|------|---------|
| 启动速度 | Splash → 首页内容加载不超过 2s | 真机测试，WiFi 环境 |
| 离线韧性 | 断网时显示提示，不崩溃 | 手动断开网络验证 |
| 空态覆盖 | 所有列表页有空态设计 | 断网/空数据验证 |
| 错误处理 | 网络错误可重试 | 每个网络请求都有 retry 机制 |
| 内存 | 图片列表流畅滑动，不 OOM | Coil 图片加载+缓存 |
| 包体积 | APK ≤ 25MB | GitHub Actions release build |

### 3.3 页面树（完整版）

```
App
├── Splash Screen ──── 纯黑背景 + 金色熊猫标识，1.5s后淡入Home
│
├── Home ──────────── 品牌首页，灵感驱动
│   ├── Hero 区
│   │   ├── 金色品牌标识 + 标语 "Your AI China Travel Companion"
│   │   └── 主 CTA: "Plan Your Trip" → Chat（带预热prompt）
│   ├── 精选城市 (LazyRow, 3-4张)
│   │   └── 点击 → City Detail
│   ├── 主题灵感区（P1）
│   │   ├── "First Time in China"
│   │   ├── "Food Journey"
│   │   └── "Culture & Heritage"
│   ├── AI 规划入口卡
│   │   └── "Chat with Panda" → Chat
│   └── Tools 快捷入口（P1，图标矩阵）
│
├── Explore ───────── 城市浏览 + 地图
│   ├── 视图切换器（卡片流 / 地图）
│   ├── 城市卡片网格 (LazyVerticalGrid)
│   │   ├── 头图（Coil 加载）
│   │   ├── 城市名 + 一句话
│   │   ├── 标签：美食/文化/自然
│   │   └── 点击 → City Detail
│   ├── 地图视图 (osmdroid)
│   │   ├── 城市标记
│   │   └── 点击标记 → Info Window → City Detail
│   └── 搜索/筛选（P1）
│
├── Chat ──────────── AI 旅行规划
│   ├── 顶部上下文栏
│   ├── Prompt Suggestion Chips
│   │   ├── "Plan my 3-day trip to Beijing"
│   │   ├── "Best cities for food lovers"
│   │   └── "What to prepare before traveling to China?"
│   ├── 消息流（反向列表）
│   │   ├── 用户消息：右对齐，#2A2A2A 卡片
│   │   ├── AI 消息：左对齐，#1A1A1A 背景
│   │   ├── Token 逐字渲染（打字机效果）
│   │   ├── Itinerary Block（P1，金色左边框+卡片布局）
│   │   ├── Recommendation Card（P1，横向 LazyRow）
│   │   ├── Image（P1，内嵌图片）
│   │   └── FAQ（P2，可折叠）
│   └── 底部输入栏
│       ├── 输入框 "Ask Panda..."
│       └── 发送按钮（金色）
│
├── Trips ─────────── 我的旅行资产
│   ├── 最近生成（按时间倒序）
│   ├── 已保存的行程卡片
│   │   └── 标题 + 城市 + 天数 + 预览摘要
│   ├── 空状态引导插画
│   │   └── "Start planning your China adventure" + CTA
│   └── 长按删除（P2，红色确认）
│
├── City Detail ───── 目的地叙事页
│   ├── 全宽头图（24dp 底圆角）
│   ├── 城市名 + 一句话气质描述
│   ├── 统计数据：推荐停留天数 + 预算范围
│   ├── Must-see / Must-eat / Stay / Tips 分区
│   └── CTA: "Plan my trip to {City}" → Chat（带城市上下文）
│
└── Tools ─────────── 旅行帮助中心（P2，二级页）
    └── 分类列表：Payment / Visa / SIM / Emergency / Etiquette / Phrases
```

### 3.4 核心交互流程

#### 主路径：从灵感到行程

```
首页点"Plan Your Trip"
  → Chat 页面打开
  → 显示 Welcome Chips（预设问题）
  → 用户点选或输入
  → SSE 流式输出行程规划
  → 行程自动保存到 Trips
  → 用户可在 Trips 查看
```

#### 城市驱动路径

```
Explore 浏览城市
  → 点击城市卡片 → City Detail
  → 点"Plan my trip to {City}"
  → Chat 自动带城市上下文
  → AI 输出该城市的定制行程
```

---

## 4. 设计方向与体验指南

### 4.1 设计语言：东方奢雅（Oriental Luxury）

**核心定位：** 一个严肃、克制、有质感的高端旅行品牌。不讨好所有人，只服务认可审美的那群人。

**灵感参考：**
- 柏悦酒店 / 安缦（深色暖金、克制东方元素、大留白）
- Linear（暗色系统、层次化表面、透明白边分割）
- Apple（留白、敬畏感、图片驱动的展示逻辑）

### 4.2 三要素配置

| 要素 | 值 | 说明 |
|------|-----|------|
| VISUAL_DENSITY | 3/10 | 留白优先，每屏不超过 5 个视觉焦点。宁可空，不可乱 |
| DESIGN_VARIANCE | 7/10 | 非对称布局，图片驱动，节奏变化，避免均分/对称模板 |
| MOTION_INTENSITY | 5/10 | 流畅微动效，入口断点，滚动渐入（scroll-reveal） |

### 4.3 设计原则（不可违反）

1. **留白大于填充** — 每屏元素不超过 5 个视觉焦点。宁可空，不可乱。
2. **暗色即空间** — 深色不是"夜间模式"，是内容的原生画布，内容从暗处浮现。
3. **金色不泛滥** — 主金色仅用于品牌触点（logo/CTA/高亮标签），不作为大面积填充色。
4. **图片驱动** — 每个区段必须有真实图片（城市风景、食物、文化场景），没有纯色块占位。
5. **维度层次** — 背景 → 表面 → 卡片 → 浮动，4 层深度明确。
6. **禁止 AI 标配设计** — 无紫色渐变、无霓虹发光、无三卡等分、无 Acme 假名。
7. **一致性锁定** — 一个调色板贯穿全 App，圆角系统全局统一，字体权重不混用。

### 4.4 色板系统

```
底色 (Surface)
  #0A0A0A        → 最深底（Hero/大区背景）
  #1A1A1A        → 标准表面（页面背景）
  #232323        → 卡片/容器（略微提升）
  #2A2A2A        → 交互态（hover/选中/用户消息）

主金色 (Accent)
  #C9A96E        → 主金色（品牌点缀/CTA/高亮）
  #B89255        → 金色暗态（depressed/disabled）
  #DCC798        → 金色亮态（hover/active glow）

辅助色 (Secondary)
  #5B7B5A        → 竹青（自然/成功/健康相关的强调）
  #8B8B7A        → 玉石灰（辅助文本/次级界面元素）
  #6B6B5E        → 更深的玉石灰（metatext/次级操作）

中性色 (Neutral)
  #F5F0E8        → 米白（浅色文字/大字号标题）
  #D4CEC4        → 暖灰（正文/次要文字）
  #9C9A94        → 暗灰色（de-emphasized/placeholder）
  #3A3A3A        → 分割线（薄而克制）
```

### 4.5 字体系统

| Role | Size | Weight | Line Height | Tracking | Use |
|------|------|--------|-------------|----------|-----|
| Display XL | 36sp | SemiBold | 1.1 | -0.5sp | Hero 大标题 |
| Display Large | 28sp | SemiBold | 1.2 | -0.3sp | 区段大标题 |
| Headline | 22sp | Medium | 1.3 | -0.2sp | 卡片标题 |
| Subhead | 18sp | Medium | 1.4 | 0 | 列表标题 |
| Body | 16sp | Regular | 1.6 | 0 | 正文字体 |
| Body Small | 14sp | Regular | 1.5 | 0 | 辅助说明 |
| Caption | 12sp | Medium | 1.4 | 0.2sp | 标签/角标 |
| Tab Label | 11sp | Medium | 1.2 | 0.3sp | 底部导航标签 |

### 4.6 间距与圆角

**间距刻度：**

| Token | dp | Use |
|-------|-----|-----|
| xs | 4 | 微间距 |
| sm | 8 | 内容间距 |
| md | 12 | 组件内间距 |
| lg | 16 | 组件间间距 |
| xl | 24 | 区段间距 |
| xxl | 32 | 大区段间距 |
| section | 48 | 页面节间距 |

**圆角系统：**

| Token | Value | Use |
|-------|-------|-----|
| xs | 4dp | 标签/小元素 |
| sm | 8dp | 按钮/输入框 |
| md | 12dp | 卡片标准 |
| lg | 16dp | 大卡片/弹窗 |
| xl | 24dp | Hero/头图 |

**阴影层级：**

| Level | Value | Use |
|-------|-------|-----|
| 0 | (无阴影) | 页面背景 |
| 1 | `0 2dp 4dp rgba(0,0,0,0.3)` | 轻微卡片 |
| 2 | `0 4dp 12dp rgba(0,0,0,0.4)` | 浮起卡片 |
| 3 | `0 8dp 24dp rgba(0,0,0,0.5)` | 弹窗/BottomSheet |
| 4 | `0 12dp 36dp rgba(0,0,0,0.6)` | 全屏Modal |

### 4.7 关键交互动效规范

| 交互 | 动效 | 参数 |
|------|------|------|
| Tab 切换 | cross-fade + 交错渐入 | 200ms |
| Tab 选中 | 图标颜色变化 + 轻微上浮 | 2dp up, 100ms |
| AI Token 渲染 | 打字机逐字 | 30ms/字 |
| 城市 Detail 进入 | 图片放大 + 信息覆盖层上推 | shared element, 300ms |
| 保存行程 | 底部滑入确认 Toast | 250ms ease-out |
| 空态入场 | 插画渐入 + CTA 延迟出现 | 500ms stagger |
| 页面加载 | Shimmer 骨架屏（金色脉冲） | 1.5s loop |

---

## 5. 技术架构指引

### 5.1 架构概览

```
┌──────────────────────────────────────────┐
│  UI Layer (Jetpack Compose)               │
│  Home / Explore / Chat / Trips / Detail   │
│  └─ 只消费 UiState，不直接操作网络/DB     │
├──────────────────────────────────────────┤
│  ViewModel Layer                          │
│  HomeViewModel / ExploreViewModel / ...   │
│  └─ StateFlow<UiState<T>> 驱动 UI        │
├──────────────────────────────────────────┤
│  Domain Layer (UseCases) [可选]           │
│  GetFeaturedDestinationsUseCase           │
│  SendChatMessageUseCase                   │
│  SaveTripUseCase                          │
├──────────────────────────────────────────┤
│  Data Layer (Repository)                  │
│  DestinationRepository                    │
│  ChatRepository (SSE)                     │
│  TripRepository (DataStore)               │
│  ToolsRepository                          │
├──────────────────────────────────────────┤
│  Network Layer (Core)                     │
│  OkHttp Client + SSE Client               │
│  Retrofit (可选) / 手写 OkHttp            │
└──────────────────────────────────────────┘
```

### 5.2 技术选型建议

| 决策 | 建议选择 | 理由 |
|------|---------|------|
| UI 框架 | Jetpack Compose + Material3 | 现代声明式，与设计系统 token 天然对接 |
| 导航 | Navigation Compose | 官方方案，类型安全，支持 deep link |
| 网络 | OkHttp + SSE Client | 轻量、可控、Chat SSE 是核心功能 |
| 地图 | osmdroid | 零 API Key，离线可用，无需 Google Play |
| 本地存储 | DataStore Preferences + Proto | 协程原生，轻量 |
| 图片加载 | Coil 3 | Compose 原生，轻量，支持 Shimmer |
| 编译 | GitHub Actions | 无需本地环境，自动签名 APK |
| 设计系统 | Compose 自定义 Theme | 品牌定制的 Color/Typography/Shape/Spacing tokens |

### 5.3 API 接口规范

App 通过 RESTful API + SSE 与后端通信。以下是关键接口定义：

#### 核心接口

```
GET  /api/destinations          → 获取所有目的地城市列表
GET  /api/destinations/{id}     → 获取单个城市详情
POST /api/chat/message          → 发送聊天消息，返回 SSE stream
GET  /api/trips                 → 获取用户已保存的行程
POST /api/trips                 → 保存行程
DELETE /api/trips/{id}          → 删除行程
```

#### 关键数据结构

**Destination:**
```json
{
  "id": "beijing",
  "name": "Beijing",
  "name_cn": "北京",
  "description": "China's ancient capital...",
  "image_url": "https://...",
  "tags": ["history", "culture", "food"],
  "must_see": ["Forbidden City", "Great Wall", ...],
  "must_eat": ["Peking Duck", ...],
  "stay_tips": "Best areas: ...",
  "best_days": 4,
  "budget_range": "$$-$$$"
}
```

**Chat SSE Event:**
```
event: message
data: {"type": "token", "content": "北京"}
data: {"type": "token", "content": "的建议行程如下"}
data: {"type": "image", "url": "https://...", "alt": "故宫"}
data: {"type": "itinerary", "content": {"days": [...]}}
data: {"type": "faq", "content": [{"q": "...", "a": "..."}]}
data: {"type": "done"}
```

### 5.4 统一 UiState 设计

```kotlin
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data object Empty : UiState<Nothing>()
    data class Error(val message: String, val retry: (() -> Unit)? = null) : UiState<Nothing>()
}
```

### 5.5 加载/空/错态处理规范

| 状态 | 视觉处理 | 动效 | 行为 |
|------|---------|------|------|
| Loading | Shimmer 骨架屏（暗色适配） | 金色脉冲 shimmer | 自动 fetch |
| Empty | 插画+引导文案+CTA | 入场渐入 | 点 CTA 跳转 |
| Error | 适中图标+错误原因+重试按钮 | 微颤动效 | 点重试重新加载 |
| 网络差 | Snackbar toast（不打断流程） | 底部滑入 | 自动重试×3后显示断网提示 |

---

## 6. 迭代路线图

### 6.1 阶段总览

```
Phase 0: 工程骨架 + 设计系统 ────  1天  ⬅ 起点
Phase 1: 顶层导航 + 页面容器 ────  1天
Phase 2: Home + Explore ──────────  2天
Phase 3: City Detail ─────────────  1天
Phase 4: Chat 主线 ───────────────  2天
Phase 5: Trips + Tools ───────────  1天
Phase 6: 收尾验证 + APK ──────────  1天
```

**总工期估计：** 9 天（单人全栈，若多人并行可缩短）

### 6.2 迭代详情

#### Phase 0：工程骨架 + 设计系统

**目标：** 搭建可编译的 Android 工程，建立品牌设计系统

**交付物：**
- [ ] Android 工程模块化结构（core:designsystem, core:network, core:common, app）
- [ ] Gradle 配置（AGP 8.2+, Kotlin 1.9+, Compose BOM）
- [ ] Design System 模块
  - `Color.kt`（8 色+语义化命名）
  - `Typography.kt`（8 级字形）
  - `Spacing.kt`（间距刻度）
  - `Shape.kt`（圆角系统）
  - 核心组件：VpButton / VpChip / VpCard / VpBottomNav
  - Shimmer 骨架屏
- [ ] 网络模块（OkHttp + SSE Client，Mock/Real 双通道）
- [ ] GitHub Actions assembleRelease

**验证门禁：**
- `./gradlew assembleDebug` 编译通过 ✅
- 截图展示 Design System 组件渲染效果

---

#### Phase 1：顶层导航 + 页面容器

**目标：** 完成 Bottom Navigation + 5 个 Tab 的页面容器骨架

**交付物：**
- [ ] Bottom Navigation（Home/Explore/Chat/Trips）
- [ ] 5 个 Tab 的页面容器 Compose（骨架屏展示）
- [ ] Navigation Compose 路由定义
- [ ] Tab 切换动画（cross-fade）

**验证门禁：**
- 4 tab 切换流畅 ✅
- 每个 tab 显示对应的骨架屏/空态 ✅

---

#### Phase 2：Home + Explore

**目标：** 首页和探索页内容填充，接入真实数据

**交付物：**
- [ ] Home 页：Hero 区 + 精选城市（LazyRow）+ AI 入口卡
  - 头图/品牌标识/标语
  - "Plan Your Trip" CTA
- [ ] Explore 页：城市卡片网格（LazyVerticalGrid）
  - 图片加载（Coil）+ 标签 + 点击跳转
- [ ] Explore 页：地图视图（osmdroid）
  - 城市标记 + 点击弹 Info Window
- [ ] DestinationRepository 接入真实 API
- [ ] 空态和错误处理

**验证门禁：**
- 首页加载显示真实城市数据 ✅
- Explore 卡片/地图切换正常 ✅
- 断网显示错误态+重试按钮 ✅

---

#### Phase 3：City Detail

**目标：** 城市详情页完整实现

**交付物：**
- [ ] 全宽头图（24dp 底圆角）
- [ ] 城市名 + 描述 + 统计数据
- [ ] Must-see / Must-eat / Stay / Tips 分区
- [ ] "Plan my trip to {City}" CTA → Chat（带城市上下文）
- [ ] 进入动效（shared element transition）

**验证门禁：**
- 城市详情页展示完整信息 ✅
- CTA 跳转 Chat 带正确城市上下文 ✅

---

#### Phase 4：Chat 主线

**目标：** AI 对话核心体验完整可用

**交付物：**
- [ ] ChatRepository SSE 接入
- [ ] 消息列表（用户右对齐/AI 左对齐）
- [ ] Token 逐字渲染（30ms/字）
- [ ] Prompt Suggestion Chips
- [ ] Itinerary Block 渲染（P1）
- [ ] Recommendation Card 渲染（P1）
- [ ] 图片内嵌（P1）
- [ ] 消息自动保存到 Trips
- [ ] 底部输入框 + 发送按钮

**验证门禁：**
- 输入→流式输出→完整消息 ✅
- Itinerary Block 格式正确 ✅
- 行程自动保存到 Trips ✅

---

#### Phase 5：Trips + Tools

**目标：** 行程管理和工具页

**交付物：**
- [ ] Trips 列表（时间倒序）
- [ ] 行程卡片：标题+城市+天数+预览
- [ ] 空态引导插画+CTA
- [ ] 删除功能（P2）
- [ ] Tools 页面列表（P2）
- [ ] 6 个工具内容页（P2）

**验证门禁：**
- 重启 App 后行程数据存在 ✅
- Trips 空态引导路径正确 ✅

---

#### Phase 6：收尾验证 + APK

**目标：** 全路径验收 + 发布

**交付物：**
- [ ] 全路径 E2E 测试
- [ ] 断网/空数据/错误态全覆盖验证
- [ ] Splash 屏
- [ ] GitHub Actions release build
- [ ] APK 签名 + 分发

**验证门禁：**
- 核心路径（Home → Explore → Chat → Trips）走通 ✅
- APK 可安装、可运行 ✅
- 设计一致性检查通过 ✅

### 6.3 P2 功能（迭代后版本）

以下功能在首版迭代完成后开发：

| 功能 | 预计投入 | 说明 |
|------|---------|------|
| 搜索/筛选城市 | 0.5天 | Explore 页添加搜索框 |
| FAQ 折叠渲染 | 0.5天 | Chat 中 FAQ 区域可折叠 |
| 主题灵感区 | 0.5天 | Home 页增加主题路线流转 |
| Tools 完整模块 | 1天 | 6 个工具内容页 |
| 删除行程确认 | 0.5天 | 长按/滑动删除 |

---

## 7. 测试策略

### 7.1 测试层次

| 层次 | 范围 | 工具 | 负责人 |
|------|------|------|--------|
| 单元测试 | ViewModel / Repository / UseCase | JUnit + MockK | 开发者 |
| 组件测试 | UI 组件渲染 | Compose Test | 开发者 |
| 集成测试 | ViewModel + Repository 交互 | JUnit + MockWebServer | 开发者 |
| E2E 测试 | 核心用户路径 | 手动测试 | PM/QA |
| 兼容性测试 | Android 12+ | 真机/模拟器 | PM |

### 7.2 关键测试场景

**核心路径（每次提交前必须通过）：**

| # | 场景 | 步骤 | 预期 |
|---|------|------|------|
| 1 | 首次启动 | 打开 App → 等待加载 | Splash → Home 骨架屏 → 城市数据加载完成 |
| 2 | 浏览城市 | Home 滑动城市卡片 → 点击 | 跳转 City Detail，显示完整信息 |
| 3 | 地图探索 | 切换到地图视图 | 城市标记显示，点击弹 Info Window |
| 4 | AI 对话 | 点"Plan Your Trip"→ 输入"3 days in Beijing" | SSE 流式输出，完整消息渲染 |
| 5 | 行程保存 | AI 回复含行程 → 查看 Trips | 行程自动出现，信息完整 |
| 6 | 断网韧性 | 打开 App → 关闭网络 | 显示断网提示，不崩溃 |
| 7 | 空数据 | 卸载重装 → 看 Trips | 空态引导插画 + CTA |

### 7.3 人工验收清单

Phase 结束时对照以下清单：

- [ ] 编译（assembleRelease）无错误
- [ ] 设计与 token 规范一致（截图对比）
- [ ] 所有列表页有空态覆盖
- [ ] 所有网络请求有错误重试
- [ ] 流式 Chat 在弱网下不卡死
- [ ] APK 可正常安装运行

---

## 8. 交付标准与工程归档

### 8.1 交付物清单

| 交付物 | 说明 | 责任人 |
|--------|------|--------|
| 源码仓库 | GitHub 仓库，含完整 Git 历史 | 开发者 |
| APK 文件 | 签名后的 release APK | 开发者 |
| 设计系统代码 | Color/Typography/Spacing/Shape tokens | 开发者 |
| README.md | 项目说明（功能/技术栈/快速开始） | 开发者 |
| PLAN.md | 迭代记录（已完成/进行中/待完成） | 开发者 |
| HANDOFF.md | 项目交接文档 | 开发者 |
| 技术设计文档 | 架构图/API 接口/数据流 | 开发者 |

### 8.2 编码规范

**通用要求：**
- 中文注释优先，变量/函数名用英文
- 所有公共函数和接口必须有类型注解/KDoc 注释
- 每个业务模块附带 README 说明
- 每个版本有使用说明（功能说明、运行说明）

**Kotlin 规范：**
- 强制类型注解
- 遵循 Kotlin coding conventions
- Compose 组件名以 `Vp` 前缀（如 `VpButton`）
- UiState 使用 sealed class 模式

### 8.3 代码审查清单

| 检查项 | 标准 |
|--------|------|
| 设计 token | 全 App 使用设计系统 token，无硬编码色值/间距 |
| 空态/错态 | 每个列表/网络请求的 UI 覆盖 Loading/Empty/Error |
| 网络异常 | OkHttp interceptor 统一处理超时/401/500 |
| 内存 | 图片 Coil disk cache + 列表 RecyclerView 复用 |
| 导航 | Navigation Compose 类型安全，无 hardcoded route string |
| 架构 | ViewModel 不持有 View reference，StateFlow 驱动 UI |
| 构建 | GitHub Actions 可复制、可 fork、零手动步骤 |

### 8.4 版本号约定

| 版本 | 含义 |
|------|------|
| v0.1.x | Phase 0-1：骨架+导航完成 |
| v0.2.x | Phase 2-3：Home/Explore/CityDetail 完成 |
| v0.3.x | Phase 4：Chat 主线完成 |
| v0.4.x | Phase 5-6：Trips/Tools + 收尾 |
| v1.0.0 | 正式发布版 |

---

## 9. 附录：关键决策记录

### ADR-001：English-native 前端

**决定：** 所有前端界面使用英文。

**理由：**
- 目标用户是来华国际旅行者，英语是他们的通用语言
- 保持品牌一致性和用户心智模型
- 不自动检测语言，不提供 i18n 切换

### ADR-002：osmdroid 替代 Google Maps

**决定：** 使用 osmdroid 作为地图组件。

**理由：**
- 零 API Key 依赖，无成本
- 无需 Google Play Services，覆盖更多设备
- 中国地区地图数据可用
- 离线缓存支持

### ADR-003：SSE 替代 WebSocket

**决定：** 聊天使用 Server-Sent Events。

**理由：**
- 单向流（AI → 用户），SSE 天然适合
- 实现简单，OkHttp 原生支持
- 无连接池/心跳等 WebSocket 复杂度

### ADR-004：DataStore 替代 Room

**决定：** 行程数据使用 DataStore + JSON 序列化存储。

**理由：**
- 行程数据结构简单，无复杂查询
- DataStore 协程原生，减少依赖
- 数据量小（典型用户 < 50 条行程）
- 如果后续需要复杂查询，可升级到 Room

### ADR-005：暗色唯一主题

**决定：** 仅支持暗色模式，不做亮色/暗色切换。

**理由：**
- 品牌定位（东方奢雅）天然适合暗色
- 减少 50% 的 UI 复杂度（不需要维护两套色板）
- 竞品（TripIt/Wanderlog）以亮色为主，暗色形成差异化

---

*本文档是 AI+ Coding SOP 指南的第 1-3 阶段（需求编写 + 产品/UI设计 + 技术设计）的完整输出，可作为第 4 阶段（代码编写）的输入依据。*

*开发者开始编码前，建议按 SOP 第 2 步（产品/UI设计）先产出 UI 效果图或 Figma 原型，与 PM 确认后再进入编码阶段。*
