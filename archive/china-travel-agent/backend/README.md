# China Travel Agent MVP (Backend)

这是一个可运行的 MVP 后端（FastAPI + SQLite），用于验证：
- 对话式需求深挖（最小规则版 slots + policy）
- 行程版本化（v1）
- 多语种翻译（stub）与图片翻译（OCR stub）
- 菜名/景点知识库检索（seed_kb.json）
- 酒店搜索/下单（Trip.com connector 的 stub）
- RFP 报价与服务订单（供应商 API key 鉴权）
- 事件留痕（EventLog）

## 运行

1) 安装依赖（在 `backend/` 下）：

```bash
pip install -r requirements-dev.txt --break-system-packages
```

2) 启动：

```bash
uvicorn app.main:app --reload --port 8000
```

3) 打开接口文档：
- http://localhost:8000/docs

## 测试

```bash
pytest -q
```

## 重要说明（MVP取舍）
- DB 使用 SQLite 方便本地跑通；生产建议换 Postgres（字段大多为 JSON，可平滑迁移）。
- 翻译/OCR/Trip.com 均为 Stub Provider：已做成可替换 Provider，后续接真实供应商时只需替换实现并补充错误码映射测试。
- 对接真实供应商前请先看：`CONNECTOR_CONTRACTS.md`（字段与行为契约）。

## LLM 追问策略（如何启用）

当前版本默认使用**规则抽取**（无外部依赖，便于跑通）。已预留 OpenAI 兼容的 LLM Provider：
- 代码：`app/llm_provider.py`
- 启用方式（环境变量）：
  - `LLM_ENABLED=1`
  - `LLM_BASE_URL=https://api.openai.com/v1`（或你的网关地址）
  - `LLM_API_KEY=...`
  - `LLM_MODEL=...`

说明：
- “少问问题、多选项、关键缺口才问、每轮最多1问”的策略在 `app/orchestrator.py`。
- 启用 LLM 后，`/chat/messages` 会优先使用 LLM 做 **JSON 槽位抽取 + 置信度 + 可选追问**；失败或禁用会自动回退规则抽取。
- 抽取提示词：`app/prompts.py`（可按你的业务词表/风控要求持续迭代）。

## “深挖需求”策略要点（当前已落地）
- 槽位扩展：`party`（人数/关系/是否亲子）、`constraints`（饮食/体力/必去/避开/到离时间等）已纳入抽取目标。
- “高影响槽位”优先级：在 RFP 意图下，`party` 属于高影响字段（影响报价/履约），缺失或低置信度会优先追问。
- 防止重复追问：使用 `slot_state._last_asked` 避免连续追问同一字段。

## 否定/改口（已支持的最小机制）
对话中用户可能会改口（例如“其实不去北京，改去上海”）。当前支持两类机制：
- **remove**：从列表字段移除部分项（例如从 `cities` 移除 `Beijing`）
- **clear**：清空整个字段（例如清空 `interests`）

LLM 抽取时可通过 `clear/remove` 字段输出；规则抽取中也对常见模式做了最小支持（如 `not Beijing` / `不去北京`）。

另外，已支持对 `constraints.must_see` 的否定移除（例如“不去故宫/Actually not Forbidden City”）。
