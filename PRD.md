# VisePanda — 产品需求文档

## 1. 产品定位

VisePanda 是一个专为**非跟团自由行（FIT）外国来华游客**打造的一站式英文原生 AI 旅游管家。它打破了传统旅行工具与中文本土应用的隔阂，集成了**行程规划、酒店预订、景点/餐饮选择、实时翻译**等功能，并深度打通了**交通、支付、地图**等多插件能力。用户无需在各类中文 App 之间低效跳转，通过与 VisePanda 的持续对话即可实时生成、修改、执行完整的中国自由行计划。

一句话定位：

> VisePanda is a one-stop AI travel butler for independent foreign travelers in China, integrating itinerary planning, booking, translation, and local utility plugins (transit, payment, maps) into a live, actionable trip canvas.

## 2. 目标用户

- **独立自由行外国游客（FIT）**：不希望跟团，渴望自由探索中国，但面临信息壁垒（如景点实名预约、复杂的国内地铁与支付配置等）的国际游客。
- **首次来华探索者**：需要一站式集成方案来解决签证、Alipay/WeChat Pay 支付绑定、eSIM 联网、以及乘车打车的痛点。
- **在华常驻外籍人士**：需要深度定制化周末游、短途游，并需要强大的本地餐饮/娱乐插件推荐。

核心场景：

- 用户在 Chat 里说明旅行天数、城市、兴趣和限制。
- VisePanda 在右侧聊天区回答，并在左侧实时生成 day-by-day 行程画布。
- 用户继续要求"less tiring""more food-focused""add a cooking class"等，画布实时更新。
- 用户可以从 Trips 查看已保存/草稿/可分享行程，并回到 Chat 继续调整。

## 3. 功能清单（MVP）

| 功能 | 优先级 | 描述 | 验收标准 |
|------|--------|------|----------|
| AI Butler 工作台 | P0 | 默认进入 Chat / Butler 页面，桌面左侧为画布，右侧为聊天。 | 用户打开 `/chat` 后可以看到 live trip canvas 和 Ask VisePanda 聊天区。 |
| Live Trip Canvas | P0 | 展示 trip summary 和 Day 时间线，每天直接显示 Morning / Afternoon / Evening 三段。 | 用户可以看到 Day 1、城市、Morning、Afternoon、Evening、酒店、交通、节奏和预算提示；顶部不再出现 Visa / Payment / Booking / Less tiring / Food-focused 五个任务框。 |
| Day Detail Drawer | P0 | 用户点击某一天后，从侧边抽屉查看并修改该日完整行程。 | 默认不显示每日详情；点击 Edit 后显示三段日程、酒店、交通、备注字段，用户可本地修改并保存回画布。 |
| DeepSeek AI Pipeline | P0 | 服务端 `/api/chat` 调用 DeepSeek V4 Flash 生成结构化 canvas patch，同时保留 deterministic mock fallback。 | 配置 `DEEPSEEK_API_KEY` 时返回 `deepseek` mode；缺 key、API 失败或输出不合法时返回 `mock` mode，画布仍可更新。 |
| Chat Panel | P0 | 支持两列建议问题、输入框、发送按钮、聊天记录、busy 状态。 | 初始不显示演示对话；用户发送后出现用户和 VisePanda 消息；AI 回复后刷新 2 个上下文建议问题。 |
| Trips Dashboard | P0 | 行程库，展示状态筛选、概览指标、状态说明和 Continue in Chat 入口；已登录且 Supabase 已配置时展示用户真实保存的行程，否则展示静态示例行程。 | 未登录/未配置 Supabase 时,用户打开 `/trips` 看到示例 trip cards、状态筛选和 Draft/Ready/Shared/Archived 说明；已登录且配置 Supabase 时，看到该账号真实保存的行程（可能为空），点击 Continue in Chat 带着 trip id 回到 `/chat` 并恢复该行程画布。 |
| Account 登录 | P0 | 顶部导航的账号图标点开悬浮窗口完成登录/账号管理，guest 模式始终可用。 | 配置 Supabase 后，用户点击头部账号图标打开悬浮窗口，可用邮箱密码登录/注册，或点击 Continue with Google 用 Google 账号登录；未配置 Supabase 时悬浮窗口显示 guest-only 提示，不阻断任何功能；不再有独立 `/account` 页面。 |
| Account 资料管理 | P0 | 已登录用户在账号悬浮窗口里可以更改显示名称、更改密码、登出。 | 已登录用户在悬浮窗口点击 Change name 可以提交新名称并看到 Name updated 提示；点击 Change password 可以提交新密码并看到 Password updated 提示；点击 Log out 后悬浮窗口回到未登录状态。 |
| Save to Trips | P0 | Chat 工作台可以把当前 canvas 保存为一条 Supabase trip + canvas version。 | 已登录且配置 Supabase 时,点击 Save to Trips 会创建/更新 `trips` 行和一条 `canvas_versions` 快照，并把当前对话消息写入 `messages`；未登录或未配置时给出对应提示文案，不会报错或崩溃。 |
| Guest Draft 自动迁移 | P0 | 未登录用户在 Chat 里产生的草稿自动存在浏览器 `localStorage`，登录后自动同步到 Supabase。 | 未登录用户发消息后,草稿写入 `localStorage` 并在刷新/重新打开页面后还原；该用户随后登录,草稿在不需要再点 Save to Trips 的情况下自动保存为一条真实 trip,并清除本地草稿。 |
| Trip Detail 页面 | P0 | `/trips/[id]` 展示单条行程的完整详情；真实 trip 显示完整 Live Trip Canvas，示例 trip 显示摘要卡，并解释当前状态含义和下一步。 | 已登录且配置 Supabase 时,点击某条真实保存行程的 View details 会看到完整 Day 时间线画布和当前状态说明；点击示例行程会看到摘要信息、"这是示例行程"提示和状态说明；访问不存在的 id 会看到 Trip not found 提示。 |
| 归档与分享链接 | P0 | Trip detail 页面支持 Mark as Ready / Archive / Restore from archive 状态切换，以及生成/撤销只读公开分享链接 `/share/[token]`。 | 已登录用户在自己的 trip detail 页面可以切换 draft/ready/archived 状态；点击 Get share link 会生成 token 并展示完整分享 URL；点击 Revoke share link 会清除 token；任何人（包括未登录访客）打开有效的 `/share/[token]` 链接都能只读查看该行程画布，看不到聊天记录；token 被撤销后该链接显示"Link not available"。 |
| Canvas Patch 应用 | P0 | 将 AI 返回的 patch 合并到当前 TripState。 | summary、days、alerts 能按规则更新，alert 按 type/title 去重。 |
| Butler Reminders | P1 | Trip Canvas 行程时间线下方展示轻量 `ButlerReminders` 告警列表，每个 alert 类型链接到对应 Tools 分类，便于一键跳转获取工具建议。 | 用户在 Trip Canvas 看到 alert（如 visa 类型）时，点击其 action 链接会跳转到 `/tools?category=visa-and-entry` 并自动预选签证入境分类；`booking`/`weather` 等无对应 Tools 分类的 alert 显示为纯文本，不生成链接；空 alerts 列表不渲染任何内容。 |
| Explore 骨架 | P1 | `/explore` 通过 provider abstraction 展示城市、景点、美食、住宿，当前静态覆盖北京、上海、成都、西安、广州、杭州、苏州、重庆。 | 用户打开 `/explore` 能看到城市筛选按钮，点击切换城市后下方更新该城市的景点、美食、住宿列表；当前由 `lib/explore` 静态 provider 提供数据，UI 不直接依赖任何第三方 API。 |
| Explore Provider Readiness | P1 | Explore 展示当前 provider 模式、覆盖范围、候选第三方数据源和下一步接入重点。 | 用户打开 `/explore` 能看到当前为 static curated provider，候选数据源包含 Amap / Trip.com / Meituan / Tripadvisor，并说明下一步优先验证 POI/place-detail。 |
| Explore Add to Trip | P1 | Explore 每个景点/美食/住宿条目有 Add to Trip 按钮，点击后跳转到 Chat 并通过真实 AI pipeline 把这条内容加入当前行程画布。 | 用户在 `/explore` 点击某个条目的 Add to Trip，会跳转到 `/chat` 并自动发送一条描述该条目、要求 VisePanda 重新平衡路线的消息；Chat 收到回复后画布按正常 Canvas Patch 流程更新，地址栏的 `add` 参数被清除；该流程不会绕开 `/api/chat`、不会在 Explore 组件里直接拼装 TripDay 数据。 |
| Tools 骨架 | P1 | `/tools` 通过 provider abstraction 展示签证入境、支付设置、翻译、汇率、地铁、eSIM/VPN、应急 7 个分类的静态参考清单，并支持分类深链、结构化分组、离线 pocket notes 和 API priority 说明。 | 用户打开 `/tools` 能看到 7 个分类按钮，点击切换分类后右侧更新该分类的摘要、实用建议、分组清单、离线可读提示和后续 API 接入优先级；打开 `/tools?category=payment-setup` 等 URL 时自动选中对应分类，无效分类回退默认；当前由 `lib/tools` 静态 provider 提供内容，不包含实时汇率/翻译/签证规则查询。 |
| Tools Provider Readiness | P1 | Tools 展示当前 provider 模式、覆盖范围、候选真实数据源和下一步接入重点。 | 用户打开 `/tools` 能看到当前为 static travel tools provider，候选数据源包含 exchange-rate、machine translation、visa rules 和 transit data，并说明优先验证实时汇率 API。 |
| Destination-aware Background | P1 | Trip Canvas 根据当前行程目的地自动切换水墨背景氛围。 | 当前行程包含 Beijing 时页面使用长城/故宫风格暖朱砂氛围；包含 Shanghai 时使用外滩/江南园林风格；Hangzhou/Suzhou 使用江南湖景氛围；Chongqing 使用山城江景氛围；未知目的地回落默认水墨背景。 |
| Translator 页面 | P1 | `/translate` 独立翻译页面，含文字翻译、OCR 扫描翻译、常用短语词典。 | 用户打开 `/translate` 能看到三 Tab 布局（文字翻译/扫描翻译/短语词典）；文字翻译支持 EN↔ZH 双向、翻译+拼音、TTS 朗读、一键复制；扫描翻译支持上传或拍照、Canvas 缩放、OCR 识别、自动翻译+TTS；短语词典展示 6 类常用短语和 3 类特殊词语（景点/菜名/标识），每条带 TTS；翻译 API 通过 DeepSeek (`DEEPSEEK_API_KEY`)，OCR 通过 OCR.space (`OCR_SPACE_API_KEY` 或免费 key)，均为服务端代理；语音转文字（STT）规划中，暂显示"Coming soon"。 |
| Icon Navigation | P1 | 顶部 Chat / Trips / Explore / Tools / Translate 使用明确的线性图标辅助识别。 | 用户在任意页面顶部导航看到 Chat 气泡、Trips 行李、Explore 指南针、Tools 工具、Translate 语言图标；不再显示字母占位。 |
| Warm New Chinese Visual | P1 | 使用暖纸色、水墨背景、朱砂、金色、墨棕、实底纸卡。 | 页面不使用半透明玻璃聊天框；背景不影响可读性。 |
| 自动化测试 | P1 | 覆盖 DeepSeek provider、mock fallback、patch reducer、env、API、组件、e2e。 | `npm run test`、`npm run build`、`npm run test:e2e` 通过。 |

