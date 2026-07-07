# Trips 分阶段生成落地设计(对应 Issue #78)

版本:v1(设计文档,定案后开始实施)
日期:2026-07-07
性质:技术方案设计
前置:Issue #78 "[Backend+跨端] Trips 分阶段生成"

## 0. 结论先行:分两个阶段做,现在只做阶段 A

操作者确认"两轮请求和真流式都做"——两个方案不是二选一,是先后关系:

- **阶段 A(现在做)**:两轮 HTTP 请求,不需要新增流式基建。第一轮只生成 summary + 每天的骨架(city/pace/food/stay/transport/note),`blocks` 留空,立刻返回给客户端渲染雏形;客户端收到骨架后自动(无用户感知)发起第二轮请求,补全每天的 `blocks`。三端改动都不大,复用现有的"一次 HTTP 请求 = 一个完整 patch"模型两次,不用碰 `/api/chat` 的响应格式。
- **阶段 B(后续做,本文档不展开)**:`/api/chat` 改成 SSE,LLM 每生成完一天就推一次——真正的渐进式体验,但三端都要新增流式解析能力,是这个项目目前没有的基建,工作量和风险都大得多,值得单独立项,不和阶段 A 混在一起做。

本文档只设计阶段 A。

## 1. 范围边界:只影响 `create_trip`

`adjust_trip`(方案 B 的 `TripEditIntent` 路径)和 `add_alerts` 完全不受影响——分阶段生成要解决的是"从零生成一整个多日行程,一次 LLM 调用要等全部天数生成完"这个延迟问题,只有 `create_trip` 有这个问题。

## 2. 契约变更

### 2.1 `CanvasPatch` 新增字段

```ts
interface CanvasPatch {
  // ...现有字段不变...
  /**
   * Only present for create_trip. "skeleton" means days[].blocks is
   * intentionally empty — city/pace/food/stay/transport/note are real, but
   * the day-by-day details haven't been generated yet; the client should
   * render a lightweight placeholder and expect a follow-up "complete"
   * patch for the same days. "complete" (or absent, for every other
   * intent) means blocks are fully generated as today.
   */
  generationStage?: "skeleton" | "complete";
}
```

**向后兼容**:老客户端(还没升级消费 `generationStage` 的 Android)看到 `blocks: []` 的骨架 patch,和"这天没有安排"的现有空状态渲染完全一样,不会崩溃、不会显示错误内容——只是暂时看不到雏形上的"生成中"提示,体验退化但不出错。这是刻意的设计:Android 暂停期间不需要同步改动也能安全兼容。

### 2.2 请求侧新增一个内部请求模式

`/api/chat` 新增一个可选请求字段:

```ts
{
  message: string,
  trip: TripState,
  messages: ChatMessage[],
  preferenceProfile?: UserPreferenceProfile,
  // 新增,仅阶段 A 使用:
  completeSkeletonFor?: TripState  // 骨架版 trip(即上一轮 patch 应用后的 trip.days,blocks 全空)
}
```

当 `completeSkeletonFor` 存在时,这是一次"补全骨架"请求,不走正常的 intent 分类/工具前缀逻辑——直接进入补全 prompt(见 2.3),`message` 字段被忽略(客户端可以传空字符串)。

### 2.3 Prompt 改动(`lib/ai/butlerPrompt.ts`)

新增两个 system prompt 变体,复用现有 `SHARED_PERSONA_AND_HARD_RULES`/`SHARED_STYLE_TAIL`:

- `buildSkeletonSystemPrompt()`:在 `buildFullArraySystemPrompt()` 基础上加一句硬性约束——"每天的 blocks 必须是空数组 `[]`;只生成 tripSummary、每天的 city/pace/food/stay/transport/note。这是第一轮快速响应,详细行程会在第二轮单独生成。"
- `buildSkeletonCompletionSystemPrompt()`:输入是已经确定的骨架(city/pace/day 数量已锁定),提示词明确"这是第二轮:每天的 city、pace、总天数已经由第一轮锁定,不能修改、不能增删天数——只需要为每天填充 blocks(以及在必要时补充/微调 food、stay、transport、note)"。这本质上是 `create_trip` 全量生成路径的一个变体,不是新的 `TripEditIntent` op。

### 2.4 `lib/ai/orchestrator.ts` / `app/api/chat/route.ts` 改动

- `route.ts`:识别 `body.completeSkeletonFor`,如果存在就调用一个新函数(如 `requestSkeletonCompletion(skeletonTrip, env, fetchImpl)`),否则维持现有 `requestOrchestratedButlerPatch` 调用不变;但如果这是一次普通 `create_trip` 请求,`requestOrchestratedButlerPatch` 内部改为先用 `buildSkeletonSystemPrompt()` 生成骨架,返回时打上 `generationStage: "skeleton"`。
- 是否要在同一个请求里“自动”触发补全(服务端悄悄再打一次 LLM 再一起返回)?**不要**——那样又变回单次请求等待全部生成完,违背了这次要解决的延迟问题。骨架必须尽快独立返回,补全必须是客户端发起的第二次独立请求。

