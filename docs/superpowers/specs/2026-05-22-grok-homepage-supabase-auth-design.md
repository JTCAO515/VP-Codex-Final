# Grok 风格主页 + Supabase/Google 登录（v2.2）设计文档

日期：2026-05-22  
状态：待实现（已与用户确认方案）

## 0. 目标与非目标

### 目标
1) 做一个 **Grok 风格**的极简主页：页面只突出一个“开始对话”的输入框（回车即发送），其余信息弱化。  
2) 背景具备 **中国经典元素**（祥云纹样 + 水墨山水远景），但透明度足够高、可读性优先。  
3) 设计并接入 **用户登录系统**：采用 **Supabase Auth**，并接入 **Google 登录**。  
4) 允许 **游客试用**：不强制登录即可开始对话；登录后拥有“身份归属”。  
5) 实现“保留/历史记录”能力：对话消息、行程版本、酒店订单、RFP/服务订单可回看（登录后持久化；游客仅本地临时）。

### 非目标（本迭代不做）
- 支付/会员/计费
- 完整用户中心（头像修改、账号设置、解绑等）
- 多租户/组织体系
- 生产级风控（仅保留扩展点）

## 1. 现状与约束

- 当前项目为静态前端（`frontend/index.html`）+ FastAPI 后端（挂载到 `/api`），并适配 Vercel 同域名部署。  
- `/api/chat/messages` 当前入参包含 `user_id`/`trip_id`，但未做身份验证，存在伪造风险。  
- Vercel 运行环境：**为了实现“历史记录持久化”**，本迭代将把数据库从 SQLite 升级为 **外部 Postgres**（优先使用 Supabase Postgres）。

## 2. 体验设计（UX）

### 2.1 主页布局（Grok 风格）
- 背景：暗色渐变底（近黑），叠加两层“高透明度中国元素”
  - 远景：水墨山水（雾化/模糊、低对比）
  - 近景：祥云/回纹线稿的 repeating SVG pattern（极低不透明度）
- 中央内容（严格简化）：
  - 标题（可选）：一句话价值主张（例如 “Plan your China trip”）
  - **一个输入框**：placeholder 引导（例如 “Start a conversation…”）
  - 发送行为：回车发送；移动端提供“Start”按钮但视觉弱化
  - 次级引导：`Continue as guest · Sign in with Google`
- 顶部导航：仅右上角
  - 未登录：`Sign in`
  - 已登录：显示用户状态（例如 “Signed in” + `Sign out`）

### 2.2 对话页与历史记录
- 从主页发送第一条消息后进入对话页（建议 `/chat`）：
  - 主区域：对话流（消息气泡）
  - 底部：输入框（回车发送）
  - 桌面端左侧：历史会话列表（最近 N 个 trip）
  - 移动端：左上角“历史”按钮打开抽屉/底部弹层
- 历史会话列表每条显示：
  - 标题：自动摘要（例如 “Beijing 5 days · food+history”）
  - 时间：最近更新时间
  - 状态：Guest / Signed in（可选）

### 2.2 游客试用策略
- 默认允许游客直接对话
- 游客身份：前端生成 `guest_id`（UUID）并存入 localStorage
- 游客数据策略（本迭代）：
  - 游客可对话，但**不写入服务器端持久化历史**
  - 游客历史记录仅保存在本地（localStorage）：
    - 最近 1~3 个会话（可配置）
    - 刷新不丢、清缓存即丢
  - 当游客尝试“保存/查看完整历史/查看订单”时提示登录

## 2.4 登录后历史记录能力（登录态）
- 登录后将以下数据持久化到 Postgres（Supabase）：
  - 对话消息（ChatMessage）
  - 行程版本（Trip 的 itinerary_versions 或单独版本表）
  - 酒店订单（HotelBooking）
  - RFP/报价/服务订单（RFP/Quote/ServiceOrder）
- 用户进入对话页默认加载最近会话列表；点击即可回看完整消息与行程版本

## 3. 登录方案（Supabase Auth + Google）

### 3.1 方案选择理由
- Supabase Auth 集成成本低，Google OAuth 开箱即用
- 前端拿到 JWT（access_token），后端可按 JWKS 校验，避免自建 OAuth 安全坑

### 3.2 登录流程
1) 用户点击 “Sign in with Google”
2) 前端调用 Supabase `signInWithOAuth({ provider: "google", redirectTo: <callbackUrl> })`
3) Supabase 回调到：`/auth/callback`
4) callback 页面读取 session（或 URL fragment），保存到 localStorage（Supabase SDK 管理）
5) 跳转回主页 `/`

### 3.3 登出流程
- 前端调用 Supabase `signOut()`
- UI 切回游客态

## 4. 前后端身份对接（关键安全点）

