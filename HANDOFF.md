# VisePanda — 交接文档

## 当前状态

- 完成阶段：阶段一 AI Butler Chat MVP 骨架；阶段二真实 AI provider + Supabase 登录 + guest draft 自动迁移已接入；阶段三 Trips 已接入真实 Supabase persistence 首个闭环，加入了 trip detail 页面、归档/分享链接流程和状态说明系统（任务 3.6）；阶段四 Explore 已升级为 Amap 实时 POI 驱动（景点/美食/住宿），完成 provider abstraction、Add to Trip、route rebalance 文案和 provider readiness metadata（任务 4.1-4.5、7.1-7.2、9.2）；阶段五 Tools 已从占位页升级为静态 provider 驱动的 7 个分类骨架，支持分类深链、结构化内容、离线 pocket notes、API priority、provider readiness metadata，以及实时 ExchangeRate-API 汇率接入（任务 5.1-5.3、7.3-7.4、9.1）；阶段六目的地感知水墨背景切换已完成第一版（任务 6.1-6.4）；阶段八 Canvas ButlerReminders 深链 Tools 分类已完成（任务 8.1）；Account 已从独立页面改为头部图标 + 悬浮窗口，登录方式从 magic link 改为邮箱密码 + Google OAuth，登录后支持改名/改密码/登出（任务 2.5）；阶段十翻译页面已全部实现（任务 10.1-10.4），含文字翻译、OCR 扫描翻译、短语词典，ButlerReminders 已从 TripCanvas 移除（v0.1.28）。
- 当前分支：`main`
- 当前版本：`v0.1.32`
- 重要（已完成）：
  - `supabase/migrations/0002_trip_archive_and_share.sql`：用户已手动在 Supabase SQL Editor 执行，归档/分享 RLS policy 已生效。
  - Google OAuth：用户已在 Google Cloud 创建 OAuth 凭据并在 Supabase Authentication → Providers → Google 填入，Google 登录功能已配置就绪。
  - ExchangeRate-API：用户已获取 API Key，已配置 `EXCHANGE_RATE_API_KEY` 到 Vercel 环境变量；`/api/exchange-rate` 路由已连接，Tools Currency 分类展示实时 CNY 汇率（每小时刷新）。
  - 高德地图 API：用户已获取 Web Service API Key，已配置 `AMAP_API_KEY` 到 Vercel 环境变量；`/api/explore/amap` 路由已连接，Explore 景点/美食/住宿来自高德 POI 实时搜索。
- 最新实现 commit：本轮提交后以 `git log -1 --oneline` 为准
- 当前远端：`https://github.com/JTCAO515/VP-Codex-Final.git`
- 部署地址：`https://go2china.space`

## 已完成的功能

