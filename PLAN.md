# VisePanda — 执行计划

## 阶段一：AI Butler Chat MVP 骨架

- [x] 任务 1.1：建立 Next.js + React + TypeScript 项目骨架，适配 Vercel 部署。
- [x] 任务 1.2：定义 Trip Canvas、Butler Alert、Chat Message、Canvas Patch 等核心类型。
- [x] 任务 1.3：实现 mock AI butler pipeline，让用户消息可以生成结构化 canvas patch。
- [x] 任务 1.4：实现 Live Trip Canvas，包括 trip summary、day summary cards、顶部任务提醒卡。
- [x] 任务 1.5：实现 Chat / AI Butler 主工作台，桌面为左画布右聊天。
- [x] 任务 1.6：实现 Trips、Explore、Tools、Account 初始页面。
- [x] 任务 1.7：加入 warm New Chinese 水墨背景和纸卡视觉系统。
- [x] 任务 1.8：加入单元测试、组件测试、API 测试、Playwright 烟测。
- [x] 任务 1.9：将 Chat 工作台调整为桌面横屏一屏固定布局，页面本身不纵向滚动。
- [x] 任务 1.10：Trip Canvas 主界面只显示每日一句摘要，完整每日行程通过右侧抽屉查看。
- [x] 任务 1.11：移除独立 Practical Reminder 区块，将管家提醒合并到顶部五张任务卡。
- [x] 任务 1.12：移除 Chat 默认演示对话，建议问题改为两列，并在每次 AI 回复后生成 2 个上下文建议问题。
- [x] 任务 1.13：将 Live Trip Canvas 改为 Day 时间线，每天显示 Morning / Afternoon / Evening 三段，并移除顶部五张任务卡。
- [x] 任务 1.14：将每日详情抽屉升级为可编辑抽屉，支持本地修改日程块、酒店、交通和备注。
- [x] 任务 1.15：压缩 Chat / Trips / Explore / Tools 桌面端标题、间距和摘要卡高度，保持页面一屏锁定，长内容通过内部滚动展示。
- [x] 任务 1.16：为顶部 Chat / Trips / Explore / Tools 导航添加线性图标，替换字母占位。

## 阶段二：AI Provider 与 Supabase 接入

- [x] 任务 2.1：接入 DeepSeek V4 Flash 真实 AI provider，保留 mock fallback。
- [x] 任务 2.2：设计 Supabase schema：users、trips、messages、canvas_versions。
- [x] 任务 2.3：实现 guest draft 到 logged-in synced trip 的迁移路径。
- [x] 任务 2.4：实现基础 auth（Supabase magic link），将 Account 占位页升级为真实登录/同步入口。
- [x] 任务 2.5：将 Account 从独立页面改为头部图标 + 悬浮窗口，登录方式从 magic link 改为邮箱密码 + Google OAuth，登录后支持改名/改密码/登出。

## 阶段三：Trips 行程库

- [x] 任务 3.1：将 Trips 从占位页升级为静态 saved trips dashboard 骨架。
- [x] 任务 3.2：加入 mock trip cards、状态筛选、概览指标和 Continue in Chat 入口。
- [x] 任务 3.3（首个闭环）：接入真实 Supabase trip persistence，支持从 Chat 保存当前 canvas、在 Trips 读取已登录用户的真实行程、并从 Trips 恢复到 Chat。仍需：未登录/未配置 Supabase 时使用 mock 数据兜底；trip detail 页面、归档/分享流程未实现。
- [x] 任务 3.4：实现 trip detail 页面和从 Trips 恢复 Chat Canvas 上下文。
- [x] 任务 3.5：实现分享状态、归档状态和基础协作/分享链接。
- [x] 任务 3.6：强化 Trips dashboard 和 trip detail 的 draft / ready / shared / archived 状态说明与下一步操作提示。

## 阶段四：Explore 与第三方 Provider

- [x] 任务 4.1：将 Explore 从占位页升级为城市、景点、美食、住宿入口。
- [x] 任务 4.2：设计 provider abstraction，避免 UI 直接依赖 Amap、Trip.com、Meituan。
- [x] 任务 4.3：扩展 verified/static provider 城市覆盖和字段结构，为真实第三方 API 接入前验证 Explore 信息架构。
- [x] 任务 4.4：把 Explore 结果加入 Chat 工作台的 Add to Trip / Add to Canvas 流程。
- [x] 任务 4.5：强化 Explore Add to Trip 文案，明确跳回 Chat 后由 AI 管家重新平衡路线。

## 阶段五：Tools 与落地旅行能力

- [x] 任务 5.1：实现签证、入境、支付设置、翻译、汇率、地铁、eSIM/VPN、应急工具页面（静态 provider 驱动的骨架，覆盖 7 个分类）。
- [x] 任务 5.2：让顶部任务/提醒卡可以深链到对应工具（`/tools?category=<tool-category-id>`）。
- [x] 任务 5.3：补充工具分类的结构化内容、离线可读 pocket notes 和移动端一列可读布局。

## 阶段六：场景化视觉与体验增强

- [x] 任务 6.1：实现 destination-aware background switching。
- [x] 任务 6.2：规划北京时切换为长城/故宫风格水墨背景氛围。
- [x] 任务 6.3：规划上海时切换为外滩/江南园林风格水墨背景氛围。
- [x] 任务 6.4：根据 active trip canvas destination state 自动切换背景，避免做成手动主题选择器。

## 阶段七：真实 Provider 接入准备

- [x] 任务 7.1：为 Explore provider 增加 provider readiness metadata，记录当前静态模式、覆盖范围、候选第三方 API、限制和下一步接入优先级。
- [x] 任务 7.2：在 Explore 页面渲染 provider status，不让 UI 直接硬编码候选 provider 文案。
- [x] 任务 7.3：为 Tools provider 增加 provider readiness metadata，记录实时汇率、机器翻译、签证规则、交通数据等候选数据源。
- [x] 任务 7.4：在 Tools 页面渲染 provider status，与分类级 `apiPriority` 形成总览 + 详情两层信息。

## 阶段八：Canvas 管家提醒深链

- [x] 任务 8.1：为 Trip Canvas 添加轻量 `ButlerReminders` 提醒列表，位于 Day 时间线下方（不在顶部），每个 `ButlerAlert` 类型映射到对应 Tools 分类深链（`visa`→`visa-and-entry` 等）。

## 阶段九：真实数据源接入

- [x] 任务 9.1：接入 ExchangeRate-API 实时汇率数据。新增 `/api/exchange-rate` 服务端路由（通过 `EXCHANGE_RATE_API_KEY` 环境变量调用，不暴露给浏览器）；新增 `lib/tools/liveToolsProvider.ts`，包装静态 provider 并在 currency 分类注入实时 CNY 汇率行（每小时 ISR 刷新），API 不可达时优雅回落到静态文案；`lib/tools/index.ts` 工厂切换为 `createLiveToolsProvider()`。
- [x] 任务 9.2：接入高德地图 POI 搜索 API。新增 `/api/explore/amap` 服务端路由（通过 `AMAP_API_KEY` 环境变量调用，支持 `cityId` + `type` 查询参数，类型码：景点 `110000`/餐饮 `050000`/住宿 `100000`）；新增 `lib/explore/amapProvider.ts`，按城市按分类请求高德 POI，每个方法在返回空列表时回落到对应城市的静态数据；`lib/explore/index.ts` 工厂切换为 `createAmapExploreProvider()`。

## 阶段十：Translator 独立页面

- [x] 任务 10.1：建立 `/translate` 独立页面，含三标签布局（Text / Scan / Phrases）、导航新增 Translate 标签（`Languages` 图标）、Tools Translate 分类新增「Open Full Translator」CTA 链接，TripCanvas 移除 ButlerReminders 渲染（组件文件保留）。
- [x] 任务 10.2：实现文字翻译功能——`/api/translate/text` 服务端路由（DeepSeek，返回翻译 + 拼音 JSON）；支持 EN/FR/DE/JA/KO/ES → ZH 和 ZH → EN 双向；语言选择器 + 一键互换；Web Speech API TTS 朗读（zh-CN）；复制按钮。
- [x] 任务 10.3：实现 OCR 扫描翻译——拍照/上传图片后在客户端将图片转为 base64 并调整尺寸（Canvas API，最大 1200px）；`/api/translate/ocr` 服务端路由调用 OCR.space API（中英文识别，免费 demo key 无需配置，可选 `OCR_SPACE_API_KEY` 环境变量提升限额）；提取文字后自动转入翻译流程并支持 TTS。
- [x] 任务 10.4：实现静态常用短语词库（Greetings / Dining / Transport / Shopping / Emergency / Hotel 6 类，共 44 条）和特殊词语词库（景点 8 条 / 菜名 12 条 / 常见标识 8 条）；每条包含英文、中文、拼音、背景说明；每条可点击 TTS 朗读（Web Speech API）。
- [ ] 任务 10.5（规划中，暂未实现）：语音识别输入——Web Speech API SpeechRecognition 识别语音转文字，自动送入翻译流程。当前仅占位，按钮显示"Coming soon"。

## 阶段十一：社区页面（规划中，暂未实现）

- [ ] 任务 11.1（规划）：Community 页面骨架——用户发帖/分享入口，`/community` 路由，NavTabs 可选择性添加社区标签。
- [ ] 任务 11.2（规划）：行程分享广场——浏览已公开分享的行程卡片，查看分享详情，评论（需 Supabase 登录）。
- [ ] 任务 11.3（规划）：旅行照片分享——上传照片到 Supabase Storage，与行程/城市/景点绑定，浏览社区照片墙。
- [ ] 任务 11.4（规划）：景点/美食点评联动——与高德 POI（`AMAP_API_KEY`）和美团数据（待评估 API）联动，展示真实评分、热度和用户点评。
- [ ] 任务 11.5（规划）：社区后台数据库设计——Supabase 新表：`community_posts`（标题/正文/用户/类型）、`community_media`（图片/存储路径）、`community_likes`、`community_comments`，新增对应 migration SQL。
- [ ] 任务 11.6（规划）：社区内容基础治理——内容举报功能占位、关键词过滤（本地规则集）、管理员审核队列设计。

## 阶段十二：智能对话管线与数据融合（v0.1.45 规划，代码未改动）

> 本阶段是一次**纯文档产品规划迭代**（v0.1.45），不改动任何代码。目标是从宏观层面重新定义产品方向，并把「让 Chat 更高效、更规范、更懂用户」和「Chat×Explore×Trips 通过对话联动真实地图/美团数据」两条主线，蒸馏成 v0.1.46–v0.1.52 的可执行路线图。所有下列任务均为**规划中（未实现）**，实现时必须遵守既有的 provider abstraction、server-side key、mock fallback 约束。

