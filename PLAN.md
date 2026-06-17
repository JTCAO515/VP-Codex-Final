# VisePanda — 代码实现规划书

> **版本:** v1.0 | **日期:** 2026-06-17
> **策略:** 从零构建 | 多模型并行开发 | 分阶段交付
> **交付物:** Android App + Web 管理后台 + 后端 API

---

## 总览

### 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│  Android App (Jetpack Compose)      Web Admin (React+Vite)  │
│  ┌─ Home ─ Explore ─ City ─────┐   ┌─ Login ───────────┐  │
│  │  Chat ─ Trips ─ Tools ─ Acct│   │  Dashboard ───────│  │
│  └─────────────────────────────┘   │  UserList ─ Detail │  │
│      ↓ Retrofit/OkHttp ↓           └────────────────────┘  │
├─────────────────────────────────────────────────────────────┤
│               Backend API (FastAPI + Python)                 │
│  Auth │ Destinations │ Chat │ Trips │ Admin (Users/Stats)   │
├─────────────────────────────────────────────────────────────┤
│                    PostgreSQL Database                       │
│      users │ destinations │ trips │ sessions                 │
└─────────────────────────────────────────────────────────────┘
```

### 依赖关系链

```
Phase 0 ──── 后端基础设施（DB + Auth）
     │
     ↓
Phase 1 ──── 后端 API（所有接口）
     │
     ├──────────┬──────────┐
     ↓          ↓          ↓
Phase 2   Phase 3    Phase 4
Android    Android   Web Admin
Foundation  Screens   (独立)
(scaffold) (并行:    ↑
 design)    Home/     (依赖 Phase 1 API)
  ↑         Explore/
  │         Chat/
  │         Trips/
  │         Auth)
  │          │
  └──────────┘
     │
     ↓
Phase 5 ──── 集成 · 测试 · 部署
```

### 多模型并行策略

| 阶段 | 子代理数量 | 并行方式 |
|------|-----------|---------|
| Phase 0 | 1个 | 顺序执行（基础必须先行） |
| Phase 1 | 1~2个 | API 可拆分为 Auth + Business 两组并行 |
| Phase 2 | 2个 | Android Scaffold + Design System 并行 |
| Phase 3 | **4~5个并行** | 每个 Screen 一个子代理（Home/Explore/City/Chat/Trips） |
| Phase 4 | 1~2个 | Web Admin 可与 Phase 3 并行 |
| Phase 5 | 1个 | 集成串行 |

---

## Phase 0：后端基础设施

> **用时估计：** 1 个代理 × 1 轮
> **依赖：** 无（从零开始）
> **输出：** PostgreSQL Schema + FastAPI 骨架 + Auth 系统

### 任务详情

#### 0.1 数据库 Schema 设计 + Migrations

| 表 | 核心字段 | 索引 |
|----|---------|------|
| `users` | id(UUID), email(UNIQUE), password_hash, display_name, avatar_url, role(enum), status(enum), created_at, updated_at, last_login_at | email UNIQUE, role, status |
| `destinations` | id(VARCHAR PK), name, name_cn, description, image_url, tags(TEXT[]), must_see(JSONB), must_eat(JSONB), stay_tips, best_days(INT), budget_range | tags GIN |
| `trips` | id(UUID), user_id(FK→users), title, cities(TEXT[]), days(INT), content(JSONB), created_at, updated_at | user_id |
| `email_verifications` | id(UUID), user_id(FK→users), token, expires_at, used(BOOL) | token UNIQUE |
| `password_resets` | id(UUID), user_id(FK→users), token, expires_at, used(BOOL) | token UNIQUE |

**交付物：**
- `migrations/` 目录，Alembic 初始化
- `models.py` — SQLAlchemy ORM 模型
- `schemas.py` — Pydantic 请求/响应 Schema
- 初始化 SQL 脚本（seed 数据：28+ 城市）

#### 0.2 FastAPI 项目骨架

```
backend/
├── app/
│   ├── __init__.py
│   ├── main.py              # FastAPI app 入口
│   ├── config.py            # 环境变量配置
│   ├── database.py          # DB 连接 + Session
│   ├── models/
│   │   ├── __init__.py
│   │   ├── user.py
│   │   ├── destination.py
│   │   └── trip.py
│   ├── schemas/
│   │   ├── __init__.py
│   │   ├── auth.py
│   │   ├── user.py
│   │   ├── destination.py
│   │   ├── trip.py
│   │   └── admin.py
│   ├── api/
│   │   ├── __init__.py
│   │   ├── auth.py
│   │   ├── destinations.py
│   │   ├── chat.py
│   │   ├── trips.py
│   │   └── admin.py
│   ├── core/
│   │   ├── __init__.py
│   │   ├── security.py      # JWT + bcrypt
│   │   ├── deps.py          # 依赖注入
│   │   └── email.py         # 邮件发送
│   └── seed/
│       └── destinations.py  # 28+ 城市种子数据
├── requirements.txt
├── Dockerfile
├── docker-compose.yml       # PostgreSQL + App
└── alembic.ini
```

#### 0.3 认证系统

- JWT Token 生成/验证（`python-jose`）
- 密码 bcrypt 哈希
- 邮箱验证流程（发送验证邮件 + Token 校验）
- 忘记密码/重置密码流程
- Token 刷新机制

**交付物：**
- `core/security.py` — JWT + bcrypt 工具函数
- `core/email.py` — SMTP 邮件发送
- `api/auth.py` — 6 个认证端点
- `deps.py` — `get_current_user` 依赖注入

---

## Phase 1：后端 API 层

> **用时估计：** 2 个代理并行 × 1 轮
> **依赖：** Phase 0
> **输出：** 所有业务 API 端点

### 1A — 业务 API（1 个子代理）

| 端点 | 方法 | 功能 |
|------|------|------|
| `GET /api/destinations` | 列表 | 分页+搜索+标签筛选 |
| `GET /api/destinations/{id}` | 详情 | 城市详细信息 |
| `POST /api/chat/message` | SSE | 流式返回 AI 回复 |
| `GET /api/trips` | 列表 | 当前用户的行程 |
| `POST /api/trips` | 创建 | 保存行程 |
| `DELETE /api/trips/{id}` | 删除 | 删除行程 |

**Chat SSE 规范：**
```
POST /api/chat/message
Request: { "message": "...", "city_context": "beijing" }
Response: SSE stream
  event: message
  data: {"type": "token", "content": "北京"}
  data: {"type": "token", "content": "建议3天行程如下"}
  data: {"type": "itinerary", "content": {"days": [...]}}
  data: {"type": "image", "url": "...", "alt": "故宫"}
  data: {"type": "faq", "content": [{"q":"...","a":"..."}]}
  data: {"type": "done"}