- Next.js + React + TypeScript 项目骨架 ✅
- Vercel-ready route/API 结构 ✅
- Warm New Chinese 水墨背景视觉系统 ✅
- Chat / AI Butler 主工作台 ✅
- 左侧 Live Trip Canvas ✅
- 右侧持续聊天面板 ✅
- 无默认演示对话的真实 Chat 初始态 ✅
- 两列建议问题 + 每轮 2 个上下文 follow-up ✅
- Day-by-day summary cards ✅
- Day 时间线 + Morning / Afternoon / Evening 三段卡 ✅
- 可编辑每日详情抽屉 ✅
- 桌面横屏一屏固定工作台 ✅
- Canvas 顶部五张任务/提醒卡已移除 ✅
- DeepSeek V4 Flash provider ✅
- mock AI fallback ✅
- canvas patch reducer ✅
- Trips Dashboard 骨架 ✅
- Trips mock trip cards、状态筛选、概览指标、Continue in Chat ✅
- Explore / Tools / Account 占位页 ✅
- `/api/chat` DeepSeek + fallback route ✅
- `/api/trips`、`/api/explore`、`/api/tools` placeholder routes ✅
- Vitest 单元/组件/API 测试 ✅
- Playwright 桌面/移动烟测 ✅
- Supabase schema 设计：`users`、`trips`、`canvas_versions`、`messages` 表 + RLS policies ✅
- Supabase 登录（最初为 magic link，`v0.1.16` 改为邮箱密码 + Google OAuth，guest 模式始终可用）✅
- Chat 工作台 Save to Trips：保存当前 canvas 到 `trips` + `canvas_versions`，并同步聊天记录到 `messages` ✅
- Trips Dashboard：已登录且配置 Supabase 时读取真实行程列表；未登录/未配置时回落到 mock 行程 ✅
- Trips 状态说明（任务 3.6）：`TripsDashboard` 和 `TripDetail` 显示 draft / ready / shared / archived 的含义和下一步操作，状态文案集中在 `lib/trips/mockTrips.ts` ✅
- 从 Trips 的 Continue in Chat 恢复真实保存的 canvas 到 Chat 工作台（`/chat?trip=<id>`） ✅
- Guest draft 自动持久化到 `localStorage` 并在刷新后还原；用户登录后自动同步草稿到 Supabase（任务 2.3） ✅
- Trip detail 页面（`/trips/[id]`）：已登录显示真实 Live Trip Canvas，未登录/示例行程显示摘要卡，未知 id 显示 not found（任务 3.4） ✅
- 归档与分享链接（任务 3.5）：Trip detail 页面支持 Mark as Ready / Archive / Restore from archive 状态切换；支持 Get share link 生成 token 并展示完整 URL，支持 Revoke share link 撤销；新增公开只读分享页 `/share/[token]`（`components/share/ShareView.tsx`），未登录访客可查看分享行程的 Canvas，看不到聊天记录 ✅
- Explore provider abstraction 与静态骨架（任务 4.1-4.3）：新增 `lib/explore/types.ts` 定义 `ExploreProvider` 接口和城市/景点/美食/住宿类型；`lib/explore/staticProvider.ts` 提供覆盖北京/上海/成都/西安/广州/杭州/苏州/重庆的静态数据；`lib/explore/index.ts` 的 `getExploreProvider()` 是组件唯一允许调用的入口；`components/explore/ExploreBoard.tsx` 支持城市筛选按钮切换和景点/美食/住宿三列展示 ✅
- Explore provider readiness（任务 7.1、7.2）：`ExploreProvider` 新增 `getProviderStatus()`，静态 provider 返回当前 static mode、8 城覆盖范围、Amap/Trip.com/Meituan/Tripadvisor 候选 provider、限制和下一步 POI/place-detail 验证重点；`ExploreBoard` 渲染该状态，不在组件里硬编码候选 provider 文案 ✅
- Account 图标 + 悬浮窗口重做（任务 2.5）：删除独立 `/account` 页面和 `AccountPanel.tsx`；新增 `components/account/AccountMenu.tsx`，渲染在 `AppShell` 头部、`NavTabs` 旁边；登录方式从 magic link 改为邮箱密码登录/注册（`signInWithPassword`/`signUpWithPassword`）和 Google 登录（`signInWithGoogle`）；已登录状态下悬浮窗口提供 Change name（`updateDisplayName`）、Change password（`updatePassword`）、Log out 三个操作 ✅
- Explore Add to Trip 流程（任务 4.4、4.5）：`ExploreBoard` 的每个景点/美食/住宿条目新增 Add to Trip 按钮，点击后跳转到 `/chat?add=<编码后的草稿消息>`；草稿消息明确要求 VisePanda 把该地点加入行程并重新平衡路线；`ButlerWorkspace` 挂载时读取 `add` 参数，清空 URL 后调用既有的 `handleSend`，新内容和用户手动发消息一样经过 `/api/chat` → `CanvasPatch` → `applyCanvasPatch`，没有新增任何绕开 AI pipeline 的画布写入入口 ✅
- Tools 静态骨架（任务 5.1-5.3）：新增 `lib/tools/types.ts`（`ToolsProvider` 接口、`ToolCategory` 类型）、`lib/tools/staticProvider.ts`（签证入境/支付设置/翻译/汇率/地铁/eSIM-VPN/应急 7 个分类的静态参考清单、结构化 sections、离线 pocket notes、API priority）、`lib/tools/index.ts`（`getToolsProvider()` 工厂）；`components/tools/ToolsBoard.tsx` 替换占位页，左侧分类列表 + 右侧摘要、建议清单、出发前清单、离线提示和 API 优先级；Currency/Translate 文案明确说明实时汇率/翻译尚未接入 ✅
- Tools provider readiness（任务 7.3、7.4）：`ToolsProvider` 新增 `getProviderStatus()`，静态 provider 返回当前 static mode、7 类覆盖范围、Exchange-rate/Machine translation/Visa rules/Transit data 候选数据源、限制和下一步实时汇率验证重点；`ToolsBoard` 渲染该状态，与分类级 `apiPriority` 形成总览 + 详情 ✅
- Tools 分类深链（任务 5.2）：`components/tools/ToolsBoard.tsx` 会读取 `/tools?category=<tool-category-id>` 并自动选中匹配分类，无效参数回退默认分类；点击分类时会用 `history.replaceState` 更新地址栏，便于从 Chat/Canvas/未来提醒入口复用深链 ✅
- 顶部导航图标：`components/shell/NavTabs.tsx` 使用 `lucide-react` 为 Chat / Trips / Explore / Tools 渲染线性图标，替换 C/T/E/X 字母占位 ✅
- 桌面端密度优化：Chat / Trips / Explore / Tools 标题、摘要区、筛选区、卡片间距已压缩；桌面端页面本身保持一屏锁定，长内容在 `.trip-canvas__days`、`.trip-library`、`.explore-board__columns`、`.tools-category-detail` 内部滚动 ✅
- 目的地感知背景切换（任务 6.1-6.4）：新增 `lib/visual/destinationBackground.ts`，`TripCanvas` 根据当前 `summary.destinations` 把 Beijing / Shanghai / Hangzhou / Suzhou / Chongqing 映射到不同水墨背景氛围；CSS 通过 `body[data-destination-scene]` 使用同一张 `ink-landscape.png` 叠加不同场景色层，未知目的地回落默认水墨背景 ✅
- ButlerReminders 深链（任务 8.1，v0.1.26）：新增 `components/canvas/ButlerReminders.tsx`，渲染在 `TripCanvas` 行程时间线下方（不在顶部）；`alertToolCategoryMap` 把 `visa`→`visa-and-entry`、`payment`→`payment-setup`、`language`→`translate`、`transport`→`metro`、`risk`/`emergency`→`emergency`，有映射的 alert 渲染为 `<a href="/tools?category=<id>">` 链接，`booking`/`weather` 等无对应分类的渲染为纯文本；空 alerts 列表不渲染任何内容 ✅
- 社区页面框架（Phase 11 初步实现，v0.1.29）：新增 `/community` 作为第六个主导航 Tab（Globe 图标）；`CommunityBoard` 三 Tab 布局（动态 Feed / 热门 Hot Spots / 照片 Photos）；`CommunityFeed` 展示 6 条 mock 行程/攻略帖，含作者信息、城市标签、摘要、hashtag、点赞/评论数；`CommunityHotSpots` 按 5 个城市（北京/上海/成都/西安/杭州）+类别（景点/美食/宝藏）筛选展示 12 条社区精选，含星级、点评数、旅行者贴士、Add to Trip 按钮（路由到 `/chat?add=…` 走既有 AI pipeline）；`CommunityPhotos` 展示 8 张 mock 照片卡，含标题、地点、点赞；所有数据来自 `lib/community/mockData.ts` 静态数据，`lib/community/types.ts` 定义类型；Supabase/API 接入留待 Phase 11 后续迭代 ✅
- 翻译页面（任务 10.1-10.4，v0.1.28）：新增 `/translate` 作为第五个主导航 Tab（Languages 图标）；`TranslatorPage` 三 Tab 布局（文字翻译 / 扫描翻译 / 短语词典）；`TextTranslator` 支持 EN↔ZH 双向文字翻译（DeepSeek via `/api/translate/text`，返回翻译+拼音）、Web Speech API TTS（zh-CN rate 0.85）、剪贴板复制、Ctrl+Enter 快捷键；`OcrTranslator` 支持拖放/相机拍照/文件上传图片，Canvas API 本地缩放至 1200px 后调用 `/api/translate/ocr`（OCR.space，默认免费 key，optional `OCR_SPACE_API_KEY`），识别结果自动送入翻译接口并提供 TTS；`PhraseBook` 包含 44 条常用短语（6 分类：问候/餐饮/交通/购物/应急/酒店）和 28 条特殊词语（3 分类：景点/菜名/标识），每条带 TTS 按钮；`lib/translate/types.ts` 定义 `Phrase`/`SpecialTerm` 类型；`lib/translate/phrases.ts` 提供全量静态数据；`ToolCategory` 新增可选 `cta?: { label; href }` 字段，Translate 分类注入 CTA 链接到 `/translate`，`ToolsBoard` 渲染时展示该链接；`ButlerReminders` 从 `TripCanvas` 移除（组件文件保留），Canvas 测试已同步更新 ✅

