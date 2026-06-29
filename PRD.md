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
| Trips Dashboard | P0 | 行程库，展示状态筛选、概览指标和 Continue in Chat 入口；已登录且 Supabase 已配置时展示用户真实保存的行程，否则展示静态示例行程。 | 未登录/未配置 Supabase 时,用户打开 `/trips` 看到示例 trip cards 和筛选；已登录且配置 Supabase 时，看到该账号真实保存的行程（可能为空），点击 Continue in Chat 带着 trip id 回到 `/chat` 并恢复该行程画布。 |
| Account 登录 | P0 | 顶部导航的账号图标点开悬浮窗口完成登录/账号管理，guest 模式始终可用。 | 配置 Supabase 后，用户点击头部账号图标打开悬浮窗口，可用邮箱密码登录/注册，或点击 Continue with Google 用 Google 账号登录；未配置 Supabase 时悬浮窗口显示 guest-only 提示，不阻断任何功能；不再有独立 `/account` 页面。 |
| Account 资料管理 | P0 | 已登录用户在账号悬浮窗口里可以更改显示名称、更改密码、登出。 | 已登录用户在悬浮窗口点击 Change name 可以提交新名称并看到 Name updated 提示；点击 Change password 可以提交新密码并看到 Password updated 提示；点击 Log out 后悬浮窗口回到未登录状态。 |
| Save to Trips | P0 | Chat 工作台可以把当前 canvas 保存为一条 Supabase trip + canvas version。 | 已登录且配置 Supabase 时,点击 Save to Trips 会创建/更新 `trips` 行和一条 `canvas_versions` 快照，并把当前对话消息写入 `messages`；未登录或未配置时给出对应提示文案，不会报错或崩溃。 |
| Guest Draft 自动迁移 | P0 | 未登录用户在 Chat 里产生的草稿自动存在浏览器 `localStorage`，登录后自动同步到 Supabase。 | 未登录用户发消息后,草稿写入 `localStorage` 并在刷新/重新打开页面后还原；该用户随后通过 magic link 登录,草稿在不需要再点 Save to Trips 的情况下自动保存为一条真实 trip,并清除本地草稿。 |
| Trip Detail 页面 | P0 | `/trips/[id]` 展示单条行程的完整详情；真实 trip 显示完整 Live Trip Canvas，示例 trip 显示摘要卡。 | 已登录且配置 Supabase 时,点击某条真实保存行程的 View details 会看到完整 Day 时间线画布；点击示例行程会看到摘要信息和"这是示例行程"提示；访问不存在的 id 会看到 Trip not found 提示。 |
| 归档与分享链接 | P0 | Trip detail 页面支持 Mark as Ready / Archive / Restore from archive 状态切换，以及生成/撤销只读公开分享链接 `/share/[token]`。 | 已登录用户在自己的 trip detail 页面可以切换 draft/ready/archived 状态；点击 Get share link 会生成 token 并展示完整分享 URL；点击 Revoke share link 会清除 token；任何人（包括未登录访客）打开有效的 `/share/[token]` 链接都能只读查看该行程画布，看不到聊天记录；token 被撤销后该链接显示"Link not available"。 |
| Canvas Patch 应用 | P0 | 将 AI 返回的 patch 合并到当前 TripState。 | summary、days、alerts 能按规则更新，alert 按 type/title 去重。 |
| Explore 骨架 | P1 | `/explore` 通过 provider abstraction 展示城市、景点、美食、住宿。 | 用户打开 `/explore` 能看到城市筛选按钮，点击切换城市后下方更新该城市的景点、美食、住宿列表；当前由 `lib/explore` 静态 provider 提供数据，UI 不直接依赖任何第三方 API。 |
| Explore Add to Trip | P1 | Explore 每个景点/美食/住宿条目有 Add to Trip 按钮，点击后跳转到 Chat 并通过真实 AI pipeline 把这条内容加入当前行程画布。 | 用户在 `/explore` 点击某个条目的 Add to Trip，会跳转到 `/chat` 并自动发送一条描述该条目的消息；Chat 收到回复后画布按正常 Canvas Patch 流程更新，地址栏的 `add` 参数被清除；该流程不会绕开 `/api/chat`、不会在 Explore 组件里直接拼装 TripDay 数据。 |
| Placeholder Tabs | P1 | Tools 暂时占位。 | 用户能打开 Tools tab，并看到 reserved for later 的说明。 |
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
用户在任意页面点击头部账号图标
→ 悬浮窗口打开，输入邮箱和密码并点击 Sign in（或点击 Continue with Google）
→ 悬浮窗口显示已登录邮箱，并提供 Change name / Change password / Log out
→ 回到 /chat，点击 Save to Trips
→ 当前 canvas 写入 Supabase trips + canvas_versions + messages
→ 打开 /trips，看到刚保存的真实行程
→ 点击 Continue in Chat，带着 trip id 回到 /chat 并恢复该行程画布
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
- 不做完整账号体系：当前只做 Supabase 邮箱密码登录、Google OAuth 登录和 guest draft 自动迁移，不做找回密码邮件、其他第三方登录（Apple/微信等）、多设备草稿合并。
- 归档与分享链接已完成基础版本：支持 draft/ready/archived 状态切换和只读公开分享链接；不做多人协作编辑、分享链接访问权限分级、分享链接过期时间设置。
- 不做真实 Trip.com / Meituan / Amap API：Explore 当前用静态 provider，真实第三方能力边界必须先验证。
- Explore 已有静态骨架（城市、景点、美食、住宿）和基础 Add to Trip 入口（跳转 Chat 并通过 AI pipeline 加入画布），但不做收藏、真实地图、预订能力或在 Explore 内直接预览/编辑画布。
- 不做完整 Tools：当前仍占位，后续再做翻译、支付、签证、汇率、地铁等工具。
- 不恢复顶部五个任务框：Visa / Payment / Booking / Less tiring / Food-focused 已从 Canvas 顶部移除。
- 不在主界面展开长篇每日详情：当前主画布显示 Morning / Afternoon / Evening 摘要，完整详情和修改通过抽屉完成。
- 不做后台管理：当前没有运营后台需求。
- 不做支付或预订闭环：VisePanda 先做规划和管家，不做订单交易。
- 不做 MVP 目的地背景切换：北京/上海等场景化水墨背景已纳入后续迭代，不在当前版本实现。
