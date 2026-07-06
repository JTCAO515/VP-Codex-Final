# 外部能力接口契约（Connector Contracts）

本文件用于把 MVP 中的 **Stub Provider** 替换为真实供应商能力时，工程侧需要对齐的字段与行为约定，避免“接上就炸/字段不一致”。

> MVP 代码中位置：`backend/app/providers.py`

---

## 1) Trip.com(携程) 酒店（HotelProvider）

### 1.1 search() 输出字段约定

`HotelProvider.search(city, check_in, check_out, adults) -> list[offer]`

每个 `offer` 建议至少包含：
- `offer_id`（string，后续下单引用的唯一标识）
- `hotel_name`（string）
- `price`（object）
  - `amount`（number）
  - `currency`（string，如 CNY）
  - `unit`（string，如 night）
- `pay_type`（string）
  - 推荐枚举：`pay_at_hotel`（到店付）、`guarantee`（担保）、`prepay`（预付，后续才做）
- `cancel_policy`（string，展示给用户的“可理解”文本；或结构化后再渲染）
- `notes`（string[]，如“Passport required for check-in”）

### 1.2 create_booking() 输出字段约定

`HotelProvider.create_booking(offer_id, guest_info) -> booking_result`

`booking_result` 建议包含：
- `provider_booking_ref`（string，供应商侧订单号/参考号）
- `status`（string）
  - 允许：`confirmed | failed | created`
- `raw`（object，可选：供应商原始响应，用于排障）

### 1.3 错误码映射（建议）
- 供应商网络错误/超时 → 5xx + 可重试提示
- offer 不可用/价格变更 → 409（需要用户重新确认）
- 入住人信息不符合（证件/姓名）→ 400（指出缺哪个字段）

---

## 2) 文本翻译（TranslationProvider）

`translate(source_lang, target_lang, text) -> translated_text`

建议补充：
- 专有名词保护策略（地点/菜名/酒店名）：输出“原文+转写+解释”的组合结构（后续可扩展为结构化返回）。

---

## 3) 图片 OCR（OCRProvider）

`extract_text_lines(image_bytes) -> list[str]`

建议真实实现返回：
- `lines`（按阅读顺序）
- （后续增强）返回 bounding boxes 与置信度，用于菜单表格结构化

---

## 4) KB/RAG

MVP 使用 `seed_kb.json` + contains 检索。

后续对接向量库时建议保持条目结构稳定：
- `type`（dish/poi/tip）
- `title_zh/title_en/pinyin`
- `brief/highlights/tips/keywords`

