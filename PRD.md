# VisePanda — 产品需求文档

## 1. 产品定位

VisePanda 是一个面向外国人来中国旅行的英文原生 AI 管家。用户不需要学习中文 App，也不需要反复问当地人；他们可以通过持续对话让 VisePanda 实时生成、修改并解释可执行的中国旅行计划。

一句话定位：

> VisePanda is an AI China travel butler that turns conversation into a live, practical trip canvas.

## 2. 目标用户

- 第一次来中国的国际游客：需要路线、签证、支付、交通、翻译、住宿位置建议。
- 高意向自由行用户：不想跟团，但希望快速得到靠谱行程。
- 在中国的外国人、留学生、商务访客：需要周末游、短途游、落地工具。
- 英文旅行规划者：替朋友、客户、家人快速制作中国旅行草案。

核心场景：

- 用户说：“I am visiting China for the first time for 5 days.”
- VisePanda 在右侧聊天区回答，并在左侧实时生成 day-by-day 行程。
- 用户继续说：“Make it less tiring and keep hotels convenient.”
- VisePanda 更新画布，将行程变得更轻松，并调整住宿区域建议。
- 顶部五张任务/提醒卡同步提醒签证、支付、预订、交通、语言、风险等实际问题。

## 3. 功能清单（MVP）

| 功能 | 优先级 | 描述 | 验收标准 |
|------|--------|------|----------|
| AI Butler 工作台 | P0 | 默认进入 Chat / Butler 页面，桌面左侧为画布，右侧为聊天。 | 用户打开 `/chat` 后可以看到 live trip canvas 和 Ask VisePanda 聊天区。 |
| Live Trip Canvas | P0 | 展示 trip summary、顶部任务/提醒卡、day summary cards。 | 用户可以看到 Day 1、城市、节奏和一句当日总结；主界面不直接展开每日详情，也不出现独立 Practical Reminder 区块。 |
| Day Detail Drawer | P0 | 用户点击某一天后，从侧边抽屉查看该日完整行程。 | 默认不显示每日详情；点击 View details 后显示时间段、餐饮、住宿、交通、备注，并可关闭。 |
| DeepSeek AI Pipeline | P0 | 服务端 `/api/chat` 调用 DeepSeek V4 Flash 生成结构化 canvas patch，同时保留 deterministic mock fallback。 | 配置 `DEEPSEEK_API_KEY` 时返回 `deepseek` mode；缺 key、API 失败或输出不合法时返回 `mock` mode，画布仍可更新。 |
| Chat Panel | P0 | 支持 prompt chips、输入框、发送按钮、聊天记录、busy 状态。 | 用户输入内容并点击 Send 后，聊天记录出现用户和 VisePanda 消息；请求中按钮禁用。 |
| Canvas Patch 应用 | P0 | 将 AI 返回的 patch 合并到当前 TripState。 | summary、days、alerts 能按规则更新，alert 按 type/title 去重。 |
| Placeholder Tabs | P1 | Trips、Explore、Tools、Account 只做占位。 | 用户能打开四个 tab，并看到 coming next / reserved for later 的说明。 |
| Warm New Chinese Visual | P1 | 使用暖纸色、水墨背景、朱砂、金色、墨棕、实底纸卡。 | 页面不使用半透明玻璃聊天框；背景不影响可读性。 |
| API Placeholders | P1 | 提供 `/api/chat` 真实 AI route 和其他 placeholder API。 | `/api/chat` 可在真实和 fallback 间切换；其他 API 返回 `{ ok: true }`。 |
| 自动化测试 | P1 | 覆盖 DeepSeek provider、mock fallback、patch reducer、env、API、组件、e2e。 | `npm run test`、`npm run build`、`npm run test:e2e` 通过。 |

## 4. 用户流程

```text
用户打开 /chat
→ 看到 VisePanda 顶部导航
→ 看到左侧 Live Trip Canvas 和右侧 Ask VisePanda
→ 用户输入 “I am visiting China for the first time for 5 days”
→ 点击 Send
→ 浏览器请求 /api/chat
→ 服务端优先调用 DeepSeek V4 Flash；失败时回落到 mock
→ 右侧聊天出现用户消息和 VisePanda 回复
→ 左侧画布生成/更新 Beijing + Shanghai 行程
→ 顶部五张任务/提醒卡显示支付、签证等提醒
→ 用户点击某一天 View details
→ 右侧抽屉显示完整每日行程
```

## 5. 非功能需求

- 性能：MVP 首屏保持轻量，避免真实第三方 SDK 阻塞加载。
- AI 稳定性：DeepSeek 调用失败、缺少 key、返回非 JSON 或 patch 不合法时必须 fallback。
- 安全：`DEEPSEEK_API_KEY` 只在服务端环境变量读取，不传给前端、不提交到仓库。
- 终端适配：当前迭代优先电脑横屏端；移动竖屏保持可用，细节适配后续再做。
- 桌面布局：Chat 工作台固定为横屏一页，页面本身不纵向滚动；长内容在聊天、行程列表或详情抽屉内部滚动。
- 部署方式：Vercel。
- 数据安全：当前仍不写真实用户数据，不接真实 Supabase。
- 可维护性：AI provider、mock fallback、canvas patch、Trip types 分层清晰。
- 可访问性：导航、聊天输入、状态提示需要可被键盘和辅助技术访问。

## 6. 不做什么（明确排除）

- ❌ 无 fallback 的真实 AI：DeepSeek 已接入，但任何真实模型错误都必须回落到 mock。
- ❌ 真实 Supabase 登录/同步：待 Trip/Canvas 数据模型稳定后再做。
- ❌ 真实 Trip.com / Meituan / Amap API：必须先验证真实能力边界，不能假集成。
- ❌ 完整 Explore：当前只占位，后续再做城市、景点、美食、住宿。
- ❌ 完整 Tools：当前只占位，后续再做翻译、支付、签证、汇率、地铁等工具。
- ❌ 主界面展开每日详情：当前主画布只显示每日一句总结，完整详情必须通过抽屉查看。
- ❌ 后台管理：当前没有运营后台需求。
- ❌ 支付或预订闭环：VisePanda 先做规划和管家，不做订单交易。
- ❌ MVP 目的地背景切换：北京/上海等场景化水墨背景已纳入后续迭代，不在当前版本实现。
