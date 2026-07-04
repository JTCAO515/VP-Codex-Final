# VisePanda Changelog

## v0.3.18 - 2026-07-04

**LLM 修复轮 + 生产环境首次真实 AI 行程生成。** 操作者提供三家真实 key(Qwen 专属网关/智谱/Kimi),实测根治:Qwen 与 GLM-5.x 默认思考模式烧光延迟预算(禁用后 1.9s/6.6s 直出 JSON);kimi-k2.x 只接受 temperature=1(400 根因);Qwen 专属网关正确路径为 `/compatible-mode/v1`。四家真跑后又暴露解析边界缺口:LLM 返回的 day 缺 `blocks` 字段导致 write-through 崩溃丢弃获胜 patch —— `normalizeDays` 在解析边界归一化修复(与 Android TripJson.normalizeNulls 同一思想)。合并 main 上并行会话的模型名升级(glm-5.1/kimi-k2.6/qwen3.7-plus),文档冲突仲裁为架构师版本。**生产实测:`mode:"zhipu"`,完整 2 天行程,零降级 —— 项目生产历史上第一次真实 AI 生成行程。** 173 测试通过。

## v0.3.17 - 2026-07-04

**Chatbot 可靠性 + 质量轮（架构师执行的 Web 冻结例外，操作者直接指令）。** 根治 2026-07-04 生产连通性审计发现的三家 LLM provider 全病问题，同时升级提示词与对话体验。详见 DESIGN.md ADR-119。

### 可靠性根治（对应审计发现的三个真实故障）
- **JSON 截断容错**（新建 `lib/ai/jsonRepair.ts`）：DeepSeek 生产报 "Unterminated string in JSON at position 5212" 的根因是行程类回复被 max_tokens 截断后 `JSON.parse` 裸炸。现在解析先剥 markdown 围栏/前导废话，再闭合截断处的字符串和括号栈，仍失败则逐步回退到上一个结构边界重试——挽救最长合法前缀而不是丢弃整个回答。
- **按意图预算**：行程类意图（create/adjust/add_location/add_poi）4096 tokens / 25s，轻量意图 1400 / 15s——替换原来一刀切的 2200/18s（3 天行程必然截断的元凶）。
- **Provider 熔断器**：连续 2 次失败进入 120s 冷却，期间不再参赛（原来病号每轮都白白拖满超时）；若熔断会清空候选池则自动忽略——永远不会把自己锁死在 mock 模式。诚实记录 serverless 局限：状态按 warm 实例存续，冷启动重置。

### 生成质量
- **系统提示词宪法化分层**：人设（懂行的本地朋友，不油腻）+ 六条硬规则（不编造中国事实、预订仅信息、每轮最多一个澄清问题、回复语言跟随用户、危难优先、接受纠正不狡辩）+ 输出契约 + 长度自适应。
- **create_trip 质量闸门**：嘴上说"帮你规划好了"但没返回 days 数组的 patch 直接判无效，让真正干活的 provider 赢得竞速。
- **建议问题缺口驱动**：fallback 建议优先反映行程真实缺口（没排天数/没选酒店/高优先级提醒未完成），模板句垫底。
- **Prompt 瘦身**：行程 payload 剥离 photoUrl（纯展示字段，长 CDN URL 白耗 token）；jsonMode 温度降到 0.3 稳定结构。

### 安全体验
- **急难词快速通道**：robbed/stolen/injured/lost passport 等词汇现在正确分类为 concern 并触发应急工具卡（110/120/使馆信息），不再可能落进普通推荐路径。

### 验证
- 167 个单元测试全部通过（新增 18 个：jsonRepair 13 个、orchestrator 熔断/闸门/截断恢复 5 个、intentClassifier 急难词 1 组）；`npm run build` 通过。

## v0.3.12 - 2026-07-03

**Chat 真实 API 根因修复:一直显示离线兜底不是因为没接好,而是超时设置太短。** 操作者要求:"将web端已经配好的chat 的api接入，将网页端chat的功能和配置全部导入apk"。

### 调研:两条并行研究线,对比 Web 端 `/api/chat` 与 Android 端现状
分别彻底梳理了 Web 端 `/api/chat` 路由(6 个 OpenAI 兼容 provider 并行竞速、18 秒单 provider 超时、确定性 mock 兜底、`ask_factual` 事实问题快速通道会跳过 LLM 直接返回 `InlineToolCard`、完整的 `CanvasPatch`/`AssistantResponse` 契约)和 Android 端现状(`ButlerApiService`/`RoomTripRepository`/`CanvasPatchApplier`/`NativeButlerFallback`,自 v0.3.6 起就已经接上)。逐字段对比后发现:请求/响应契约几乎完全对齐,唯一真实缺口是 `AssistantResponse.toolCards` 字段——Android 的数据类里根本没有对应属性(Gson 反序列化时会静默丢弃未知 JSON 字段,所以这不是解析错误,只是数据被悄悄丢掉)。

### 真正的根因:OkHttp 默认 10 秒超时,远短于生产环境真实延迟
直接 `curl` 生产环境 `/api/chat` 能拿到真实 DeepSeek 回复,证明后端配置完全正常。但计时后发现:一次涉及完整 mock 行程数据的请求耗时 **14.4 秒**;通过新加的调试期 `HttpLoggingInterceptor` 在 Android App 里实测同一类请求,耗时 **20.2 秒**。而 `di/AppModule.kt` 里的 `OkHttpClient` 用的是裸的默认值(connect/read/write 全部 10 秒)。这意味着:**从 v0.3.6 到现在,这个项目里每一次真实的 chat 请求都在服务器还没来得及响应之前就被客户端超时掐断**,`RoomTripRepository.sendButlerMessage()` 用 `runCatching` 包住了这次调用,超时异常被无声地转换成 `NativeButlerFallback` 的离线兜底文案——这正是为什么整个项目历史上,每一次手动验收看到的都是"Saved for the Butler",从来没有一次是真实 AI 回复。

### 修复
- `di/AppModule.kt`:`provideOkHttpClient()` 显式设置 `connectTimeout(15s)`/`readTimeout(45s)`/`writeTimeout(15s)`,45 秒的读超时在实测约 20 秒最坏情况和后端 orchestrator 自身约 18 秒单 provider 上限之上留了余量。同时加了 `HttpLoggingInterceptor`(仅 `BuildConfig.DEBUG` 时启用,`Level.BODY`),往后再遇到连接问题能直接看 logcat,不用再靠猜。
- `data/model/ButlerModels.kt`:新增 `InlineToolCard`(`id`/`categoryId`/`title`/`summary`/`items`/`nextAction`/`href`/`tone`/`sourceLabel`)和 `InlineToolCardTone` 枚举,`AssistantResponse` 新增 `toolCards: List<InlineToolCard>? = null` 字段,和 Web 端 `lib/types/trip.ts` 逐字段对齐。
- `ui/butler/ButlerScreen.kt`:`MessageBubble` 新增 `InlineToolCardView`,把每张 tool card 渲染成消息气泡内嵌的卡片(标题/摘要/要点列表/`nextAction`)。`href` 深链到 Tools 分类页**特意没有接**——`ui/tools/ToolsScreen.kt` 目前还是诚实占位页(要等 Translator 轮才会实现),所以 `nextAction` 渲染成普通文字标签,不是一个点了没反应的假按钮。
- `android/app/build.gradle.kts`:顺手修复了一个放了好几轮都没更新的遗留问题——`versionCode`/`versionName` 自 v0.3.6 起就一直停在 `1`/`"0.3.6"`,这次更新为 `12`/`"0.3.12"`,和仓库整体版本号对齐。

### 明确不在本轮范围内(记录原因,不是遗漏)
- **`preferenceProfile`**(Web 端基于正则的静默饮食/预算/节奏/兴趣提取系统,`lib/ai/preferenceProfile.ts`):这是一整套独立子系统,不是一个配置项;且该字段在请求里是可选的,不传不会导致任何错误。
- **`intent`/`strategy`/`providersTried`/`toolContext`** 这几个响应字段:Web 端只是用来给自己的调试状态文字用的,Android 目前没有对应的调试 UI 展示场景,且 Gson 已经在无害地忽略它们。

### 验证
- `./gradlew :app:testDebugUnitTest :app:assembleDebug`:过程中遇到一次已知的增量构建产物冲突(`bundleDebugClassesToRuntimeJar` 报重复的 `R.class` 条目,和 v0.3.8 起记录的 dex 冲突是同一类非代码问题),`clean` 后重新编译通过。
- **真实模拟器端到端验收(对接生产环境,不是 mock)**:发送"What is the best way to see the Great Wall"(带完整初始 mock 行程数据),请求真实耗时 20169ms(以前 10 秒就会被硬性掐断),App 正确渲染了服务器自己优雅降级返回的 `mode: "mock"` 响应(和 `NativeButlerFallback` 的离线兜底文案明显不同,`logcat` 确认是真实的 200 响应,只是服务器端三个 provider 对这个超大上下文请求都失败了才走的服务器自己的 mock);另外发送"Do I need a visa for China",738 毫秒内拿到真实的 `ask_factual` tool card 回复,新写的 `InlineToolCardView` 正确渲染了服务器返回的"Visa and entry"卡片(4 条要点,`tone: warning`)。

详见 DESIGN.md ADR-116。

## v0.3.11 - 2026-07-03

**Chat 输入区重新设计:建议问题改为单行可横滑,输入框明显变大。** 操作者在实际用过几轮之后给出反馈:"chat这一轮的布局要重新设计，输入框太小了，建议问题占比太大了"。

### 建议问题:从会换行的 FlowRow 改为单行可横滑的 LazyRow
- `ButlerScreen.kt` 里两处建议问题(空状态欢迎卡片 `EmptyButlerPrompt` + 消息列表下方常驻的 `ButlerComposer`)之前都用 `FlowRow`,3 个建议 chip 大概率装不下一行,会自动换到第二行,白白占用一整行的竖直空间。改用 `LazyRow` 后,无论有多少个建议,永远只占一行高度,多出来的靠横向滑动查看。`ButlerComposer` 里还加了判断:没有建议时完全不渲染这一行(以前哪怕是空的也会预留位置)。
- 移除了不再需要的 `FlowRow`/`ExperimentalLayoutApi` import 和两处 `@OptIn(ExperimentalLayoutApi::class)` 标注。

### 输入框:相机/麦克风挪进输入框内部,框本身明显变大
- 之前 `OutlinedTextField` 和相机、麦克风两个独立 `IconButton`、以及发送按钮挤在同一个 `Row` 里,四份东西平分宽度,输入框被压得很窄;而且 `minLines = 1`,平时看起来就是薄薄一条线。
- 现在把相机图标改成 `OutlinedTextField` 的 `leadingIcon`、麦克风改成 `trailingIcon`(功能不变,仍是未接线的禁用态占位),这样外层 `Row` 里只剩输入框(`weight(1f)`)和发送按钮两样东西,输入框几乎占满整行宽度。`minLines` 从 1 提到 2、`maxLines` 从 4 提到 6,默认就能看出是一个多行的大输入框,而不是等用户打字换行了才被动变大。`Row` 的垂直对齐从"居中"改成"底部对齐",这样输入框往上长高的时候,发送按钮始终钉在右下角,符合聊天输入区的常见交互习惯。

### 验证
- `./gradlew :app:testDebugUnitTest :app:assembleDebug`:`BUILD SUCCESSFUL`。
- Android 34 模拟器手动验收(清空 App 数据后全新启动,确保看到真正的空状态):空状态欢迎卡片的建议问题确认是单行、可横滑,不再换行;常驻输入区下方的建议问题同样单行,横向滑动后能看到被裁切在屏幕外的第三个建议;在输入框里打一段长文字,确认输入框跟着长高到多行,相机/麦克风图标始终贴在输入框内部两端,发送按钮始终贴在右下角,全程无崩溃。

详见 DESIGN.md ADR-115。

## v0.3.10 - 2026-07-03

**屏幕适配打磨:AndroidManifest 加入 resizeableActivity/max_aspect,导航栏改为真正的悬浮遮罩(不再挤占布局空间),Web 端 viewport 只声明 width。** 操作者在一条消息里给出四条指令:(1)"AndroidManifest application 标签内加入 resizeableActivity="true""；(2)"application 内部加入 max_aspect=2.4 元数据"；(3)"不要写死任何像素尺寸，全部用 match_parent 铺满屏幕"；(4)"viewport 不要限制最大高度，只写 width=device-width"。前三条是 Android 术语,第四条"viewport"/"width=device-width"是纯 Web 术语——本仓库是 monorepo,`android/` 是原生 Android 模块,`app/` 是原 Next.js Web 端(自 v0.2.17 起已冻结,主线全面转向原生),因此把前三条落在 Android 端、第四条落在 Web 端。另外操作者单独指出:"导航栏是浮现在page文字和内容上面的，不需要有边框，要融入"——这是对 v0.3.9 已实现的"悬浮胶囊导航栏"的一个功能性补完:v0.3.9 的导航栏外观是悬浮的,但结构上仍是 `Scaffold` 的 `bottomBar` 插槽,会整体挤占等高的布局空间,内容永远不会真正出现在导航栏"下面"。

### AndroidManifest 改动
- `AndroidManifest.xml` 的 `<application>` 标签内加入 `android:resizeableActivity="true"`,并在 `<application>` 内新增 `<meta-data android:name="android.max_aspect" android:value="2.4" />`,支持分屏/多窗口和超高屏/折叠屏等非常规宽高比,不被系统裁切letterbox。

### Compose 布局审计
- 全仓库搜索 `.height(`/`.width(`/`.size(` 硬编码像素调用,只发现两处,且都是合理的小尺寸 UI 元素(`VisePandaBottomBar.kt` 里 52dp 的占位 Spacer 和 58dp 的 Chat 悬浮按钮圆),不是应该铺满屏幕的容器级尺寸。所有顶层页面本来就统一用 `fillMaxSize()`/`fillMaxWidth()`/`weight(1f)`(Compose 里 `match_parent` 的等价写法)——代码库在这条指令上本来就合规,无需改动,只是确认。

### 导航栏改为真正的悬浮遮罩
- `VisePandaNavHost.kt`:`VisePandaApp` 不再用 `Scaffold(bottomBar = VisePandaBottomBar(...))` 包裹 `NavHost`,改为 `Box(Modifier.fillMaxSize())` 内部 `NavHost` 铺满整个 Box(`fillMaxSize()`,不再有底部 padding),`VisePandaBottomBar` 通过 `Modifier.align(Alignment.BottomCenter)` 叠加在最上层(`VisePandaBottomBar` 新增 `modifier` 参数支持这个用法)。
- `ui/theme/Dimens.kt` 新增 `BottomNavContentClearance`(96dp = 导航栏 64dp 高度 + 16dp 底部 inset + 小段余量),应用到需要"绕开"悬浮导航栏的地方:`TripsScreen`/`MeScreen` 的 `LazyColumn` `contentPadding` 底部,以及 `ButlerScreen` 的聊天输入区(`ButlerComposer`)——后者比滚动列表更重要,因为它是 Chat 页的核心可交互控件(输入框/相机/麦克风/发送按钮),绝不能被不透明的导航栏挡住或劫持点击。
- `DayDetailScreen` 无需改动:它不是 `TopLevelDestination`,导航栏本来就不会在这个页面显示。

### Web 端 viewport 修正(自 v0.2.17 冻结以来首次触碰 Web 端)
- `app/layout.tsx` 新增显式 `export const viewport: Viewport = { width: "device-width" }`(Next.js 14+ App Router 的 `Viewport` API),刻意不写 `initialScale`/`maximumScale`/任何高度属性,严格对应操作者"只写 width=device-width"的字面指令。这是本项目自 v0.2.17 Web 端冻结、主线转向原生 Android/iOS 以来第一次触碰 Web 端代码,范围严格限定在这一行低风险的正确性修正,不代表 Web 端开发重新启动。