## 未完成/待办

- [x] 真实 Supabase 项目已创建（用户已完成部署），`0002_trip_archive_and_share.sql` 已在 Supabase SQL Editor 手动执行，归档/分享 RLS policy 已生效。
- [x] Google OAuth：已在 Supabase Authentication → Providers → Google 配置完毕。
- [x] ExchangeRate-API 接入：`EXCHANGE_RATE_API_KEY` 已配置到 Vercel，`/api/exchange-rate` 路由已连接，Tools Currency 分类展示实时 CNY 汇率。
- [x] Amap POI API 接入：`AMAP_API_KEY` 已配置到 Vercel，`/api/explore/amap` 路由已连接，Explore 景点/美食/住宿来自高德实时搜索，未配置时回落静态数据。
- [x] 翻译页面：`/translate` 已实现，含文字翻译、OCR 扫描、短语词典（v0.1.28）。
- [x] 社区页面框架：`/community` 已实现，含 Feed/Hot Spots/Photos 三 Tab，静态 mock 数据（v0.1.29）。
- [ ] 后续 Tools 真实数据源：签证规则查询 API、地铁路线 API。
- [ ] 社区页面真实数据接入（Phase 11）：Supabase posts/photos/likes 表、Supabase Storage 照片上传、高德/美团 API 联动热榜。
- [ ] 目的地背景切换当前是 CSS 氛围层；后续如需要更惊艳，可生成/接入城市级真实水墨背景资产。
- [ ] 移动端竖屏端细节适配。

