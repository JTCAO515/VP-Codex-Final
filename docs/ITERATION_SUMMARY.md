# China Travel Agent — 五版本迭代进展汇总

> 2026-05-23 | 接手 v2.3 → 完成 v2.8

---

## 概览

| 版本 | 主题 | 测试数 | 提交 |
|------|------|--------|------|
| v2.3 | 接手基线 | 15 | — |
| v2.4 | LLM 深度接入 | 18 | `feat: v2.4 LLM deep integration` |
| v2.5 | 酒店系统升级 | 20 | `feat: v2.5 hotel system with seed data` |
| v2.6 | 支付闭环 | 24 | `feat: v2.6 payment system` |
| v2.7 | 供应商后台 | 24 | `feat: v2.7 supplier admin portal` |
| v2.8 | 集成测试 | 26 | `feat: v2.8 E2E tests + supplier auth fix` |

---

## v2.4: LLM 深度接入

**目标**: 从规则版骨架升级为真正的 AI 对话体验

**核心变更**:
- NUWA API 为默认 LLM provider (`api.nuwaflux.com`)
- 新增 `orchestrate()` 单次 LLM 调用：意图识别 + slot 抽取 + 自然回复 + 行程生成
- 替换三个硬编码函数：`detect_intent`（关键词匹配）、`_render_reply`（固定字符串）、`generate_itinerary_v1`（空壳行程）
- LLM 生成真实行程（含景点/时段/备注的 time_blocks）
- 多语言自适应（用户什么语言输入就什么语言回复）
- 规则版兜底：LLM 不可用时自动回退

**改动文件**: `llm_provider.py`, `prompts.py`, `orchestrator.py`, `routers/chat.py`

---

## v2.5: 酒店系统升级

**目标**: 替换 stub 酒店数据为真实中国酒店信息

**背景**: Trip.com API 需商务合作申请，调整为多 provider 架构 + 真实种子数据。

**核心变更**:
- 65 家真实中国酒店种子数据（北京 20、上海 20、成都 20、广州 5）
- 价格区间 ¥120-5,800/晚，含星级/评分/地址/设施/退订政策
- `SeedHotelProvider` 替代 `StubHotelProvider` 为默认 provider
- `get_hotel_provider()` 工厂函数，环境变量 `HOTEL_PROVIDER=seed|stub` 切换
- 酒店搜索返回完整对象：name/price/stars/rating/amenities/cancel_policy

**改动文件**: `seed_hotels.py`(新), `providers.py`, `routers/hotel.py`

---

## v2.6: 支付闭环

**目标**: 酒店 booking 和服务 order 支持完整支付流程

**核心变更**:
- `Payment` 模型：entity_type/entity_id/user_id/amount/status/provider
- 支付状态机：`pending_payment → paid → (refunding → refunded)`
- API: `POST /payments`, `GET /payments/{id}`, `POST :pay`, `POST :refund`
- 用户隔离：按 user_id 归属校验，越权访问返回 404
- Mock 支付 provider（开发/测试用，可替换为微信/支付宝）

**改动文件**: `models.py`, `routers/payments.py`(新), `main.py`

---

## v2.7: 供应商后台

**目标**: 供应商可登录、查看 RFP、提交报价、管理订单

**核心变更**:
- 供应商 API: `GET /supplier/me`（API key 验证）
- 前端三页面：登录（`/supplier/login`）、仪表盘（`/supplier/dashboard`）、RFP 详情+报价（`/supplier/rfp`）
- 暗色主题 UI，与主站视觉一致
- 认证方式修正为 `X-API-Key` header

**改动文件**: `routers/supplier_portal.py`, `frontend/supplier/*.html`(新)

---

## v2.8: 集成测试 + 部署准备

**目标**: 端到端验证 + 生产部署就绪

**核心变更**:
- E2E 测试 1: 完整用户旅程（登录→对话→搜酒店→下单→支付→查看历史）
- E2E 测试 2: 供应商完整流程（注册→API 验证→查看 RFP→查看订单）
- 修复供应商认证 header（`X-API-Key`）

**改动文件**: `tests/test_e2e.py`(新), 前端 auth 修复

---

## 系统全景

```
游客 → / (Grok 主页) → 输入需求
  ↓
/chat 对话页
  ├── LLM 编排 (v2.4): 意图识别 → slot 抽取 → 自然回复 → 行程生成
  ├── 酒店搜索 (v2.5): 65 家真实中国酒店 → 下单
  ├── 支付 (v2.6): create → pay → refund
  └── 右侧面板: 行程/酒店/订单历史

供应商 → /supplier/login
  ├── 仪表盘 (v2.7): RFP 列表 + 订单列表
  └── 报价: 查看 RFP → 提交报价 → 管理订单
```

---

## 关键指标

| 指标 | 开始 (v2.3) | 结束 (v2.8) |
|------|-------------|-------------|
| 测试数 | 15 | 26 (+73%) |
| 后端路由 | 10 | 11 (+payments) |
| 数据模型 | 7 | 8 (+Payment) |
| 前端页面 | 4 | 7 (+3 supplier) |
| 酒店数据 | 2 stub | 65 真实种子 |
| LLM | 仅 slot 抽取 | 完整对话编排 |
| 支付 | 无 | 完整闭环 |
| 供应商 | 仅 API | 完整后台 |

---

## 生产部署待办

1. **Supabase 环境变量**: `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `DATABASE_URL`
2. **NUWA API 密钥**: `LLM_ENABLED=1`, `LLM_API_KEY=<key>`
3. **关闭测试 bypass**: 移除 `AUTH_TEST_BYPASS=1`
4. **Vercel 部署**: 推送到 GitHub → 导入 Vercel → 配置环境变量
5. **Trip.com 商务合作**: 申请 partner API 后启用 `TripComProvider`
6. **微信/支付宝商户号**: 替换 mock payment provider

---

*汇总完成: 2026-05-23 | 五版本全部交付*
