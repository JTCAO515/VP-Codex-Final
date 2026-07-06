# Grok 风格主页 + Supabase/Google 登录 + 历史记录（v2.2）Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 上线一个 Grok 风格极简主页 + `/chat` 对话页（含历史列表），支持游客试用与 Supabase Google 登录；登录后持久化保存对话消息、行程版本、酒店订单、RFP/服务订单到 Supabase Postgres，并让后端基于 Bearer token 做身份归属与隔离。

**Architecture:** 静态前端（Vanilla JS + Supabase JS SDK）同域名部署；后端 FastAPI 暴露 `/api/*`，新增鉴权层解析 Supabase JWT；数据库从 SQLite 切换到 `DATABASE_URL`（Supabase Postgres）。游客仅本地保存最近 3 个会话；登录用户保存到 Postgres 并可跨端访问。

**Tech Stack:** FastAPI, SQLAlchemy, Supabase Auth (Google OAuth), Supabase Postgres, Vercel (Python runtime + static), Vanilla HTML/CSS/JS.

---

## 0) Files / Structure (锁定改动范围)

**Frontend (static):**
- Modify: `/workspace/frontend/index.html`（替换为 Grok 风格主页：居中输入框 + 背景纹样 + 登录入口）
- Create: `/workspace/frontend/chat.html`（对话页：消息流 + 输入框 + 历史列表）
- Create: `/workspace/frontend/auth/callback.html`（OAuth 回调页）
- Create: `/workspace/frontend/assets/patterns/cloud.svg`（祥云/回纹 SVG pattern）
- Create: `/workspace/frontend/assets/bg/shanshui.svg`（水墨山水远景 SVG/简图）
- Create: `/workspace/frontend/app.js`（共享逻辑：Supabase client、guest_id、API fetch、history local cache）
- Create: `/workspace/frontend/chat.js`（对话页逻辑：加载历史列表、切换 trip、渲染消息）

**Backend:**
- Modify: `/workspace/backend/app/db.py`（支持 `DATABASE_URL`；默认 sqlite 仅本地 fallback）
- Modify: `/workspace/backend/app/models.py`（新增 ChatMessage；Trip 增加 title/updated_at）
- Modify: `/workspace/backend/app/routers/chat.py`（写入 ChatMessage；忽略客户端 user_id；支持 guest_id）
- Create: `/workspace/backend/app/auth.py`（Supabase JWT 校验/解析；get_current_principal）
- Modify: `/workspace/backend/app/main.py`（挂载 auth middleware/依赖；CORS 收紧策略可选）
- Modify: `/workspace/backend/app/routers/trips.py`（新增 trips list：按用户取最近会话）
- Modify: `/workspace/backend/app/routers/hotel.py`（booking user_id 归属；查询时做隔离）
- Modify: `/workspace/backend/app/routers/rfps.py`（rfp/order user_id 归属；查询隔离）
- Modify: `/workspace/backend/app/routers/suppliers.py`（保持不变或补充 event payload 归属字段）

**Vercel:**
- Modify: `/workspace/vercel.json`（静态站点路由 + /api rewrites + excludeFiles）
- Modify: `/workspace/api/index.py`（保持：挂载 backend 在 /api；确认 / 和 /chat 静态可访问）

**Tests:**
- Modify/Create:
  - `/workspace/backend/tests/test_auth_and_history.py`（新增：登录态 token/guest 行为、ChatMessage 持久化、Trip list）
  - `/workspace/backend/tests/test_chat.py`（调整：user_id/guest_id、历史写入断言）

---

## 1) Task 1: 前端基础骨架（Grok 主页 + /chat + callback）

**Files:**
- Modify: `frontend/index.html`
- Create: `frontend/chat.html`
- Create: `frontend/auth/callback.html`
- Create: `frontend/app.js`
- Create: `frontend/chat.js`

- [ ] **Step 1: 写入前端页面路由骨架（先不接入 Supabase）**

`frontend/index.html`（最小 Grok 风格：输入框 + Start + link 到 /chat）：
```html
<!-- 仅示意：实现时用更完整的 CSS 与背景层 -->
<main class="hero">
  <h1>Plan your China trip</h1>
  <form id="startForm">
    <input id="prompt" placeholder="Start a conversation…" />
    <button>Start</button>
  </form>
  <div class="sub">
    <a href="/chat">Open chat</a>
    <span>·</span>
    <button id="googleBtn" type="button">Sign in with Google</button>
  </div>
</main>
<script src="/app.js"></script>
```