## 已知问题

- DeepSeek 输出虽要求 JSON，但真实模型仍可能返回无效 patch 或无效 suggestions；当前会自动回落/规范化。
- 用户提供的 DeepSeek key 已出现在聊天上下文中，验证完成后建议在 DeepSeek 后台轮换。
- npm audit 当前可能报告若干依赖安全提示；尚未使用 `npm audit fix --force`，避免破坏 Next/React 版本组合。
- Trips 在未登录或未配置 Supabase 时仍为 mock data；登录且配置后才会显示真实保存的行程。
- Day 抽屉编辑仍是本地状态；只有点击 Save to Trips 才会把当时的 canvas 整体快照写入 Supabase，抽屉内的单次编辑不会自动保存。
- Explore 当前是 8 城静态 provider，已有 provider readiness metadata，但尚未接入真实 POI / 酒店 / 餐饮 / 票务 API。
- Tools 当前是结构化静态参考清单，支持分类深链、离线 pocket notes、API priority 和 provider readiness metadata，但没有接入真实汇率/翻译/签证规则数据源。
- 目的地背景当前通过 CSS 场景层模拟北京/上海/江南/山城氛围，尚未引入单独城市背景图片资产。
- 桌面端已按 1440x900 做过截图和滚动断言；移动竖屏仍是后续精修范围。
- 桌面横屏端为当前优先体验；移动竖屏端后续需要针对抽屉、画布密度和 Trips 卡片继续优化。
- OneDrive 目录偶尔会锁住 `.next` 构建缓存；如出现 `readlink` / `EBUSY`，停止 dev server 并安全删除 `.next` 后重跑。
- `playwright.config.ts` 的 `webServer.command` 写的是 `npm.cmd run dev`（Windows 专用），在非 Windows 环境（例如本次远程沙箱）跑 `npm run test:e2e` 会因为找不到 `npm.cmd` 而无法启动开发服务器；本轮在远程沙箱里改为手动启动 `npm run dev` 并用 Playwright 直接连接验证，未 touch 该配置文件，因为用户本地是 Windows 环境，`npm.cmd` 在那边是正确的。

