# VisePanda Native — 用户系统产品规划

> 版本: v1 | 日期: 2026-06-17 | 状态: 待评审

---

## 一、为什么要做

### 当前问题

VisePanda Native (vp-hermes) 目前是一个**无用户状态的应用**：
- 打开即用，任何人都能使用 AI 旅行助手
- 行程（Trips）数据只存在本地，无法跨设备同步
- 没有用户画像，AI 回复无法个性化
- 管理者无法看见谁在使用、使用情况如何

### 这次先做什么 / 不做什么

| 做 ✅ | 不做 ❌ |
|-------|---------|
| 邮箱 + 密码注册登录 | 第三方登录（微信/Google）|
| Token 鉴权 | OAuth 2.0 完整流程 |
| 用户管理后台（Web） | 数据分析/统计面板 |
| Android 登录界面 | iOS 版本 |
| 自动注册首用户为管理员 | 角色权限分层管理 |

### 核心目标

1. **用户可注册/登录** — 用邮箱就能创建账号
2. **App 有登录墙** — 未登录不能使用核心功能
3. **后台可管理用户** — 查看用户列表、删除用户
4. **为后续用户系统打地基** — 个性推荐、行程云端同步、收藏列表都依赖用户体系

---

## 二、产品流程

### 用户旅程

```
打开 App
    │
    ▼
┌─────────────┐      ┌───────────────┐
│  登录界面    │──────│  注册界面      │
│  邮箱+密码    │      │  邮箱+密码+确认  │
└──────┬──────┘      └───────┬───────┘
       │                     │
       └──────────┬──────────┘
                  ▼
          ┌──────────────┐
          │  Token 本地存储 │
          └──────┬───────┘
                 ▼
          ┌──────────────┐
          │   主页面      │
          │  Home/Explore │
          │  Chat/Trips   │
          └──────────────┘
```

### 后台管理流程

```
管理员登录后台（Web）
    │
    ▼
┌───────────────────┐
│  用户列表          │
│  - 邮箱 / 注册时间 │
│  - 角色(Admin/User)│
│  - 操作(删除)      │
└───────────────────┘
```

---

## 三、技术架构

### 整体架构

```
┌─────────────────────┐      ┌──────────────────────┐
│  Android App        │      │  Web Admin Panel      │
│  ┌───────────────┐  │      │  ┌────────────────┐   │
│  │ LoginScreen    │──┼──────┼─┤ /admin.html     │   │
│  │ TokenStorage   │  │      │  └────────────────┘   │
│  │ ApiClient+Auth │  │      └──────────┬───────────┘
│  └───────────────┘  │                 │
└──────────┬──────────┘                 │
           │                            │
           ▼                            ▼
    ┌────────────────────────────────────────┐
    │  Backend (go2china.space / Vercel)     │
    │  ┌──────────────────────────────────┐  │
    │  │ POST /api/auth/register          │  │
    │  │ POST /api/auth/login             │  │
    │  │ GET  /api/auth/me                │  │
    │  │ GET  /api/admin/users            │  │
    │  │ DEL  /api/admin/users/:id        │  │
    │  └──────────────────────────────────┘  │
    │  ┌──────────────────────────────────┐  │
    │  │ SQLite: data/users.db            │  │
    │  │  users (id, email, pwd_hash,    │  │
    │  │         role, created_at)        │  │
    │  │  sessions (token, user_id,       │  │
    │  │            expires_at)           │  │
    │  └──────────────────────────────────┘  │
    └────────────────────────────────────────┘
```

### 数据模型

**users 表:**
```json
{
  "id": "uuid",
  "email": "user@example.com",
  "password_hash": "sha256(密码+salt)",
  "role": "user | admin",        // 第一个注册的自动成为 admin
  "created_at": "ISO8601",
  "updated_at": "ISO8601"
}
```

**sessions 表:**
```json
{
  "token": "uuid (随机生成)",
  "user_id": "uuid (外键)",
  "created_at": "ISO8601",
  "expires_at": "ISO8601 (7天后)"
}
```

### API 设计

