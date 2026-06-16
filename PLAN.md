# VisePanda Native — 迭代计划 (v2.0)

> 版本: v2.0 | 日期: 2026-06-17
> 总计: 9 个阶段 | 预计 ~11 天
> 核心理念: **从设计开始，而不是从代码开始**

---

## 阶段总览

```
P0: 工程骨架+设计系统 ────────── 1天     代码
P1: 🎨 Sketch 视觉方案验证 ────── 0.5天   设计 (sketch skill)
P2: 🎨 Figma 设计系统+页面 ────── 1天     设计 (figma-generate-* skills)
P3: 顶层导航+页面容器 ────────── 1天     代码
P4: Home + Explore ───────────── 2天     代码
P5: City Detail ──────────────── 1天     代码
P6: Chat 主线+后端修复 ───────── 2天     代码
P7: Trips + Tools ───────────── 1天     代码
P8: 收尾验证+APK ────────────── 1天     代码+部署
```

**关键变更：v1.0 直接写代码 → v2.0 先出设计再写代码**

---

## P0：工程骨架 + 设计系统

**目标：** 可编译的 Android 工程 + 完整设计 token + 复用组件库

### 任务清单

- [ ] 创建 Android 工程目录结构
  ```
  app/
  core/designsystem/
  core/network/
  core/common/
  domain/
  data/
  feature/home/
  feature/explore/
  feature/chat/
  feature/trips/
  feature/tools/
  ```
- [ ] 配置 Gradle（AGP 8.2, Kotlin 1.9, Compose BOM 2024.02）
- [ ] 设计系统模块
  - [ ] Color tokens（VisePandaColor.kt）
    - `VisePandaDarkColorScheme`
    - Primary accent: `Gold = 0xFFC9A96E`
    - Secondary: `JadeGreen = 0xFF5B7B5A`, `JadeGrey = 0xFF8B8B7A`
    - Surface tones: `SurfaceDark(0xFF0A0A0A)`, `Surface(0xFF1A1A1A)`, `SurfaceElevated(0xFF232323)`, `SurfaceHover(0xFF2A2A2A)`
    - Text tones: `TextPrimary(0xFFF5F0E8)`, `TextSecondary(0xFFD4CEC4)`, `TextTertiary(0xFF9C9A94)`
    - Border: `Border(0xFF3A3A3A)`
    - Error: `Error(0xFFCF6679)`
  - [ ] Typography tokens（8级字形）
    - DisplayXL / DisplayL / Headline / Subhead / Body / BodySmall / Caption / TabLabel
    - 每个 token 包含：fontSize, fontWeight, lineHeight, letterSpacing
  - [ ] Spacing scale（xs/sm/md/lg/xl/xxl/section）
  - [ ] Shape tokens（xs/sm/md/lg/xl）
  - [ ] Shadow elevation tokens（0-4级）
- [ ] 组件
  - [ ] VpGoldButton — 主金色CTA
  - [ ] VpSecondaryButton — 次按钮
  - [ ] VpGhostButton — 幽灵按钮
  - [ ] VpChip — 标签（金色outline/竹青/玉石灰）
  - [ ] VpCard — 城市卡片（头图+覆盖层+标签）
  - [ ] VpSectionHeader — 区段标题
  - [ ] VpBottomNav — 底部导航（4 tab，金色选中态）
  - [ ] VpShimmer — 骨架屏组件（暗色适配）
- [ ] 网络模块
  - [ ] OkHttpClient 封装
  - [ ] SSE Client（独立协议层）
  - [ ] MockInterceptor（Mock/Real 双通道）
- [ ] 通用 UiState 定义
- [ ] 通用 AppError 定义
- [ ] GitHub Actions build.yml（签名APK）

### 已确定的色板

