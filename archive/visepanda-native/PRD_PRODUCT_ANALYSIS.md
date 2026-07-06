# VisePanda Native — 产品规划文档 (v2.0)

> 版本: v2.0 | 日期: 2026-06-17
> 作者: 资生产品经理
> 设计参考: taste-skill / popular-web-designs(Linear/Apple) / figma-generate-library

---

## 一、产品定位

### 一句话

**VisePanda 是一个面向来华国际旅行者的 AI 中国旅行顾问 App。**

不是"又一个旅行工具"，而是一个有品牌信任感的 AI 旅行伙伴。从灵感启发到行程落地，在一条产品线内完成。

### 核心价值主张

| 维度 | 描述 |
|------|------|
| 目标用户 | 计划或正在中国旅行的国际游客（英语母语优先，25-45岁） |
| 用户状态 | 对中国有兴趣但信息有限，被语言/文化/支付壁垒困扰 |
| 核心价值 | AI 一键生成个性化中国行程，城市选择→路线规划→实用工具全程覆盖 |
| 差异化 | 专精中国市场（非通用全球）+ 东方奢雅品牌调性（非工具堆砌） |
| 情感承诺 | "一个懂中国的旅行伙伴"——不是信息罗列，是规划能力的延伸 |

---

## 二、设计愿景 (核心)

### 2.1 设计三要素

```
VISUAL_DENSITY:  3  (留白优先，画廊呼吸感)
DESIGN_VARIANCE: 7  (非对称布局，图片驱动，节奏变化)
MOTION_INTENSITY: 5  (流畅微动效，入口断点，scroll-reveal)
```

> 参照 taste-skill 三盘配置：低密度 + 中高变化 + 中等动效
> 对标参考：Linear (暗色系统/层次感) + Apple (留白/质感/敬畏感)

### 2.2 东方奢雅——视觉语言规范

#### 色板系统

```
底色 (Surface)
  #0A0A0A        → 最深底（Hero/大区背景）
  #1A1A1A        → 标准表面（页面背景）
  #232323        → 卡片/容器（略微提升）
  #2A2A2A        → 交互态（hover/选中）

点缀色 (Accent)
  C9A96E         → 主金色（品牌点缀/CTA/高亮）
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

#### 字体系统

| Role | Size | Weight | Line Height | Tracking | Notes |
|------|------|--------|-------------|----------|-------|
| Display XL | 36sp | SemiBold | 1.1 | -0.5sp | Hero 大标题 |
| Display Large | 28sp | SemiBold | 1.2 | -0.3sp | 区段大标题 |
| Headline | 22sp | Medium | 1.3 | -0.2sp | 卡片标题 |
| Subhead | 18sp | Medium | 1.4 | 0 | 列表标题 |
| Body | 16sp | Regular | 1.6 | 0 | 正文字体 |
| Body Small | 14sp | Regular | 1.5 | 0 | 辅助说明 |
| Caption | 12sp | Medium | 1.4 | 0.2sp | 标签/角标 |
| Tab Label | 11sp | Medium | 1.2 | 0.3sp | 底部导航标签 |

#### 间距刻度

| Token | dp | Use |
|-------|-----|-----|
| xs | 4 | 微间距 |
| sm | 8 | 内容间距 |
| md | 12 | 组件内间距 |
| lg | 16 | 组件间间距 |
| xl | 24 | 区段间距 |
| xxl | 32 | 大区段间距 |
| section | 48 | 页面节间距 |

#### 圆角系统

| Token | Value | Use |
|-------|-------|-----|
| xs | 4dp | 标签/小元素 |
| sm | 8dp | 按钮/输入框 |
| md | 12dp | 卡片标准 |
| lg | 16dp | 大卡片/弹窗 |
| xl | 24dp | Hero/头图 |

#### 阴影层级

| Level | Value | Use |
|-------|-------|-----|
| 0 | (无阴影) | 页面背景 |
| 1 | `0 2dp 4dp rgba(0,0,0,0.3)` | 轻微卡片 |
| 2 | `0 4dp 12dp rgba(0,0,0,0.4)` | 浮起卡片 |
| 3 | `0 8dp 24dp rgba(0,0,0,0.5)` | 弹窗/BottomSheet |
| 4 | `0 12dp 36dp rgba(0,0,0,0.6)` | 全屏Modal |

### 2.3 设计原则（恒生效）

1. **留白大于填充** — 每屏元素不超过 5 个视觉焦点。宁可空，不可乱。
2. **暗色即空间** — 深色不是"夜间模式"，是内容的原生画布，内容从暗处浮现。
3. **金色不泛滥** — 主金色仅用于品牌触点（logo/CTA/高亮标签），不作为大面积填充色。
4. **图片驱动** — 每个区段必须有真实图片（城市风景、食物、文化场景），没有纯色块占位。
5. **竹青做呼吸** — 竹青只用在自然/成功/安全的状态提示中，维持绿色系在暗色下的温度感。
6. **维度层次** — 背景(#0A0A0A) → 表面(#1A1A1A) → 卡片(#232323) → 浮动(#2A2A2A)，4层深度。
7. **禁止 AI 标配设计** — 无紫色渐变、无霓虹发光、无三卡等分、无 Acme 假名、无 em-dash。
8. **一致性锁定** — 一个调色板贯穿全 App，圆角系统全局统一，字体权重不混用。

### 2.4 设计对标与灵感参考

| 对标 | 借什么 | 不借什么 |
|------|--------|---------|
| Linear (暗色系统) | 层次化表面、透明白边分割、低饱和度文字色 | 极简工具感（VisePanda需要情感温度） |
| Apple (材质质感) | 留白、敬畏感、图片驱动的展示逻辑 | 纯白底+SF Pro（VisePanda是暗色品牌） |
| 柏悦酒店/安缦 (东方高端) | 深色暖金、克制东方元素、大留白 | 纯中文设计（VisePanda是国际受众） |
| Stripe (CTA设计) | 干净的按钮层级、微妙的hover态 | 紫色系、卡片式布局 |

### 2.5 设计可交付物路线图

```
Phase 0: Sketch 快速原型
  └─ 2-3 个视觉方案的 HTML mockup (sketch skill)
  └─ 通过 browser_vision 可视化对比
  └─ 用户选择方向