### 验证
- `./gradlew :app:testDebugUnitTest :app:assembleDebug`:过程中遇到一次已知的、非代码性质的 Gradle 增量构建陈旧 dex 产物冲突(v0.3.8 就记录过的同一类问题),`./gradlew clean` 后重新编译一次通过。
- Android 34 模拟器手动验收:把 Trips 页面滚动到底,确认最后一张 Day 卡片和悬浮导航栏之间留有清晰的间隙(滚动过程中内容确实会从导航栏"下方"经过,静止时又能完全避开);Chat 页面的输入框/相机/麦克风/发送按钮整排都在悬浮导航栏之上,没有被遮挡;Me/Explore/Tools 页面渲染无回归。
- `npx tsc --noEmit` 确认 `app/layout.tsx` 的改动没有引入新的类型错误(仓库里已有的、与本次改动无关的测试文件类型错误保持原样)。
- `strings.xml` 里 Tools/Explore 占位文案顺延为 v0.3.11/v0.3.12(因本轮用掉了 v0.3.10)。

详见 DESIGN.md ADR-114。

## v0.3.9 - 2026-07-03

**视觉打磨:导航栏改为圆角悬浮胶囊,文本框/对话框加大圆角,Taxi Driver Card 保留并顺带修复一个对话框布局 bug。** 操作者发来一整套黑色手机 mockup 产品设计参考图(5屏:Chat home/Plan/Explore/Tools/Me),并给出三条明确指令:(1)"导航栏要做圆角悬浮式";(2)"taxi driver card 不重要 优先级极低，如果影响了项目进展可以移除";(3)"所有的文本框，对话框都做成圆角的，弧形"。

### 导航栏改为悬浮胶囊
- `navigation/VisePandaBottomBar.kt`:外层 `Surface` 从贴边、`fillMaxWidth().height(80dp)`、纸白色背景,改为四周留白(水平/底部各 16dp inset)、`RoundedCornerShape(RadiusPill)` 圆角、`Ink` 深棕黑色背景的悬浮胶囊,高度收窄到 64dp。两侧导航项(Trips/Explore/Tools/Me)去掉文字标签,只保留图标(对齐参考图),未选中态图标改用半透明 `Paper` 提供在深色底上的对比度(原先的 `onSurfaceVariant` 是为浅色底设计的);选中态仍保持 Cinnabar 红。中间悬浮的 Chat 圆形按钮形状/颜色不变,只是重新对齐到更矮的新导航栏上。
- `ui/theme/Dimens.kt`:新增 `BottomNavFloatingHeight`(64dp)、`BottomNavHorizontalInset`(16dp)、`BottomNavBottomInset`(16dp)三个常量。

### 文本框与对话框加大圆角
- `ui/butler/ButlerScreen.kt`:聊天输入框 `OutlinedTextField` 加上 `shape = RoundedCornerShape(RadiusXL)`(20dp,复用既有 token)。
- `ui/components/TaxiDriverCard.kt`:`AlertDialog` 同样加上 `shape = RoundedCornerShape(RadiusXL)`。

### Taxi Driver Card:评估后保留,顺带修复一个真实 bug
按操作者"优先级极低,若影响进展可移除"的指示评估后,判断这轮改动都不依赖移除它,予以保留(`AGENTS.md` 里"必须通过可见按钮触发,不恢复隐藏手势"的既有约束继续生效)。在给对话框加圆角、用模拟器截图验收时,发现一个**这轮之前就存在的真实布局 bug**:`confirmButton` 里 Copy/Speak/Close 三个 `TextButton` 塞进同一个 `Row` 装不下,导致 `Close` 按钮被挤压成几乎不可读的窄条(uiautomator dump 显示其可点击区域只有 54px 宽、307px 高,说明文字被迫逐字换行)。修复方式:把这个 `Row` 换成 `FlowRow`(`Arrangement.End`,装不下会自动换到第二行),模拟器截图确认 `Close` 现在正常换行显示、清晰可读。

### 验证
- `./gradlew :app:assembleDebug`:`BUILD SUCCESSFUL`(两次,首次改动后 + FlowRow 修复后各跑一次)。
- Android 34 模拟器手动验收:用 `uiautomator dump` 取精确坐标而非肉眼估算(吸取此前"坐标计算多次出错"的教训),依次点击 Trips/Explore/Tools/Me/Chat 五个入口,确认新悬浮胶囊导航栏在所有页面渲染一致、选中态高亮正确;打开 Taxi Driver Card 对话框确认圆角渲染、Copy/Speak/Close 三个按钮都清晰可读;聊天输入框圆角渲染正确。全程无崩溃。

详见 DESIGN.md ADR-113。

## v0.3.8 - 2026-07-03

**底部导航重构:Trips / Explore / Chat / Tools / Me,Chat 要求视觉突出。** 操作者发消息"底部导航栏顺序Trips/Explore/Chat/Tools/Me（account），Chat要突出"——这是产品结构层面的改动,不同于 v0.3.7 那轮"只做视觉层"的范围。用 `AskUserQuestion` 确认了两个关键点:Trips = 合并 Today + Plan(顶部保留 Now/Next/Later,下接 Day 列表);Chat 突出 = 中间悬浮圆形按钮(同 Figma 参考)。

### 导航结构改动
- `navigation/AppDestination.kt`:`TopLevelDestination` 重写为 Trips/Explore/Butler(Chat)/Tools/Me,新增 `leftOfCenter`/`rightOfCenter` 分组供底部导航使用。
- `navigation/VisePandaBottomBar.kt`:完全重写,从标准 Material3 `NavigationBar` 改为自定义布局——左侧 Trips/Explore、右侧 Tools/Me,中间一个凸起的圆形红色 Chat 按钮(`Modifier.shadow` + `CircleShape` + 负 offset 上浮),视觉上明显比其他四个入口突出。
- `navigation/VisePandaNavHost.kt`:路由注册更新为新的五个目的地。

### 页面合并与新增
- 新增 `ui/trips/`(`TripsScreen.kt`/`TripsViewModel.kt`/`TripsUiState.kt`):合并原 `Today`(Now/Next/Later 时间轴 + Ask Butler 入口)和 `Plan`(readiness 进度条 + Day 列表)为一个页面。删除整个 `ui/today/` 目录和 `ui/plan/PlanScreen.kt`/`PlanViewModel.kt`/`PlanUiState.kt`(保留 `DayDetailScreen.kt`/`DayDetailViewModel.kt`)。
- 新增 `ui/me/`(`MeScreen.kt`/`MeViewModel.kt`):全新的 Profile/设置页面。只有"当前行程"这一行是真实数据(从 `TripRepository` 读取),其余(Preferences/Data & Privacy)都是明确标注"本地占位数据,尚未接入真实账号系统"的诚实占位内容,不假装有真实的 Supabase auth/account 系统。

### 意外发现并修复:Card 内文字颜色 bug(贯穿多个既有屏幕)
构建 Trips/Me 新页面时,通过截图像素采样(而非肉眼判断)发现:`Card` 组件内没有显式指定颜色的 `Text`,会渲染成一种意外的红棕色(接近 `error`/`CinnabarDeep` 色值),而不是预期的中性深色文字。这是个**贯穿 `ButlerScreen.kt`、`DayDetailScreen.kt` 等既有屏幕的系统性问题**,不是这轮新增代码才引入的,只是之前的验收测试没有专门核实过这个细节。
- 根因排查:先尝试补全 `Theme.kt` 里 `lightColorScheme()` 遗漏的十几个 Material 3 角色(之前只设置了约 16/30 个,其余会静默回退到 Material 基线的紫色系默认值),但复测后确认这个补全没有解决问题。
- 最终修复:给所有依赖"Card 默认继承色"的 `Text` 都显式补上 `MaterialTheme.colorScheme.onSurface`/`onSurfaceVariant`,不再依赖不透明的默认解析链路。已在 `ButlerScreen.kt`(欢迎卡片标题、消息气泡内容)、`DayDetailScreen.kt`(`BlockDetailCard`/`BookingCandidateRow`)、`TripsScreen.kt`(`TimelineEntryCard`/`DayCard`)、`MeScreen.kt`(`SettingsSection`)里补齐。
- `Color.kt`/`Theme.kt` 仍然保留了补全后的完整 `ColorScheme`(更规范,即使没有直接解决这个具体 bug,也消除了未来出现类似问题的一类风险)。

### 验证
- `./gradlew :app:testDebugUnitTest :app:assembleDebug`:`BUILD SUCCESSFUL`。
- Android 34 模拟器手动验收:新底部导航两侧+悬浮 Chat 按钮渲染正确;Trips 页面正确合并 Now/Next/Later + Day 列表;Me 页面正确显示真实行程数据 + 诚实占位内容;颜色 bug 修复前后用像素采样对比确认(而非仅凭肉眼判断),`Dietary`/`Forbidden City` 等文字从错误的红棕色变为正确的深色,`Not set`/`Active`/`NOW`/`NEXT` 等本来就该是强调色的文字未受影响;全程无崩溃。
- `strings.xml` 里 `Tools`/`Explore` 占位文案顺延为 v0.3.9/v0.3.10(因为这轮用掉了 v0.3.8)。

## v0.3.7 - 2026-07-03