## 下一步优先级

1. 选择并验证第一个真实数据源：建议从 Tools 的实时汇率 API 开始，因为风险低、字段简单、对旅行实用性明确。
2. 评估 Explore 真实第三方 provider 接入，优先验证 Amap/Trip.com/Meituan/Tripadvisor 哪个最适合 POI、餐饮、住宿和票务数据。
3. 如需要强化视觉惊艳感，生成/接入北京、上海、江南、山城等城市级水墨背景资产，替换当前 CSS 氛围层。
4. 继续移动端竖屏细节适配。

## 关键文件索引

- `app/chat/page.tsx` — Chat / AI Butler 页面入口。
- `app/trips/page.tsx` — Trips Dashboard 页面入口。
- `app/trips/[id]/page.tsx`、`components/trips/TripDetail.tsx` — Trip detail 页面，含归档状态切换和分享链接管理。
- `app/share/[token]/page.tsx`、`components/share/ShareView.tsx` — 公开只读分享页。
- `app/api/chat/route.ts` — DeepSeek + fallback chat API。
- `components/chat/ButlerWorkspace.tsx` — 主工作台状态管理和 API 调用，挂载时读取 `?trip=`（恢复保存的画布）和 `?add=`（自动发送 Explore 的 Add to Trip 草稿消息）两个 URL 参数。
- `components/chat/ChatPanel.tsx` — 聊天面板。
- `components/canvas/TripCanvas.tsx` — live trip canvas 组合组件，渲染 Day 时间线（ButlerReminders 已在 v0.1.28 移除）。
- `components/canvas/ButlerReminders.tsx` — 行程时间线下方的轻量提醒列表组件（文件保留，但已不在 TripCanvas 中渲染）。
- `lib/visual/destinationBackground.ts` — 目的地到水墨背景场景的唯一映射入口。
- `components/canvas/DayCard.tsx` — 单日行程摘要卡片。
- `components/canvas/DayDetailDrawer.tsx` — 单日完整行程详情抽屉。
- `components/trips/TripsDashboard.tsx` — Trips 行程库 dashboard。
- `lib/trips/mockTrips.ts` — Trips 当前静态 mock data，以及 `tripStatusDescriptions` / `tripStatusNextActions` 共享状态说明。
- `lib/ai/deepseekButler.ts` — DeepSeek V4 Flash provider 与 mock fallback。
- `lib/mock-ai/mockButler.ts` — mock AI fallback provider。
- `lib/canvas/applyCanvasPatch.ts` — canvas patch reducer。
- `lib/types/trip.ts` — 核心产品类型。
- `lib/supabase/schema.ts` — Supabase 表结构的 TypeScript 契约。
- `lib/supabase/client.ts` — 浏览器 Supabase 客户端 + `isSupabaseConfigured`。
- `lib/supabase/auth.ts` — 邮箱密码登录/注册、Google OAuth、改名、改密码、登出/session 读取。
- `lib/supabase/useSupabaseSession.ts` — React hook，给组件提供 `{ configured, loading, session }`。
- `lib/supabase/tripsRepository.ts` — trips/canvas_versions/messages 的唯一读写入口。
- `supabase/migrations/0001_init_trip_schema.sql` — Supabase schema SQL 迁移（需要在真实项目里手动跑一次）。
- `supabase/migrations/0002_trip_archive_and_share.sql` — 新增 `archived` 状态和分享链接公开只读 RLS policy（需要在 0001 之后手动追加执行）。
- `lib/explore/types.ts` — Explore 域类型、`ExploreProvider` 接口和 `ExploreProviderStatus`。
- `lib/explore/staticProvider.ts` — 静态 Explore provider 实现（北京/上海/成都/西安/广州/杭州/苏州/重庆）和 provider readiness metadata。
- `lib/explore/index.ts` — `getExploreProvider()` 工厂，组件唯一允许调用的入口。
- `app/explore/page.tsx`、`components/explore/ExploreBoard.tsx` — Explore 页面入口和城市/景点/美食/住宿看板组件，每个条目带 Add to Trip 按钮（跳转 `/chat?add=<草稿消息>`）。
- `components/account/AccountMenu.tsx` — Account 头部图标 + 悬浮窗口，登录/注册/Google 登录/改名/改密码/登出的唯一入口。
- `lib/tools/types.ts` — Tools 域类型、`ToolsProvider` 接口和 `ToolsProviderStatus`；`ToolCategory` 包含 `tips`、`sections`、`offlineTips`、`apiPriority`。
- `lib/tools/staticProvider.ts` — 静态 Tools provider 实现（签证入境/支付设置/翻译/汇率/地铁/eSIM-VPN/应急 7 个分类），包含结构化内容、离线提示、API 接入优先级和 provider readiness metadata。
- `lib/tools/index.ts` — `getToolsProvider()` 工厂，组件唯一允许调用的入口。
- `components/shell/NavTabs.tsx` — 顶部 Chat / Trips / Explore / Tools / Translate 导航，使用 `lucide-react` 图标。
- `app/tools/page.tsx`、`components/tools/ToolsBoard.tsx` — Tools 页面入口和分类列表/详情看板组件，支持 `/tools?category=<tool-category-id>` 分类深链，并渲染出发前清单、离线 pocket notes、API priority；Translate 分类有 CTA 链接到 `/translate`。
- `app/translate/page.tsx`、`components/translate/TranslatorPage.tsx` — Translate 页面入口和三 Tab 布局（文字翻译 / 扫描翻译 / 短语词典）。
- `components/translate/TextTranslator.tsx` — EN↔ZH 文字翻译，TTS，复制。
- `components/translate/OcrTranslator.tsx` — 图片上传/拍照，Canvas API 缩放，OCR.space 识别，自动翻译，TTS。
- `components/translate/PhraseBook.tsx` — 44 条常用短语 + 28 条特殊词语，每条有 TTS。
- `lib/translate/types.ts` — `Phrase`/`SpecialTerm` 类型定义。
- `lib/translate/phrases.ts` — 全量静态短语和特殊词语数据。
- `app/api/translate/text/route.ts` — DeepSeek 翻译服务端代理（`DEEPSEEK_API_KEY`）。
- `app/api/translate/ocr/route.ts` — OCR.space 扫描识别服务端代理（`OCR_SPACE_API_KEY` 或免费 key）。
- `app/globals.css` — 当前视觉系统和响应式布局，含 `.butler-reminders`、`.translator-page`、`.text-translator`、`.ocr-translator`、`.phrase-book`、`.tools-category-cta` 样式和 `body[data-destination-scene]` 场景样式。
- `public/ink-landscape.png` — MVP 水墨背景资产。
- `tests/butler-reminders.test.tsx` — ButlerReminders 组件测试（映射/未映射类型渲染、空 alerts）。
- `tests/canvas-components.test.tsx` — Canvas 组件综合测试，含 ButlerReminders 深链断言。
- `tests/tools-board.test.tsx` — ToolsBoard 测试，含 `?category=` URL 预选中断言。
- `tests/destination-background.test.ts` — 目的地到背景场景映射测试。
- `tests/explore-provider-status.test.ts` — Explore provider readiness metadata 测试。
- `tests/tools-provider-status.test.ts` — Tools provider readiness metadata 测试。
- `tests/` — 单元、组件、API、e2e 测试。