## 4. 用户流程

```text
用户打开 /chat
→ 看到 VisePanda 顶部导航
→ 看到左侧 Live Trip Canvas 和右侧 Ask VisePanda
→ 用户输入 "I am visiting China for the first time for 5 days"
→ 点击 Send
→ 浏览器请求 /api/chat
→ 服务端优先调用 DeepSeek V4 Flash；失败时回落到 mock
→ 右侧聊天出现用户消息和 VisePanda 回复
→ 建议问题刷新为 2 个上下文相关 follow-up questions
→ 左侧画布生成/更新 Beijing + Shanghai 行程
→ 左侧画布按 Day 1 / Day 2 / Day 3 时间线显示 Morning / Afternoon / Evening
→ 画布底部 Butler Reminders 区域显示 visa/payment 等告警
→ 用户点击 visa alert 的 "Review visa checklist" 链接
→ 跳转到 /tools?category=visa-and-entry 查看签证清单
→ 用户回到 /chat，点击某一天 Edit
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
- 桌面布局：Chat、Trips、Explore、Tools 固定为横屏一页，页面本身不纵向滚动；长内容在聊天、行程列表、Explore columns、Tools detail 或详情抽屉内部滚动；标题、摘要和筛选区保持紧凑，把更多高度留给主要内容。
- 部署方式：Vercel，生产域名 `go2china.space`。
- 数据安全：当前不写真实用户数据，不接真实 Supabase。
- 可维护性：AI provider、mock fallback、canvas patch、Trip types、Trips mock data 分层清晰。
- 可访问性：导航、聊天输入、筛选按钮、状态提示需要可被键盘和辅助技术访问。

## 6. 不做什么（明确排除）

- 不做没有 fallback 的真实 AI：DeepSeek 已接入，但任何真实模型错误都必须回落到 mock。
- 不做完整账号体系：当前只做 Supabase 邮箱密码登录、Google OAuth 登录和 guest draft 自动迁移，不做找回密码邮件、其他第三方登录（Apple/微信等）、多设备草稿合并。
- 归档与分享链接已完成基础版本：支持 draft/ready/archived 状态切换和只读公开分享链接；不做多人协作编辑、分享链接访问权限分级、分享链接过期时间设置。
- Explore 已接入高德 POI API（`v0.1.27`）：`/api/explore/amap` 服务端路由通过 `AMAP_API_KEY` 调用高德地图 POI 搜索，返回景点/餐饮/住宿真实数据，API 不可达时回落 8 城静态数据；不做收藏、真实地图、预订能力或在 Explore 内直接预览/编辑画布；Trip.com / Meituan 尚未接入。
- Tools Currency 分类已接入 ExchangeRate-API 实时汇率（`v0.1.27`）：`/api/exchange-rate` 服务端路由通过 `EXCHANGE_RATE_API_KEY` 获取 CNY 基准汇率，注入 Currency 分类内容（每小时 ISR 刷新）；翻译 API、签证规则 API、地铁数据 API 暂未接入。
- 不恢复顶部五个任务框：Visa / Payment / Booking / Less tiring / Food-focused 已从 Canvas 顶部移除；`ButlerReminders` 组件也已从 TripCanvas 移除（v0.1.28），文件保留。
- 不在主界面展开长篇每日详情：当前主画布显示 Morning / Afternoon / Evening 摘要，完整详情和修改通过抽屉完成。
- 不做后台管理：当前没有运营后台需求。
- 不做支付或预订闭环：VisePanda 先做规划和管家，不做订单交易。
- 目的地背景切换已完成第一版氛围切换，但不做按城市加载真实长城/故宫/外滩图片资产；当前通过同一水墨底图和目的地 CSS 场景层保持性能。

## 7. 后续迭代规划（仅规划，暂不实现）

### 社区页面（Phase 11）

- 独立的 `/community` 页面，用户可以共享 trips、上传旅途照片、分享行程、景点、美食点评。
- 与高德/美团 API 联动：景点信息、餐厅评分、票务价格可来自高德 POI 或美团数据接口。
- 核心能力：公开 trip 发布与浏览、照片上传（Supabase Storage）、点赞/收藏、城市维度的热门榜单（景点/餐厅）。
- 推荐第三方数据源：高德地图 POI 搜索 API、美团外卖/餐饮 API（评分/评价）、Supabase Realtime（社区动态推送）。
- 前提依赖：Supabase auth 已接入；需新增 `posts`、`photos`、`likes` Supabase 表；需评估高德/美团 API 申请资质。
## v0.1.30 Translator Requirement Update

Translator is now a four-tab travel translation tool: Text, Scan, Voice, and Phrases.

MVP acceptance additions:

- Text translation uses Aliyun Bailian Qwen `qwen-mt-flash` through `/api/translate/text`, returning translation plus pinyin when the target is Chinese.
- Scan translation uses Aliyun Bailian Qwen `qwen3.5-ocr` through `/api/translate/ocr`; OCR.space is no longer part of the product path.
- TTS playback uses `/api/translate/tts` with `qwen3-tts-instruct-flash`; browser `speechSynthesis` is no longer the primary playback path.
- Voice translation uses `/api/translate/stt` with `qwen3-asr-flash`; users can record, upload audio, or paste a public audio file URL, then the transcript is automatically translated.
- All Bailian/Qwen keys remain server-side in environment variables. The browser only calls internal `/api/translate/*` routes.

Explicit exclusions for this iteration:

- No direct client-side DashScope calls.
- No permanent audio storage yet; longer production-grade recordings should later upload to Supabase Storage or OSS before recognition.

## v0.1.31 Community Requirement Update

Community is now a local interactive MVP rather than a static placeholder.

MVP acceptance additions:

- `/community` shows a compact membership strip with five levels: Bamboo Guest, Panda Explorer, Silk Road Insider, Dragon Pass, and VisePanda Concierge.
- Feed supports local posting, type filters, city filter, likes, saves, comments, and read-more detail. Current state is stored in browser `localStorage`.
- Photos supports local photo-card publishing and likes. It does not upload real image files yet.
- Hot Spots keeps city/category filters and Add to Trip behavior, routing through `/chat?add=` and the existing AI planning pipeline.
- Account displays a panda avatar and lets the user choose from six bundled panda avatars. The selected avatar is stored locally and reused by local community posts/photos.

Explicit exclusions:

- No Supabase community persistence in this iteration.
- No real avatar upload or photo file upload in this iteration.
- No paid membership, points ledger, payment, or entitlement enforcement yet.

## v0.1.32 Tools Requirement Update

Tools is now a card-drawer reference surface rather than a category sidebar with visible implementation status.

MVP acceptance additions:

- `/tools` shows six non-Translate travel tool cards: Visa and entry, Payment setup, Currency, Metro, eSIM/VPN, and Emergency.
- Each card shows only the category name before selection.
- No category detail is visible by default unless the URL contains a valid `?category=<tool-category-id>` deep link.
- Clicking a card opens that category's checklist drawer; clicking the active card closes it and clears the category URL param.
- `/translate` remains the dedicated translation product area and is not duplicated inside Tools.
- Any retained language reminder entry should route to `/translate`, not to a removed Tools category.
- The Tools page must not show implementation/provider metadata such as provider labels, coverage summaries, "next integration" copy, candidate API-source strings, or category API-priority planning blocks.

Explicit exclusions:

- Do not restore a Translate category inside `/tools`.
- Do not show internal API/provider roadmap strings in user-facing Tools UI.

## v0.1.33 Visual Layout Requirement Update

The product must feel more like a polished AI China travel desk than a generic dashboard.

MVP acceptance additions:

- Desktop landscape remains the primary target for this phase.
- Chat, Trips, Explore, Tools, Translate, and Community stay inside one viewport; long lists and page bodies scroll internally.
- Page titles, summaries, filters, and metadata are compact enough to leave more room for itinerary/cards/content.
- The visual language uses warm paper, ink-line dividers, cinnabar accents, compact serif headings, and solid paper inputs.
- Chat prompt suggestions render as compact two-column rows, not tall cards.
- Chat composer, Save to Trips, and status copy remain visually separated and do not overlap.
- Translator and Community page-level labels are clean and readable; no visible mojibake should appear in primary page headers or tab labels.

Explicit exclusions:

- No new business logic, AI provider changes, Supabase schema changes, or provider API changes in this iteration.
- Mobile portrait polish remains deferred to a later dedicated pass.

## v0.1.42 Translator Requirement Update

Translator is now a unified, locale-aware travel translation workspace rather than four separate cards.

MVP acceptance additions:

- `/translate` shows two equal upper text panels: source on the left and translated output on the right.
- Translation direction follows the active website language and Chinese: EN/ES/AR/JA/KO/FR ↔ ZH.
- Text input, image OCR, voice recording, and TTS continue to use server-side Aliyun Bailian Qwen routes.
- Image input exposes exactly two user-facing actions: Upload Image and Take Photo. Take Photo is visible but disabled on desktop and reserved for mobile camera capture.
- Voice translation exposes one Record button only; audio upload and public audio URL controls are removed from the traveler UI.
- Common phrases and special terms sit horizontally below the two text panels as lightweight support content.
- Desktop landscape `/translate` must fit inside one viewport; page-level body/main scrolling is not allowed, and overflowing results/support content must scroll internally.
- The surface should feel clean and background-forward: avoid heavy translucent cards, use hairline dividers and very low-opacity paper backing only where text needs contrast.
- Account avatar selection uses the six new panda PNG assets supplied in `public/avatars`.

Explicit exclusions:

- No translation history, account sync, Supabase persistence, or uploaded audio storage.
- No full mobile redesign beyond preserving functional stacked layout and mobile camera capture affordance.

## v0.1.44 Mobile Portrait Requirement Update

MVP acceptance additions:

- On screens ≤ 760px, the 6-tab navigation must appear as a fixed bottom bar so all tabs are reachable by thumbs without extending to the top of the screen.
- The day detail drawer must appear as a full-width bottom sheet on mobile (not a narrow right-side panel); it should cover ~80dvh and have rounded top corners.
- The account popover must not overflow the left edge of narrow mobile screens; it must be bounded to the viewport width.
- Explore city filter pills must scroll horizontally on mobile instead of wrapping into multiple rows.
- Page content must never be hidden behind the fixed bottom navigation; the app shell must add enough bottom padding to account for the nav height plus device safe-area insets.

Explicit exclusions:

- No gesture-based dismiss or swipe-to-close for the bottom sheet (tap the close button).
- No design changes for tablet or wide-mobile breakpoints in this iteration.

## v0.1.43 Repair Requirement Update

MVP acceptance additions:

- Text translation must remain usable when the Qwen/DashScope text route is unavailable but DeepSeek is configured; `/api/translate/text` should fall back server-side rather than leaving the Translator broken.
- If no translation provider is configured, the Translator should show a clear configuration message instead of a generic failure.
- Trip Canvas day cards should use `View details`, not `Edit`.
- Opening a day drawer should show the itinerary details only: time blocks, hotel, transport, and notes. It should not show editable inputs or a save button.
- On real Trip Detail pages with a saved canvas, Continue/Back/status/archive/share controls should be compact and live inside the Live Trip Canvas summary card so the detailed itinerary is the main page content.

Explicit exclusions:

- No new translation history, user translation storage, or provider selection UI.
- No inline day editing in the Trip Canvas drawer for this repair iteration.

## v0.1.45 Product Direction Update - Intelligent Chat Pipeline & Data Fusion (Planning Only)

This is a documentation-only planning iteration. It does not change code. It defines the product direction and acceptance criteria for the seven implementation iterations that follow (v0.1.46–v0.1.52, tracked as 阶段十二 in PLAN.md).

### Repositioning: from a feature grid to a fear-resolution engine

VisePanda is not "an app with Chat, Explore, Tools, and Translate." It is the single product that resolves the five hard blockers a Western traveler hits before a China trip:

| Blocker | Traveler anxiety | Surface that resolves it |
|---|---|---|
| Visa | "Do I even qualify?" | Tools (visa) + Chat `ask_factual` auto-triggers the checklist |
| Payment | "Can I use my card?" | Tools (payment) + pre-trip checklist |
| Connectivity | "Will my phone work?" | Tools (eSIM/VPN) |
| Language | "I can't read anything" | Translate, available everywhere as a 1-tap overlay |
| Itinerary | "How do I connect 10 cities in 2 weeks?" | Chat + Canvas + real POI data |

Design consequence: Chat is the spine. Explore data, Tools checklists, and Translate should all be reachable from inside a Chat conversation, not only as separate tabs. When the traveler's fear is resolved inside Chat, they stop bouncing between tabs.

### Requirement A — Chat efficiency: not every message is a full LLM call

Today every message takes the same path (raw text → full DeepSeek call → 2–3s → response), regardless of whether it is a factual question, a preference statement, or an itinerary request. This is slow, expensive, and inconsistent.

Acceptance criteria (target for v0.1.46):

- A fast, local intent classifier (regex + keyword, no LLM cost, <50ms) labels every message with one of 10 intents: `create_trip`, `adjust_trip`, `add_location`, `add_poi`, `ask_factual`, `ask_recommendation`, `preference_signal`, `concern`, `logistics`, `unclear`.
- `ask_factual` messages (visa, payment, VPN, metro, currency) are answered from the existing `lib/tools` static knowledge base and rendered as an inline tool card — no LLM call, <100ms, zero cost. These are an estimated 30–40% of chat traffic.
- `preference_signal` messages update the preference profile and return a one-line acknowledgment only; they do not trigger a full canvas patch.
- `ask_recommendation` messages call Amap/Dianping first and return real POI cards, then optionally use a lightweight summarization call for 1–2 sentences of context — never hallucinated place lists.
- Only `create_trip` / `adjust_trip` / `add_*` reach the full generative Butler LLM.

### Requirement B — Input refinement: expand casual text into structured intent

Users type casually ("beijing shanghai maybe xi'an 2 weeks family tired"). The system must extract entities (destinations, duration, party composition, preference signals) and build a structured `refinedPrompt` that is sent to DeepSeek instead of the raw message. The refined prompt is what drives quality and consistency.

### Requirement C — Response normalization: structured, scannable answers

Replace the single free-text `assistantMessage` with an enforced schema:

```
{ headline, body, highlights[], watchOut[], nextStep }
```

`ChatPanel` renders these as visual sections: large headline, green-check highlights, amber watch-out items, and `nextStep` promoted to the primary suggestion chip. Every reply becomes scannable and consistent. Suggestions are always exactly 2 chips, computed from the current trip state.

### Requirement D — Preference distillation without interrogation

The Butler must build a `UserPreferenceProfile` by reading between the lines of natural conversation, not by presenting a form. Examples: "tired of walking" → pace:light, mobility:moderate; "my kids love animals" → family_with_kids, interests:nature; "student budget" → economy, 2–3 stars; "I don't eat pork" → dietary:no_pork.

The one-question rule: the Butler may ask at most one clarifying question per turn, and only when the missing information would produce a materially wrong itinerary (e.g., season affects cherry-blossom timing). The question must be embedded naturally, not asked as a form field.

Acceptance criteria (target for v0.1.47):

- Profile fields: pace, travelStyle, partySize, partyComposition, budgetPerDay, hotelStars, dietaryRestrictions, cuisinePreferences, interests, mobilityLevel, experienceLevel, profileConfidence.
- Profile persists in Supabase for logged-in users and localStorage for guests, and is injected into every Butler system prompt.
- Profile chips are visible in the ChatPanel header (e.g., "Foodie · Mid budget · 2 people").

### Requirement E — Real data in the itinerary, not hallucinations

The Butler must recommend real places with real attributes. Acceptance criteria (targets for v0.1.48–v0.1.49, v0.1.52):

- Amap POI data captured today is enriched to expose rating, average cost, phone, opening hours, and photos (fields the Amap API already returns but the current provider discards).
- Explore and Chat POI cards conditionally display rating (★ score), price level (¥/¥¥/¥¥¥), opening hours, phone, review count, and a booking CTA — all optional, degrading gracefully when a field is missing.
- The Butler can call Amap/Dianping live during planning (tool calling), so itinerary blocks carry real POI IDs, real hours, and real ratings.
- Static/mock providers are never removed; when a live source is unavailable or unapproved, the conversation still works with text-only itineraries.

### Requirement F — UX audit: journeys, redundancy, and navigation

User-journey targets:

- First-run: replace the blank textarea with 3 archetype entry points ("First trip — 10 days essentials", "Foodie — 3 cities", "History + nature"). Selecting one pre-seeds a starter draft the Butler frames as a suggestion. Show a trip-completeness progress indicator.
- Ask-visa: answered instantly from static content as an inline tool card, with a proactive offer to add a visa reminder — no navigation, no LLM call.
- Refine-day: Day cards get inline quick-actions (Lighten / Swap morning / Add food) that send pre-formed Butler intents so the user never types prompt-engineering text.
- Translate-menu: a floating camera/mic button opens OCR from anywhere in ~3 seconds without leaving the current screen.

Redundancy to remove:

- Provider-status jargon in Explore (move to an internal/debug surface).
- Duplicate "Ask VisePanda" labels in ChatPanel.
- Developer-facing `confidence` copy ("Draft/Refined/Ready to save") → friendly phrases.
- The generic "Live Trip Canvas" h1 + "VP" badge → the trip's own title once one exists.
- The homepage feature grid for signed-in users → redirect `/` to `/chat`.

Navigation restructure (target for v0.1.51): from 6 flat tabs (Chat · Trips · Explore · Tools · Translate · Community) to 4 tabs (Chat · Trips · Tools · Community) plus a floating Translate action. Explore is demoted to a sub-feature accessed from inside Chat.

### Explicit exclusions for v0.1.45

- No code changes at all. This iteration only produces documentation (PRD/PLAN/DESIGN/AGENTS/HANDOFF/CHANGELOG/VERSIONING).
- No AI provider, Supabase schema, provider API, or component changes.
- Exact field names, schemas, and tool signatures in this document are the planning target and may be refined during each implementation iteration.

## v0.1.46 Product Direction Update - Product Expansion (Planning Only)

Documentation-only planning iteration. No code changes. Authoritative deep-dive: `docs/planning/v0.1.46-product-expansion.md`. This section states the product-level requirements for seven new tracks.

### Guiding principle: quality over cost

The overriding requirement is **user experience and answer quality**. Token/compute cost is explicitly not a constraint. Where a better answer is possible via a larger model, a multi-model ensemble, or a multi-pass refine-and-verify loop, that is the correct choice. This supersedes the v0.1.45 rationale of routing messages away from the LLM "to save cost": the intent classifier is retained, but its purpose is now routing for **quality and correctness** (right specialist model / verified source), not cost reduction. The only real limits are acceptable latency and never fabricating China-specific facts.

### Requirement G — Multi-model Chinese LLM precision

- The Butler may draw on multiple Chinese LLMs (DeepSeek, Qwen/Aliyun Bailian, Zhipu GLM, Moonshot Kimi, Baidu ERNIE, optionally MiniMax/Doubao), each used for what it is strongest at (reasoning, Chinese POI/menu understanding, long context, China-specific facts).
- High-stakes answers (final itinerary, visa eligibility) should use a parallel ensemble with a judge/reconciler; disagreements surface as `watchOut` items.
- All model keys are server-side only; the chain always degrades to the mock Butler if every model fails.

### Requirement H — Native iOS + Android apps (plan only)

- The product mainline is now a native Android APK first. The active Android baseline is Kotlin + Jetpack Compose + Material 3 + Room/DataStore + MVI/StateFlow, with existing Next.js API routes reused as backend contracts where possible.
- Must support offline-first travel data (trips, tools checklists, phrasebook cached locally), native camera (OCR), microphone (STT), push notifications (visa/booking reminders), and deep links.
- China distribution is a distinct track requiring ICP 备案, software copyright 软著, and MIIT registration — treated as a legal/ops workstream with long lead time. iOS is planned separately after the Android shape is proven.

### Requirement I — Tools deliver real functionality, not just text

Each of the six Tools becomes interactive:

- Visa & entry: nationality-driven eligibility checker + transit-time calculator + progress-tracked document checklist.
- Payment setup: step-by-step Alipay/WeChat linking wizard with app deep links and card-compatibility check.
- Currency: full converter (amount input, live rate, reverse, offline snapshot).
- Metro: real route planner (from/to station → route, transfers, fare, time) via Amap transit; station names shown in EN + 中文.
- eSIM/VPN: comparable providers with real purchase links and install guidance.
- Emergency: one-tap 110/120/119 call buttons, GPS embassy locator, medical phrase cards with TTS, share-location-in-Chinese, nearest hospital.

Interactive widgets degrade to the existing static content when data is unavailable.

### Requirement J — Professional Account UI + lead capture (留资)

- A dedicated, mainstream, professional `/account` center that signals trust and formality (in addition to the quick popover): profile, security, membership, preferences, travel documents (opt-in, encrypted), notifications, and privacy/data controls with export & delete.
- Lead capture uses progressive profiling (never a wall of forms). Prioritized fields: Tier 1 contact (name, email, phone and/or WeChat, preferred channel); Tier 2 trip qualification (nationality, dates, party size/composition, cities, budget range, trip purpose); Tier 3 enrichment (interests, dietary/mobility, booking needs, attribution, explicit marketing consent).
- Consent is explicit, timestamped, and source-tagged (PIPL/GDPR aware). A single "Talk to a China travel expert" CTA opens the lead form; core planning is never blocked behind it.

### Requirement K — Admin backend with LLM customer briefs

- A role-gated `/admin` area (never in traveler navigation) where staff view leads and chat conversations.
- For each customer, an LLM pipeline distills lead fields + conversation + trip state into a structured `CustomerBrief`: summary, trip intent, budget signal, readiness-to-book score (0–100), key preferences, open questions to close the sale, detected objections/risks, suggested next action, and preferred contact language. The brief is advisory; raw data is always available beside it.
- Admin routes are server-side only, gated by Supabase session + role check; `SUPABASE_SERVICE_ROLE_KEY` is used strictly server-side; data access is logged.

### Requirement L — Frontend & visual optimization

- Formalize design tokens and a small reusable component library; add purposeful motion, loading skeletons, and designed empty/error states; complete an accessibility pass (contrast, focus, keyboard, reduced-motion, RTL); polish responsive/tablet layouts; optimize performance (images, code splitting, fonts); and introduce a cohesive brand illustration/icon system.

### Explicit exclusions for v0.1.46

- No code changes at all; documentation only.
- Exact schemas, field names, model choices, and native stack details are planning targets and may be refined per implementation iteration.
- Native apps, admin backend, and lead capture are planned, not built, in this iteration.

## v0.1.47 Implementation Update - Multi-LLM Butler Orchestrator

First code iteration delivering Requirement G (multi-model Chinese LLM precision) and the quality-over-cost principle (ADR-043).

Delivered:

- The Butler answers through a provider-agnostic orchestrator that can use DeepSeek, Qwen, Zhipu GLM, Moonshot Kimi, Baidu ERNIE, and MiniMax. Each request is classified (10 intents) and routed to the strongest available specialist, with the remaining providers as a fallback chain.
- High-stakes intents (`create_trip`, `ask_factual`) run a small parallel ensemble when two or more providers are configured, preferring the primary's answer.
- All keys are server-side only. The mock Butler fallback is preserved: with no keys the product behaves exactly as before; adding a provider key upgrades answer quality with no code change.

Acceptance criteria met:

- With `DEEPSEEK_API_KEY` (or any provider key) set, `/api/chat` returns a real model's canvas patch and the chat status shows which model produced it.
- With no keys, `/api/chat` returns the mock Butler patch and the app still works end-to-end.
- Provider selection is deterministic and unit-tested; adding/removing a key changes which model answers without any redeploy of logic.

Deferred to later iterations:

- The response-normalization schema `{headline, body, highlights, watchOut, nextStep}` and the full refine-and-verify loop with a judge model writing disagreements into `watchOut` (task 13.5) — these layer onto the Chat Intelligence work.
- Vision/long-context specialization beyond capability tagging (e.g. routing menu photos to Qwen-VL).

## v0.1.48 Requirement Update - Configured Models and Structured Butler Replies

This iteration activates the configured Vercel provider setup and delivers the first user-visible response-normalization layer.

MVP acceptance additions:

- The Butler model registry defaults match the configured Vercel provider choices: DeepSeek v4 flash, Qwen 3.6 Flash, Zhipu GLM5, and Moonshot Kimi 2.5. Each remains overridable with the existing server-side `*_CHAT_MODEL` env vars.
- Live Butler responses should include a structured `assistantResponse` object with `headline`, `body`, `highlights`, optional `watchOut`, and `nextStep`.
- `assistantMessage` remains required so saved chat history, old provider responses, and existing fallback paths keep working.
- `ChatPanel` renders structured assistant responses as a compact guidance card; old/plain messages still render as normal text.

Explicit exclusions:

- No full judge/refine-and-verify loop yet.
- No preference-profile persistence yet.
- No Amap rich-field enrichment in this iteration; that work is now the next data-fusion step.

## v0.1.49-v0.1.51 Requirement Update - Rich POI, Tool Context, Preference Memory

This is a three-iteration implementation batch focused on making Chat and Explore feel more real and more personal.

MVP acceptance additions:

- Explore live Amap POIs can include optional rich metadata: rating, approximate cost, price level, phone, opening hours, photo URL, business area, source label, and location.
- Explore cards conditionally render that metadata when available and continue to render cleanly when static fallback or sparse Amap rows omit it.
- `/api/chat` can attach bounded live Amap POI context to relevant Butler intents before calling the model, so live providers can plan with real POI candidates instead of inventing every place name.
- The app stores a lightweight guest `UserPreferenceProfile` in localStorage and injects it into Chat requests. The profile is extracted silently from natural messages and covers pace, budget, party, dietary restrictions, cuisine preferences, interests, and confidence.
- Chat displays compact remembered-preference chips so travelers can see that VisePanda is adapting.

Explicit exclusions:

- No full multi-round function-calling loop yet; v0.1.50 is a bounded tool-context prefetch.
- No Dianping/Meituan integration yet.
- No Supabase `profiles` table yet; logged-in cross-device preference persistence remains planned.
- No TripBlock POI embedding or Add-to-Day inline POI cards yet.

## v0.1.52 Product Interaction Blueprint Update - Planning Only

Documentation-only strategic iteration. No product runtime code changes. Authoritative deep-dive: `docs/planning/v0.1.52-product-interaction-blueprint.md`.

### Product position

VisePanda should be positioned as a China travel operating system for foreign visitors, not merely an AI itinerary generator. The product must reduce five traveler anxieties:

- Entry: visa, transit, customs, documents.
- Payment: Alipay, WeChat Pay, foreign cards, cash backup.
- Connectivity: eSIM, VPN, maps, translation availability.
- Language: menus, taxi addresses, signs, emergencies.
- Itinerary: route logic, fatigue, opening hours, real POIs, hotel areas.

### Core product loop

The required experience loop is:

1. The user expresses intent naturally or chooses an archetype.
2. VisePanda extracts preferences and constraints without a form.
3. VisePanda uses live tools/data where factual confidence matters.
4. Trip Canvas updates as the stable source of truth.
5. Chat explains what changed and what decision remains.
6. The user acts through small next-step controls instead of prompt engineering.
7. Trips saves status, readiness, sharing, and continuity.

### Journey-stage requirements

- Curious: the first screen should help the user start a realistic China trip quickly through archetypes, not explain every product feature.
- Planning: Chat remains the main workspace; Canvas exposes completeness and quick day-level actions.
- Preparing: Trips/Canvas should surface readiness blockers and contextual Tools cards.
- In China: Translate must become a global utility; Day detail must show operational fields such as Chinese address, map link, phone, and opening hours.
- Share/Get help: Trips status should use traveler language, and lead capture should appear only at high-intent moments.

### Page-role requirements

- Home: acquisition and first-start surface; show archetype starts and route returning users toward active trips.
- Chat: operating center for planning; show first-run chips, structured next-step actions, and subtle preference memory.
- Trip Canvas: visible trip object plus action surface; prioritize trip title, completeness, quick actions, and operational day detail.
- Trips: continuity, readiness, review, and sharing; show blockers and compact actions while keeping itinerary primary.
- Explore: discovery engine feeding Chat/Canvas; show "In your trip" and Add-to-Day/Replace precision over generic Add to Trip.
- Tools: operational widgets that resolve anxieties; surface contextually inside Chat when relevant.
- Translate: global camera/mic/text utility that returns users to their prior context.
- Community: social proof and inspiration; convert posts/shared trips into Chat planning prompts.
- Account: trust, preference review, consent, and progressive lead capture.

### Roadmap acceptance targets

- `v0.1.54` implements the first 60 seconds: Home archetypes, Chat first-run chips, and primary `nextStep` actions.
- `v0.1.55` should make Canvas operational: completeness score, day quick actions, and prep blockers.
- `v0.1.56` should render inline tool cards inside Chat for visa, payment, eSIM, currency, and emergency needs.
- `v0.1.57` should persist rich POI fields in TripBlocks and upgrade Day detail with real operational fields.
- `v0.1.58` should make Translate available from anywhere without stranding the user on a separate page.
- `v0.1.59` should turn top Tools into real widgets.
- `v0.1.60` should add a real Account center and editable preference/consent review.
- `v0.1.61` should connect lead/admin planning to customer briefs.

### UX writing and metrics

- Replace developer-facing status with traveler-facing status: Draft → Taking shape, Refined → Looking good, Ready to save → Travel-ready.
- Avoid provider/API/model jargon in traveler-facing text.
- Each AI reply should end with one concrete next action.
- Every sensitive or personal question must explain why it matters.
- Track time to first canvas, meaningful edits/session, save-to-Trips rate, prep item completion, Translate quick-action usage, share-link creation, lead CTA completion, and the percentage of replies grounded in live tool/POI context.

### Explicit exclusions for v0.1.52
 
- No product code changes in this iteration.
- No provider/API/Supabase schema changes.
- The blueprint sets the next implementation order but does not mark those future features as complete.
 
## v0.1.53 Strategic Requirement Update

This iteration establishes requirements for deeper travel assistance capabilities (resilience, context interpretation, card routing, contextual layout promotion, and bilingual offline handoff) to guide the next phases of development.

### 1. Offline-First Travel Vault (Offline Resilience)
* **Goal**: Enable the traveler to view and operate their trip details in China when network connectivity (Wi-Fi, cellular, or VPN) is lost.
* **Requirements**:
  - Automatically cache the active `TripState` (JSON snapshot), offline pocket notes, emergency contacts, local bilingual addresses, and translator phrasebooks locally in the browser (`localStorage` or IndexedDB).
  - When connection is lost (`window.navigator.onLine === false` or API fetch timeout), automatically toggle an "Offline Desk" layout banner.
  - In Offline Desk mode, all main navigation is disabled except Trip Canvas, Tools, and Translate. They render exclusively using cached local data.
  - Show a prominent "Cached version - Last updated [time]" badge.

### 2. Cultural Context Interpreter
* **Goal**: AI Butler should act as a cultural and operational interpreter rather than just outputting generic tourism names.
* **Requirements**:
  - Ground the Butler's reasoning in Chinese digital rules: e.g., flag that certain museums (like the Forbidden City or Shaanxi History Museum) require real-name passport reservation 7 days in advance.
  - Warn travelers about local holiday congestions (e.g. National Day Golden Week, Golden Week crowd levels, and train ticket release policies).
  - Explain why specific spots require Cash/WeChat Pay (e.g. street food) and why other spots are cashless.

### 3. Intelligent Payment Card Routing
* **Goal**: Provide a payment wizard resolving credit card integration anxieties.
* **Requirements**:
  - A multi-step questionnaire card in Chat/Tools where users select their credit card brand (Visa, Mastercard, Amex, Discover).
  - The tool outputs a personalized setup flow detailing:
    - Transaction fee structure (e.g. Alipay 3% fee waiver threshold for transactions under 200 CNY).
    - Setup steps for linking international cards directly to Alipay and WeChat Pay.
    - Cash backup strategy: locating UnionPay-compatible ATMs for cash withdrawal.

### 4. Contextual Tool Promotion
* **Goal**: Dynamically promote relevant tools on the active layout based on the traveler's context.
* **Requirements**:
  - Detect current trip day/city context. For example, if the active day is set to "Shanghai", tools like "Metro Route Planner" and "Alipay Guide" are floated to the top of the Tools drawer, and Translate menu-recognition prompts are pre-loaded.
  - Display helpful warnings (e.g. "Prepare Alipay for transit in Shanghai") directly on the Day Card in the Trip Canvas.

### 5. Bilingual Export & Print Kit
* **Goal**: Provide an offline handoff tool for local drivers and hotel staff who do not read English.
* **Requirements**:
  - Generate a "Bilingual Handoff Page" layout for print or mobile display.
  - Every Day card and POI should have a "Show Taxi Driver (显示给司机)" button that renders the Chinese name, address, and an offline map block in large, clear text.
  - Support exporting the full Trip Canvas as a clean, compact bilingual PDF/PNG card.

### Explicit exclusions for v0.1.53
- No product runtime code changes.
- No Supabase or database schema migrations.

## v0.1.54 Implementation Update - Interaction Shell I

This iteration implements the first code slice after the v0.1.52 interaction blueprint and v0.1.53 one-stop FIT strategy.

MVP acceptance additions:

- Home provides three high-confidence independent-travel archetype starts: First China 10 Days Essentials, Foodie China, and History & Nature.
- Each Home archetype routes to `/chat?archetype=<archetype-id>` and does not hardcode a finished itinerary.
- Chat detects `?archetype=` once on launch and sends the matching prompt through the existing `handleSend` → `/api/chat` → `CanvasPatch` → `applyCanvasPatch` pipeline.
- Empty Chat state shows exactly three no-typing starter chips for FIT travelers.
- The latest structured Butler `nextStep` is promoted to a primary action card/chip that can be clicked to continue the conversation.
- Trip Canvas h1 uses the current trip title when available instead of the generic "Live Trip Canvas" placeholder.
- Canvas confidence labels use traveler-facing wording: Taking shape, Looking good, and Travel-ready.

Explicit exclusions:

- No direct canvas mutation from Home, Chat chips, or archetype starts.
- No new provider keys, API integrations, Supabase migrations, or booking/payment logic in this iteration.
- Offline Vault, Payment Wizard, Bilingual Handoff, and Contextual Tool Promotion remain planned follow-up tracks.

## v0.1.55 Design-Spec Update - UX Layout & Frontend (planning only)

Documentation-only. Establishes the design contract the FIT roadmap phases build against; deep-dive in `docs/planning/ux-design-and-layout-spec.md`.

- Requirement: the product must present as one spatial system (Canvas + Chat as the home), with every one of the five traveler anxieties resolvable within one tap of the Canvas. Pages that don't answer a traveler question are demoted.
- Requirement: Butler replies render as structured blocks (headline / body / highlights / watchOut / nextStep), with `nextStep` as a tappable primary action; canvas changes are visually signaled (new/revised animation).
- Requirement: a formalized frontend design system (tokens + reusable component library + motion + a11y) underpins all new surfaces so Account/Admin/Tools widgets and the future native app inherit one coherent system.
- Exclusion: no code, no new roadmap phases; this is the design layer for existing phases. Exact tokens/component APIs are targets, refined per implementation iteration.

## v0.2.2 Update - Chat Core-Loop Fixes

Fixes three reported product problems:

- Chat replies must be fast: the Butler races configured models in parallel and bounds each call with a timeout, so latency tracks the fastest healthy model.
- Chat and the Live Canvas must stay in sync: any message that changes the itinerary updates the canvas — guaranteed even in fallback via a destination-aware skeleton, and required of live models via the prompt contract.
- Chats persist automatically: signed-in chats auto-save to Trips; there is no manual Save button. Guests keep an automatic local draft.

## v0.2.3 需求更新 —— UI 优化路线(纯规划)

依据一站式 FIT 管家定位,确立三条体验需求主线(详见 `docs/planning/v0.2.3-ui-optimization-roadmap.md`):

- 行程可操作:完成度可见(0–100% 进度条 + 缺口 chips)、Day 卡一键快捷动作、画布变化必须有视觉信号、五焦虑聚合为"出发准备"区。
- 对话像管家:回复分块可扫(headline/✓/⚠/下一步按钮)、任何发送 100ms 内有反馈、事实类问题 <150ms 秒答内联卡且可一键加入行程提醒。
- 界面成体系:token + 组件库统一(新界面禁手写同类样式)、空/错状态设计化、移动端逐轮附带打磨。

排除项:本轮零代码;不新增路线图阶段;不引入新依赖或新 key。


## v0.2.4 需求更新 —— 交互深化(纯规划)

在 v0.2.3 三条主线上新增可验收的交互需求(细则见 `docs/planning/v0.2.4-interaction-deep-dive.md`):

- 联动可见:每次 AI 改动生成"本次改动"摘要卡,点条目画布定位并高亮;AI 修改支持一键撤销。
- 回复可扫:MessageBlock 分层 + 伪流式呈现(<400ms);任何发送 100ms 内有反馈,3s/8s 有等待叙事。
- 画布可指挥:Day 卡快捷动作意图必须带天数;完成度六段可点补缺;出发准备区可勾选且回写完成度。
- 视觉纪律:朱砂每屏唯一、金色禁作文字、五档墨灰之外禁新增灰、全站文本对比度 ≥4.5:1。

排除:真流式 SSE、抽屉下滑手势、Day 卡拖拽排序(三轮后候选)。

## v0.2.5 需求更新 —— 规划融合 + Readiness Seed

本轮将远端 `v0.2.4` 交互深化规格与本地视觉规划/实现 seed 融合到同一条 `0.2.x` 主线。它不是完整 Canvas 行动层,而是为下一轮行动层提前落下部分视觉与信息架构基础。

已吸收/确认:

- Trip Canvas 可以展示 traveler-facing readiness 初版,但当前 readiness 仍是从既有 `TripState` 派生的展示值。
- Summary/readiness/action rail 是后续 Canvas 行动层的视觉基础,但 prep blockers、alert done、可点 completion 分段仍未完整落地。
- Chat first-run starter state 与 Home launcher polish 继续保留,作为 FIT 用户首分钟入口。
- 后续完整执行路线修正为:
  1. `v0.2.6` Canvas 行动层+画布交互。
  2. `v0.2.7` Chat 体验重塑+内联工具卡。
  3. `v0.2.8` 设计系统收口+Tools 交互组件。

排除:

- 不把当前 readiness 当作完整的六维 completion schema。
- 不将快捷动作、prep blockers、Change Digest、undo 视为已完成。
- 不新增外部 key、Supabase schema 或真实 booking/payment/map 闭环。


## v0.2.6 更新 —— FlyAI(飞猪)Skill 研究(纯规划 + 开发工具)

依据用户请求研究 `alibaba-flyai/flyai-skill` 并评估集成路径。结论记录于 `docs/planning/flyai-skill-integration.md`:

- 需求缺口确认:VisePanda 目前没有任何真实可预订数据——Canvas 的 `stay`/`transport` 字段和 Explore 的住宿/景点列表全部止步于 AI 生成文字或高德的位置/评分信息,没有一条路径能让用户点一下就去预订酒店、买门票或查真实车次。飞猪 FlyAI 的 `search-hotel`/`search-poi`/`search-flight`/`search-train` 精确对应这个缺口。
- 生产集成前提:需飞猪官方服务端合作确认(其 CLI 是开发者/Agent 工具,非公开生产 API,内嵌凭证为共享试用额度且带风控反滥用机制)——与既有 Dianping/Meituan「先官方申请、不逆向」原则一致,本轮不写任何调用真实飞猪数据的产品代码。
- 已落地:项目级开发工具 Skill(`.claude/skills/flyai/`),供开发阶段研究/内容生产使用,不影响任何traveler-facing 行为。

排除:不新增外部 key、不改 Explore/Tools/Chat 的生产运行时代码、不对外发起飞猪官方合作申请(需操作者自行跟进)。


## v0.2.7 更新 —— Canvas 行动层(第一轮代码实现,已完成)

交付:六维完成度评分与进度条、Day 卡快捷动作(预制意图走 AI 管道)、变更摘要卡(点击定位+高亮)、patch 演出动画、撤销(本地确定性回滚)、出发准备区(可勾选清单)。验收标准(点"减负"→3秒内对应卡脉冲+摘要卡列出改动+完成度动画;点摘要条目画布定位;点撤销恢复上一版)已通过端到端测试验证。

一处对规格文档的刻意偏离:撤销未按原计划路由给 AI,而是做成本地确定性回滚——因为 LLM 无法在未被喂入"撤销前的权威状态"的情况下可靠地精确重建原状态,若严格照做会让"撤销"按钮的行为不可预测。详见 DESIGN.md ADR-070。


## v0.2.8 更新 —— Chat/Canvas 视觉重设计(已完成)

需求来源:操作者在 v0.2.7 收尾阶段提供了一张 Chat 页面高保真设计稿,要求按此重做视觉。范围因此从原计划的"MessageBlock 伪流式渲染 + composer 规格"调整为"对齐设计稿的完整 Chat/Canvas 视觉重做"。

交付:`TripSummary` 标题内联改名 + 状态徽标(进度条/下一步) + 一览 chip 行 + 动作行(Add day/Rebalance route 真实走 AI 管道,View map/Trip settings 诚实禁用);`DayCard` 新增 Day 级完成度徽标、真实照片或图标占位的三段式 block、快捷动作拆分为常显主动作 + "…" 溢出次动作;`ChatPanel` 新增头像化头部、消息时间戳、结构化高亮卡片、赞踩/复制反馈(本地状态,不接后端)、可关闭 Next Step 卡、图标化输入区、AI 免责声明。

验收标准:所有内容型变更(改名除外的行程内容)仍必须走既有 `handleSend → /api/chat → CanvasPatch → applyCanvasPatch` 管道——本轮未新增任何绕过该管道的内容写入路径;所有非功能性控件(Attach/Mic/Pin/View map/Trip settings)必须是真实 `disabled` 状态并带 `title="Coming soon"`,不得是无 `disabled` 属性的装饰性假按钮——已逐一核对。全部 134 测试通过(新增 8 个),`npm run build` 成功,并用 Playwright 对渲染结果做了视觉核验。

排除:不新增外部 key、不改变任何既有 API 契约、不实现设计稿之外的功能(地图/导出/行程设置等在设计稿中出现但无对应后端能力的项,统一诚实降级为禁用态,留给未来迭代)。

## v0.2.9 更新 —— Chat factual fast-path + inline Tools cards(已完成)

需求来源:在 v0.2.8 视觉重设计后,Chat 仍缺少原 v0.2.4 规格里"事实类问题快速回答 + 内联工具卡"的实用闭环。本轮补齐第一版,优先解决签证、支付、汇率、地铁、eSIM/VPN、应急等外国 FIT 游客最常问、且不应每次都等待 LLM 的基础问题。

交付:

- `ask_factual` / emergency concern 命中高置信 Tools 分类时,Chat 直接返回结构化 inline tool card,不调用 LLM provider。
- 工具卡内容来自现有 Tools provider 的静态知识,并深链到 `/tools?category=...`;这让 Chat 回答和 Tools 页面保持同一信息源。
- `AssistantResponse.toolCards` 为可选字段,不会破坏旧 provider、历史消息或 mock fallback。
- Chat busy 状态增加可见 waiting narrative,避免发送后没有反馈。

验收标准:

- 问 "Do I need a visa?" / "How do I pay with Alipay?" 这类问题时,即使无 LLM key 也能得到可扫的工具卡回答。
- 命中快通道时不得调用 LLM provider;未命中时仍走原有 orchestrator + mock fallback。
- 卡片只能指向已有 Tools 深链,不得伪造真实签证审批、支付交易、预订或地图能力。

排除:

- 不做完整签证资格决策树、支付向导、汇率换算器;这些进入 v0.2.10 Tools Widgets I。
- 不做实体 chip 双向悬停、不做完整 MessageBlock 伪流式。
- 不新增外部 key、Supabase migration 或生产级 FlyAI 接入。

## v0.2.10 更新 —— Tools Widgets I(已完成)

需求来源:Tools 页面此前能提供清单,但用户仍需要自己把信息转化为行动。本轮把最常见的三类旅行焦虑做成可直接操作的小工具:汇率、签证/入境初筛、支付设置。

交付:

- Currency: RMB converter,支持输入金额、选择目标货币、常用金额 chip;有 live rate section 时优先使用 live rate,否则使用离线估算 fallback。
- Visa and entry: conservative entry planning checker,支持国籍、停留天数、transit-only 开关,输出规划建议与官方确认提醒。
- Payment setup: payment setup wizard,支持钱包(Alipay/WeChat Pay)与卡品牌选择,输出出发前设置步骤和卡兼容提醒。
- 三个 widget 都由 `ToolCategory.interactive` 描述符驱动;未配置 interactive 的分类保持原静态内容。

验收标准:

- 打开 `/tools?category=currency` 能完成 RMB 到常见货币的快速估算。
- 打开 `/tools?category=visa-and-entry` 能基于国籍和天数得到保守入境规划提示。
- 打开 `/tools?category=payment-setup` 能生成 Alipay/WeChat Pay + 卡品牌的设置步骤。
- 静态 tips、sections、offline notes 不被移除,仍作为 fallback 和详情内容。

排除:

- 不做官方签证裁定或实时政策保证。
- 不处理真实支付、绑卡、交易、钱包授权。
- 不新增外部 API key、Supabase migration、FlyAI 生产调用或预订能力。

## v0.2.11 更新 —— Frontend Design Resource Stack 配置(纯文档)

需求来源:操作者要求配置一组前端/设计/Agent 资源,包括 Frontend Design、UI design system、CSS animation、creative aesthetics、Awwwards landing、web design guidelines、Vercel React best practices、Superpowers、Impeccable、better-icons、UI Design Brain、DESIGNmd、awesome-design-md 等。

交付:

- 新增 `PRODUCT.md`,为设计工具和后续 agent 提供简短产品上下文、目标用户、产品语气和设计红线。
- 新增 `docs/planning/v0.2.11-frontend-design-resource-stack.md`,把各资源归类为 source of truth、流程规则、设计评审词汇、图标发现工具、组件模式参考或待验证资源。
- 明确 `DESIGN.md`、`app/globals.css`、现有 React 组件和产品能力边界仍是 VisePanda 的权威来源。
- 明确 Impeccable、better-icons、UI Design Brain、DESIGNmd 等均为可选外部开发辅助;没有安装为运行时依赖。

验收标准:

- 后续前端设计任务能先读 `PRODUCT.md` + `DESIGN.md` + 相关组件/CSS,再使用外部资源作为 critique/reference。
- 新 UI 不应因为外部设计系统而丢失 VisePanda 旅行管家工作台定位。
- 图标优先使用现有 `lucide-react`;外部图标工具只能用于缺失图标的发现/检索。

排除:

- 不下载外部 DESIGN.md 覆盖当前 `DESIGN.md`。
- 不安装 Impeccable、better-icons、MCP server、CLI 或 npm 包。
- 不新增生产功能、API key、Supabase schema 或用户可见行为。

## v0.2.12 更新 —— 文档/版本交接统一(纯文档)

需求来源:操作者要求把所有 MD 文档更新到 v0.2.12,避免另一台设备或新的 coding agent 接手时误读当前版本、下一轮编号或设计资源栈状态。

交付:

- 当前项目版本统一为 `v0.2.12`。
- 明确 `v0.2.11` 是已完成的 Frontend Design Resource Stack 配置轮,不是下一轮功能开发。
- 明确下一轮建议为 `v0.2.13` TripBlock POI Embedding + Day Detail Operational Upgrade。
- 保留 v0.2.10 Tools Widgets、v0.2.11 设计资源栈配置的历史记录,但当前接手依据以 v0.2.12 文档顶部为准。

验收标准:

- 新接手 agent 阅读 `VERSIONING.md`、`HANDOFF.md`、`PLAN.md` 后能判断当前版本是 v0.2.12。
- 新接手 agent 不会把 v0.2.11 设计资源配置误认为待执行任务。
- 下一轮功能开发从 v0.2.13 开始。

排除:

- 不改运行时代码、UI、API key、Supabase schema 或 provider routing。

- 不安装 Impeccable、better-icons、MCP server、CLI 或 npm 包。
- 不新增生产功能、API key、Supabase schema 或用户可见行为。

## v0.2.13 更新 —— TripBlock POI + Day detail operational upgrade

需求来源:外国 FIT 游客在中国执行行程时,单日详情不能只告诉他“去哪儿”,还要能帮助他落地执行:给司机看中文地址、知道营业时间、打开地图、保留预订信息入口。

交付:

- `TripBlock` 支持可选运营字段:英文地址、中文地址、电话、营业时间、地图链接、预订信息链接、来源标签和坐标。
- Day detail 抽屉在每个 block 下显示 POI execution details。
- 新增 Show taxi driver 卡,优先显示中文地址。
- mock/static fallback 行程包含代表性 POI 地址与地图信息,保证无 API key 时也能演示。

验收标准:

- 打开 Day detail 能看到 POI 地址、营业时间、地图入口和司机卡。
- 老 trip JSON 没有这些字段时仍正常显示原有详情。
- 所有 booking/map 字段都以信息入口呈现,不声称真实库存或交易。

排除:

- 不做真实酒店/门票/交通预订。
- 不做支付、库存、退款或订单管理。
- 不新增 Supabase schema、外部 API key 或生产 FlyAI 调用。

## v0.2.14 更新 —— Real POI context write-through + booking candidate model

需求来源:Chat 已经能拿到 bounded Amap `liveToolContext`,v0.2.13 也让 TripBlock 能存放 POI 执行字段。本轮解决“模型用了真实 POI 名称,但遗漏地址/电话/坐标等字段”的稳定性问题,并先建立非交易型 booking candidate 模型。

交付:

- `BookingCandidate` 非交易型模型,用于酒店、门票、交通、餐厅候选。
- Amap tool context 携带 id、phone、mapUrl、coordinates、bookingCandidates。
- provider patch 解析后,`applyToolContextToPatch` 将匹配 POI 的安全字段补入 TripBlock。
- Day detail 展示 booking candidates,并标记为 Info only。

验收标准:

- 当 liveToolContext 中的 POI 名称匹配 TripBlock title 时,TripBlock 获得地址、电话、营业时间、地图链接、坐标和 booking candidates。
- 已有 TripBlock 字段不被覆盖。
- booking candidates 不被描述为可下单、可支付或库存已确认。

排除:

- 不做 checkout、库存、支付、退款、订单管理。
- 不新增 Supabase schema、外部 API key 或生产 FlyAI 调用。

## v0.2.15 更新 —— Explore Add-to-Trip POI write-through

需求来源:Explore 是 FIT 用户发现景点/餐厅/住宿候选的入口。Add to Trip 不能只把一句自然语言请求交给 Chat,否则真实 POI 的 id、地图入口、来源、营业时间、电话和 booking candidate 会在页面跳转时丢失。

交付:

- Explore Add to Trip 同时传递 Chat draft 与结构化 POI payload。
- Chat 首跑解析 `poi` payload,并在应用 AI/mock patch 前执行确定性写入。
- 若行程里已有匹配 POI block,只补齐缺失的执行字段;若没有匹配,在目标城市 day 追加一个可见的 Flexible candidate block。
- Day card 与 Day detail 显示 Flexible block,让候选 POI 在画布上可见,而不是只存在状态里。
- booking candidates 继续以 Info only / planning reference 方式呈现。

验收标准:

- 从 Explore 点击 Add to Trip 后,Chat 自动发送 draft,Trip Canvas 能看到该 POI 名称。
- 该 POI block 保留 map/source/coordinates/booking candidate 等可用字段,已有字段不被覆盖。
- 静态 fallback 没有地址/电话时不伪造,只提供地图搜索入口。

排除:

- 不做真实预订、购票、支付、库存、退款或订单管理。
- 不新增 Supabase schema、外部 API key 或生产 FlyAI 调用。

## v0.2.16 更新 —— Explore candidate review / Day detail action polish

需求来源:v0.2.15 已让 Explore POI 稳定进入 Canvas,但 FIT 用户仍需要清楚区分“候选”与“已排程”。候选 POI 应该被看见、可理解、可交给 Butler 安排,但不能被伪装成已经确定的上午/下午/晚上行程。

交付:

- 只有真实 Flexible block 存在时才显示候选块,不显示通用 Flexible 占位。
- Flexible 候选在 Day card / Day detail 中显示为 `Needs scheduling`。
- Day detail 增加 `Ask VisePanda to schedule` 动作,让用户把候选交给 Butler 重排。
- 调度动作继续走 Chat/AI patch 管道,不在本地直接硬改行程内容。

验收标准:

- 普通行程不会出现无意义的 Flexible 占位。
- 从 Explore 加入但尚未排程的 POI 会显示 `Needs scheduling`。
- 点击调度按钮会生成包含 day number、city、POI title 的请求。

排除:

- 不做候选删除/收藏/排序。
- 不做真实预订、购票、支付、库存、退款或订单管理。
- 不新增 Supabase schema、外部 API key 或生产 FlyAI 调用。


## v0.2.17 需求更新 —— 景点/餐饮/酒店数据与预订服务拓展评估(纯文档,已完成)

需求来源:操作者主动提出"针对景点/餐饮/酒店推荐,除了高德 api 的 poi 之外,市面上还有类似的服务,我想要拓宽产品线",经追问确认深挖范围为 Trip.com、大众点评、百度地图、腾讯位置服务、Klook、KKday 六个候选。

交付:`docs/planning/data-provider-expansion-assessment.md`,以"离外国游客能在 App 内直接付款拿到票/房间有多近"为核心筛选标准(而非单纯的数据完整度),逐服务给出定位、接入方式与门槛、对现有产品缺口的映射,并给出综合优先级矩阵。

结论:优先研究 **Trip.com**(酒店预订,唯一能补 Canvas `stay` 字段可预订能力的候选)与 **Klook**(门票/活动,外国游客定位最精准);**KKday** 与 Klook 高度同质化,建议只接一家;**大众点评** 内容质量最高但存在竞品条款法律风险,建议先做商务/法务评估;**百度地图**/**腾讯位置服务** 与高德数据重合度高,无增量价值,不建议投入。

