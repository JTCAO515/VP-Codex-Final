# 中国旅行 AI Agent（MVP）Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.
>
> 本计划基于 spec：`docs/superpowers/specs/2026-05-21-china-travel-agent-design.md`

**Goal:** 在 Web/手机网页端交付一个可运行的对话式旅行 Agent MVP：支持混合式需求深挖与结构化行程版本化；对接 Trip.com 酒店搜索+下单（首期不做平台支付，优先到店付/担保产品）；支持定制服务/门票/体验的 RFP 报价与服务订单（未支付）闭环；支持多语种双向文本翻译、图片翻译（OCR）与菜名/景点讲解（KB/RAG）；供应商开放入驻（后台 + API）。

**Architecture:** 单一 Chat Orchestrator 负责意图/槽位/工具调度；所有“下单/发RFP/状态变更”使用确定性工作流状态机；领域对象（Trip/Itinerary/Booking/RFP/Quote/ServiceOrder）事件留痕与版本化；供应商侧先以后台落地、API并行。

**Tech Stack（建议）:**
- Backend: Node.js (NestJS/Fastify) 或 Python (FastAPI) 二选一；本文以 **FastAPI + Postgres** 书写示例
- DB: Postgres（JSONB 存行程版本）
- Queue/Webhook: 可先用 DB + 轮询，后续接 Celery/Redis 或 Cloud queue
- Frontend: Next.js（Web/手机网页）
- Auth: JWT + 供应商 API Key（或 OAuth2 client credentials）
- OCR/翻译/LLM：按你实际选择的模型/供应商适配为 Provider 接口

---

## 0. 预备：初始化仓库与开发约定

> 注意：当前 `/workspace` 不是 git 仓库。本任务从“新项目”角度规划；若你已有仓库结构，请在执行前对齐路径。

### Task 0: 初始化项目骨架（Backend + Frontend）

**Files:**
- Create: `backend/pyproject.toml`
- Create: `backend/app/main.py`
- Create: `backend/app/settings.py`
- Create: `backend/app/db.py`
- Create: `backend/app/api/router.py`
- Create: `backend/app/api/health.py`
- Create: `backend/app/models/__init__.py`
- Create: `backend/app/migrations/`（Alembic）
- Create: `frontend/package.json`
- Create: `frontend/next.config.js`
- Create: `frontend/app/page.tsx`（聊天 UI 雏形）

- [ ] **Step 1: 创建 FastAPI healthcheck**

```python
# backend/app/api/health.py
from fastapi import APIRouter

router = APIRouter()

@router.get("/health")
def health():
    return {"ok": True}
```

- [ ] **Step 2: 创建 API router 并挂载**

```python
# backend/app/api/router.py
from fastapi import APIRouter
from .health import router as health_router

router = APIRouter()
router.include_router(health_router, tags=["health"])
```

```python
# backend/app/main.py
from fastapi import FastAPI
from app.api.router import router

app = FastAPI(title="China Travel Agent MVP")
app.include_router(router)
```

- [ ] **Step 3: 运行并验证**

Run: `uvicorn app.main:app --reload --port 8000`（在 `backend/` 下）  
Expected: GET `/health` 返回 `{"ok": true}`（或大小写差异）

---

## 1. 领域建模与数据库（Trip/Itinerary/HotelBooking/RFP/Quote/ServiceOrder）

### Task 1: 定义核心表结构（Postgres + Alembic）

**Files:**
- Create: `backend/app/models/base.py`
- Create: `backend/app/models/user.py`
- Create: `backend/app/models/trip.py`
- Create: `backend/app/models/hotel.py`
- Create: `backend/app/models/rfp.py`
- Create: `backend/app/models/service_order.py`
- Create: `backend/app/models/supplier.py`
- Create: `backend/app/models/event_log.py`
- Create: `backend/app/schemas/*.py`（Pydantic）
- Create: `backend/app/migrations/versions/0001_init.py`
- Test: `backend/tests/test_db_smoke.py`

- [ ] **Step 1: 写 DB smoke test（先失败）**

```python
# backend/tests/test_db_smoke.py
def test_db_smoke():
    assert True
```

- [ ] **Step 2: 定义最小 SQLAlchemy 模型（示例字段）**