| 端点 | 方法 | 说明 | 鉴权 |
|------|------|------|------|
| `/api/auth/register` | POST | 注册新用户 | 无 |
| `/api/auth/login` | POST | 登录，返回 token | 无 |
| `/api/auth/me` | GET | 获取当前用户信息 | Bearer Token |
| `/api/admin/users` | GET | 用户列表（管理员）| Bearer Token + admin |
| `/api/admin/users/:id` | DELETE | 删除用户（管理员）| Bearer Token + admin |

### 密码安全方案

- 密码哈希使用 `hashlib.sha256(password + salt).hexdigest()`
- Salt 每个用户独立（uuid）
- Token 使用 `uuid.uuid4().hex` (64字符 hex)
- Token 有效期 7 天

---

## 四、Android 界面设计

### 登录界面

```
┌──────────────────────────┐
│                          │
│      [V Logo]            │
│                          │
│   Welcome to VisePanda   │
│   Sign in to start your  │
│   China travel journey   │
│                          │
│   ┌──────────────────┐   │
│   │ ✉️ Email          │   │
│   └──────────────────┘   │
│   ┌──────────────────┐   │
│   │ 🔒 Password       │   │
│   └──────────────────┘   │
│                          │
│   ┌──────────────────┐   │
│   │    Sign In        │   │
│   └──────────────────┘   │
│                          │
│   Don't have an account? │
│        Create One        │
└──────────────────────────┘
```

### 注册界面

```
┌──────────────────────────┐
│                          │
│      Create Account      │
│                          │
│   ┌──────────────────┐   │
│   │ ✉️ Email          │   │
│   └──────────────────┘   │
│   ┌──────────────────┐   │
│   │ 🔒 Password       │   │
│   └──────────────────┘   │
│   ┌──────────────────┐   │
│   │ 🔒 Confirm Pass   │   │
│   └──────────────────┘   │
│                          │
│   ┌──────────────────┐   │
│   │   Create Account  │   │
│   └──────────────────┘   │
│                          │
│   Already have an acco.. │
│        Sign In           │
└──────────────────────────┘
```

---

## 五、后端 Admin Panel (Web)

一个极简的后台页面，放在 `go2china.space/admin`：
- 管理员邮箱/密码登录（复用 auth API）
- 用户列表表格：邮箱 / 角色 / 注册时间 / 操作
- 删除用户（带确认弹窗）
- 无需额外框架 — 纯 HTML + CSS + JS（跟现有项目一样）

---

## 六、迭代计划

### Phase 1: 后端 Auth API (约 30min)
- 创建 `api/auth.py`：数据库初始化 + 注册/登录/鉴权
- 接入 `index.py` 路由表
- 部署验证 API 可用

### Phase 2: Admin Panel (约 20min)  
- 在 `web/` 创建 `admin.html`
- 管理员登录 → 用户列表 → 删除操作
- 部署验证

### Phase 3: Android 登录界面 (约 30min)
- 创建 `ui/auth/LoginScreen.kt`
- 创建 `data/AuthRepository.kt` (Token 存储)
- 修改 `App.kt` 加入鉴权路由
- 编译 APK

### Phase 4: 端到端验证 (约 10min)
- 注册 → 登录 → 使用 App → 后台查看

---

## 七、成功标准

- [ ] POST /api/auth/register 成功创建用户，第一个用户为 admin
- [ ] POST /api/auth/login 返回有效 token
- [ ] GET /api/auth/me 带 token 返回用户信息
- [ ] GET /api/admin/users 仅 admin 可访问
- [ ] DELETE /api/admin/users/:id 删除用户
- [ ] Android 登录界面可注册/登录
- [ ] 登录后自动跳转主页面
- [ ] Token 过期后要求重新登录
- [ ] 后台可看到用户列表并删除用户
- [ ] 密码哈希存储（不存明文）
- [ ] 全部 stdlib（不增加 pip 依赖）

---

## 八、风险

| 风险 | 概率 | 影响 | 应对 |
|------|------|------|------|
| Token 传输被截获 | 低 | 高 | 全站 HTTPS |
| 密码弱 | 中 | 中 | 最低 6 位校验 |
| SQLite 并发写 | 低 | 低 | Vercel 单实例，WSGI 串行 |
| 第一个注册的不是自己 | 低 | 中 | 注册时设 admin_key 验证 |
