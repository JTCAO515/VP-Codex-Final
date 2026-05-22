# Grok 风格主页 + Supabase/Google 登录（v2.2）设计文档

日期：2026-05-22  
状态：待实现（已与用户确认方案）

## 0. 目标与非目标

### 目标
1) 做一个 **Grok 风格**的极简主页：页面只突出一个“开始对话”的输入框（回车即发送），其余信息弱化。  
2) 背景具备 **中国经典元素**（祥云纹样 + 水墨山水远景），但透明度足够高、可读性优先。  
3) 设计并接入 **用户登录系统**：采用 **Supabase Auth**，并接入 **Google 登录**。  
4) 允许 **游客试用**：不强制登录即可开始对话；登录后拥有“身份归属”（便于后续保存行程/历史）。

### 非目标（本迭代不做）
- 支付/会员/计费
- 完整用户中心（头像修改、账号设置、解绑等）
- 多租户/组织体系
- 生产级风控（仅保留扩展点）

## 1. 现状与约束

- 当前项目为静态前端（`frontend/index.html`）+ FastAPI 后端（挂载到 `/api`），并适配 Vercel 同域名部署。  
- `/api/chat/messages` 当前入参包含 `user_id`/`trip_id`，但未做身份验证，存在伪造风险。  
- Vercel 运行环境：建议后续把 SQLite 持久化迁移到外部 DB（不在本迭代范围）。

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

### 2.2 游客试用策略
- 默认允许游客直接对话
- 游客身份：前端生成 `guest_id`（UUID）并存入 localStorage
- 服务器端权限策略（MVP）：
  - 游客可调用：`/api/chat/messages`、`/api/kb/search`、`/api/translate/*`、`/api/hotel/search`（可选）
  - 游客限制：不允许写入“持久化用户数据”（本期尚未做历史/保存，先预留）
- 未来扩展（不做）：游客每日对话轮次限制、保存前强制登录

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

## 5. 页面与路由结构（Vercel 同域名）
- `/`：Grok 风格主页（静态）
- `/auth/callback`：OAuth 回调页（静态）
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

### 8.2 测试（最小）
- 前端：基本 E2E（手测）+ 可选 Playwright
- 后端：单测覆盖
  - Bearer token 缺失 → guest 逻辑
  - Bearer token 存在且无效 → 401
  - Bearer token 有效 → user_id 来自 token.sub

## 9. 迭代路线（后续）
- v2.3：聊天历史/行程保存（登录后）
- v2.4：Postgres + 多端同步
- v2.5：Trip.com 真对接 + 订单回查/取消完善

