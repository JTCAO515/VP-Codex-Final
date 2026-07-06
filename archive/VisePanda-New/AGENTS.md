# AGENTS.md — China Travel Agent

> 面向入境游客的中国旅行对话式 Agent。
> 本文件是项目级 Agent 指令。接手此项目的 AI 必须先读本文件。

---

## 项目目标

做一个"少问问题、多交流"的对话式旅行 Agent：
1. 深挖需求 → 生成结构化行程
2. 酒店搜索/下单（Trip.com stub）
3. 本地服务 RFP 报价 → 供应商竞价 → 服务订单
4. 多语种翻译 + 菜名/景点知识库
5. 登录后可回看对话/行程/订单历史

---

## 技术栈

| 层 | 技术 |
|----|------|
| 前端 | Vanilla HTML/CSS/JS（零依赖） |
| 后端 | FastAPI + SQLAlchemy + SQLite（本地）/ Supabase Postgres（生产） |
| 认证 | Supabase Auth + Google OAuth + JWT |
| 部署 | Vercel（前端 `/`，后端 `/api/*`） |
| 测试 | pytest（15 tests） |

---

## 目录结构

```
china-travel-agent/
├── frontend/
│   ├── index.html          ← / Grok 风格主页
│   ├── chat.html + chat.js ← /chat 对话页 + 右侧面板
│   ├── app.js              ← 共享 JS（auth helpers、工具函数）
│   └── auth/callback.html  ← Supabase OAuth 回调
├── backend/
│   ├── app/
│   │   ├── main.py         ← FastAPI app 工厂
│   │   ├── auth.py         ← Supabase JWT 验证 + test bypass
│   │   ├── models.py       ← 7 个 ORM 模型
│   │   ├── db.py           ← SQLAlchemy session 管理
│   │   ├── prompts.py      ← LLM prompt 模板
│   │   ├── orchestrator.py ← 对话编排逻辑
│   │   ├── providers.py    ← 外部服务 stub（Trip.com 等）
│   │   └── routers/        ← 10 个路由模块
│   └── tests/              ← 15 tests
├── api/index.py            ← Vercel Python Runtime 入口
├── vercel.json             ← Vercel rewrites
├── docs/superpowers/       ← 设计文档 + 实现计划
└── handoff/                ← 交接文档
```

---

## 本地运行

```bash
# 后端
cd backend
pip install -r requirements-dev.txt
export AUTH_TEST_BYPASS=1   # 本地测试用
uvicorn app.main:app --reload --port 8000

# 前端
cd frontend
python3 -m http.server 5173

# 测试
cd backend && AUTH_TEST_BYPASS=1 pytest -q
```

---

## 关键约定

### 认证体系
- 游客：`guest_id`（localStorage），仅保存最近 3 个会话
- 登录：Supabase Google OAuth → JWT `sub` 获取 `user_id`
- 测试：`AUTH_TEST_BYPASS=1` 时 `Bearer test:<user_id>` 直接视为登录
- 订单类接口（rfps/service-orders/hotel bookings）必须登录

### 数据隔离
- 所有订单类操作按 Trip/RFP 归属隔离
- 越权访问返回 404（不泄露存在性）

### 前端口令
- 保持零依赖 Vanilla
- 移动端优先，右侧面板在移动端通过 header button 切换

---

## 当前状态 (v2.3)

| 功能 | 状态 |
|------|------|
| Grok 风格主页 + 背景 | ✅ |
| /chat 对话 + 左侧会话列表 | ✅ |
| Supabase Google 登录 | ✅ |
| 对话历史持久化 | ✅ |
| 订单/RFP/booking 权限隔离 | ✅ |
| /chat 右侧详情面板（行程/酒店/RFP&订单） | ✅ |
| 行程结构化生成 | ✅ |
| 多语种翻译 | ✅ |
| 知识库检索 | ✅ |
| 酒店搜索 stub | ✅ |
| RFP 报价流程 | ✅ |

### 测试：15 passed ✅

---

## 下一步候选

1. **真 LLM 接入** — 当前 orchestrator 是规则版，接入 LLM 增强对话自然度
2. **Trip.com 真对接** — 替换 stub，实现真实酒店搜索/下单
3. **支付闭环** — ServiceOrder 目前无支付
4. **供应商后台 UI** — 目前只有 API，需要管理界面
5. **多语言 UI** — 前端国际化
6. **Vercel 生产部署** — 配置 Supabase Postgres + 环境变量上线

---

*最后更新：2026-05-23 | 接手自 handoff v2.3*
