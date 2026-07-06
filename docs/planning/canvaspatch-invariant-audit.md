# CanvasPatch 不变量跨端审计(对应 Issue #133)

日期:2026-07-07
性质:审计结论文档,不改代码(核实过程中未发现需要修复的绕开路径)
范围:Web、iOS。Android 当前暂停派工,待补(见文末)。

## 核心不变量

**AI 只能通过 CanvasPatch(服务端产出)+ 客户端的合并函数(Web `applyCanvasPatch`/iOS `CanvasPatchApplier.apply`)改动 Trip 的行程内容(days/blocks/tripSummary 等),不存在任何其它代码路径能让 AI 静默写入 TripState。**

## 方法

对 Web(`components/`、`app/`)和 iOS(`ios/VisePandaIOS/Data/TripStore.swift`、Views)做 grep 级别的穷举:找出所有对 trip 状态的赋值点(`setTrip(`/`trip = `),逐一分类是不是经过合法的 CanvasPatch 合并路径。

## Web 侧结果:干净,3 类合法写入路径

`components/chat/ButlerWorkspace.tsx` 是唯一持有 `trip` state 的地方,所有赋值点:

1. **AI 写入(唯一路径)**:`const nextTrip = applyCanvasPatch(previousTrip, patch); setTrip(nextTrip);`——这是本次审计要保护的核心路径,确认没有第二条。
2. **持久化数据恢复**(不是 AI 写入,是加载用户自己已保存的数据):
   - `setTrip(remote.canvas)`——从 Supabase 加载已保存的行程。
   - `setTrip(draft.trip)`——恢复本地访客草稿。
3. **明确的本地白名单动作**(代码里已有注释说明"为什么不走 AI 管线",审计确认这些注释和实际行为一致):
   - `handleUndo`:撤销是"确定性本地恢复",不是让模型重新生成——注释直接引用 ADR-070(DESIGN.md v0.2.7),原因写得很清楚:让模型"撤销上一次修改"在没有拿到权威的旧 TripState 做上下文时,只会生成一个"看起来差不多但不是原样"的结果,不可靠;本地快照恢复既快又保真。
   - `handleToggleAlertDone`:alert 的完成状态是"操作性记账"(用户自己勾选"这件事我做完了"),不是行程内容,注释引用 AGENTS.md v0.2.7,明确"这类数据直接更新 TripState,不走 AI 管线"。
   - `handleRenameTrip`:行程标题是"用户完全自主控制的标签",不是 AI 规划的内容,和 alert.done 同样的理由。

**结论**:Web 侧没有发现绕开路径。三个"本地写入"都有明确的代码注释说明理由,而且理由本身合理(不是为了偷懒绕过校验,而是这些数据本来就不属于"AI 规划的行程内容")。

## iOS 侧结果:干净,2 类合法写入路径

`ios/VisePandaIOS/Data/TripStore.swift` 是唯一持有 `trip` 的地方,全部 `trip = ` 赋值点(共 5 处):

1. **初始化/本地重置**(不是 AI 写入):`init()` 里从本地持久化恢复或用起始种子数据;`resetLocalDraft()` 手动重置为起始数据。
2. **AI 写入(仅 2 处,都经过 `CanvasPatchApplier.apply`)**:
   - `resolve(_:)`(普通 Chat 回复路径):`let updatedTrip = CanvasPatchApplier.apply(current: trip, patch: response.patch); trip = updatedTrip`
   - `completeSkeletonDetails(for:dayNumbers:)`(Trips 分阶段生成的补全轮,Issue #113/#114):同样经过 `CanvasPatchApplier.apply`,不是另开一条路径。

另外核实:
- 全仓库搜索 `$store.trip`/`store.trip.days[...] =` 这类"View 直接可写绑定进 Trip 内部数组"的模式,**零匹配**——没有发现任何 View 能绕过 TripStore 的方法直接改 Trip 内容。
- iOS 目前**没有**类似 Web 的 `handleToggleAlertDone`/`handleRenameTrip` 这类本地白名单动作(`TripStore` 里只有 `resetLocalDraft`/`retrySkeletonDetails` 两个非合并类方法,前者是重置,后者是重新触发 AI 补全,仍然会落到 `CanvasPatchApplier.apply`)——这不是一个问题,只是说明 iOS 目前没有实现"手动切换 alert 完成状态"这类本地功能,等 iOS 做这类功能时,应该参照 Web 的注释风格,明确写清楚"为什么这次写入不经过 CanvasPatchApplier"。

**结论**:iOS 侧没有发现绕开路径。

## 允许的本地写入白名单(汇总)

| 写入内容 | 平台 | 理由 |
|---|---|---|
| 撤销/回滚到本地快照 | Web | 确定性操作,AI 无法可靠重建,ADR-070 |
| Alert 完成状态勾选 | Web | 操作性记账,不是行程内容,AGENTS.md v0.2.7 |
| 行程标题重命名 | Web | 用户自主标签,不是 AI 规划内容 |
| 恢复已保存/草稿数据 | Web、iOS(init) | 加载用户自己的数据,不是新的 AI 写入 |
| 本地骨架完成状态追踪(`pendingDetailDays`/`recentlyUpdatedDays`) | iOS | UI 状态,不改 `trip.days` 实际内容本身 |

新增本地写入功能时,应该遵循同样的举证责任:写清楚"这个字段/动作为什么不需要经过 CanvasPatch",而不是默认允许。

## Android:待补

Android 当前暂停派工(2026-07-07 起),没有在本次审计范围内实际核查代码。等暂停解除、Android 重新排期时,应该用同样的方法(找出所有 Trip 状态赋值点,逐一分类)补一次核查,不能假设 Android 的实现和 Web/iOS 一致。

## 验收结论

- Web/iOS 均未发现绕开 CanvasPatch 的 AI 写入路径。
- 本次审计没有发现需要拆分修复的 Issue。
- Android 部分记录为待补,不属于本次审计范围。