```
VisePandaDarkColorScheme(
  primary:         Gold(0xFFC9A96E),
  onPrimary:       Dark(0xFF0A0A0A),
  secondary:       JadeGrey(0xFF8B8B7A),
  tertiary:        JadeGreen(0xFF5B7B5A),
  background:      Dark(0xFF0A0A0A),
  surface:         Surface(0xFF1A1A1A),
  surfaceVariant:  SurfaceElevated(0xFF232323),
  ... 
)
```

### 交付物
- 工程可编译 ✅
- 设计 token 可引用，全 App 统一主题 ✅
- 组件可预览（@Preview）✅
- 网络层可发起请求 ✅
- GitHub Actions 自动构建 APK ✅

---

## P1：🎨 Sketch 视觉方案验证

**设计先行步骤 — 避免代码写到一半发现方向不对**

### 任务清单

- [ ] 用 `sketch` skill 创建 2-3 个 HTML mockup
  - 方案 A：东方奢雅（推荐）— 暗色底+金色点缀+大留白+竹青辅助
  - 方案 B：极简暗色 — 更接近 Linear，克制金色，银灰为主
  - 方案 C：浅色东方 — 米白底+金色点缀+竹青，更明亮
- [ ] 每个方案包含核心 3 页: Home / Chat / Explore
- [ ] 使用 `browser_vision` 截图对比
- [ ] 输出 "设计读" (Design Read) — 按 taste-skill 规则
- [ ] 加载 `popular-web-designs` 参考页
  - Linear: 暗色系统层次设计
  - Apple: 留白与视觉敬畏感
- [ ] **用户选择方向后进入 P2**

### 对比维度

| 维度 | 方案A 东方奢雅 | 方案B 极简暗色 | 方案C 浅色东方 |
|------|--------------|--------------|--------------|
| 底色 | #0A0A0A | #08090A | #F5F0E8 |
| 品牌色 | 金色 #C9A96E | 银色灰 #8B8B7A | 米金 #D4CEC4 |
| 密度 | 低(3) | 中(4) | 中(4) |
| 变化 | 高(7) | 中(5) | 中(6) |
| 动效 | 中(5) | 低(3) | 中(5) |
| 个性 | 🌟 强 | 克制 | 温暖 |

---

## P2：🎨 Figma 设计系统 + 页面

**在 Figma 中建立完整的视觉语言和页面设计，再开始写代码**

### 任务清单

#### 设计系统 (figma-generate-library)

- [ ] 创建 Figma 设计系统文件
- [ ] 建立 Color tokens
  - Primitives 集合（纯色值）
  - Semantic 集合（Light/Dark 模式，VisePanda 只有 Dark）
- [ ] 建立 Typography styles（8级）
- [ ] 建立 Spacing / Radius / Shadow tokens
- [ ] 创建 Core 组件
  - Button（Gold / Secondary / Ghost × 状态：Default/Pressed/Disabled）
  - Card（City Card / Trip Card / Info Card）
  - Chip（Gold outline / Jade Green / Jade Grey）
  - Bottom Nav
  - Section Header

#### 页面设计 (figma-generate-design)

- [ ] Home Screen（Hero + 精选城市 + 灵感区 + Tools入口）
- [ ] Explore Screen（卡片流 + 地图视图）
- [ ] Chat Screen（消息流 + 输入栏 + suggestion chips）
- [ ] City Detail Screen（头图 + 信息区 + CTA）
- [ ] Trips Screen（列表 + 空态）

#### 交付物

- Figma 设计系统文件 URL
- 每个页面的 screenshot 对比
- 设计 token 导出文档
- **用户确认后进入 P3**

---

## P3：顶层导航 + 页面容器

### 任务清单

- [ ] 路由定义（NavHost + Sealed class routes）
  - Home / Explore / Chat / Trips / CityDetail / Tools
- [ ] BottomNavBar 组件（金色选中态 + 竹青辅助）
- [ ] NavHost 装配 + 导航图
- [ ] 4 个 Feature 模块的占位页面
- [ ] App 主题入口（`VisePandaTheme { ... }`）
- [ ] 导航过渡动画（cross-fade + 内容交错渐入）
- [ ] 测试：4 Tab 可流畅切换