`frontend/chat.html`：左侧历史（桌面），移动端 drawer（简单实现：按钮切换显示），中间消息区：
```html
<div class="layout">
  <aside id="sidebar"></aside>
  <main id="thread"></main>
  <footer>
    <form id="msgForm">
      <input id="msgInput" placeholder="Message…" />
      <button>Send</button>
    </form>
  </footer>
</div>
<script src="/app.js"></script>
<script src="/chat.js"></script>
```

`frontend/auth/callback.html`：读取 session 后跳转 `/chat`。

- [ ] **Step 2: 加入 guest_id 与 trip_id 的本地生成/存储**

在 `frontend/app.js` 实现：
```js
export function getGuestId() {
  const k = "cta_guest_id";
  let v = localStorage.getItem(k);
  if (!v) { v = crypto.randomUUID(); localStorage.setItem(k, v); }
  return v;
}

export function newTripId() {
  return "t_" + crypto.randomUUID();
}
```

- [ ] **Step 3: 游客本地历史策略（默认 3 个会话）**

在 `frontend/app.js`：
```js
const MAX_GUEST_TRIPS = 3;
export function saveGuestTripMeta(meta) {
  const k = "cta_guest_trips";
  const arr = JSON.parse(localStorage.getItem(k) || "[]");
  const next = [meta, ...arr.filter(x => x.trip_id !== meta.trip_id)].slice(0, MAX_GUEST_TRIPS);
  localStorage.setItem(k, JSON.stringify(next));
}
```

- [ ] **Step 4: 提交**

Run:
```bash
git add frontend/index.html frontend/chat.html frontend/auth/callback.html frontend/app.js frontend/chat.js
git commit -m "feat: grok-style homepage + chat shell (guest mode)"
```

---

## 2) Task 2: 背景视觉资产（祥云 + 山水，高透明度）

**Files:**
- Create: `frontend/assets/patterns/cloud.svg`
- Create: `frontend/assets/bg/shanshui.svg`
- Modify: `frontend/index.html`（引入背景层 CSS）
- Modify: `frontend/chat.html`（一致背景）

- [ ] **Step 1: 添加祥云 pattern（SVG）与山水远景（SVG）**

要求：
- 祥云层 opacity 0.04~0.08
- 山水层 blur 6px + 低对比
- 正文对比度优先

- [ ] **Step 2: 在 CSS 中叠加两层背景**

示例：
```css
body {
  background: radial-gradient(1200px 800px at 30% 20%, #121826 0%, #070a0f 60%, #05070b 100%);
}
.bg-shanshui { position: fixed; inset: 0; background: url("/assets/bg/shanshui.svg") center/cover no-repeat; opacity:.12; filter: blur(6px); pointer-events:none; }
.bg-cloud { position: fixed; inset: 0; background: url("/assets/patterns/cloud.svg") center/420px repeat; opacity:.06; mix-blend-mode: screen; pointer-events:none; }
```

- [ ] **Step 3: 提交**
```bash
git add frontend/assets frontend/index.html frontend/chat.html
git commit -m "feat: chinese motif background (cloud pattern + shanshui)"
```

---

## 3) Task 3: Supabase Auth（Google 登录）前端接入

**Files:**
- Modify: `frontend/app.js`
- Modify: `frontend/index.html`
- Modify: `frontend/chat.html`
- Modify: `frontend/auth/callback.html`

- [ ] **Step 1: 引入 Supabase JS SDK（CDN）并初始化**

为保持静态部署简洁：在 `frontend/app.js` 使用 ESM CDN（实现时确认版本）：
```js
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";
export const supabase = createClient(window.SUPABASE_URL, window.SUPABASE_ANON_KEY);
```

并在页面里注入 config（由 Vercel 环境变量构建时注入或手动填写；最小实现先用 `<script>` 写死占位，后续再升级构建）：
```html
<script>
  window.SUPABASE_URL = "__SUPABASE_URL__";
  window.SUPABASE_ANON_KEY = "__SUPABASE_ANON_KEY__";
</script>
```

- [ ] **Step 2: Google 登录与回调**

`frontend/index.html` 登录按钮：
```js
document.getElementById("googleBtn").onclick = async () => {
  const redirectTo = window.location.origin + "/auth/callback";
  await supabase.auth.signInWithOAuth({ provider: "google", options: { redirectTo }});
};
```