### 产品定位（宏观）

- VisePanda 的核心不是「功能集合」（Chat/Explore/Tools/Translate），而是解决西方游客来中国前的**五大恐惧**：签证是否合格、能不能付款、手机能不能上网、看不懂中文、行程怎么串。每个功能都映射到它所化解的恐惧。
- Chat 是把其他界面串起来的**唯一主线**。Explore 的数据、Tools 的清单、Translate 的能力，最终都应该能在 Chat 对话里被自然触发，而不是让用户在 6 个 tab 之间来回跳。
- 当用户的恐惧被 Chat 主动化解，他们就会停留在 Chat，而不是在 tab 之间流失。

### v0.1.46：Chat 智能层 I —— 意图分类 + 路由 + 提示词精炼 + 回答规范化

- [ ] 任务 12.1：新增轻量意图分类器（正则 + 关键词，无 LLM 成本，<50ms），把用户消息分类为 10 种意图：`create_trip` / `adjust_trip` / `add_location` / `add_poi` / `ask_factual` / `ask_recommendation` / `preference_signal` / `concern` / `logistics` / `unclear`。
- [ ] 任务 12.2：实体抽取——从原始消息中抽取城市、天数、预算信号、饮食提及、同行人数，构造结构化 `refinedPrompt` 再送入 DeepSeek，而不是直接把原始口语送进模型。
- [ ] 任务 12.3：非 LLM 处理器——`ask_factual`（签证/支付/eSIM/地铁等）直接查 `lib/tools` 静态知识库并以内联工具卡返回（<100ms，零成本）；`preference_signal` 只更新 profile 并返回一行确认，不触发完整 canvas patch。
- [ ] 任务 12.4：回答规范化——在 system prompt 中强制 `{headline, body, highlights, watchOut, nextStep}` 结构，`ChatPanel` 按分区渲染（headline 大字、highlights 绿色勾、watchOut 琥珀色、nextStep 变主建议 chip），取代单条自由文本 `assistantMessage`。
- [ ] 任务 12.5：上下文建议——固定返回 2 个（而非 4 个）根据当前 trip state 计算的建议 chip；空画布/已建行程/某天过满/提到美食/提到签证各有不同建议。

### v0.1.47：偏好画像 + 意图蒸馏

- [ ] 任务 12.6：定义 `UserPreferenceProfile` 类型（pace / travelStyle / partySize / budgetPerDay / hotelStars / dietaryRestrictions / cuisinePreferences / interests / mobilityLevel / experienceLevel / profileConfidence）与 `ProfileContext`。
- [ ] 任务 12.7：静默抽取——从任意口语消息中提取偏好（「走累了」→ pace:light；「带小孩」→ family_with_kids；「学生党」→ economy；「不吃猪肉」→ no_pork），不做表单式盘问。
- [ ] 任务 12.8：一问规则——Butler 每轮最多问 1 个澄清问题，且仅当该信息缺失会导致行程明显错误时才问（如季节影响赏花时间），问题自然嵌入对话。
- [ ] 任务 12.9：画像持久化——登录用户存 Supabase（新增 `profiles` 表 + migration），guest 存 localStorage；每次 DeepSeek 调用都把结构化 profile 注入 system prompt。
- [ ] 任务 12.10：ChatPanel 头部显示偏好 chips（如「Foodie · Mid budget · 2 people」），让用户知道 Butler「记得」自己。

### v0.1.48：Chat 质量激活 —— 已配置模型 + 结构化回复（实际完成）

> 实际 v0.1.48 根据用户最新输入优先执行：DeepSeek、Zhipu、Moonshot、Qwen keys 已在 Vercel 配好，因此本轮先激活多模型默认配置和回答规范化。原计划的高德 POI rich fields 顺延为后续实现项。

- [x] 任务 13.8：将多模型 registry 默认模型对齐到生产配置：DeepSeek v4 flash、Qwen 3.6 Flash、Zhipu GLM5、Moonshot Kimi 2.5，同时保留 `*_CHAT_MODEL` 环境变量覆盖。
- [x] 任务 13.9：扩展 `CanvasPatch` / `ChatMessage`，支持可选结构化回复 `{ headline, body, highlights, watchOut, nextStep }`，同时保留 `assistantMessage` 作为纯文本 fallback。
- [x] 任务 13.10：更新 Butler prompt 和 parser，要求 live providers 返回结构化回复；旧格式 provider JSON 自动转为最小结构，不阻断历史兼容。
- [x] 任务 13.11：更新 `ChatPanel`，将 VisePanda 的回复渲染为 headline + highlights + watchOut + nextStep 的紧凑管家卡片，历史消息和 mock fallback 继续按普通文本显示。
- [x] 任务 13.12：补充模型默认值和结构化回复解析测试。

### v0.1.49：高德 POI 数据丰富化（已完成）

- [x] 任务 12.11：升级 `/api/explore/amap` 使用 `extensions=all`，捕获高德已返回但此前被丢弃的字段：`rating` / `cost` / `tel` / `opentime_week` / `photos` / `business_area` / `location`。
- [x] 任务 12.12：`AmapPoi` 接口补齐上述字段；`lib/explore/types.ts` 新增全可选的 `ExploreRichMeta`（rating / pricePerPerson / priceLevel / tel / openHours / photoUrl / sourceLabel / location），`ExploreAttraction`/`ExploreFoodSpot`/`ExploreStay` 继承之。
- [x] 任务 12.13：`amapProvider.ts` 的 mapper 填充 rich 字段；静态 provider 保持字段为 undefined（graceful degradation）。
- [x] 任务 12.14：Explore 卡片 UI 条件渲染评分、价格档、营业时间、电话、缩略图、来源和商圈；没有 rich data 时保持原卡片。

### v0.1.50：工具调用 Butler 第一版（真实 POI 数据进对话，已完成）

- [x] 任务 12.15（第一版）：`/api/chat` 的 orchestrator 在调用模型前，为 `create_trip` / `add_poi` / `ask_recommendation` / `logistics` 预取高德 POI 上下文并注入 prompt。完整多轮 function-calling loop 留待后续。
- [x] 任务 12.16（第一版）：定义 `ButlerToolContext` / `ButlerToolPoi`，等价于 bounded `search_pois` 工具结果；Dianping stub 尚未接入。
- [ ] 任务 12.17：扩展 `TripBlock`（向后兼容，全可选）：`poiId` / `sourceLabel` / `rating` / `priceEstimate` / `openHours` / `phone` / `photoUrl` / `mapUrl` / `bookingUrl` / `location{lat,lng}`。
- [ ] 任务 12.18：在 Chat 对话流内联渲染真实 POI 卡片（评分/价格/照片），卡片带「Add to Day N」按钮，直接进既有 AI pipeline。
- [ ] 任务 12.19：确认 DeepSeek 计划支持 function calling（`deepseek-chat` 支持；若 V4 Flash 不支持，简单调整用 Flash、工具循环用 V3）。

### v0.1.51：偏好画像 + 一问规则第一版（已完成）

- [x] 任务 12.6（第一版）：定义 `UserPreferenceProfile`，覆盖 pace / budget / party / dietaryRestrictions / cuisinePreferences / interests / profileConfidence。
- [x] 任务 12.7（第一版）：从任意口语消息中静默抽取偏好（预算、轻松节奏、带孩子、no pork、food/history 等），不做表单式盘问。
- [x] 任务 12.8（第一版）：prompt 注入“一问规则”，要求 Butler 每轮最多问 1 个必要澄清问题。
- [x] 任务 12.9（第一版）：guest profile 存 localStorage 并随 `/api/chat` 发送；Supabase `profiles` 表持久化留待后续 migration。
- [x] 任务 12.10（第一版）：ChatPanel 头部显示偏好 chips（如 `light pace` / `economy budget` / `family with kids`）。

### v0.1.52：产品交互逻辑与用户旅程蓝图（文档规划，代码未改动）

> 本轮是一次深度产品策略/交互规划迭代。详细蓝图见 `docs/planning/v0.1.52-product-interaction-blueprint.md`。核心判断：VisePanda 不应只被定位为 AI itinerary generator，而应是外国游客来华的 China travel operating system，把入境、支付、联网、语言和行程五类焦虑连接成一条可执行旅程。

- [x] 任务 12.20a：重新定义产品定位：Chat 是 command center，Trip Canvas 是 source of truth，Tools/Translate/Explore/Trips/Account/Community 是围绕旅程运转的支持系统。
- [x] 任务 12.20b：建立用户旅程模型：Curious → Planning → Preparing → In China → Share/Get help，并为每一阶段定义用户心理、最佳交互和成功信号。
- [x] 任务 12.20c：重定义页面职责：Home 负责 archetype start，Chat 负责决策，Canvas 负责可执行行程和动作，Trips 负责连续性/ready 状态，Explore 负责数据发现，Tools 负责旅前/旅中小工具，Translate 变成全局 utility，Account 承担信任/偏好/留资。
- [x] 任务 12.20d：补齐 feature linkage matrix，明确 Home→Chat、Explore→Chat、Canvas quick actions→Chat、Tools→Chat、Translate→当前上下文、Account/Trips→高意向留资的联动关系。
- [x] 任务 12.20e：给出后续实施路线；v0.1.54 已完成 Interaction Shell I，当前顺延为 v0.1.55–v0.1.61 继续执行 Canvas Action Layer、Inline Tool Cards、TripBlock POI Embedding、Translate Everywhere、Tools Widgets I、Account Center + Preference Review、Admin + Customer Brief。
- [x] 任务 12.20f：沉淀 UX writing rules 和指标体系：用 traveler-facing 状态替代 developer-facing 状态，每轮 AI 回复都要有具体下一步；关注 time to first canvas、trip edits/session、prep item completion、translate quick action usage、share/lead conversion 等。

### v0.1.53：Interaction Shell I / 引导入口 + 画布快捷操作（规划）

- [ ] 任务 12.20：Home 增加 3 个原型入口（「首次中国 10 天精华」/「美食三城」/「历史+自然」），点击后带 `?archetype=` 参数预置 trip state，并由 Butler 以「建议」口吻呈现。
- [ ] 任务 12.21：首跑向导——3 个 chip 问题（无需打字）即可生成起始草稿；替换空白 textarea 空状态。
- [ ] 任务 12.22：画布进度指示（「你的行程已成型 40%」），基于 TripState 完整度（城市/日期/酒店区/美食/交通/行前清单）打分。
- [ ] 任务 12.23：Day 卡片内联快捷按钮（Lighten / Swap morning / Add food），每个按钮发送预制 Butler 意图，用户无需打字。
- [ ] 任务 12.24：把 `confidence`（Draft/Refined/Ready to save）映射为友好文案（「Taking shape / Looking good / Save it!」）；行程有标题后用标题替换「Live Trip Canvas」h1。