Phase 1: Figma 设计系统 (figma-generate-library)
  └─ Color tokens (Primitives + Semantic)
  └─ Typography tokens (8级字形)
  └─ Spacing/Radius/Shadow tokens
  └─ 核心组件：Button/Chip/Card/BottomNav/SectionHeader

Phase 2: Figma 页面设计 (figma-generate-design)
  └─ Home Screen
  └─ Explore Screen (卡片流+地图)
  └─ Chat Screen
  └─ City Detail Screen
  └─ Trips Screen
  └─ Tools 二级页

Phase 3: 代码实现
  └─ Design System module (Color/Typography/Spacing tokens in Compose)
  └─ 组件库 (Button/Chip/Card/BottomNav/Shimmer)
  └─ 页面组装
```

---

## 三、竞争分析

### 3.1 竞争格局

| 竞品 | 类型 | 优势 | 劣势 | VisePanda 机会 |
|------|------|------|------|---------------|
| TripAdvisor | 旅行社区 | 内容丰富，用户量大 | 通用型，中国数据弱；广告多 | 专精中国，无广告，品牌克制 |
| TripIt | 行程管理 | 行程组织强 | 无灵感/探索功能，UI老旧 | AI 生成+灵感+管理一体化 |
| 小红书 | UGC 内容 | 中国内容最丰富 | 中文为主，非结构化，质量参差 | 英文+结构化AI输出 |
| 携程国际版 | OTA | 预订能力强，中文数据好 | 工具感强，品牌感弱，设计过时 | 东方奢雅品牌化+AI旅行顾问 |
| ChatGPT Travel | 通用AI | 理解能力强，交互好 | 无中国专精数据，不能保存行程 | 中国垂直+行程资产化 |
| Wanderlog | AI旅行 | 已有AI行程功能 | 全球通用，中国数据弱；设计平淡 | 中国垂直+东方品牌设计 |

### 3.2 机会树

```
机会：来华国际旅行者的 AI 规划需求未被优质满足
├── 1. 目的地内容：中国城市结构化数据
│   ├── 28+ 城市卡片（图片+一句话描述+Must-see/Must-eat/Stay/Tips）
│   ├── 地图可视化（osmdroid，无API key）
│   └── 主题路线流转（First Time / Food / Culture）
├── 2. AI 规划能力
│   ├── 中国专精：知道北京适合3天，丽江适合5天
│   ├── 行程生成：按天数/偏好/预算定制方案
│   ├── 富内容输出：itinerary block + 推荐卡片 + 图片
│   └── 实时调整：对话式迭代
├── 3. 品牌溢价
│   ├── 东方奢雅暗色设计（非廉价工具）
│   ├── 克制金色点缀（可信赖非土豪）
│   └── 国际化视觉语言（非堆砌中国元素）
└── 4. 实用工具集成
    ├── Payment/Visa/网络指南
    ├── 应急信息
    ├── Etiquette/常用中文
    └── 场景化分类，视觉与全App一致
