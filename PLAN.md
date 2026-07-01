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

### v0.1.48：高德 POI 数据丰富化（无需新审批）

- [ ] 任务 12.11：升级 `/api/explore/amap` 使用 `extensions=all`，捕获高德已返回但当前被丢弃的字段：`rating` / `cost` / `tel` / `opentime_week` / `photos` / `business_area`。
- [ ] 任务 12.12：`AmapPoi` 接口补齐上述字段；`lib/explore/types.ts` 新增全可选的 `ExploreRichMeta`（rating / reviewCount / pricePerPerson / priceLevel / tel / openHours / photoUrl / bookingUrl / sourceLabel），`ExploreAttraction`/`ExploreFoodSpot`/`ExploreStay` 继承之。
- [ ] 任务 12.13：`amapProvider.ts` 的 mapper 填充 rich 字段；静态 provider 保持字段为 undefined（graceful degradation）。
- [ ] 任务 12.14：Explore 卡片 UI 条件渲染评分（★ 4.7，`--gold`）、价格档（¥/¥¥/¥¥¥）、营业时间、缩略图；移动端卡片改为左图右文横向布局。

### v0.1.49：工具调用 Butler（真实 POI 数据进对话）

- [ ] 任务 12.15：将 `/api/chat/route.ts` 升级为**工具执行循环**（最多 3 轮）：DeepSeek 返回 `tool_calls` → 服务端执行真实 Amap/Dianping 调用 → 把结果回灌 → 模型再基于真实数据生成行程。
- [ ] 任务 12.16：定义工具 schema：`search_pois`（city/category/keyword/priceLevel）、`get_poi_detail`（poiId/city）、`search_dianping`（先做 stub，待审批）。
- [ ] 任务 12.17：扩展 `TripBlock`（向后兼容，全可选）：`poiId` / `sourceLabel` / `rating` / `priceEstimate` / `openHours` / `phone` / `photoUrl` / `mapUrl` / `bookingUrl` / `location{lat,lng}`。
- [ ] 任务 12.18：在 Chat 对话流内联渲染真实 POI 卡片（评分/价格/照片），卡片带「Add to Day N」按钮，直接进既有 AI pipeline。
- [ ] 任务 12.19：确认 DeepSeek 计划支持 function calling（`deepseek-chat` 支持；若 V4 Flash 不支持，简单调整用 Flash、工具循环用 V3）。

### v0.1.50：引导入口 + 画布快捷操作

- [ ] 任务 12.20：Home 增加 3 个原型入口（「首次中国 10 天精华」/「美食三城」/「历史+自然」），点击后带 `?archetype=` 参数预置 trip state，并由 Butler 以「建议」口吻呈现。
- [ ] 任务 12.21：首跑向导——3 个 chip 问题（无需打字）即可生成起始草稿；替换空白 textarea 空状态。
- [ ] 任务 12.22：画布进度指示（「你的行程已成型 40%」），基于 TripState 完整度（城市/日期/酒店区/美食/交通/行前清单）打分。
- [ ] 任务 12.23：Day 卡片内联快捷按钮（Lighten / Swap morning / Add food），每个按钮发送预制 Butler 意图，用户无需打字。
- [ ] 任务 12.24：把 `confidence`（Draft/Refined/Ready to save）映射为友好文案（「Taking shape / Looking good / Save it!」）；行程有标题后用标题替换「Live Trip Canvas」h1。

### v0.1.51：导航重构（两种模式，而非六个 tab）

- [ ] 任务 12.25：Translate 从主导航移除，改为全局悬浮相机/麦克风按钮（底部右侧常驻），点开是已激活 OCR 的 bottom sheet，不离开当前上下文。
- [ ] 任务 12.26：Explore 从主导航降级，POI 浏览通过 Chat 内「Browse [City]」按钮进入；Explore 页面保留为可浏览的发现面，但不再是一级 tab。
- [ ] 任务 12.27：主导航精简为 Chat · Trips · Tools · Community（4 tab）。
- [ ] 任务 12.28：Chat 对话内联渲染工具卡（签证提醒、支付清单在对话中直接出现），复用 Tools modal 的组件。
- [ ] 任务 12.29：已登录用户从 `/` 直接进 `/chat`（Home 仅作落地页）。

### v0.1.52：大众点评/美团 + 地图（审批后）

- [ ] 任务 12.30：`search_dianping` 接真实大众点评 POI 搜索（评分/评论数/人均），替换 stub。
- [ ] 任务 12.31：酒店预订 deeplink 进美团/携程 App；餐厅卡展示真实人均消费与评论数。
- [ ] 任务 12.32：注册 `NEXT_PUBLIC_AMAP_MAPS_KEY`（仅用于地图显示、域名白名单、可公开）；在 Day 抽屉/Canvas 加 `DayMapWidget`，对含 2+ 定位块的天渲染标记 + 路线。
- [ ] 任务 12.33：Chat→Explore 反向联动——行程内 POI 在 Explore 城市视图显示「In your trip」徽标；偏好驱动 Explore 卡片排序。

### 阶段十二关键设计取舍（详见 DESIGN.md ADR-037~042）

- 为什么用工具调用而非 RAG：RAG 预嵌入静态知识；工具调用在规划时查实时数据，知道某餐厅当天是否营业、某景点是否需提前购票。
- 为什么保留 static/mock provider：既有安全约束——真实数据源不可用/未审批时对话仍可用，只是退化为无真实 POID 的纯文本行程。
- 为什么不流式工具调用：`response_format: json_object` 一次性输出，解析更稳；流式可后续仅对 `assistantMessage` 字段加。
- 为什么高德要两把 key：POI 搜索 REST key（`AMAP_API_KEY`）永远服务端、按调用计费、绝不进浏览器；地图显示 JS key（`NEXT_PUBLIC_AMAP_MAPS_KEY`）按 Referer 白名单、可公开、无计费风险。

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

Sequencing rationale (why this order):

- [ ] Chat Intelligence (v0.1.46) ships first because it improves every message for zero new API dependencies and reduces cost/latency immediately.
- [ ] Preference Profile (v0.1.47) is next because tool-calling and onboarding both consume the profile.
- [ ] Amap Enrichment (v0.1.48) precedes Tool-Calling Butler (v0.1.49) because the rich data model and mappers are prerequisites for feeding real POI data into the canvas.
- [ ] Onboarding + Quick-Actions (v0.1.50) and Nav Restructure (v0.1.51) are UX-surface iterations that build on the intelligence + data layers.
- [ ] Dianping/Meituan + Map (v0.1.52) is last because it depends on external API approval (multi-week queue — start the application during v0.1.46).

Immediate no-code actions the user should take this week:

- [ ] Apply for 大众点评开放平台 developer access now (2-week review queue).
- [ ] Register an Amap JS Maps key (`NEXT_PUBLIC_AMAP_MAPS_KEY`, domain whitelist `go2china.space`) — 5-minute self-serve.
- [ ] Confirm the DeepSeek plan includes function calling for the tool-calling Butler (v0.1.49).