### v0.1.57：Translate Everywhere + 导航模式重构（规划）

- [ ] 任务 12.25：Translate 从主导航移除，改为全局悬浮相机/麦克风按钮（底部右侧常驻），点开是已激活 OCR 的 bottom sheet，不离开当前上下文。
- [ ] 任务 12.26：Explore 从主导航降级，POI 浏览通过 Chat 内「Browse [City]」按钮进入；Explore 页面保留为可浏览的发现面，但不再是一级 tab。
- [ ] 任务 12.27：主导航精简为 Chat · Trips · Tools · Community（4 tab）。
- [ ] 任务 12.28：Chat 对话内联渲染工具卡（签证提醒、支付清单在对话中直接出现），复用 Tools modal 的组件。
- [ ] 任务 12.29：已登录用户从 `/` 直接进 `/chat`（Home 仅作落地页）。

### 后续规划：大众点评/美团 + 地图（审批后）

- [ ] 任务 12.30：`search_dianping` 接真实大众点评 POI 搜索（评分/评论数/人均），替换 stub。
- [ ] 任务 12.31：酒店预订 deeplink 进美团/携程 App；餐厅卡展示真实人均消费与评论数。
- [ ] 任务 12.32：注册 `NEXT_PUBLIC_AMAP_MAPS_KEY`（仅用于地图显示、域名白名单、可公开）；在 Day 抽屉/Canvas 加 `DayMapWidget`，对含 2+ 定位块的天渲染标记 + 路线。
- [ ] 任务 12.33：Chat→Explore 反向联动——行程内 POI 在 Explore 城市视图显示「In your trip」徽标；偏好驱动 Explore 卡片排序。

### v0.1.54–v0.1.61 当前推荐实施顺序（v0.1.54 已完成 Interaction Shell I）

1. `v0.1.54` Interaction Shell I（已完成）：Home archetype starts、Chat first-run chips、structured `nextStep` 主按钮、Canvas 标题/状态友好化。
2. `v0.1.55` Canvas Action Layer：trip completeness、day quick actions、prep blockers，从“展示行程”升级为“可操作行程”。
3. `v0.1.56` Inline Tool Cards：签证、支付、eSIM、汇率、应急卡在 Chat 内出现，减少 tab 切换。
4. `v0.1.57` TripBlock POI Embedding + Day Detail Upgrade：把真实 POI 字段持久化到行程块，Day detail 展示中文名/地址/营业时间/电话/地图/为什么适合用户。
5. `v0.1.58` Translate Everywhere：Translate 作为全局 camera/mic utility，能从 Day detail、Tools、Chat 回到当前上下文。
6. `v0.1.59` Tools Widgets I：currency converter、visa eligibility checker、payment setup wizard、emergency card generator。
7. `v0.1.60` Account Center + Preference Review：真实 `/account` 信任中心、偏好编辑、隐私/同意、渐进式留资。
8. `v0.1.61` Admin + Customer Brief Planning/Build：leads schema、admin role、customer brief、对话/行程摘要视图。

### 阶段十二关键设计取舍（详见 DESIGN.md ADR-037~042）

- 为什么用工具调用而非 RAG：RAG 预嵌入静态知识；工具调用在规划时查实时数据，知道某餐厅当天是否营业、某景点是否需提前购票。
- 为什么保留 static/mock provider：既有安全约束——真实数据源不可用/未审批时对话仍可用，只是退化为无真实 POID 的纯文本行程。
- 为什么不流式工具调用：`response_format: json_object` 一次性输出，解析更稳；流式可后续仅对 `assistantMessage` 字段加。
- 为什么高德要两把 key：POI 搜索 REST key（`AMAP_API_KEY`）永远服务端、按调用计费、绝不进浏览器；地图显示 JS key（`NEXT_PUBLIC_AMAP_MAPS_KEY`）按 Referer 白名单、可公开、无计费风险。

## 阶段十三～十八：产品扩张规划（v0.1.46 规划，代码未改动）

> 第二次**纯文档战略规划迭代**（v0.1.46）。详细深潜见 `docs/planning/v0.1.46-product-expansion.md`。核心原则变更：**不再考虑 token 成本，只优化用户体验与回答质量**——可以用更大模型、多模型集成、多轮精炼+校验。以下全部为**规划中（未实现）**。

### 阶段十三：质量优先 + 多中国 LLM 编排（v0.1.47 首个代码迭代）

- [x] 任务 13.1：新增 `lib/ai/modelRegistry.ts` 描述各中国厂商（DeepSeek / Qwen-DashScope / 智谱 GLM / Moonshot Kimi / 百度 ERNIE / MiniMax），含能力标签 `reasoning|chinese|longContext|vision|chinaFacts|judge` 和服务端 key 名（含别名）。
- [x] 任务 13.2：新增 `lib/ai/orchestrator.ts`——按意图选择编排模式；每个 provider 封装在统一 `ChatCompletionProvider` 接口后（`lib/ai/providers/*`，OpenAI 兼容实现覆盖全部 6 家）。
- [x] 任务 13.3：意图路由到专长模型（行程推理→DeepSeek/GLM；中文 POI/菜名→Qwen；签证法规→ERNIE；`selectProvidersForIntent` 按能力优先级排序 + fallback 链）。新增 `lib/ai/intentClassifier.ts`（10 类意图，本地正则）。
- [x] 任务 13.4（第一版）：高风险意图（`create_trip`/`ask_factual`）在 2+ provider 时走并行集成（race，优先主模型），失败继续 fallback 链。完整 judge 合并 + 分歧写 `watchOut` 留作后续。
- [ ] 任务 13.5（规划中）：精炼-校验循环——reasoner 起草，另一模型用工具数据校验事实并改写为 `{headline,body,highlights,watchOut,nextStep}`（依赖阶段十二 Chat 智能层的回答 schema）。
- [x] 任务 13.6：全链 fallback，最终仍回落 mock Butler；新增 key 全服务端并在 `lib/env/placeholders.ts` 文档化：`ZHIPU_API_KEY`/`MOONSHOT_API_KEY`/`ERNIE_API_KEY`/`MINIMAX_API_KEY` + 各家 `*_BASE_URL`/`*_CHAT_MODEL` 覆盖。
- [x] 任务 13.7：`/api/chat` 切换到 orchestrator；`ButlerWorkspace` 状态显示真实模型标签；新增 3 个测试文件（16 用例）；全套 95 测试通过、build 成功。

### 阶段十四：原生 iOS / Android 应用（仅规划，不执行）

- [ ] 任务 14.1：技术选型——推荐 React Native + Expo（真原生视图、复用现有 TS 领域层与 API），备选纯 Swift/Kotlin（双代码库）。
- [ ] 任务 14.2：抽取共享核心包（`lib/types`、`lib/i18n`、provider 接口、orchestrator client），web 与 mobile 共用；现有 Next.js API 路由直接作为移动端后端。
- [ ] 任务 14.3：原生 UI——底部 tab 导航 + Chat/Trips/Explore/Tools/Account 原生屏；原生地图（react-native-maps + 高德 SDK）。
- [ ] 任务 14.4：离线优先——本地 SQLite 缓存行程/工具清单/短语库，断网可用，重连同步。
- [ ] 任务 14.5：原生设备能力——相机 OCR、麦克风 STT、推送（签证截止/预订提醒）、深链、生物识别解锁。
- [ ] 任务 14.6：应用商店——Apple Developer / Play Console；**中国分发单独 track**（ICP 备案 + 软著 + MIIT 登记），法务/运营与工程并行（提前量长）。

### 阶段十五：Tools 功能化升级（6 个工具给真实效果）

- [ ] 任务 15.1：为 `ToolCategory` 增加可选 `interactive` 描述符（widget 类型 + 配置），卡片在静态内容下渲染交互组件，数据不可用时降级为文字。
- [ ] 任务 15.2：签证——交互式资格检查器（国籍→签证类型/免签/144-240h 过境）、过境时长计算器、带进度的文档清单。
- [ ] 任务 15.3：支付——Alipay/微信支付国际卡绑定分步向导 + App 深链 + 卡兼容检查。
- [ ] 任务 15.4：汇率——完整换算器（金额输入、实时汇率、反向换算、常用金额、离线快照）。
- [ ] 任务 15.5：地铁——路线规划器（城市+起终站→路线/换乘/票价/时间，经 `/api/tools/transit` 高德换乘 API；站名 EN+中文）。
- [ ] 任务 15.6：eSIM/VPN——真实运营商对比 + 购买链接 + QR 安装引导 + VPN 合规说明。
- [ ] 任务 15.7：应急——一键拨号 110/120/119、GPS 使馆定位、医疗短语卡 TTS、"用中文分享我的位置"、最近医院。

### 阶段十六：Account 专业化 UI + 留资

- [ ] 任务 16.1：新增独立 `/account` 页面（主流、专业、正式，凸显信任感），卡片式资料中心：头像+姓名+会员等级、账户安全、资料完整度进度条。
- [ ] 任务 16.2：分区——Profile / My Trips / Preferences（偏好画像）/ Travel documents（护照国、签证状态，加密、opt-in）/ Notifications / Privacy & data（导出/删除/同意管理）。
- [ ] 任务 16.3：留资（渐进式画像，不一次性堆表单）——T1 联系（姓名/邮箱/手机+微信/首选渠道）；T2 行程资格（国籍/日期/人数/城市/预算/目的）；T3 增值（兴趣/饮食/是否需代订/来源渠道/营销同意）。
- [ ] 任务 16.4：存 Supabase `leads` 表（登录关联 user，guest 用 session id）；同意标记含时间戳与来源；单一"Talk to a China travel expert" CTA 打开留资表单。

### 阶段十七：用户管理后台 + LLM 客户简报