```

---

## 四、信息架构

### 4.1 页面树（完整版）

```
App
├── Home ───────────── 品牌首页，灵感驱动
│   ├── Hero 区
│   │   ├── 金色品牌标识 + 标语 "Your AI China Travel Companion"
│   │   └── 主 CTA: "Plan Your Trip" → Chat（预热prompt）
│   ├── 精选城市 (LazyRow, 3-4张)
│   │   └── 点击 → City Detail
│   ├── 主题灵感区
│   │   ├── "First Time in China"
│   │   ├── "Food Journey"
│   │   └── "Culture & Heritage"
│   ├── AI 规划入口卡
│   │   └── "Chat with Panda" → Chat
│   └── Tools 快捷入口（图标矩阵）
│
├── Explore ────────── 城市浏览 + 地图
│   ├── 视图切换器（卡片流 / 地图）
│   ├── 城市卡片网格 (LazyGrid)
│   │   ├── 头图
│   │   ├── 城市名 + 一句话
│   │   ├── 标签：美食/文化/自然
│   │   └── 点击 → City Detail
│   ├── 地图视图 (osmdroid)
│   │   ├── 城市标记
│   │   └── 点击标记 → 信息卡 → City Detail
│   └── 搜索/筛选（首版可选）
│
├── Chat ──────────── AI 旅行规划
│   ├── 顶部上下文栏（可选城市/行程名称）
│   ├── Prompt Suggestion Chips
│   │   ├── "Plan my 3-day trip to Beijing"
│   │   ├── "Best cities for food lovers"
│   │   └── "What to prepare before traveling to China?"
│   ├── 消息流
│   │   ├── 文本 Tokens（流式渲染）
│   │   ├── Itinerary Block（结构化行程卡片）
│   │   ├── Recommendation Card（推荐卡片）
│   │   ├── Image（图片内嵌）
│   │   └── FAQ（可折叠）
│   └── 底部输入栏
│       ├── 输入框 "Ask Panda..."
│       └── 发送按钮（金色）
│
├── Trips ──────────── 我的旅行资产
│   ├── 最近生成（按时间倒序）
│   ├── 已保存（本地持久化）
│   │   └── 行程卡片：标题 + 城市 + 天数 + 预览摘要
│   ├── 空状态引导
│   │   └── "Start planning your China adventure"
│   └── 删除行程（长按/滑动）
│
├── City Detail ────── 目的地叙事页
│   ├── 强头图（全宽，24dp底圆角）
│   ├── 城市名 + 一句话气质描述
│   ├── 统计数据：推荐停留天数 + 预算范围
│   ├── Must-see / Must-eat / Stay / Tips 分区
│   └── CTA: "Plan my trip to {City}" → Chat（预热prompt）
│
└── Tools ─────────── 旅行帮助中心（二级页）
    ├── Payment & Mobile
    ├── Visa & Entry
    ├── SIM & Internet
    ├── Emergency
    ├── Etiquette Guide
    └── Useful Chinese
```

### 4.2 Bottom Navigation 设计

| Tab | Icon | Label | 设计说明 |
|-----|------|-------|---------|
| Home | 熊猫标志 | Home | 品牌入口，特殊处理（可大小变化） |
| Explore | 指南针 | Explore | 经典探索图标 |
| Chat | 对话气泡 | Chat | 可加徽标（新消息） |
| Trips | 行李箱 | Trips | 资产中心 |

设计规范：
- 选中态：金色 (#C9A96E)
- 未选态：玉石灰 (#8B8B7A)
- 背景：#1A1A1A + 顶部 0.5dp #3A3A3A 分割线
- 图标大小：24dp
- 标签：11sp Medium, 0.3sp tracking

---

## 五、用户旅程设计

### 5.1 主路径：从灵感到行程

```
首页                        AI规划                      行程管理
┌─────────┐   点"Plan"    ┌──────────────┐   保存      ┌──────────┐
│ Home    │ ────────────> │ Chat         │ ──────────> │ Trips    │
│ 一张图  │              │ Panda助手     │             │ 我的行程  │
│ 一句话  │              │ 流式输出行程   │             │ 卡片列表  │
│ 一个CTA │              │ 结构化itinerary│             │ 可删除   │
└─────────┘              └──────────────┘             └──────────┘
       ↑                       │                              │
       │    (也可从Explore/City  │ 点城市卡片                     │
       │     Detail进入Chat)    │ 出发                          │
       │                       ↓                              │
       └───────────────────────────────────────────────────────┘