## 本地验证记录

本轮 `v0.1.25` 已通过：

```bash
npm.cmd run test      # 25 files, 52 tests passed
npm.cmd run build     # production build passed
npm.cmd run test:e2e  # 2 Playwright tests passed
```

额外做过 1440x900 桌面布局断言：`/chat`、`/explore`、`/tools?category=currency` 均保持 `bodyScroll=false`，主要内容在各自内部滚动容器中滚动；`/chat` 的目的地场景为 `beijing-imperial`，Explore/Tools provider status 文案可见。

本轮 `v0.1.26` 需通过：

```bash
npm run test      # all tests pass (including butler-reminders.test.tsx)
npm run build     # production build passes
```
## v0.1.31 Handoff Update - Community MVP, Membership, and Avatars

Current version: `v0.1.31`.

Completed in this iteration:

- Upgraded `/community` from static mock display to local interactive MVP.
- Feed now supports local post publishing, type/city filters, likes, saves, comments, read-more details, and localStorage persistence.
- Photos now supports local photo-card publishing and local likes. Real image upload is still deferred.
- Added membership system with five tiers: Bamboo Guest, Panda Explorer, Silk Road Insider, Dragon Pass, and VisePanda Concierge.
- Community authors now show panda avatars and membership badges.
- Account trigger now shows the selected panda avatar. Account popover includes a six-avatar picker and stores the choice in `localStorage`.
- Added six local panda avatar SVG assets under `public/avatars/`.
- Added `tests/community-board.test.tsx` and expanded Account guest tests for avatar selection.

