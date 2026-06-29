# VisePanda — 产品需求文档

## 1. 产品定位

VisePanda 是一个面向外国人来中国旅行的英文原生 AI 管家。用户不需要学习中文 App，也不需要反复问当地人；他们可以通过持续对话，让 VisePanda 实时生成、修改、解释并保存可执行的中国旅行计划。

一句话定位：

> VisePanda is an AI China travel butler that turns conversation into a live, practical trip canvas.

## 2. 目标用户

- 第一次来中国的国际游客：需要路线、签证、支付、交通、翻译、住宿位置建议。
- 高意向自由行用户：不想跟团，但希望快速得到靠谱行程。
- 在中国的外国人、留学生、商务访客：需要周末游、短途游、落地工具。
- 英文旅行规划者：替朋友、客户、家人快速制作中国旅行草案。

核心场景：

- 用户在 Chat 里说明旅行天数、城市、兴趣和限制。
- VisePanda 在右侧聊天区回答，并在左侧实时生成 day-by-day 行程画布。
- 用户继续要求“less tiring”“more food-focused”“add a cooking class”等，画布实时更新。
- 用户可以从 Trips 查看已保存/草稿/可分享行程，并回到 Chat 继续调整。

## 3. 功能清单（MVP）

| 功能 | 优先级 | 描述 | 验收标准 |
|------|--------|------|----------|
| AI Butler 工作台 | P0 | 默认进入 Chat / Butler 页面，桌面左侧为画布，右侧为聊天。 | 用户打开 `/chat` 后可以看到 live trip canvas 和 Ask VisePanda 聊天区。 |
| Live Trip Canvas | P0 | 展示 trip summary 和 Day 时间线，每天直接显示 Morning / Afternoon / Evening 三段。 | 用户可以看到 Day 1、城市、Morning、Afternoon、Evening、酒店、交通、节奏和预算提示；顶部不再出现 Visa / Payment / Booking / Less tiring / Food-focused 五个任务框。 |
| Day Detail Drawer | P0 | 用户点击某一天后，从侧边抽屉查看并修改该日完整行程。 | 默认不显示每日详情；点击 Edit 后显示三段日程、酒店、交通、备注字段，用户可本地修改并保存回画布。 |
| DeepSeek AI Pipeline | P0 | 服务端 `/api/chat` 调用 DeepSeek V4 Flash 生成结构化 canvas patch，同时保留 deterministic mock fallback。 | 配置 `DEEPSEEK_API_KEY` 时返回 `deepseek` mode；缺 key、API 失败或输出不合法时返回 `mock` mode，画布仍可更新。 |
| Chat Panel | P0 | 支持两列建议问题、输入框、发送按钮、聊天记录、busy 状态。 | 初始不显示演示对话；用户发送后出现用户和 VisePanda 消息；AI 回复后刷新 2 个上下文建议问题。 |
| Trips Dashboard | P0 | 行程库骨架，展示静态 saved trips、状态筛选、概览指标和 Continue in Chat 入口。 | 用户打开 `/trips` 可以看到 Your trips、至少 3 张 trip cards、All/Draft/Ready/Shared 筛选，并可点击 Continue in Chat 返回 `/chat`。 |
| Canvas Patch 应用 | P0 | 将 AI 返回的 patch 合并到当前 TripState。 | summary、days、alerts 能按规则更新，alert 按 type/title 去重。 |
| Placeholder Tabs | P1 | Explore、Tools、Account 暂时占位。 | 用户能打开三个 tab，并看到 reserved for later 的说明。 |
| Warm New Chinese Visual | P1 | 使用暖纸色、水墨背景、朱砂、金色、墨棕、实底纸卡。 | 页面不使用半透明玻璃聊天框；背景不影响可读性。 |
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
→ 建议问题刷新为 2 个上下文相关 follow-up questions
→ 左侧画布生成/更新 Beijing + Shanghai 行程
→ 左侧画布按 Day 1 / Day 2 / Day 3 时间线显示 Morning / Afternoon / Evening
→ 用户点击某一天 Edit
→ 右侧抽屉显示完整每日行程并允许本地编辑
→ 用户保存后，该日卡片同步更新
```

```text
用户打开 /trips
→ 看到 Your trips 行程库
→ 浏览 Draft / Ready / Shared 状态
→ 点击某个筛选按钮
→ 行程卡和概览指标同步更新
→ 点击 Continue in Chat
→ 回到 /chat 继续和 AI 管家调整行程
```

## 5. 非功能需求

- 性能：MVP 首屏保持轻量，避免真实第三方 SDK 阻塞加载。
- AI 稳定性：DeepSeek 调用失败、缺少 key、返回非 JSON 或 patch 不合法时必须 fallback。
- 安全：`DEEPSEEK_API_KEY` 只在服务端环境变量读取，不传给前端、不提交到仓库。
- 终端适配：当前迭代优先电脑横屏端；移动竖屏保持可用，细节适配后续再做。
- 桌面布局：Chat 工作台固定为横屏一页，页面本身不纵向滚动；长内容在聊天、行程列表或详情抽屉内部滚动。
- 部署方式：Vercel，生产域名 `go2china.space`。
- 数据安全：当前不写真实用户数据，不接真实 Supabase。
- 可维护性：AI provider、mock fallback、canvas patch、Trip types、Trips mock data 分层清晰。
- 可访问性：导航、聊天输入、筛选按钮、状态提示需要可被键盘和辅助技术访问。

## 6. 不做什么（明确排除）

- 不做没有 fallback 的真实 AI：DeepSeek 已接入，但任何真实模型错误都必须回落到 mock。
- 不做真实 Supabase 登录/同步：待 Trip/Canvas 数据模型稳定后再做。
- 不做 Trips 真实保存：`v0.1.8` 只做静态 dashboard 骨架。
- 不做真实 Trip.com / Meituan / Amap API：必须先验证真实能力边界。
- 不做完整 Explore：当前仍占位，后续再做城市、景点、美食、住宿。
- 不做完整 Tools：当前仍占位，后续再做翻译、支付、签证、汇率、地铁等工具。
- 不恢复顶部五个任务框：Visa / Payment / Booking / Less tiring / Food-focused 已从 Canvas 顶部移除。
- 不在主界面展开长篇每日详情：当前主画布显示 Morning / Afternoon / Evening 摘要，完整详情和修改通过抽屉完成。
- 不做后台管理：当前没有运营后台需求。
- 不做支付或预订闭环：VisePanda 先做规划和管家，不做订单交易。
- 不做 MVP 目的地背景切换：北京/上海等场景化水墨背景已纳入后续迭代，不在当前版本实现。
