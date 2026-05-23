# China Travel Agent — 五版本迭代规划

> 接手 v2.3 后，四个方向（LLM / Trip.com / 支付 / 供应商后台）分五个版本迭代。

---

## 版本路线图

```
v2.3 (现状)
  │
  ├─ v2.4: LLM 深度接入        ← 核心体验质变
  │
  ├─ v2.5: Trip.com 真对接     ← 酒店闭环
  │
  ├─ v2.6: 支付闭环            ← 交易可行
  │
  ├─ v2.7: 供应商后台           ← 服务端就绪
  │
  └─ v2.8: 集成 + 部署          ← 可上线
```

---

## v2.4: LLM 深度接入

**目标**: 把规则版骨架升级为真正的 AI 对话体验。NUWA API 接入，替换硬编码回复，生成真实行程。

| 现状 | → 目标 |
|------|--------|
| LLM 仅用于 slot 抽取 | LLM 驱动完整对话：意图识别 + 自然回复 + 行程生成 |
| `_render_reply` 硬编码字符串 | LLM 生成多语种自然回复 |
| `generate_itinerary_v1` 空壳 | LLM 生成有景点/时段/描述的完整 itinerary |
| `detect_intent` 关键词匹配 | LLM 意图分类 |
| 仅中/英两个语言路径 | LLM 原生多语言支持 |

**In Scope**:
- NUWA API (`api.nuwaflux.com`) 接入为默认 LLM provider
- 替换 `_render_reply` → LLM 对话回复
- 替换 `generate_itinerary_v1` → LLM 行程生成
- 替换 `detect_intent` → LLM 意图识别
- 多语言自动适配（用户什么语言输入就用什么语言回复）
- prompt 工程：保持"少问问题、多交流"策略

**Out of Scope**:
- 流式输出（SSE）
- 上下文压缩/长对话管理
- Function calling 替代现有 action 机制

**后续 v2.5 计划**: Trip.com API 对接（酒店搜索/下单/取消真实调用）

---

## v2.5: Trip.com 真对接

**目标**: 替换 `StubHotelProvider` 为真实 Trip.com API，实现酒店搜索→下单→取消完整闭环。

| 现状 | → 目标 |
|------|--------|
| `StubHotelProvider` 返回假数据 | `TripComProvider` 调用真实 API |
| 搜索结果固定两条 | 真实酒店列表（价格/评分/图片/位置） |
| booking 总是 confirm | 真实下单 + 状态回调 |

**In Scope**:
- Trip.com API 对接（search / booking / cancel / booking status）
- 酒店列表渲染（前端展示价格/图片/评分/位置）
- booking 状态机（created → confirmed → cancelled/failed）
- 错误处理与降级

**Out of Scope**:
- 机票/火车票
- 酒店评论/详情页

**后续 v2.6 计划**: 支付对接（微信/支付宝），订单支付状态

---

## v2.6: 支付闭环

**目标**: 用户可以对酒店 booking 和服务 order 进行支付，订单状态可追踪。

| 现状 | → 目标 |
|------|--------|
| 无支付 | 微信支付 / 支付宝对接 |
| 订单只有 created/cancelled | 增加 pending_payment → paid → refunded 状态 |
| ServiceOrder 无支付信息 | 绑定支付记录 |

**In Scope**:
- 支付网关抽象层（Provider 模式，先微信支付）
- 酒店 booking 支付
- 服务 order 支付
- 支付状态 webhook 回调
- 退款流程

**Out of Scope**:
- 账单/发票系统
- 多币种结算

**后续 v2.7 计划**: 供应商管理后台

---

## v2.7: 供应商后台

**目标**: 供应商可以注册、登录、查看 RFP、提交报价、管理订单。

| 现状 | → 目标 |
|------|--------|
| 供应商仅 API（无 UI） | 供应商后台 Web 界面 |
| 供应商注册无入口 | 注册/登录/资料管理 |
| RFP 报价仅 API | 可视化报价提交 + 修改 |
| 订单仅 API | 订单列表 + 状态更新 |

**In Scope**:
- 供应商注册/登录（基于现有 auth 体系或独立）
- RFP 列表 + 报价提交 UI
- 报价修改/撤回
- 订单管理（查看/确认/完成）
- 简单数据看板

**Out of Scope**:
- 供应商审核流程
- 供应商评级体系

**后续 v2.8 计划**: 端到端测试 + Vercel 生产部署

---

## v2.8: 集成测试 + 部署

**目标**: 五个版本的功能端到端可用，部署到 Vercel 生产环境。

| 现状 | → 目标 |
|------|--------|
| 本地 SQLite | Supabase Postgres 生产数据库 |
| `AUTH_TEST_BYPASS=1` | 生产级 Supabase Auth |
| 15 tests | 全链路 E2E 测试 |
| 本地开发 | Vercel 生产部署 |

**In Scope**:
- 全链路 E2E 测试（游客→登录→对话→下单→支付→查看历史）
- 生产环境配置（Supabase Postgres + Auth）
- Vercel 部署 + 环境变量
- 冒烟测试
- 文档完善

**Out of Scope**:
- CI/CD pipeline
- 监控/告警
- 性能优化

---

*创建: 2026-05-23*
