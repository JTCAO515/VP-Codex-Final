# VisePanda 产品需求与开发设计文档

> **版本:** v2.0 | **日期:** 2026-06-17
> **用途:** 交付给开发团队从零构建 VisePanda 完整产品体系
> **交付形态:** Android 原生 App + Web 管理后台 + 后端 API
> **核心原则:** 不依赖任何已有代码、数据库或之前的设计

---

## 目录

1. [产品定义与战略](#1-产品定义与战略)
2. [用户与市场分析](#2-用户与市场分析)
3. [产品范围与边界](#3-产品范围与边界)
4. [信息架构](#4-信息架构)
5. [核心模块需求](#5-核心模块需求)
6. [用户系统与账号体验](#6-用户系统与账号体验)
7. [管理后台需求](#7-管理后台需求)
8. [数据与权限模型](#8-数据与权限模型)
9. [设计系统（东方奢雅）](#9-设计系统东方奢雅)
10. [技术架构指引](#10-技术架构指引)
11. [非功能性要求](#11-非功能性要求)
12. [指标与验收口径](#12-指标与验收口径)

---

## 1. 产品定义与战略

### 一句话定义

**VisePanda 是一款面向来华外国游客的旅行决策与在地服务入口：** 用城市内容帮助建立理解，用 AI 问答帮助做出选择，用行程承接帮助保存与复访，用工具箱帮助解决在地难题，并通过账号体系为后续运营、个性化与商业化建立基础。

### 北极星体验

> 让一个对中国不熟悉的海外用户，在 10 分钟内从"我想去中国但不知道怎么选"推进到"我知道去哪、怎么玩、需要准备什么，并且敢于开始计划"。

### 核心价值主张

| 维度 | 描述 |
|------|------|
| **目标用户** | 计划或正在中国旅行的国际游客（英语母语优先，25-45岁） |
| **用户状态** | 对中国有兴趣但信息有限，被语言/文化/支付壁垒困扰 |
| **核心价值** | AI 一键生成个性化中国行程，城市选择→路线规划→实用工具全程覆盖 |
| **差异化** | 专精中国市场（非通用全球）+ 东方奢雅品牌调性（非工具堆砌） |
| **情感承诺** | **"一个懂中国的旅行伙伴"**——不是信息罗列，是规划能力的延伸 |

### 价值主张拆解

- **更快的理解**：把城市与旅行信息从"搜索结果"变成"结构化解释"
- **更短的决策**：用 AI 让问题变成建议，把犹豫变成可执行选择
- **更高的行动信心**：工具箱覆盖高焦虑点，降低行中不确定性
- **更强的复访理由**：行程承接与账号体系让内容不再一次性消费

### 竞争格局

| 竞品 | 类型 | 优势 | 劣势 | VisePanda 机会 |
|------|------|------|------|---------------|
| TripAdvisor | 旅行社区 | 内容丰富，用户量大 | 通用型，中国数据弱；广告多 | 专精中国，无广告，品牌克制 |
| TripIt | 行程管理 | 行程组织强 | 无灵感/探索功能，UI 老旧 | AI 生成+灵感+管理一体化 |
| 小红书 | UGC 内容 | 中国内容最丰富 | 中文为主，非结构化 | 英文+结构化 AI 输出 |
| 携程国际版 | OTA | 预订能力强，中文数据好 | 工具感强，品牌感弱 | 东方奢雅品牌化+AI 旅行顾问 |
| ChatGPT Travel | 通用 AI | 理解能力强，交互好 | 无中国专精数据，不能保存行程 | 中国垂直+行程资产化 |
| Wanderlog | AI 旅行 | 已有 AI 行程功能 | 全球通用，中国数据弱；设计平淡 | 中国垂直+东方品牌设计 |

### 战略取舍

| ✅ 要做 | ❌ 不做 |
|---------|---------|
| AI 驱动的中国旅行规划 | 全球旅行（保持专注） |
| 深色东方奢雅品牌设计 | 通用 Material 主题 |
| 英文原生内容 | 多语言（首版不覆盖） |
| 行程灵感到落地一体化 | 纯工具/信息罗列 |
| 轻量无广告体验 | 付费墙/订阅付费模式 |
| 邮箱注册登录体系 | 社交登录（Google/Apple） |
| 最小可用管理后台 | 复杂数据分析、审计日志、营销邮件 |
| 用户角色与状态管理 | 复杂权限矩阵 |

---

## 2. 用户与市场分析

### 2.1 用户画像

| | 画像 A：行前探索者 | 画像 B：规划推进者 | 画像 C：行中求助者 | 画像 D：复访与沉淀者 |
|--|-------------------|-------------------|-------------------|-------------------|
| **状态** | 对中国有兴趣，想选城市和路线 | 已选定城市组合，需具体日程 | 旅途中遇到支付/交通/翻译问题 | 希望二次规划或复用行程 |
| **信息来源** | 零散，缺少可信对比 | 有方向但缺落地细节 | 问题碎且急 | 经验复用 |
| **敏感能力** | 解释型内容 | 顾问式问答 | 即时工具 | 行程资产化 |
| **产品入口** | Home → Explore | City Detail → Chat | Tools | Trips |

### 2.2 典型场景

1. **行前**：筛选城市，理解城市气质与亮点，估算预算，确定路线
2. **行前**：围绕个人偏好提问，得到更个性化的建议与注意事项
3. **行前**：把答案保存成自己的计划，并在之后反复查看与调整
4. **行中**：快速解决支付、交通、换乘、翻译、通信、紧急情况等问题

### 2.3 情感曲线设计

| 阶段 | 用户状态 | 设计目标 | 关键体验点 |
|------|---------|---------|-----------|
| 首次打开 | 好奇但陌生 | 建立信任，传达价值 | Hero 大图+金色标识+一句话 |
| 浏览城市 | 兴趣激发 | 美图吸引，轻操作 | 全宽卡片流，左右滑动 |
| 进入详情 | 决策预备 | 说服 + 降低启动门槛 | 对称布局+CTA 显眼 |
| AI 对话 | 期待但犹豫 | 降低犹豫，给予安全感 | 预设问题卡片（点选即发） |
| 流式输出 | 验证预期 | 明确进度，建立可信 | token 逐字渲染 + 卡片渐入 |
| 保存行程 | 成就感 | 满足完成欲 | 保存成功动效 |
| 再次打开 | 信赖沉淀 | 减少步骤，直达价值 | 首页显示最近行程 |

---

## 3. 产品范围与边界

### 3.1 当前阶段要做的事

**Android 原生客户端：**
- Home / Explore / City Detail / Chat / Trips / Tools（含 Map 视图）
- 邮箱注册、登录、登出、邮箱验证、忘记密码、重置密码闭环、会话恢复
- Account 页面展示当前状态与基本信息，提供登出入口

**后端 API：**
- 认证系统（注册/登录/验证/重置/会话）
- 目的地数据接口（城市列表/详情）
- Chat SSE 流式接口
- Trip CRUD 接口
- 用户管理接口（管理员）

**管理后台（Web）：**
- 管理员登录
- Dashboard：用户总数与状态分布
- 用户列表：搜索与筛选
- 用户详情：查看字段与编辑 role/status/display_name

### 3.2 明确暂不做

- 复杂的交易闭环（订票、支付、订单、退款）
- 复杂的权限矩阵（多角色、多层级、多资源授权）
- 社区化运营（内容发布、评论体系、关注关系、动态流）
- 社交登录（Google/Apple/手机号）与双因素认证
- 审计日志、批量导入导出、营销邮件等重后台功能
- 完整的个人资料编辑页（头像上传、偏好设置等）

---

## 4. 信息架构

### 4.1 页面树（完整版）

```
VisePanda App
├── Splash ────────── 纯黑背景 + 金色熊猫标识，1.5s后淡入Home
│
├── [未登录] Welcome ── 登录/注册入口
│   ├── 登录页
│   ├── 注册页
│   └── 忘记密码 → 重置密码
│
├── Home ──────────── 品牌首页，灵感驱动
│   ├── Hero 区（品牌标识 + 标语 + "Plan Your Trip" CTA）
│   ├── 精选城市 (LazyRow, 3-4张)
│   ├── 主题灵感区（"First Time in China" / "Food Journey" / "Culture"）
│   ├── AI 规划入口卡（"Chat with Panda"）
│   └── Tools 快捷入口（图标矩阵）
│
├── Explore ───────── 城市浏览 + 地图
│   ├── 视图切换器（卡片流 / 地图）
│   ├── 城市卡片网格 (LazyVerticalGrid)
│   │   ├── 头图 + 城市名 + 一句话 + 标签
│   │   └── 点击 → City Detail
│   └── 地图视图 (osmdroid)
│       ├── 城市标记
│       └── 点击标记 → Info Window → City Detail
│
├── City Detail ───── 目的地叙事页
│   ├── 全宽头图（24dp 底圆角）
│   ├── 城市名 + 一句话气质描述
│   ├── 统计数据：推荐停留天数 + 预算范围
│   ├── Must-see / Must-eat / Stay / Tips 分区
│   └── CTA: "Plan my trip to {City}" → Chat（带城市上下文）
│
├── Chat ──────────── AI 旅行规划
│   ├── 顶部上下文栏（当前城市/行程名称）
│   ├── Prompt Suggestion Chips
│   │   ├── "Plan my 3-day trip to Beijing"
│   │   ├── "Best cities for food lovers"
│   │   └── "What to prepare before traveling to China?"
│   ├── 消息流（反向列表）
│   │   ├── 用户消息：右对齐，#2A2A2A 卡片
│   │   ├── AI 消息：左对齐，#1A1A1A 背景
│   │   ├── Token 逐字渲染（打字机效果）
│   │   ├── Itinerary Block（金色左边框+卡片布局）
│   │   ├── Recommendation Card（横向 LazyRow）
│   │   ├── Image（内嵌图片）
│   │   └── FAQ（可折叠）
│   └── 底部输入栏（输入框 + 发送按钮）
│
├── Trips ─────────── 我的旅行资产
│   ├── 最近生成（按时间倒序）
│   ├── 已保存的行程卡片（标题 + 城市 + 天数 + 预览摘要）
│   ├── 空状态引导插画 + "Start planning your China adventure" + CTA
│   └── 删除行程（长按/滑动确认）
│
├── Tools ─────────── 旅行帮助中心
│   ├── Payment & Mobile Guide
│   ├── Visa & Entry Info
│   ├── SIM & Internet Guide
│   ├── Emergency Contacts
│   ├── Etiquette Guide
│   └── Useful Chinese Phrases
│
└── Account ───────── 账号管理页
    ├── [未登录] 展示登录/注册入口
    ├── [已登录] 展示邮箱、昵称、角色、状态
    └── 登出按钮

=== 管理后台（Web）===

Admin Login ───────── 管理员邮箱密码登录
├── Dashboard ─────── 用户总数 + active/pending/disabled 数量
├── User List ─────── 用户列表（搜索/筛选/分页）
│   └── 点击 → User Detail
└── User Detail ───── 用户详情 + 编辑
    ├── 查看字段：email, display_name, role, status, created_at, last_login_at
    └── 操作：修改 display_name, 调整 role, 启用/禁用 status
```

### 4.2 Bottom Navigation 设计

| Tab | Icon | Label | Design |
|-----|------|-------|--------|
| Home | 熊猫标志 | Home | 品牌入口，特殊处理 |
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

## 5. 核心模块需求

### 5.1 Home（首页）

| # | 需求 | 优先级 | 说明 |
|---|------|--------|------|
| 1 | 品牌化的首屏表达 | P0 | 用户一眼知道"这是做中国旅行的" |
| 2 | 精选城市入口 | P0 | 支持直达城市详情 |
| 3 | 明显入口：Explore / Chat / Trips / Tools / Account | P0 | 5 个核心入口 |
| 4 | 加载/失败/重试状态 | P0 | 基本状态覆盖 |
| 5 | 主题灵感区 | P1 | "First Time in China" / "Food Journey" / "Culture" |

### 5.2 Explore（城市探索）

| # | 需求 | 优先级 | 说明 |
|---|------|--------|------|
| 1 | 城市集合易扫读展示 | P0 | 卡片网格，便于快速筛选 |
| 2 | 点击进入城市详情 | P0 | 从 Explore → City Detail |
| 3 | 加载/失败/重试状态 | P0 | 基本状态覆盖 |
| 4 | 地图视图 | P0 | osmdroid 加载，城市标记 |
| 5 | 视图切换（卡片/地图） | P1 | 切换动画 |
| 6 | 搜索/筛选 | P2 | 按城市名/标签搜索 |

### 5.3 City Detail（城市详情）

| # | 需求 | 优先级 | 说明 |
|---|------|--------|------|
| 1 | 结构化信息展示 | P0 | 简介、亮点、预算、必吃、住宿、旅行提示 |
| 2 | 一键进入 Chat 带城市上下文 | P0 | "Plan my trip to {City}" CTA |
| 3 | 把建议沉淀为计划 | P1 | 与 Trips 关联 |

### 5.4 Chat（AI 旅行助手）

| # | 需求 | 优先级 | 说明 |
|---|------|--------|------|
| 1 | 连续对话 | P0 | 围绕城市/行程展开 |
| 2 | 流式输出 | P0 | token 逐字渲染 |
| 3 | 基础富文本展示 | P0 | 链接、强调、代码样式 |
| 4 | 常见问题快速入口 | P1 | 建议问题 chips |
| 5 | 行程保存到 Trips | P0 | 自动或手动保存 |
| 6 | Itinerary Block 渲染 | P1 | 金色边框+布局 |
| 7 | Recommendation Card | P1 | 横向滚动 |
| 8 | 图片内嵌 | P1 | AI 回复中的图片 |
| 9 | FAQ 折叠 | P2 | 可折叠区域 |

### 5.5 Map（地图）

| # | 需求 | 优先级 | 说明 |
|---|------|--------|------|
| 1 | 展示核心城市分布 | P0 | 建立"去哪"的空间感 |
| 2 | 点击城市标记弹出信息 | P0 | 简要信息 → 进入城市详情 |

### 5.6 Trips（行程）

| # | 需求 | 优先级 | 说明 |
|---|------|--------|------|
| 1 | 展示保存的行程列表 | P0 | 按时间倒序 |
| 2 | 空状态表达+下一步引导 | P0 | 去 Chat/Explore |
| 3 | 删除行程 | P0 | 避免"保存后不可控" |

### 5.7 Tools（工具箱）

| # | 需求 | 优先级 | 说明 |
|---|------|--------|------|
| 1 | 面向来华游客的工具入口集合 | P0 | 覆盖高频焦虑点 |
| 2 | 至少包含 8 个工具 | P1 | 行李/货币/签证/翻译/应急/礼仪/景点/学习 |

---

## 6. 用户系统与账号体验

### 6.1 邮箱登录体系

| 能力 | 用户动作 | 期望结果 | 备注 |
|------|---------|---------|------|
| 注册 | 输入邮箱、密码、昵称（可选） | 创建账号，进入待验证状态 | 需要邮箱验证 |
| 登录 | 输入邮箱、密码 | 登录成功进入 Account 页 | 清晰的失败提示 |
| 登出 | Account 页点击登出 | 清空登录态 | 不应残留"已登录 UI" |
| 忘记密码 | 输入邮箱申请重置 | 发出重置邮件 | 可复用提示文案 |
| 重置密码闭环 | 从邮件链接打开 App，设置新密码 | 新密码设置成功，引导返回登录 | 支持深链回跳 |
| 会话恢复 | 冷启动 App | 自动恢复到已登录状态 | Account 页展示 |

### 6.2 Account 页面

- **未登录**：展示"未登录状态"，提供进入登录/注册入口
- **已登录**：展示邮箱、昵称、角色、状态等关键信息，并提供登出
- **登录/注册成功**：默认跳转到 Account，作为登录结果确认页

### 6.3 当前阶段不做

- 手机号登录
- 第三方登录（Google/Apple）
- 双因素认证
- 完整的个人资料编辑页

---

## 7. 管理后台需求

### 7.1 管理员进入条件

- 必须是已登录用户
- 必须满足 `role = admin` 且 `status = active`

### 7.2 后台页面范围

| 页面 | 目标 | 必须能力 |
|------|------|---------|
| 登录页 | 管理员登录入口 | 邮箱密码登录，失败提示清晰 |
| Dashboard | 快速了解用户盘子 | 总数、active/pending/disabled 数量 |
| 用户列表 | 查找与筛选用户 | 按邮箱搜索、按角色筛选、按状态筛选 |
| 用户详情 | 查看用户信息 | email, display_name, role, status, created_at, last_login_at |
| 用户操作 | 基础管理 | 修改 display_name、调整 role、启用/禁用 status |

### 7.3 后台明确不做

- 审计日志页面
- 批量导入导出
- 邮件营销与用户触达系统
- 复杂数据分析大屏

---

## 8. 数据与权限模型

### 8.1 角色与状态

| 维度 | 可选值 | 说明 |
|------|--------|------|
| 角色 | user / admin | 普通用户 vs 后台管理员 |
| 状态 | pending / active / disabled | pending=未验证邮箱, active=正常, disabled=禁用 |

### 8.2 权限边界

| 能力 | user | admin |
|------|------|-------|
| 登录 App | ✅ 允许 | ✅ 允许 |
| 查看自己的 profile | ✅ 允许 | ✅ 允许 |
| 进入管理后台 | ❌ 不允许 | ✅ 允许（需 active） |
| 查看全部用户 | ❌ 不允许 | ✅ 允许 |
| 修改他人角色/状态 | ❌ 不允许 | ✅ 允许 |

### 8.3 用户表字段建议

| 字段 | 类型 | 说明 |
|------|------|------|
| id | UUID | 用户唯一标识 |
| email | VARCHAR | 邮箱（唯一） |
| password_hash | VARCHAR | bcrypt 加密密码 |
| display_name | VARCHAR | 昵称（可选） |
| avatar_url | TEXT | 头像链接（可留空） |
| role | ENUM | user / admin |
| status | ENUM | pending / active / disabled |
| created_at | TIMESTAMP | 注册时间 |
| updated_at | TIMESTAMP | 更新时间 |
| last_login_at | TIMESTAMP | 最后登录时间 |

### 8.4 其他表结构（概要）

**destinations（目的地）：**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR | 城市标识（如 "beijing"） |
| name | VARCHAR | 城市英文名 |
| name_cn | VARCHAR | 城市中文名 |
| description | TEXT | 城市描述 |
| image_url | TEXT | 头图链接 |
| tags | TEXT[] | 标签（history/food/culture） |
| must_see | JSONB | 必看景点列表 |
| must_eat | JSONB | 必吃列表 |
| stay_tips | TEXT | 住宿建议 |
| best_days | INT | 推荐停留天数 |
| budget_range | VARCHAR | 预算范围 |

**trips（行程）：**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | UUID | 行程标识 |
| user_id | UUID | 关联用户 |
| title | VARCHAR | 行程标题 |
| cities | TEXT[] | 城市列表 |
| days | INT | 行程天数 |
| content | JSONB | 行程详细内容 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

---

## 9. 设计系统（东方奢雅）

### 9.1 三要素

| 要素 | 值 | 说明 |
|------|-----|------|
| VISUAL_DENSITY | 3/10 | 留白优先，每屏不超过 5 个视觉焦点 |
| DESIGN_VARIANCE | 7/10 | 非对称布局，图片驱动 |
| MOTION_INTENSITY | 5/10 | 流畅微动效，滚动渐入 |

### 9.2 设计原则

1. **留白大于填充** — 宁可空，不可乱
2. **暗色即空间** — 深色是内容的原生画布
3. **金色不泛滥** — 仅用于品牌触点
4. **图片驱动** — 每个区段必须有真实图片
5. **维度层次** — 4 层深度明确
6. **禁止 AI 标配设计** — 无紫色渐变、无霓虹发光、无三卡等分
7. **一致性锁定** — 一个调色板贯穿全 App

### 9.3 色板系统

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

### 9.4 字体系统

| Role | Size | Weight | Line Height | Tracking |
|------|------|--------|-------------|----------|
| Display XL | 36sp | SemiBold | 1.1 | -0.5sp |
| Display Large | 28sp | SemiBold | 1.2 | -0.3sp |
| Headline | 22sp | Medium | 1.3 | -0.2sp |
| Subhead | 18sp | Medium | 1.4 | 0 |
| Body | 16sp | Regular | 1.6 | 0 |
| Body Small | 14sp | Regular | 1.5 | 0 |
| Caption | 12sp | Medium | 1.4 | 0.2sp |
| Tab Label | 11sp | Medium | 1.2 | 0.3sp |

### 9.5 间距与圆角

**间距：** xs(4) / sm(8) / md(12) / lg(16) / xl(24) / xxl(32) / section(48)

**圆角：** xs(4dp) / sm(8dp) / md(12dp) / lg(16dp) / xl(24dp)

**阴影：** 0(无) / 1(卡片) / 2(浮起卡片) / 3(弹窗) / 4(全屏Modal)

### 9.6 关键动效规范

| 交互 | 动效 | 参数 |
|------|------|------|
| Tab 切换 | cross-fade + 交错渐入 | 200ms |
| Tab 选中 | 颜色变化 + 轻微上浮 | 2dp up, 100ms |
| AI Token 渲染 | 打字机逐字 | 30ms/字 |
| City Detail 进入 | 图片放大 + 信息覆盖层上推 | shared element, 300ms |
| 保存行程 | 底部滑入确认 Toast | 250ms ease-out |
| 空态入场 | 插画渐入 + CTA 延迟出现 | 500ms stagger |
| 页面加载 | Shimmer 骨架屏（金色脉冲） | 1.5s loop |

---

## 10. 技术架构指引

### 10.1 三层架构概览

```
┌─────────────────────────────────────────────────────────────┐
│  Android App (Jetpack Compose + Kotlin)                     │
│  Home / Explore / City / Chat / Trips / Tools / Account     │
├─────────────────────────────────────────────────────────────┤
│  Web Admin (React + TypeScript)                              │
│  Login / Dashboard / User List / User Detail                 │
├─────────────────────────────────────────────────────────────┤
│                    Backend API (FastAPI + Python)             │
│  Auth / Destinations / Chat / Trips / Users / Admin          │
├─────────────────────────────────────────────────────────────┤
│                    Database (PostgreSQL)                      │
│  users / destinations / trips / sessions                     │
└─────────────────────────────────────────────────────────────┘
```

### 10.2 技术选型

**后端：**
| 组件 | 选择 | 理由 |
|------|------|------|
| 框架 | FastAPI (Python) | 异步、类型安全、自动 OpenAPI |
| 数据库 | PostgreSQL | 关系型、JSONB 支持 |
| ORM | SQLAlchemy + Alembic | 成熟、Migrations |
| 认证 | JWT (python-jose) + bcrypt | 无状态、轻量 |
| 邮件 | SMTP (标准协议) | 邮箱验证/重置密码 |
| 部署 | Docker + VPS/Vercel | 灵活 |

**Android App：**
| 组件 | 选择 | 理由 |
|------|------|------|
| UI 框架 | Jetpack Compose + Material3 | 现代声明式 |
| 导航 | Navigation Compose | 官方方案，类型安全 |
| 网络 | Retrofit + OkHttp + SSE | 成熟、流式支持 |
| 图片 | Coil 3 | Compose 原生，轻量 |
| 地图 | osmdroid | 零 API Key，离线可用 |
| 本地存储 | DataStore | 协程原生 |
| 认证 | JWT Token 本地存储 | 无状态 |

**Web Admin：**
| 组件 | 选择 | 理由 |
|------|------|------|
| 框架 | React + Vite | 轻量、快速 |
| UI | Tailwind CSS | 快速开发 |
| 状态 | React Query | 服务端状态管理 |
| 路由 | React Router | 成熟 |

### 10.3 API 接口规范

**认证：**
```
POST /api/auth/register      → 注册
POST /api/auth/login         → 登录
POST /api/auth/logout        → 登出
POST /api/auth/verify-email  → 邮箱验证
POST /api/auth/forgot-password → 忘记密码
POST /api/auth/reset-password → 重置密码
GET  /api/auth/me            → 获取当前用户信息
```

**用户（管理员）：**
```
GET    /api/admin/users        → 用户列表（分页+搜索+筛选）
GET    /api/admin/users/{id}   → 用户详情
PATCH  /api/admin/users/{id}   → 修改用户信息
GET    /api/admin/stats        → Dashboard 统计数据
```

**目的地：**
```
GET  /api/destinations       → 城市列表
GET  /api/destinations/{id}  → 城市详情
```

**Chat：**
```
POST /api/chat/message       → 发送消息（SSE 流式返回）
```

**行程：**
```
GET    /api/trips            → 获取我的行程列表
POST   /api/trips            → 创建/保存行程
DELETE /api/trips/{id}       → 删除行程
```

### 10.4 认证流程

```
注册流程：
  用户提交 email + password
  → 后端创建用户（status=pending）
  → 发送验证邮件（含验证链接/Token）
  → 用户点击链接 → 验证成功 → status=active
  → 返回 JWT Token

登录流程：
  用户提交 email + password
  → 后端验证凭据
  → 验证 status=active
  → 返回 JWT Token（access + refresh）

会话恢复：
  App 冷启动 → 读取本地 Token
  → 调用 GET /auth/me 验证是否有效
  → 有效：自动登录；无效：跳转登录页
```

### 10.5 Android App 架构

```kotlin
// 模块化结构
app/                    // 主模块（Application + 页面组装）
core/designsystem/      // 设计系统（Color/Typography/Spacing/Shape）
core/network/           // 网络层（Retrofit/OkHttp/SSE）
core/common/            // 通用工具（UiState、扩展函数）
feature/home/           // Home 页
feature/explore/        // Explore + Map
feature/city/           // City Detail
feature/chat/           // Chat（含 SSE）
feature/trips/          // Trips
feature/tools/          // Tools
feature/auth/           // Auth（Login/Register/Reset）
feature/account/        // Account
```

---

## 11. 非功能性要求

### 11.1 体验与可用性

- 关键页面必须具备加载态、错误态和重试入口
- Chat 必须始终感知"系统正在响应"，避免长时间无反馈
- 空状态必须提供下一步引导
- 所有列表页有空态覆盖
- 每个网络请求有错误重试

### 11.2 安全与合规

- 普通用户只能访问自己的资料信息
- 后台能力必须只对管理员开放，且管理员状态必须为 active
- 敏感密钥不得进入客户端
- 密码必须 bcrypt 加密
- JWT Token 应有过期时间

### 11.3 国际化与内容表达

- 核心文案与内容表达必须面向海外用户可理解
- 日期、货币、预算等信息展示应当考虑国际用户习惯
- App 界面默认英文

### 11.4 性能

| 指标 | 目标 |
|------|------|
| App 启动时间 | ≤ 2s（WiFi） |
| Chat 首字响应 | ≤ 1.5s |
| 存储 | DataStore 本地缓存 |
| 图片 | Coil 磁盘缓存 |
| APK 体积 | ≤ 25MB |

---

## 12. 指标与验收口径

### 12.1 核心体验指标

| 指标 | 说明 |
|------|------|
| 探索到详情转化 | 进入 Explore 后打开 City Detail 的比例 |
| 详情到对话转化 | 打开 City Detail 后进入 Chat 的比例 |
| 对话有效性 | 聊天会话平均轮次、建议问题点击率 |
| 承接与复访 | 被保存为 Trips 的比例、Trips 的二次访问率 |
| 工具使用 | Tools 入口点击率与单工具打开率 |

### 12.2 账号与后台指标

| 指标 | 说明 |
|------|------|
| 注册成功率 | 注册流程完成率 |
| 登录成功率 | 登录流程完成率 |
| 邮箱验证转化率 | pending → active 转化率 |
| 忘记密码成功率 | 重置密码流程完成率 |
| 后台可用性 | 管理员在用户列表完成搜索/筛选/编辑的成功率 |

### 12.3 交付验收清单

- [ ] Android App 完整核心路径可走通
- [ ] 邮箱注册/登录/登出/忘记密码/重置密码闭环完成
- [ ] Admin 后台可登录、查看用户列表、编辑用户
- [ ] Chat SSE 流式对话可用
- [ ] Trip 保存/查看/删除可用
- [ ] Map 展示城市标记
- [ ] 所有页面覆盖加载态/空态/错误态
- [ ] APK 可安装运行
- [ ] 设计一致性检查通过（截图对比 token）