```

### 5.2 情感曲线设计

| 阶段 | 用户状态 | 设计目标 | 视觉处理 |
|------|---------|---------|---------|
| 首次打开 | 好奇但陌生 | 建立信任，传达价值 | Hero 大图+金色标识+一句话 |
| 浏览城市 | 兴趣激发 | 美图吸引，轻操作 | 全宽卡片流，左右滑动 |
| 进入详情 | 决策预备 | 说服 + 降低启动门槛 | 对称布局+CTA显眼 |
| AI 对话 | 期待但犹豫 | 降低犹豫，给予安全感 | 预设问题卡片（点选即发） |
| 流式输出 | 验证预期 | 明确进度，建立可信 | token逐字渲染+卡片渐入 |
| 保存行程 | 成就感 | 满足完成欲 | 保存成功动效 |
| 再次打开 | 信赖沉淀 | 减少步骤，直达价值 | 首页显示最近行程 |

---

## 六、数据流架构

### 6.1 层级划分

```
┌──────────────────────────────────────────┐
│  UI Layer (Compose)                       │
│  Home / Explore / Chat / Trips / Detail   │
│  └─ 只消费 UiState，不直接操作网络       │
├──────────────────────────────────────────┤
│  ViewModel Layer                          │
│  HomeViewModel / ExploreViewModel / ...   │
│  └─ StateFlow<UiState<T>> 驱动 UI        │
├──────────────────────────────────────────┤
│  Domain Layer (UseCases)                  │
│  GetFeaturedDestinationsUseCase           │
│  SendChatMessageUseCase                   │
│  SaveTripUseCase / ...                    │
├──────────────────────────────────────────┤
│  Data Layer (Repository)                  │
│  DestinationRepository                    │
│  ChatRepository (SSE)                     │
│  TripRepository (DataStore)               │
│  ToolsRepository                          │
├──────────────────────────────────────────┤
│  Network Layer (Core)                     │
│  OkHttp Client + SSE Client               │
│  Mock / Real 双通道                       │
└──────────────────────────────────────────┘
```

### 6.2 统一 UiState

```kotlin
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data object Empty : UiState<Nothing>()
    data class Error(val message: String, val retry: (() -> Unit)? = null) : UiState<Nothing>()
}
```

### 6.3 加载/空/错态设计规范

| 状态 | 视觉处理 | 动效 | 行为 |
|------|---------|------|------|
| Loading | Shimmer 骨架屏（暗色适配） | 金色脉冲 shimmer | 自动 fetch |
| Empty | 插画+引导文案+CTA | 入场渐入 | 点 CTA 跳转 |
| Error | 适中图标+错误原因+重试按钮 | 微颤动效 | 点重试重新加载 |
| 网络差 | Snackbar toast（不打断流程） | 底部滑入 | 自动重试×3后显示断网提示 |

---

## 七、关键交互设计

### 7.1 Launch 启动体验

- Splash 屏：纯黑背景 + 金色熊猫标识（SVG），居中，1.5s
- 标识渐隐 + Home 页 Hero 淡入
- 首页首次加载：shimmer 骨架屏

### 7.2 Tab 切换

- 水平滑动 + 内容交错渐入（cross-fade, 200ms）
- 选中 tab 图标/标签有颜色变化 + 轻微上浮（2dp）

### 7.3 Chat 流式渲染

- 用户消息：右对齐，暗色卡片 #2A2A2A，12dp 圆角
- AI 消息：左对齐，#1A1A1A 背景，12dp 圆角
- Token 逐字渲染：打字机效果（30ms/字）
- Itinerary Block：金色左边框 + 卡片布局 + 缩略图
- 推荐卡片：横向 LazyRow，带阴影层级

### 7.4 城市 Detail 进入动效

- 列表/地图点击城市 → 图片放大 + 信息覆盖层上推（shared element transition）
- 返回：逆动画

### 7.5 Trip 保存 & 空态

- 保存：底部滑入确认 Toast
- 空态：插画风格熊猫 + "Start planning..." CTA 按钮
- 列表长按：红色删除确认

---

## 八、迭代计划

### 阶段总览

```
P0: 工程骨架 + 设计系统 ──────── 1天   ⬅ 当前
P1: Sketch 视觉方案验证 ──────── 0.5天  ⬅ 新增！设计先行
P2: Figma 设计系统 + 页面 ────── 1天   ⬅ 新增！设计先行
P3: 顶层导航 + 页面容器 ──────── 1天
P4: Home + Explore ───────────── 2天
P5: City Detail ──────────────── 1天
P6: Chat 主线 + 后端修复 ──────── 2天
P7: Trips + Tools ────────────── 1天
P8: 收尾验证 + APK ───────────── 1天
```

### P0：工程骨架 + 设计系统

核心输出物：
- Android 工程目录结构（模块化，8+ 子模块）
- Gradle 配置（AGP 8.2, Kotlin 1.9, Compose BOM 2024.02）
- **Design System 模块（core/designsystem）**
  - Color tokens（`VisePandaColor.kt`，8色+）
  - Typography tokens（8级字形，`VisePandaTypography.kt`）
  - Spacing tokens（间距刻度，`VisePandaSpacing.kt`）
  - Shape tokens（圆角系统，`VisePandaShape.kt`）
  - 组件：VpButton / VpChip / VpCard / VpSectionHeader / VpBottomNav
  - Shimmer 骨架屏自定义组件
- 网络模块（OkHttp + SSE Client + Mock/Real）
- GitHub Actions build.yml

### P1：Sketch 视觉方案验证（新增 — 设计先行）

- 创建 2-3 个 HTML mockup 对比方案
  - 方案 A：东方奢雅（暗色+金色）（推荐）
  - 方案 B：极简暗色（更接近 Linear）
  - 方案 C：浅色融合（浅米金质感）
- 使用 `browser_vision` 可视化验证
- **用户选择方向后再进入下一步**

### P2：Figma 设计系统（新增 — 设计先行）

- 在 Figma 中建立完整设计系统
  - Color tokens（Primitives + Semantic + Dark mode only）
  - Typography styles（8级）
  - Spacing / Radius / Shadow tokens
  - Core 组件：Button / Card / Chip / Input / BottomNav
- 设计 5 个关键页面
  - Home / Explore / Chat / City Detail / Trips
- 用户确认设计稿后进入编码

### P3 ~ P8

[同原计划 P1-P7，但设计系统已提前就位，编码阶段直接复用 Figma 设计的 token 和组件]

---

## 九、技术决策

| 决策 | 选择 | 理由 |
|------|------|------|
| UI 框架 | Jetpack Compose + Material3 | 现代声明式，与设计系统 token 天然对接 |
| 导航 | Navigation Compose | 官方方案，类型安全路由 |
| 网络 | OkHttp + 自定义 SSE Client | 轻量、可控、无冗余依赖 |
| 地图 | osmdroid | 无 API key，离线可用 |
| 本地存储 | DataStore Preferences + Proto | 轻量、协程原生 |
| 图片加载 | Coil 3 | Compose 原生、轻量、支持 shimmer |
| 编译 | GitHub Actions | 无需本地环境，自动签名 |
| 设计系统 | Compose 自定义（非 Material 默认） | 品牌定制的 Color/Typography/Shape tokens |

---

## 十、成功标准

| 指标 | 目标 | 验证方式 |
|------|------|---------|
| 编译通过 | GitHub Actions assembleRelease ✅ | 检查 Action run |
| 设计一致性 | 东方奢雅主题全 App 统一 | 截图对比设计 token |
| 空/错/重试态 | 所有页面有覆盖 ✅ | 断网测试+空数据测试 |
| 城市列表 | 从真实 API 加载 28+ 城市 ✅ | curl 验证 |
| Chat SSE | 流式对话可用 ✅ | 输入→流式输出→完整消息 |
| Trip 持久化 | 保存/读取/删除 ✅ | 重启 App 验证数据存在 |
| 地图 | osmdroid 加载+城市标记 ✅ | 视觉验证 |
| 动效 | 4 个关键动效点 ✅ | visual diff |
| 安装验证 | APK 可安装、核心路径可走通 ✅ | 手动测试 |

---

## 十一、设计技能链（构建过程中使用）

```
┌─────────────────────────────────────────────┐
│ 设计流程与可调用的 skills                      │
├─────────────────────────────────────────────┤
│ Phase 0:                                   │
│   taste-skill → 设计方向校准 (3-dial 系统)    │
│   popular-web-designs → 设计参考 (Linear)    │
│   sketch → 快速 HTML mockup + browser_vision │
│                                             │
│ Phase 1:                                   │
│   figma-generate-library → 设计系统创建      │
│   figma-generate-design → 页面设计布局       │
│   figma-use → Figma API 操作               │
│                                             │
│ Phase 2:                                   │
│   coding-workflow → TDD 编码流程            │
│   test-driven-development → 测试驱动开发     │
│   project-iteration → 迭代管理+进度汇报      │
└─────────────────────────────────────────────┘
```

---

> 文档完结。一切从设计开始。
> "在代码之前，先看见。
>  在 Figma 之前，先感觉。
>  在感觉之前，先理解用户是谁。"