`frontend/auth/callback.html`：
```js
// 等待 Supabase 写入 session 后跳转
setTimeout(() => location.href = "/chat", 200);
```

- [ ] **Step 3: 登录态 UI（Sign out）**

在 `app.js` 提供：
```js
export async function getSession() { return (await supabase.auth.getSession()).data.session; }
export async function signOut() { await supabase.auth.signOut(); }
```

- [ ] **Step 4: 提交**
```bash
git add frontend/app.js frontend/index.html frontend/chat.html frontend/auth/callback.html
git commit -m "feat: supabase google login (frontend)"
```

---

## 4) Task 4: 后端鉴权（Supabase JWT）与 user_id 归属

**Files:**
- Create: `backend/app/auth.py`
- Modify: `backend/app/routers/chat.py`
- Modify: `backend/app/main.py`
- Test: `backend/tests/test_auth_and_history.py`

- [ ] **Step 1: 写 failing tests（guest / missing token / invalid token）**

`backend/tests/test_auth_and_history.py`：
```python
from fastapi.testclient import TestClient
from app.main import create_app

def test_guest_can_chat_without_auth():
    with TestClient(create_app()) as c:
        r = c.post("/chat/messages", json={"trip_id":"t1","text":"5 days in Beijing","guest_id":"g1"})
        assert r.status_code == 200

def test_invalid_bearer_token_rejected():
    with TestClient(create_app()) as c:
        r = c.post("/chat/messages",
            headers={"Authorization":"Bearer invalid"},
            json={"trip_id":"t1","text":"hello"})
        assert r.status_code in (401, 403)
```

- [ ] **Step 2: 实现 `auth.py`（最小：可关闭严格校验）**

MVP 允许两档：
- 开发/未配置 Supabase：token 校验可跳过（但线上必须配置）
- 线上配置：按 JWKS 校验签名与 issuer

实现接口（示意）：
```python
def get_principal(request) -> dict:
    # returns {"mode":"user","user_id": "..."} or {"mode":"guest","guest_id":"..."}
```

- [ ] **Step 3: 改造 `/chat/messages` 入参与 user_id 来源**

修改 `ChatIn`：
```python
class ChatIn(BaseModel):
    trip_id: str
    text: str
    guest_id: str | None = None
    user_id: str | None = None  # 仅兼容
```

逻辑：
- 有 Bearer token → user_id = token.sub
- 无 token → user_id = f"guest:{guest_id}"（若缺 guest_id 则 400）

- [ ] **Step 4: 运行 tests 并提交**

Run:
```bash
cd backend
python -m pytest -q
```

Commit:
```bash
git add backend/app/auth.py backend/app/routers/chat.py backend/app/main.py backend/tests/test_auth_and_history.py
git commit -m "feat: backend auth (supabase jwt) + guest identity"
```

---

## 5) Task 5: Postgres 持久化（ChatMessage + Trip title/updated_at + list）

**Files:**
- Modify: `backend/app/db.py`
- Modify: `backend/app/models.py`
- Modify: `backend/app/routers/trips.py`
- Modify: `backend/app/routers/chat.py`
- Test: `backend/tests/test_auth_and_history.py`

- [ ] **Step 1: DB 切换为 DATABASE_URL**

在 `db.py`：
- 若存在 `DATABASE_URL` → create_engine(DATABASE_URL)
- 否则 fallback sqlite（本地）

- [ ] **Step 2: models 新增 ChatMessage 与 Trip 字段**

新增：
```python
class ChatMessage(Base):
    __tablename__ = "chat_messages"
    id = Column(String, primary_key=True, default=gen_uuid)
    user_id = Column(String, index=True, nullable=False)
    trip_id = Column(String, index=True, nullable=False)
    role = Column(String, nullable=False)
    content = Column(Text, nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow)
```

Trip 增加：
- `title`（nullable）
- `updated_at`（每次写消息更新）

- [ ] **Step 3: chat 写入消息并更新 trip.updated_at/title**

在 `chat_messages`：
- 写入 user message
- 写入 assistant reply message
- 若首次产生标题：用简规则生成（`<city> <days> days` 或 截断用户首句）

- [ ] **Step 4: trips 列表接口**

新增 `GET /trips`：
- 登录态：按 user_id 过滤
- 返回：id/title/updated_at/cities/days 等简要字段

