# VisePanda AI（可运行骨架）

你要求“我直接把整个 task 做完，按阶段给 review 点，再整体修改”，我已在此 workspace 交付一个 **可运行的 MVP 骨架**（后端 + 手机网页 demo），用于验证核心链路：
- 混合式需求深挖（少问问题、多交流：目前为规则版 slots/policy，可替换为 LLM）
- 行程 v1 结构化与版本化写入
- 多语种翻译（文本 + 图片 OCR stub）
- 菜名/景点知识库检索（seed_kb.json）
- 酒店搜索/下单（Trip.com provider stub）
- RFP 报价模式（供应商开放入驻：后台/API 先以 API key 方式跑通）

## 目录

- `docs/superpowers/specs/2026-05-21-china-travel-agent-design.md`：设计文档（已确认）
- `docs/superpowers/plans/2026-05-21-china-travel-agent-mvp-implementation-plan.md`：实施计划
- `backend/`：FastAPI 后端（含测试）
- `frontend/`：静态手机网页 demo（零依赖）

## 快速运行（本地）

### 1) 启动后端

```bash
cd backend
pip install -r requirements.txt --break-system-packages
uvicorn app.main:app --reload --port 8000
```

接口文档：
- http://localhost:8000/docs

说明：
- 后端已开启 CORS（本地联调用，生产需收紧）
- 外部能力对接契约见 `backend/CONNECTOR_CONTRACTS.md`

跑测试：

```bash
pytest -q
```

### 2) 启动前端静态页

```bash
cd frontend
python3 -m http.server 5173
```

打开：
- http://localhost:5173/

## 部署到线上（Vercel，v2.1）

本仓库已加入 Vercel 所需入口与路由：
- `api/index.py` 暴露顶层 ASGI `app`，并把后端挂载到 `/api`
- `vercel.json` 将 `/api/*` rewrite 到 `api/index.py`

> 注意：当前后端使用 SQLite 文件作为数据库。Vercel Functions 的文件系统是临时的，不保证持久化；上线演示没问题，但生产建议换成 Postgres。

部署步骤（推荐 GitHub → Vercel）：
1) 把本仓库推到 GitHub
2) 在 Vercel 新建项目并选择该仓库
3) Framework 选择 **Other**（或保持默认自动识别）
4) Environment Variables（可选）：
   - `LLM_ENABLED=1/0`
   - `LLM_BASE_URL`
   - `LLM_API_KEY`
   - `LLM_MODEL`
5) Deploy 完成后：
   - 前端：`/`（静态页）
   - 后端：`/api/docs`、`/api/health`