- [ ] 任务 17.1：角色门控 `/admin` 区（仅 `admin` 角色），不出现在旅行者导航。
- [ ] 任务 17.2：Leads 看板——可排序/筛选表（姓名/联系/国籍/日期/预算/lead score/状态/来源/日期），CSV 导出。
- [ ] 任务 17.3：客户详情——完整 lead + 关联行程 + 完整 chat 历史，并排 LLM 客户简报。
- [ ] 任务 17.4：LLM 简报管线——把 lead 字段 + 对话 + trip state 过多模型编排，产出结构化 `CustomerBrief`（summary / tripIntent / budgetSignal / readinessToBook 0-100 / keyPreferences / openQuestions / objections / suggestedNextAction / language）；按需生成 + 缓存，新消息到达重算。
- [ ] 任务 17.5：安全——admin API 全服务端 + 每请求角色校验；`SUPABASE_SERVICE_ROLE_KEY` 仅服务端；PIPL/GDPR：访问日志、保留策略、导出/删除。

### 阶段十八：前端与视觉优化（持续轨道）

- [ ] 任务 18.1：设计 token 化 + 复用组件库（Button/Card/Field/Pill/Modal/Sheet/Toast），页面不再手写样式。
- [ ] 任务 18.2：动效与反馈——canvas patch 入场动画、加载骨架屏、发送/保存乐观 UI、toast。
- [ ] 任务 18.3：空状态/错误状态设计（与 v0.1.45 首跑原型入口衔接）。
- [ ] 任务 18.4：可访问性——对比度、focus-visible、键盘导航、reduced-motion、RTL 校正。
- [ ] 任务 18.5：响应式（含平板断点）+ 性能（图片优化、路由级代码分割、字体策略、背景图权重预算）。
- [ ] 任务 18.6：品牌插画/图标体系（熊猫吉祥物状态、目的地插画）。

### Supabase 新迁移（阶段十六~十七）

- [ ] 任务 S.1：`supabase/migrations/0004_leads_and_admin.sql` —— `leads` / `lead_events` / `customer_briefs` / `profiles` 表 + 用户 `role`（traveler|admin）+ admin 跨表读 RLS。全部 key 服务端；service-role key 不入浏览器。

### 推荐构建顺序（v0.1.46 规划，版本号按实际构建时分配）

1. 多 LLM 编排（阶段十三，质量地基）→ 2. Tools 功能化（阶段十五，即时价值）→ 3. Account+留资（阶段十六，商业闭环）→ 4. Admin 后台+简报（阶段十七，依赖前两者）→ 5. 设计系统（阶段十八，持续，原生前先成熟）→ 6. 原生 App（阶段十四，最大工作量，放最后）。并行：阶段十八设计系统持续；阶段十三可即刻起步；阶段十四法务/运营（备案/软著/开发者账号）因提前量可并行启动。

## 关键约束

- 技术选型：Next.js App Router、React、TypeScript、Vercel、Supabase 预留。
- AI 约束：DeepSeek V4 Flash 只在服务端 API route 调用；真实 key 不进入浏览器、不写入仓库。
- Fallback 约束：缺少 `DEEPSEEK_API_KEY`、API 失败或模型输出不合法时必须回落到 mock provider。
- 当前重点：Chat / AI Butler 已完成 MVP 骨架；Trips 行程库已接入真实 Supabase persistence 首个闭环、归档/分享流程，并补充了状态说明；guest draft 已能自动迁移到登录账号；Explore 已升级为静态 provider 驱动的城市/景点/美食/住宿骨架，覆盖北京/上海/成都/西安/广州/杭州/苏州/重庆，并接入了 Add to Trip 回 Chat 重新平衡路线流程和 provider readiness metadata；Account 已从独立页面改为头部图标 + 悬浮窗口，支持邮箱密码登录/注册、Google 登录，登录后可改名/改密码/登出；Tools 已从占位页升级为静态 provider 驱动的 7 个分类骨架（签证入境/支付设置/翻译/汇率/地铁/eSIM-VPN/应急），支持深链、结构化分组、离线 pocket notes、API priority 和 provider readiness metadata；Trip Canvas 已支持目的地感知水墨背景氛围切换；`ButlerReminders` 已为每个 alert type 提供 Tools 分类深链，画布告警现在可直接导航到对应工具分类。
- ButlerReminders 约束：`components/canvas/ButlerReminders.tsx` 渲染在 Day 时间线下方（不占顶部），不恢复 CanvasTaskStrip；alert 类型到 Tools 分类的映射集中在该组件的 `alertToolCategoryMap`，后续如有新 alert 类型或新 Tools 分类，只修改映射表，不改 TripCanvas 或 ToolsBoard。
- Tools provider 约束：`lib/tools/types.ts` 定义 `ToolsProvider` 接口和 `ToolCategory` 类型；`lib/tools/staticProvider.ts` 是当前唯一实现（静态 checklist 内容，覆盖 7 个分类）；`lib/tools/index.ts` 的 `getToolsProvider()` 是组件唯一允许调用的入口；当前所有内容均为静态参考信息，不包含实时汇率、实时翻译或实时签证规则查询，后续接入真实数据源时只需替换该工厂内的实现。
- Tools 深链约束：`/tools?category=<tool-category-id>` 会自动选中对应工具分类；无效分类回退到第一个分类。后续从 Chat/Canvas/提醒入口跳转 Tools 时应复用该 URL 参数，不要恢复 Canvas 顶部五个任务框。
- Explore provider 约束：`lib/explore/types.ts` 定义 `ExploreProvider` 接口；`lib/explore/staticProvider.ts` 是当前唯一实现（静态数据，覆盖 Beijing/Shanghai/Chengdu/Xi'an/Guangzhou/Hangzhou/Suzhou/Chongqing）；`lib/explore/index.ts` 的 `getExploreProvider()` 是组件唯一允许调用的入口，后续切换真实 Amap/Trip.com/Meituan/Tripadvisor provider 时只替换这里，不应改动 `components/explore/ExploreBoard.tsx` 的渲染逻辑；provider 必须提供 `getProviderStatus()`。
- Explore Add to Trip 约束（`v0.1.17`）：`ExploreBoard` 的每个景点/美食/住宿条目都有一个 "Add to Trip" 按钮；点击后跳转到 `/chat?add=<编码后的草稿消息>`，由 `ButlerWorkspace` 在挂载时读取 `add` 参数并通过既有的 `handleSend` → `/api/chat` → `CanvasPatch` → `applyCanvasPatch` 流程发送，不允许在 Explore 组件里直接拼装 `TripDay`/`TripState` 或绕开 AI pipeline 写入画布。
- Trips 当前限制：`v0.1.14` 已支持 trip detail 页面、归档/恢复状态切换、生成与撤销分享链接、以及只读公开分享页 `/share/[token]`；分享页不展示 chat 历史。
- Account 约束：`v0.1.16` 起不再有独立 `/account` 页面；`components/account/AccountMenu.tsx` 是登录/账号管理唯一入口，渲染在 `AppShell` 头部；登录方式为邮箱密码（`signInWithPassword`/`signUpWithPassword`）和 Google OAuth（`signInWithGoogle`），不再提供 magic link；登录后的改名/改密码通过 `updateDisplayName`/`updatePassword`（均封装在 `lib/supabase/auth.ts`）完成。
- Supabase schema 约束：`supabase/migrations/0001_init_trip_schema.sql`、`supabase/migrations/0002_trip_archive_and_share.sql` 和 `lib/supabase/schema.ts` 是当前 schema 契约；`lib/supabase/tripsRepository.ts` 是唯一允许的 persistence 入口，不要绕开它直接拼 Supabase 查询。需要在 Supabase SQL Editor 中按顺序运行这两个迁移文件。
- Supabase 部署约束：本仓库目前没有真实 Supabase 项目；`NEXT_PUBLIC_SUPABASE_URL`、`NEXT_PUBLIC_SUPABASE_ANON_KEY`、`SUPABASE_SERVICE_ROLE_KEY` 未配置前，所有 Supabase 相关代码必须保持优雅降级（不崩溃、自动回落到 mock/guest 体验）。
- 视觉约束：warm New Chinese、水墨背景、实底纸卡；不要半透明玻璃聊天框；顶部 Chat / Trips / Explore / Tools 使用线性图标，不使用字母占位。
- 桌面布局约束：当前阶段优先电脑横屏端，一屏工作台和内部滚动优先；移动竖屏端后续精修。
- Canvas 约束：不要恢复顶部 Visa / Payment / Booking / Less tiring / Food-focused 五个任务框；每日主卡必须直接呈现 Morning / Afternoon / Evening。
- 文档约束：每一次迭代都必须同步更新 `PLAN.md`、`PRD.md`、`DESIGN.md`、`AGENTS.md`、`HANDOFF.md`。
- 测试约束：最终完成前必须跑 `npm run test`、`npm run build`、`npm run test:e2e`。

## 里程碑

- M1：AI Butler Chat MVP 骨架完成（2026-06-29，已完成）。
- M2：真实 AI provider 完成（2026-06-29，已完成）；Supabase schema 待排期。
- M3：Trips Dashboard 骨架完成（2026-06-29，v0.1.8）。
- M3.5：Live Trip Canvas 三段式时间线和可编辑每日抽屉完成（2026-06-29，v0.1.9）。
- M3.6：Supabase schema 设计完成（2026-06-29，v0.1.10）。
- M4：Supabase magic link 登录 + Trips/Chat 真实 persistence 首个闭环完成（2026-06-29，v0.1.11）；trip detail、归档、分享待排期。
- M4.5：guest draft 到 logged-in synced trip 自动迁移路径完成（2026-06-29，v0.1.12）。
- M4.6：Trip detail 页面完成，支持从 Trips 查看真实/示例行程详情并恢复到 Chat（2026-06-29，v0.1.13）。
- M4.7：分享状态、归档状态和只读分享链接完成（2026-06-29，v0.1.14）。
- M5：Explore provider abstraction 和静态城市/景点/美食/住宿骨架完成（2026-06-29，v0.1.15）。
- M5.5：Account 从独立页面改为头部图标 + 悬浮窗口，邮箱密码登录 + Google 登录，登录后可改名/改密码/登出完成（2026-06-29，v0.1.16）。
- M5.6：Explore Add to Trip / Add to Canvas 流程完成（2026-06-29，v0.1.17）。
- M6：Tools 从占位页升级为静态 provider 驱动的 7 个分类骨架完成（2026-06-29，v0.1.18）；Tools 分类深链完成（2026-06-30，v0.1.19）；真实第三方数据源待排期。
- M7：目的地感知背景切换完成（2026-06-30，v0.1.23）。
- M8：Explore/Tools provider readiness metadata 完成（2026-06-30，v0.1.24-v0.1.25）。
- M9：Canvas ButlerReminders 深链 Tools 分类完成（2026-06-30，v0.1.26）。
- M10：真实数据源接入完成（2026-06-30，v0.1.27）：ExchangeRate-API 实时汇率 + 高德地图 POI 搜索，两者均含静态 fallback。
- M11：Translator 独立页面完成（2026-06-30，v0.1.28）：文字翻译（DeepSeek + 拼音 + TTS）+ OCR 扫描翻译（OCR.space + TTS）+ 静态短语/特殊词语词库；NavTabs 增加第五个 Translate 标签。
- M12（规划中）：Community 社区页面——行程分享广场 + 照片 + 景点/美食点评 + 高德/美团联动。