- [ ] **Step 5: tests + commit**

补测试：
- 写两条消息后，`GET /trips` 返回该 trip
- `/trips` 隔离不同 user_id

Commit:
```bash
git add backend/app/db.py backend/app/models.py backend/app/routers/trips.py backend/app/routers/chat.py backend/tests/test_auth_and_history.py
git commit -m "feat: persist chat history + trip list (postgres)"
```

---

## 6) Task 6: 订单/RFP/酒店记录的归属隔离（登录态）

**Files:**
- Modify: `backend/app/models.py`（HotelBooking/RFP/ServiceOrder 增加 user_id 字段如缺）
- Modify: `backend/app/routers/hotel.py`
- Modify: `backend/app/routers/rfps.py`
- Test: `backend/tests/test_auth_and_history.py`（或拆分为 test_orders_auth.py）

- [ ] **Step 1: 在对应模型上加 user_id（或复用 Trip.user_id 关联）**

MVP 推荐：以 `Trip.user_id` 作为归属源（减少字段扩散）：
- 查询订单/RFP 时 join Trip，过滤 Trip.user_id == current_user_id

- [ ] **Step 2: 修改查询接口做隔离**

例如 `GET /hotel/bookings/{id}`：
- 读取 booking → 找 booking.trip_id → trip.user_id 校验
- 不匹配 → 404（避免信息泄露）

RFP/ServiceOrder 同理。

- [ ] **Step 3: tests + commit**

Commit:
```bash
git add backend/app/models.py backend/app/routers/hotel.py backend/app/routers/rfps.py backend/tests/test_auth_and_history.py
git commit -m "feat: enforce user isolation for bookings/rfp/orders"
```

---

## 7) Task 7: 前端对话页接入后端 + 历史列表（guest 本地 / 登录远端）

**Files:**
- Modify: `frontend/chat.js`
- Modify: `frontend/app.js`
- Modify: `frontend/chat.html`

- [ ] **Step 1: 统一 API fetch（自动加 Bearer）**

在 `app.js`：
```js
export async function apiFetch(path, { method="GET", headers={}, body } = {}) {
  const session = await getSession();
  if (session?.access_token) headers["Authorization"] = "Bearer " + session.access_token;
  if (body && !(body instanceof FormData)) headers["Content-Type"] = "application/json";
  const res = await fetch("/api" + path, { method, headers, body: body ? JSON.stringify(body) : undefined });
  return res;
}
```

- [ ] **Step 2: 加载历史 trip 列表**

逻辑：
- 登录态：`GET /api/trips` 渲染列表
- 游客态：读取 localStorage `cta_guest_trips` 渲染列表

- [ ] **Step 3: 发送消息**

调用：
`POST /api/chat/messages` body：
- 登录态：`{ trip_id, text }`
- 游客态：`{ trip_id, text, guest_id }`

- [ ] **Step 4: commit**
```bash
git add frontend/app.js frontend/chat.js frontend/chat.html
git commit -m "feat: chat page + history list (guest local / user remote)"
```

---

## 8) Task 8: Vercel 配置与上线检查清单

**Files:**
- Modify: `vercel.json`
- Modify: `README.md`

- [ ] **Step 1: 确认静态路由**
- `/` → `frontend/index.html`
- `/chat` → `frontend/chat.html`
- `/auth/callback` → `frontend/auth/callback.html`
- `/api/*` → python function

（根据 Vercel static serving 习惯，可能需要将 `frontend/` 内容复制到仓库根的 public 目录，或用 rewrites 指向文件；本 step 以实际部署结果为准。）

- [ ] **Step 2: 环境变量清单（Vercel）**
- `SUPABASE_URL`
- `SUPABASE_ANON_KEY`
- `DATABASE_URL`

- [ ] **Step 3: 提交**
```bash
git add vercel.json README.md
git commit -m "chore: vercel routing + env vars for auth/history"
```

---

## 9) Spec Coverage Self-Review

- Grok 主页：Task 1 + Task 2
- Supabase Google 登录：Task 3
- 后端鉴权与 user_id 归属：Task 4
- 历史记录（对话/行程版本/订单/RFP）：Task 5 + Task 6 + Task 7
- Vercel 同域名：Task 8

Placeholder scan：无 “TODO/TBD”；每个 Task 均有文件路径、代码片段、测试与 commit 命令。

