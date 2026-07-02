# VisePanda — 交接文档
 
## 当前状态
 
- 完成阶段：阶段一 AI Butler Chat MVP 骨架；阶段二真实 AI provider + Supabase 登录 + guest draft 自动迁移已接入；阶段三 Trips 已接入真实 Supabase persistence 首个闭环，加入了 trip detail 页面、归档/分享链接流程和状态说明系统（任务 3.6）；阶段四 Explore 已升级为 Amap 实时 POI 驱动（景点/美食/住宿），完成 provider abstraction、Add to Trip、route rebalance 文案和 provider readiness metadata（任务 4.1-4.5、7.1-7.2、9.2）；阶段五 Tools 已从占位页升级为静态 provider 驱动的 7 个分类骨架，支持分类深链、结构化内容、离线 pocket notes、API priority、provider readiness metadata，以及实时 ExchangeRate-API 汇率接入（任务 5.1-5.3、7.3-7.4、9.1）；阶段六目的地感知水墨背景切换已完成第一版（任务 6.1-6.4）；阶段八 Canvas ButlerReminders 深链 Tools 分类已完成（任务 8.1）；Account 已从独立页面改为头部图标 + 悬浮窗口，登录方式从 magic link 改为邮箱密码 + Google OAuth，登录后支持改名/改密码/登出（任务 2.5）；阶段十翻译页面已全部实现（任务 10.1-10.4），含文字翻译、OCR 扫描翻译、短语词典，ButlerReminders 已从 TripCanvas 移除（v0.1.28）；v0.1.34 桌面横屏前端优化：Tools 6 个模态卡片 + 浮层对话框、Trips 筛选按钮布局修复（过滤器始终可见）、Translator 单页 2×2 网格布局（同时展示四个功能面板无需切换 tab）。
- 当前分支：`main`
- 当前版本：`v0.3.10`（版本序列 `0.3.x` 原生 Android APK 主干；本轮是屏幕适配打磨:AndroidManifest 加入 resizeableActivity/max_aspect、导航栏改为真正的悬浮遮罩、Web 端 viewport 修正）
- 重要（已完成）：
  - v0.3.10:屏幕适配打磨。操作者一条消息给出四条指令:"AndroidManifest application 标签内加入 resizeableActivity="true""、"application 内部加入 max_aspect=2.4 元数据"、"不要写死任何像素尺寸，全部用 match_parent 铺满屏幕"、"viewport 不要限制最大高度，只写 width=device-width"。前三条落在 Android 端(`AndroidManifest.xml` 加 `resizeableActivity`/`max_aspect` 元数据;全仓库审计确认 Compose 布局本来就没有硬编码屏幕级尺寸,已合规),第四条落在 Web 端(`app/layout.tsx` 新增只写 width 的 `viewport` 导出,这是 v0.2.17 冻结以来第一次触碰 Web 端,范围严格限定为这一行)。另外操作者单独指出导航栏"是浮现在page文字和内容上面的，不需要有边框，要融入"——这是对 v0.3.9 的功能性补完:之前导航栏外观悬浮,但结构上仍是 `Scaffold` 的 `bottomBar` 插槽会挤占布局空间;本轮把 `VisePandaNavHost.kt` 改成 `Box` 叠加布局,`NavHost` 铺满全屏,`VisePandaBottomBar` 用 `Alignment.BottomCenter` 叠在上层,新增 `Dimens.BottomNavContentClearance`(96dp)应用到 Trips/Me 的滚动列表和 Chat 输入区,保证内容能滚动到导航栏"下面"又不会被永久遮挡。`./gradlew :app:testDebugUnitTest :app:assembleDebug` 通过(遇到一次已知的 Gradle 增量 dex 冲突,`clean` 后重新编译通过);Android 34 模拟器验收:Trips 滚动到底最后一张卡片和导航栏有清晰间隙、Chat 输入区完全不被遮挡、Me/Explore/Tools 无回归;`npx tsc --noEmit` 确认 Web 端改动无新增类型错误。详见 DESIGN.md ADR-114。
  - v0.3.9:导航栏圆角悬浮化 + 文本框/对话框加大圆角。操作者提供一整套黑色手机 mockup 设计参考图(5屏:Chat home/Plan/Explore/Tools/Me),给出三条指令:"导航栏要做圆角悬浮式"、"taxi driver card 不重要 优先级极低，如果影响了项目进展可以移除"、"所有的文本框，对话框都做成圆角的，弧形"。改动:`VisePandaBottomBar.kt` 从贴边纸白色 `NavigationBar` 改为四周留白(16dp inset)、`RadiusPill` 圆角、`Ink` 深色背景的悬浮胶囊,高度从 80dp 收窄到 64dp,两侧图标去掉文字标签、未选中态用半透明 `Paper` 保证在深色底上的对比度;`ButlerScreen.kt` 聊天输入框和 `TaxiDriverCard.kt` 的 `AlertDialog` 都加上 `RoundedCornerShape(RadiusXL)`。Taxi Driver Card 评估后判断本轮不依赖移除它,予以保留,但在验收截图时**顺带发现并修复一个此前就存在的真实 bug**:对话框 `confirmButton` 里 Copy/Speak/Close 三个按钮塞进一个 `Row` 装不下,`Close` 被挤压成不可读窄条,改用 `FlowRow` 修复。`./gradlew :app:assembleDebug` 两次(改动后+FlowRow 修复后)均 `BUILD SUCCESSFUL`;Android 34 模拟器手动验收(改用 `uiautomator dump` 取精确坐标,不再靠肉眼估算截图坐标)确认新导航栏在 Trips/Explore/Chat/Tools/Me 五个页面都渲染一致、Taxi Card 对话框圆角和三按钮都清晰可读,全程无崩溃。详见 DESIGN.md ADR-113。
  - v0.3.8:底部导航重构。操作者明确要求"底部导航栏顺序Trips/Explore/Chat/Tools/Me（account），Chat要突出"——这是产品结构改动,不同于 v0.3.7"只做视觉层"的范围。用 `AskUserQuestion` 确认两点:Trips = 合并 Today + Plan;Chat 突出 = 中间悬浮圆形按钮(同 Figma 参考的两侧+悬浮布局)。改动:`navigation/AppDestination.kt`/`VisePandaBottomBar.kt`(重写为自定义两侧+中间悬浮 Chat 按钮布局)/`VisePandaNavHost.kt`;新增 `ui/trips/`(合并 Today 的 Now/Next/Later + Plan 的 day 列表,删除 `ui/today/` 和 `ui/plan/PlanScreen.kt` 等,保留 `DayDetailScreen.kt`);新增 `ui/me/`(新 Profile 页,只有"当前行程"一行是真实数据,其余诚实标注为本地占位)。**意外发现并修复一个贯穿多个既有屏幕的真实 bug**:`Card` 内没有显式指定颜色的 `Text` 会渲染成意外的红棕色(接近 error 色),而非中性深色——通过截图像素采样(不是肉眼判断)确认,修复涉及 `ButlerScreen.kt`/`DayDetailScreen.kt`/新增的 `TripsScreen.kt`/`MeScreen.kt`,同时把 `Theme.kt` 的 `ColorScheme` 补全为完整的 30 个角色(此前只设置了约 16 个)。`./gradlew :app:testDebugUnitTest :app:assembleDebug` 通过;Android 34 模拟器验收新导航/Trips/Me 内容和颜色修复均确认;`strings.xml` 里 Tools/Explore 占位顺延为 v0.3.9/v0.3.10。
  - v0.3.7:Android 视觉层对齐 Figma Make 设计参考。操作者提供 Figma Make 文件 `Design According to MD Document`(https://www.figma.com/make/8J1WnuwCHwx60bSC6f7fQn/),要求"将布局修改为figma的设计"。读取该文件(通过打开代码编辑器视图 + CodeMirror `EditorView.state.doc` 取出完整 `App.tsx` 源码,而非 Figma MCP 对 Make 文件只返回 resource_link 清单的默认行为)后发现其产品结构(五屏 Chat/Plan/Explore/Tools/Me、两侧+中间悬浮按钮底部导航)和 repo 既有路线图(Today/Chat/Plan/Explore/Tools 横向导航)有实质性差异,遂用 AskUserQuestion 征询操作者采纳范围,操作者选择"只做视觉层"。改动:`Color.kt` 配色对齐 Figma 精确值(`#FAF8F4`/`#C1292E`/`#C9A84C`,这是对 ADR-094/095"和 Web 端逐字对齐"规则的一次刻意例外,只改 Android 未同步改 Web,记录见 DESIGN.md ADR-105);首次引入 Playfair Display + DM Sans 两款 Google Fonts(此前 v0.3.3 是系统字体占位);`Dimens.kt`/`Theme.kt` 新增更大圆角 token 让卡片更"软";`TaxiDriverCard.kt` 中文地址字号从 34sp 提到 Figma 规格的 52sp,过程中发现并修复一个真实 bug——只调字号不调行高会导致多行文字笔画重叠,已同步把 lineHeight 调到 64sp。真实构建:先跑出一个真实编译错误(`FontVariation` 需要 `@OptIn(ExperimentalTextApi::class)`),修复后 `BUILD SUCCESSFUL`;Android 34 模拟器手动验收新视觉效果和 Taxi Card 修复,`Copy Chinese address` 功能未受影响。明确未采纳的部分:底部导航形态、"Me" tab、Plan 提前加候选区、Tools 8 格 grid、Translator overlay——这些是产品结构改动,超出本轮"视觉层"范围,留给操作者未来决定。`strings.xml` 里 Tools/Explore 占位顺延为 v0.3.8/v0.3.9。
  - v0.3.6:Native Android Butler + Sync Bridge I(原计划编号 "v0.3.5 Butler + Sync Bridge",因 v0.3.5 被构建验证收尾轮占用,实际在 v0.3.6 交付)。Chat(Butler)从诚实占位页升级为真实 Jetpack Compose 对话界面并成为默认首页(底部导航顺序 Today/Chat/Plan/Explore/Tools);新增 `data/model/ButlerModels.kt`/`CanvasPatchApplier.kt` 镜像 Web 端 `CanvasPatch`/`AssistantResponse`/merge 规则;`data/repository/RoomTripRepository.kt` 替换 `MockTripRepository` 成为真实绑定,通过 Retrofit 调用现有 `/api/chat`,失败时走 `NativeButlerFallback` 诚实兜底(不假装有实时 AI,不生成真实交易能力);Room 存储当前行程 + Butler 消息记录。上一轮 Codex 会话因沙箱无法跑 Gradle(网络/native service 受限)未能验证,已在本轮真实 macOS/Android Studio 环境补上:`./gradlew :app:testDebugUnitTest :app:assembleDebug` 一次性 `BUILD SUCCESSFUL`(与 v0.3.4 那批不同,这批代码没有真实编译错误需要修),4 个单元测试全部通过,Android 34 模拟器手动验收 Chat 默认首页、五 Tab 切换、消息发送→API 失败→offline fallback 完整链路、Today 页面数据未被破坏均通过。观察到一处非阻塞设计风险:`NativeButlerFallback` 用简单关键词匹配(nanjing/shanghai/beijing)决定是否改写行程标题,离线场景下有被误伤的可能,记录供后续评估。未做 WebView、未恢复隐藏打车卡触发、未开 Dynamic Color、未新增 Supabase schema、未实现真实支付/预订/地图/相机/麦克风。Tools/Explore 占位文案已顺延为 v0.3.7/v0.3.8。
  - v0.3.5:Android 构建验证与验收交接收尾。上一轮非沙箱 macOS/Android Studio 环境已完整跑通 `./gradlew :app:assembleDebug`，生成 `android/app/build/outputs/apk/debug/app-debug.apk`（约 17.5 MB），并在 Android 34 `google_apis/arm64-v8a` 模拟器上完成手动验收：五个底部 surface 可切换；Plan -> Day Detail 正常；Today 和 Day Detail 均可打开 Taxi Driver Card；`Copy Chinese address` 复制 `北京市东城区景山前街4号` 成功；真实关闭 Wi-Fi/data 后 mock 数据路径不崩溃。已补齐 Gradle wrapper，修复 Kotlin 2.0 Compose compiler plugin、`getValue` import、Material 3 `TopAppBar` opt-in 三类真实编译错误，并更新 `android/README.md`。操作者还提供 Lovable 预览和 Figma Make `Design According to MD Document` 作为后续 Android UI 借鉴来源；当前 Figma connector 可读取该 Make 文件的资源列表，但源码资源读取未开放，后续若要精确复刻需要截图或 node 级设计链接。下一步仍是功能性 `v0.3.5 Butler + Sync Bridge`，本收尾版本不代表 Butler sync 已完成。
  - v0.3.4:第一轮真实 Android 原生代码(v0.3.3 Native Foundation + v0.3.4 Today/Plan Execution MVP 合并交付,操作者明确要求 Android 工程放在本仓库 `android/` 子目录,monorepo)。技术栈 Kotlin + Jetpack Compose + Material 3 + Hilt + Room/DataStore(已定义未深接)+ Retrofit(为 v0.3.5 预置)。五 surface 底导(Today/Butler/Plan/Explore/Tools);Today 与 Plan+Day Detail 有真实内容,数据层从 Web 端 `lib/mock-ai/mockButler.ts`/`lib/trips/completeness.ts` 逐字段/逐规则移植,确保两端 readiness 百分比一致;Butler/Explore/Tools 为诚实占位页;打车卡组件只能通过显性按钮触发(v0.3.2 已否决隐藏手势方案)。该轮原本因沙箱无 Android SDK 而未验证；v0.3.5 已补上真实构建与模拟器验收。
  - v0.3.2 是纯文档规划融合轮：操作者说明另一个 coding agent 已把 Android 规划上传到 GitHub，要求读取、审核、蒸馏并与 Codex 自己的规划合成新版本。新增 `docs/planning/v0.3.2-android-planning-synthesis.md`，保留对方规划中 Kotlin/Jetpack Compose/Material 3/Room/DataStore/MVI/StateFlow/API 复用/权限分步/MapView 生命周期等强工程判断，同时把产品模型修正为 Today / Butler / Plan / Explore / Tools。按操作者最新要求，已从规划中移除非常规后台式打车卡入口，改为 Today / 当前行程卡 / Day Detail 显性按钮。下一轮建议 `v0.3.3 Android Native Foundation`，再到 `v0.3.4 Today + Plan Execution MVP`、`v0.3.5 Butler + Sync Bridge`。本轮不写业务代码、不创建 Android 工程、不新增 key/schema/provider/交易能力。
  - v0.3.1 原生 Android APK 专项规划及研发主线调整：产品核心研发主干由 Web 端彻底转向原生移动端（iOS SwiftUI + Android Jetpack Compose）。编制了原生规格说明书 `docs/planning/v0.3.1-android-native-spec.md`。本期仅作方案规划与逻辑设计，未修改/产出任何 Java/Kotlin、XML 布局或业务实现代码。**同一迭代内补充**（未新增版本号）：合并了并行会话草稿里此前未覆盖的内容——4 大 Tab 之外的页面落地（Home 作为一次性引导层、Day 详情独立页面、Trips/Account 归属、Translate 原生采集层、Community 明确不展开）、现有 `/api/*` 后端逐路由复用边界表（绝大多数直接复用，仅 OCR/STT 文件上传需一次兼容性核实）、游客态 vs 登录态权限边界表、iOS 规划边界重申。详见规格书"阶段 5：补充规划"一节与 `CHANGELOG.md` 对应条目。
  - v0.2.17 是纯文档规划轮：应操作者要求，评估高德之外可拓宽产品线的景点/餐饮/酒店数据与预订服务，新增 `docs/planning/data-provider-expansion-assessment.md`，覆盖 Trip.com（酒店）、大众点评（餐饮）、百度地图、腾讯位置服务、Klook（门票/活动）、KKday（门票/活动）六个候选。结论：优先研究 Trip.com 与 Klook（两者都是外国游客友好、能补上"能直接付款下单"这个高德完全没覆盖的缺口）；大众点评存在竞品条款法律风险，建议先做商务/法务评估而非技术调研；百度地图/腾讯位置服务与高德数据重合度高，不建议投入。**研究局限已诚实披露**：六个官方文档站点直接 WebFetch 均返回 403（反爬所致，非代理故障），本轮结论基于 WebSearch 结果交叉印证，不是逐字源码级核实，标注「⚠️ 待人工核实」的条目在真正对接前需人工登录官网确认。未改动任何产品运行时代码。**版本协调**：本轮开发时本地分支曾与 `main` 短暂分叉（`main` 已并行推进至 v0.2.16，含 Explore/Tools/TripBlock POI 多轮实现），发现后未强推覆盖，而是在 `main` 最新提交基础上重新构建，版本号顺延为 `v0.2.17`。
  - v0.2.16 完成 Explore candidate review / Day detail action polish：Flexible 候选块只在真实候选存在时显示，用户可见文案改为 “Needs scheduling”；Day detail 为候选 POI 提供 “Ask VisePanda to schedule” 动作，并继续通过 Chat/AI patch 管道重排，不本地硬改行程。未新增 checkout、库存、支付、Supabase schema、新 key 或生产 FlyAI。
  - v0.2.15 完成 Explore Add-to-Trip POI write-through：Explore 的 Add to Trip 现在同时携带自然语言 Chat draft 与结构化 POI payload；Chat 首跑会解析 payload，并确定性 enrich 匹配 TripBlock 或在目标城市 day 追加可见的 Flexible 候选块；Day card 与 Day detail 均显示 Flexible block。未新增 checkout、库存、支付、Supabase schema、新 key 或生产 FlyAI。
  - v0.2.14 完成 Real POI context write-through + booking candidate model：Amap `liveToolContext` 现在携带 id、电话、地图链接、坐标和 info-only booking candidates；orchestrator 在 provider patch 解析后会把匹配 POI 的安全字段确定性写回 TripBlock；Day detail 以 “Info only” 展示 booking candidates。未新增 checkout、库存、支付、Supabase schema、新 key 或生产 FlyAI。
  - v0.2.13 完成 TripBlock POI embedding + Day detail operational upgrade：`TripBlock` 新增可选运营字段（地址、中文地址、电话、营业时间、地图/预订链接、source、坐标），Day detail 抽屉展示 POI 执行卡和 Show taxi driver 卡，mock/static fallback 已有代表性中文地址与地图链接。未新增 key、Supabase schema、真实预订交易或生产 FlyAI。
  - v0.2.12 完成 MD 交接面统一：所有当前状态/版本头部指向 v0.2.12，明确 v0.2.11 是已完成的 Frontend Design Resource Stack 配置轮，下一轮代码建议顺延为 v0.2.13 TripBlock POI Embedding + Day Detail Operational Upgrade。本轮不改运行时代码。
  - v0.2.11 完成 Frontend Design Resource Stack 配置：新增 `PRODUCT.md` 与 `docs/planning/v0.2.11-frontend-design-resource-stack.md`，把 Frontend Design / UI design system / CSS animation / creative aesthetics / Awwwards landing / web design guidelines / Vercel React best practices / Superpowers / Impeccable / better-icons / UI Design Brain / DESIGNmd 等资源纳入仓库工作流。纯文档配置，不安装外部工具、不改运行时代码。
  - v0.2.10 完成 Tools Widgets I：`ToolCategory.interactive` 可描述 Currency converter、Visa checker、Payment setup wizard；`/tools` 弹窗在静态清单前渲染相应 widget。所有结果都是保守旅行规划辅助,不做官方签证裁定、不处理真实支付、不新增外部 key。详见 `CHANGELOG.md` v0.2.10 与 `DESIGN.md` ADR-075/076。
  - v0.2.9 完成 Chat factual fast-path + inline Tools cards：签证/支付/汇率/地铁/eSIM/应急等高置信事实问题先从现有 Tools 知识生成结构化工具卡，返回 `mode:"tools"` / `strategy:"tool"`，不等待 LLM；Chat 可渲染工具卡并深链到 `/tools?category=...`，同时保留多模型与 mock fallback。详见 `CHANGELOG.md` v0.2.9 与 `DESIGN.md` ADR-073/074。
  - v0.2.8 完成了操作者提供的高保真 Chat 页面设计稿的视觉重设计：`TripSummary` 新增可内联改名的标题、状态徽标（进度条+下一步单元格）、一览 chip 行、Add day/Rebalance route（真实走 AI 管道）+ View map/Trip settings（诚实禁用）动作行；`DayCard` 新增 Day 级完成度徽标、真实照片或图标占位的三段式 block、主/次两组快捷动作（次要动作收进"…"溢出菜单）；`ChatPanel` 新增头像化头部、消息时间戳与已读对勾、结构化高亮卡片、赞踩/复制反馈、可关闭的 Next Step 卡、图标化输入区与 AI 免责声明。所有非功能性控件（Attach/Mic/Pin/View map/Trip settings）均为真实 `disabled` + `title="Coming soon"`，未伪造任何交互或数据。详见 `CHANGELOG.md` v0.2.8 与 `DESIGN.md` 对应章节。
  - `supabase/migrations/0002_trip_archive_and_share.sql`：用户已手动在 Supabase SQL Editor 执行，归档/分享 RLS policy 已生效。
  - Google OAuth：用户已在 Google Cloud 创建 OAuth 凭据并在 Supabase Authentication → Providers → Google 填入，Google 登录功能已配置就绪。
  - ExchangeRate-API：用户已获取 API Key，已配置 `EXCHANGE_RATE_API_KEY` 到 Vercel 环境变量；`/api/exchange-rate` 路由已连接，Tools Currency 分类展示实时 CNY 汇率（每小时刷新）。
  - 高德地图 API：用户已获取 Web Service API Key，已配置 `AMAP_API_KEY` 到 Vercel 环境变量；`/api/explore/amap` 路由已连接，Explore 景点/美食/住宿来自高德 POI 实时搜索。
  - v0.1.52 是纯文档产品策略/交互规划迭代：新增 `docs/planning/v0.1.52-product-interaction-blueprint.md`，把产品定位、用户旅程、页面职责、功能联动、UX writing 和指标体系定为后续实施依据；路线在 v0.1.54 代码实现后顺延为 v0.1.55-v0.1.61。
  - v0.1.53 是纯文档战略规划迭代：细化了“中国旅行操作系统”产品方向，设计了离线保险库（Offline Vault）、文化背景解读器（Cultural Context Interpreter）、信用卡智能支付路由（Payment Card Routing）、上下文工具推荐（Contextual Tool Promotion）和双语打印包（Bilingual Export & Print Kit）五大模块及系统关联。
  - v0.1.54 是 Interaction Shell I 代码实现：Home 三个 FIT archetype 入口、Chat `?archetype=` 首跑、3 个免打字 starter chips、结构化 `nextStep` 主行动卡、Trip Canvas 标题/状态友好化。
  - v0.2.5 是规划融合 + readiness seed 迭代：读取并合并远端 `v0.2.1`–`v0.2.4` 更新,将本地 FIT travel desk visual polish/readiness seed 融入 `0.2.x` 主线,并把完整代码三轮修正为 `v0.2.6`/`v0.2.7`/`v0.2.8`。
- 最新实现 commit：本轮提交后以 `git log -1 --oneline` 为准
- 当前远端：`https://github.com/JTCAO515/VP-Codex-Final.git`
- 部署地址：`https://go2china.space`

## v0.3.10 Handoff Update - 屏幕适配打磨:Manifest 标志位 + 导航栏真悬浮遮罩 + Web viewport

- 触发来源：操作者一条消息给出四条技术指令,混合了 Android 端和 Web 端术语(monorepo 结构下,前三条对应 `android/` 原生模块,第四条"viewport"/"width=device-width"是纯 Web/HTML 术语,对应 `app/`):
  1. "AndroidManifest application 标签内加入 resizeableActivity="true""
  2. "application 内部加入 max_aspect=2.4 元数据"
  3. "不要写死任何像素尺寸，全部用 match_parent 铺满屏幕"
  4. "viewport 不要限制最大高度，只写 width=device-width"
  另外单独指出:"导航栏是浮现在page文字和内容上面的，不需要有边框，要融入"。
- **AndroidManifest 改动**:`<application>` 标签加 `android:resizeableActivity="true"`(严格按操作者原话只加在 `<application>`,没有重复加到 `<activity>`,避免超出指令范围);`<application>` 内新增 `<meta-data android:name="android.max_aspect" android:value="2.4" />`。两者配合支持分屏/多窗口和超高屏/折叠屏宽高比,避免系统 letterbox 裁切。
- **Compose 布局审计**(第 3 条指令):`grep` 全仓库 `.height(`/`.width(`/`.size(` 硬编码调用,只发现两处——`VisePandaBottomBar.kt` 里 52dp 占位 Spacer 和 58dp Chat 悬浮圆按钮,都是合理的小尺寸 UI 元素,不是应该铺满屏幕的容器。所有顶层页面本来就用 `fillMaxSize()`/`fillMaxWidth()`/`weight(1f)`(Compose 里 `match_parent` 的等价写法)——代码库这条已经合规,本轮无需改动,只做了确认。
- **导航栏改为真正的悬浮遮罩**(响应"浮现在...内容上面...要融入"):
  - `VisePandaNavHost.kt`:`VisePandaApp` 不再用 `Scaffold(bottomBar = VisePandaBottomBar(...))`,改为 `Box(Modifier.fillMaxSize())`——`NavHost` 用 `Modifier.fillMaxSize()` 铺满整个 Box(不再有 Scaffold 自动计算的底部 padding),`VisePandaBottomBar` 通过 `Modifier.align(Alignment.BottomCenter)` 叠加在最上层。
  - `navigation/VisePandaBottomBar.kt`:新增 `modifier: Modifier = Modifier` 参数,应用到外层 `Box`,支持上面的叠加用法。
  - `ui/theme/Dimens.kt`:新增 `BottomNavContentClearance = 96.dp`(导航栏 64dp 高度 + 16dp 底部 inset + 小段余量)。
  - 应用 clearance 的地方:`ui/trips/TripsScreen.kt`/`ui/me/MeScreen.kt` 的 `LazyColumn` `contentPadding` 底部改为 `Dimens.BottomNavContentClearance`(而不是和其他三边一样的 `SpaceLG`);`ui/butler/ButlerScreen.kt` 的 `ButlerComposer`(输入框+相机+麦克风+发送按钮所在的 `Column`)底部 padding 同样改为这个值——因为它是 Chat 页最核心的可交互控件,绝不能被不透明的导航栏挡住导致点不到。
  - `ui/plan/DayDetailScreen.kt` 无需改动:它不属于 `TopLevelDestination`,导航栏在这个页面本来就不显示。
- **Web 端 viewport 修正**(第 4 条指令,`app/layout.tsx`,自 v0.2.17 冻结以来首次触碰 Web 端):新增 `export const viewport: Viewport = { width: "device-width" }`(Next.js 14+ `Viewport` API 导出),刻意不写 `initialScale`/`maximumScale`/任何高度属性,严格对应"只写 width=device-width"的字面指令。范围严格限定在这一行,不代表 Web 端开发重新启动。
- 验证：`./gradlew :app:testDebugUnitTest :app:assembleDebug` 过程中遇到一次已知的 Gradle 增量构建陈旧 dex 产物冲突(v0.3.8 记录过的同一类问题,非代码错误),`./gradlew clean` 后重新编译通过。Android 34 模拟器手动验收:Trips 页面滚动到底,最后一张 Day 卡片和悬浮导航栏之间留有清晰间隙(滚动过程中内容确实会从导航栏"下方"经过);Chat 页面输入框/相机/麦克风/发送按钮整排都在导航栏之上,没有被遮挡;Me/Explore/Tools 渲染无回归。`npx tsc --noEmit` 确认 `app/layout.tsx` 改动没有引入新的类型错误(仓库里已有的、与本次改动无关的测试文件类型错误保持原样,未修复也未加剧)。
- `strings.xml` 里 Tools/Explore 占位文案顺延为 v0.3.11/v0.3.12(因本轮用掉了 v0.3.10)。
- 未改动任何导航路由/数据流/Supabase schema,也未在 Web 端做除 viewport 之外的任何其他改动。详见 DESIGN.md ADR-114。

## v0.3.9 Handoff Update - 导航栏圆角悬浮化 + 文本框/对话框圆角

- 触发来源：操作者发来一整套黑色手机 mockup 产品设计参考图(5屏:Chat home/Plan/Explore/Tools/Me),并给出三条明确指令:(1)"导航栏要做圆角悬浮式";(2)"taxi driver card 不重要 优先级极低，如果影响了项目进展可以移除";(3)"所有的文本框，对话框都做成圆角的，弧形"。
- 导航栏改动：
  - `navigation/VisePandaBottomBar.kt`：外层 `Surface` 从 `fillMaxWidth().height(80dp)` + 贴边 + `colorScheme.surface`(纸白),改为 `Box` 包一层水平/底部各 16dp `padding`(悬浮留白)+ `RoundedCornerShape(RadiusPill)` 圆角 + `Ink`(`0xFF1C1410`)深色背景,高度收窄到 `BottomNavFloatingHeight`(64dp)。两侧 `SideNavItem`(Trips/Explore/Tools/Me)去掉文字标签只保留图标,未选中态图标颜色从 `onSurfaceVariant`(为浅色底设计)改为 `Paper.copy(alpha = 0.55f)`(半透明纸白,在深色底上保证对比度),选中态仍是 `colorScheme.primary`(Cinnabar 红)。中间悬浮的圆形 Chat 按钮形状/颜色/交互不变,只是重新对齐到更矮的新导航栏(偏移量从 -14dp 调整为 -10dp)。
  - `ui/theme/Dimens.kt`：新增 `BottomNavFloatingHeight`(64dp)/`BottomNavHorizontalInset`(16dp)/`BottomNavBottomInset`(16dp)三个常量。
- 文本框/对话框改动：
  - `ui/butler/ButlerScreen.kt`：聊天输入框 `OutlinedTextField` 加 `shape = RoundedCornerShape(Dimens.RadiusXL)`(20dp,复用既有 token,未新增)。
  - `ui/components/TaxiDriverCard.kt`：`AlertDialog` 同样加 `shape = RoundedCornerShape(Dimens.RadiusXL)`。
- Taxi Driver Card 评估：按操作者"优先级极低,若影响进展可移除"的指示评估后,判断本轮任何改动都不依赖移除它——它继续满足 `AGENTS.md` 里"必须通过可见按钮触发,不恢复隐藏手势"的既有约束,予以保留。**给对话框加圆角、用模拟器截图验收时,顺带发现一个此前就存在的真实布局 bug**：`confirmButton` 槽位里塞了 Copy/Speak/Close 三个 `TextButton` 到同一个 `Row`,宽度不够时 `Close` 被挤压成几乎不可读的窄条——用 `uiautomator dump` 取精确坐标核实,该按钮可点击区域只有 54px 宽却有 307px 高,说明文字被迫逐字换行。修复：把这个 `Row` 换成 `FlowRow`(`horizontalArrangement = Arrangement.End`,装不下自动换到第二行),重新截图确认 `Close` 现在正常换行、清晰可读。
- 验证方法论修正：延续 v0.3.8 建立的"验证不能只靠肉眼"的规矩,本轮把这个原则也用到了坐标计算上——之前几次模拟器点击测试因为把预览图坐标乘 1.2 换算成实机坐标时估算出错,点错了位置(比如误点成 Chat 消息建议 chip、误触发了一次不小心的发送)；改用 `adb shell uiautomator dump` 取 UI 元素的精确 `bounds` 后计算中心点坐标,后续点击(Trips/Explore/Tools/Me/Chat 五个导航项、Taxi Card 按钮、Close 按钮)全部一次命中,不再依赖对截图预览图的肉眼像素估算。
- 验证：`./gradlew :app:assembleDebug` 两次均 `BUILD SUCCESSFUL`(FlowRow 修复前后各一次)。Android 34 模拟器手动验收：依次点击 Trips/Explore/Tools/Me/Chat,确认悬浮胶囊导航栏在所有五个页面渲染一致、选中态高亮正确、图标无文字标签;打开 Taxi Driver Card 对话框确认圆角渲染正确、Copy/Speak/Close 三个按钮都清晰可读(此前 Close 是不可读窄条);聊天输入框圆角正确渲染。全程无崩溃。
- 未改动任何 Web 端代码、`/api/*` 路由、Supabase schema,也未移除 Taxi Driver Card 的任何真实功能(Copy/Speak 均照常工作)。

## v0.3.8 Handoff Update - 底部导航重构:Trips / Explore / Chat / Tools / Me

- 触发来源：操作者发消息"底部导航栏顺序Trips/Explore/Chat/Tools/Me（account），Chat要突出"。这是产品结构层面的改动请求,不同于上一轮"只做视觉层"的范围限定,遂用 `AskUserQuestion` 确认两个关键歧义点：
  1. 现有 "Today" 首页已经实现了真实内容(行程标题/readiness/Now-Next-Later 时间轴/Ask Butler/离线横幅),新顺序里没有 Today 了,"Trips" 具体指什么？——操作者选择"Trips = 合并 Today + Plan"。
  2. "Chat 要突出"具体指什么形式？——操作者选择"中间悬浮圆形按钮,同 Figma 参考"(两侧 Trips/Explore + 中间悬浮 Chat + 两侧 Tools/Me)。
- 导航结构改动：
  - `navigation/AppDestination.kt`：`TopLevelDestination` 重写,五个目的地 Trips/Explore/Butler(Chat)/Tools/Me,新增 `leftOfCenter = [Trips, Explore]`/`rightOfCenter = [Tools, Me]` 分组常量。
  - `navigation/VisePandaBottomBar.kt`：完全重写。不再是标准 Material3 `NavigationBar` 平铺五项,改为 `Box` 叠加布局——底部 `Surface` 里用 `Row` 放两侧四个 `NavItem`(中间留一个占位 `Box` 让开空间),上层 `align(Alignment.TopCenter)` + `offset(y = -14.dp)` 叠一个 58dp 圆形红色按钮(`Modifier.shadow` + `CircleShape` + `background(primary)`),视觉上明显比其他四个入口凸起、更大、更醒目。
  - `navigation/VisePandaNavHost.kt`：路由从 Today/Butler/Plan/Explore/Tools 改为 Trips/Butler/Explore/Tools/Me 五个 `composable` 注册。
- 页面合并与新增：
  - 新增 `ui/trips/TripsScreen.kt`/`TripsViewModel.kt`/`TripsUiState.kt`：`TripsViewModel` 用 `combine` 同时订阅 `tripRepository.observeActiveTrip()` 和 `observeOffline()`,`TripsUiState.Content` 字段合并了原 `TodayUiState`(`timeline`)和 `PlanUiState`(`dayCompleteness`)的内容。`TripsScreen` 顶部渲染行程标题/readiness 进度条/Ask Butler 按钮/Now-Next-Later 时间轴(原 Today 内容),下接"Day by day"标题 + Day 卡片列表(原 Plan 内容),点击 Day 卡片沿用原有的 `onOpenDay` 回调进入 Day Detail。
  - 删除整个 `ui/today/` 目录(`TodayScreen.kt`/`TodayViewModel.kt`/`TodayUiState.kt`)和 `ui/plan/PlanScreen.kt`/`PlanViewModel.kt`/`PlanUiState.kt`(功能已合并进 Trips),保留 `ui/plan/DayDetailScreen.kt`/`DayDetailViewModel.kt` 不变(仍是 Day 详情页,包名沿用 `ui.plan` 未做无谓搬迁)。
  - 新增 `ui/me/MeScreen.kt`/`MeViewModel.kt`：`MeViewModel` 只从 `tripRepository.observeActiveTrip()` 读取当前行程标题作为唯一的真实数据(显示在"Trips"设置区块,标注"Active")。`Preferences`(Dietary/Daily budget/Crowd preference)和 `Data & privacy`(Offline data)区块都是明确标注"本地示例数据,尚未接入真实账号系统"的诚实占位内容——参照操作者提供的 Figma 本地源码里 `MeScreen` 的信息架构,但没有伪造虚假的用户偏好数据。
- **意外发现并修复：一个贯穿多个既有屏幕的真实 UI bug**。构建 Trips/Me 页面截图验收时,发现 `Card` 组件内没有显式指定颜色的 `Text`(比如 Day Detail 里的景点标题、Me 页面的设置项 label)渲染成了一种意外的红棕色,肉眼第一眼容易误判成"是不是 section 标题也变红了"(其实不是),用 Python/Pillow 对截图做像素采样才精确定位：真正出问题的是 Card 内**没有**显式设置颜色的 Text,采样值 `(160,35,39)`/`(163,42,46)` 非常接近 `CinnabarDeep`(`0xFFA02226`)。这个问题实际上**从 v0.3.4/v0.3.6 起就存在**于 `ButlerScreen.kt`(欢迎卡片标题、消息气泡内容)和 `DayDetailScreen.kt`(`BlockDetailCard`/`BookingCandidateRow`)里,只是之前的验收测试没有专门核实过"普通正文文字的确切颜色"这个细节,这次因为新页面把同一模式重复了好几处(Preferences 三行 + Trips 一行 + Data & Privacy 一行),连续出现才变得肉眼可见。
  - 第一次尝试的修复(未成功)：怀疑是 `Theme.kt` 的 `lightColorScheme()` 只设置了约 16/30 个 Material 3 角色,其余(`surfaceContainer*`/`surfaceTint`/`errorContainer` 等)静默回退到 Material 基线紫色系默认值,`Card` 的 tonal elevation 混色计算后可能意外解析出接近 error 色的值。补全了全部约 30 个角色(新增 `InversePrimaryTint`/`SecondaryContainerTint`/`ErrorContainerTint`/`SurfaceContainer*` 等常量),重新编译安装后用相同坐标像素采样复测——**没有解决问题**,Card 内文字依然是同样的红棕色。
  - 最终务实修复：不再纠结于完全搞清楚 Material3 内部 `contentColorFor()` 的精确解析链路,直接给所有受影响的 `Text` 补上显式颜色(`MaterialTheme.colorScheme.onSurface` 或 `onSurfaceVariant`),不依赖 Card 的默认继承色。逐个修复 `ButlerScreen.kt`(`EmptyButlerPrompt` 标题、`MessageBubble` 内的 headline/body/highlights)、`DayDetailScreen.kt`(`BlockDetailCard`/`BookingCandidateRow`)、`TripsScreen.kt`(`TimelineEntryCard`/`DayCard`)、`MeScreen.kt`(`SettingsSection`)。重新编译安装后用相同坐标复测,确认文字颜色恢复正常,而 `Not set`/`Active`/`NOW`/`NEXT` 等本来就该是强调色(primary)的文字未受影响。
  - `Color.kt`/`Theme.kt` 里补全的完整 `ColorScheme` 予以保留(即使没有直接解决这次的具体 bug,仍是更规范的写法,减少未来出现同类问题的风险)。
- 验证：`./gradlew :app:testDebugUnitTest :app:assembleDebug` 期间遇到一次真实的、非代码性质的失败——`clean` 之前的一次编译报了 `DexArchiveBuilderException: Type ... is defined multiple times`,是删除 `PlanScreen.kt` 等文件后 Gradle 增量构建留下的陈旧 dex 产物冲突,不是真实代码错误,跑 `./gradlew clean` 后重新编译一次通过。Android 34 模拟器手动验收：新底部导航两侧+悬浮 Chat 按钮渲染正确且可点击切换;Trips 页面正确显示合并后的 Now/Next/Later + Day by day 列表;Me 页面正确显示真实行程数据("China Trip Draft · Active")和诚实占位内容;颜色 bug 修复前后分别截图并像素采样对比确认;全程无崩溃。
- 未改动任何 Web 端代码、`/api/*` 路由或 Supabase schema。

## v0.3.7 Handoff Update - Android 视觉层对齐 Figma Make 设计参考

- 触发来源：操作者发消息"开始下一轮的工作，并且将布局修改为figma的设计：https://www.figma.com/make/8J1WnuwCHwx60bSC6f7fQn/Design-According-to-MD-Document"。
- 读取方式：Figma MCP 的 `get_design_context` 对 Make 文件默认只返回全部源文件的 `resource_link` 清单(不内联代码),尝试 `forceCode`/`excludeScreenshot` 等参数均无变化；改用 `claude-in-chrome` 打开该 Figma Make 网址,切到其内置代码编辑器视图(`</>` 图标),用 `javascript_tool` 在页面里找到 CodeMirror 6 的 `.cm-content` DOM 节点的 `cmView.view` 属性,调用 `view.state.doc.toString()` 拿到完整 `App.tsx` 源码(44220 字符)；注意直接返回大段原文会被安全过滤器判定为"看起来像 cookie/query string"而拦截,改用 `=`/`&`/`?` 占位符替换后分段取出。
- 关键发现：Figma 设计的产品结构和 repo 现有 Android 路线图有实质性差异——五屏是 Chat(home)/Plan/Explore/Tools/**Me**(而非 Today/Chat/Plan/Explore/Tools),底部导航是两侧(Plan/Explore)+中间(Chat)+两侧(Tools/Me)的悬浮按钮布局(而非现有的横向 5 等分 `NavigationBar`),Plan 里还提前包含了原计划 v0.3.8(现顺延为 v0.3.9)才做的 "Needs scheduling" 候选区。这已超出"视觉参考"范畴,而 `AGENTS.md`/`DESIGN.md` ADR-099 一直明确"Figma Make 只是设计参考,不是行为的权威来源"。
- 用 `AskUserQuestion` 征询操作者要采纳到什么程度,给出三个选项(只做视觉层 / 全面采纳产品结构 / 视觉层+局部结构),操作者选择"只做视觉层"，保留现有产品结构(Today/Chat/Plan/Explore/Tools 横向导航、现有路线图节奏),不引入 Me tab,不做 Needs Scheduling 提前,不做完整 Tools grid 和 Translator overlay。
- 抓取的 Figma 视觉 token：背景纸色 `#FAF8F4`、中国红 `#C1292E`、金色 `#C9A84C`、标题字体 Playfair Display(serif)、正文字体 DM Sans(sans-serif)、圆角 20/50/999(px)、字号系统 52(Taxi Card)/28/26/20/16/12/10/9、阴影 `0 6px 20px rgba(193,41,46,0.45)`(红色光晕,推测用于 Chat 悬浮按钮,本轮未采纳该悬浮按钮结构故未使用)。
- 代码改动：
  - `ui/theme/Color.kt`：配色改为 Figma 精确值。**这是对 ADR-094/095 的一次刻意例外**——本轮只改 Android,不动 Web 端 `app/globals.css`,两端色值从此有意存在细微差异(仍是同一套暖色系),记录为 DESIGN.md ADR-105。
  - `ui/theme/Type.kt`：下载 Google Fonts 的 Playfair Display 和 DM Sans(均为 variable font,OFL 许可可自由打包)放入 `res/font/`,用 `FontVariation.Settings` 按字重取用,替换掉 v0.3.3 "系统字体占位,自定义字体推迟"的决定。
  - `ui/theme/Dimens.kt`/`Theme.kt`：新增 `RadiusXL`(20dp)/`RadiusPill`(999dp),`Shapes.medium/large` 调大,卡片更"软"。
  - `ui/components/TaxiDriverCard.kt`：中文地址字号 34sp→52sp。**过程中发现并修复一个真实 bug**：只调字号没同步调行高,导致 52sp 下多行地址文字笔画重叠(34sp 时不明显);已把 `lineHeight` 同步调到 64sp,复测确认不再重叠。
  - `android/app/src/main/res/values/strings.xml`：Tools/Explore 占位版本号顺延为 v0.3.8/v0.3.9(因本轮用掉了 v0.3.7)。
- 验证：`./gradlew :app:testDebugUnitTest :app:assembleDebug` 先报出一个真实编译错误(`FontVariation`/`FontVariation.weight` 是实验性 API,需要 `@OptIn(ExperimentalTextApi::class)`,与之前 `TopAppBar`/`ExperimentalMaterial3Api` 同一类问题),修复后 `BUILD SUCCESSFUL`；Android 34 模拟器手动验收新配色/字体/圆角渲染正常,Taxi Card 52sp 地址不再重叠,`Copy Chinese address` 功能未受影响,全程无崩溃。
- 明确排除：底部导航形态、"Me" tab、Needs Scheduling 提前、Tools 8 格 grid、Translator overlay——这些是产品结构/功能层面的改动,不在本轮"视觉层"范围内,留给操作者未来决定是否要单独立项实现。
- **同轮追加**：操作者随后提供了 Figma Make 项目的本地源码导出(`/Users/jtcao/Downloads/Design According to MD Document/`,比之前浏览器里用 CodeMirror hack 抠出来的片段更完整可靠),交叉核对确认前面记录的配色/字体/圆角/字号均准确,同时发现两处第一遍漏掉的 UI 细节,征询操作者后确认补上：
  - `ui/butler/ButlerScreen.kt`：Chat composer 加 Camera/Mic 图标按钮,禁用态视觉占位——真正的相机/麦克风权限仍按既定策略留到 v0.3.8 Translator 轮按需申请,这里不提前接权限。
  - `ui/components/TaxiDriverCard.kt`：加 Speak 按钮和 Copy 并列,接的是 Android 系统自带 `TextToSpeech` API(不需要运行时权限,不违反"权限按需申请"原则,所以直接接了真实功能)。Android 34 模拟器验证:首次使用时 TTS 引擎联网下载 zh-CN 语音包,下载完成后按钮从禁用变可用,点击后 logcat 确认真实触发了中文语音合成请求,无崩溃。
  - 追加改动同样跑过 `./gradlew :app:testDebugUnitTest :app:assembleDebug`,`BUILD SUCCESSFUL`。
- 未改动任何 Web 端代码、`/api/*` 路由或 Supabase schema。

## v0.3.6 Handoff Update - Native Android Butler + Sync Bridge I

- 触发来源：v0.3.5 构建验证收尾完成后，按既定路线图继续做原计划的 "Butler + Sync Bridge" 功能实现；因版本号 v0.3.5 已被验证收尾占用，本轮功能实现顺延为 v0.3.6。上一轮 Codex 会话在自己的沙箱里完成了全部代码改动并提交、推送，但因沙箱缺 Android SDK、`services.gradle.org` 网络被拦、原生 Gradle service 受限，无法运行 `./gradlew` 做真实验证，写了详细的交接提示词记录已知状态和待办清单。
- 本轮任务：`git status`/`git diff` 确认工作区（发现其实已经干净——上一轮 Codex 会话的提交已经完成并推送到 `origin/main`，不是"未提交"状态，说明交接文档写成时的状态和最终状态之间发生了变化），随后聚焦在真实验证而非重新实现。
- 代码改动内容（继承自上一轮，未做修改，因为编译/测试全部一次通过）：
  - `ui/butler/ButlerScreen.kt`/`ButlerViewModel.kt`/`ButlerUiState.kt`：真实 Chat 界面（输入、发送、消息列表、starter chips、结构化 response、offline 提示）。
  - `navigation/AppDestination.kt`：`TopLevelDestination.all` 改为 Today/Butler/Plan/Explore/Tools（Butler 居中），`VisePandaNavHost.kt` 的 `startDestination` 改为 `Butler.route`。
  - `data/model/ButlerModels.kt`（新增）、`CanvasPatchApplier.kt`（新增）：镜像 Web 端 `CanvasPatch`/`AssistantResponse`/`TripSummaryPatch`，合并规则与 Web 端 `applyCanvasPatch` 对齐。
  - `data/model/TripModels.kt`：枚举加 `@SerializedName` 对齐 Web JSON 序列化值。
  - `data/local/TripCacheEntity.kt`（加 `messagesJson`）、`VisePandaDatabase.kt`（版本号升到 2）、`data/serialization/TripJson.kt`（新增，统一 Gson 编解码）。
  - `data/remote/ButlerApiService.kt`（新增）：Retrofit 接口调用 `/api/chat`。
  - `data/repository/RoomTripRepository.kt`（新增，替换 `MockTripRepository` 绑定）、`NativeButlerFallback.kt`（新增，诚实离线兜底文案与建议）、`TripRepository.kt`（新增 `observeButlerMessages()`/`sendButlerMessage()`）、`MockTripRepository.kt`（补齐新接口，供测试/未来复用）。
  - `di/AppModule.kt`：`TripRepository` 绑定切到 `RoomTripRepository`；新增 Retrofit/OkHttp/Gson provider。
  - `android/app/build.gradle.kts`：`buildFeatures.buildConfig = true`，新增 `BuildConfig.VISEPANDA_API_BASE_URL = "https://www.go2china.space/"`，`versionName` 升到 `0.3.6`。
  - `AndroidManifest.xml`：新增 `INTERNET`/`ACCESS_NETWORK_STATE` 权限。
  - 新增测试：`CanvasPatchApplierTest.kt`、`NativeButlerFallbackTest.kt`。
- **本轮真实验证记录**（在真实 macOS + Android Studio 自带 JBR JDK 21 环境完成，非沙箱）：
  - `local.properties` 补上 `sdk.dir`（不同工作区拷贝需要各自的 `local.properties`，不入库）。
  - `./gradlew :app:testDebugUnitTest :app:assembleDebug` **一次性 `BUILD SUCCESSFUL`**，没有出现 v0.3.4 那批代码曾经遇到的真实编译错误类型（Compose compiler plugin/getValue import/Material3 opt-in 这次全部提前写对了）。
  - 单元测试结果：`CanvasPatchApplierTest` 2/2 通过、`NativeButlerFallbackTest` 2/2 通过，0 failures/errors。
  - Android 34 `google_apis/arm64-v8a` 模拟器手动验收：
    - 冷启动默认落在 Chat 页；第一次截图时底部导航高亮看似缺失，第二次冷启动多等待几秒后确认正确高亮 Chat——判定为截图时机竞态的误报，不是真实 bug。
    - 五个 Tab 顺序 Today/Chat/Plan/Explore/Tools 正确，均可点击切换。
    - 点击 Chat 的 "Make today easier" starter chip，真实发起 `/api/chat` 调用，因模拟器无法访问 `https://www.go2china.space/api/chat`（该路由不对本地模拟器开放/不存在），请求失败，`runCatching` 正确捕获，走 `NativeButlerFallback`：标题栏显示 "Offline fallback · native mock fallback"，assistant 气泡展示 "Saved for the Butler" 诚实文案（"The native app could not reach the live AI service..."），后续 suggestion chips 变为 "Try again when online"/"Show my current plan"。
    - 切到 Today 页面确认正确显示 "You are offline. Showing the last saved trip." 横幅，行程标题（"China Trip Draft"）和 83% readiness 未被破坏——因为这次消息不含城市关键词，`NativeButlerFallback` 的标题改写分支未触发。
    - Explore/Tools 占位页文案确认已正确顺延为 v0.3.8/v0.3.7，彼此不冲突。
    - `logcat` 全程无 `FATAL`/`AndroidRuntime` 崩溃。
  - **观察记录（非阻塞）**：`NativeButlerFallback.createPatch` 里，判断是否改写行程标题用的是 `lower.contains("nanjing"/"shanghai"/"beijing")` 简单关键词匹配，即便是完全离线、无法做真实语义理解的兜底路径也会生效。这意味着用户发一条提到城市名但意图并非改标题的消息（例如"别把行程搞得像南京团一样敷衍"），标题可能被误改。建议后续版本收紧触发条件或去掉这个"伪智能"分支。
- 未改动任何 Web 端代码、`/api/*` 路由或 Supabase schema；未做 WebView 套壳；未恢复隐藏打车卡触发方式；未开启 Material 3 Dynamic Color；未实现真实支付/预订/订单/地图/相机/麦克风能力。
- 下一步建议：真正接入 Supabase auth/trips/messages 持久化（当前 `RoomTripRepository` 只做本地 Room 缓存，没有云端同步）、guest draft 迁移路径、Butler change digest UI。

## v0.3.4 Handoff Update - 第一轮真实 Android 原生代码（v0.3.3 + v0.3.4 合并交付）

- 触发来源：操作者确认"就放在同一个 repo 里面，现在开始正式开工"（回应此前关于独立仓库 vs monorepo 的提问），随后追加"0.3.4也一起做完"，把原计划分两轮的 v0.3.3 Android Native Foundation 与 v0.3.4 Today + Plan Execution MVP 合并为一轮交付。
- 新增 `android/` Gradle 模块（monorepo 内子目录）：`settings.gradle.kts`/`build.gradle.kts`/`app/build.gradle.kts` 完整配置（AGP 8.5.2、Kotlin 2.0.20、Compose BOM 2024.06.00、Hilt 2.51.1、Room 2.6.1、Navigation Compose 2.7.7）；包名 `space.go2china.visepanda`；约 30 个 Kotlin 源文件覆盖数据层（`data/model`/`data/repository`/`data/local`/`data/datastore`）、DI（`di/AppModule.kt`）、导航（`navigation/`）、UI（`ui/theme`/`ui/components`/`ui/today`/`ui/plan`/`ui/butler`/`ui/explore`/`ui/tools`）。
- **数据一致性**：`MockTripData.kt` 与 `TripCompleteness.kt` 分别是 `lib/mock-ai/mockButler.ts` 的 `initialTripState` 与 `lib/trips/completeness.ts` 的逐字段/逐规则 Kotlin 移植，包括容易被忽略的四舍五入细节（`Math.round` → `roundToInt`，避免整数除法截断导致两端百分比不一致的真实 bug）。
- **产品决策落实**：五 surface 底导（非 v0.3.1 草稿的 4-Tab）；Taxi Driver Card 单一共享组件仅可通过显性按钮触发（`ui/components/TaxiDriverCard.kt`），落实 v0.3.2 对隐藏手势触发方案的否决；Dynamic Color 主动关闭，Warm New Chinese 配色精确对齐 `app/globals.css` 十六进制值。
- **历史说明**：v0.3.4 初始提交时确实没有完成真实 Gradle 构建验证,所以当时的文档要求下一位接手者优先跑 `./gradlew :app:assembleDebug`。这个风险已经在 v0.3.5 关闭:Gradle wrapper 已入库,Compose compiler 插件/import/Material3 opt-in 等真实编译错误已修复,debug APK 已生成,Android 34 模拟器手动验收已通过。除非后续改动触碰 Android 构建配置或 Compose 源码,接手者可以直接进入 Butler + Sync Bridge 功能实现。
- 未改动任何 Web 端代码、`/api/*` 路由、Supabase schema 或环境变量配置。

## v0.2.17 Handoff Update - 数据/预订服务拓展评估（纯文档）

- 触发来源：操作者主动提出"针对景点/餐饮/酒店推荐，除了高德 api 的 poi 之外，市面上还有类似的服务，我想要拓宽产品线"，追问后确认要深挖 Trip.com、大众点评、百度地图、腾讯位置服务、Klook、KKday 六个候选（先给了四个方向的问卷，用户追加要求把 Klook/KKday 也一起研究）。
- 交付：`docs/planning/data-provider-expansion-assessment.md`。核心结论已写入本文件"当前状态"一节；完整的逐服务分析（申请门槛、佣金/费用模式、认证方式、⚠️ 待人工核实条目清单）在文档本体。
- 研究方法与 v0.2.6 FlyAI 研究的关键差异：本轮六个官方文档站点直接 `WebFetch` 全部返回 `403 Forbidden`，已用 `curl "$HTTPS_PROXY/__agentproxy/status"` 核实是目标站点反爬所致，非代理故障。因此改用多次独立 `WebSearch` 检索交叉印证，**这是基于搜索引擎摘要的二手信息整理，不是逐字源码级核实**——文档开头和每个条目都明确标注了这一置信度差异，避免下一位读者把它当成和 FlyAI 研究同等严谨度的依据。
- 未触碰 `mock-inventory.md`：六个候选目前都停留在"要不要研究"的规划阶段，不是"代码已就绪等 key"状态，不满足新增条目的既有标准。
- **分支协调**：本轮开发时基于较早的 `main`（v0.2.8）开工，完成后发现 `main` 已被并行会话推进到 v0.2.16（Explore/Tools/TripBlock POI 等多轮实现）。`git fetch origin main` 后确认存在真实分叉，`git merge` 试探性合并在 7 份工作流文档 + `package.json` 上产生版本号编号冲突（属预期——双方都在各自版本号下新增了发布说明段落）；判断这类冲突不适合逐行强行合并，遂 `git merge --abort`，改为在 `main` 最新提交上重新构建本轮的纯文档改动，版本号从原计划的 `v0.2.9` 顺延为 `v0.2.17`。**没有丢失任何一方内容**：`main` 的全部 v0.2.9–v0.2.16 代码与文档原样保留，本轮的新增内容（评估文档 + 7 份工作流文档的对应段落）在其基础上追加。
- 下一步是操作者/团队需要做的事，不是本轮或下一轮 coding agent 能直接推进的：真正启动 Trip.com/Klook 对接需要人工注册对方开发者账号才能拿到一手条款，coding agent 不能代替完成账号注册与商务审核。

## v0.2.8 Handoff Update - Chat/Canvas 视觉重设计

- 触发来源：操作者在 v0.2.7 Canvas 行动层代码收尾阶段提供了一张高保真 Chat 页面设计稿（Canvas 左栏 + Chat 右栏），要求"把 chat 设计成这样"；本会话先完成手头的 v0.2.7 交互逻辑接线并验证通过后，才开始本轮视觉重设计，避免中断已接近完成的工作。
- 组件改动：`TripSummary.tsx`（内联改名 + 状态徽标 + chip 行 + 动作行，`onRenameTrip`/`onAddDay`/`onRebalanceRoute` 三个新 props）、`DayCard.tsx`（Day 完成度徽标 + 三段式 block 重排 + 主/次快捷动作分离）、`ChatPanel.tsx`（头像头部 + byline/highlights/feedback + 可关闭 next-step 卡 + 图标化 composer）、`TripCanvas.tsx`（透传三个新 props 给 `TripSummary`）、`ButlerWorkspace.tsx`（新增 `handleRenameTrip`/`handleAddDay`/`handleRebalanceRoute`，`createMessage` 补充 `createdAt`）、`lib/canvas/quickActions.ts`（拆分为 `DAY_PRIMARY_ACTIONS`/`DAY_SECONDARY_ACTIONS`，新增 3 种 kind）、`lib/types/trip.ts`（`ChatMessage.createdAt?` 新字段）。
- CSS：`app/globals.css` 追加约 350 行"v0.2.8 CHAT + CANVAS VISUAL REDESIGN"新块，在文件末尾（层叠优先级最高）。排查确认了此前并行会话遗留的两处 append-only 覆盖块（`v0.1.55 VISUAL POLISH` 与 `min-width:900px` 紧凑布局块）不会与新样式冲突：`.trip-summary` 已改 `display:flex`，遗留的 grid 定位规则自然失效为 no-op；`.chat-panel` 的三层 `order` 声明在 head/empty-state/prompt-row/chat-log/next-step/composer 上数值完全一致，新增的 `.chat-disclaimer` 用 `order:7` 单独占位，不受影响。过程中定位并修复一处真实回归：遗留的 `.day-card__head span { display:none }` 意外隐藏了新的 pace/完成度徽标，已用更高特异性选择器覆盖。
- 验证：全部 41 个测试文件 134 个测试通过（含新增 8 个）；`npm run build` 通过（20 个静态页）；额外用 Playwright 启动 dev server 对 `/chat` 页面截图核验桌面布局、溢出菜单展开态、内联改名态，确认视觉效果符合设计稿意图后才提交。
- 诚实降级原则再次贯彻：mock fallback（无真实 API key 时）不产出 `AssistantResponse`，因此 highlights/反馈图标等新 UI 在 mock 模式下不会出现在纯文字回复上——这是既有行为，不是本轮引入的缺陷；真实 provider 返回结构化 `assistantResponse` 时会完整渲染。
- 下一轮建议：`docs/planning/v0.2.4-interaction-deep-dive.md` 中尚未实现的部分（MessageBlock 伪流式渲染、`ask_factual` <150ms 快通道、实体 chip 双向悬停联动、设计系统 token 收口、Tools 交互组件）可作为后续迭代的候选范围，具体编号以下一轮开工时的 `package.json`/`git log` 实际状态为准。

## v0.2.9 Handoff Update - Chat factual fast-path + inline Tools cards

- 用户意图：在同步到 `v0.2.8` 后开始执行后三轮建议中的第一轮，让 Chat 从“视觉更像管家”进一步变成“事实类问题能快速给出可操作工具卡”的管家入口。
- 实现思路：不把签证/支付等知识写死在 UI；新增 `lib/tools/factualToolCards.ts`，从现有 Tools 静态 provider 取内容并生成 `AssistantResponse.toolCards`。`lib/ai/orchestrator.ts` 在选择 LLM provider 前先尝试该快通道；命中则返回 `mode:"tools"` / `strategy:"tool"`，不调用 provider。
- 主要文件：
  - `lib/types/trip.ts`：新增 `InlineToolCard` 与 `AssistantResponse.toolCards?`。
  - `lib/tools/factualToolCards.ts`：新增事实问题到 Tools 卡片的确定性映射。
  - `lib/ai/orchestrator.ts`：新增 LLM 前置快通道。
  - `lib/ai/butlerPrompt.ts`：解析器支持并验证 provider 返回的可选 `toolCards`。
  - `components/chat/ChatPanel.tsx`、`app/globals.css`：渲染 inline tool cards 与 busy thinking 状态。
  - `tests/factualToolCards.test.ts`、`tests/orchestrator.test.ts`、`tests/chat-panel.test.tsx`：覆盖生成、绕过 provider、渲染。
- 边界：本轮不做完整 Tools widgets，不做签证决策树/支付向导/汇率换算器，不新增任何外部 key、Supabase schema、生产 FlyAI 调用或预订能力。
- 验证：受影响测试已通过；本轮收尾前需跑完整 `npm run test` 与 `npm run build`。
- 下一步建议：`v0.2.16` Explore candidate review / day-detail action polish：把 Flexible 候选块升级为更明确的 "Needs scheduling" 状态，并在 Day detail 提供 ask-to-schedule / open map / booking-info 的更清晰动作分组。

## v0.2.15 Handoff Update - Explore Add-to-Trip POI write-through

- 用户意图：继续上一轮 POI/booking candidate 基础，让 Explore 页面里的 Add to Trip 不只是把一句话交给 Chat，而是把景点/餐厅/住宿的结构化 POI 信息稳定带入 Trip Canvas。
- 实现思路：保持“所有内容变化仍走 Chat/Canvas patch 管道”的产品规则；Explore 负责生成自然语言 draft + 结构化 `poi` payload，Chat 首跑负责把 payload 合并到 AI/mock patch。若模型已经生成匹配 block，则只补齐缺失字段；若没有生成，则在目标城市 day 追加一个 visible Flexible candidate block。
- 主要文件：`lib/explore/addToTrip.ts`、`components/explore/ExploreBoard.tsx`、`components/chat/ButlerWorkspace.tsx`、`components/canvas/DayCard.tsx`、`components/canvas/DayDetailDrawer.tsx`、`tests/explore-add-to-trip.test.ts`、`tests/explore-board.test.tsx`、`tests/chat-workspace.test.tsx`。
- 边界：不新增 Supabase schema、不保存新的外部交易数据、不做 checkout/库存/支付/退款、不新增 API key、不生产调用 FlyAI；booking candidates 继续只表示 info-only planning reference。
- 历史下一步 `v0.2.16` 已完成；当前下一步建议：`v0.2.17` Candidate controls：为 Flexible 候选补充 Remove candidate / Keep for later 之类的非交易控制,仍走可审计的 Chat 或本地操作边界。

## v0.2.16 Handoff Update - Candidate review and schedule action

- 用户意图：继续 v0.2.15 的 Explore 候选写入,让用户明确知道这些 POI 还是候选,并给出下一步让 Butler 安排进日程的动作。
- 实现思路：不把 Flexible 候选伪装成 Morning/Afternoon/Evening；在 Day card/detail 中显示 “Needs scheduling”。安排候选的按钮不直接修改 `TripState.days`,而是构造自然语言请求并走 `handleSend` -> `/api/chat` -> `CanvasPatch` 的既有管道。
- 主要文件：`components/canvas/DayCard.tsx`、`components/canvas/DayDetailDrawer.tsx`、`components/canvas/TripCanvas.tsx`、`components/chat/ButlerWorkspace.tsx`、`lib/canvas/quickActions.ts`、`app/globals.css`、`tests/canvas-components.test.tsx`、`tests/quickActions.test.ts`。
- 边界：不新增 Supabase schema、不做候选持久化新表、不做 checkout/库存/支付/订单、不新增 API key 或生产 FlyAI。
- 下一步建议：`v0.2.17` Candidate controls。

## v0.2.14 Handoff Update - Real POI write-through + booking candidates

- 用户意图：继续 v0.2.13 后的下一轮，让 live Amap POI context 不只进入 prompt，还能稳定沉淀到 TripBlock，并先建立非交易型 booking candidate 模型。
- 实现思路：不相信模型一定会复制字段；在 provider 返回 patch 后用 `applyToolContextToPatch` 做确定性补齐。booking candidate 只表示信息候选，不代表库存、下单或支付能力。
- 主要文件：`lib/types/trip.ts`、`lib/ai/toolContext.ts`、`lib/ai/toolContextWriteThrough.ts`、`lib/ai/orchestrator.ts`、`components/canvas/DayDetailDrawer.tsx`、`tests/toolContextWriteThrough.test.ts`。
- 边界：不新增 Supabase schema、不新增外部 key、不接 checkout/库存/退款、不生产调用 FlyAI；后续真实预订能力必须另设交易边界。
- 历史下一步 `v0.2.15` 与 `v0.2.16` 已完成；当前下一步建议：`v0.2.17` Candidate controls。

## v0.2.13 Handoff Update - TripBlock POI + Day detail operations

- 用户意图：开始 v0.2.13 功能代码轮，让 Day detail 从“只读行程说明”升级为可执行的旅行运营面板，尤其服务外国 FIT 游客在中国落地时给司机看地址、查营业时间、打开地图或预订信息。
- 实现思路：先扩展 `TripBlock` 的可选字段，不改 Supabase schema，不破坏旧 trip JSON；Day detail 只在字段存在时显示 POI 执行卡。mock/static fallback 填入代表性地址，真实 live provider 后续可复用同一字段。
- 主要文件：`lib/types/trip.ts`、`components/canvas/DayDetailDrawer.tsx`、`lib/mock-ai/mockButler.ts`、`lib/ai/butlerPrompt.ts`、`lib/ai/deepseekButler.ts`、`app/globals.css`、`tests/canvas-components.test.tsx`。
- 边界：不做真实预订/购票/支付，不新增外部 key，不新增 Supabase migration，不生产调用 FlyAI；`bookingUrl` 只是信息链接，不代表库存或交易能力。
- 历史下一步 `v0.2.14` 已完成；当前下一步建议：`v0.2.15` Explore Add-to-Trip POI write-through。

## v0.2.12 Handoff Update - Documentation Alignment

- 用户意图：把所有 MD 文档都更新到 v0.2.12，防止另一台设备或新的 coding agent 接手时误以为当前仍停在 v0.2.11 或下一轮仍是 v0.2.12。
- 实现思路：保留 v0.2.11 的设计资源栈作为历史完成项，同时把当前版本、当前接手依据、下一轮建议和版本规则统一到 v0.2.12 / v0.2.13。
- 主要文件：`VERSIONING.md`、`CHANGELOG.md`、`HANDOFF.md`、`PLAN.md`、`PRD.md`、`DESIGN.md`、`AGENTS.md`、`PRODUCT.md`、`docs/planning/v0.2.11-frontend-design-resource-stack.md`。
- 边界：本轮是文档与版本交接对齐，不改运行时代码、不新增 API key、不改 Supabase schema、不调整 provider routing。
- 历史下一步 `v0.2.13` 与 `v0.2.14` 均已完成；当前下一步建议：`v0.2.15` Explore Add-to-Trip POI write-through。

## v0.2.11 Handoff Update - Frontend Design Resource Stack

- 用户意图：配置一组前端/设计/Agent 资源，包括 Frontend Design、UI design system、CSS animation、creative aesthetics、Awwwards landing、web design guidelines、Vercel React best practices、Superpowers、Impeccable、better-icons、UI Design Brain、DESIGNmd、awesome-design-md 等。
- 实现思路：不把外部资源安装成生产依赖，也不覆盖当前视觉系统；新增一个产品上下文文件和一份资源映射文档，让后续设计/前端任务知道何时使用这些资源、何时以 VisePanda 本地规则为准。
- 主要文件：
  - `PRODUCT.md`:简短产品上下文、目标用户、语气和设计红线。
  - `docs/planning/v0.2.11-frontend-design-resource-stack.md`:资源清单、使用方式、Impeccable readiness、排除项。
  - `AGENTS.md` / `DESIGN.md` / `PRD.md` / `PLAN.md` / `CHANGELOG.md` / `VERSIONING.md`:同步记录 v0.2.11 规则与状态。
- 边界：没有安装 Impeccable、better-icons、MCP server、CLI 或 npm 包；没有新增 API key、Supabase schema、运行时代码或用户可见行为。
- 历史下一步 `v0.2.13` 与 `v0.2.14` 均已完成；当前下一步建议：`v0.2.15` Explore Add-to-Trip POI write-through。

## v0.2.10 Handoff Update - Tools Widgets I

- 用户意图：继续上一轮后的下一轮,把 Tools 从静态清单推进为能实际操作的旅行小工具,优先覆盖汇率、签证/入境、支付三类高焦虑问题。
- 实现思路：不在 `ToolsBoard` 里写死每类逻辑,而是在 `ToolCategory` 上新增可选 `interactive` 描述符,由 `components/tools/widgets/ToolWidget.tsx` 统一按类型渲染。这样未来 live provider 或 Chat inline card 可以复用同一 Tools 数据层。
- 主要文件：
  - `lib/tools/types.ts`:新增 `ToolInteractiveDescriptor` union。
  - `lib/tools/staticProvider.ts`:为 Currency / Visa and entry / Payment setup 三类加入 widget metadata。
  - `components/tools/widgets/ToolWidget.tsx`:新增 RMB converter、Entry planning checker、Payment setup wizard。
  - `components/tools/ToolsBoard.tsx`:在 modal summary 下方挂载 `<ToolWidget />`。
  - `app/globals.css`:新增 `.tool-widget*` 样式。
  - `tests/tools-board.test.tsx`、`tests/tools-provider.test.ts`:覆盖 descriptor 与三件套交互。
- 边界：不新增官方签证判断、不处理真实支付或银行卡、不新增外部 key、不调用 FlyAI 生产能力;静态 tips/sections/offlineTips 全部保留。
- 验证：受影响 Tools 测试已通过;收尾前需跑完整 `npm run test` 与 `npm run build`。
- 历史下一步 `v0.2.13` 与 `v0.2.14` 均已完成；当前下一步建议：`v0.2.15` Explore Add-to-Trip POI write-through。

## v0.1.53 Handoff Update - Strategic Handoff Snapshot
 
- Current version: `v0.1.53` in `package.json` and `VERSIONING.md`.
- Completed since v0.1.52:
  - Brainstormed and established core architecture for 5 travel-facing enhancements (Offline Vault, Cultural Interpreter, Payment Routing, Contextual Promotion, Bilingual Export).
  - Drafted ADR-060 through ADR-063 in `DESIGN.md`.
  - Added implementation details and roadmap checks in `PLAN.md`, `PRD.md`, and `AGENTS.md`.
- Active development status on MacBook:
  - Repository cloned to `/Users/jtcao/Documents/Antigravity/VP-Codex-Final`.
  - Node dependencies installed successfully via `npm install`.
  - Dev server running in background.
  - All 98 unit/component tests passing (100% green).
- Next recommended iteration was the Interaction Shell I code pass, now completed in `v0.1.54`.

## v0.1.54 Handoff Update - Interaction Shell I Code Implementation

- Current version: `v0.1.54` in `package.json`, `package-lock.json`, and `VERSIONING.md`.
- User intent: implement the first code pass after the v0.1.52/v0.1.53 strategy work, while firmly locking product positioning to independent FIT foreign travelers in China and a one-stop AI travel butler that integrates planning, booking, POI/food choices, translation, transit, payment, and maps.
- Workspace note: user supplied `/Users/jtcao/Documents/Antigravity/VP-Codex-Final` as the active local path. This Codex sandbox could read/sync that repo but could not apply patches there, so implementation was completed in the VPMCO writable workspace `/Users/jtcao/Documents/Codex 2/VP-Codex-Final`, synced to the same GitHub `main` source of truth.
- Implemented:
  - `lib/chat/archetypes.ts` defines three shared FIT starts: First China 10 Days Essentials, Foodie China, History & Nature.
  - Home surfaces those starts and routes to `/chat?archetype=<id>`.
  - `ButlerWorkspace` detects `?archetype=`, clears the URL, and sends the mapped prompt through `handleSend`, preserving the existing `/api/chat` → `CanvasPatch` → `applyCanvasPatch` path and mock fallback.
  - `ChatPanel` shows three first-run starter chips and promotes the latest structured `nextStep` into a primary action card.
  - `TripCanvas` uses the current trip title as h1; `TripSummary` maps Draft/Refined/Ready to traveler-facing status text.
- Key files changed: `components/home/HomePage.tsx`, `components/chat/ButlerWorkspace.tsx`, `components/chat/ChatPanel.tsx`, `components/canvas/TripCanvas.tsx`, `components/canvas/TripSummary.tsx`, `lib/chat/archetypes.ts`, `app/globals.css`, tests, and docs.
- Verification: `npm run test` passed (37 files, 103 tests); `npm run build` passed with 20 generated pages.
- Dev server note: this Codex sandbox rejected local port binding with `listen EPERM` for both `127.0.0.1:3000` and `127.0.0.1:3001`, and it could not stop the stale local Node listener on port 3000. Run `npm run dev` from a normal Mac terminal after pulling to verify `http://localhost:3000`.
- Push note: local commit was created, but `git push origin main` failed in this sandbox because DNS could not resolve `github.com`. Push from a normal network-enabled Mac terminal with `git push origin main`.
- Known issue: Antigravity local repo will need `git pull origin main` after this commit is pushed, because code changes were made in the writable VPMCO workspace.
- Next recommended iteration: Canvas Action Layer (`v0.1.55`) with trip completeness, Day quick actions, and prep blockers.
 
## v0.1.41 Handoff Update - Documentation Alignment Snapshot


- Current source-of-truth version: `v0.1.41` in `VERSIONING.md` and `package.json`.
- Completed since v0.1.34:
  - v0.1.35: English-only UI pass and bilingual place names.
  - v0.1.36: mobile layout overhaul with scrollable phone pages and six-tab mobile nav.
  - v0.1.37: multi-language i18n for EN/ES/AR/JA/KO/FR, including LanguageSwitcher persistence and Arabic RTL.
  - v0.1.38: standalone `/` landing home page.
  - v0.1.39: golden-line Chinese landscape background.
  - v0.1.40: compact single-viewport landing home page pass.
  - v0.1.41: Save to Trips fix using `supabase/migrations/0003_fix_auth_user_sync.sql`, `public.users` upsert before trip insert, non-fatal message append failures, and better save diagnostics.
- Production note: if not already applied in Supabase, run `supabase/migrations/0003_fix_auth_user_sync.sql` in Supabase SQL Editor so new `auth.users` rows automatically sync to `public.users` and existing users can upsert their own profile row.
- Documentation note: older Chinese sections still contain mojibake from previous encoding issues. The newer English addenda in PLAN, CHANGELOG, VERSIONING, and this handoff snapshot are the current operational source of truth until a full documentation rewrite is scheduled.
- Next recommended iteration: v0.1.42 Translator simplification. Merge Text/OCR/Voice/Phrases into one locale-aware translator workspace; translate between current site language and Chinese; use separate Upload Image / Take Photo buttons; simplify Voice to one record button; keep phrases and special terms as lightweight support content.

## v0.1.42 Handoff Update - Unified Translator and Panda Avatars

- Current version after this iteration: `v0.1.42`.
- `/translate` now renders `components/translate/UnifiedTranslator.tsx` instead of the old four-panel grid.
- Translator direction is active site language ↔ Chinese. Supported site languages remain EN/ES/AR/JA/KO/FR.
- Desktop Translator layout is a clean one-page desk: two equal source/output text panels across the top and a horizontal common-phrases/special-terms rail below.
- The visual surface is intentionally background-forward with hairline dividers and very low-opacity backing rather than heavy translucent cards.
- Image input has two actions: Upload Image and Take Photo. Take Photo is intentionally disabled on desktop and reserved for mobile camera capture.
- Voice input has one Record button. The old audio upload and public audio URL controls are no longer shown in the traveler UI.
- Desktop 1440x900 Playwright check should be rerun before final handoff to confirm body/html page scroll remains zero after the final visual pass.
- Account avatar selection now uses six new panda PNG assets under `public/avatars` while preserving the existing six avatar IDs for localStorage and community compatibility.

## v0.1.48 Handoff Update - Configured Models and Structured Butler Replies

- Current version after this iteration: `v0.1.48`.
- User confirmed Vercel provider keys are configured for DeepSeek, Zhipu, Moonshot, and Qwen. Registry defaults now target DeepSeek v4 flash, Qwen 3.6 Flash, Zhipu GLM5, and Moonshot Kimi 2.5.
- `lib/types/trip.ts` now adds optional structured assistant response data while preserving required `assistantMessage` text.
- `lib/ai/butlerPrompt.ts` now asks live providers for `{ headline, body, highlights, watchOut, nextStep }` and derives a safe fallback response when a provider returns the old plain shape.
- `components/chat/ChatPanel.tsx` renders structured VisePanda replies as compact guidance cards; historical/plain messages still render as ordinary text.
- Key files changed: `lib/ai/modelRegistry.ts`, `lib/env/placeholders.ts`, `lib/ai/butlerPrompt.ts`, `lib/types/trip.ts`, `components/chat/ButlerWorkspace.tsx`, `components/chat/ChatPanel.tsx`, `app/globals.css`, tests and docs.
- Verification: full `npm run test` passed (34 files, 97 tests); `npm run build` passed with 20 static pages generated.

## v0.1.49-v0.1.51 Handoff Update - Rich POI, Tool Context, Preference Memory

- Current version after this batch: `v0.1.51`.
- User asked to implement the next three recommended rounds in one pass.
- v0.1.49: Amap POI search now requests `extensions=all`; Nanjing is supported; `ExploreRichMeta` carries optional rating, price, phone, opening hours, photo, business area, source, and location fields; Explore cards render those fields when available.
- v0.1.50: Chat orchestrator now builds a bounded live Amap POI context for create/recommend/add/logistics intents and injects it into the model prompt before requesting a normal `CanvasPatch`.
- v0.1.51: Chat extracts a lightweight `UserPreferenceProfile` from natural language, persists it for guests in localStorage, sends it to `/api/chat`, and shows compact remembered-preference chips.
- Key files changed: `lib/explore/amapSearch.ts`, `app/api/explore/amap/route.ts`, `lib/explore/types.ts`, `lib/explore/amapProvider.ts`, `components/explore/ExploreBoard.tsx`, `lib/ai/toolContext.ts`, `lib/ai/preferenceProfile.ts`, `lib/ai/butlerPrompt.ts`, `lib/ai/orchestrator.ts`, `app/api/chat/route.ts`, `components/chat/ButlerWorkspace.tsx`, `components/chat/ChatPanel.tsx`, `app/globals.css`, tests and docs.
- Verification: full `npm run test` passed (35 files, 98 tests); `npm run build` passed with 20 static pages generated.

## v0.1.52 Handoff Update - Product Interaction Blueprint

- Current version after this iteration: `v0.1.52`.
- User intent: no product code changes this round; think deeply about the whole product's interaction logic, product positioning, user experience, linked features, page priorities, and write the planning into the repo docs.
- Product decision: VisePanda should be treated as a China travel operating system for foreign visitors, not only an AI itinerary generator. The product should reduce five anxieties: entry, payment, connectivity, language, and itinerary.
- New planning doc: `docs/planning/v0.1.52-product-interaction-blueprint.md`.
- Key thinking recorded:
  - Core loop: intent/archetype → preference extraction → live tools/data → Trip Canvas source of truth → Chat explanation → small next-step controls → Trips readiness/continuity.
  - Journey model: Curious → Planning → Preparing → In China → Share/Get help.
  - Page roles: Home starts archetypes; Chat is command center; Canvas is operational trip object; Trips is continuity/readiness/sharing; Explore feeds Chat/Canvas; Tools resolves anxieties as widgets/cards; Translate becomes an everywhere utility; Account is trust/preference/consent/lead capture; Community is proof and inspiration.
  - Roadmap was later shifted when `v0.1.54` implemented Interaction Shell I. Active next order: `v0.1.55` Canvas Action Layer, `v0.1.56` Inline Tool Cards, `v0.1.57` TripBlock POI Embedding + Day Detail Upgrade, `v0.1.58` Translate Everywhere, `v0.1.59` Tools Widgets I, `v0.1.60` Account Center + Preference Review, `v0.1.61` Admin + Customer Brief Planning/Build.
- Files changed: `docs/planning/v0.1.52-product-interaction-blueprint.md`, `PLAN.md`, `PRD.md`, `DESIGN.md`, `AGENTS.md`, `HANDOFF.md`, `CHANGELOG.md`, `VERSIONING.md`, `package.json`, `package-lock.json`.
- Product code intentionally untouched; only docs/version metadata changed.
- Push note: the user said they will push later. Local repo is expected to remain ahead of `origin/main` until the user pushes from a network-enabled terminal.

## v0.1.44 Handoff Update - Mobile Portrait Optimization

- Current version after this iteration: `v0.1.44`.
- All changes are CSS-only in `app/globals.css`; no component logic was modified.
- Bottom nav is now `position: fixed; bottom: 0` on mobile (`max-width: 760px`), turning the 6-icon tab strip into a thumb-friendly bottom bar. The app-header on mobile shows only brand mark, LanguageSwitcher, and AccountMenu.
- `.day-drawer-shell` on mobile becomes a full-width bottom sheet (`inset: auto 0 56px 0; height: 80dvh`) with rounded top corners and a drag-handle hint, replacing the 34vw right-side panel that was ~133px on a 390px phone.
- `.account-menu__popover` on mobile is constrained to `calc(100vw - 28px)` and `max-height: 82dvh` with `overflow-y: auto`, preventing clip on narrow screens.
- Explore city filter pills now scroll horizontally on mobile (`overflow-x: auto; flex-wrap: nowrap`); explore columns stack to a single column.
- `.app-shell` gets `padding-bottom: calc(64px + env(safe-area-inset-bottom, 0px))` to prevent the fixed bottom nav from covering page content.
- Trip detail header stacks vertically; trip summary actions (compact Trip Detail controls) switch to left-aligned.
- Tool cards get `min-height: 52px` for better touch targets.
- Key file: `app/globals.css` (v0.1.44 mobile optimization block at end of file).
- Verification: `npm run test` passed (31 files, 79 tests); `npm run build` passed with 20 static pages generated.

## v0.1.43 Handoff Update - Translator and Trip Detail Repair

- Current version after this iteration: `v0.1.43`.
- User intent: fix the still-broken Translator and make trip/day details read as details, not editing surfaces.
- `/api/translate/text` now attempts Qwen first and falls back to DeepSeek (`DEEPSEEK_API_KEY`/`AI_API_KEY`) before returning `translation_provider_unavailable`; this protects production if DashScope/Qwen is missing or failing while the existing DeepSeek key is available.
- `UnifiedTranslator` now surfaces a clearer configuration error when neither Qwen nor DeepSeek translation is available.
- `DayCard` now says `View details`; `DayDetailDrawer` is read-only and shows Morning/Afternoon/Evening descriptions, hotel, transport, and notes without editable inputs or a save button.
- Real Trip Detail pages that have a saved canvas now push Continue/Trips/status/archive/share actions into the Live Trip Canvas summary card as compact right-side controls, reducing top-of-page clutter so itinerary content appears earlier.
- Key files changed: `app/api/translate/text/route.ts`, `components/translate/UnifiedTranslator.tsx`, `components/canvas/TripSummary.tsx`, `components/canvas/TripCanvas.tsx`, `components/canvas/DayCard.tsx`, `components/canvas/DayDetailDrawer.tsx`, `components/trips/TripDetail.tsx`, `app/globals.css`, related tests and docs.
- Verification: targeted Vitest passed for `tests/trip-detail-actions.test.tsx`, `tests/canvas-components.test.tsx`, and `tests/qwen-translate-api.test.ts`; full `npm run test` passed (31 files, 79 tests); `npm run build` passed with 20 static pages generated.

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

1. `v0.1.55` Canvas Action Layer：增加 trip completeness、Day quick actions、prep blockers，让 Canvas 从展示行程升级为可操作行程。
2. `v0.1.56` Inline Tool Cards：把签证、支付、eSIM、汇率、应急等工具卡放进 Chat 回复流，减少用户跳 tab。
3. `v0.1.57` TripBlock POI Embedding + Day Detail Upgrade：把真实 POI 字段持久化到行程块，Day detail 显示中文地址、营业时间、电话、地图和“为什么适合你”。
4. `v0.1.58+`：Translate Everywhere、Tools Widgets、Account Center、Admin/Customer Brief 按 `PLAN.md` 的 v0.1.55-v0.1.61 顺序推进。

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
## v0.1.33 Handoff Update - Desktop Visual Layout Refresh

Current version: `v0.1.33`.

Completed in this iteration:

- Added approved visual-refresh spec at `docs/superpowers/specs/2026-06-30-visual-layout-refresh-design.md`.
- Added a v0.1.33 visual-system override layer in `app/globals.css` for tighter global shell/header/nav, Warm New Chinese paper/ink styling, smaller serif headings, compact cards, unified inputs, and desktop internal scrolling.
- Rebalanced `/chat`: smaller Live Trip Canvas title, compact two-row prompt chips, scrollable chat log, and separated composer / Save to Trips / status rows.
- Improved desktop density for `/trips`, `/explore`, `/tools`, `/translate`, and `/community` so headers and filters take less vertical space and main content gets more room.
- Cleaned visible page-level mojibake in Translator and Community labels, and normalized Translator Text/OCR/Voice control copy without changing Qwen API routes.
- Verified 1440x900 pages for `/chat`, `/trips`, `/explore`, `/tools`, `/translate`, and `/community`; body/main stayed locked to one viewport and Chat composer no longer overlapped Save to Trips.

Known constraints:

- This is still a desktop-landscape-first pass. Mobile portrait polish remains a later dedicated iteration.
- The v0.1.33 CSS layer is intentionally an override layer to reduce regression risk; a future design-system refactor may extract shared UI primitives once the direction is stable.
- Historical mojibake remains in some older docs/changelog entries, but current visible Translator/Community primary labels are cleaned.

Next recommended steps:

1. Validate the production Vercel deployment visually after `main` deploys.
2. Plan a mobile portrait layout pass or a shared component-system cleanup after this visual direction is approved in production.

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

## v0.1.45 Handoff Update - Intelligent Chat Pipeline & Data-Fusion Roadmap (Docs Only)

- Current version after this iteration: `v0.1.45`.
- This iteration changed **documentation only**. No code, provider, schema, or component changes. The runtime behavior is identical to v0.1.44.
- What was produced: a distilled seven-iteration roadmap (v0.1.46–v0.1.52) recorded across PRD (product direction + acceptance criteria), PLAN (阶段十二, tasks 12.1–12.33 + sequencing rationale), DESIGN (target architecture + ADR-037 through ADR-042), and AGENTS (implementation rules).
- The three research threads that fed this: (1) Amap/Meituan POI data enrichment and display, (2) Chat×Explore×Trips fusion via a tool-calling Butler + preference profile, (3) chat-efficiency/UX overhaul (intent routing, input refinement, response normalization, navigation restructure).

### Recommended next implementation iteration

- **v0.1.46 — Chat Intelligence Layer I** is the recommended next code iteration: intent classifier + routing + input refinement + response normalization schema, with non-LLM handlers for `ask_factual` and `preference_signal`. It improves every message for zero new external dependencies and is fully independent of any pending API approval.

### External actions the user should start now (multi-week lead time)

**Apply for 大众点评开放平台 (Dianping Open Platform) — the realistic Meituan-group data path**

Meituan does not offer a general public POI API like Google Places. For a foreign-facing travel app, the accessible path is Dianping (owned by Meituan Group), which has an individual-developer track and provides POI name, rating, review count, price level, business hours, category, photos, and address.

1. Prepare identity: a Chinese business license (营业执照, fastest, full quota) OR personal real-name verification (身份证 + Chinese 手机号, slower, lower quota). For a US-based entity, start with the personal-verification path.
2. Register at `https://open.dianping.com` → 登录/注册 with a Chinese phone → complete 实名认证 (submit ID). Identity review: ~1–3 business days.
3. Create an application: 我的应用 → 创建应用. Category = 旅游/出行. Callback URL = `https://go2china.space/api/auth/dianping/callback`. Request API products: 商户搜索 (POI search), 商户详情 (full details incl. rating/photos), optionally 团购/优惠 (deals). Submit for review: ~3–7 business days.
4. On approval you receive `app_key` and `app_secret`. Store them as `DIANPING_APP_KEY` / `DIANPING_APP_SECRET` in Vercel env vars — server-side only, never exposed to the browser.
5. Usage shape: `GET https://api.dianping.com/v1/business/find_businesses?appkey=…&city=北京&category_id=…&limit=20&sign=<HMAC-MD5 of sorted params + app_secret>`. Response includes `rating` (0–5), `review_count`, `avg_price`, `open_time`, `photo_list[].url`.

Alternatives if Dianping approval is slow: (a) Amap enriched fields — already licensed under the existing `AMAP_API_KEY`, zero extra cost, unlocked in v0.1.48; (b) Trip.com (携程) open API — English docs, travel-partner friendly, covers hotels/attractions/restaurants with booking CTAs.

**Register an Amap JS Maps key (for v0.1.52 map widget)**

- In the same Amap console, create a **Web端(JS API)** key (separate from the existing Web Service POI key). Add `go2china.space` to the Referer whitelist. This becomes `NEXT_PUBLIC_AMAP_MAPS_KEY` — it is display-only and safe to expose because it is domain-locked. The billable POI key (`AMAP_API_KEY`) stays server-side.

**Confirm DeepSeek function-calling support (for v0.1.49 tool loop)**

- `deepseek-chat` (V3) supports OpenAI-compatible function calling. If the current `deepseek-v4-flash` model does not, plan to use `deepseek-chat` for the tool-calling loop and keep Flash for simple adjustments. Verify against the DeepSeek account/plan before starting v0.1.49.

### Standing security constraints (unchanged, reaffirmed)

- All keys server-side only: `DEEPSEEK_API_KEY`, `DASHSCOPE_API_KEY`, `EXCHANGE_RATE_API_KEY`, `AMAP_API_KEY`, future `DIANPING_APP_KEY`/`DIANPING_APP_SECRET`. Never write real keys into the repo or docs.
- `NEXT_PUBLIC_AMAP_MAPS_KEY` is the only public key permitted, and only because it is domain-whitelisted and display-only.
- Never remove the mock/static fallback; real integrations must always degrade gracefully.
- `SUPABASE_SERVICE_ROLE_KEY` must not be introduced into any browser-side code.

## v0.1.46 Handoff Update - Product Expansion Roadmap (Docs Only)

- Current version after this iteration: `v0.1.46`.
- Documentation only — no code, provider, schema, or component changes. Runtime behavior is identical to v0.1.45.
- Authoritative deep-dive added: `docs/planning/v0.1.46-product-expansion.md` (seven tracks, full detail). Summaries + decisions recorded in PRD (Requirements G–L), PLAN (阶段十三–十八 + Supabase task S.1), DESIGN (ADR-043–ADR-049), and AGENTS (v0.1.46 rules).

### What changed at the direction level

- **Guiding principle:** optimize for UX and answer quality; token/compute cost is explicitly not a constraint. The intent classifier is retained but repurposed for quality/correctness routing, not cost savings.
- Seven tracks planned: (1) multi-model Chinese LLM orchestration, (2) native iOS/Android apps, (3) Tools functional upgrade, (4) professional Account UI + lead capture, (5) admin backend + LLM customer briefs, (6) Supabase schema additions, (7) frontend/visual optimization.

### Recommended build sequence

- Multi-LLM orchestration → Tools functional upgrade → Account + lead capture → Admin backend + briefs → design-system pass (continuous) → native apps (last, largest). Version numbers assigned when each is built; the order is the commitment.

### External / long-lead-time actions the user should start now

**Additional Chinese LLM accounts (for multi-model orchestration)**

- Register developer keys to trial: Zhipu AI (智谱, GLM) `open.bigmodel.cn`; Moonshot (Kimi) `platform.moonshot.cn`; Baidu 千帆/ERNIE `console.bce.baidu.com`; optionally MiniMax. Store as `ZHIPU_API_KEY`, `MOONSHOT_API_KEY`, `ERNIE_API_KEY`/`BAIDU_*`, `MINIMAX_API_KEY` in Vercel — server-side only. Qwen/DashScope is already configured.

**Native app prerequisites (if confirmed — multi-week/month lead times)**

- Decide the stack: React Native + Expo (recommended, reuses the TypeScript core) vs. fully native Swift/Kotlin.
- Enroll Apple Developer Program ($99/yr) and Google Play Console ($25 one-time).
- Start the China distribution track early: ICP 备案, software copyright 软著, and MIIT app registration. These gate Chinese app-store release and take the longest.

**Business / data prerequisites (for lead capture + admin)**

- Decide the concierge follow-up workflow (who contacts leads, via what channel — note WeChat is often the real channel for China travel).
- Confirm PIPL/GDPR posture for storing traveler PII (consent copy, retention window, export/delete process). The `0004_leads_and_admin.sql` migration will need to be run in the Supabase SQL Editor when the admin/lead-capture work is built.
- Designate at least one `admin` user for the `/admin` backend.

### Standing security constraints (reaffirmed for the new scope)

- New LLM keys and `SUPABASE_SERVICE_ROLE_KEY` are server-side only; the service-role key must never enter any client bundle (admin routes are server-side only).
- Lead/PII data requires explicit, timestamped, source-tagged consent; passport/document fields require opt-in and encryption.
- The mock/static fallback is never removed; multi-model failure still degrades to the mock Butler.

## v0.1.47 Handoff Update - Multi-LLM Butler Orchestrator (first code iteration)

- Current version after this iteration: `v0.1.47`.
- The Butler now uses a real multi-model orchestrator (`lib/ai/orchestrator.ts`) over six Chinese LLMs. `/api/chat` was switched from the single DeepSeek call to `requestOrchestratedButlerPatch`. 95 tests pass; production build succeeds.
- **No regression risk without keys:** with zero LLM keys configured, the app behaves exactly as before (mock Butler). Each key you add turns on that model automatically — no redeploy of logic needed.
- New docs: `docs/planning/mock-inventory.md` (every mock/placeholder + real-replacement plan) and `CLAUDE.md` (project memory: VPCC on explicit request only; all md docs updated + pushed every iteration; beginner tutorials for manual steps).

### Project rules recorded this iteration (in `CLAUDE.md`)

- VPCC is not automatic — it runs only when you explicitly ask ("执行vpcc" / "/vpcc").
- Every iteration still updates all md docs in detail (especially HANDOFF/PLAN/PRD) and pushes to the feature branch + `origin/main`.
- Every manual step you must do yourself comes with a numbered, click-by-click beginner tutorial (see below).

### 手把手教程：如何添加中国 LLM 的 API Key（技术小白版）

下面每个模型都是**可选**的：你加了哪个，Butler 就会用哪个；一个都不加也能正常运行（用 mock）。**强烈建议至少配置 DeepSeek**（已在用）和 **Qwen/通义千问**（你已有 DashScope key）。其余按需增加，答案质量会更好。

**第 1 步：先理解"环境变量"是什么（1 句话）**
- 环境变量 = 存放在 Vercel 服务器上的"密码本"，代码运行时才去读它。它不会出现在网页里，也不会进代码仓库，所以安全。你要做的只是把各家给你的 key 复制粘贴到 Vercel 后台。

**第 2 步：去各家官网申请 key（选你想用的）**

1. **DeepSeek（已在用，若没配就配）**
   - 打开 `https://platform.deepseek.com` → 注册/登录 → 左侧「API Keys」→「Create API Key」→ 复制那串以 `sk-` 开头的字符。
   - 环境变量名：`DEEPSEEK_API_KEY`
2. **通义千问 Qwen（阿里云百炼，你已有 `DASHSCOPE_API_KEY`）**
   - 已配置则无需再做。Butler 聊天会用 `qwen-plus`；如果想换模型，可加 `QWEN_CHAT_MODEL`。
3. **智谱 GLM**
   - 打开 `https://open.bigmodel.cn` → 注册/登录（手机号）→ 右上角头像「API keys」→ 创建 → 复制。
   - 环境变量名：`ZHIPU_API_KEY`
4. **月之暗面 Kimi（Moonshot）**
   - 打开 `https://platform.moonshot.cn` → 注册/登录 → 「API Key 管理」→ 新建 → 复制。
   - 环境变量名：`MOONSHOT_API_KEY`
5. **百度文心 ERNIE（千帆）**
   - 打开 `https://console.bce.baidu.com/qianfan/` → 登录 → 开通「千帆大模型平台」→ 「应用接入 / API Key」创建 v2 兼容 key → 复制。
   - 环境变量名：`ERNIE_API_KEY`
6. **MiniMax（可选）**
   - 打开 `https://platform.minimax.chat` → 登录 → 账户里找到 API Key → 复制。
   - 环境变量名：`MINIMAX_API_KEY`

**第 3 步：把 key 填进 Vercel（每个 key 都是同样的操作）**

1. 打开 `https://vercel.com` 并登录，进入 VisePanda 这个项目。
2. 顶部点 **Settings**（设置）。
3. 左侧点 **Environment Variables**（环境变量）。
4. 在 **Key** 框里填变量名（例如 `ZHIPU_API_KEY`），在 **Value** 框里粘贴你复制的那串 key。
5. **Environment** 选择 **Production, Preview, Development** 三个都勾（默认全选即可）。
6. 点 **Save**（保存）。
7. 每加一个模型，就把第 4–6 步重复一次。

**第 4 步：让改动生效（重新部署）**

1. 在 Vercel 项目顶部点 **Deployments**（部署）。
2. 找到最上面那条最新部署，点右边的「⋯」菜单 → **Redeploy**（重新部署）→ 确认。
3. 等待约 1–2 分钟变成绿色 **Ready**。
4. 打开 `https://go2china.space/chat`，随便发一句「帮我规划 5 天中国行程」，屏幕下方状态会显示是哪个模型回复的（例如 "Qwen Plus" 或 "DeepSeek V4 Flash"）。看到真实模型名 = 配置成功。

**常见问题**
- 状态一直显示 "mock fallback"？→ 说明一个 key 都没读到：检查变量名有没有拼错、是否点了 Save、是否 Redeploy。
- 只想用一个模型？→ 完全可以，配 1 个就行，其余不填。
- key 会泄露吗？→ 不会。它只存在 Vercel 服务器端，永远不会出现在浏览器或代码里。

### Recommended next iteration

- **Chat Intelligence Layer / response normalization** (阶段十二): add the `{headline, body, highlights, watchOut, nextStep}` schema so answers are consistent and scannable, then layer the refine-and-verify loop (task 13.5) on top of the orchestrator. Alternatively, **Tools functional upgrade** (阶段十五) for immediate, self-contained traveler value. See `docs/planning/mock-inventory.md` for the full replace-the-mocks queue.

## v0.1.55 Handoff Update - UX Layout & Frontend Design Spec (docs only)

- Current version after this iteration: `v0.1.55`.
- Documentation only. Added `docs/planning/ux-design-and-layout-spec.md`, the design/experience companion to the v0.1.52 interaction blueprint and v0.1.53 technical blueprint. No product code changed.

### ⚠️ Parallel-session / version-collision note (important for the operator)

- This session discovered that `origin/main` had already advanced to **v0.1.54** via a **parallel session** (it did the FIT one-stop positioning, model activation, structured chat replies, rich POI context, preference memory, and the interaction shell).
- To avoid destroying that work, this session **synced onto `origin/main` first** (dropping a now-redundant local v0.1.48 model-selection commit — the parallel session's own v0.1.48 already activated the same models) and then added this docs-only iteration on top. **No history was overwritten.**
- **Risk:** two sessions both auto-increment `0.1.x` and both push to `main`, so version numbers can collide (this already happened once at v0.1.48). The roadmap reserves `v0.1.55` for the *Canvas Action Layer* code phase; this docs iteration also took `v0.1.55`. If the other session ships Canvas Action Layer, reconcile by renumbering one of them.
- **Recommendation:** run one session at a time, OR give each session a version lane (e.g. code sessions use even patch numbers, planning sessions use a `-docs` suffix), OR designate this session for design/docs and the other for feature code. Awaiting the operator's preference.

### What this doc adds (not a duplicate of v0.1.52/v0.1.53)

- v0.1.52 = interaction blueprint (what/why); v0.1.53 = plugin/technical architecture; **v0.1.55 = concrete UX layout + component interaction mechanics + frontend visual design system** (the "how it looks and lays out" layer), plus a table mapping each roadmap phase to its governing design section.

## v0.2.2 Handoff Update - Chat Core-Loop Fixes (speed, sync, auto-save)

- Current version after this iteration: `v0.2.2`.
- Fixes three reported problems in the Chat↔Canvas loop. All keep the mock fallback and keys server-side.

### 1. Slow replies → parallel racing + timeouts
- The orchestrator (`lib/ai/orchestrator.ts`) now races all configured providers in parallel with `Promise.any` (first valid patch wins) instead of sequential attempts.
- Each provider call has an 18s abort timeout (`lib/ai/providers/openaiCompatibleProvider.ts`); the Amap tool-context prefetch is bounded to 6s and best-effort.
- Result: reply latency ≈ the fastest healthy model, and a wrong/hung model can't stall the chat.

### 2. Chat and Live Canvas not syncing → destination-aware fallback + stricter prompt
- The mock butler (`lib/mock-ai/mockButler.ts`) now detects cities (EN + 中文) and a day/week count in the message and generates a matching skeleton itinerary, so the canvas always reflects the chat even when live models fail.
- The system prompt now requires live models to return the COMPLETE `days` array on any itinerary change.
- **If the canvas still doesn't update with live models on, the likely cause is a wrong model id** (see the v0.1.48 tutorial): set `ZHIPU_CHAT_MODEL` / `MOONSHOT_CHAT_MODEL` / `QWEN_CHAT_MODEL` in Vercel to the exact model id from each provider's console, then Redeploy. Until then the destination-aware fallback keeps the canvas in sync.

### 3. Auto-save + button removed
- Every chat auto-saves to Trips for signed-in users (silent "Saved to your Trips." note in the status line). The manual "Save to Trips" button is gone. Guests keep the automatic localStorage draft. Sign-in sync and auto-save no longer double-write.

### Next three iterations (planned)
- **v0.2.3 — Canvas Action Layer**: trip-completeness score + progress meter in `TripSummary`; Day quick-actions (Lighten / Add food / Swap morning / Add rest) sending structured Butler intents; prep blockers surfaced on the canvas.
- **v0.2.4 — Inline Tool Cards + factual fast-path**: `ask_factual` answered instantly from `lib/tools` static data as inline cards in Chat (also speeds those replies by skipping the LLM); "Add reminder / Mark as done" hooks into trip prep.
- **v0.2.5 — Tools Interactive Widgets I**: currency converter (live rate), visa eligibility checker, payment setup wizard, via an optional `interactive` descriptor on `ToolCategory` (static checklist stays as fallback).

## v0.2.3 交接更新 —— 整体规划 + 前端 UI 优化路线(纯文档)

- 本轮版本:`v0.2.3`,纯文档,产品代码零改动,运行时行为与 v0.2.2 完全一致。
- 权威新文档:`docs/planning/v0.2.3-ui-optimization-roadmap.md`(宏观差距审计 G1–G10、逐界面微观优化清单、设计系统迭代规划、后三轮执行承诺)。
- 新操作者规则已入项目记忆:后续所有思考/回答/汇报一律中文。

### 后三轮执行承诺(版本重排:规划占用 v0.2.3,代码轮顺延)

1. **v0.2.4 —— Canvas 行动层 + 画布 UI 升级**:完成度评分与进度条、Day 卡快捷动作(减负/加美食/换上午/加休息,走 AI 管道)、出发准备聚合区、patch 动画与缺口 chips。验收:点"减负"3 秒内画布可见变化且变化处高亮。
2. **v0.2.5 —— 对话体验重塑 + 内联工具卡**:ask_factual 快通道(<150ms 直出签证/支付/eSIM/汇率/地铁/应急静态卡,跳过 LLM)、MessageBlock 分块渲染、乐观 UI + 打字指示 + 骨架屏、>8s 安抚文案、模型标签弱化。验收:问签证秒出卡片并可一键加入行程提醒。
3. **v0.2.6 —— 设计系统收口 + Tools 交互组件**:token 层 + 组件库归一(Button/Pill/Card/Sheet/Toast/ProgressMeter/MessageBlock/ToolCard/EmptyState);Tools 三件套 widget(汇率换算器/签证资格问答器/支付设置向导),静态清单降级保留。验收:三工具桌面+移动可完成真实任务,新组件 ≥3 页复用。

### 操作者无需任何手动步骤

- 本轮及后三轮均不需要新 API key、不需要 Vercel/Supabase 操作。之前待办不变:如画布仍偶发不联动,按 v0.1.48 教程核对 `*_CHAT_MODEL` 精确模型 ID。


## v0.2.4 交接更新 —— UI/交互深化规格 + 实现交接提示词(纯文档)

- 本轮版本:`v0.2.4`,纯文档,零代码,运行时与 v0.2.2 一致。
- 两份新文档:
  1. `docs/planning/v0.2.4-interaction-deep-dive.md` —— 后三轮的**交互验收标准**(变更摘要卡、双向悬停联动、patch 演出、撤销、composer/MessageBlock/Day 卡/完成度条/准备区组件级规格、字体色彩细则、动效参数总表、移动手势、无障碍底线)。
  2. `docs/planning/handoff-prompt-for-coding-agent.md` —— **可直接复制给其他 coding agent 的实现提示词**(自包含:背景/必读文档/八条硬约束/三轮任务规格/验收)。操作者用法:整段复制「======」之间内容发给实现 agent 即可。
- 版本重排(当时):代码三轮曾定为 **v0.2.5 Canvas 行动层+画布交互 → v0.2.6 Chat 体验重塑+内联工具卡 → v0.2.7 设计系统收口+Tools 交互组件**;该编号已在 v0.2.5 融合轮再次顺延,最新以 v0.2.5 交接更新为准。
- 操作者无需任何手动步骤;实现可由本会话继续,也可把提示词交给其他 agent。

## v0.2.5 交接更新 —— 规划融合 + FIT Travel Desk Readiness Seed

- 本轮版本:`v0.2.5`。
- 用户意图:再次读取 GitHub/repo 更新,把前几轮 UI/产品设计规划融入当前 repo 主线,并修正后续建议路线后汇报。
- 同步发现:本地存在未推送提交 `47eba46 feat(ui): polish fit travel desk`,远端已推进到 `5414c7e v0.2.4`。本轮没有运行会强制 reset 的 VPMCO 脚本,而是安全 fetch/merge,保留两边工作。
- 融合结论:
  - 远端 `v0.2.4` 的交互深化规格仍是最高优先级的实现验收标准。
  - 本地 UI polish 的 readiness、summary/readiness rail、Chat starter state、Home launcher polish 作为 `0.2.x` 主线的 seed 保留。
  - 当前 readiness 是派生展示,不是完整 completion schema;完整 Canvas 行动层仍未完成。
- 最新后续路线:
  1. `v0.2.6` Canvas 行动层+画布交互:完成度纯函数、可点缺口、Day 快捷动作、Change Digest、patch 演出、undo、Before you fly。
  2. `v0.2.7` Chat 体验重塑+内联工具卡:MessageBlock、等待叙事、composer、ask_factual 快通道、实体 chip 联动。
  3. `v0.2.8` 设计系统收口+Tools widgets:token/组件库、汇率换算、签证问答、支付向导、移动 Chat sheet。
- 已更新:VERSIONING/CHANGELOG/PLAN/PRD/DESIGN/AGENTS/HANDOFF,以及 `docs/planning/v0.2.4-interaction-deep-dive.md` 和 `docs/planning/handoff-prompt-for-coding-agent.md` 的版本路线。
- 下一步:跑测试和构建确认 merge 后状态;若通过,提交并尝试推送。


## v0.2.6 交接更新 —— FlyAI(飞猪)Skill 研究 + 项目级开发工具接入

- 本轮版本:`v0.2.6`。不改产品运行时代码;新增一个开发工具向的项目级 Claude Code Skill。
- 用户意图:研究 `https://github.com/alibaba-flyai/flyai-skill`(飞猪官方 Agent Skill),把它加入 VisePanda 项目,并说明可以在哪些现有功能里用上。
- 已逐字核实(非猜测)的关键事实:
  - flyai-cli 是通过 **MCP streamable_http 协议**连接飞猪官方托管服务的瘦客户端(npm 包 `@fly-ai/flyai-cli`,仅依赖 `commander`)。
  - 已发布包内嵌阿里官方**默认试用凭证**(构建期写入),每次请求携带 `x-ff-ctx` **设备指纹头**(gzip+AES-256-GCM,用于风控反滥用)——这是共享试用额度,不是 VisePanda 专属配额。
  - 没有任何公开文档描述面向第三方网站后端的**生产级 API 合作申请流程**(对比 Dianping 开放平台有明确注册审核路径)。
- 判断结论:**这目前是开发者/Agent 工具,不是可直接嵌入生产后端的公开 API**。绕过官方渠道复用内嵌凭证违反其风控设计,且与我们对 Dianping/Meituan 的既定原则相悖(先官方申请、不逆向/爬取)。生产集成必须等飞猪官方合作确认。
- 已落地(零风险,立即可用):将上游 `skills/flyai/`(SKILL.md + 8 份 references,MIT 许可)原文 vendor 进 `.claude/skills/flyai/`,附 `LICENSE-NOTICE.md` 说明使用边界——仅供开发阶段研究/内容生产(丰富 Explore/Tools 静态 fallback 内容、验证行程文案事实合理性、为未来真实 provider 设计数据模型参考样例 JSON),**禁止**任何生产代码调用。任何以后打开本仓库的 coding agent 会话都能直接用。
- 已产出 `docs/planning/flyai-skill-integration.md`:含 8 子命令精确参数/输出字段表、逐项功能映射(Tools 地铁分类补齐城市间交通空白、Tools 签证/eSIM、Explore 住宿/景点补齐真实价格与预订链接、Chat 工具调用、Canvas Day 详情 stay/transport 字段)。
- `docs/planning/mock-inventory.md` 新增第 23 项(真实预订数据),🔴,目标"飞猪官方合作确认后"。
- **版本编号说明(重要)**:本轮为纯规划+开发工具轮,不消耗"Canvas 行动层"等实现轮编号。此前 v0.2.5 融合轮定的完整代码三轮(v0.2.6 Canvas 行动层 → v0.2.7 Chat 体验 → v0.2.8 设计系统)因本轮占用 v0.2.6,**再顺延一位为 v0.2.7 / v0.2.8 / v0.2.9**,任务内容不变。
- 如需推进生产级飞猪合作:联系 `flyai@alibaba-inc.com` 或 `https://open.fly.ai/`,咨询第三方网站服务端 API 合作方案,不要复用 CLI 内嵌的共享试用凭证。本轮不需要任何 Vercel 环境变量配置。

### 当时的后三轮编号(已被后续 v0.2.7-v0.2.10 实际实现覆盖)

1. `v0.2.7` Canvas 行动层+画布交互:完成度纯函数、可点缺口、Day 快捷动作(带天数)、Change Digest 变更摘要卡、patch 演出、撤销、Before you fly 准备区(在现有 v0.2.5 readiness seed 基础上补全为完整 completion schema)。
2. `v0.2.8` Chat 体验重塑+内联工具卡:MessageBlock 分块渲染、composer 规格、等待叙事、`ask_factual` <150ms 快通道、实体 chip 双向悬停联动。
3. `v0.2.9` 设计系统收口+Tools 交互组件:token 层+组件库归一、Tools 三件套 widget(汇率换算/签证问答/支付向导)、移动 Chat sheet。

当前真实状态以文件顶部 `v0.2.14 Handoff Update` 为准:`v0.2.9` 已用于 Chat factual fast-path + inline Tools cards,`v0.2.10` 已用于 Tools Widgets I,`v0.2.11` 已用于 Frontend Design Resource Stack 配置,`v0.2.12` 已用于交接文档/版本线统一,`v0.2.13` 已用于 TripBlock POI / Day detail operational upgrade,`v0.2.14` 已用于 Real POI context write-through + booking candidate model。后续建议为 `v0.2.15` Explore Add-to-Trip POI write-through。


## v0.2.7 交接更新 —— Canvas 行动层(第一轮代码实现)

- 本轮版本：`v0.2.7`。这是继 v0.2.2 核心环路修复之后的第一轮**真正的功能代码**迭代（v0.2.3–v0.2.6 都是纯文档规划轮）。
- 交付内容：六维完成度（`lib/trips/completeness.ts`）、变更摘要卡 + diff（`lib/canvas/diffTripState.ts` + `ChangeDigestCard.tsx`）、Day 卡快捷动作（`lib/canvas/quickActions.ts`）、patch 演出动画（新增可重放动效 hook `lib/canvas/useReplayableAnimation.ts`）、撤销、出发准备区（`PrepChecklist.tsx`）。22 个新测试，全部 122 测试通过，构建成功。

### 一个值得记录的架构决策偏离：Undo 为什么没有按规格文档走 AI 管道

`docs/planning/v0.2.4-interaction-deep-dive.md` 原规格写的是"点击撤销发送预制意图 `Undo the last change...`，走既有 AI 管道；若 AI 通道失败，本地快照直接回滚"。实现时我判断这样做**不可靠**：LLM 在没有被喂入"撤销前的权威 TripState"作为上下文的情况下，无法可靠地精确重建出撤销前的行程——它只会即兴生成一个"看起来更宽松"但不是原样的版本，这不是真正的"撤销"，反而会让一个标着"撤销"的按钮做出不可预测的事。所以我把**本地确定性回滚**从"AI 失败时的兜底"提升为**唯一路径**：立即、可靠、零延迟地恢复上一份快照，并追加一条本地生成的确认消息（不调用 `/api/chat`）。这个决策已记录为 DESIGN.md ADR-070，并在 AGENTS.md 里更新了"仅允许 undo 本地直改"的例外条款说明。如果你认为应该严格按原规格走 AI 路径，这是可以讨论调整的一处已知偏离，不是疏漏。

### 修复的架构缺陷（顺带发现，非本轮原计划）

排查一个"摘要卡已出现但 Day 卡内容未同步"的时序竞态（只在特定并发测试场景下复现）时，定位到 `TripCanvas` 内部维护了一份通过 `useEffect` 从 `trip` prop 同步的 `editableTrip` 本地状态——这是 v0.1.43 抽屉从可编辑改为只读之前的历史遗留，早已没有存在必要，却引入了一次多余的渲染周期，是一处真实的、潜伏的正确性风险。已移除，`TripCanvas` 现在直接渲染 `trip` prop。

### 下一步（按已确认的顺延编号）

1. `v0.2.8` Chat/Canvas 视觉重设计：已完成。
2. `v0.2.9` Chat factual fast-path + inline Tools cards：已完成。
3. `v0.2.10` Tools Widgets I：已完成。
4. `v0.2.11` Frontend Design Resource Stack 配置：已完成。
5. `v0.2.12` 文档/版本交接统一：已完成。
6. `v0.2.13` TripBlock POI embedding + Day detail operational upgrade：已完成。
7. `v0.2.14` Real POI context write-through + booking candidate model：已完成。
8. `v0.2.15` Explore Add-to-Trip POI write-through。

### 操作者无需任何手动步骤

本轮不需要新 API key、不需要 Vercel/Supabase 操作。