## 风险

- 已知风险 1：真实 AI 输出需要严格结构化，否则 canvas patch 可能不稳定。
- 已知风险 2：DeepSeek key、限流、网络和模型输出质量会影响实时体验，因此必须保留 mock fallback。
- 已知风险 3：Supabase schema 一旦过早固定，后续 Trips/Canvas 迭代会受限。
- 已知风险 4：未配置 Supabase 或未登录时 Trips 仍展示 mock data；文案需要持续避免让用户误以为这些是真实保存的行程。
- 已知风险 5：第三方 API 能力边界必须先验证，不能伪造未确认的 Trip.com、Meituan、Amap 能力。
- 已知风险 6：水墨背景如果过重，会影响文字可读性和移动端性能。
- 待验证假设 1：用户会更喜欢"右侧持续聊天 + 左侧实时画布"而不是传统单列聊天。
- 待验证假设 2：Trips 行程库先以 dashboard 管理草稿/已确认/已分享状态，能自然承接 Chat 生成的行程。
## v0.1.30 Addendum - Translator Qwen Upgrade

- [x] Replace Translator text translation provider with Aliyun Bailian Qwen `qwen-mt-flash`.
- [x] Replace OCR.space scan translation with Aliyun Bailian Qwen OCR `qwen3.5-ocr`.
- [x] Replace browser Web Speech TTS with server-side Aliyun Bailian Qwen `qwen3-tts-instruct-flash`.
- [x] Implement Voice tab and `/api/translate/stt` with Aliyun Bailian Qwen `qwen3-asr-flash`, supporting recording/uploaded audio data URLs and public audio URLs.
- [x] Add shared `lib/aliyun/qwen.ts` helper, tests for Qwen routes, and Translator page test for the Voice tab.

Next translator priorities:

- [ ] Add optional Supabase Storage or OSS upload for recorded audio before STT when longer recordings need durable public URLs.
- [ ] Add language auto-detection and more source-language presets after the Qwen path is stable in production.

## v0.1.31 Addendum - Community MVP, Membership, and Avatars

- [x] Upgrade `/community` from static framework to local interactive MVP: local posting, type/city filters, likes, saves, comments, read-more detail, and localStorage persistence.
- [x] Add a compact membership level system: Bamboo Guest, Panda Explorer, Silk Road Insider, Dragon Pass, and VisePanda Concierge.
- [x] Show membership badges on community authors and a compact membership strip on the Community page.
- [x] Upgrade Photos tab to local photo-card publishing and local likes.
- [x] Add reusable panda avatar assets and Account avatar picker with localStorage persistence.
- [ ] Future: connect community posts/photos/likes/comments to Supabase and add Supabase Storage avatar/photo uploads, moderation, and real member progression.

## v0.1.32 Addendum - Tools Card Drawers

- [x] Remove the old Translate category from `/tools`; translation remains available through the dedicated `/translate` tab.
- [x] Convert Tools into six compact category cards: Visa and entry, Payment setup, Currency, Metro, eSIM/VPN, and Emergency.
- [x] Keep category cards name-only; hide checklist details until a card is opened.
- [x] Preserve `/tools?category=<tool-category-id>` deep links so external entries can open a specific drawer.
- [x] Remove provider implementation metadata from the Tools UI, including live-provider labels, coverage copy, next-integration copy, candidate API-source strings, and category API-priority blocks.
- [x] Update retained `ButlerReminders` language alerts to link `/translate` instead of the removed `/tools?category=translate` path.
- [x] Update Tools tests for the six-category provider, drawer interaction, Translate removal, and hidden provider metadata.
- [ ] Future: decide whether Tools provider status should remain internal-only or move to an admin/debug surface.

## v0.1.33 Addendum - Desktop Visual Layout Refresh

- [x] Create a lightweight approved visual-refresh spec in `docs/superpowers/specs/2026-06-30-visual-layout-refresh-design.md`.
- [x] Refresh the global Warm New Chinese visual system: warmer paper base, ink-line structure, restrained cinnabar accents, smaller serif headings, and compact UI text scale.
- [x] Tighten the app shell, logo, nav, page headers, card spacing, and one-page desktop workspace behavior.
- [x] Rebalance Chat: compact Live Trip Canvas title, two-row prompt chips, internal chat scroll, and separated composer / Save to Trips / status controls.
- [x] Give Trips, Explore, Tools, Translate, and Community more usable content area by reducing header/filter height and keeping long content inside internal scroll regions.
- [x] Normalize visible Translator and Community page labels, and clean Translator Text/OCR/Voice control copy while preserving the existing Qwen API routes.
- [x] Verify 1440x900 desktop pages for `/chat`, `/trips`, `/explore`, `/tools`, `/translate`, and `/community` with Playwright screenshots and body/main overflow metrics.
- [ ] Future: run a dedicated mobile portrait design pass; current v0.1.33 remains desktop-landscape first.

## v0.1.34-v0.1.41 Alignment Addendum - Completed Remote Iterations

- [x] v0.1.34: polished the desktop landscape frontend with Tools modal-card drawers, fixed Trips filter visibility, and changed Translator into a single-page Text/OCR/Voice/Phrases grid.
- [x] v0.1.35: completed the English-only UI pass and converted mock place names to bilingual `English (Chinese)` naming.
- [x] v0.1.36: completed the mobile layout overhaul with bottom six-tab navigation, scrollable mobile pages, and single-column Translator on phones.
- [x] v0.1.37: added the multi-language i18n system for EN/ES/AR/JA/KO/FR, including LanguageSwitcher persistence and Arabic RTL support.
- [x] v0.1.38: added the standalone `/` landing home page with hero CTA and six feature cards.
- [x] v0.1.39: replaced the background with a golden-line Chinese landscape and reduced overlay opacity.
- [x] v0.1.40: tightened the landing home page into a single-viewport desktop layout with compact hero and feature cards.
- [x] v0.1.41: fixed Save to Trips by adding Supabase auth-user synchronization migration `0003_fix_auth_user_sync.sql`, upserting `public.users` before trip insert, and making message append failures non-fatal.
- [x] Documentation alignment checkpoint: PLAN, HANDOFF, CHANGELOG, and VERSIONING now reflect v0.1.41 as the current source of truth.

## v0.1.42 Addendum - Unified Locale-Aware Translator

- [x] Replace the four-card Translator layout with one unified translator workspace.
- [x] Bind translation direction to the active website language and Chinese instead of hard-coding English/Chinese only.
- [x] Keep text, image OCR, voice STT, and TTS on Aliyun Bailian Qwen server-side routes.
- [x] Expose image input as two buttons: Upload Image and Take Photo; desktop shows Take Photo disabled because camera capture is mobile-only.
- [x] Reduce Voice translation to one Record button and remove upload-audio / public-audio-URL UI from the page.
- [x] Arrange source and output text as two equal panels across the top of the page.
- [x] Simplify common phrases and special terms into a horizontal support rail below the text panels.
- [x] Reduce heavy translucent panel styling so the background image remains visible and the page feels cleaner.
- [x] Replace Account avatar picker artwork with the six new panda PNG assets while preserving existing avatar IDs.
- [x] Lock `/translate` to a single desktop landscape viewport with internal overflow only.
- [x] Verify `/translate` at 1440x900 with Playwright metrics: body/html page scroll is zero and the translator surface uses hidden/internal overflow.

## v0.1.44 Addendum - Mobile Portrait Optimization

- [x] Move the 6-tab navigation to a fixed bottom bar on mobile; header shows only brand + lang + account.
- [x] Convert day detail drawer from a right-side narrow panel to a full-width bottom sheet on mobile.
- [x] Constrain account popover to viewport width on mobile.
- [x] Make explore city filter pills scroll horizontally on mobile.
- [x] Stack explore POI columns to a single column on mobile.
- [x] Stack trip detail header vertically on mobile.
- [x] Left-align trip summary actions on mobile.
- [x] Add shell bottom padding to prevent content hiding behind fixed bottom nav.
- [x] Enlarge tool card touch targets on mobile.

## v0.1.43 Addendum - Translator and Trip Detail Repair

- [x] Fix text translation availability by adding a DeepSeek fallback behind `/api/translate/text` when Qwen/DashScope is unavailable.
- [x] Improve Translator failure copy so missing translation providers are described clearly instead of appearing as a generic broken state.
- [x] Change Trip Canvas day-card CTA from `Edit` to `View details`.
- [x] Convert the day detail drawer from an editable form to a read-only itinerary detail surface.
- [x] Move real Trip Detail page status/actions/share controls into the Live Trip Canvas summary card when a saved canvas exists, reducing top-of-page chrome and prioritizing itinerary content.
- [x] Add targeted tests for translation fallback, read-only day details, and compact Trip Detail controls.

Next product iteration:

- [ ] Continue product planning from VPCC backlog after reviewing the deployed v0.1.43 repair iteration.

## v0.1.45 Addendum - Intelligent Chat Pipeline & Data-Fusion Roadmap (Docs Only)

- [x] Distill three research threads (Amap/Meituan data enrichment, Chat×Explore×Trips fusion, chat-efficiency/UX overhaul) into one coherent roadmap.
- [x] Add 阶段十二 to PLAN with tasks 12.1–12.33 across seven planned iterations (v0.1.46–v0.1.52).
- [x] Sharpen product positioning in PRD around the five traveler fears; add the Chat Intelligence pipeline, intent taxonomy, response-normalization schema, preference-profile requirements, UX audit, and navigation restructure.
- [x] Record data-fusion architecture in DESIGN (intent routing, preference profile, tool-calling Butler, rich data model, two-key Amap split, nav restructure) as ADR-037 through ADR-042.
- [x] Add AGENTS rules for implementing the roadmap without breaking provider abstraction, server-side keys, or mock fallback.
- [x] Record the Dianping/Meituan API application guide and timeline in HANDOFF.
- [x] No code changes this iteration; all tasks 12.1–12.33 remain planned/unimplemented.

Historical sequencing note (superseded by the v0.1.52 blueprint):

