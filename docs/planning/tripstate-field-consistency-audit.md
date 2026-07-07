# TripState 多端字段一致性审计(对应 Issue #134)

日期:2026-07-07
性质:审计结论文档,不改代码(仅记录发现,高风险项拆独立 Issue)
范围:Web(`lib/types/trip.ts`)vs iOS(`ios/VisePandaIOS/Models/TripModels.swift`、`ButlerModels.swift`)。Android 当前暂停派工,待补(见文末)。

## 方法

逐个类型对照 Web/iOS 的字段名、类型、可选性,不是只看字段名字符串匹配,还追踪了几处"看起来不一致"的字段到服务端实际生产代码,确认运行时真实发生什么,而不是只看类型声明。

## 完全一致的类型(核心 Trip 内容,无差异)

`TripBlock`、`BookingCandidate`、`TripDay`、`ButlerAlert`、`TripSummary`、`TripState`、`CanvasPatch`、`AssistantResponse`、`TripSummaryPatch`(iOS)/`Partial<TripSummary>`(Web)、`InlineToolCard` —— 字段名、可选性、类型逐一核对,**没有发现差异**。`CanvasPatch` 新增的 `affectedDays`/`generationStage` 两个字段(本次 session 新增)也已经在 iOS 侧同步(PR #111/#114),核对确认命名和类型一致。

## 发现的差异(3 处)

### 1. `ChatMessage.changeDigest` —— Web 有,iOS 没有(真实缺口,但不是 bug)

Web 的 `ChatMessage` 有一个 `changeDigest?: ChangeDigestEntry[]` 字段,由 `lib/canvas/diffTripState.ts` 的 `diffTripState(previous, next)` 纯函数计算(对比 patch 前后的 `TripState`,产出 day 级"added/revised/removed"和 alert 级"new reminder"条目),渲染成 Chat 消息下方的"Change Digest"卡片(`components/chat/ChatPanel.tsx` 消费)。iOS 的 `ChatMessage` 完全没有这个字段,也没有等价的本地 diff 逻辑。

**不是解码风险**:字段可选,双方都不会因为对方没有这个字段而崩溃。

**是一个真实的功能缺口,而且和已经开的 Issue #124 直接相关**:Issue #124("Chat回答展示CanvasPatch改动摘要")本质上就是在 iOS 上重新发明 Web 这个已经跑通的功能。已在 Issue #124 补充评论,建议 Codex 直接参考 `lib/canvas/diffTripState.ts` 的算法(day-level 内容 key 对比 + alert key 对比),移植成 Swift,而不是从零设计——这份审计的价值就在这里:省掉 Codex 重新摸索的时间。

### 2. `ExploreRef.pricePerPerson` —— 类型声明看起来不一致,实际有防御性处理,核实后不是问题

Web 类型是 `pricePerPerson?: number`,服务端 `buildExploreRefs`(`lib/ai/toolContextWriteThrough.ts`)也确实用 `toNumber(...)` 转成真正的数字类型再放进 JSON。iOS 的 `ButlerExploreRef.pricePerPerson` 声明是 `String?`,乍看类型不匹配,应该会解码失败。

**追踪到 iOS 的自定义解码逻辑**(`ButlerModels.swift` 的 `init(from decoder:)` 用了 `decodeFlexibleString(forKey:)`,定义在 `ExploreModels.swift`)——这是一个专门写来处理"服务端字段可能是数字也可能是字符串"的 flexible decoder,`rating`/`cost`/`tel`/`address` 等字段也在用同一套机制。**核实结论:不是 bug,是已经被正确防御的已知模式**,值得在这里记录下来,避免以后有人看到类型声明"不一致"又重新报一次。

### 3. `ButlerExploreRef.editorial` —— iOS 有字段和 UI 逻辑,Web 服务端从未填充(死代码,不紧急)

iOS 的 `ButlerExploreRef` 有 `editorial: Bool?` 字段,`ChatView.swift` 里还有 `if ref.editorial == true` 的 UI 分支(应该是渲染一个"编辑精选"徽标)。但 Web 的 `ExploreRef` 类型(`lib/types/trip.ts`)根本没有 `editorial` 字段,服务端 `buildExploreRefs` 也从未设置它——这个字段在当前代码库状态下**永远是 nil**,对应的 UI 分支实际上是死代码。

推测这是预留给"把 Explore 的 curated_pois 编辑精选标记也带进 Chat 的 exploreRefs 卡片"这个方向准备的(和现在正在做的 curated 知识库工作——Issue #49/#53——是同一个概念),只是服务端那一半没有跟着实现。**不紧急,不建议现在顺手接上**(这会牵扯到 `buildExploreRefs` 要不要查 `curated_pois` 表,是一个新功能范围,不是这次审计的修复项),但值得记录,以免以后有人以为这是个已经上线的功能。

## Android:待补

Android 当前暂停派工(2026-07-07 起),没有在本次审计范围内核查 Android 端的对应 model 文件。等暂停解除后,应该用同样的方法(逐类型对照 + 追踪可疑差异到服务端生产代码)补一次。

## 验收结论

- 核心 Trip 内容类型(TripBlock/TripDay/TripSummary/TripState/CanvasPatch等)Web/iOS 完全一致,没有需要修复的问题。
- 发现 3 处差异,1 处是真实功能缺口(已关联到 Issue #124,补充了实现建议),1 处是"看起来不一致但已被正确防御"(记录避免误报),1 处是预留但未接通的死代码(记录,不建议现在处理)。
- 没有发现需要立即修复的高风险差异(不会导致解码失败或数据损坏)。