```python
# backend/app/models/trip.py
from sqlalchemy.orm import Mapped, mapped_column
from sqlalchemy.dialects.postgresql import JSONB
from .base import Base

class Trip(Base):
    __tablename__ = "trips"
    id: Mapped[str] = mapped_column(primary_key=True)
    user_id: Mapped[str] = mapped_column(index=True)
    cities: Mapped[list] = mapped_column(JSONB, default=list)
    start_date: Mapped[str | None]
    end_date: Mapped[str | None]
    party: Mapped[dict] = mapped_column(JSONB, default=dict)
    current_itinerary: Mapped[dict] = mapped_column(JSONB, default=dict)
    itinerary_versions: Mapped[list] = mapped_column(JSONB, default=list)
```

（其他表同理：HotelBooking/RFP/Quote/ServiceOrder/Supplier/EventLog）

- [ ] **Step 3: 生成并应用 Alembic migration**

Run: `alembic revision --autogenerate -m "init"`  
Run: `alembic upgrade head`  
Expected: 生成 `trips/hotel_bookings/rfps/quotes/service_orders/suppliers/event_logs` 等表

- [ ] **Step 4: 补充事件留痕写入 helper**

```python
# backend/app/models/event_log.py
class EventLog(Base):
    __tablename__ = "event_logs"
    id: Mapped[str] = mapped_column(primary_key=True)
    entity_type: Mapped[str]
    entity_id: Mapped[str]
    event_type: Mapped[str]
    payload: Mapped[dict] = mapped_column(JSONB, default=dict)
    created_at: Mapped[str]
```

---

## 2. Chat Orchestrator（意图/槽位/动作选择）+ 行程版本化

### Task 2: Chat API（/chat/messages）与会话存储

**Files:**
- Create: `backend/app/api/chat.py`
- Create: `backend/app/schemas/chat.py`
- Modify: `backend/app/api/router.py`
- Test: `backend/tests/test_chat_smoke.py`

- [ ] **Step 1: 写 failing test（接口存在且返回结构）**

```python
# backend/tests/test_chat_smoke.py
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)

def test_chat_message_smoke():
    r = client.post("/chat/messages", json={"user_id":"u1","trip_id":"t1","text":"I want 5 days in Beijing"})
    assert r.status_code == 200
    body = r.json()
    assert "reply" in body
    assert "actions" in body
```

- [ ] **Step 2: 最小实现（先回显+空 actions）**

```python
# backend/app/api/chat.py
from fastapi import APIRouter
from pydantic import BaseModel

router = APIRouter()

class ChatIn(BaseModel):
    user_id: str
    trip_id: str
    text: str

@router.post("/chat/messages")
def chat_messages(payload: ChatIn):
    return {"reply": f"ACK: {payload.text}", "actions": []}
```

- [ ] **Step 3: 挂载路由并跑测试**

Run: `pytest -q`  
Expected: PASS

### Task 3: 槽位抽取与缺口评估（不依赖 LLM 的最小版 + 可插拔 LLM）

**Files:**
- Create: `backend/app/orchestrator/slots.py`
- Create: `backend/app/orchestrator/policy.py`
- Modify: `backend/app/api/chat.py`
- Test: `backend/tests/test_slot_policy.py`

- [ ] **Step 1: 测试——能从文本中抽取天数/城市（规则版）**

```python
# backend/tests/test_slot_policy.py
from app.orchestrator.slots import extract_slots

def test_extract_slots_basic():
    s = extract_slots("5 days in Beijing")
    assert s["cities"] == ["Beijing"]
    assert s["days"] == 5
```

- [ ] **Step 2: 最小实现 extract_slots（先硬编码/简单正则）**

```python
# backend/app/orchestrator/slots.py
import re

def extract_slots(text: str) -> dict:
    out = {"cities": [], "days": None}
    m = re.search(r"(\\d+)\\s*day", text, re.I)
    if m:
        out["days"] = int(m.group(1))
    if re.search(r"beijing", text, re.I):
        out["cities"] = ["Beijing"]
    return out
```

- [ ] **Step 3: 缺口评估 policy（决定追问 vs 产出 v1）**

