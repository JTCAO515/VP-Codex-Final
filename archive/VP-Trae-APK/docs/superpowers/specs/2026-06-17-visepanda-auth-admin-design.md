# VisePanda 用户认证与管理后台设计

日期：2026-06-17

## 目标

为 `VisePanda` 建立一套可扩展、适合 Android 与未来 iOS 共同复用的用户系统基础设施，第一阶段采用 `Supabase` 作为认证与数据库底座，并建设一个独立的 `Next.js` 用户管理后台。

本次目标不是只做“邮箱登录页”，而是同时完成：

- 用户注册 / 登录 / 登出
- 邮箱验证
- 忘记密码
- 用户资料扩展表
- 管理员后台登录
- 用户列表与详情查看
- 角色与状态管理

## 背景判断

当前仓库已经有 Android 原生客户端主线，但没有现成的服务端、数据库或 Web 管理后台基础。若现在直接自建完整认证服务，会显著增加首轮实现复杂度和上线风险；而若完全依赖纯客户端方案，则无法满足“用户管理后台”的需求。

因此本次方案选择：

- 认证与数据库：`Supabase`
- 管理后台：`Next.js`
- Android 客户端：接入 Supabase 登录体系

## 技术路线结论

本项目采用以下三段式架构：

- `app/`：Android 客户端
- `admin/`：Next.js 用户管理后台
- `supabase/`：数据库 schema、RLS policy、触发器、说明文档

### 为什么选择 Supabase

对当前阶段而言，`Supabase` 比 Firebase 和完全自建后端更平衡：

- 比 Firebase 更适合做结构化用户管理与后台查询
- 比自建认证服务更快落地，风险更小
- 后续 Android、iOS、Admin 三端可共享一套认证与 profile 结构
- 基于 Postgres，后续要扩展会员、偏好、审计、运营字段更自然

## 权限模型

第一阶段只保留最小权限模型：

- `user`
- `admin`

不引入 `editor`、`operator`、`super_admin` 等额外角色，避免首版权限系统失控。

### 用户状态

第一阶段定义以下状态：

- `pending`
- `active`
- `disabled`

### 权限边界

| 能力 | user | admin |
|------|------|-------|
| 登录 App | 是 | 是 |
| 查看自己的 profile | 是 | 是 |
| 编辑自己的基础资料 | 是 | 是 |
| 访问 Admin | 否 | 是 |
| 查看全部用户 | 否 | 是 |
| 修改他人角色 | 否 | 是 |
| 启用 / 禁用用户 | 否 | 是 |

## 安全边界

本项目必须遵守以下约束：

1. Android 客户端只使用 `anon key`
2. `service role key` 只允许服务端使用，不进入客户端
3. Admin 后台的重要读写通过服务端处理，不在浏览器中直接暴露高权限能力
4. `user_profiles` 受 RLS 保护
5. 普通用户只能读写自己的 profile
6. 管理员列表查询、角色修改、禁用启用等行为通过后台服务端校验 admin 身份后执行

## 数据结构

### 认证层

认证由 `Supabase Auth` 管理，用户主身份位于：

- `auth.users`

### 业务扩展表

业务层用户信息扩展表定义为：

- `public.user_profiles`

建议字段：

- `id UUID PRIMARY KEY`，与 `auth.users.id` 一致
- `email TEXT NOT NULL`
- `display_name TEXT`
- `avatar_url TEXT`
- `role TEXT NOT NULL DEFAULT 'user'`
- `status TEXT NOT NULL DEFAULT 'pending'`
- `created_at TIMESTAMPTZ NOT NULL`
- `updated_at TIMESTAMPTZ NOT NULL`
- `last_login_at TIMESTAMPTZ`

### 设计原则

- 认证与业务字段分离
- Android / iOS / Admin 统一读 `user_profiles`
- 未来可继续增加偏好、会员、审计、封禁等数据，不污染认证层

## 数据库行为设计

### 自动创建 profile

当用户通过邮箱注册成功后，系统自动为其创建一条 `user_profiles` 记录。

默认值：

- `role = 'user'`
- `status = 'pending'`

### 邮箱验证后的状态

第一阶段推荐逻辑：

- 用户注册后要求完成邮箱验证
- 验证完成后，profile 状态从 `pending` 切换到 `active`