**Android 视觉层对齐 Figma Make 设计参考(纯视觉,产品结构不变)。** 操作者提供了 Figma Make 文件 `Design According to MD Document`(https://www.figma.com/make/8J1WnuwCHwx60bSC6f7fQn/),要求"将布局修改为figma的设计"。读取该文件后发现它的产品结构(五屏 Chat/Plan/Explore/Tools/Me、两侧+中间悬浮按钮的底部导航)和现有 repo 路线图(Today/Chat/Plan/Explore/Tools 横向导航)有实质性差异,遂就采纳范围征询操作者,操作者选择"只做视觉层"——保留现有产品结构和路线图节奏,只对齐配色/字体/圆角/Taxi Card 字号等视觉细节。

### 视觉改动
- `ui/theme/Color.kt`:配色更新为 Figma 精确值——纸白 `#FAF8F4`、中国红 `#C1292E`、金色 `#C9A84C` 等。**这是对 ADR-094/095"和 Web 端 `globals.css` 逐字对齐"规则的一次刻意例外**,本轮只改 Android,未同步改动 Web 端,两端色值从此有意产生细微差异(仍是同一套暖色新中式色系),详见 DESIGN.md ADR-105。
- `ui/theme/Type.kt`:首次引入自定义字体——Playfair Display(标题/品牌时刻)+ DM Sans(全部 UI 正文),替换掉此前"系统字体占位,自定义字体推迟"的 v0.3.3 决定。两款字体均为 Google Fonts 的 variable font(OFL 许可,可自由打包),通过 `FontVariation.Settings` 按字重取用,已加 `@OptIn(ExperimentalTextApi::class)`。
- `ui/theme/Dimens.kt` / `Theme.kt`:新增 `RadiusXL`(20dp)/`RadiusPill`(999dp)圆角 token,`Shapes.medium/large` 相应调大,让卡片呈现 Figma 要求的"更软"观感。
- `ui/components/TaxiDriverCard.kt`:中文地址字号从 34sp 提升到 Figma 规格的 52sp。**过程中发现并修复一个真实的视觉 bug**:只提字号不提行高会导致多行地址文字笔画重叠(原 34sp 时不明显,放大到 52sp 后暴露出来),已把 `lineHeight` 一并调整到 64sp。

### 未采纳的部分(明确排除,超出本轮"视觉层"范围)
底部导航形态(横向 5 等分 → 两侧+中间悬浮 Chat 按钮)、"Me" 新 tab(取代 Today)、Plan 提前加入 Needs Scheduling 候选区、Tools 做成 8 格 utility grid、Translator 全屏 overlay——这些都是产品结构/功能层面的改动,留给操作者未来决定是否要单独立项。

### 验证
- `./gradlew :app:testDebugUnitTest :app:assembleDebug`:先跑出一个真实编译错误(`FontVariation` 相关 API 需要 `@OptIn(ExperimentalTextApi::class)`),修复后 `BUILD SUCCESSFUL`。
- Android 34 模拟器手动验收:新配色/字体/圆角渲染正常;Taxi Card 52sp 地址文字修复后不再重叠;`Copy Chinese address` 功能未受影响,复制正常;全程无 `FATAL`/`AndroidRuntime` 崩溃。
- `strings.xml` 里 `Tools`/`Explore` 占位文案顺延为 v0.3.8/v0.3.9(因为这轮用掉了 v0.3.7)。

### 同轮追加:补上两处遗漏的视觉细节
操作者随后提供了 Figma Make 项目的本地源码导出(比之前浏览器里抠出来的片段更完整可靠),交叉核对确认了上面的配色/字体/圆角/字号都准确,同时发现两处第一遍漏掉的 UI 细节:
- `ui/butler/ButlerScreen.kt`:Chat 输入框加了 Camera/Mic 图标按钮(禁用态视觉占位,真正的相机/麦克风权限仍按计划留到 v0.3.8 Translator 轮按需申请,不在这里提前要)。
- `ui/components/TaxiDriverCard.kt`:Taxi Card 加了 Speak 按钮,和 Copy 并列。这个接的是 Android 系统自带 `TextToSpeech` API——因为 TTS 不需要运行时权限,所以没有违反"权限按需申请"的既定原则,直接接了真实功能而不是占位。已在 Android 34 模拟器上验证:真实触发了中文语音合成请求(下载 zh-CN 语音包后 Speak 按钮从禁用变可用),无崩溃。

## v0.3.6 - 2026-07-02

**Native Android Butler + Sync Bridge I。** 原计划称为 "v0.3.5 Butler + Sync Bridge" 的功能实现,因 v0.3.5 版本号被构建验证收尾轮占用,实际在 v0.3.6 交付。Chat(Butler)从诚实占位页升级为真实的 Jetpack Compose 对话界面,并成为底部导航默认首页。

### 新增:Butler Chat 真实界面
- `ui/butler/ButlerScreen.kt` + `ButlerViewModel.kt` + `ButlerUiState.kt`:输入框、发送按钮、消息列表、starter chips(首次进入展示三个引导问题)、结构化 assistant response 展示(headline/body/highlights/watchOut/nextStep)、offline fallback 状态提示。
- 底部导航顺序改为 Today / Butler(Chat)/ Plan / Explore / Tools,Chat 居中且为 `NavHost` 的 `startDestination`;`strings.xml` 里 `nav_butler` 文案改为 "Chat"。

### 新增:Web 端 CanvasPatch 契约的 Kotlin 镜像
- `data/model/ButlerModels.kt`:镜像 Web 端 `CanvasPatch`、`AssistantResponse`、`TripSummaryPatch`、聊天消息模型。
- `data/model/CanvasPatchApplier.kt`:按 Web 端 `applyCanvasPatch` 同样的规则合并 patch —— summary 部分合并、days 整体替换、alerts 按 type+title 去重、`lastUpdatedReason` 更新。
- `data/model/TripModels.kt` 给枚举加了 `@SerializedName`,对齐 Web 端 JSON 序列化值(`high`/`medium`/`low`、`info-only`、`needs-confirmation`、`Ready to save` 等)。

### 新增:本地缓存 + API Bridge
- `data/local/TripCacheEntity.kt` 增加 `messagesJson` 字段存 Butler 消息记录;`VisePandaDatabase.kt` 版本号升到 2。
- `data/remote/ButlerApiService.kt`:Retrofit 接口,调用现有 `/api/chat` 路由(`BuildConfig.VISEPANDA_API_BASE_URL = "https://www.go2china.space/"`)。
- `data/repository/RoomTripRepository.kt`:替换 `MockTripRepository` 成为 `TripRepository` 的真实绑定 —— 从 Room 读取/保存当前行程和 Butler 消息;发消息时先调 `/api/chat`,失败时(`runCatching` 兜底)走本地 fallback。
- `data/repository/NativeButlerFallback.kt`:网络失败时的诚实兜底文案 —— 明确告诉用户"已本地保存,联网后再试",不假装有实时 AI 理解、不生成真实预订/支付/交易能力。
- `di/AppModule.kt`:`TripRepository` 绑定从 `MockTripRepository` 切到 `RoomTripRepository`;新增 Retrofit/OkHttp/Gson provider。

### 新增测试
- `CanvasPatchApplierTest.kt`、`NativeButlerFallbackTest.kt`。

### 真实构建与手动验收(本轮在非沙箱 macOS + Android Studio JBR 环境完成)
- 上一轮 Codex 会话在其沙箱里无法跑 Gradle(缺 Android SDK、`services.gradle.org` 网络被拦、原生 Gradle service 受限),已提交代码但标注"未验证"。本轮在真实环境重新验证:
  - `./gradlew :app:testDebugUnitTest :app:assembleDebug` **一次性 `BUILD SUCCESSFUL`**,没有发现真实编译错误(与 v0.3.4 那批不同,这批代码本身没有语法/API 问题)。
  - 4 个单元测试全部通过(`CanvasPatchApplierTest` ×2、`NativeButlerFallbackTest` ×2,0 failures/errors)。
  - Android 34 模拟器手动验收:冷启动默认落在 Chat 页(底部导航正确高亮,验证了两次冷启动排除截图时机竞态的误报);五个 Tab 顺序 Today/Chat/Plan/Explore/Tools 正确;点击 starter chip 发送消息,真实调用 `/api/chat` 失败后正确走 `NativeButlerFallback`,标题栏显示 "Offline fallback · native mock fallback",气泡展示诚实的 "Saved for the Butler" 文案;Today 页面正确显示 "You are offline. Showing the last saved trip." 且行程数据(标题、83% readiness)未被破坏。
  - `strings.xml` 里 Tools/Explore 占位文案已正确顺延为 v0.3.7/v0.3.8,内部自洽。
- **观察记录(非阻塞,供后续参考)**:`NativeButlerFallback.createPatch` 用简单的 `lower.contains("nanjing"/"shanghai"/"beijing")` 关键词匹配来决定是否改写行程标题,即使是完全离线兜底路径。这在语义上有被误伤的风险(例如用户消息里恰好提到城市名但意图并非改标题),建议后续版本评估是否需要更保守的触发条件。

### 项目计划同步
- 未做 WebView 套壳、未恢复隐藏打车卡触发方式、未开启 Material 3 Dynamic Color、未新增 Supabase schema、未实现真实支付/预订/订单/地图/相机/麦克风能力。
- `AndroidManifest.xml` 新增 `INTERNET`/`ACCESS_NETWORK_STATE` 权限(Retrofit 网络调用所需,普通权限无需运行时弹窗)。
- `package.json`/`package-lock.json` 已更新到 `0.3.6`。

## v0.3.5 - 2026-07-02

**Android build verification handoff.** This release closes the major v0.3.4 risk: the native Android module has now been build-verified and manually accepted on an Android emulator.

- Generated and committed the Gradle wrapper (`android/gradlew`, `android/gradlew.bat`, `android/gradle/wrapper/*`) for Gradle 8.9.
- Fixed real Android compile errors found by `./gradlew :app:assembleDebug`:
  - added `org.jetbrains.kotlin.plugin.compose` to root and app Gradle files for Kotlin 2.0 Compose compiler support;
  - imported `androidx.compose.runtime.getValue` in `VisePandaBottomBar.kt`;
  - added `@OptIn(ExperimentalMaterial3Api::class)` to `PlanScreen.kt` and `DayDetailScreen.kt` for Material 3 `TopAppBar`.
- Updated `android/README.md` from "not build-verified" to a concrete `2026-07-02` verification record.
- Recorded real successful build output: `./gradlew :app:assembleDebug` produced `android/app/build/outputs/apk/debug/app-debug.apk` (~17.5 MB).
- Recorded manual emulator acceptance on Android 34: five bottom surfaces switch; Plan opens Day Detail; Today and Day Detail open Taxi Driver Card; copying the Chinese address works; disabling Wi-Fi and mobile data does not crash the mock-data path.
- Added design-reference context for the next Android UI pass: borrow from the operator's Lovable prototype and Figma Make file `Design According to MD Document`, while preserving the native Android/Material 3/Warm New Chinese direction.
- No web runtime code, API routes, Supabase schema, real Butler sync, booking/payment, map, camera, microphone, or hidden taxi-card trigger was added.

## v0.3.4 - 2026-07-02

**第一轮真实原生 Android 代码。** 操作者要求把 v0.3.3(Android Native Foundation)和 v0.3.4(Today + Plan Execution MVP)合并一轮做完,并明确 Android 工程放在本仓库内(monorepo,`android/` 目录)。

### 新增 `android/` Gradle 模块
- **技术栈**:Kotlin + Jetpack Compose + Material 3 + Navigation Compose + Hilt(依赖注入)+ Room(离线缓存,已定义未深度接入)+ DataStore(轻量设置)+ Retrofit/OkHttp(为 v0.3.5 预置依赖)。包名 `space.go2china.visepanda`(反向域名对应 go2china.space),`minSdk 26` / `compileSdk 34`。
- **五 surface 底部导航**:Today / Butler / Plan / Explore / Tools,对应 `docs/planning/v0.3.2-android-planning-synthesis.md` 定稿的产品模型,而非 v0.3.1 草稿的 4-Tab Canvas/Chat/Explore/Tools。
- **Today**:当前行程标题、readiness 百分比、Now/Next/Later 时间轴(诚实标注了局限——`TripState` 目前没有真实开始日期字段,v0.3.4 用第一天/前两个 block 做时间轴占位演示,不是伪装成真实的"现在几点"感知)、Ask Butler 入口、离线横幅。
- **Plan + Day Detail**:行程 readiness 进度条、Day 卡列表(带完成度徽标)、点击进入独立 Day Detail 页(非 Bottom Sheet),展示 block 详情、地址/营业时间、booking candidate(统一 "Info only" 标签,不暗示可下单)。
- **Taxi Driver Card**:单一共享组件(`ui/components/TaxiDriverCard.kt`),只能通过 Today 和 Day Detail 的**显性按钮**触发——落实 v0.3.2 对 v0.3.1 草稿"摇一摇/双击电源键"隐藏触发方式的否决;支持大字中文地址展示与一键复制到剪贴板。
- **Butler / Explore / Tools**:诚实占位页(明确写出各自会在 v0.3.5/v0.3.7/v0.3.6 实现),不是假聊天界面或假数据。
- **数据层**:`data/model/` 是 `lib/types/trip.ts` 的 1:1 Kotlin 镜像;`MockTripData.kt` 逐字段移植自 `lib/mock-ai/mockButler.ts` 的 `initialTripState`(同样的北京/上海/北京三天行程);`TripCompleteness.kt` 移植自 `lib/trips/completeness.ts`,连四舍五入行为(`Math.round` 对应 `roundToInt`)和"payment/visa 维度在无对应 alert 时视为完成"的规则都保持一致,确保同一份行程在 Web 和原生两端算出的完成度百分比永不打架。
- **Warm New Chinese 配色移植**:`ui/theme/Color.kt`/`colors.xml` 精确对齐 `app/globals.css` 的 `--paper`/`--cinnabar`/`--gold`/`--sage` 十六进制值,**故意关闭 Material 3 Dynamic Color**(会被用户壁纸取色覆盖品牌配色,与保持 Web/原生视觉一致性的目标冲突)。

### 历史说明:v0.3.4 初始提交时未构建验证
v0.3.4 初始提交是在受限环境中产出的,当时没有完成真实 Gradle 构建和模拟器验收。这个风险已经在 **v0.3.5** 关闭:Gradle wrapper 已入库,真实编译错误已修复,`./gradlew :app:assembleDebug` 已成功生成 debug APK,并完成 Android 34 模拟器手动验收。

### 项目计划同步
- `PLAN.md` 阶段十四勾选任务 14.3/14.4(v0.3.3/v0.3.4)为已完成;v0.3.5 已补上真实构建验证与模拟器验收。下一步排期不变:继续做 Butler + Sync Bridge 的功能实现。
- 未改动任何 Web 端代码、`/api/*` 路由或 Supabase schema。

## v0.3.2 - 2026-07-02

**Android planning synthesis.** This documentation-only release reads the parallel agent's GitHub planning, audits it, removes over-scoped interaction ideas, and merges it with the Today-first native Android product plan.

- Added `docs/planning/v0.3.2-android-planning-synthesis.md`.
- Preserved the strongest Android-native ideas from `v0.3.1`: Kotlin, Jetpack Compose, Material 3, Room, DataStore, MVI/StateFlow, API route reuse, permission scaffolding, and Amap MapView lifecycle caution.
- Corrected the product model from implementation-led `Canvas / Chat / Explore / Tools` into traveler-led `Today / Butler / Plan / Explore / Tools`.
- Removed hidden/global taxi-card triggers from the roadmap; taxi-card access should use visible Today / current trip / Day Detail actions.
- Revised the next implementation path to `v0.3.3` Android Native Foundation, `v0.3.4` Today + Plan Execution MVP, `v0.3.5` Butler + Sync Bridge, then Translator and Explore candidate work.
- No runtime code, Android implementation code, API keys, Supabase schema, provider integration, or transaction capability changed.

## v0.3.1 - 2026-07-02

**项目研发主线重大转向：原生移动端应用开发（Android APK + iOS App）主导。** Web 端（Next.js Web App）不再作为核心研发迭代方向，全部降级为次要维护支线。本版本（v0.3.1）专注于 Android 原生 APK 的全维度落地规划。

- **创建 Android 原生 APK 规格设计文档**：新增了 `docs/planning/v0.3.1-android-native-spec.md`，深度覆盖头脑风暴、筛选收敛、多角色对抗评审及定稿规划。
- **确立 Android 原生布局规范**：确立 Material Design 3 (M3)、8dp 原生栅格和圆角嵌套数学。废弃 WebView 套壳与混合开发，使用纯 Jetpack Compose (ConstraintLayout-compose) 响应式 WindowSizeClass。
- **确立触控体验方案**：针对 4.7-6.7 寸手机拇指热区（Thumb Zone）排布触控，规划 MapView 在 Compose 中生命周期代理托管，及 nested scroll 手势滑动阻尼隔离。
- **确立 MVI 状态与离线持久化**：采用 ViewModel + StateFlow 单向流动模式，配置本地 Room Database 离线优先缓存，杜绝状态竞态，支持弱网 Checklist 本地落库及 WorkManager 后台同步。
- **重构产品功能树与底导 Tab**：底部 NavigationBar 4 大一级 Tab（Canvas Screen, Chat Screen, Explore Screen, Tools Screen），集成 Show Taxi Driver 中文大字卡片（显性按钮直达）、Bilingual 翻译等。
- **重排里程碑计划**：原建议下阶段进入 Android 原生 APK 基础脚手架与数据层搭建；该建议已由 `v0.3.2` 融合规划修正为 `v0.3.3` 开始实现。
- **本版本仅作方案规划与逻辑设计，未修改/产出任何 Java/Kotlin、XML 布局或业务实现代码。**

### v0.3.1 补充(同一迭代内追加,未新增版本号)

原始规格书聚焦 4 大主 Tab(Canvas/Chat/Explore/Tools)的对抗性评审,以下内容
在 `docs/planning/v0.3.1-android-native-spec.md` 新增"阶段 5:补充规划"一节
中合并进来,填补的是"4 大 Tab 之外还有什么"以及"后端接口逐条能不能直接用"
这两块此前未展开的空白:

- **4 大 Tab 之外的页面落地**:Home(一次性引导层,非常驻 Tab)、Day 详情
  (独立页面而非 Bottom Sheet,因内容量大)、Trips(归入 Account 子页面)、
  Account(独立 Profile 页,顶部头像图标进入)、Translate 子功能(OCR 用
  `CameraX` 原生采集层,语音优先用系统级 `SpeechRecognizer`)、Community
  (Web 端本身未实现,本轮明确不展开原生设计)。
- **`/api/*` 逐路由复用边界表**:`/api/chat`、`/api/explore*`、`/api/tools`、
  `/api/exchange-rate`、`/api/trips`、`/api/translate/text` 直接复用;仅
  `/api/translate/ocr`/`stt` 的文件上传格式需要一次兼容性核实(不一定要改
  代码);Supabase Auth 改用原生 `supabase-kt` SDK,底层项目/表结构/RLS
  policy 不变。
- **游客态 vs 登录态权限边界表**:延续现有 Web 端"游客可用、登录增强"逻辑,
  存储介质从 `localStorage` 换成 Room + DataStore。
- **iOS 规划边界重申**:本文档全篇是 Android 专项,iOS 技术栈与视觉细节不能
  直接套用本文档的 Compose/M3 专属内容,留待独立版本展开。
- 未改变原规格书阶段 1～4 的主体判断,不涉及代码。后续 Sprint 计划已由
  `v0.3.2` 融合规划重新排序。

## v0.2.17 - 2026-07-02

**景点/餐饮/酒店数据与预订服务拓展评估(纯文档规划,不改代码)。** 操作者要求研究高德之外可以拓宽产品线的同类服务。

- 新增 `docs/planning/data-provider-expansion-assessment.md`,评估六个候选:**Trip.com**(携程国际版,酒店预订)、**大众点评**(美团,餐饮内容)、**百度地图**、**腾讯位置服务**、**Klook**(客路,门票/活动)、**KKday**(门票/活动)。
- 筛选标准不是"数据全不全",而是**离"外国游客能在 App 内直接付款拿到票/房间"有多近**——这是高德 POI 层数据完全没有覆盖的缺口。
- 结论与优先级:**Trip.com**(补酒店预订,唯一能补 Canvas `stay` 字段可预订能力的候选)与 **Klook**(补门票/活动预订,外国游客定位最精准)排前两位,建议优先研究;**KKday** 与 Klook 高度同质化,建议只接一家跑通,不必两家并行;**大众点评** 内容质量最高但存在明确的竞品条款风险(可能被判定为"用户点评+商户预订"竞品导致协议终止)且需要额外中译英本地化层,建议先做法律/商务风险评估而非技术调研;**百度地图**/**腾讯位置服务** 与高德数据重合度高,无增量用户价值,不建议投入,仅记录为技术容灾备选。
- **诚实披露研究局限**:六个候选服务的官方文档页面直接 `WebFetch` 均返回 `403 Forbidden`(已用 `curl "$HTTPS_PROXY/__agentproxy/status"` 核实非代理故障,是目标站点反爬所致),因此本轮结论基于多次独立 `WebSearch` 结果交叉印证,**不是像 v0.2.6 FlyAI 研究那样的逐字源码级核实**,文档中逐条标注「⚠️ 待人工核实」的条目在真正启动对接前必须由人登录官网确认最新条款。
- 未新增 `mock-inventory.md` 条目——六个候选目前都停留在"值不值得研究"阶段,不是"代码已就绪等 key"状态,待某一服务进入官方合作申请、产出逐字核实技术画像后再补充。
- 不改动任何产品运行时代码、不新增 provider 抽象、不新增外部 key。
- **版本协调说明**:本轮开发时本地分支曾与 `main` 短暂分叉(`main` 已并行推进至 `v0.2.16`,含 Explore/Tools/TripBlock POI 等多轮实现);发现分叉后未强推覆盖,而是在 `main` 最新提交基础上重新构建本轮改动,版本号相应从原计划的 `v0.2.9` 顺延为 `v0.2.17`。

## v0.2.16 - 2026-07-02

**Explore candidate review / Day detail action polish.** This release makes Add-to-Trip candidates clearer and actionable without pretending they are already scheduled.

- Changed Day cards and Day detail to render Flexible blocks only when a real Flexible candidate exists, avoiding generic open-time placeholders.
- Renamed visible Flexible candidates to "Needs scheduling" so travelers understand the POI is a candidate, not a locked itinerary slot.
- Added an "Ask VisePanda to schedule" action in Day detail for Flexible candidates.
- Routed that action through the existing Chat/AI canvas patch pipeline via a dedicated schedule-candidate message helper.
- Added tests for visible candidate state, scheduling action routing, and the helper message.
- No checkout, inventory, payment, Supabase migration, new API key, or production FlyAI integration.

## v0.2.15 - 2026-07-02

**Explore Add-to-Trip POI write-through.** This release makes Explore selections land in the canvas as structured, visible planning candidates.

- Added a structured Explore POI payload for Add to Trip, carrying POI id, city, category, map URL, source, coordinates, hours/phone when present, and info-only booking candidates.
- Updated Explore navigation to pass both the traveler-facing Chat draft message and the structured POI payload.
- Updated Chat auto-send handling to parse the POI payload and deterministically enrich a matching TripBlock or append a visible Flexible candidate block to the relevant city day.
- Updated Day cards and Day detail drawers to render Flexible blocks, so Add-to-Trip candidates do not disappear from the traveler view.
- Added tests for payload construction, deterministic write-through, Explore navigation, and Chat auto-apply behavior.
- No checkout, inventory, payment, Supabase migration, new API key, or production FlyAI integration.

## v0.2.14 - 2026-07-02

**Real POI context write-through + booking candidate model.** This release makes live Amap POI context more durable by writing safe fields into matching TripBlocks after model parsing.

- Added a non-transactional `BookingCandidate` model and optional `TripBlock.bookingCandidates`.
- Expanded `ButlerToolPoi` with Amap id, phone, map URL, coordinates, and info-only booking candidates.
- Added deterministic write-through from `liveToolContext` to provider-generated TripBlocks when names match, so the canvas keeps real POI execution fields even if a model omits them.
- Updated Day detail to show booking candidates as "Info only" guidance.
- Added tests for write-through behavior and booking-candidate rendering.
- No checkout, inventory, payment, Supabase migration, new API key, or production FlyAI integration.

## v0.2.13 - 2026-07-02

**TripBlock POI embedding + Day detail operational upgrade.** This release makes day details more executable for FIT travelers without adding any booking/payment backend.

- Added optional operational POI fields to `TripBlock`: address, Chinese address, phone, opening hours, map link, booking info link, source label, and coordinates.
- Updated Butler prompts so future model/provider output can preserve safe POI execution fields when sourced from live context or static fallback.
- Upgraded the read-only Day detail drawer with POI execution panels, map/booking links, source labels, coordinates, and a Show taxi driver card.
- Seeded mock/static fallback itinerary blocks with representative Chinese addresses and map links so the feature works without API keys.
- Added tests covering Day detail POI rendering and taxi-driver handoff copy.
- No new external API keys, Supabase migration, real booking transaction, payment flow, or production FlyAI integration.

## v0.2.12 - 2026-07-02

**Documentation handoff alignment.** This release updates the active project handoff surface so another device or coding agent can safely continue from the same version line.

- Updated package metadata and active documentation status to `v0.2.12`.
- Clarified that `v0.2.11` is a completed frontend design-resource configuration pass, not the next coding task.
- Set the next recommended implementation round to `v0.2.13` TripBlock POI embedding + Day detail operational upgrade.
- No runtime code, API keys, Supabase schema, provider routing, or user-facing capability changed.

## v0.2.11 - 2026-07-02

**Frontend design resource stack configuration.** This is a documentation-only configuration pass for the operator-requested design and UI-skill resources.

- Added `PRODUCT.md` as a concise product/audience/register context file so future design tools and agents have a stable product brief.
- Added `docs/planning/v0.2.11-frontend-design-resource-stack.md` mapping frontend-design, UI design system, CSS animation, creative aesthetics, Awwwards landing inspiration, web design guidelines, Vercel React best practices, Superpowers planning, Impeccable, better-icons, UI Design Brain, DESIGNmd, and awesome-design-md into the VisePanda workflow.
- Recorded that VisePanda's existing `DESIGN.md` and `app/globals.css` remain the source of truth; external resources are advisory unless a later implementation explicitly installs or imports them.
- Verified `designmd.co` is not a coding design-system source for this project and excluded it from the active stack.
- No product runtime code, npm dependencies, API keys, Supabase schema, or user-facing behavior changed.

## v0.2.10 - 2026-07-02

**Tools Widgets I.** This release turns the first three high-anxiety Tools categories into real interactive widgets while preserving the existing static checklists as fallback context.

- Added an optional `ToolCategory.interactive` descriptor for widget metadata. Categories without it continue rendering static checklist content only.
- Added `components/tools/widgets/ToolWidget.tsx` with:
  - RMB currency converter, using live injected CNY rates when present and conservative fallback rates otherwise.
  - Visa/entry planning checker with nationality, stay length, and transit-only inputs.
  - Payment setup wizard for wallet and card-brand choices.
- Rendered widgets inside the existing Tools modal, above quick tips and detailed sections, so travelers can act first and read details after.
- Added focused widget styling that follows the current paper/hairline visual system.
- Added tests for interactive descriptor coverage and all three widget interactions.
- No new external API keys, no official visa adjudication, no payment transaction API, no booking integration, and no production FlyAI usage.

## v0.2.9 - 2026-07-02

**Chat factual fast-path + inline Tools cards.** This release completes the first practical slice of the remaining Chat interaction work after the v0.2.8 visual redesign.

- Added `InlineToolCard` support to structured assistant responses. Existing provider responses remain compatible because `toolCards` is optional and the parser drops invalid card payloads.
- Added `lib/tools/factualToolCards.ts`, a deterministic fast-path that maps factual China-travel questions to existing Tools knowledge for visa/entry, payment, currency, metro, eSIM/VPN, and emergency topics.
- Updated the orchestrator so matching `ask_factual` / emergency concern messages return immediately as `mode: "tools"` / `strategy: "tool"` without calling any LLM provider. Missing provider keys still degrade gracefully.
- Updated Chat to render inline tool cards inside assistant replies, with links to the matching `/tools?category=...` deep link and copy-to-clipboard support that includes card content.
- Added a visible thinking state while the Butler is busy so sends no longer feel silent.
- Added tests for factual tool-card generation, LLM bypass behavior, Chat card rendering, and the thinking state.
- No new external API keys, no Supabase schema changes, and no production FlyAI integration.

## v0.2.8 - 2026-07-02

**Chat/Canvas 视觉重设计,对齐操作者提供的高保真设计稿。** 不改变既有交互契约(所有内容变更仍走 AI 管道),纯视觉层与少量新的本地操作性交互。

### TripSummary(行程摘要卡)
- 标题可内联改名:铅笔图标 → 点击进入编辑态(input,Enter 提交/Escape 取消/失焦提交) → `onRenameTrip(nextTitle)`,`ButlerWorkspace.handleRenameTrip` 本地 `setTrip` 仅改 `summary.title`(操作性状态,不路由 AI,与既有 Undo/prep-checklist 例外原则一致)。
- 新增状态徽标:圆点 + confidence 文案 + 进度条 + 百分比说明 + 下一步单元格(展示首个未完成维度)。
- 新增"一览"chip 行(路线/天数/旅行风格)与动作行两组按钮簇:Add day(`onAddDay` → `ButlerWorkspace.handleAddDay`,拼装预制消息通过既有 AI 管道追加一天)、Rebalance route(`onRebalanceRoute` → `handleRebalanceRoute`,同样走 AI 管道)均为真实功能;View map / Trip settings 诚实禁用 + `title="Coming soon"`,不是假交互。
- 原有 `.trip-summary__readiness` 勾选清单结构与 `aria-label` 完整保留,未破坏既有测试契约。

### DayCard(单日卡片)
- 新增 Day 级完成度徽标(`calculateDayCompleteness`,4 维:blocks≥3/food/stay/transport)。
- Morning/Afternoon/Evening 三个 block 现渲染真实图标(Sunrise/Sun/MoonStar)+ 真实照片(`block.photoUrl`,如无则展示图标占位,不编造图片)+ 可选 highlights 清单(无则回退展示 description 单条)。
- 快捷动作拆分为主/次两组:`DAY_PRIMARY_ACTIONS`(Add to day / Find food nearby / Get tickets,始终可见)与 `DAY_SECONDARY_ACTIONS`(Lighten this day / Swap morning / Add rest,收进 "…" 溢出菜单,`role="menu"`/`role="menuitem"`)。`lib/canvas/quickActions.ts` 相应重写,新增 `add_activity`/`find_food`/`get_tickets` 三种 kind。

### ChatPanel(对话面板)
- 头部重构为头像 + "Ask VisePanda" 标题 + 历史(真实链接到 `/trips`)/ Pin(诚实禁用)图标行。
- 每条消息新增 byline(助手头像 + 角色 + 时间戳 + 用户消息的"已发送"对勾),`ChatMessage.createdAt`(新增可选字段)驱动时间戳。
- 结构化 AI 回复的 `highlights` 现渲染为带 Sparkles 图标的高亮卡片列表;新增反馈行(赞/踩本地状态切换,不接后端;复制到剪贴板 + "Copied" 提示,`navigator.clipboard` 失败静默降级)。
- Next Step 卡新增 Star 图标与可关闭的 X 按钮(仅本地隐藏当前这条,下一条新 nextStep 到达后重新出现)。
- 输入区重构为图标行:Attach/Mic 诚实禁用,Send 图标按钮,textarea 支持 Enter 发送 / Shift+Enter 换行;底部新增 AI 免责声明"VisePanda can make mistakes. Please double-check important details."。

### 样式层排查
- `app/globals.css` 存在多处并行会话遗留的 append-only 层叠覆盖块(v0.1.55 视觉打磨块、`min-width:900px` 紧凑布局块),核实后确认它们对 `.trip-summary`(已改 `display:flex`)的 grid 定位规则已失效为 no-op,`.chat-panel` 的 `order` 分层在各层间数值一致、互不冲突;新增 `.chat-disclaimer { order: 7 }` 并将 `.chat-panel` 的 `grid-template-rows` 追加第 7 条 track,确保免责声明稳定渲染在输入框下方而非被隐式行顺序打乱。
- 发现并修复一处真实回归:遗留的 `.day-card__head span { display: none; }` 规则(用于隐藏旧版卡片副标题)意外把新的 pace 文案与完成度徽标一起隐藏,已用更高特异性的 `.day-card__head-meta span` / `span.day-card__completeness` 选择器覆盖。

### 测试与验证
- 新增 8 个测试(`tests/canvas-components.test.tsx`:内联改名/Add day/Rebalance route/禁用态断言;`tests/chat-panel.test.tsx`:next-step 关闭/反馈切换/复制确认/Enter 发送),全部 134 测试通过,`npm run build` 成功。
- 用 Playwright 对 `/chat` 页面做了视觉核验(桌面宽度截图 + 溢出菜单展开 + 内联改名态),确认布局与设计稿意图一致后才提交。

## v0.2.7 - 2026-07-02

**Canvas 行动层 —— 第一轮代码实现。** 让行程从"能看"变成"能指挥"。

### 新增功能
- **六维完成度**(`lib/trips/completeness.ts`,纯函数):route/stay/food/transport/payment/visa,payment/visa 维度基于「是否存在未勾选的对应类型 alert」判定,无该类型 alert 时视为完成(vacuous)。`TripSummary` 的进度条改用该函数,替换原先内联的 5 维实现。
- **变更摘要卡**(`lib/canvas/diffTripState.ts` + `components/canvas/ChangeDigestCard.tsx`):day 级(added/revised/removed)+ alert 级(alert)diff,渲染在对应助手回复末尾;无实质变化时不渲染。点击条目 → 画布滚动定位 + 目标 Day 卡金色脉冲高亮。
- **Day 卡快捷动作**(`lib/canvas/quickActions.ts` + `DayCard.tsx`):Lighten / Add food / Swap morning / Add rest,预制自然语言消息**始终包含天数与城市**,通过既有 `handleSend` → `/api/chat` → `CanvasPatch` 管道发送,不直接改画布。
- **Patch 演出动画**:新增 Day(`data-status="new"`)纯 CSS 淡入上滑;修改 Day(`data-status="revised"`)通过可重放动效 hook(`lib/canvas/useReplayableAnimation.ts`,remove-reflow-readd 技术)播放金色脉冲,支持同一天连续两次修改都能正确重放。画布在 patch 落地后自动滚动到第一个变更 Day。
- **撤销(Undo)**:`ChangeDigestCard` 上的撤销按钮触发**本地确定性回滚**(恢复上一份 `TripState` 快照),而非路由给 AI——见下方 ADR-070,这是一处经过论证的、对规格文档的刻意偏离。
- **出发准备区**(`components/canvas/PrepChecklist.tsx`,"Before you fly"):聚合 `trip.alerts` 为可勾选清单;`ButlerAlert` 新增可选 `done?: boolean` 字段(向后兼容);勾选为本地操作性状态变更,不路由给 AI(理由见 AGENTS.md)。

### 修复的架构缺陷
- 排查一个仅在特定并发/高负载测试场景下复现的"摘要卡已出现但 Day 卡内容未同步"竞态时,定位到 `TripCanvas` 内部维护了一份通过 `useEffect` 从 `trip` prop 同步的 `editableTrip` 本地状态——这是 v0.1.43 抽屉从可编辑改为只读之前遗留的历史包袱,早已没有存在的必要,却引入了一次多余的渲染周期。已移除该缓冲状态,`TripCanvas` 现在直接渲染 `trip` prop。这不只是修了一个测试问题,是移除了一处真实的、潜伏的正确性风险。

### 测试
- 新增 `tests/completeness.test.ts`、`tests/diffTripState.test.ts`、`tests/quickActions.test.ts`(纯函数单测)与 `tests/canvas-action-layer.test.tsx`(端到端交互:快捷动作发消息→摘要卡渲染→点击定位滚动→撤销回滚→准备区勾选联动完成度)。更新 `tests/canvas-components.test.tsx` 以匹配六维模型的新分数与新标签;`tests/setup.ts` 新增 jsdom `scrollIntoView` polyfill。全部 122 测试通过,`npm run build` 成功。

## v0.2.6 - 2026-07-02

**FlyAI(飞猪)Skill 研究与集成规划 + 项目级开发工具接入。** 不改产品运行时代码;新增一个开发工具向的 Claude Code Skill。

- 逐字核实上游仓库 `alibaba-flyai/flyai-skill`(MIT):`SKILL.md`、8 份 `references/*.md` 子命令文档,以及已发布 npm 包 `@fly-ai/flyai-cli@1.0.16` 的 `package.json`/README 源码,拒绝任何未经证实的能力假设。
- 新增 `docs/planning/flyai-skill-integration.md`:
  - **技术画像**:确认 flyai-cli 是通过 MCP `streamable_http` 协议连接飞猪官方托管服务的瘦客户端;8 个子命令(`keyword-search`/`ai-search`/`search-flight`/`search-train`/`search-hotel`/`search-poi`/`search-marriott-hotel`/`search-marriott-package`)的精确参数与输出字段表;`keyword-search` 意图元数据额外覆盖签证/电话卡租赁/租车/邮轮,超出官方 README 首屏总结的"8 大能力"。
  - **认证与隐私细节**:构建期内嵌阿里官方默认试用凭证 + 每请求携带 `x-ff-ctx` 设备指纹头(gzip+AES-256-GCM,用于风控反滥用)。
  - **架构现实判断**:Vercel serverless 无法可靠内嵌 CLI 子进程调用(冷启动/进程管理);内嵌凭证是官方共享试用额度非专属配额,绕过官方渠道直接复用违反其风控设计且与 Dianping/Meituan 的既定"先官方申请、不逆向"原则相悖。结论:目前是开发者/Agent 工具,非可直接嵌入生产后端的公开 API。
  - **逐项功能映射表**(用户要求):Tools 地铁分类(`search-flight`/`search-train` 补齐城市间交通空白)、Tools 签证/eSIM 分类(`keyword-search`)、Explore 住宿/景点列(`search-hotel`/`search-poi` 补齐真实价格与预订链接,精确对应既有 `ExploreRichMeta` 字段)、Chat 工具调用(`lib/ai/toolContext.ts` 未来新增工具函数)、Canvas Day 详情 `stay`/`transport` 字段(升级为真实可预订选项)。
- **落地**:将上游 `skills/flyai/` 原文(SKILL.md + 8 份 references)vendor 进 `.claude/skills/flyai/`,附 `LICENSE-NOTICE.md` 说明来源与使用边界——仅供开发阶段研究/内容生产,禁止生产代码调用。任何打开本仓库的 coding agent 会话均可直接使用。
- `docs/planning/mock-inventory.md` 新增第 23 项(真实预订数据),状态 🔴,目标"飞猪官方合作确认后"。
- 版本编号:本轮为纯规划+开发工具轮,完整代码三轮(Canvas 行动层/Chat 体验/设计系统)再顺延一位为 **v0.2.7/v0.2.8/v0.2.9**。

## v0.2.5 - 2026-07-02

**规划融合 + FIT travel desk readiness seed。** 本轮把远端 `v0.2.4` 交互深化规格与本地视觉规划/实现 seed 合并到同一条 `0.2.x` 主线,避免 `0.1.55` 本地 UI polish 与远端 `0.2.x` 规划分叉。

- 安全同步远端更新:检测到本地 `47eba46 feat(ui): polish fit travel desk` 与远端 `v0.2.1–v0.2.4` 分叉,先读取远端新文档与版本线,再合并而非重置覆盖。
- 吸收本地视觉规划/seed:Trip Canvas readiness 初版、summary/readiness/action rail、Chat first-run starter state、Home launcher polish 与响应式兜底纳入 `0.2.x` 主线。
- 修正后续版本路线:完整 Canvas 行动层不再占用 `v0.2.5`,顺延为 `v0.2.6`;Chat 体验重塑 + 内联工具卡顺延为 `v0.2.7`;设计系统收口 + Tools widgets 顺延为 `v0.2.8`。
- 更新 `PLAN.md`、`PRD.md`、`DESIGN.md`、`AGENTS.md`、`HANDOFF.md`、`VERSIONING.md`、`docs/planning/v0.2.4-interaction-deep-dive.md` 与 `docs/planning/handoff-prompt-for-coding-agent.md` 的路线编号。
- 保留核心约束:快捷动作仍走 AI 管道;readiness 目前是派生展示/seed,不是完整持久化 completion schema;mock/static fallback 不移除。

## v0.2.4 - 2026-07-02

**纯文档规划轮 —— 前端 UI 与用户交互深化规格 + 实现交接提示词。** 不改任何产品代码。

- 新增 `docs/planning/v0.2.4-interaction-deep-dive.md`(v0.2.3 路线图的下钻层,后三轮的交互验收标准):
  - **交互设计哲学五判据**:对话是手画布是脸 / 可点优于可打 / 100ms 反馈定律 / 一屏一个朱砂 / 动效只为"发生了什么"服务。
  - **Chat↔Canvas 联动可见性设计**(本轮核心命题):变更摘要卡(Change Digest,基于 `diffTripState` 纯函数的 day/alert 级 diff,点条目画布定位+金色脉冲)、桌面双向悬停联动(共享 highlightDayNumber)、patch 演出编排(600ms staged reveal + 自动滚动到首个变更卡)、撤销机制(预制 undo 意图 + 本地快照兜底)。
  - **组件级交互规格**:composer(Enter/Shift+Enter、自动长高、乐观清空、轮换占位)、MessageBlock 分层(serif headline → 60ms stagger 伪流式)、等待叙事(100ms/3s/8s 递进)、Day 卡结构与六态、时段块级微操作、完成度六段进度条、出发准备区复选(alert.done 可选字段)。
  - **视觉系统细则**:五级字体层级表、色彩使用规则(朱砂每屏唯一、金禁作文字、五档墨灰)、三层纸面无阴影、图标两档。
  - **毫秒级动效参数总表**(实现即抄表)+ 移动端手势(Chat sheet 70dvh/半收 30dvh、visualViewport 键盘适配)+ 无障碍底线(焦点管理、4.5:1 对比度)。
  - **三轮吸收方案(当时规划)**:v0.2.5 Canvas 行动层+画布交互 / v0.2.6 Chat 体验重塑+内联工具卡 / v0.2.7 设计系统收口+Tools 交互组件;该编号已在 v0.2.5 融合轮顺延为 v0.2.6/v0.2.7/v0.2.8。
- 新增 `docs/planning/handoff-prompt-for-coding-agent.md`:**自包含实现交接提示词**,可直接复制给其他 coding agent——含 60 秒项目背景、必读文档序、八条硬约束(AI 管道/fallback/key/视觉红线/动效/7 文档+双分支推送/并行防护/中文汇报)、三轮任务规格与操作者视角验收。
- 真流式 SSE、抽屉下滑手势、Day 卡拖拽排序:记录为三轮之后候选,本三轮不承诺。

## v0.2.3 - 2026-07-02

**纯文档规划轮 —— 整体项目规划 + 前端 UI 优化迭代路线。** 不改任何产品代码。

- 新增 `docs/planning/v0.2.3-ui-optimization-roadmap.md`,与既有三份蓝图(v0.1.52 交互蓝图 / v0.1.53 技术蓝图 / v0.1.55 布局规范)互补,构成完整设计契约:
  - **宏观**:定位到体验的四条推论(管家≠工具箱、一站式=不离开、FIT 买确定性、智能必须可见);10 项体验差距审计(G1 文字墙、G2 画布被动、G3 变化不可感、G4 无加载态、G5 完成度不可知、G6 工具与对话割裂、G7 样式无系统、G8 空错态未设计、G9 移动端未打磨、G10 五焦虑入口分散),并聚类为后三轮主题。
  - **微观**:逐界面 UI 优化清单——Canvas(完成度进度条/Day 卡快捷动作/patch 动画/出发准备区)、Chat(分块渲染/乐观 UI+骨架屏/内联工具卡/模型标签弱化)、Tools(widget 优先+清单折叠)、移动端专项、空/错状态。
  - **设计系统**:token 层整理、组件库首批(Button/Pill/Card/Sheet/Toast/ProgressMeter/MessageBlock/ToolCard/EmptyState)、动效与反馈准则(100ms 反馈、有含义的动效、reduced-motion)。
  - **后三轮执行承诺**:v0.2.4 Canvas 行动层+画布 UI;v0.2.5 对话体验重塑+内联工具卡(含 ask_factual <150ms 快通道);v0.2.6 设计系统收口+Tools 三件套 widget(汇率换算/签证问答/支付向导)。每轮含功能/UI/测试/边界四栏与验收标准。
- 项目记忆 `CLAUDE.md` 新增操作者指令:**思考、推理、回答、汇报一律中文**(代码与提交信息可保留英文)。
- 版本占位说明:原口头排期的三轮代码迭代因本规划轮占用 v0.2.3 顺延为 v0.2.4/v0.2.5/v0.2.6。

## v0.2.2 - 2026-07-01

**Chat core-loop fixes: speed, Chat↔Canvas sync, and auto-save.** Addresses three reported problems.

### Slow replies
- `lib/ai/orchestrator.ts` now **races all candidate providers in parallel** (`Promise.any`, first valid patch wins) instead of trying them one-by-one. Latency ≈ the fastest healthy model instead of the sum of failing ones.
- `lib/ai/providers/openaiCompatibleProvider.ts` gives every provider call an **18s abort timeout**, so a hung or misconfigured model fails fast instead of stalling the reply.
- The Amap tool-context prefetch is now time-bounded (6s) and best-effort, so a slow POI lookup never blocks the answer.

### Chat and Live Canvas not syncing
- Root cause: when a provider failed (e.g. a model-id mismatch), the orchestrator fell back to the mock butler, which only produced a full itinerary for "first time"/"5 days" messages — every other message returned no `days`, so `applyCanvasPatch` kept the old canvas.
- `lib/mock-ai/mockButler.ts` is now **destination-aware**: it extracts city names (EN + 中文) and a day/week count from the message and generates a matching multi-day skeleton itinerary, so the canvas reflects the chat even in fallback mode.
- `lib/ai/butlerPrompt.ts` system prompt now **requires live models to return the complete `days` array** (and set `tripSummary` title/duration/destinations) whenever the itinerary changes; days may be omitted only for pure factual questions.

### Auto-save
- `components/chat/ButlerWorkspace.tsx` now **auto-saves every chat** to Trips for signed-in users (silent "Saved to your Trips." note), and the manual **"Save to Trips" button was removed**. Guests keep the automatic localStorage draft. The sign-in sync and auto-save no longer double-write.

### Tests
- Updated `orchestrator`, `mockButler`, `chat-workspace`, and `chat-workspace-guest-sync` tests; added parallel-race and destination-skeleton coverage. Full suite 105 passing; production build succeeds.

## v0.2.1 - 2026-07-01

**Version-series reset (operator directive).** The product moves from the `0.1.x` line to the `0.2.x` line. `v0.1.55` was the last `0.1.x` iteration; `v0.2.1` is the new baseline, and every subsequent iteration increments `0.2.x`.

- `package.json` version → `0.2.1`.
- `VERSIONING.md` records the reset and updates the versioning rule to `0.2.x`.
- `CLAUDE.md` project memory records the new versioning rule so future sessions continue from `0.2.x`.
- No product code, behavior, provider, or schema change — version metadata only.

## v0.1.55 - 2026-07-01

**Documentation-only iteration — UX layout & frontend design specification.** Complements the existing v0.1.52 interaction blueprint (what/why) and v0.1.53 technical blueprint (plugin architecture) with the missing design/experience layer (how it looks, lays out, and feels) for the one-stop FIT travel-butler positioning.

- Added `docs/planning/ux-design-and-layout-spec.md` with three parts:
  - **Macro**: the single-surface spatial model (Canvas + Chat as home, not six tabs), a full information-architecture table, and the five-anxiety layout principle (every traveler anxiety must have a resolution path within one tap of the Canvas).
  - **Micro**: wireframe-level page composition and component-level interaction mechanics for Home, the Chat command center + Live Canvas (desktop two-column and mobile stacked), Day detail, Explore, Tools, Translate FAB, Trips, Account, and Admin — including structured-reply block rendering, canvas-patch animation, day quick-actions, and precise Add-to-Trip.
  - **Frontend design system**: formalized design tokens (Warm New Chinese palette, type/space/radius/motion scales), a reusable component library, per-surface visual hierarchy, motion/feedback, mobile-first specifics, and accessibility/i18n rules.
- Mapped each existing roadmap phase (Canvas Action Layer, Inline Tool Cards, TripBlock POI, Translate Everywhere, Tools Widgets, Account, Admin) to its governing design section so implementation has a concrete design contract.
- No product code changes. Synced this branch onto `origin/main` (v0.1.54) first to preserve the parallel session's work — no history was overwritten.

## v0.1.54 - 2026-07-01

**Interaction Shell I code implementation.** This release implements the first code slice planned after the v0.1.52/v0.1.53 strategy documents.

- Added three Home archetype starts for independent FIT travelers: First China 10 Days Essentials, Foodie China, and History & Nature. Each routes to `/chat?archetype=<id>`.
- Added shared archetype configuration in `lib/chat/archetypes.ts`, so Home links and Chat starter prompts use the same labels and Butler prompts.
- Chat now detects `?archetype=` on launch, clears the URL, and sends the matching archetype prompt through the existing Butler pipeline. No direct canvas mutation was added.
- Chat first-run suggestions now show three no-typing FIT starter chips instead of generic prompt buttons.
- The latest structured Butler `nextStep` now appears as a prominent primary action card in the Chat panel and can be clicked to continue the same AI pipeline.
- Trip Canvas now uses the trip title as its main h1 when available, replacing the generic "Live Trip Canvas" heading.
- Trip summary confidence labels now render as traveler-facing copy: Draft → Taking shape, Refined → Looking good, Ready / Ready to save → Travel-ready.
- Added tests for Home archetype routing, Chat first-run starts, `?archetype=` auto-send, primary `nextStep` action, and Canvas title/status wording.
- Updated planning docs so the active next implementation sequence begins at `v0.1.55` Canvas Action Layer after this `v0.1.54` code pass.

## v0.1.53 - 2026-07-01

**Documentation-only strategic planning pass.** No product runtime code changes. This iteration refines VisePanda's architectural design and product goals from a comprehensive traveler-first experience angle, detailing new features and linkages.

- **Offline-First Travel Vault**: Added requirements and designs for a local storage caching architecture. The vault caches the current active Trip Canvas, offline pocket notes, emergency contacts, local bilingual addresses, and essential translate phrasebooks. When the network is lost, the application seamlessly enters "Offline Desk" mode.
- **Cultural Context Interpreter**: Upgraded AI Butler requirements to provide cultural context for travelers (e.g., explaining why reservations must be booked 7 days in advance for certain museums, local holiday crowd advisories, and ticketing rules).
- **Intelligent Payment Card Routing**: Added a structural payments wizard requirement mapping foreign credit cards (Visa/Mastercard/Amex) to optimized WeChat Pay and Alipay setup procedures, identifying transaction limits, local ID verifications, and ATMs/cash backups.
- **Contextual Tool Promotion**: Outlined page linkage rules to promote relevant tools in real-time context (e.g., if a user is in Shanghai on Day 2, automatically float the Metro and Alipay guides, and prioritize menu translation).
- **Bilingual Export & Print Kit**: Added design specification to export itineraries as compact, clean bilingual (EN/ZH) cards, maps, and offline print sheets, specifically tailored for taxi drivers and hotel staff.
- Added design documents **ADR-060** through **ADR-063** to `DESIGN.md`.
- Updated `PRD.md`, `PLAN.md`, `AGENTS.md`, and `HANDOFF.md` to establish the new execution baseline.

## v0.1.52 - 2026-07-01

**Documentation-only strategic interaction iteration.** No product runtime code changes. This iteration defines the deeper product/UX direction for the next implementation rounds.

- Added `docs/planning/v0.1.52-product-interaction-blueprint.md` as the authoritative blueprint for product positioning, journey design, page roles, feature linkage, roadmap, UX writing, metrics, and implementation guardrails.
- Repositioned VisePanda as a China travel operating system for foreign visitors rather than only an AI itinerary generator.
- Defined the five anxieties the product must reduce: entry, payment, connectivity, language, and itinerary.
- Defined the core loop: intent/archetype → preference extraction → live tools/data → Trip Canvas source of truth → Chat explanation → small next-step controls → Trips readiness/continuity.
- Defined the journey model: Curious → Planning → Preparing → In China → Share/Get help.
- Reassigned page roles: Home starts archetypes; Chat is the command center; Canvas is the operational trip object; Trips handles continuity/readiness/sharing; Explore feeds Chat/Canvas; Tools resolves anxieties as contextual widgets/cards; Translate becomes an everywhere utility; Account handles trust/preference/consent/lead capture; Community supports inspiration and proof.
- Updated the original roadmap so the active sequence now treats `v0.1.54` as the completed Interaction Shell I code pass, followed by `v0.1.55` Canvas Action Layer and later tracks.
- Updated `PLAN.md`, `PRD.md`, `DESIGN.md`, `AGENTS.md`, `HANDOFF.md`, and `VERSIONING.md` to reflect the new planning baseline.

## v0.1.51 - 2026-07-01

**Three-iteration implementation batch:** Amap rich POI data, first Chat tool-context loop, and lightweight preference memory.

- **v0.1.49 Amap rich POI:** `/api/explore/amap` now requests `extensions=all` and supports Nanjing. A shared `lib/explore/amapSearch.ts` normalizes rating, price, phone, opening hours, photos, business area, location, and source metadata.
- **Explore rich cards:** `ExploreAttraction`, `ExploreFoodSpot`, and `ExploreStay` now carry optional rich metadata. `ExploreBoard` conditionally renders ratings, price level, approximate per-person price, opening hours, phone, source, business area, and thumbnails when Amap provides them, while static fallback cards stay unchanged.
- **v0.1.50 Chat tool context:** the Butler orchestrator now builds a bounded Amap POI context for relevant intents (`create_trip`, `add_poi`, `ask_recommendation`, `logistics`) and injects real POI candidates into the model prompt before planning. If Amap is unavailable or unconfigured, the context is omitted and the normal provider/mock chain continues.
- **v0.1.51 Preference memory:** added `UserPreferenceProfile` extraction for pace, budget, party type, dietary restrictions, cuisine preferences, interests, and confidence. Guest profiles persist in localStorage and are sent to `/api/chat`; Chat shows compact remembered-preference chips.
- **Compatibility:** all new POI and preference fields are optional. Existing mock/static data, saved trips, and plain `assistantMessage` flows remain valid.
- **Tests:** added preference-profile coverage and updated affected Chat/Explore tests.

## v0.1.48 - 2026-07-01

**Chat quality activation for the configured provider keys.** DeepSeek, Qwen, Zhipu, and Moonshot are now aligned to the user's Vercel configuration, and Butler replies can render as structured travel guidance instead of a single plain paragraph.

- Updated the multi-LLM registry defaults to the configured production model choices: DeepSeek v4 flash, Qwen 3.6 Flash, Zhipu GLM5, and Moonshot Kimi 2.5. Env overrides remain available through the existing `*_CHAT_MODEL` variables.
- Extended `CanvasPatch` and `ChatMessage` with optional `assistantResponse` / `response` data: `{ headline, body, highlights, watchOut, nextStep }`.
- Updated `lib/ai/butlerPrompt.ts` so live providers are asked to return the structured response object while still populating `assistantMessage` for backwards compatibility.
- Added parser fallback behavior so older/plain provider JSON is still accepted and converted into a minimal structured response.
- Updated `ChatPanel` to render structured VisePanda replies with a headline, highlights, optional watch-out line, and next-step line while preserving plain text rendering for historical messages and mock fallback.
- Added tests for the new model defaults and structured-response parsing compatibility.

## v0.1.47 - 2026-07-01

**First code iteration of the multi-model track (阶段十三).** The Butler now answers through a real multi-LLM orchestrator instead of a single hardcoded DeepSeek call, following the quality-over-cost principle (ADR-043). All keys are server-side only; the mock fallback is preserved.

- **Provider abstraction** (`lib/ai/providers/types.ts`, `openaiCompatibleProvider.ts`): a `ChatCompletionProvider` interface plus one OpenAI-compatible implementation that covers every Chinese LLM (they all expose an OpenAI-shaped `/chat/completions`).
- **Model registry** (`lib/ai/modelRegistry.ts`): six providers — DeepSeek (reasoning), Qwen/Aliyun Bailian (chinese/vision), Zhipu GLM (reasoning/judge/long-context), Moonshot Kimi (long-context), Baidu ERNIE (china-facts), MiniMax (judge). Each declares capabilities and a server-side key env with aliases; base URL and model are overridable per deployment.
- **Intent classifier** (`lib/ai/intentClassifier.ts`): a fast local regex/keyword classifier (10 intents) whose purpose is quality routing (pick the right specialist), not cost savings.
- **Orchestrator** (`lib/ai/orchestrator.ts`): classify → select candidate providers (specialist first, then a fallback chain) → for high-stakes intents (`create_trip`, `ask_factual`) with 2+ providers run a small parallel ensemble preferring the primary → parse the patch → on total failure fall back to the mock Butler. Returns `mode`, `modelLabel`, `intent`, `strategy`, and `providersTried`.
- **Chat route** (`app/api/chat/route.ts`): now calls `requestOrchestratedButlerPatch` and returns the richer metadata. With zero keys configured it behaves exactly as before (mock); it upgrades automatically as each provider key is added — no code change required.
- **Workspace status** (`components/chat/ButlerWorkspace.tsx`): shows the actual model label (e.g. "Qwen Plus (Aliyun Bailian)") that produced the canvas update, instead of only "DeepSeek/mock".
- **Env documentation** (`lib/env/placeholders.ts`): 16 new keys documented — `ZHIPU_API_KEY`, `MOONSHOT_API_KEY`, `ERNIE_API_KEY`, `MINIMAX_API_KEY`, plus each provider's `*_BASE_URL` / `*_CHAT_MODEL` overrides and `QWEN_CHAT_MODEL`. All server-side only.
- **Mock inventory** (`docs/planning/mock-inventory.md`): a complete list of every mock / placeholder / local-simulation point (22 items) with its real-replacement plan, status, and target phase — per the request to fill in previously-skipped steps with real tools/data/workflows while keeping fallbacks.
- **Project memory** (`CLAUDE.md`): VPCC runs only on explicit request (not automatically); every iteration updates all md docs in detail (especially HANDOFF/PLAN/PRD) and pushes; manual steps get beginner tutorials; security constraints restated. `.claude/commands/vpcc.md` annotated accordingly.
- **Tests**: added `tests/orchestrator.test.ts`, `tests/intentClassifier.test.ts`, `tests/modelRegistry.test.ts` (16 new tests). Full suite 95 passing; the legacy `deepseekButler.test.ts` stays green (that path is retained for back-compat). Production build succeeds.
- **ADR-050** added (`DESIGN.md`): provider-agnostic orchestration on top of OpenAI-compatible endpoints.

## v0.1.46 - 2026-07-01

**Documentation-only iteration — no code changes.** A strategic product-expansion plan covering seven new tracks. Full deep-dive in `docs/planning/v0.1.46-product-expansion.md`; summaries and decisions recorded across the seven workflow docs.

- **Guiding principle changed** (`PRD.md`, `DESIGN.md`): optimize for user experience and answer quality first — **token cost is explicitly not a constraint**. The v0.1.45 "route factual messages away from the LLM to save cost" rationale is reframed: the intent classifier now exists for quality/correctness (route to the right specialist model / verified source), not cost savings. Larger models, multi-model ensembles, and refine-and-verify loops are all in scope.
- **Multi-model Chinese LLM orchestration defined** (§1, ADR-044): a provider-agnostic orchestrator over DeepSeek, Qwen (Aliyun Bailian), Zhipu GLM, Moonshot Kimi, and Baidu ERNIE, with intent-based routing, parallel ensemble + judge for high-stakes answers, a refine-and-verify loop, and a graceful fallback chain that still ends at the mock Butler.
- **Native iOS + Android apps planned** (§2, ADR-045): React Native + Expo recommended (genuinely native, reuses the TypeScript domain layer and API routes) over dual Swift/Kotlin; offline-first travel cache, native camera/mic/push, and a separate China distribution track requiring ICP 备案 / 软著. Plan only — not executed.
- **Tools functional upgrade planned** (§3, ADR-046): the six static Tools become interactive — visa eligibility/transit checker, payment setup wizard, full currency converter, Amap-backed metro route planner, actionable eSIM/VPN options, and one-tap Emergency (call buttons, GPS embassy locator, phrase TTS, share-location).
- **Professional Account UI + lead capture planned** (§4, ADR-047): a dedicated `/account` center (professional, formal, trust-signaling) plus progressive-profiling lead capture (留资) with a prioritized field list — contact (incl. WeChat), trip qualification (nationality/dates/party/budget/cities/purpose), and enrichment with explicit consent.
- **Admin backend + LLM customer briefs planned** (§5, ADR-048): a role-gated `/admin` area where staff see leads and chat conversations, with a multi-model pipeline that distills each customer's data + conversation into a structured `CustomerBrief` (summary, trip intent, budget signal, readiness-to-book score, key preferences, open questions, objections, suggested next action).
- **Supabase schema additions planned** (§6): `0004_leads_and_admin.sql` — `leads`, `lead_events`, `customer_briefs`, `profiles`, and an admin `role`; RLS for admin-wide read; `SUPABASE_SERVICE_ROLE_KEY` server-side only.
- **Frontend & visual optimization track planned** (§7, ADR-049): design tokens + a reusable component library, motion/feedback, empty/error states, accessibility, responsive/tablet polish, performance, and a brand illustration system.
- **Consolidated build sequence recorded** (`PLAN.md` 阶段十三–十八): multi-LLM → Tools functional → Account+leads → admin backend → design system → native apps, with parallelizable legal/ops lead-time work started early.
- **New env keys reserved (server-side only):** `ZHIPU_API_KEY`, `MOONSHOT_API_KEY`, `ERNIE_API_KEY`/`BAIDU_*`, `MINIMAX_API_KEY`. Standing security constraints reaffirmed (mock fallback preserved; service-role key never in browser; only `NEXT_PUBLIC_AMAP_MAPS_KEY` is public).

## v0.1.45 - 2026-07-01

**Documentation-only iteration — no code changes.** This is a macro product-planning pass that distills three research threads (Amap/Meituan data enrichment, Chat×Explore×Trips fusion, and chat-efficiency/UX overhaul) into one coherent seven-iteration roadmap.

- **Product positioning sharpened** (`PRD.md`): VisePanda is reframed around the five concrete fears a Western traveler has before a China trip — visa eligibility, payment, connectivity, language, and itinerary anxiety — and every feature is mapped to the fear it resolves. Chat becomes the single thread that holds the other surfaces together.
- **Chat Intelligence pipeline defined** (`PRD.md`, `DESIGN.md`): a five-stage `Input → Classify → Route → Handle → Normalize` pipeline replaces the current "every message is a full LLM call" model. A 10-intent taxonomy (`create_trip`, `adjust_trip`, `add_poi`, `ask_factual`, `ask_recommendation`, `preference_signal`, `concern`, `logistics`, `add_location`, `unclear`) routes factual and preference messages to non-LLM handlers, cutting cost and latency for an estimated 30–40% of traffic.
- **Response normalization schema defined**: a structured `{headline, body, highlights, watchOut, nextStep}` contract replaces the single free-text `assistantMessage` blob so every Butler reply is scannable and consistent.
- **Preference Profile + intent distillation defined**: a `UserPreferenceProfile` extracted silently from natural conversation (no interrogation form), with a strict "one clarifying question per turn, only when a gap would produce a wrong itinerary" rule.
- **Data-fusion architecture defined** (`DESIGN.md`): a tool-calling Butler that calls Amap/Dianping live during planning (`search_pois`, `get_poi_detail`, `search_dianping`), an `ExploreRichMeta` model for ratings/price/hours/photos, a backwards-compatible rich `TripBlock`, and a two-key Amap split (server-side POI key vs. public JS-map key).
- **Seven-iteration roadmap added** (`PLAN.md` 阶段十二): v0.1.46 Chat Intelligence Layer → v0.1.47 Preference Profile → v0.1.48 Amap Enrichment → v0.1.49 Tool-Calling Butler → v0.1.50 Onboarding + Canvas Quick-Actions → v0.1.51 Navigation Restructure → v0.1.52 Dianping/Meituan + Map.
- **UX audit added** (`PRD.md`): user-journey walkthroughs (first-run, ask-visa, refine-day, translate-menu), a redundancy-removal list (provider-status jargon, duplicate labels, developer-facing confidence copy), and a navigation restructure from 6 flat tabs to 4 tabs + a floating Translate action.
- **Meituan/Dianping API application guide recorded** (`HANDOFF.md`): step-by-step registration path on 大众点评开放平台, credential requirements, realistic review timelines, and the recommendation to start the application now because of the multi-week queue.
- **ADR-037 through ADR-042 added** (`DESIGN.md`): intent routing, preference profile, tool-calling loop, rich data model, two-key Amap split, and navigation restructure decisions.
- **Security constraints reaffirmed**: all keys (`DEEPSEEK_API_KEY`, `DASHSCOPE_API_KEY`, `AMAP_API_KEY`, future `DIANPING_APP_KEY`/`DIANPING_APP_SECRET`) stay server-side only; mock/static fallback is never removed; `NEXT_PUBLIC_AMAP_MAPS_KEY` is the only new public key and is domain-whitelisted.

## v0.1.44 - 2026-07-01

- Moved the 6-tab navigation to a **fixed bottom bar** on mobile (`position: fixed; bottom: 0`), so thumbs can reach tabs without stretching to the top of the screen. The header now shows only the brand mark, language switcher, and account icon on mobile.
- Converted the day detail drawer from a narrow right-side panel (`min(430px, 34vw)` = ~133px on a 390px phone) into a **full-width bottom sheet** (80dvh, slides up from the bottom with rounded top corners and a drag-handle hint).
- Constrained the **account menu popover** to `calc(100vw - 28px)` on mobile, preventing it from overflowing the left edge of narrow screens.
- Made **explore city filter pills** scroll horizontally on mobile instead of wrapping into multiple rows.
- Forced **explore POI columns** (Attractions / Food / Stays) to a single column on mobile instead of the auto-fit multi-column grid.
- **Trip detail header** now stacks vertically on mobile (title above action links) instead of trying to fit both on one row.
- **Trip summary actions** (compact controls inside the Live Trip Canvas on Trip Detail) switch to left-aligned and wrap freely on mobile.
- Enlarged **tool card** touch targets on mobile (`min-height: 52px`).
- Added `padding-bottom: calc(64px + env(safe-area-inset-bottom))` to `.app-shell` so page content is never hidden behind the fixed bottom nav on any screen, including notch devices.
- Removed the circular border from nav icons inside the bottom bar; added a 2px active-state line at the top of the active tab for visual feedback.
- Shrunk brand mark logo and title text slightly on mobile to free header height.

## v0.1.43 - 2026-07-01

- Fixed `/api/translate/text` so text translation no longer fails outright when the Qwen/DashScope route is unavailable; the route now falls back to the existing DeepSeek server-side provider before returning a provider-unavailable error.
- Updated the unified Translator UI to show a clearer configuration message when neither Qwen nor DeepSeek translation providers are available.
- Changed Trip Canvas day cards from `Edit` to `View details`; the day drawer is now read-only and shows itinerary details instead of editable form fields.
- Reworked real Trip Detail pages with saved canvases so Continue/Trips/status/archive/share controls live as compact buttons/status copy inside the Live Trip Canvas summary card instead of taking over the top of the page.
- Added tests for Qwen-to-DeepSeek translation fallback, read-only day details, and compact Trip Detail canvas actions.

## v0.1.42 - 2026-07-01

- Simplified `/translate` from four separate cards into one locale-aware translator workspace.
- Translation direction now follows the active website language and Chinese, supporting English, Spanish, Arabic, Japanese, Korean, French, and Chinese through the existing Qwen text route.
- Image translation now exposes two clear actions: Upload Image and Take Photo. Take Photo is visible but disabled on desktop because it is intended for mobile camera capture.
- Voice translation is reduced to one Record button; audio upload and public audio URL controls were removed from the page UI.
- Common phrases and special terms now sit in a horizontal support rail below the two equal source/output text panels.
- Reduced heavy translucent panel styling on Translator so the ink landscape background remains visible, using hairline dividers and very low-opacity paper backing instead.
- Replaced the Account avatar picker artwork with six new panda PNG assets while keeping the existing six avatar IDs stable for localStorage and community compatibility.
- Desktop landscape `/translate` is locked to one viewport with internal overflow only; 1440x900 verification showed no body/html page scroll.
- Updated `/api/translate/text` so non-English target locales are prompted correctly instead of defaulting every non-Chinese target to English.

## v0.1.41 - 2026-07-01

- Fixed "Saving failed" error on Save to Trips in Chat.
- Root cause: `trips.owner_id` FK references `public.users(id)`, but Supabase Auth users only exist in `auth.users` — no `public.users` row → FK violation on every insert.
- Fix 1 (DB): migration `0003_fix_auth_user_sync.sql` adds a trigger that auto-upserts a `public.users` row whenever a new `auth.users` entry is created, plus INSERT/UPDATE RLS policies so existing users can upsert their own row.
- Fix 2 (code): `saveTripCanvas` now upserts the caller's `public.users` row (using `ownerEmail` from the session) before inserting the trip, covering users who signed up before the trigger was applied.
- Fix 3 (code): `appendMessage` failures are now non-fatal (logged as warnings, don't block the save success message).
- Added `console.error` logging in the save catch block for easier diagnosis of future failures.

## v0.1.40 - 2026-07-01

- Tightened the standalone landing home page into a single-viewport desktop layout.
- Reduced hero, header, CTA, and feature-card spacing so the home page reads faster without pushing key content below the fold.
- Made the six feature cards more compact while preserving the route entry points for Chat, Trips, Explore, Tools, Translate, and Community.
- Preserved the v0.1.38 home-page structure, brand mark behavior, LanguageSwitcher, and Account menu entry.

## v0.1.39 - 2026-07-01

- Replaced background image with new golden-line Chinese landscape (mountains, pagodas, traditional buildings on warm cream paper).
- Reduced background gradient overlay opacity across all body and destination-scene variants so the new line-art image shows clearly (was ~0.58, now ~0.16–0.18).
- Day drawer panel overlay also lightened to match.

## v0.1.38 - 2026-06-30

- New landing home page (`/`) replaces the redirect-to-chat default — brand hero with "Start Planning →" CTA and 6 feature cards (Chat, Trips, Explore, Tools, Translate, Community).
- Home page is standalone (no AppShell/NavTabs); has its own minimal top bar with LanguageSwitcher and AccountMenu.
- Brand-mark in AppShell now links to `/` instead of `/chat`.
- Compact interior page headers: section kicker/h1/subtitle made tighter across Trips, Explore, Tools, Community, and Translator via CSS overrides.
- Feature cards on home page each show icon, translated nav label, and one-line description; responsive 3→2→1 column grid.

## v0.1.37 - 2026-06-30

- Added multi-language (i18n) system supporting English, Spanish, Arabic, Japanese, Korean, and French.
- Language preference stored in localStorage (`visepanda:locale`); persists across sessions.
- Custom React context (`I18nProvider`) and `useTranslation()` hook — no external library required.
- Locale bundles dynamically imported per language (only the active locale is loaded).
- Arabic (`ar`) sets `document.documentElement.dir="rtl"` automatically; all other locales use LTR.
- `[dir="rtl"]` CSS overrides for Arabic: card accent borders, day notes, tool modal tips, explore add-note, and phrase-book notes flip to the right side.
- `LanguageSwitcher` component added to the app header (left of the account menu) — compact EN/ES/AR/JA/KO/FR selector.
- Navigation labels (Chat, Trips, Explore, Tools, Translate, Community) translated in all 6 locales.
- Trips Dashboard: heading, filter pills, card labels, status copy, empty state all translated.
- Tools Board: section heading, "Open checklist →", badge labels, "Close", "Offline pocket notes" all translated.
- Explore Board: section heading, column headers (Attractions/Food/Stays), "Add to Trip" translated.
- Community Board: section heading, tab labels (Feed/Hot Spots/Photos) translated.
- Translator Page: section heading and subtitle translated.

## v0.1.36 - 2026-06-30

- Mobile layout overhaul: all pages now scroll naturally on phones (390px viewport).
- Nav bar becomes a 6-icon bottom strip on mobile, fitting all tabs including Community.
- Translator switches to single-column stack on mobile (fixed CSS cascade bug where desktop 2×2 grid overrode the mobile rule).
- Day block thumbnail placeholder hidden on mobile (title already shown in block text).
- Trip status guide hidden on mobile to bring trip cards immediately into view; trip meta table hidden to reduce card height.
- Trip filter pills horizontally scrollable on mobile (no more wrapping).
- Community membership strip horizontally scrollable on mobile (full tier names visible).
- Community board body and all page containers flow with natural height on mobile (no more overflow:hidden clipping).
- Trips summary stats remain 3-column on mobile.
- Added late-cascade `@media (max-width: 760px)` block at end of globals.css to override warm visual system styles that weren't guarded by a min-width breakpoint.

## v0.1.35 - 2026-06-30

- English-only UI pass: all Chinese-only UI labels replaced with English across Tools badges, Community HotSpots filters and actions, Community Photos default emoji, mock trip highlights, and mock butler itinerary attraction names.
- All attraction and place names in mock data now use "English Name (中文名)" bilingual format (e.g. "Forbidden City (故宫)", "The Bund (外滩)").
- Tool card badges changed to English: Required, Pre-trip, Live, Transit, Connectivity, Emergency.

## v0.1.34 - 2026-06-30

- Redesigned Tools page with 6 modal-overlay card dialogs: each tool now appears as a themed card (icon, accent color) in a 3×2 grid; clicking opens a floating modal with tips, checklist sections, and offline notes. ESC and backdrop click close the modal. URL deep links (`?category=<id>`) preserved.
- Fixed Trips dashboard filter button visibility: ALL/DRAFT/READY/SHARED/ARCHIVED filter pills are now always visible above the scrollable trip library, with the status guide moved inside the scroll area. Grid reduced from 5 rows to 4.
- Redesigned Translator as a single-page 2×2 grid: Text (top-left), OCR (top-right), Voice (bottom-left), Phrases (bottom-right) are all visible simultaneously — no tab navigation. Each panel scrolls independently.
- Updated CSS throughout: `.tools-grid` (3×2 card grid), `.tool-card` with per-card accent theming, `.tool-modal-overlay` + `.tool-modal`, `.translator-grid` (2×2 panel grid), `.translator-grid__panel`, and responsive overrides for mobile.
- Updated `tests/tools-board.test.tsx` to mock the static provider directly and updated `tests/translator-page.test.tsx` for the single-page layout.

## v0.1.33 - 2026-06-30

- Added a desktop-first visual layout refresh across Chat, Trips, Explore, Tools, Translate, Community, and Account.
- Tightened the global shell, header, navigation, page titles, card rhythm, and one-page desktop workspace behavior.
- Added a Warm New Chinese visual-system override with warmer paper tones, ink-line dividers, smaller serif headings, compact cards, and unified paper-style inputs.
- Rebalanced Chat so the Live Trip Canvas title is smaller, the right prompt chips stay in two compact rows, the composer/save/status controls no longer overlap, and the page stays locked to one viewport.
- Improved Trips, Explore, Tools, Translate, and Community content areas so headers and filters take less vertical space while long content scrolls inside each page.
- Cleaned visible mojibake from Translator and Community page-level labels, and normalized Translator Text/OCR/Voice controls to stable English labels without changing the Qwen API flow.
- Added `docs/superpowers/specs/2026-06-30-visual-layout-refresh-design.md` to record the approved visual refresh direction.

## v0.1.32 - 2026-06-30

- Removed the old Translate category from `/tools`; translation now remains a dedicated `/translate` main navigation page.
- Converted Tools categories into compact name-only cards. Details stay hidden until a category card is opened, and URL deep links such as `/tools?category=currency` still open the matching drawer.
- Removed Tools provider implementation/status metadata from the page UI, including live-provider labels, coverage copy, next-integration copy, and candidate API-source strings.
- Removed the visible Tools API-priority block from category drawers so users see travel checklists rather than implementation planning text.
- Updated the retained `ButlerReminders` helper so `language` alerts route to `/translate` instead of the removed `/tools?category=translate` category.
- Updated Tools tests to cover the six-category provider, card-first drawer interaction, Translate removal, and hidden provider metadata.

## v0.1.31 - 2026-06-30

- Upgraded the Community page from static mock display to a local interactive MVP.
- Feed now supports local post publishing, type/city filters, likes, saves, comments, read-more detail, and localStorage persistence.
- Photos now supports local photo-card publishing and local likes; real image upload remains planned for Supabase Storage.
- Added a five-level membership system: Bamboo Guest, Panda Explorer, Silk Road Insider, Dragon Pass, and VisePanda Concierge.
- Community authors now show panda avatars and membership badges.
- Added six bundled panda avatar SVG assets and a reusable avatar registry in `lib/account/avatars.ts`.
- Account trigger now displays the selected panda avatar, and the Account popover lets users choose a panda avatar stored in localStorage.
- Added tests for Community MVP interactions and Account avatar selection.

## v0.1.30 - 2026-06-30

- Upgraded the Translator stack to **Aliyun Bailian Qwen** across text translation, OCR, TTS, and STT.
- `/api/translate/text` now uses `qwen-mt-flash` via the DashScope OpenAI-compatible chat completions endpoint, returning translation and optional pinyin JSON.
- `/api/translate/ocr` now uses `qwen3.5-ocr` via the DashScope OpenAI-compatible multimodal endpoint; OCR.space is no longer used.
- Added `/api/translate/tts`, using `qwen3-tts-instruct-flash` through the DashScope multimodal generation endpoint and returning a temporary Qwen audio URL.
- Added `/api/translate/stt`, using `qwen3-asr-flash` with `input_audio` through the DashScope OpenAI-compatible chat completions endpoint.
- Added a **Voice** tab to `/translate` for recording, uploading audio, or pasting a public audio URL; recognized speech is automatically sent through the text translation route.
- Replaced browser `speechSynthesis` in Text, OCR, and Phrase Book with server-side Qwen TTS requests so API keys remain server-only.
- Added `lib/aliyun/qwen.ts` as the shared Bailian/Qwen helper and updated environment placeholders for `DASHSCOPE_API_KEY`, endpoint overrides, and optional model overrides.
- Added tests for the Qwen translator API routes and the Voice tab; full Vitest suite now covers 30 files and 69 tests.

## v0.1.29 - 2026-06-30

- Added **Community page** (`/community`) as the 6th main navigation tab (Globe icon), implementing the Phase 11 framework.
- Three-tab layout: **动态 Feed** (shared trip posts and tips), **热门 Hot Spots** (city-level attraction/food/hidden-gem rankings), **照片 Photos** (photo wall grid).
- `CommunityFeed`: 6 mock posts (trips + tips) with author avatars, city tags, excerpts, hashtags, like/comment counts, and a "Share My Trip" CTA placeholder.
- `CommunityHotSpots`: 12 community-rated hot spots across 5 cities (Beijing, Shanghai, Chengdu, Xi'an, Hangzhou) with star ratings, review counts, traveler tips, and "Add to Trip" buttons (routes to `/chat?add=…` via existing AI pipeline).
- `CommunityPhotos`: 8 mock photo cards with emoji covers, location labels, captions, and like counts; upload CTA placeholder for future Supabase Storage integration.
- Static data in `lib/community/types.ts` and `lib/community/mockData.ts`; no real API yet — community Supabase tables (`posts`, `photos`, `likes`) and high-de/美团 API integration planned for Phase 11.
- Added CSS classes for `.community-board`, `.community-tabs`, `.community-feed`, `.community-post-card`, `.community-photos`, `.community-photo-card`, `.community-hotspots`, `.community-hotspot-card`.
- Updated `tests/nav-tabs.test.tsx` to 6 tabs/SVGs. All 64 tests pass.

## v0.1.28 - 2026-06-30

- Added a full-featured **Translator page** (`/translate`) as the fifth main navigation tab (Languages icon).
- **Text translation** (`components/translate/TextTranslator.tsx`): bidirectional EN↔ZH text translation via `/api/translate/text` (DeepSeek server proxy, `DEEPSEEK_API_KEY`); returns translation + pinyin; supports Web Speech API TTS (zh-CN, rate 0.85) and clipboard copy; Ctrl+Enter shortcut.
- **OCR scan translation** (`components/translate/OcrTranslator.tsx`): upload or camera-capture image → client-side Canvas API resize (max 1200 px) → `/api/translate/ocr` (OCR.space, `OCR_SPACE_API_KEY` or free "helloworld" key, language `chs`, engine 2) → auto-translates recognized Chinese text to English; TTS for Chinese output; drag-and-drop zone.
- **Phrase book** (`components/translate/PhraseBook.tsx`): 44 static phrases across 6 categories (Greetings, Dining, Transport, Shopping, Emergency, Hotel) and 28 special terms across 3 categories (Attractions, Dishes, Signs); each item shows Chinese, pinyin, English, optional notes/context, and a TTS speak button.
- New server routes: `app/api/translate/text/route.ts` and `app/api/translate/ocr/route.ts`; both keys are server-only, never exposed to the browser.
- Static phrase data in `lib/translate/phrases.ts` and `lib/translate/types.ts`.
- `ToolsBoard` Translate category gains a data-driven CTA link (`/translate`) via new optional `cta?: { label; href }` field on `ToolCategory`.
- Removed `ButlerReminders` rendering from `TripCanvas` (component file kept); updated canvas tests accordingly.
- Added CSS classes for `.translator-page`, `.translator-tabs`, `.text-translator`, `.ocr-translator`, `.phrase-book`, `.tools-category-cta`.
- Community page planning added to PLAN.md (Phase 11), PRD.md, DESIGN.md, AGENTS.md, and HANDOFF.md — no implementation yet.
- Added `tests/translate-api.test.ts` and `tests/phrase-book.test.tsx`; updated `tests/nav-tabs.test.tsx` to 5 tabs/SVGs and `tests/canvas-components.test.tsx` to remove ButlerReminders assertions. All 64 tests pass.

## v0.1.27 - 2026-06-30

- Connected real-time exchange-rate data via ExchangeRate-API: new `/api/exchange-rate` server-side route fetches CNY-base rates hourly using `EXCHANGE_RATE_API_KEY` (env var, never exposed to browser); new `lib/tools/liveToolsProvider.ts` wraps the static provider and injects live rate rows into the `currency` category when the API is reachable, falling back to static text when it is not.
- Connected Amap live POI data for Explore: new `/api/explore/amap` server-side route fetches tourist attractions (`types=110000`), restaurants (`050000`), and hotels (`100000`) per city using `AMAP_API_KEY`; new `lib/explore/amapProvider.ts` wraps the static provider and falls back city-by-city to static data when the API is unreachable.
- `lib/tools/index.ts` now returns `createLiveToolsProvider()` instead of `createStaticToolsProvider()`; `lib/explore/index.ts` now returns `createAmapExploreProvider()` instead of `createStaticExploreProvider()`. Static providers are fully preserved as fallbacks.
- All 53 existing tests continue to pass because live API calls fail gracefully (try/catch → null/empty) in the Vitest environment, leaving static data as the result.

## v0.1.26 - 2026-06-30

- Added `ButlerReminders` — a lightweight alert list rendered below the day timeline in `TripCanvas` (not at the top, not the removed five-card grid).
- Each `ButlerAlert` maps by `type` to a Tools category id (`visa`→`visa-and-entry`, `payment`→`payment-setup`, `language`→`translate`, `transport`→`metro`, `risk`/`emergency`→`emergency`) and renders as an `<a href="/tools?category=<id>">` link; unmapped types (`booking`, `weather`) render as plain text.
- `CanvasTaskStrip.tsx` remains unused — not restored, per the existing constraint against the removed top task-card grid.
- Added `tests/butler-reminders.test.tsx` covering mapped/unmapped alert rendering and updated `tests/canvas-components.test.tsx` since reminders are now intentionally rendered with working deep links.

## v0.1.25 - 2026-06-30

- Added Tools provider readiness metadata so the Tools layer documents static mode, coverage, candidate live data sources, limitations, and the first integration priority.
- `ToolsBoard` now renders the provider status alongside category content without hardcoding provider-specific copy in the component.
- Added provider status tests for the Tools abstraction.

## v0.1.24 - 2026-06-30

- Added Explore provider readiness metadata for static mode, current coverage, candidate data providers, limitations, and the next integration target.
- `ExploreBoard` now shows the provider status so users and future agents can see that the page is static today and prepared for POI/place-detail provider validation.
- Added provider status tests for the Explore abstraction.

## v0.1.23 - 2026-06-30

- Added destination-aware background scene selection for the Trip Canvas route.
- `TripCanvas` now syncs the active destination scene to the document body, and CSS switches the ink-wash atmosphere for Beijing, Shanghai/Jiangnan, Hangzhou/Suzhou, Chongqing, or the default China ink landscape.
- Added tests for the destination-to-scene mapper.

## v0.1.22 - 2026-06-30

- Implemented the Tools practicalization pass: every static Tools category now has structured sections, offline pocket notes, and an API priority note.
- `lib/tools/types.ts` and `lib/tools/staticProvider.ts` now model `sections`, `offlineTips`, and `apiPriority` so future real providers can fill the same fields without changing `ToolsBoard`.
- `components/tools/ToolsBoard.tsx` renders category tips, grouped checklist sections, offline-readable notes, and the next API integration priority.
- Expanded Tools tests to require structured sections, offline content, and API priority metadata.

## v0.1.21 - 2026-06-30

- Expanded Explore static data with Guangzhou, Hangzhou, Suzhou, and Chongqing across city summaries, attractions, food, and stays.
- Updated Explore Add to Trip messaging so every item sends the user back to Chat with a request for VisePanda to rebalance the route around the selected place.
- Added a visible note explaining that Add to Trip reopens Chat and updates the canvas through the AI planning pipeline.
- Expanded Explore provider and board tests for the new cities and rebalanced Add to Trip message.

## v0.1.20 - 2026-06-30

- Added a Trips status guide that explains Draft, Ready, Shared, and Archived states in plain language.
- Trip Detail now shows the current status meaning and next recommended action for both real Supabase-backed trips and example trips.
- Shared status copy now lives in `lib/trips/mockTrips.ts` so dashboard cards and detail pages use the same language.
- Expanded Trips Dashboard and Trip Detail tests for the status guide behavior.

## v0.1.19 - 2026-06-30

- Implemented task 5.2: Tools category deep links.
- `components/tools/ToolsBoard.tsx` now reads `/tools?category=<tool-category-id>` on mount and opens the matching category, falling back to the default category for invalid values.
- Category clicks now update the URL with `history.replaceState`, making selected Tools categories copyable and reusable from future Chat/Canvas reminder entry points.
- Added a subtle active background highlight for the selected Tools category.
- Expanded `tests/tools-board.test.tsx` to cover URL-selected categories and invalid-category fallback.
- Replaced the letter placeholders in the top navigation with Lucide icons for Chat, Trips, Explore, and Tools.
- Tightened desktop page density across Chat, Trips, Explore, and Tools: smaller headers, slimmer summary cards, tighter spacing, and internal scroll containers so the page itself stays locked to one viewport.
- Added `tests/nav-tabs.test.tsx` to cover the icon-backed primary navigation.

## v0.1.18 - 2026-06-29

- Implemented task 5.1: upgraded `/tools` from a placeholder into a real static-provider-driven skeleton covering 7 categories — Visa and entry, Payment setup, Translate, Currency, Metro, eSIM/VPN, and Emergency.
- Added `lib/tools/types.ts` (`ToolsProvider` interface, `ToolCategory` type), `lib/tools/staticProvider.ts` (static reference checklists per category), and `lib/tools/index.ts` (`getToolsProvider()` factory — the only entry point components may call).
- Added `components/tools/ToolsBoard.tsx` and replaced the Tools placeholder page with it: a category list and a detail panel showing that category's summary and tips.
- Currency/Translate copy explicitly states that live exchange-rate conversion and machine translation aren't wired up yet, to avoid implying real-time data.
- Added `tests/tools-provider.test.ts` and `tests/tools-board.test.tsx` covering the static provider's category list and the board's category-switch interaction.

## v0.1.17 - 2026-06-29

- Implemented task 4.4: Explore "Add to Trip" flow.
- `components/explore/ExploreBoard.tsx`: every attraction/food/stay item now has an "Add to Trip" button that navigates to `/chat?add=<encoded draft message>` (e.g. "Add Forbidden City in Beijing to my trip.").
- `components/chat/ButlerWorkspace.tsx`: added a one-time mount effect that reads the `add` URL param, clears it via `history.replaceState`, and calls the existing `handleSend` so the new content always goes through `/api/chat` → `CanvasPatch` → `applyCanvasPatch`, never a direct UI-side canvas write.
- Added `.explore-add-button` styles in `app/globals.css`.
- Added a navigation test in `tests/explore-board.test.tsx` and an auto-send test in `tests/chat-workspace.test.tsx`.

## v0.1.16 - 2026-06-29

- Implemented task 2.5: replaced the standalone `/account` page with a header icon + popover, and switched login from magic link to email/password and Google OAuth.
- Removed `app/account/page.tsx` and `components/account/AccountPanel.tsx`; removed `"account"` from `NavTabs`'s `AppTab` union and tab list.
- Added `components/account/AccountMenu.tsx`: a header icon button that toggles a popover. Shows guest messaging when Supabase isn't configured, a sign-in/sign-up email+password form plus a "Continue with Google" button when signed out, and Change name / Change password / Log out actions when signed in. Mounted in `AppShell`'s header next to `NavTabs`.
- `lib/supabase/auth.ts`: removed `signInWithMagicLink`; added `signInWithPassword`, `signUpWithPassword`, `signInWithGoogle`, `updateDisplayName`, `updatePassword`.
- Added `.account-menu*` styles in `app/globals.css` for the trigger button and popover.
- Added `tests/account-menu-guest.test.tsx`, `tests/account-menu-signin.test.tsx`, `tests/account-menu-signedin.test.tsx` covering the unconfigured, signed-out, and signed-in states; removed `tests/account-panel.test.tsx`.

## v0.1.15 - 2026-06-29

- Implemented tasks 4.1 and 4.2: Explore skeleton and provider abstraction.
- Added `lib/explore/types.ts`: `ExploreCity`, `ExploreAttraction`, `ExploreFoodSpot`, `ExploreStay` domain types and the `ExploreProvider` interface.
- Added `lib/explore/staticProvider.ts`: `createStaticExploreProvider()` with static data covering Beijing, Shanghai, Chengdu, and Xi'an.
- Added `lib/explore/index.ts`: `getExploreProvider()` factory — the only entry point components are allowed to call; swapping in a real Amap/Trip.com/Meituan provider later only requires changing this file.
- Added `components/explore/ExploreBoard.tsx` and replaced the Explore placeholder page with it: city filter buttons, a city summary card, and an Attractions/Food/Stays column layout that reloads when the active city changes.
- Added `tests/explore-provider.test.ts` and `tests/explore-board.test.tsx` covering the static provider's filtering behavior and the board's city-switch interaction.

## v0.1.14 - 2026-06-29

- Implemented task 3.5: trip archive state and share links.
- Added `supabase/migrations/0002_trip_archive_and_share.sql`: extends the `trips.status` check constraint to allow `archived`, and adds RLS policies so anyone can read a `trips`/`canvas_versions` row once `share_token` is set.
- Added `updateTripStatus`, `createShareLink`, `revokeShareLink`, and `loadSharedTrip` to `lib/supabase/tripsRepository.ts`.
- `TripDetail` now exposes Mark as Ready / Archive / Restore from archive buttons, plus Get share link / Revoke share link actions with a live status message and the full share URL.
- Added `app/share/[token]/page.tsx` and `components/share/ShareView.tsx`: a public, unauthenticated, read-only page that renders a shared trip's saved canvas without exposing chat history.
- `lib/trips/mockTrips.ts` gained an `archived` status and a fourth example trip; `TripsDashboard` filters now include "Archived".
- Added `tests/trip-detail-actions.test.tsx` and `tests/share-view.test.tsx` covering the new archive/share flows and the public share page.

## v0.1.13 - 2026-06-29

- Implemented task 3.4: trip detail page (`/trips/[id]`).
- Added `components/trips/TripDetail.tsx`: shows the real saved canvas (via `TripCanvas`) for signed-in Supabase-backed trips, falls back to an example-trip summary for mock trips, and shows a not-found notice otherwise.
- Added a "View details" link on each Trips Dashboard card alongside the existing "Continue in Chat" link.
- Added `tests/trip-detail.test.tsx` covering the mock-trip and not-found paths.

## v0.1.12 - 2026-06-29

- Implemented task 2.3: guest draft to logged-in synced trip migration path.
- `ButlerWorkspace` now persists an in-progress guest (not signed in, not yet saved) trip draft to `localStorage` under `visepanda:guest-draft` and restores it on remount.
- When a guest signs in via magic link while a local draft exists, the draft is now automatically saved to Supabase (`saveTripCanvas` + `appendMessage`) without the user needing to click "Save to Trips" again.
- The local draft is cleared once a trip is associated with a signed-in session (either restored from Supabase or freshly saved).
- Added `tests/chat-workspace-guest-sync.test.tsx` covering the auto-save-on-sign-in flow, and a guest-draft persistence/restore test in `tests/chat-workspace.test.tsx`.

## v0.1.11 - 2026-06-29

- Added a Supabase browser client and a `isSupabaseConfigured` guard so missing project keys never crash the app.
- Added magic-link sign-in/sign-out (`lib/supabase/auth.ts`) and a `useSupabaseSession` hook.
- Replaced the Account placeholder with `AccountPanel`: email magic-link form when Supabase is configured, guest-mode messaging when it is not.
- Added `lib/supabase/tripsRepository.ts` with `saveTripCanvas`, `listTripsForOwner`, `loadTripWithCanvas`, and `appendMessage`, all RLS-scoped to the signed-in user.
- Added a "Save to Trips" action in the Chat workspace that writes the current canvas to `trips` + `canvas_versions` and syncs chat history to `messages`.
- Trips Dashboard now loads real saved trips for signed-in users when Supabase is configured, and falls back to the existing mock trips otherwise.
- "Continue in Chat" now passes the saved trip id so the Chat workspace can restore that canvas via `/chat?trip=<id>`.
- No live Supabase project is connected yet; all of this activates once `NEXT_PUBLIC_SUPABASE_URL`, `NEXT_PUBLIC_SUPABASE_ANON_KEY`, and the migration are in place.

## v0.1.10 - 2026-06-29

- Designed the Supabase schema for `users`, `trips`, `canvas_versions`, and `messages` (task 2.2).
- Added `supabase/migrations/0001_init_trip_schema.sql` with table definitions, indexes, foreign keys, and row-level security policies scoped to trip owners.
- Added `lib/supabase/schema.ts` with TypeScript row types matching the migration, reusing `TripState`, `ChatMessage`, and `SavedTripStatus`.
- No live Supabase project is connected yet; this is the schema contract that task 3.3 persistence work will implement against.

## v0.1.9 - 2026-06-29

- Redesigned Live Trip Canvas day cards into a vertical Day 1 / Day 2 / Day 3 timeline.
- Added Morning / Afternoon / Evening blocks directly inside each day card.
- Removed the five top butler task cards from the canvas surface.
- Upgraded the day detail drawer into a local editor for city, day blocks, hotel, transport, and notes.
- Added component coverage for the new editable canvas workflow.

## v0.1.8 - 2026-06-29

- Upgraded `/trips` from a placeholder page into a saved trips dashboard skeleton.
- Added three mock trip cards with route, date, length, traveler, status, highlights, butler task count, and summary copy.
- Added All / Draft / Ready / Shared filters and summary metrics for the currently visible trips.
- Added Continue in Chat links so saved-trip work can return to the AI Butler flow.
- Added Trips Dashboard component tests.

## v0.1.7 - 2026-06-29

- Removed the default demo conversation from the chat page now that live AI is connected.
- Changed suggested prompts to a stable two-column layout instead of a clipped horizontal row.
- Added two context-aware follow-up questions to `/api/chat` responses.
- Updated the chat panel so suggestions refresh after each AI answer.

## v0.1.6 - 2026-06-29

- Added a DeepSeek V4 Flash provider for `/api/chat`.
- Routed chat submissions through the server API so provider keys stay server-side.
- Kept deterministic mock fallback for missing keys, API failures, or invalid model output.
- Updated environment placeholders to use `DEEPSEEK_API_KEY`, `DEEPSEEK_BASE_URL`, and `DEEPSEEK_MODEL`.

## v0.1.5 - 2026-06-29

- Removed day itinerary details from the main Trip Canvas surface.
- Kept each day card to a one-sentence daily summary with a details action.
- Changed the day detail view into a closed-by-default side drawer.
- Fixed the desktop landscape workspace to one viewport with internal scrolling areas.
- Removed the standalone Practical Reminder rail and merged butler reminders into the top task cards.

## v0.1.4 - 2026-06-29

- Replaced the temporary `VP` header mark with the panda icon from the supplied brand manual.
- Kept the current warm New Chinese interface direction instead of applying the full brand manual system.

## v0.1.3 - 2026-06-29

- Reduced the Live Trip Canvas heading so it takes less desktop workspace.
- Changed day cards into one-line itinerary summaries for faster scanning.
- Added a click-through day detail drawer for daily blocks, food, stay, transport, and notes.
- Kept this iteration focused on desktop landscape layout; mobile portrait refinement is deferred.

## v0.1.2 - 2026-06-29

- Restyled the Chat workspace toward the approved open ink-painting concept.
- Removed the large glass-like chat container and shifted the right side to an open conversation rail.
- Added a thin vertical divider between the live trip canvas and the chat rail.
- Added the canvas task strip for visa, payment, booking, pace, and food-focused butler work.
- Updated the trip summary and day cards to feel more integrated with the paper background.
- Recorded the production domain as `go2china.space`.

## v0.1.1 - 2026-06-29

- First working AI Butler Chat MVP skeleton.
- Added the two-column Chat + Live Trip Canvas workspace.
- Added mock canvas patching, trip cards, butler alerts, placeholder tabs, tests, and Vercel-ready structure.

## Versioning Rule

- Default iteration format is `0.1.x`.
- Each product iteration must update `package.json` and this changelog.
- Use a custom version only when the user explicitly provides one.