---

## P4：Home + Explore

### Home 任务

- [ ] Hero 区
  - 深色背景 #0A0A0A
  - 金色品牌标识
  - 标语 "Your AI China Travel Companion"
  - 主 CTA: VpGoldButton → Chat
  - 入场动效：标识渐入 → 文字上移 + CTA 出现
- [ ] 精选城市 LazyRow（加载 shimmer → 图片卡片）
- [ ] 主题灵感区（3 个标签，横向滑动）
- [ ] Chat 入口卡片
- [ ] Tools 2×3 入口矩阵
- [ ] 数据：Loading/Error/Empty 三态

### Explore 任务

- [ ] 视图切换 Tab（卡片流 / 地图）
- [ ] 城市卡片网格（LazyVerticalGrid，2列）
  - 卡片设计：全宽图片 + 底部覆盖层(城市名+一句话)
  - 点击 → City Detail
- [ ] 地图视图（osmdroid）
  - 城市标注
  - 点击标注 → 信息弹出 → City Detail
- [ ] 搜索/筛选（首版可选）

### 数据

- [ ] DestinationRepository（API: /api/cities → dict 解析）
- [ ] MapRepository（/api/map → dict 解析）
- [ ] 真实 API 通路 + Mock 兜底

---

## P5：City Detail

### 任务清单

- [ ] 头图区（全宽，24dp 底圆角，入场 zoom-in）
- [ ] 城市名（Display Large） + 一句话气质描述（Body）
- [ ] 统计卡：推荐天数 + 预算范围
- [ ] Must-see / Must-eat / Stay / Tips 四个展开区
  - 设计：金色左边框 + 卡片内容
- [ ] CTA: VpGoldButton "Plan my trip to {City}" → Chat（预热 prompt）
- [ ] 加载态：shimmer 骨架屏
- [ ] 错误态 + 重试

---

## P6：Chat 主线 + 后端修复

### Chat 任务

- [ ] ChatSseClient（适配后端 `event: message` + payload type）
- [ ] ChatRepository（连接管理 + 消息流）
- [ ] ChatViewModel + UiState
- [ ] Chat 页面 UI
  - [ ] 顶部上下文栏（金色标签显示当前城市/行程名）
  - [ ] Prompt Suggestion Chips（横向滚动，金色 outline）
    - "Plan my 3-day trip to Beijing"
    - "Best cities for food lovers"
    - "What to prepare before traveling to China?"
  - [ ] 消息流
    - [ ] Token 文本打字机渲染
    - [ ] Itinerary Block（金色左边框+行程卡片+缩略图）
    - [ ] Recommendation Card（横向LazyRow）
    - [ ] 图片内嵌
    - [ ] FAQ（可折叠，竹青展开指示）
  - [ ] 输入栏（"Ask Panda..."）+ 金色发送按钮
  - [ ] "Save to Trips" 按钮（下一条消息后出现）
- [ ] Mock 数据通道（用于离线开发）

### 后端任务

- [ ] 修复 `vise-panda-2/api/index.py` 的 `import re`（chat SSE 回归）
- [ ] 切换 DeepSeek → GLM 5.1
- [ ] 本地测试 chat SSE
  ```bash
  curl -X POST https://www.go2china.space/api/chat \
    -H "Content-Type: application/json" \
    -d '{"messages":[{"role":"user","content":"帮我规划北京3日游"}]}'
  # 应返回 event: message / data: {"type":"token","content":"..."}
  ```
- [ ] 部署到 Vercel

---

## P7：Trips + Tools

### Trips 任务

- [ ] 最近生成列表（时间倒序，顶部3条）
- [ ] 已保存列表（DataStore Proto 持久化）
- [ ] 行程卡片设计
  - 金色左边框 + 城市名 + 天数 + 预览摘要（2行）
  - 滑动删除（红色确认）