### 4.1 客户端请求规范
- 若已登录：请求后端时带 `Authorization: Bearer <supabase_access_token>`
- 若游客：不带 Authorization；同时在 body 里提供 `guest_id`

### 4.2 后端鉴权策略（最小可用）
新增一个 `auth` 模块：
- 尝试从 `Authorization` 解析 Bearer token
  - 有 token：校验 token（使用 Supabase JWKS 或按 Supabase 公钥验证）
    - 通过后从 token 取 `sub` 作为 `user_id`
  - 无 token：视为游客
    - 使用 `guest_id` 作为 `user_id`（或映射到 `user_id = "guest:<guest_id>"`）
- 后端忽略客户端传来的 `user_id`（登录态下必须由后端从 token 得出）

### 4.3 API 兼容策略（避免大改动）
当前 `/api/chat/messages` 入参为：
```json
{ "user_id": "...", "trip_id": "...", "text": "..." }
```
建议演进为（兼容旧字段）：
```json
{
  "trip_id": "...",
  "text": "...",
  "user_id": "optional (guest only)",
  "guest_id": "optional (guest only)"
}
```
后端优先级：
1) Bearer token → user_id = sub
2) guest_id → user_id = "guest:<guest_id>"
3) user_id（仅为兼容，不推荐长期保留）

## 4.4 持久化与数据模型（Supabase Postgres）

### 数据库选择
- 采用 Supabase Postgres（外部 DB），通过 `DATABASE_URL` 连接
- FastAPI 启动时依旧可用 `create_all`（MVP），但后续建议引入 Alembic 做迁移

### 新增/调整表（最小）
在现有 `User/Trip/...` 的基础上新增：
- `ChatMessage`
  - `id`（uuid）
  - `user_id`（登录用户为 supabase sub；游客使用 `guest:<guest_id>` 仅用于本地，不持久化）
  - `trip_id`
  - `role`（user/assistant/system）
  - `content`（text）
  - `created_at`

Trip 增强字段（建议）：
- `title`：会话标题（用于历史列表）
- `updated_at`：最后更新时间（排序）

## 5. 页面与路由结构（Vercel 同域名）
- `/`：Grok 风格主页（静态）
- `/auth/callback`：OAuth 回调页（静态）
- `/chat`：对话页（静态，含历史列表）
- `/api/*`：后端 FastAPI（现有 `api/index.py` 挂载 `/api`）

## 6. 配置与环境变量

### 6.1 前端（Vercel 环境变量）
- `SUPABASE_URL`
- `SUPABASE_ANON_KEY`
- （可选）`SUPABASE_AUTH_REDIRECT_URL`（不提供则用 `window.location.origin + "/auth/callback"`）

### 6.2 后端（Vercel 环境变量）
- `SUPABASE_URL`（用于拼 JWKS/issuer 校验）
- `SUPABASE_JWT_AUD`（如需更严格校验；可选）
- `SUPABASE_JWT_ISSUER`（可选，默认从 URL 推断）
- `DATABASE_URL`（指向 Supabase Postgres；用于历史记录持久化）
- 现有 LLM 相关变量保持不变

## 7. 视觉实现建议（背景）
- 祥云纹样：SVG path + pattern fill，opacity 建议 0.04~0.08
- 山水远景：可以用一张轻量的半透明背景图（或 SVG）并 `filter: blur(6px)` + `mix-blend-mode: screen/overlay` 轻混合
- 任何背景层都必须确保正文对比度 AA 以上（至少 4.5:1）

## 8. 测试与验收标准

### 8.1 验收标准
- 主页首屏只有一个主要输入框；移动端可用；对比度良好
- 游客可直接对话；刷新后 guest_id 不丢
- Google 登录可完成；登录后 UI 显示已登录态
- 登录态调用后端时，不依赖前端传 user_id；后端从 token 解析用户身份
- 登录态可查看“历史会话列表”，点击可回看消息与行程版本
- 登录态创建的订单/RFP 记录可通过接口回查（最小展示即可）

### 8.2 测试（最小）
- 前端：基本 E2E（手测）+ 可选 Playwright
- 后端：单测覆盖
  - Bearer token 缺失 → guest 逻辑
  - Bearer token 存在且无效 → 401
  - Bearer token 有效 → user_id 来自 token.sub
  - 登录态写入 ChatMessage 成功；list/read 能按 user_id 隔离
  - Trip list 按 updated_at 排序

## 9. 迭代路线（后续）
- v2.3：聊天历史/行程保存的增强（多端同步、搜索、置顶）
- v2.4：Postgres schema 迁移（Alembic）、更完善的数据归档/清理策略
- v2.5：Trip.com 真对接 + 订单回查/取消完善
