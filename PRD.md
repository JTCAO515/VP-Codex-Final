# VisePanda — 产品需求文档

## 1. 产品定位

VisePanda 是一个面向外国人来中国旅行的英文原生 AI 管家。用户不需要学习中文 App，也不需要反复问当地人；他们可以通过持续对话让 VisePanda 实时生成、修改并解释可执行的中国旅行计划。

一句话定位：

> VisePanda is an AI China travel butler that turns conversation into a live, practical trip canvas.

## 2. 目标用户

### 用户画像

- 第一次来中国的国际游客：需要路线、签证、支付、交通、翻译、住宿位置建议。
- 高意向自由行用户：不想跟团，但希望快速得到靠谱行程。
- 在中国的外国人、留学生、商务访客：需要周末游、短途游、落地工具。
- 英文旅行规划者：替朋友、客户、家人快速制作中国旅行草案。

### 核心场景

- 用户说：“I am visiting China for the first time for 5 days.”
- VisePanda 在右侧聊天区回答，并在左侧实时生成 day-by-day 行程。
- 用户继续说：“Make it less tiring and keep hotels convenient.”
- VisePanda 更新画布，将行程变得更轻松，并调整住宿区域建议。
- Butler Rails 同步提醒签证、支付、交通、语言、风险等实际问题。

## 3. 功能清单（MVP）

| 功能 | 优先级 | 描述 | 验收标准 |
|------|--------|------|----------|
| AI Butler 工作台 | P0 | 默认进入 Chat / Butler 页面，桌面左侧为画布，右侧为聊天。 | 用户打开 `/chat` 后可以看到 live trip canvas 和 Ask VisePanda 聊天区。 |
| Live Trip Canvas | P0 | 展示 trip summary、day-by-day itinerary、Butler Rails。 | 用户可以看到 Day 1、城市、时间段、餐饮、住宿、交通、提醒。 |
| Mock AI Pipeline | P0 | 第一阶段不用真实 AI，用 deterministic mock 根据用户消息返回 canvas patch。 | 用户发送首次来中国、放慢节奏、预算、食物、酒店、签证、支付等消息后，画布会更新。 |
| Chat Panel | P0 | 支持 prompt chips、输入框、发送按钮、聊天记录。 | 用户输入内容并点击 Send 后，聊天记录出现用户和 VisePanda 消息。 |
| Canvas Patch 应用 | P0 | 将 mock AI 返回的 patch 合并到当前 TripState。 | summary、days、alerts 能按规则更新，alert 按 type/title 去重。 |
| Placeholder Tabs | P1 | Trips、Explore、Tools、Account 只做占位。 | 用户能打开四个 tab，并看到“coming next / reserved for later”的说明。 |
| Warm New Chinese Visual | P1 | 使用暖纸色、水墨背景、朱砂、金色、墨棕、实底纸卡。 | 页面不使用半透明玻璃聊天框；背景不影响可读性。 |
| API Placeholders | P1 | 提供 `/api/chat` mock route 和其他 placeholder API。 | API 返回 `{ ok: true }`，缺少 key 不报错。 |
| 自动化测试 | P1 | 覆盖 mock AI、patch reducer、env、API、组件、e2e。 | `npm run test`、`npm run build`、`npm run test:e2e` 通过。 |

## 4. 用户流程

```text
用户打开 /chat
→ 看到 VisePanda 顶部导航
→ 看到左侧 Live Trip Canvas 和右侧 Ask VisePanda
→ 用户输入 “I am visiting China for the first time for 5 days”
→ 点击 Send
→ 右侧聊天出现用户消息和 VisePanda 回复
→ 左侧画布生成/更新 Beijing + Shanghai 行程
→ Butler Rails 显示支付、签证等提醒
→ 用户继续输入 “Make this trip less tiring”
→ 画布更新为 Relaxed pace
```

## 5. 非功能需求

- 性能：MVP 首页首屏应保持轻量，避免真实第三方 SDK 阻塞加载。
- 终端适配：PC 和 Mobile 必须可用；390px 宽度不能出现横向溢出或导航遮挡核心内容。
- 部署方式：Vercel。
- 数据安全：第一阶段不写真实用户数据，不接真实 Supabase。
- 稳定性：缺少 env key 时仍可完整体验 mock butler flow。
- 可维护性：AI、canvas patch、Trip types、placeholder tabs 分层清晰。
- 可访问性：导航、聊天输入、状态提示需要可被键盘和辅助技术访问。

## 6. 不做什么（明确排除）

- ❌ 真实 AI provider：第一阶段只验证交互骨架，避免 key 和输出质量阻塞。
- ❌ 真实 Supabase 登录/同步：待 Trip/Canvas 数据模型稳定后再做。
- ❌ 真实 Trip.com / Meituan / Amap API：必须先验证真实能力边界，不能假集成。
- ❌ 完整 Explore：当前只占位，后续再做城市、景点、美食、住宿。
- ❌ 完整 Tools：当前只占位，后续再做翻译、支付、签证、汇率、地铁等工具。
- ❌ 后台管理：当前没有运营后台需求。
- ❌ 支付或预订闭环：VisePanda 先做规划和管家，不做订单交易。
- ❌ MVP 目的地背景切换：北京/上海等场景化水墨背景已纳入后续迭代，不在第一阶段实现。