- [x] Chat Intelligence / multi-model orchestration shipped in v0.1.47-v0.1.48.
- [x] Amap enrichment, bounded Chat tool context, and lightweight preference memory shipped in v0.1.49-v0.1.51.
- [x] v0.1.52 was reassigned to the product interaction blueprint. After the v0.1.54 code pass, the next implementation order is now the v0.1.55-v0.1.61 sequence above.
- [ ] Dianping/Meituan + map remains a later external-approval track, not the v0.1.52 implementation target.

Immediate no-code actions the user should take this week:

- [ ] Apply for 大众点评开放平台 developer access now (2-week review queue).
- [ ] Register an Amap JS Maps key (`NEXT_PUBLIC_AMAP_MAPS_KEY`, domain whitelist `go2china.space`) — 5-minute self-serve.
- [ ] Confirm the DeepSeek plan includes function calling for the tool-calling Butler (v0.1.49).

## v0.1.46 Addendum - Product Expansion Roadmap (Docs Only)

- [x] Write the authoritative deep-dive `docs/planning/v0.1.46-product-expansion.md` covering all seven requested tracks.
- [x] Establish the quality-over-cost principle (token cost is not a constraint) and reframe the intent classifier's purpose from cost-saving to quality/correctness routing.
- [x] Plan multi-model Chinese LLM orchestration (DeepSeek/Qwen/GLM/Kimi/ERNIE) with routing, ensemble+judge, and refine-verify (阶段十三, ADR-044).
- [x] Plan native iOS/Android apps via React Native + Expo, incl. offline-first and China 备案/软著 distribution track (阶段十四, ADR-045). Plan only.
- [x] Plan the Tools functional upgrade turning six static tools interactive (阶段十五, ADR-046).
- [x] Plan the professional Account center + progressive-profiling lead capture with a prioritized field list (阶段十六, ADR-047).
- [x] Plan the admin backend with LLM `CustomerBrief` distillation and the `0004_leads_and_admin.sql` schema (阶段十七, ADR-048).
- [x] Plan the frontend/visual-optimization track: tokens, component library, motion, a11y, performance, brand (阶段十八, ADR-049).
- [x] Record the recommended build sequence and parallelizable lead-time work.
- [x] No code changes this iteration; all 阶段十三–十八 tasks remain planned/unimplemented.

Immediate no-code actions the user should take (long lead times):

- [ ] Register developer accounts / API keys for the additional Chinese LLMs to trial (Zhipu, Moonshot, Baidu ERNIE, MiniMax) — server-side keys only.
- [ ] If native apps are confirmed: enroll Apple Developer ($99/yr) + Google Play Console ($25), and start China 备案 (ICP) + 软著 (software copyright) — these have multi-week/month lead times.
- [ ] Decide native stack: React Native + Expo (recommended, reuses TS core) vs. fully native Swift/Kotlin.

## v0.1.47 Addendum - Multi-LLM Butler Orchestrator (阶段十三, first code iteration)

- [x] Built the provider-agnostic multi-LLM orchestrator: `ChatCompletionProvider` interface + OpenAI-compatible provider + six-provider registry + intent classifier + orchestrator with routing, high-stakes ensemble, and mock fallback.
- [x] Wired `/api/chat` to the orchestrator; `ButlerWorkspace` shows the real model label; documented 16 new server-side env keys.
- [x] Produced `docs/planning/mock-inventory.md` — 22 mock/placeholder/local-sim items with real-replacement plans and target phases (the "fill in skipped steps" request). Principle: real primary, mock stays as graceful fallback.
- [x] Added `CLAUDE.md` project memory (VPCC on explicit request only; every iteration updates all md docs + pushes; beginner tutorials for manual steps) and annotated `.claude/commands/vpcc.md`.
- [x] Tests: +16 (orchestrator/intentClassifier/modelRegistry); full suite 95 pass; build succeeds.

How it behaves without keys (important):

- With zero LLM keys configured, `/api/chat` returns the mock Butler exactly as before — no regression. Each provider key the user adds (server-side) automatically upgrades the Butler; no code change needed. This is the 🟡→🟢 transition described in `docs/planning/mock-inventory.md`.

Next code iterations (recommended order):
 
- [ ] Chat Intelligence Layer (阶段十二 v0.1.46-plan): response-normalization schema `{headline,body,highlights,watchOut,nextStep}`, then wire the refine-verify loop (task 13.5) on top of it.
- [ ] Tools functional upgrade (阶段十五) — high immediate traveler value, self-contained.
- [ ] Amap POI enrichment (阶段十二 v0.1.48) — unlocks rich Explore/Chat cards with no new approval.

## v0.1.53 Addendum - Strategic Documentation Pass (Docs Only)

- [x] Brainstormed and documented detailed requirements for the "China Travel Operating System" strategy.
- [x] Defined Offline-First Travel Vault (caching `TripState`, phrasebooks, contacts, and switching to "Offline Desk" layout when disconnected).
- [x] Outlined Cultural Context Interpreter (AI Butler grounding in China digital rules, booking warnings, and payment explanations).
- [x] Defined Intelligent Payment Card Routing (credit card setup wizard for Alipay/WeChat Pay transaction fees, thresholds, and ATMs).
- [x] Documented Contextual Tool Promotion rules (floating Shanghai-specific tools like Metro/Alipay and menu OCR translation when the traveler is in Shanghai).
- [x] Defined Bilingual Export & Print Kit ( taxi cards, bilingual addresses, print-ready PDF/PNG trip summary maps).
- [x] Recorded architectural decisions in DESIGN.md (ADR-060 to ADR-063).
- [x] Created the custom VPGA Skill in `.agents/skills/VPGA/` for automatic synchronization and iteration summary.
- [x] Updated PRD.md, PLAN.md, AGENTS.md, and HANDOFF.md.
- [x] No code changes this iteration; all tasks remain strategic and planned for subsequent implementation.

Next implementation tasks:
- [x] v0.1.54 Code implementation: home archetype starts, chat first-run empty state, primary `nextStep` action chips, traveler-facing status wording.

## v0.1.54 Addendum - Interaction Shell I Code Implementation

- [x] Added shared `TRIP_ARCHETYPES` configuration for First China 10 Days Essentials, Foodie China, and History & Nature.
- [x] Updated Home to surface the three FIT archetype entry points and route each one to `/chat?archetype=<id>`.
- [x] Updated Chat workspace to detect `?archetype=`, clear the URL, and send the corresponding prompt through the existing Butler pipeline.
- [x] Replaced first-run Chat suggestions with exactly three no-typing starter chips.
- [x] Promoted structured Butler `nextStep` into a primary action card in Chat.
- [x] Updated Trip Canvas h1 to use the current trip title and mapped confidence labels to traveler-facing copy.
- [x] Added tests for Home archetype routing, Chat archetype auto-send, ChatPanel next-step action, and Canvas title/status wording.

Next implementation tasks:

- [ ] v0.1.55 Canvas Action Layer: trip completeness, Day quick actions, and prep blockers.
- [ ] v0.1.56 Inline Tool Cards or Tools Widgets, depending on whether the next priority is Chat anxiety resolution or standalone utility depth.

## v0.1.55 Addendum - UX Layout & Frontend Design Spec (docs only)

- [x] Added `docs/planning/ux-design-and-layout-spec.md`: the design/experience companion to the v0.1.52 interaction blueprint and v0.1.53 technical blueprint.
- [x] Macro: single-surface spatial model, information-architecture table, five-anxiety layout principle.
- [x] Micro: page-by-page layout + component interaction mechanics for Home, Chat command center + Canvas, Day detail, Explore, Tools, Translate FAB, Trips, Account, Admin.
- [x] Frontend design system: tokens, reusable component library, per-surface visual hierarchy, motion, mobile-first, accessibility/i18n.
- [x] Mapped each existing roadmap phase to its governing design section (design contract for implementation).
- [x] Synced onto origin/main (v0.1.54) before working — parallel session's work preserved, nothing overwritten.

Note: this iteration is the design contract for the already-planned code phases (Canvas Action Layer, Inline Tool Cards, Tools Widgets, etc.); it does not add new phases. Version numbering overlaps with the parallel session's roadmap — see the HANDOFF v0.1.55 parallel-session note.

## v0.2.1 Addendum - Version-Series Reset (operator directive)

- [x] Reset the version series from `0.1.x` to `0.2.x`. `v0.1.55` was the final `0.1.x`; `v0.2.1` is the new baseline; subsequent iterations increment `0.2.x`.
- [x] Updated `package.json`, `VERSIONING.md` (rule + release note), `CHANGELOG.md`, `HANDOFF.md`, and `CLAUDE.md` (project memory).
- [x] No product code, provider, or schema change — version metadata only. All prior roadmap phases (阶段一…十八 and the FIT v0.1.5x roadmap) remain valid; only their future version tags renumber onto the `0.2.x` line.

## v0.2.2 Addendum - Chat Core-Loop Fixes

- [x] Speed: orchestrator races providers in parallel (`Promise.any`) + 18s per-provider timeout + 6s bounded tool-context prefetch.
- [x] Sync: destination-aware mock fallback (city + day-count extraction → skeleton itinerary) + system prompt requires full `days` on itinerary change.
- [x] Auto-save every chat for signed-in users; removed the manual Save to Trips button; de-duplicated sign-in sync vs auto-save.
- [x] Tests updated (+ parallel-race, + destination-skeleton); 105 pass; build green.

Next three planned iterations:

- [ ] v0.2.3 Canvas Action Layer: completeness score + progress meter + Day quick-actions (structured intents) + prep blockers.
- [ ] v0.2.4 Inline Tool Cards + factual fast-path: `ask_factual` answered from static Tools data as inline chat cards (also faster); reminder/mark-done hooks.
- [ ] v0.2.5 Tools Interactive Widgets I: currency converter, visa checker, payment wizard via optional `interactive` ToolCategory descriptor (static fallback kept).

## v0.2.3 附录 —— 整体规划 + UI 优化路线(纯文档)

- [x] 新增 `docs/planning/v0.2.3-ui-optimization-roadmap.md`:宏观差距审计(G1–G10)、逐界面微观 UI 清单、设计系统迭代规划、后三轮执行承诺。
- [x] 项目记忆写入中文规则(思考/回答/汇报一律中文)。
- [x] 版本重排:代码三轮顺延为 v0.2.4 / v0.2.5 / v0.2.6。

后三轮任务队列(执行承诺):

