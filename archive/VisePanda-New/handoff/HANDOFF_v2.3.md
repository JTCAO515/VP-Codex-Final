# 交接包说明（v2.3，给另一位 Coding AI）

本交接包包含：
1) **项目完整代码与资料**（前端静态站点、后端 FastAPI、Vercel 配置、spec/plan 文档）
2) **对话整理（脱敏版）**（不包含任何明文 token/密钥）

> 当前本地代码版本：`a86085e`（feat: v2.3 orders isolation + chat details panel）

---

## 1. 项目目标（一句话）
做一个面向入境游客的中国旅行对话式 Agent：通过“少问问题、多交流”深挖需求，生成结构化行程，并支持酒店与本地服务（RFP 报价 → 服务订单）闭环；登录后可回看对话/行程/订单历史。

## 2. 关键架构
- **前端**：纯静态（Vanilla HTML/CSS/JS），路由由 `vercel.json` rewrites 映射：
  - `/` Grok 风格主页（仅一个开始对话框）
  - `/chat` 对话页（左侧会话列表 + 中间对话流 + 右侧详情面板）
  - `/auth/callback` Supabase OAuth 回调页
- **后端**：FastAPI 挂载到同域名 `/api/*`（`api/index.py`）
- **身份体系**：
  - 游客：`guest_id`（localStorage），仅本地保存最近 3 个会话
  - 登录：Supabase Auth（Google OAuth），后端从 JWT `sub` 获取 user_id
- **持久化**：优先使用 `DATABASE_URL` 指向 Supabase Postgres（Vercel 环境下必须外部 DB 才能保存历史）

## 3. v2.2 / v2.3 已实现能力（重点）
### 3.1 Grok 风格主页 + 中国元素背景
- 深色极简 UI
- 背景两层：祥云纹样 + 水墨山水远景（高透明度）

### 3.2 登录（Supabase + Google）
- 前端按钮触发 Google OAuth
- 回调页落地 session
- 后端提供 `GET /api/public-config` 给前端读取 `SUPABASE_URL` / `SUPABASE_ANON_KEY`

### 3.3 历史记录（对话/行程）
- 对话消息写入 `chat_messages`
- `GET /api/trips`：登录用户会话列表
- `GET /api/trips/{trip_id}/messages`：加载历史对话

### 3.4 v2.3：订单/RFP/booking 的登录强制与隔离 + /chat 右侧面板
- **后端安全升级**：以下接口必须登录（Bearer token），并按 Trip/RFP 归属隔离：
  - RFP：`POST /api/rfps`、`GET /api/rfps/{id}`、`POST /api/rfps/{id}:shortlist`
  - 服务订单：`POST /api/service-orders`、`GET /api/service-orders/{id}`
  - 酒店 booking：`POST /api/hotel/bookings`、`GET /api/hotel/bookings/{id}`、`POST /api/hotel/bookings/{id}:cancel`
- **Trip 维度聚合列表接口**（供右侧面板加载）：
  - `GET /api/trips/{trip_id}`（已鉴权）
  - `GET /api/trips/{trip_id}/hotel-bookings`
  - `GET /api/trips/{trip_id}/rfps`
  - `GET /api/trips/{trip_id}/service-orders`
- **前端 /chat 右侧详情面板**（Tabs：行程 / 酒店 / RFP&订单）
  - 登录态：加载后端数据
  - 游客态：提示登录（不允许下单/查看订单）

## 4. 本地运行（开发）
后端：
```bash
cd backend
pip install -r requirements-dev.txt --break-system-packages
export AUTH_TEST_BYPASS=1  # 仅测试用途
uvicorn app.main:app --reload --port 8000
```
前端：
```bash
cd frontend
python3 -m http.server 5173
```
打开：http://localhost:5173/

## 5. Vercel 部署（关键环境变量）
必须配置：
- `SUPABASE_URL`
- `SUPABASE_ANON_KEY`
- `DATABASE_URL`（Supabase Postgres 连接串）

Supabase 后台：
- 开启 Google Provider
- Redirect URL：`https://<你的域名>/auth/callback`

## 6. 安全注意（给接手的 AI）
- 历史对话中用户曾明文提供过 token；本交接包已脱敏，不包含 token。
- 生产环境务必关闭 `AUTH_TEST_BYPASS`（该开关仅用于本地/CI 单测）。

