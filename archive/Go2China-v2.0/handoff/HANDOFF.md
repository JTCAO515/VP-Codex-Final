# 交接包说明（给另一个 AI）

本交接包包含：
1) 项目代码与资料（backend/frontend/api/vercel 配置、spec、plan、说明文档）
2) 本次对话的整理版文字记录（已对敏感信息做脱敏处理）

> 版本标识：v2.1.1（已在 GitHub 打 tag；本包为本地同等内容的打包交付）

---

## 1. 项目目标（一句话）
面向入境游客的中国旅行对话式 AI Agent：通过“少问问题、多交流”的方式深挖需求，生成结构化行程，并支持酒店直订（stub）与本地服务 RFP 报价模式下单（MVP 不做支付）。

## 2. 当前已实现能力（MVP骨架）
- `/api/chat/messages`：多轮 slot_state 记忆 + 置信度；每轮最多 1 个关键追问；2-4 选项；关键槽位齐了先出行程 v1，附带可选细化建议
- 深挖需求槽位：cities/days/pace/budget/interests/party/constraints（dietary、walking_level、must_see/must_avoid、到离时间等）
- 否定/改口：支持 clear/remove（例如不去北京、不要购物、不去故宫等）
- KB：`/api/kb/search`（seed_kb.json + contains 检索）
- 翻译：`/api/translate/text`（stub），`/api/translate/image`（OCR stub）
- 酒店：`/api/hotel/search`、`/api/hotel/bookings`、查询/取消（stub provider）
- 供应商/RFP：供应商入驻、启用；RFP 创建/分发；供应商报价/改价；生成服务订单并回传履约信息；服务订单状态机最小校验
- 测试：backend 下 `python -m pytest -q` 全绿

## 3. 本地运行
后端：
```bash
cd backend
pip install -r requirements-dev.txt --break-system-packages
uvicorn app.main:app --reload --port 8000
```
前端（静态 demo）：
```bash
cd frontend
python3 -m http.server 5173
```
打开：http://localhost:5173/

## 4. Vercel 部署（同域名 /api）
- 入口：`api/index.py`（暴露顶层 ASGI `app`，并把后端挂载到 `/api`）
- 路由：`vercel.json`（将 `/api/*` rewrite 到 `api/index.py`）
- 依赖：根目录 `requirements.txt`

注意：后端当前使用 SQLite 文件数据库。Vercel Functions 文件系统不保证持久化，适合演示；生产建议迁移 Postgres。

## 5. 另一个 AI 建议继续做的方向
1) 接入真实 LLM（启用 LLM_ENABLED=1）并基于置信度做更少追问（低置信度 + 高影响才问）
2) 把行程从 v1 skeleton 升级为 v2/v3（时间块、交通、餐饮、POI 卡片、讲解与翻译输出）
3) Trip.com（携程）酒店 API 真对接（按 `backend/CONNECTOR_CONTRACTS.md`）
4) 把 SQLite 迁移为 Postgres（Vercel 推荐外部 DB）

