# VisePanda v0.2.0 — Handoff Document

> **Last Updated:** 2026-06-19
> **Status:** 🟢 合并完成 — Android 代码更新至 v0.2.6（visepanda-android-hermes），backend + admin 保留
> **Repo:** `git@github.com:JTCAO515/vp-hermes-APK.git` (master)
> **Live URL:** https://hermesapp.go2china.space (Vercel前端 + /api/* → VPS后端)
> **Agent Memory Key:** `vp-hermes`, `VisePanda`, `hermesapp.go2china.space`

---

## 1. Product Overview

**VisePanda** — 面向来华外国游客的 AI 旅行助手。核心价值：让一个对中国不熟悉的海外用户，在 10 分钟内从「我想去中国但不知道怎么选」推进到「我知道去哪、怎么玩、需要准备什么」。

产品体系包含三层：
- **Android 原生客户端**（Jetpack Compose）— 主用户入口，覆盖 Home/City/Chat/Trips/Tools
- **Web 管理后台**（React + Vite + Tailwind）— 用户管理
- **后端 API**（FastAPI + PostgreSQL）— 数据服务 + SSE 流式 AI 对话

> 另有独立产品 **VisePanda v3** (`vise-panda-2` 仓库) 部署在 `www.go2china.space`，是纯 Python stdlib 的全栈 SPA，与本 Android 原生版是不同的实现。

## 2. Architecture

```
┌─ Android Client ──────────────┐
│  Jetpack Compose + Kotlin      │
│  MVVM + Retrofit + OkHttp      │
│  SSE via okhttp-sse            │
└──────────┬─────────────────────┘
           │ HTTPS /api/*
           ▼
┌─ Vercel (hermesapp.go2china.space) ──┐
│  React Admin Panel (Vite)             │
│  vercel.json: rewrite /api/* → VPS   │
└──────────┬────────────────────────────┘
           │ proxy /api/*
           ▼
┌─ VPS (122.51.121.116:8001) ─────────┐
│  FastAPI (uvicorn, Docker)           │
│  PostgreSQL 16 (Docker)              │
│  SQLAlchemy + Alembic                │
│  Routes: /api/auth /api/destinations │
│          /api/chat  /api/trips       │
│          /api/admin                  │
└──────────────────────────────────────┘
```

### Key Design Decisions

| 决策 | 选择 | 为何 |
|------|------|------|
| Android UI | Jetpack Compose | 现代声明式 UI，与设计系统对接 |
| 后端 | FastAPI + Docker | 异步 SSE，PostgreSQL |
| Android 来源 | visepanda-android-hermes (v0.2.6) | 更完整的屏幕实现 + 工作 APK |
| 管理后台 | React + Vite + pnpm | Vercel 部署 |
| AI | DeepSeek V4 Flash (SSE) | 成本低，中文旅行内容好 |

## 3. Current State

### ✅ 已完成

**后端 API (Docker + FastAPI + PostgreSQL)**
- [x] 认证系统：注册/登录/登出/邮箱验证/忘记/重置密码
- [x] 目的地 API：城市列表 + 详情
- [x] Chat API：SSE 流式对话（DeepSeek V4 Flash）
- [x] Trip CRUD：保存/查看/删除行程
- [x] 管理员 API：用户列表/详情/编辑
- [x] Docker Compose 编排：backend-api + PostgreSQL
- [x] 种子数据脚本（seed.py）

**管理后台 (Vercel)**
- [x] 管理员登录（JWT）
- [x] Dashboard：用户总数 + 状态分布 + 趋势
- [x] 用户列表：搜索 + 筛选 + 分页
- [x] 用户详情：查看 + 编辑 role/status
- [x] Vercel auto-deploy on push

**Android 客户端 (Jetpack Compose) — 从 visepanda-android-hermes v0.2.6 合并**
- [x] Home/Chat/City/Map/Tools/Trips 全部屏幕 + ViewModel
- [x] SSE 流式聊天 + Markdown 渲染
- [x] Map 页面（osmdroid 中国全览 + 36城市标记）
- [x] Tools 页面（工具箱网格）
- [x] Trips DataStore 本地持久化
- [x] API 地址：`hermesapp.go2china.space`
- [x] GitHub Actions CI：自动编译 APK

### ❌ 待完成

- **API 端点对齐** — Android 需 `/api/cities`、`/api/map`、`/api/tools`、`/api/config`，后端目前只有 `/api/destinations`

### 🐛 已知问题

1. **API 端点不匹配** — Android 代码使用 `/api/cities`、`/api/map`、`/api/tools`、`/api/config`，vp-hermes FastAPI 后端只有 `/api/destinations`。需对齐。
2. VPS 只开放了 22/5432/8001 端口，新增服务需手动开安全组
3. JWT_SECRET 当前为硬编码，生产需改为环境变量

## 4. File Structure