```python
# backend/app/orchestrator/policy.py
def decide_next_action(slots: dict) -> dict:
    if not slots.get("cities"):
        return {"type":"ask", "question":"Which city are you visiting?", "options":["Beijing","Shanghai","Other"]}
    if not slots.get("days"):
        return {"type":"ask", "question":"How many days?", "options":["3","5","7","Other"]}
    return {"type":"generate_itinerary_v1"}
```

- [ ] **Step 4: chat 接入 policy，输出 actions**

Expected: 当缺 slot 时返回 ask action；齐全时返回 generate_itinerary_v1

### Task 4: 行程版本化存储（Trip.current_itinerary + itinerary_versions）

**Files:**
- Create: `backend/app/itinerary/generator.py`
- Modify: `backend/app/api/chat.py`
- Test: `backend/tests/test_itinerary_versioning.py`

- [ ] **Step 1: 测试——生成 v1 并写入版本列表**

```python
from app.itinerary.generator import generate_v1

def test_generate_v1_structure():
    it = generate_v1(cities=["Beijing"], days=3)
    assert it["version"] == "v1"
    assert len(it["days"]) == 3
```

- [ ] **Step 2: 最小实现 generate_v1（结构化 skeleton）**

```python
def generate_v1(cities: list[str], days: int) -> dict:
    city = cities[0]
    return {
        "version": "v1",
        "days": [{"day": i+1, "city": city, "theme": "Highlights"} for i in range(days)]
    }
```

---

## 3. 翻译（文本/图片OCR）与讲解（KB/RAG）

### Task 5: Translation Provider 抽象 + 文本翻译 API

**Files:**
- Create: `backend/app/translation/provider.py`
- Create: `backend/app/api/translate.py`
- Modify: `backend/app/api/router.py`
- Test: `backend/tests/test_translate_text.py`

- [ ] **Step 1: 测试——翻译接口存在并回结构**

```python
from fastapi.testclient import TestClient
from app.main import app
client = TestClient(app)

def test_translate_text_smoke():
    r = client.post("/translate/text", json={"source_lang":"en","target_lang":"zh","text":"Hello"})
    assert r.status_code == 200
    assert "translated_text" in r.json()
```

- [ ] **Step 2: Provider 接口 + Stub 实现（先不接真实供应商）**

```python
class TranslationProvider:
    def translate(self, source_lang: str, target_lang: str, text: str) -> str:
        raise NotImplementedError

class StubProvider(TranslationProvider):
    def translate(self, source_lang: str, target_lang: str, text: str) -> str:
        return f"[{source_lang}->{target_lang}] {text}"
```

### Task 6: 图片翻译 API（OCR→翻译→结构化输出）

**Files:**
- Create: `backend/app/ocr/provider.py`
- Modify: `backend/app/api/translate.py`
- Test: `backend/tests/test_translate_image.py`

- [ ] **Step 1: 测试——图片翻译返回 blocks/lines**
- [ ] **Step 2: OCR Provider Stub（先返回固定文本）**
- [ ] **Step 3: 把 OCR 输出拆行→逐行翻译→返回结构**

> 注：真实 OCR/图片上传（S3/本地）可后置；MVP 先用 base64 上传或 multipart。

### Task 7: KB/RAG 最小闭环（菜名/景点条目 + /kb/search）

**Files:**
- Create: `backend/app/kb/store.py`
- Create: `backend/app/api/kb.py`
- Test: `backend/tests/test_kb_search.py`

- [ ] **Step 1: 内置种子数据（JSON）**
- [ ] **Step 2: 简单关键词检索（contains）先跑通**
- [ ] **Step 3: 返回条目标准结构（title/brief/highlights/tips/keywords）**

---

## 4. Trip.com 酒店：搜索 + 下单工作流（首期不做支付）

### Task 8: Hotel Connector 抽象 + /hotel/search（Stub→真实）

**Files:**
- Create: `backend/app/hotel/provider.py`
- Create: `backend/app/api/hotel.py`
- Modify: `backend/app/api/router.py`
- Test: `backend/tests/test_hotel_search.py`

