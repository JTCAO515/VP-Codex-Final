# 对话整理（脱敏版）

> 说明：为便于在另一个 AI 上继续推进，本文件按“需求 → 方案 → 实现 → 部署 → 排错”整理了关键对话结论与决定；已对敏感信息（Token 等）做脱敏处理。

## 1) 需求与方向
- 目标：面向入境游客的中国旅行 Agent
- 核心：**混合式需求深挖（少问问题、多交流）** → 生成结构化自由行行程/攻略 → 可下单
- 下单范围：
  - 酒店：走 Trip.com(携程)（MVP 可用 stub，后续真对接）
  - 定制服务/门票/体验：走 **RFP 报价模式**（供应商报价/改方案 → 用户确认生成服务订单），首期不做支付
- 供应商接入：**API + 后台（推荐）**
- 供货方：当前免费开放入驻；后期可签约供应商

## 2) 方案选型
- 推荐架构：**单 Orchestrator + 工作流状态机 + 工具/Connector**
  - 对话大脑负责理解/决策
  - 下单/RFP/状态流转用确定性状态机保证可控
  - 翻译/OCR/KB/RAG 作为独立工具

## 3) Spec 与实施计划
- 已输出 spec：自由行结构化行程、酒店下单、RFP 与供应商、翻译与KB、风控与指标、路线图
- 已输出实施计划（MVP tasks）

## 4) 实现交付（MVP骨架）
- FastAPI + SQLite（本地），静态前端 demo（手机网页）
- API：chat、trips、translate、kb、hotel（stub）、suppliers、rfps、supplier portal、service orders
- 追问策略迭代：
  - 每轮最多 1 问
  - 2-4 选项
  - 关键缺口优先；非关键先产出行程，再给可选细化建议
  - 引入 LLM JSON 抽取入口（OpenAI兼容，可关闭），带置信度
  - 高影响槽位：RFP 优先追问 party
  - 否定/改口：clear/remove（不去某城市、不购物、不去故宫等）
- 版本：定位 v2.1 / v2.1.1

## 5) GitHub 与部署
- 代码已推送到 GitHub（私有仓库），并打 tag（v2.1、v2.1.1）
- Vercel 部署策略：同域名
  - 前端：`/`
  - 后端：`/api/*`
  - 入口：`api/index.py` + `vercel.json` rewrites
- Vercel deploy 问题排查：
  - 原因：被识别为 multiple services（backend/frontend 都被当成服务）
  - 修复：移除 `backend/requirements.txt`（改为 `backend/requirements-dev.txt`），根目录保留 `requirements.txt` 供 Vercel Python Runtime 安装依赖；并配置 excludeFiles

## 6) 安全注意
- 对话中曾出现 Token 以明文方式提供；迁移到另一个 AI 时务必避免再次在对话中直接粘贴 Token。
- 建议后续采用 Deploy Key / CI 方式推送，而非频繁在对话中提供 PAT。