验收标准:诚实披露研究方法的局限——六个候选官方文档站点直接 `WebFetch` 均返回 `403 Forbidden`(已核实非代理故障),因此结论基于 `WebSearch` 结果交叉印证而非逐字源码级核实,与 v0.2.6 FlyAI 研究的严谨度不同,文档内逐条标注「⚠️ 待人工核实」,避免被当作可直接执行的实现依据。

排除:不新增任何产品运行时代码、不新增 provider 抽象、不新增外部 key、不新增 `mock-inventory.md` 条目(六个候选均未进入"代码已就绪等 key"状态)。本轮开工基于较早的 `main`(v0.2.8),完工后发现 `main` 已并行推进至 v0.2.16,`git merge` 试探性合并因工作流文档版本号编号冲突而 `git merge --abort`,改为在 `main` 最新提交上重新构建,版本号顺延为 `v0.2.17`。

## v0.3.1 更新 —— Android 原生 APK 专项规划及主线战略调整(纯规划)

需求来源: 
为了保障产品的体验上限与核心业务成果的落地，项目研发主线正式从 Web 网页端转向**移动端原生应用开发**（原生 iOS App + 原生 Android APK）。网页端迭代降级为次要维护支线。本迭代版本（v0.3.1）为 Android 原生 APK 专项规划版本，用以全面指导后续的原生 Android 开发。