- [ ] v0.2.4 Canvas 行动层:`lib/trips/completeness.ts` 六维评分 + ProgressMeter + Day 卡快捷动作(预制意图) + 出发准备区 + patch 动画。测试:评分纯函数断言、快捷动作意图断言。
- [ ] v0.2.5 对话体验:ask_factual <150ms 内联工具卡(静态数据,跳过 LLM) + MessageBlock 分块渲染 + 乐观 UI/骨架屏/安抚文案 + 「加入提醒/标记完成」回写完成度。测试:快通道时延与绕过断言、分块渲染快照。
- [ ] v0.2.6 设计系统 + Tools widget:token 层 + 首批组件库收编;汇率换算器/签证资格问答器/支付设置向导(`ToolCategory.interactive` 可选描述符,缺数据降级静态清单)。测试:换算断言、决策树多国用例、降级断言。


## v0.2.4 附录 —— UI/交互深化规格 + 交接提示词(纯文档)

- [x] 新增 `docs/planning/v0.2.4-interaction-deep-dive.md`(交互哲学五判据、Chat↔Canvas 联动可见性、组件级规格、动效参数总表、三轮吸收方案)。
- [x] 新增 `docs/planning/handoff-prompt-for-coding-agent.md`(自包含实现提示词,可交给任意 coding agent)。
- [x] **编号勘误**:v0.2.3 附录中的代码三轮 v0.2.4/5/6 先顺延为 v0.2.5/6/7;随后 v0.2.5 被本轮"规划融合 + readiness seed"占用,完整代码三轮最终顺延为 **v0.2.6/v0.2.7/v0.2.8**,任务内容不变并按深化规格扩充:
  - [ ] v0.2.6 Canvas 行动层+画布交互:完成度评分/进度条、Day 卡快捷动作(带天数)、出发准备区(alert.done)、**变更摘要卡 + diffTripState + patch 演出 + 撤销**。
  - [ ] v0.2.7 Chat 体验重塑+内联工具卡:MessageBlock 伪流式、composer 规格、等待叙事、ask_factual <150ms 快通道、实体 chip 双向悬停联动。
  - [ ] v0.2.8 设计系统收口+Tools 交互组件:token+动效工具类+组件库归一、三件套 widget、移动 Chat sheet。

## v0.2.5 附录 —— 规划融合 + FIT Travel Desk Readiness Seed

- [x] 安全读取并合并远端 `v0.2.1`–`v0.2.4` 更新,确认远端已经进入 `0.2.x` 版本线,且 `v0.2.4` 新增交互深化规格与 coding-agent handoff prompt。
- [x] 将本地未推送的视觉规划/实现 seed 融入 `0.2.x` 主线:Trip Canvas readiness 初版、summary/readiness/action rail、Chat first-run starter state、Home launcher polish、响应式兜底。
- [x] 将 readiness 明确标记为"派生展示/seed",不是完整 completion schema;完整六维评分、可点缺口、prep blockers、alert.done 仍属于 `v0.2.6`。
- [x] 修正全部后续建议路线:完整代码三轮为 `v0.2.6 Canvas 行动层` → `v0.2.7 Chat 体验+内联工具卡` → `v0.2.8 设计系统+Tools widgets`。
- [x] 更新实现交接提示词与交互深化文档,避免下一位 agent 误用旧的 `v0.2.5/v0.2.6/v0.2.7` 编号。

下一步推荐:

- [ ] `v0.2.6` Canvas 行动层+画布交互:在现有 readiness seed 上补全六维 completion 纯函数、Day 快捷动作、Change Digest、patch 演出、撤销、Before you fly 准备区。


## v0.2.6 附录 —— FlyAI(飞猪)Skill 研究 + 项目级开发工具接入(纯规划)

- [x] 逐字核实上游 `alibaba-flyai/flyai-skill`(SKILL.md、8 份 references、已发布 npm 包源码),新增 `docs/planning/flyai-skill-integration.md`(技术画像、架构现实判断、逐项功能映射表)。
- [x] 判断:flyai-cli 是开发者/Agent 工具(MCP streamable_http 瘦客户端,内嵌共享试用凭证 + 设备指纹反滥用),不是生产级第三方 API;生产集成需飞猪官方合作确认,原则与 Dianping/Meituan 一致。
- [x] 落地开发工具:vendor 上游 `skills/flyai/` 原文进 `.claude/skills/flyai/`(含 `LICENSE-NOTICE.md` 使用边界说明),不触碰任何生产代码。
- [x] `docs/planning/mock-inventory.md` 新增第 23 项(真实预订数据,🔴)。
- [x] **编号再次勘误**:本轮占用 v0.2.6,完整代码三轮顺延为 **v0.2.7(Canvas 行动层)/ v0.2.8(Chat 体验)/ v0.2.9(设计系统)**,任务内容不变,继续以 `docs/planning/v0.2.4-interaction-deep-dive.md` 为验收标准。

待办(下一位 coding agent 或本会话下一轮):

- [ ] 同步更新 `docs/planning/handoff-prompt-for-coding-agent.md` 里的版本号引用(目前写的是 v0.2.5/6/7,需改为 v0.2.7/8/9),避免交给外部 agent 时编号过期。
- [ ] `v0.2.7` Canvas 行动层+画布交互:在现有 readiness seed(`TripSummary` 派生展示)上补全完整六维 `completeness` 纯函数、Day 快捷动作(带天数)、`diffTripState` + Change Digest 摘要卡、patch 演出动画、撤销、Before you fly 准备区。


## v0.2.7 附录 —— Canvas 行动层(第一轮代码实现,已完成)

- [x] `lib/trips/completeness.ts`:六维完成度纯函数(route/stay/food/transport/payment/visa)。
- [x] `lib/canvas/diffTripState.ts`:day 级(added/revised/removed)+ alert 级 diff 纯函数。
- [x] `lib/canvas/quickActions.ts` + `DayCard.tsx`:Day 卡快捷动作(Lighten/Add food/Swap morning/Add rest),预制消息带天数+城市,走既有 AI 管道。
- [x] `components/canvas/ChangeDigestCard.tsx`:变更摘要卡,渲染在助手回复末尾,点击条目画布定位+高亮,无变化不渲染。
- [x] `lib/canvas/useReplayableAnimation.ts` + patch 演出 CSS:新卡淡入、revised 卡可重放金色脉冲、自动滚动到首个变更 Day。
- [x] 撤销(Undo):本地确定性回滚(非走 AI 管道,ADR-070,见 DESIGN.md/HANDOFF.md 详细论证)。
- [x] `components/canvas/PrepChecklist.tsx`(Before you fly):`ButlerAlert.done?` 可选字段,本地勾选更新完成度。
- [x] 修复架构缺陷:移除 `TripCanvas` 内冗余的 `editableTrip` 本地缓冲状态(v0.1.43 只读化后的历史遗留),直接渲染 `trip` prop,消除一处渲染时序竞态风险。
- [x] 新增 22 个测试(`completeness`/`diffTripState`/`quickActions`/`canvas-action-layer` 集成测试),更新 `canvas-components.test.tsx` 匹配六维模型;全部 122 测试通过,构建成功。

下一步:

- [x] `v0.2.8` Chat 体验重塑(改为按操作者提供的高保真设计稿实现,范围调整为下方附录所记)。
- [x] `v0.2.9` Chat factual fast-path + inline Tools cards(本轮完成,替代旧编号里的"设计系统收口"占位)。
- [x] `v0.2.10` Tools Widgets I:汇率换算器、签证资格问答、支付设置向导;`docs/planning/v0.2.4-interaction-deep-dive.md` 中尚未落地的完整 MessageBlock 伪流式、实体 chip 双向悬停联动可并入后续单独排期。

## v0.2.8 附录 —— Chat/Canvas 视觉重设计(按操作者设计稿实现,已完成)

- [x] 收到操作者高保真 Chat 页面设计稿(Canvas 左栏 + Chat 右栏),范围从原计划的"MessageBlock 伪流式+composer 规格"调整为"对齐设计稿的完整视觉重设计",在 v0.2.7 交互逻辑收尾验证通过后开始。
- [x] `TripSummary.tsx`:标题内联改名(铅笔图标,`onRenameTrip`)、状态徽标(进度条+下一步单元格)、一览 chip 行、动作行(Add day/Rebalance route 走 AI 管道,View map/Trip settings 诚实禁用)。既有 `.trip-summary__readiness` 勾选清单结构不变。
- [x] `DayCard.tsx`:Day 级完成度徽标(`calculateDayCompleteness`)、三段式 block 改为图标+照片/占位+可选 highlights 清单、快捷动作拆分为 `DAY_PRIMARY_ACTIONS`(常显)与 `DAY_SECONDARY_ACTIONS`("…" 溢出菜单)。
- [x] `ChatPanel.tsx`:头像化头部(History 真实链接/Pin 诚实禁用)、消息 byline(头像+时间戳+已读对勾)、结构化 highlights 高亮卡片、赞踩+复制反馈(均本地状态,复制用 `navigator.clipboard`)、可关闭 Next Step 卡、图标化 composer(Attach/Mic 诚实禁用,Enter 发送)、AI 免责声明。
- [x] `ButlerWorkspace.tsx`:新增 `handleRenameTrip`/`handleAddDay`/`handleRebalanceRoute` 三个本地/AI-管道处理函数;`ChatMessage.createdAt`(新增可选字段)驱动时间戳。
- [x] `app/globals.css`:新增约 350 行样式,排查并确认与两处并行会话遗留的层叠覆盖块(`v0.1.55 VISUAL POLISH`、`min-width:900px` 紧凑布局)无实质冲突;修复一处遗留选择器(`.day-card__head span { display:none }`)意外隐藏新徽标的真实回归。
- [x] 新增 8 个测试(内联改名/Add day/Rebalance route/禁用态/next-step 关闭/反馈切换/复制确认/Enter 发送),全部 134 测试通过,`npm run build` 成功;用 Playwright 对 `/chat` 页面截图核验桌面布局与交互态。

## v0.2.9 附录 —— Chat factual fast-path + inline Tools cards(已完成)

- [x] `AssistantResponse` 新增可选 `toolCards`,作为 Chat 内联工具卡的结构化载体;旧 provider / mock 回复不返回该字段也继续兼容。
- [x] 新增 `lib/tools/factualToolCards.ts`:基于现有 Tools 静态 provider 生成签证入境、支付、汇率、地铁、eSIM/VPN、应急六类事实卡,不新增外部 key,不依赖 FlyAI 生产集成。
- [x] `lib/ai/orchestrator.ts`:在调用 LLM provider 之前拦截高置信 `ask_factual` / emergency concern,命中后返回 `mode:"tools"`、`strategy:"tool"`,不调用任何 LLM;未命中则保持原有多模型/Mock fallback 链。
- [x] `ChatPanel.tsx`:结构化回复中渲染 inline tool cards,链接到对应 `/tools?category=...` 深链;复制回复时包含工具卡内容;busy 时显示可见 thinking 状态。
- [x] 新增测试:`factualToolCards.test.ts`、orchestrator 快通道绕过 provider 断言、ChatPanel 工具卡/等待态断言。