```
vp-hermes-APK/
├── android/                      # Android 原生客户端（从 visepanda-android-hermes 合并，v0.2.6）
│   ├── app/src/
│   │   ├── main/java/space/jtcao/visepanda/
│   │   │   ├── data/api/          # ApiConfig, VisePandaApi, SseClient
│   │   │   ├── data/model/        # ApiModels, ChatEvent, City, ToolData, Trip
│   │   │   ├── data/repository/   # Chat/City/Map/Tools/Trip Repository
│   │   │   ├── ui/home/           # HomeScreen + ViewModel
│   │   │   ├── ui/chat/           # ChatScreen + ViewModel (SSE)
│   │   │   ├── ui/cities/         # CityScreen + ViewModel
│   │   │   ├── ui/map/            # MapScreen + ViewModel (osmdroid)
│   │   │   ├── ui/tools/          # ToolsScreen + ViewModel
│   │   │   ├── ui/trips/          # TripsScreen + ViewModel
│   │   │   ├── ui/components/     # MarkdownText
│   │   │   ├── ui/navigation/     # NavGraph, BottomNavBar, Routes
│   │   │   └── ui/theme/          # Color, Theme, Type
│   │   └── res/                   # 资源文件
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── gradle/                    # Gradle wrapper
│
├── backend/                      # FastAPI 后端
│   ├── app/
│   │   ├── main.py               # 入口 + 路由注册
│   │   ├── api/                  # auth/destinations/chat/trips/admin
│   │   ├── models/               # SQLAlchemy 模型
│   │   ├── schemas/              # Pydantic 请求/响应
│   │   ├── core/                 # config/database/auth 依赖
│   │   └── seed/                 # seed.py
│   ├── docker-compose.yml        # backend-api + PostgreSQL 编排
│   ├── Dockerfile
│   └── requirements.txt
│
├── admin/                        # React 管理后台
│   ├── src/
│   │   ├── pages/                # Dashboard/UserList/UserDetail/Login
│   │   ├── components/           # 通用组件
│   │   └── api/                  # API 调用
│   ├── vercel.json               # Vercel 部署配置
│   └── package.json              # pnpm
│
├── .github/workflows/
│   └── android-build.yml         # GitHub Actions APK 编译
├── PRODUCT_HANDOFF.md            # 产品设计文档（v2.0）
├── PLAN.md                       # 迭代规划
└── HANDOFF.md                    # 本文件
```

## 5. API / Interface