交付:

- **战略调整决议**: 
  - 核心主线: 原生移动端应用。Android 使用 Kotlin + Jetpack Compose，iOS 使用 Swift + SwiftUI。
  - 次要支线: Web 端（Next.js Web App）停止主线功能迭代，仅维持现有代码编译 and 基础修复。
- **Android 原生布局规范**:
  - Material Design 3 (M3) 设计体系在原生 App 上的全面设计，包含 8dp 栅格系统、圆角嵌套数学、多屏幕 WindowSizeClass 响应式规则（不采用跨端混合方案，全原生 ConstraintLayout/Column/Row/Box 架构）。
- **移动页面体验规格**:
  - 单手操作热区设计（拇指区域）；大图双指缩放及行程-地图双向 nested scroll 手势阻尼隔离；
  - 双栏布局（平板/折叠屏）与单栏布局（手机）在不同设备下的分屏与层叠机制。
- **功能模块分类与功能树**:
  - v0.3.2 融合规划后,产品层主导航改为 Today / Butler / Plan / Explore / Tools。Canvas 是 Today/Plan 内部的行程内容模型,Chat 是 Butler 的交互形态。打车司机卡片必须通过 Today、当前行程卡或 Day Detail 的显性按钮大字直达,不采用隐藏/后台式触发。