```

### 1B — 管理后台 API（1 个子代理）

| 端点 | 方法 | 功能 |
|------|------|------|
| `GET /api/admin/stats` | 统计 | 用户总数/状态分布 |
| `GET /api/admin/users` | 列表 | 分页+邮箱搜索+角色/状态筛选 |
| `GET /api/admin/users/{id}` | 详情 | 用户完整信息 |
| `PATCH /api/admin/users/{id}` | 编辑 | 修改 display_name/role/status |

**安全约束：**
- 所有 `/api/admin/*` 端点必须验证 `role=admin` 且 `status=active`
- 通过 `deps.py` 的 `get_current_admin` 依赖注入

---

## Phase 2：Android App 骨架 + 设计系统

> **用时估计：** 2 个代理并行 × 1 轮
> **依赖：** Phase 0（需了解 API 结构）
> **输出：** 可编译的 Android 工程 + 完整设计系统

### 2A — Android 项目骨架

```
visepanda-android/
├── app/
│   ├── build.gradle.kts
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   └── java/com/visepanda/app/
│   │       ├── MainActivity.kt
│   │       ├── VisePandaApp.kt        # Application 类
│   │       ├── navigation/
│   │       │   ├── NavGraph.kt
│   │       │   └── Routes.kt          # 类型安全路由
│   │       └── ui/
│   │           └── theme/             # 主题（引用 Design System）
├── core/
│   ├── designsystem/                   # 见 2B
│   ├── network/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/com/visepanda/core/network/
│   │       ├── ApiClient.kt           # Retrofit 客户端
│   │       ├── SseClient.kt           # SSE 客户端
│   │       ├── api/
│   │       │   ├── AuthApi.kt
│   │       │   ├── DestinationApi.kt
│   │       │   ├── ChatApi.kt
│   │       │   ├── TripApi.kt
│   │       │   └── AdminApi.kt
│   │       └── model/
│   │           ├── UserDto.kt
│   │           ├── DestinationDto.kt
│   │           ├── TripDto.kt
│   │           └── ChatMessageDto.kt
│   └── common/
│       ├── build.gradle.kts
│       └── src/main/java/com/visepanda/core/common/
│           ├── UiState.kt              # sealed class
│           ├── Result.kt               # Result wrapper
│           └── extensions.kt
├── feature/                            # 空壳模块（Phase 3 填充）
├── build.gradle.kts                    # 根构建文件
├── settings.gradle.kts                 # 模块注册
├── gradle.properties
└── gradle/                             # Gradle wrapper
```

**技术栈版本：**
- AGP 8.2+
- Kotlin 1.9+
- Compose BOM 2024.02+
- Min SDK 26, Target SDK 34
- Retrofit 2.9+
- OkHttp 4.12+
- Coil 3+
- Navigation Compose 2.7+

**交付物：**
- 完整 Gradle 多模块配置
- `core/network/` — Retrofit 接口定义（含 Mock 模式）
- `core/common/` — UiState sealed class
- `app/navigation/` — 路由 + Bottom Navigation 骨架

### 2B — 设计系统模块

```
core/designsystem/
├── build.gradle.kts
└── src/main/java/com/visepanda/core/designsystem/
    ├── color/
    │   ├── VpColor.kt          # 14 个语义化颜色常量
    │   └── VpColorScheme.kt    # Compose ColorScheme
    ├── typography/
    │   └── VpTypography.kt     # 8 级字形
    ├── spacing/
    │   └── VpSpacing.kt        # 7 级间距
    ├── shape/
    │   └── VpShape.kt          # 5 级圆角
    ├── component/
    │   ├── VpButton.kt         # 3 种样式（Primary/Secondary/Text）
    │   ├── VpChip.kt           # 标签组件
    │   ├── VpCard.kt           # 卡片组件
    │   ├── VpBottomNav.kt      # 底部导航
    │   ├── VpSectionHeader.kt  # 区段标题
    │   └── VpShimmer.kt        # 骨架屏
    └── theme/
        └── VisePandaTheme.kt    # 全局主题组合
```

**交付物：**
- 14 个语义化颜色常量（全暗色适配）
- 8 级字体系统
- 7 级间距刻度
- 5 级圆角 + 4 级阴影
- 6 个核心 Compose 组件
- 全局 VisePandaTheme

---

## Phase 3：Android App 屏幕开发

> **用时估计：** 5 个子代理并行 × 1-2 轮
> **依赖：** Phase 2（设计系统 + 网络层）
> **输出：** 所有 App 屏幕代码

### 并行任务分配

```
3A — Home Screen         ═══ 子代理 A
3B — Explore + Map       ═══ 子代理 B
3C — City Detail         ═══ 子代理 C
3D — Chat (SSE)          ═══ 子代理 D
3E — Trips + Tools       ═══ 子代理 E
3F — Auth + Account      ═══ 子代理 F
```

### 3A：Home Screen

```
feature/home/
├── build.gradle.kts
└── src/main/java/com/visepanda/feature/home/
    ├── HomeScreen.kt         # 主 Composable
    ├── HomeViewModel.kt      # 状态管理
    └── components/
        ├── HeroSection.kt    # 品牌+标语+CTA
        ├── FeaturedCities.kt # 精选城市 LazyRow
        └── AiEntryCard.kt    # "Chat with Panda" 入口
```

**关键交互：**
- Hero 区品牌展示 + "Plan Your Trip" CTA → Chat
- 精选城市横向滚动 → City Detail
- "Chat with Panda" 卡片 → Chat
- Shimmer 加载 → 真实数据 → 错误重试

### 3B：Explore + Map

```
feature/explore/
├── build.gradle.kts
└── src/main/java/com/visepanda/feature/explore/
    ├── ExploreScreen.kt        # 视图切换（卡片/地图）
    ├── ExploreViewModel.kt
    ├── CityGrid.kt             # LazyVerticalGrid
    └── components/
        ├── CityCard.kt         # 卡片组件（图片+名称+标签）
        ├── MapView.kt          # osmdroid 封装
        └── ViewSwitcher.kt     # 卡片/地图切换
```

**关键交互：**
- 城市卡片网格（2列），图片+城市名+标签
- 地图视图，osmdroid 城市标记
- 点击标记弹出 Info Window → City Detail
- 卡片/地图切换动画

### 3C：City Detail

```
feature/city/
├── build.gradle.kts
└── src/main/java/com/visepanda/feature/city/
    ├── CityDetailScreen.kt
    ├── CityDetailViewModel.kt
    └── components/
        ├── HeroImage.kt        # 全宽头图
        ├── InfoSection.kt      # 城市信息
        ├── MustSeeSection.kt   # 必看景点
        ├── MustEatSection.kt   # 必吃
        ├── StaySection.kt      # 住宿
        └── PlanCta.kt          # "Plan my trip" CTA
```

**关键交互：**
- 头图全宽+24dp底圆角
- 信息分区：Must-see / Must-eat / Stay / Tips
- CTA → Chat 带城市上下文
- 进入动效（shared element transition）

### 3D：Chat (SSE)

```
feature/chat/
├── build.gradle.kts
└── src/main/java/com/visepanda/feature/chat/
    ├── ChatScreen.kt
    ├── ChatViewModel.kt        # SSE 流式处理
    └── components/
        ├── MessageList.kt      # 反向列表
        ├── UserBubble.kt       # 用户消息（右对齐）
        ├── AiBubble.kt         # AI 消息（左对齐+流式）
        ├── ItineraryBlock.kt   # 行程卡片（金色边框）
        ├── RecommendationCard.kt # 推荐横向
        ├── FaqBlock.kt         # FAQ 折叠
        ├── WelcomeChips.kt     # 预设问题
        └── InputBar.kt         # 输入框+发送按钮
```

**关键交互：**
- SSE 流式接收 → token 逐字渲染（30ms/字）
- Itinerary Block 结构化渲染
- 消息自动保存到 Trips
- 预设问题 chips（点选即发）

### 3E：Trips + Tools

```
feature/trips/
├── build.gradle.kts
└── src/main/java/com/visepanda/feature/trips/
    ├── TripsScreen.kt
    ├── TripsViewModel.kt
    ├── TripCard.kt
    └── EmptyTrips.kt           # 空态引导

feature/tools/
├── build.gradle.kts
└── src/main/java/com/visepanda/feature/tools/
    ├── ToolsScreen.kt
    ├── ToolItem.kt
    └── pages/
        ├── PaymentGuide.kt
        ├── VisaGuide.kt
        ├── SimGuide.kt
        ├── EmergencyGuide.kt
        ├── EtiquetteGuide.kt
        └── ChinesePhrases.kt
```

**关键交互：**
- Trips：按时间倒序，空态引导 → Chat/Explore
- Trips：左滑删除确认
- Tools：图标矩阵入口，点击展开内容页

### 3F：Auth + Account

```
feature/auth/
├── build.gradle.kts
└── src/main/java/com/visepanda/feature/auth/
    ├── LoginScreen.kt
    ├── RegisterScreen.kt
    ├── ForgotPasswordScreen.kt
    ├── ResetPasswordScreen.kt
    ├── AuthViewModel.kt
    └── AuthTokenManager.kt     # JWT 本地存储

feature/account/
├── build.gradle.kts
└── src/main/java/com/visepanda/feature/account/
    ├── AccountScreen.kt        # 已登录/未登录状态
    └── AccountViewModel.kt
```

**关键交互：**
- 注册流程：邮箱+密码+昵称 → 发送验证邮件
- 登录：邮箱+密码 → JWT 本地存储 → 会话恢复
- 忘记密码：邮箱 → 重置邮件 → 深链回跳
- Account：未登录显示入口，已登录显示信息+登出

---

## Phase 4：Web 管理后台

> **用时估计：** 1-2 个子代理并行 × 1 轮
> **依赖：** Phase 0-1（需后端 API）
> **可与 Phase 2-3 并行执行**
> **输出：** 完整的管理后台 Web 应用

### 项目结构

```
visepanda-admin/
├── package.json
├── vite.config.ts
├── tsconfig.json
├── index.html
├── public/
│   └── favicon.svg
├── src/
│   ├── main.tsx
│   ├── App.tsx
│   ├── api/
│   │   ├── client.ts           # Axios 客户端
│   │   ├── auth.ts             # 登录 API
│   │   └── users.ts            # 用户管理 API
│   ├── pages/
│   │   ├── LoginPage.tsx
│   │   ├── DashboardPage.tsx
│   │   ├── UserListPage.tsx
│   │   └── UserDetailPage.tsx
│   ├── components/
│   │   ├── Layout.tsx          # 侧边栏+顶栏
│   │   ├── StatCard.tsx
│   │   ├── UserTable.tsx
│   │   ├── UserFilters.tsx
│   │   └── StatusBadge.tsx
│   ├── hooks/
│   │   ├── useAuth.ts
│   │   └── useUsers.ts
│   ├── types/
│   │   └── index.ts
│   └── styles/
│       └── global.css
├── Dockerfile
└── vercel.json
```

### 设计风格

- 匹配 VisePanda 品牌色（暗色背景 + 金色点缀）
- 简洁、专业的 Admin Dashboard 风格
- 响应式：桌面为主 + 平板可用

### 页面详述

**Login：**
- 邮箱+密码登录
- 错误提示（"账号不存在"/"密码错误"/"非管理员"）
- 登录后跳转 Dashboard

**Dashboard：**
- 4 个 StatCard：总用户 / Active / Pending / Disabled
- 简洁、一目了然

**User List：**
- 表格展示：Email / Display Name / Role / Status / Created
- 搜索框：按邮箱搜索
- 筛选：Role 下拉 / Status 下拉
- 点击行 → User Detail
- 分页

**User Detail：**
- 展示全部字段
- 编辑模式：修改 display_name / role / status
- 保存/取消按钮

---

## Phase 5：集成 · 测试 · 部署

> **用时估计：** 1 个子代理 × 1 轮
> **依赖：** Phase 0-4 全部完成
> **输出：** 可运行的完整系统

### 5.1 集成验证

- [ ] 后端启动 → 数据库连接成功
- [ ] 所有 API 端点可调用
- [ ] Android App → 后端通信正常
- [ ] Admin Web → 后端通信正常
- [ ] 邮箱验证邮件发送正常
- [ ] SSE Chat 流式输出正常

### 5.2 核心 E2E 测试

| # | 场景 | 步骤 | 预期 |
|---|------|------|------|
| 1 | 首次启动 | 打开 App | Home 加载，显示品牌+城市 |
| 2 | 浏览城市 | 点击城市卡片 | 跳转 City Detail，完整信息 |
| 3 | 地图探索 | 切换到地图视图 | 城市标记显示 |
| 4 | AI 对话 | "Plan 3 days in Beijing" | SSE 流式输出行程 |
| 5 | 保存行程 | AI 回复→查看 Trips | 行程自动出现 |
| 6 | 注册流程 | 注册→验证→登录 | 完整闭环 |
| 7 | 忘记密码 | 申请重置→邮件→重置→登录 | 完整闭环 |
| 8 | 断网韧性 | 关闭网络→操作 App | 显示断网提示，不崩溃 |
| 9 | 后台登录 | 管理员登录 | Dashboard 显示数据 |
| 10 | 后台管理 | 搜索用户→编辑→保存 | 操作成功 |

### 5.3 部署配置

**后端：**
- Docker Compose（PostgreSQL + FastAPI）
- CORS 配置（允许 App + Admin 域名）
- 环境变量：DB_URL / JWT_SECRET / SMTP_CONFIG

**Android App：**
- GitHub Actions assembleRelease
- API 地址通过 BuildConfig 注入（debug=local / release=production）

**Web Admin：**
- Vercel 部署（`npm run build` → `vercel --prod`）
- API 地址通过环境变量注入

---

## 执行流程总图

```
Week 1                    Week 2                    Week 3
┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐
│ Phase 0: 基础设施  │    │ Phase 2: Android  │    │ Phase 4: Web Admin│
│ DB + FastAPI      │    │ 骨架+设计系统      │    │ (可提前与P2/P3并行)│
│ Auth 系统         │    │                   │    │                  │
│                   │    │ Phase 3A: Home    │    │ Phase 5: 集成测试 │
│ Phase 1: API 层   │    │ Phase 3B: Explore │    │ 部署+发布         │
│ 业务API+Admin API │    │ Phase 3C: City    │    │                  │
└──────────────────┘    │ Phase 3D: Chat    │    └──────────────────┘
                        │ Phase 3E: Trips   │
                        │ Phase 3F: Auth    │
                        └──────────────────┘

并行阶段：
  Phase 0-1 → Phase 2-3 (并行: 2A+2B → 3A~3F)
             → Phase 4 (与 Phase 3 并行)
             → Phase 5 (串行收尾)
```

### 执行方式

每个 Phase 通过 `delegate_task` 分配给子代理，子代理在自己的隔离上下文中完成所有编码工作。

子代理交付后，主代理：
1. 检查关键文件是否存在
2. 运行编译/测试
3. 合并到主分支
4. 启动下一个 Phase

---

## 附录：子代理 Prompt 模板

### 每个子代理接收的标准上下文

```
## 项目背景
VisePanda 是一款面向来华外国游客的旅行决策 App + 管理后台。
技术栈：FastAPI(Python) + PostgreSQL + Jetpack Compose(Kotlin) + React(TypeScript)

## 本次任务
{具体模块名称} — {具体描述}

## 约束
- 从零开始，不要假设任何已有代码或数据库
- 所有代码写英文，注释优先中文
- Compose 组件以 Vp 前缀命名
- 设计 token 引用 PRODUCT_HANDOFF.md §9
- API 定义参考 PRODUCT_HANDOFF.md §10.3

## 交付标准
- 编译通过
- 所有函数有类型注解
- 模块有 README 说明
- 不引入冗余依赖
```