- [ ] 空状态（插画熊猫 + "Start planning..." CTA → Chat）

### Tools 任务

- [ ] 场景化分类卡片
  - Payment & Mobile / Visa & Entry / SIM & Internet / Emergency / Etiquette / Chinese
  - 卡片设计：竹青图标 + 标题 + 预览2行
- [ ] 展开式内容（accordion 模式可折叠）
- [ ] Mock 数据

---

## P8：收尾验证 + APK

### 任务清单

- [ ] 全链路手动验证
  - [ ] Home 加载 + shimmer → 内容
  - [ ] Explore 城市流 + 地图
  - [ ] City Detail 数据 + CTA
  - [ ] Chat 对话（流式渲染 + 富卡片 + 保存）
  - [ ] Trip 保存 + 读取 + 删除
  - [ ] Tools 内容
  - [ ] 断网测试（错误态 + 重试）
  - [ ] 空数据测试（各页空态展示）
- [ ] 设计一致性审计（对比 Figma 设计稿）
  - [ ] 色板一致性
  - [ ] 圆角系统一致性
  - [ ] 间距系统一致性
  - [ ] 字体系统一致性
  - [ ] 4 个关键动效点验证
- [ ] GitHub Actions 编译通过
- [ ] APK 下载 + 安装验证
- [ ] README.md（产品介绍+设计文档+架构说明）
- [ ] ~~微信汇报进度~~

---

## 风险与缓解

| 风险 | 概率 | 影响 | 缓解措施 |
|------|------|------|---------|
| Figma API key 未配置 | 高 | 中 | 先用 sketch 做 HTML mockup，P2 需要 Figma key |
| 后端 GLM 5.1 不兼容 SSE | 中 | 高 | Mock 通道兜底，逐步适配 |
| 编译依赖冲突 | 低 | 中 | 锁定版本号 |
| 地图性能 | 低 | 中 | osmdroid 延迟加载 |
| 用户对设计方案不满意 | 中 | 中 | P1 先出 3 个方向对比，确认后再进入 Figma |
| 设计 → 代码落地走样 | 中 | 高 | P8 做设计一致性审计 |

---

## 进度追踪

| 阶段 | 状态 | 开始 | 结束 |
|------|------|------|------|
| P0: 工程骨架+设计系统 | ⏳ 待开始 | - | - |
| P1: 🎨 Sketch 视觉方案验证 | ⏳ 待开始 | - | - |
| P2: 🎨 Figma 设计系统+页面 | ⏳ 待开始 | - | - |
| P3: 顶层导航+页面容器 | ⏳ 待开始 | - | - |
| P4: Home + Explore | ⏳ 待开始 | - | - |
| P5: City Detail | ⏳ 待开始 | - | - |
| P6: Chat 主线+后端修复 | ⏳ 待开始 | - | - |
| P7: Trips + Tools | ⏳ 待开始 | - | - |
| P8: 收尾验证+APK | ⏳ 待开始 | - | - |

---

## 设计技能资源清单

| Skill 名称 | 在本项目中怎么用 |
|-----------|----------------|
| taste-skill | 设计方向校准：3-dial 配置 (3/7/5)、防AI标配检查清单 |
| popular-web-designs | 参考 Linear.app 暗色系统设计（层次/阴影/文字色） |
| sketch | P1: 快速出 3 个 HTML mockup + browser_vision 可视化验证 |
| figma-generate-library | P2: 在 Figma 创建完整设计系统（color/typography/spacing tokens）|
| figma-generate-design | P2: 在 Figma 设计 5 个关键页面 |
| figma-use | P2: Figma Plugin API 底层操作 |
| coding-workflow | 编码阶段全流程 |
| test-driven-development | 测试驱动开发 |
| project-iteration | 迭代管理+进度汇报+微信通知 |