## 3. 客户端改动(阶段 A 范围:iOS + Web;Android 按暂停政策不动)

1. 收到 `generationStage === "skeleton"` 的 patch 后,立刻按现有 `CanvasPatchApplier`/`applyCanvasPatch` 合并逻辑应用(`blocks: []` 本来就是合法的空状态,不需要新的合并分支)。
2. 应用完骨架后,不等待用户下一次输入,自动在后台发起第二次请求(`completeSkeletonFor: <应用骨架后的 trip>`),这次请求**不产生新的 chat 消息气泡**(用户没有主动说话,不应该看到"用户消息"或多一条"assistant 回复"),只是静默拿到 `generationStage: "complete"` 的 patch 并合并进 `trip.days`。
3. UI 层在 `blocks.isEmpty` 且这天在 `recentlyUpdatedDays`(或等价机制)里时,渲染一个轻量"Generating details…"占位,而不是"这天还没有安排"的老空状态文案——避免用户以为骨架就是最终结果。
4. 失败处理:补全请求失败(网络、LLM 全部炸)时,骨架保留原样(city/pace 仍然有效、可用),不重试轰炸——DayDetail 给一个"Details didn't load — tap to retry"的手动重试入口,避免静默卡在骨架状态。

## 4. 验收标准

- `adjust_trip`/`add_alerts` 路径完全不受影响,`generationStage` 字段对它们始终是 `undefined`。
- 单元测试覆盖:骨架 prompt 生成的 `days[].blocks` 必须是空数组;补全 prompt 收到的骨架 city/pace/天数在返回结果里必须保持不变(校验层拦截天数增减或 city 改变的补全结果,视为补全失败,保留骨架不崩溃)。
- 真实 provider 调用验证:一次 create_trip 请求确认骨架先快速返回(相对全量生成明显更快),紧接着补全请求返回完整 blocks,且 city/pace/天数与骨架一致。
- iOS 端:真实触发一次多日 create_trip 请求,Trips 页面先看到雏形(city/pace 卡片,blocks 区域显示"生成中"),几秒后自动补全,不需要用户额外操作,也不多出聊天气泡。

## 5. 后端实施状态(已完成,真实 DeepSeek 调用验证)

- `CanvasPatch.generationStage?: "skeleton" | "complete"` 已加入 `lib/types/trip.ts`。
- `lib/ai/butlerPrompt.ts` 新增 `buildSkeletonSystemPrompt()`/`buildSkeletonCompletionSystemPrompt()`,`buildSystemPrompt(intent, mode)`/`parseButlerPatch(..., mode)` 均已支持 `mode: "normal" | "skeletonCompletion"`;`parseButlerPatch` 对骨架轮**强制**把每天的 `blocks` 清空(不管模型是否遵守提示词),对补全轮做结构校验(day/city/pace 任何一项被模型改动就直接抛错,不吞掉、不生成半成品)。
- `lib/ai/orchestrator.ts` 新增导出 `requestSkeletonCompletion(input: { skeletonTrip: TripState; ... })`——独立于 `requestOrchestratedButlerPatch`,不跑意图分类、不跑 tool-context 预取(没有自然语言 message 可用于生成搜索词),失败时直接抛错(不落回 mock,理由见函数注释:通用 mock 内容和骨架的具体 city 对不上,比"保留骨架、可重试"更误导人)。
- `app/api/chat/route.ts` 新增识别 `body.completeSkeletonFor`(存在时完全跳过 butler-service 转发和 `requestOrchestratedButlerPatch`,直接调用 `requestSkeletonCompletion`;`affectedDays` 的 diff 基准相应改成骨架而不是调用方的 `currentTrip`)。
- 真实调用结果(DeepSeek):第一轮返回 3 天骨架,每天 `blocks: []`、city/pace/food/stay/transport/note 均正常生成;第二轮拿到骨架后,返回的 3 天 city("Beijing"×3)、pace("Balanced"×3)、day 编号(1/2/3)与骨架逐项一致,`blocks` 补全为每天 3 个真实 block——两轮之间的契约完全按设计工作,没有触发本文档设计的"结构变化即报错"校验路径(说明正常情况下模型是配合的,校验只是兜底)。
- **测试覆盖**:`tests/butlerPrompt.test.ts` 新增 6 个用例(骨架强制清空 blocks、adjust_trip/add_alerts 不带 generationStage、补全成功、补全改天数/city/pace 三种情况下正确拒绝)。`tests/orchestrator.test.ts` 新增 3 个用例(`requestOrchestratedButlerPatch` 对 create_trip 打上 skeleton 标签、`requestSkeletonCompletion` 成功补全、`requestSkeletonCompletion` 全部 provider 失败时抛错而不落回 mock)。全仓库 `npx vitest run` 265 个测试全过,`npx tsc --noEmit` 未引入新的类型错误。

## 6. 客户端待办(下一步:iOS 优先,Android 暂停)

后端契约已经就绪,可以安全地基于它开发客户端消费逻辑。按当前"iOS 主线、Android 暂停派工"的政策,先只给 Codex 开 iOS 任务;Web(本仓库同时维护的参考实现)由架构师跟进;Android 等政策解除后再补。