所有 API 路径以 `/api/` 为前缀，通过 Vercel (`hermesapp.go2china.space`) 代理到 VPS (:8001)。

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/auth/register` | 注册 | 否 |
| POST | `/api/auth/login` | 登录，返回 JWT | 否 |
| POST | `/api/auth/logout` | 登出 | 是 |
| POST | `/api/auth/verify-email` | 邮箱验证 | 否 |
| POST | `/api/auth/forgot-password` | 忘记密码 | 否 |
| POST | `/api/auth/reset-password` | 重置密码 | 否 |
| GET | `/api/auth/me` | 当前用户信息 | 是 |
| GET | `/api/destinations?search=&tag=&page=` | 城市列表 | 否 |
| GET | `/api/destinations/{id}` | 城市详情 | 否 |
| POST | `/api/chat/message` | SSE 流式对话 | 是 |
| GET | `/api/trips` | 行程列表 | 是 |
| POST | `/api/trips` | 创建行程 | 是 |
| DELETE | `/api/trips/{id}` | 删除行程 | 是 |
| GET | `/api/admin/users?search=&role=&status=&page=` | 用户列表 | admin |
| GET | `/api/admin/users/{id}` | 用户详情 | admin |
| PATCH | `/api/admin/users/{id}` | 编辑用户 | admin |
| GET | `/api/admin/stats` | 用户统计 | admin |

## 6. Key Config

### 环境变量（后端 .env / Docker）
```
DATABASE_URL=postgresql+asyncpg://postgres:postgres@db:5432/visepanda
JWT_SECRET=[在 .env 中配置，当前为硬编码占位符]
JWT_ALGORITHM=HS256
ACCESS_TOKEN_EXPIRE_MINUTES=10080  (7天)
DEEPSEEK_API_KEY=[在 .env 中配置]
DEEPSEEK_MODEL=deepseek-v4-flash
```

### Vercel 配置
- 项目：`vp-hermes`（GitHub 自动同步）
- 域名：`hermesapp.go2china.space`
- Frontend: `admin/` 目录（Vite React SPA）
- Rewrites: `/api/*` → VPS `:8001`
- 无环境变量需要配置（纯静态前端）

### 安全组（腾讯云 122.51.121.116）
```
22/tcp   → SSH
5432/tcp → PostgreSQL
8001/tcp → FastAPI 后端
```

## 7. Core Logic / Data Flow

### 用户认证流程
```
Register → pending status → 存储密码_hash+salt → 返回用户信息
Login    → 验证密码 → 生成 JWT (7天) → 返回 access_token
API Call → Authorization: Bearer <token> → 验证 JWT → 获取当前用户
```

### SSE Chat 流式对话
```
Android App → POST /api/chat/message {message, city_context?}
  → FastAPI → DeepSeek API (SSE stream)
  → okhttp-sse EventSourceListener
  → Flow<SseEvent> (Token/Itinerary/Image/FAQ/Done/Error)
  → Compose UI 逐字渲染
```

### App 导航结构
```
BottomNav: Home → Explore → Chat → Trips → [Account in Drawer]
未登录: 拦截到 Welcome → Login/Register/ResetPassword
```

## 8. Frontend / UI Component Map

### Android 页面结构
```
MainActivity
├── NavHost
│   ├── Welcome (未登录)
│   │   ├── LoginScreen
│   │   ├── RegisterScreen
│   │   └── ResetPasswordScreen
│   ├── MainScaffold (已登录, BottomNav)
│   │   ├── HomeScreen
│   │   ├── ExploreScreen
│   │   │   └── CityDetailScreen
│   │   ├── ChatScreen
│   │   ├── TripsScreen
│   │   └── ToolsScreen
│   └── AccountScreen
```

### Web 管理后台
```
AdminPanel (React Router)
├── LoginPage
├── DashboardPage (stats)
├── UserListPage (search/filter/paginate)
│   └── UserDetailPage (view + edit)
```

## 9. Dependencies

### Android
| 依赖 | 版本 | 用途 |
|------|------|------|
| Kotlin | 1.9.22 | 语言 |
| AGP | 8.2.2 | Android 构建 |
| Compose BOM | 2024.02.00 | UI 框架 |
| Compose Compiler | 1.5.10 | Compose 编译器 |
| Gradle | 8.12 | 构建系统 |
| Retrofit | 2.9.0 | HTTP 客户端 |
| OkHttp | 4.12.0 | HTTP 底层 + SSE |
| Coil | 3.0.0-alpha10 | 图片加载 |

### Backend
| 依赖 | 版本 | 用途 |
|------|------|------|
| Python | 3.11 | 语言 |
| FastAPI | latest | Web 框架 |
| SQLAlchemy | 2.x | ORM |
| Alembic | latest | 数据库迁移 |
| PostgreSQL | 16 | 数据库 |
| Docker | latest | 容器化 |

### Admin
| 依赖 | 版本 | 用途 |
|------|------|------|
| React | 19 | 前端框架 |
| Vite | latest | 构建工具 |
| Tailwind | latest | CSS 框架 |
| pnpm | latest | 包管理 |

## 10. Next Steps

### 🔴 高优先级
- **🎯 APK 编译** — `git pull` 后运行：`gradlew.bat assembleDebug`
- 测试生产环境 API 连通性（手机装 APK 后验证 Chat+Map+Trips）

### 🟡 中优先级
- VPS 上配置 `JWT_SECRET` 为环境变量（移除硬编码）
- GitHub Actions 触发 APK 自动编译验证

### 🟢 低优先级
- 更新 Gradle 至 8.13+ 和 AGP 至更高版本

## 11. Troubleshooting

### APK 编译失败
| 问题 | 原因 | 解决 |
|------|------|------|
| `MediaType.parse()` deprecated | OkHttp 4.x API 变更 | 已修复：改为 `toMediaTypeOrNull()` |
| `Icons.Filled.Chat` 警告 | Compose Icons 新版变更 | warning，不影响编译 |
| Gradle 下载慢 | 国内网络限制 | 用腾讯云镜像：腾讯云服务器上可配 `gradle.properties` |
| GitHub Actions 构建失败 | OkHttp 弃用 API | 已修复，需重新触发 |

### 后端问题
| 问题 | 原因 | 解决 |
|------|------|------|
| `FUNCTION_INVOCATION_FAILED` | Vercel 只读文件系统 | 已修复：`vise-panda-2` 项目 `/tmp/` 改用 |
| JWT 使用硬编码 key | 未配置环境变量 | 在 VPS `.env` 中设置 `JWT_SECRET` |
| Docker 容器占用内存 | `open-notebook` + `surrealdb` | `docker stop` 不用的容器可释放 ~800MB |

## 12. References

- `PRODUCT_HANDOFF.md` — 完整产品设计文档（v2.0，714行，含信息架构/设计系统/数据库模型）
- `PLAN.md` — 迭代规划
- `README.md` — 简版说明
- 管理后台 Vercel 域名：https://hermesapp.go2china.space/
- 后端 VPS：122.51.121.116:8001
- GitHub：https://github.com/JTCAO515/vp-hermes
- 兄弟项目（不同实现）：https://www.go2china.space（vise-panda-2，纯 Python stdlib SPA）

---

*End of Handoff.*