- **原生开发落地选型**:
  - 架构采用 ViewModel + StateFlow 的 MVI (Model-View-Intent) 模式。
  - 技术选型: Room DB, DataStore, Retrofit, Coroutine + Flow, Hilt Dependency Injection。
  - 离线优先（Offline-First）数据同步与 Room 本地缓存容错机制。

验收标准:

- 原生 Android 规格说明书（`docs/planning/v0.3.1-android-native-spec.md`）已完整创建并包含头脑风暴、筛选收敛、多角色对抗评审及定稿规划。
- `v0.3.2` 已新增 `docs/planning/v0.3.2-android-planning-synthesis.md` 作为融合定稿，确认下一阶段代码研发里程碑为 `v0.3.3` Android Native Foundation。

排除:

- 本期仅做方案规划与技术选型，禁止产出任何 Java/Kotlin、XML 布局或业务实现代码。
- 不安装任何新的 npm 依赖或修改 Supabase 线上架构。


## v0.3.4 需求更新 —— 第一轮真实 Android 原生代码(v0.3.3 + v0.3.4 合并交付,已完成)

需求来源:操作者明确"就放在同一个 repo 里面，现在开始正式开工"，随后追加"0.3.4也一起做完"，把原计划分两轮的 v0.3.3 Android Native Foundation 与 v0.3.4 Today + Plan Execution MVP 合并为一轮交付。