Known follow-up:

- Add Supabase community tables and repository layer for cross-device posts, comments, likes, saves, and photos.
- Add Supabase Storage avatar/photo upload, cropping, content moderation, and profile sync.
- Add real membership progression, points ledger, paid entitlement rules, and admin moderation tools.

## v0.1.32 Handoff Update - Tools Card Drawers

Current version: `v0.1.32`.

Completed in this iteration:

- Removed the old Translate category from `/tools`. Translation remains available through the dedicated `/translate` nav tab.
- Converted Tools category selection into six compact name-only cards: Visa and entry, Payment setup, Currency, Metro, eSIM/VPN, and Emergency.
- Changed default Tools behavior so no category detail is visible unless a valid `?category=` param is present or the user opens a card.
- Kept `/tools?category=<tool-category-id>` deep links working for valid category ids.
- Added active-card toggling: clicking the currently open card closes the drawer and clears the query param.
- Removed traveler-facing provider metadata from Tools UI, including provider label, coverage, next-integration copy, candidate API-source strings, and category API-priority blocks.
- Updated retained `ButlerReminders` language alerts to route directly to `/translate`, because `/tools?category=translate` no longer exists.
- Updated targeted Tools tests for six categories, card/drawer behavior, Translate removal, and hidden provider metadata.

Known constraints:

- `ToolsProvider.getProviderStatus()` and `ToolCategory.apiPriority` still exist for internal planning, but `ToolsBoard` must not render them unless the product direction changes.
- `ButlerReminders` is still retained as an unused component file; its `language` alert route now points to `/translate` instead of the removed `/tools?category=translate` category.

Next recommended steps:

1. Decide whether Tools provider status should stay internal-only or move to a future admin/debug page.
2. Continue product polish on one-page desktop layouts after validating the Tools card/drawer behavior on production.

## v0.1.30 Handoff Update - Translator Qwen Stack

Current version: `v0.1.30`.

Completed in this iteration:

- Replaced mixed Translator providers with Aliyun Bailian Qwen across text translation, OCR, TTS, and STT.
- Added `lib/aliyun/qwen.ts` shared helper.
- Updated `/api/translate/text` to `qwen-mt-flash`.
- Updated `/api/translate/ocr` to `qwen3.5-ocr`.
- Added `/api/translate/tts` using `qwen3-tts-instruct-flash`.
- Added `/api/translate/stt` using `qwen3-asr-flash`.
- Added `VoiceTranslator` tab with record/upload/public URL input and automatic translation after transcription.
- Replaced browser `speechSynthesis` calls in Text, OCR, and Phrase Book with server-side Qwen TTS playback.
- Added tests: `tests/qwen-translate-api.test.ts` and `tests/translator-page.test.tsx`.

Known deployment requirement:

- Vercel must set `DASHSCOPE_API_KEY` (or `ALIYUN_BAILIAN_API_KEY`) for the Translator Qwen routes to work in production.
- Optional endpoint/model overrides are documented in `lib/env/placeholders.ts`.

Known follow-up:

- Longer production voice recordings should later upload to Supabase Storage or OSS before STT if data URL payload size becomes a problem.
