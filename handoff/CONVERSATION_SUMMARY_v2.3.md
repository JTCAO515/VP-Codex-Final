# 对话整理（脱敏版，v2.3）

> 说明：用户要求“打包过往对话以便在其他 AI 接手”。由于原始对话中包含敏感信息（token），本文件提供 **可执行的结论与决策记录**，并对敏感信息做脱敏处理，不包含任何密钥或 token。

## 1) 产品方向与范围
- 目标：入境游客中国旅行 Agent
- 核心体验：**少问问题、多交流**；每轮最多 1 问（2-4 选项）；关键信息足够时先出行程 v1，再给可选细化
- 下单闭环：
  - 酒店：Trip.com（当前 stub，未来真对接）
  - 本地服务/门票/体验：RFP 报价 → 供应商报价/改价 → 生成服务订单（首期不做支付）
- 供应商接入：API + 后台（推荐）

## 2) 技术路线关键决定
- 前端：先用静态页（Vanilla），快速出效果；后端 FastAPI
- 部署：Vercel 同域名（前端 `/`，后端 `/api/*`）
- 登录：采用 **Supabase Auth + Google OAuth**
- 历史记录：必须用外部 DB（Supabase Postgres），Vercel Functions 本地文件系统不可靠

## 3) 迭代摘要
### v2.1/v2.1.1：MVP 骨架 + Vercel deploy 修复
- 修复 Vercel “multiple services” 误判：移除 `backend/requirements.txt`，改为 `backend/requirements-dev.txt`，根目录保留 `requirements.txt`
- 新增 Vercel Python Runtime 入口与 rewrites：`api/index.py` + `vercel.json`

### v2.2：Grok 风格主页 + 登录 + 对话历史（基础）
- 主页：Grok 暗黑极简，只有一个开始对话框；背景为“祥云 + 山水”高透明图案
- `/chat`：对话页 + 左侧会话列表
- 游客试用：本地保存最近 3 个会话
- 登录：Supabase Google 登录 + callback
- 历史：后端写入 `chat_messages`；登录后可读 `trips` 与 `messages`

### v2.3：订单类权限隔离 + /chat 右侧详情面板
- 订单/RFP/booking 全部强制登录，用户隔离按 Trip/RFP 归属校验
- 新增 trip 维度聚合列表接口（bookings/rfps/orders）
- 前端 `/chat` 右侧面板 Tabs：行程 / 酒店 / RFP&订单（登录态加载，游客提示登录）

## 4) 安全记录
- 对话中曾出现 token 明文传递；后续建议使用 Deploy Key/CI 或本地 push，避免在对话中粘贴 token。
- 测试 bypass（`AUTH_TEST_BYPASS` + `Bearer test:<user_id>`）仅用于单测，生产必须关闭。