- [ ] **Step 1: 测试——搜索返回 offers 列表**
- [ ] **Step 2: Stub provider 返回 2-3 条 offer（含取消政策/是否到店付）**
- [ ] **Step 3: 将 offer 写入日志/缓存（DB）便于后续下单引用 offer_id**

### Task 9: /hotel/bookings 创建订单（工作流状态机）

**Files:**
- Create: `backend/app/workflows/hotel_booking.py`
- Modify: `backend/app/api/hotel.py`
- Test: `backend/tests/test_hotel_booking_workflow.py`

- [ ] **Step 1: 测试——创建订单后状态为 created/confirmed**
- [ ] **Step 2: 实现状态机：collecting→creating_booking→confirmed/failed**
- [ ] **Step 3: 写入 EventLog（booking_created/booking_confirmed/booking_failed）**

### Task 10: 订单查询/取消

**Files:**
- Modify: `backend/app/api/hotel.py`
- Test: `backend/tests/test_hotel_cancel.py`

- [ ] **Step 1: GET /hotel/bookings/{id}**
- [ ] **Step 2: POST /hotel/bookings/{id}:cancel**
- [ ] **Step 3: 状态变更写 EventLog**

> 真实 Trip.com 能力对齐：当拿到开放平台字段后，将 Stub 替换为真实调用，并补齐错误码映射与重试策略。

---

## 5. RFP（定制服务/门票/体验）+ 供应商开放入驻（后台 + API）

### Task 11: 供应商入驻（自助注册）+ 分级启用

**Files:**
- Create: `backend/app/api/suppliers.py`
- Create: `backend/app/schemas/supplier.py`
- Modify: `backend/app/api/router.py`
- Test: `backend/tests/test_supplier_onboarding.py`

- [ ] **Step 1: POST /suppliers（注册提交资料，默认 status=pending）**
- [ ] **Step 2: POST /suppliers/{id}:enable（后台人工启用，status=active）**
- [ ] **Step 3: 生成 supplier_api_key（或 token）用于 API 接入**

### Task 12: RFP 创建与分发（用户侧）

**Files:**
- Create: `backend/app/api/rfps.py`
- Create: `backend/app/matching/supplier_matcher.py`
- Test: `backend/tests/test_rfp_create_and_match.py`

- [ ] **Step 1: POST /rfps（从 trip + 对话摘要生成）**
- [ ] **Step 2: matcher：按城市/语种/标签筛选 active 供应商**
- [ ] **Step 3: 创建 rfp_targets（可用 JSONB 存 supplier_ids）并写 EventLog（rfp_created/assigned）**

### Task 13: 供应商报价（API + 后台共用）