### 登录后更新时间

登录成功后更新：

- `last_login_at`

## 用户认证流程

### App 端

第一阶段支持：

- 邮箱 + 密码注册
- 邮箱 + 密码登录
- 邮箱验证
- 忘记密码
- 登出
- 启动时恢复 session

第一阶段不支持：

- 手机号登录
- Google 登录
- Apple 登录
- 双因素认证

### Admin 端

后台登录基于同一套 Supabase Auth，但增加业务约束：

只有满足以下条件的用户才允许进入 Admin：

- `role = 'admin'`
- `status = 'active'`

即使邮箱密码正确，只要不满足角色与状态条件，也不得进入后台。

## 管理后台结构

后台第一阶段只做最核心的 5 个页面。

### 1. 登录页

管理员邮箱密码登录入口。

### 2. Dashboard

展示：

- 总用户数
- active 用户数
- pending 用户数
- disabled 用户数

### 3. 用户列表页

支持：

- 按邮箱搜索
- 按角色筛选
- 按状态筛选

### 4. 用户详情页

展示：

- email
- display_name
- role
- status
- created_at
- last_login_at

### 5. 用户操作区

支持：

- 修改角色
- 启用用户
- 禁用用户
- 修改 display_name

第一阶段明确不做：

- 审计日志页面
- 批量导入导出
- 邮件营销
- 数据分析大屏
- 复杂权限矩阵

## Android 客户端接入范围

Android 第一阶段接入以下页面和能力：

- 注册页
- 登录页
- 忘记密码页
- 登录态持久化
- 启动恢复 session
- 登出
- 读取当前用户 profile

不在这一阶段做：

- 头像上传
- 完整 profile 编辑页
- 多端同步偏好设置

## 工程结构

建议将仓库扩展为以下结构：

```text
VisePanda/
├── app/         # Android 客户端
├── admin/       # Next.js 用户管理后台
└── supabase/    # schema / policy / trigger / docs
```

### 边界原则

- `app/` 只关心登录流和用户态
- `admin/` 只关心管理员运营能力
- `supabase/` 是三端共享的真实数据契约来源

## 实施顺序

推荐顺序如下：

1. 先建立 `supabase/` schema 与策略
2. 再搭建 `admin/` 管理后台
3. 然后在 `app/` 中接入 Android 登录流
4. 最后补 profile 与体验细节

### 这样排序的原因

- 没有 schema，后面所有端都无法稳定对齐
- 有了后台后，可以更快验证用户数据是否正确写入
- 客户端接入时更容易定位账号和权限问题

## 第一阶段交付范围

### Supabase

- `user_profiles` 表
- 自动创建 profile 逻辑
- RLS policy
- profile 更新策略
- admin 判定约束

### Admin

- Next.js 工程初始化
- Supabase 服务端接入
- 后台登录页
- Dashboard
- 用户列表页
- 用户详情页
- 改角色 / 启用 / 禁用

### Android

- 登录页
- 注册页
- 忘记密码页
- 登录态持久化
- session 恢复
- 基础 profile 拉取

## 第二阶段交付范围

第二阶段再扩展：

- profile 编辑
- 头像上传
- 审计日志
- Admin 搜索筛选优化
- iOS 接入
- 用户偏好设置
- 更细的风控提示

## 成功标准

当第一阶段完成时，应满足：

- 用户可通过邮箱注册与登录
- 邮箱验证流程可用
- Android 能恢复和识别登录态
- Admin 可查看并管理用户
- 只有 admin 能进入后台
- 用户与管理员的权限边界清晰
- Android / Admin / Supabase 三部分结构清楚，后续 iOS 可复用

## 明确不做

第一阶段不做以下内容：

- 自建认证服务
- 手机号登录
- Google / Apple 登录
- 多角色复杂权限系统
- 审计平台
- CRM / 邮件营销系统
- iOS 同步首发

## 最终结论

本次最稳的方案是：

- 使用 `Supabase` 负责认证与数据库
- 使用 `Next.js` 建立独立的用户管理后台
- Android 先接入邮箱登录与用户态
- 用户权限控制维持在 `user / admin`
- 用户状态控制维持在 `pending / active / disabled`

这套方案兼顾了：

- 当前阶段的实现速度
- 后续管理能力
- Android 与未来 iOS 的复用空间