排除项:

- 不实现完整 MessageBlock 伪流式分阶段动画。
- 不实现实体 chip 双向悬停定位。
- 不新增 Tools interactive widgets、签证决策树、支付向导或汇率换算器。
- 不新增 Supabase schema 或任何生产级 FlyAI/预订接口。

下一步建议:

- [x] `v0.2.10` Tools Widgets I:在本轮 inline card 数据模型基础上,把 Currency converter、Visa checker、Payment setup wizard 做成真实可交互组件,并让 Chat tool cards 与 `/tools` 继续复用同一 Tools 数据源。
- [x] `v0.2.13` TripBlock POI Embedding + Day Detail Operational Upgrade:将中文地址、营业时间、电话、坐标、booking/map url 等可选运营字段写入 `TripBlock`,升级 Day detail 和 Taxi Driver card。
- [x] `v0.2.14` Real POI context write-through + booking candidate model:把 Chat liveToolContext / Amap rich POI 更稳定地写入生成的 TripBlock，并设计非交易型 booking candidate 字段。
- [x] `v0.2.15` Explore Add-to-Trip POI write-through:从 Explore 选择真实 POI 时将 POI id/map/source/booking candidate 放入 Chat draft,并稳定落入 TripBlock 或可见 Flexible 候选块。
- [ ] `v0.2.16` Explore candidate review / day-detail action polish:把 Flexible 候选块升级为更明确的 Needs scheduling 状态,并在 Day detail 强化 schedule/map/booking-info 动作分组。

## v0.2.10 附录 —— Tools Widgets I(已完成)

- [x] `lib/tools/types.ts`:新增可选 `ToolCategory.interactive` 描述符,覆盖 `currency-converter` / `visa-checker` / `payment-wizard`;未设置该字段的分类保持原静态清单渲染。
- [x] `lib/tools/staticProvider.ts`:为 Currency、Visa and entry、Payment setup 三类补充 widget metadata,同时保留 tips/sections/offlineTips 作为降级内容。
- [x] `components/tools/widgets/ToolWidget.tsx`:新增三件套交互组件:
  - RMB converter:优先解析 live rate section,否则使用离线估算 fallback rates。
  - Entry planning checker:国籍 + 停留天数 + transit-only 开关,输出保守规划建议。
  - Payment setup wizard:钱包 + 卡品牌选择,输出预出发设置步骤。
- [x] `ToolsBoard.tsx`:在工具弹窗 summary 下方渲染 widget,静态 tips/sections/offline notes 保持原位。
- [x] `app/globals.css`:新增 widget 纸面样式,延续现有 modal/card/hairline 视觉系统。
- [x] `tests/tools-board.test.tsx` 与 `tests/tools-provider.test.ts`:覆盖 descriptor 和三件套交互。

排除项:

- 不声称官方签证判定;所有结果都是 conservative planning guidance。
- 不接真实支付交易、不保存银行卡、不做 Alipay/WeChat OAuth。
- 不新增 transit planner、emergency generator、eSIM provider comparison。
- 不新增任何外部 key 或 Supabase schema。

下一步建议:

- [x] `v0.2.11` Frontend Design Resource Stack 配置:把操作者指定的前端/设计/Impeccable/图标/设计系统资源登记为仓库级工作流,不改运行时代码。
- [x] `v0.2.13` TripBlock POI Embedding + Day Detail Operational Upgrade:扩展 `TripBlock` 可选运营字段,把 POI 执行信息沉淀进 Day detail,并实现 Show Taxi Driver 卡。
- [x] `v0.2.14` Real POI context write-through + booking candidate model:将 Chat liveToolContext / Amap rich POI 稳定写入 TripBlock,并为后续酒店/门票/交通候选建立非交易型数据结构。
- [x] `v0.2.15` Explore Add-to-Trip POI write-through:从 Explore 选择真实 POI 时将 POI id/map/source/booking candidate 放入 Chat draft,并稳定落入 TripBlock 或可见 Flexible 候选块。
- [ ] `v0.2.16` Explore candidate review / day-detail action polish:把 Flexible 候选块升级为更明确的 Needs scheduling 状态,并在 Day detail 强化 schedule/map/booking-info 动作分组。

## v0.2.11 附录 —— Frontend Design Resource Stack 配置(纯文档)

- [x] 新增 `PRODUCT.md`,作为 Impeccable/设计 agent 可读取的产品上下文摘要,不替代 `PRD.md`。
- [x] 新增 `docs/planning/v0.2.11-frontend-design-resource-stack.md`,把操作者指定的 Frontend Design、UI design system、CSS animation、creative aesthetics、Awwwards landing、web design guidelines、Vercel React best practices、Superpowers、Impeccable、better-icons、UI Design Brain、DESIGNmd、awesome-design-md 等资源映射到 VisePanda 工作流。
- [x] 明确外部资源只作为设计评审/开发辅助,不能覆盖当前 `DESIGN.md`、`app/globals.css`、React 组件契约或产品能力边界。
- [x] 记录 `designmd.co` 为室内设计站点,不作为本项目前端设计系统来源。
- [x] 本轮不安装 npm 包、MCP server、插件或 CLI,不新增生产依赖、不改运行时代码。

下一步建议:

- [x] `v0.2.12` 文档/版本交接统一:所有当前状态文档指向 v0.2.12,明确 v0.2.11 为历史完成项,下一轮顺延为 v0.2.13。
- [x] `v0.2.13` TripBlock POI Embedding + Day Detail Operational Upgrade:扩展 `TripBlock` 可选运营字段,把 POI 执行信息沉淀进 Day detail,并实现 Show Taxi Driver 卡。

## v0.2.12 附录 —— 文档/版本交接统一(纯文档)

- [x] `VERSIONING.md` 与 `package.json` / `package-lock.json` 当前版本统一为 `0.2.12`。
- [x] `HANDOFF.md` 顶部当前状态统一为 v0.2.12,并新增本轮交接说明。
- [x] `CHANGELOG.md` 顶部新增 v0.2.12 release note。
- [x] `PLAN.md` / `PRD.md` / `DESIGN.md` / `AGENTS.md` 记录 v0.2.12 的接手规则: v0.2.11 已完成,下一轮代码为 v0.2.13。
- [x] `PRODUCT.md` 与 design resource planning 文档补充当前版本提示,防止设计工具接手时误读为活跃 v0.2.11 任务。

下一步建议:

- [x] `v0.2.13` TripBlock POI Embedding + Day Detail Operational Upgrade:已完成。
- [ ] 后续如操作者明确要求安装 Impeccable 或 better-icons,按官方安装路径单独执行,并把生成文件作为可审查 git diff。

## v0.2.13 附录 —— TripBlock POI + Day detail operational upgrade(已完成)

- [x] `TripBlock` 新增可选运营字段: `address` / `chineseAddress` / `phone` / `openingHours` / `mapUrl` / `bookingUrl` / `sourceLabel` / `coordinates`。
- [x] Butler prompt 与 legacy DeepSeek prompt 允许模型在有来源时保留这些字段。
- [x] `DayDetailDrawer` 在字段存在时显示 POI execution details、地图/预订信息链接、source、坐标和 Show taxi driver 卡。
- [x] mock/static fallback 为代表性景点补充中文地址、地图链接和坐标,无 API key 时也可验证。
- [x] `tests/canvas-components.test.tsx` 覆盖 Day detail POI 信息与司机卡。

排除项:

- 不做真实预订、购票、支付或库存判断。
- 不新增外部 API key、Supabase migration 或生产 FlyAI 调用。
- 不把 `bookingUrl` 表述为可交易能力,仅作为信息入口。

下一步建议:

- [x] `v0.2.14` Real POI context write-through + booking candidate model:将 Chat liveToolContext / Amap rich POI 稳定写入 TripBlock,并为后续酒店/门票/交通候选建立非交易型数据结构。

## v0.2.14 附录 —— Real POI write-through + booking candidate model(已完成)

- [x] 新增 `BookingCandidate` 非交易型模型,挂载到 `TripBlock.bookingCandidates?`。
- [x] `ButlerToolPoi` 扩展 Amap id、phone、mapUrl、coordinates、bookingCandidates。
- [x] 新增 `lib/ai/toolContextWriteThrough.ts`,在 provider patch 解析后把匹配 POI 的安全字段确定性写回 TripBlock。
- [x] `DayDetailDrawer` 显示 booking candidates,文案明确为 Info only。
- [x] 新增 `tests/toolContextWriteThrough.test.ts`,并扩展 Day detail 测试。

排除项:

- 不做 checkout、库存、支付、退款、订单管理。
- 不新增 Supabase schema、外部 API key 或生产 FlyAI 调用。

下一步建议:

- [x] `v0.2.15` Explore Add-to-Trip POI write-through:已完成。
- [ ] `v0.2.16` Explore candidate review / day-detail action polish:把 Flexible 候选块升级为更明确的 Needs scheduling 状态,并在 Day detail 强化 schedule/map/booking-info 动作分组。

## v0.2.15 附录 —— Explore Add-to-Trip POI write-through(已完成)

- [x] 新增 `lib/explore/addToTrip.ts`,统一构建/解析 Explore POI payload,并复用 `BookingCandidate` 的 info-only planning reference 边界。
- [x] `ExploreBoard` 的 Add to Trip 同时传递 traveler-facing Chat draft 和结构化 `poi` payload。
- [x] `ButlerWorkspace` 在 `?add=&poi=` 首跑时解析 payload,并在 AI/mock patch 应用前执行确定性写入。
- [x] 若 POI 名称匹配现有 TripBlock,只补齐缺失的 map/source/phone/hours/coordinates/booking candidates;若不匹配,追加目标城市 day 的 Flexible candidate block。
- [x] `DayCard` 与 `DayDetailDrawer` 显示 Flexible block,避免候选 POI 写入状态但用户看不到。
- [x] 新增/更新测试覆盖 payload 构建、Explore URL、Chat auto-apply 和 Canvas 可见化。

排除项:

- 不做 checkout、库存、支付、退款、订单管理。
- 不新增 Supabase schema、外部 API key 或生产 FlyAI 调用。
- Explore 静态 fallback 没有真实地址时不伪造地址,只提供搜索/地图入口和来源。