**Files:**
- Create: `backend/app/api/supplier_portal.py`（/supplier/*）
- Test: `backend/tests/test_supplier_quote.py`

- [ ] **Step 1: GET /supplier/rfps（只返回分配给自己的）**
- [ ] **Step 2: POST /supplier/rfps/{id}/quotes（提交报价与方案）**
- [ ] **Step 3: POST /supplier/quotes/{id}:revise（多轮改价/改方案）**
- [ ] **Step 4: 每次变更写 EventLog（quote_submitted/quote_revised）**

### Task 14: 用户确认报价 → 生成服务订单（未支付）+ 履约回传

**Files:**
- Create: `backend/app/api/service_orders.py`
- Test: `backend/tests/test_service_order_flow.py`

- [ ] **Step 1: POST /service-orders（chosen_quote_id）创建订单**
- [ ] **Step 2: 供应商 POST /supplier/orders/{id}/status 接单**
- [ ] **Step 3: POST /supplier/orders/{id}/fulfillment 回传集合点/联系人**
- [ ] **Step 4: 服务订单状态机：created→accepted_by_supplier→in_progress→completed**

---

## 6. 前端（Web/手机网页）：聊天 + 行程展示 + 下单入口

### Task 15: 聊天 UI（最小可用）+ actions 渲染

**Files:**
- Modify: `frontend/app/page.tsx`
- Create: `frontend/components/Chat.tsx`
- Create: `frontend/components/ItineraryView.tsx`
- Create: `frontend/lib/api.ts`

- [ ] **Step 1: Chat 输入/消息列表**
- [ ] **Step 2: 调用 POST /chat/messages 并渲染 reply**
- [ ] **Step 3: 若返回 ask action，渲染为按钮选项；点击后回填发送**
- [ ] **Step 4: 若返回 generate_itinerary_v1 action，展示行程结构化视图**

### Task 16: 酒店搜索与下单 UI（最小）

**Files:**
- Create: `frontend/components/HotelSearch.tsx`
- Create: `frontend/components/HotelOffers.tsx`
- Create: `frontend/components/HotelBookingConfirm.tsx`

- [ ] **Step 1: 表单（城市/日期/人数）→ /hotel/search**
- [ ] **Step 2: offers 列表（突出取消政策/到店付）**
- [ ] **Step 3: 确认页（联系人/入住人）→ /hotel/bookings**
- [ ] **Step 4: 订单结果页（booking_id/状态）**

### Task 17: RFP 发起与报价选择 UI（最小）

**Files:**
- Create: `frontend/components/RfpCreate.tsx`
- Create: `frontend/components/RfpQuotes.tsx`
- Create: `frontend/components/ServiceOrder.tsx`

- [ ] **Step 1: 一键从 trip 创建 RFP**
- [ ] **Step 2: 展示报价列表（价格、包含/不含、方案摘要）**
- [ ] **Step 3: 选择报价 → 创建服务订单**

---

## 7. 供应商后台（Web）：入驻资料 + RFP列表 + 报价编辑 + 订单履约

### Task 18: 供应商后台最小页面（Next.js 路由）

**Files:**
- Create: `frontend/app/supplier/page.tsx`（登录入口）
- Create: `frontend/app/supplier/rfps/page.tsx`
- Create: `frontend/app/supplier/rfps/[id]/page.tsx`
- Create: `frontend/app/supplier/orders/page.tsx`

- [ ] **Step 1: 供应商登录（先用 api_key 输入框方式）**
- [ ] **Step 2: RFP 列表 + 详情**
- [ ] **Step 3: 报价编辑器（提交/改价）**
- [ ] **Step 4: 订单页（更新状态、填写集合点/联系人）**

---

## 8. 安全、审计、风控（MVP最低可用）

### Task 19: 权限与审计（EventLog）落地 + 基本限流

**Files:**
- Modify: `backend/app/api/*`（注入 user/supplier 身份）
- Create: `backend/app/security/auth.py`
- Create: `backend/app/middleware/rate_limit.py`
- Test: `backend/tests/test_authz.py`

- [ ] **Step 1: Supplier API 通过 api_key 鉴权**
- [ ] **Step 2: 确保 supplier 只能访问自己的 rfp/quote/order**
- [ ] **Step 3: 对 /chat/messages、/supplier/* 做基本限流**

---

## 9. 自检清单（Spec Coverage）

- [ ] 混合式需求深挖：slots + policy + ask action（Task 2-3）
- [ ] 结构化行程版本化：v1 生成 + 版本存储（Task 4）
- [ ] 多语种双向翻译：/translate/text（Task 5）
- [ ] 图片翻译（OCR）：/translate/image（Task 6）
- [ ] 菜名/景点讲解：/kb/search（Task 7）
- [ ] Trip.com 酒店：search + booking + status/cancel（Task 8-10）
- [ ] RFP：创建/分发/报价/服务订单/履约回传（Task 11-14）
- [ ] 供应商开放入驻：自助注册 + 分级启用（Task 11）
- [ ] 供应商后台 + API：后台页面（Task 18）+ /supplier/*（Task 13-14）

---

## 10. Placeholder Scan（已处理方式）
本计划中若出现 “Stub provider”，其含义是：先以可测试的假实现打通链路，并在获得 Trip.com/OCR/翻译供应商的真实接口细节后替换为真实实现；替换步骤应以新增测试覆盖错误码/字段映射为前置条件。

---

## 11. 执行交接

计划已写好并保存到：`docs/superpowers/plans/2026-05-21-china-travel-agent-mvp-implementation-plan.md`

两种执行方式你选一个：
1) **Subagent-Driven（推荐）**：我按 Task 逐个派发子代理实现，每个 Task 完成后你 review 再继续  
2) **Inline Execution**：我在当前会话中按 Task 批量实现，中间设检查点给你 review

