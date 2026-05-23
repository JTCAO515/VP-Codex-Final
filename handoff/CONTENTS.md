# 交接包目录

- `handoff/HANDOFF_v2.3.md`：给接手 AI 的总览（如何跑、如何部署、关键路径）
- `handoff/CONVERSATION_SUMMARY_v2.3.md`：对话结论（脱敏）
- `docs/superpowers/specs/`：设计文档
- `docs/superpowers/plans/`：实现计划
- `frontend/`：静态站点（/、/chat、/auth/callback）
- `backend/`：FastAPI（业务逻辑、DB、鉴权、测试）
- `api/index.py`：Vercel Python Runtime 入口（挂载 backend 到 /api）
- `vercel.json`：Vercel rewrites（/、/chat、/auth/callback、/assets、/api）
- `requirements.txt`：Vercel 安装依赖