交付:`android/` Gradle 模块(monorepo,包名 `space.go2china.visepanda`),技术栈 Kotlin + Jetpack Compose + Material 3 + Hilt + Room/DataStore(定义未深接)+ Retrofit(预置)。五 surface 底导(Today/Butler/Plan/Explore/Tools);Today 与 Plan+Day Detail 有真实内容,数据层(`MockTripData.kt`/`TripCompleteness.kt`)逐字段/逐规则移植自 Web 端 `lib/mock-ai/mockButler.ts`/`lib/trips/completeness.ts`,确保两端 readiness 百分比永不打架;Butler/Explore/Tools 为诚实占位页;Taxi Driver Card 单一共享组件只能通过显性按钮触发(落实 v0.3.2 对隐藏手势方案的否决)。

验收标准:五 surface 均可切换;Today 能回答"我现在该干嘛";Plan 能查看 day 列表与完成度;Day Detail 能查看 block 详情、地址、booking readiness("Info only"标签,不暗示可下单);Taxi Driver Card 只在显性按钮处可触达,支持大字展示与复制中文地址。**⚠️ 上述验收标准是按代码设计意图描述的,不是实测结果**——本沙箱无 Android SDK 且 Google Maven 仓库域名 `dl.google.com` 被本会话出站网络策略拦截,全部代码未经任何真实 `./gradlew` 构建验证,详见 `android/README.md` 的完整披露与首次构建清单。

排除:不接入真实网络/Supabase/AI Butler 响应;不实现地图、相机、麦克风、任何运行时权限请求;Needs Scheduling 候选态管理推迟到 `v0.3.7`;不改动任何 Web 端代码、`/api/*` 路由或 Supabase schema。
