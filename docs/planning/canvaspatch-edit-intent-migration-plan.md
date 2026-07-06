# CanvasPatch 迁移方案：意图识别 + 后端代码 CRUD（方案 B 落地设计）

版本：v1（设计文档，待确认后拆 Issue）
日期：2026-07-07
性质：技术方案设计，供实施前对齐范围和分阶段计划
前置文档：`docs/planning/canvaspatch-architecture-assessment.md`（此前评估"现在不做"的结论被本轮操作者决定推翻，本文档是"确认要做"之后的具体落地设计）

## 0. 结论先行：比之前评估的成本低，因为选对了迁移策略

上一版评估文档里说方案 B"影响面大，需要重新设计整个契约和三端的行程更新逻辑"——这个判断在**"客户端自己执行 CRUD"**这个实现策略下是对的，但不是唯一的实现策略。本文档采用另一种策略：

> **LLM 只输出意图+参数，但由服务端代码执行 CRUD 后，仍然把算好的完整 `days` 数组放进 `patch.days` 返回给客户端。**

这样一来，**客户端消费逻辑完全不用变**——iOS/Android/Web 三端现在怎么消费 `patch.days`（`applyCanvasPatch`/`CanvasPatchApplier`），迁移后还是怎么消费，因为它们看到的永远是"改完之后的完整数组"，只是这个数组现在是服务端代码算出来的，不是 LLM 直接吐出来的。**这次迁移 100% 是后端/Web 侧工作，不需要 iOS/Android 端做任何改动。**

## 1. 范围边界：只改 `adjust_trip`，`create_trip` 不动

`create_trip`（从零生成一个全新的多日行程）本质上就是"发明内容"，没有比"完整 days 数组"更小的意图表达方式——你没法把"帮我规划一个 5 天北京行程"拆成几个 CRUD 操作。这部分继续走现有的"LLM 生成完整数组 + 归一化层校验"路径不变，之前做的归一化加固（`extractBlocksFromLegacyPeriods` 等）对这条路径依然有效且必要。

**只有 `adjust_trip`（修改一个已存在的行程）适合迁移**，因为编辑操作天然是可枚举的小动作："把长城换成颐和园""第二天加一个胡同游""把博物馆挪到下午"——这些都能表达成"意图 + 参数"，不需要模型重新生成整个多日数组。

## 2. 一个有用的既有信号：`ButlerIntent` 分类器已经区分了编辑类型

`lib/ai/intentClassifier.ts` 的 `classifyIntent()` 是一个纯规则的本地分类器（不调用 LLM），当前只用来做 provider 路由，但它已经能区分出比 `CanvasPatch.intent` 更细的编辑类型：

- `add_poi`："add the Summer Palace to my trip" 这类——加一个地点。
- `add_location`："add a day in Suzhou" 这类——加一个城市/天数。
- `adjust_trip`："make day 2 lighter"、"swap X for Y" 这类——调整已有内容。
- `create_trip`：从零规划。

这个分类器已经帮我们把"这次请求大概率是哪种编辑动作"提前算好了，本方案直接复用它来决定"这次该走新的意图执行路径，还是继续走旧的整体重新生成路径"，不需要重新发明一层分类逻辑。

## 3. 新增的编辑意图 schema（`TripEditIntent`）

两个层级：

### 3.1 Tier 1——精确操作（优先，风险最低）

```ts
type TripEditIntent =
  | { op: "add_block"; day: number; block: TripBlock; position?: number }
  | { op: "remove_block"; day: number; blockIndex: number }
  | { op: "update_block"; day: number; blockIndex: number; patch: Partial<TripBlock> }
  | { op: "move_block"; day: number; fromIndex: number; toIndex: number }
  | { op: "set_day_field"; day: number; field: "pace" | "note" | "stay" | "transport"; value: string }
  | { op: "add_day"; afterDay: number; content: TripDay }
  | { op: "remove_day"; day: number }
  | { op: "replace_day_blocks"; day: number; blocks: TripBlock[] }; // Tier 2，见下
```

每个 op 都对应一个纯函数（`lib/canvas/tripEditIntents.ts`，新文件），输入当前 `TripDay[]` + 这个意图，输出新的 `TripDay[]`，结构完整性由 TypeScript 类型和函数逻辑保证，不依赖 LLM 输出的 JSON 形状。

### 3.2 Tier 2——`replace_day_blocks`（逃生舱，仍然有边界）

有些请求没法精确映射成单个 op（比如"重新安排一下今天的节奏"），给模型一个"重写这一天的 blocks"的逃生舱，但**范围被强制限定在这一个 day**，不是像现在这样重新生成整个 N 天数组。这个 op 的 `blocks` 字段依然要过方案 A 已经做的归一化层校验（`normalizeDays` 里对单个 day 的 blocks 校验逻辑可以直接复用，只是现在只需要跑一个 day 而不是 N 个），**方案 A 的加固工作不是白做，是方案 B 里 Tier 2 的安全网**。

## 4. 后端改动点

1. **`lib/canvas/tripEditIntents.ts`（新文件）**：定义 `TripEditIntent` 类型 + 每个 op 的纯函数执行器 + 一个 `applyTripEditIntent(days: TripDay[], intent: TripEditIntent): TripDay[]` 总入口。
2. **`lib/ai/butlerPrompt.ts`**：
   - `buildSystemPrompt` 分两套：`create_trip` 走现有的"完整 days 数组"提示词；`adjust_trip` 走新的"输出一个 TripEditIntent"提示词，给出 8 个 op 的说明和例子。
   - `parseButlerPatch` 分支：`intent === "adjust_trip"` 时，校验的是 `TripEditIntent` 这个小得多的 schema，不是整个 `TripDay[]`；校验通过后调用 `applyTripEditIntent(currentTrip.days, editIntent)` 算出新的 `days`，塞进 `patch.days`（对客户端而言和现在返回的字段完全一样）。
3. **`app/api/chat/route.ts`**：不需要改——`affectedDays` 计算逻辑已经是"diff 请求时的 days 和 patch.days"，天然适配新路径（甚至更精确，因为我们现在确切知道改的是哪一天，不需要纯靠 diff 猜）。
4. **`lib/ai/orchestrator.ts`**：并发赛跑机制不变——多个 provider 各自尝试输出 `TripEditIntent`，谁先给出一个能通过 schema 校验的意图就赢，schema 校验比"整个多日数组是否合理"便宜得多、也更明确，**赛跑判定"谁赢了"这一步反而变简单了**（这一点修正了上一版评估文档"复杂度并没有减少多少"的判断——小 schema 的校验成本和确定性都优于大数组）。

## 5. 失败处理：比现在更诚实

如果模型吐出的 `TripEditIntent` 没通过 schema 校验（比如 `op` 不在枚举里、缺必填参数、`blockIndex` 越界），**直接判定这次 patch 无效，不修改行程**，assistantMessage 诚实告知"这次没能完成这个调整，请换个说法再试一次"——比现在"内容被静默丢弃但界面显示成功"的失败模式更安全，符合项目一贯的"失败要诚实"原则。

**特例**：`editIntent` 字段整个缺失(不是格式错误,是模型判断"这次回复不需要改行程")视为合法的"无变更"信号,和现有全量路径里"不改行程时可以省略 `days` 字段"的规则语义一致,`patch.days` 会是 `undefined`,不会报错、也不会误判成校验失败。

## 6. 已验证的真实发现

### 6.1 阶段 1(`add_block` / `remove_block` / `update_block`,用真实 DeepSeek/Qwen/Zhipu/Moonshot 调用验证)

1. **端到端验证通过**:真实调用"Change the Forbidden City description on day 1..."这类请求,确认走了新路径(`intent: "adjust_trip"`),模型正确输出 `update_block` editIntent,服务端执行后只改了目标字段,其余字段(地址/开放时间/预订信息/坐标等)完全没被模型碰过、原样保留——这正是这次迁移要解决的核心问题的直接证据。
2. **发现并修复了一个真实的提示词歧义**:最初的提示词只说"blockIndex are 0-based",没有明确说 `day` 字段本身是 1-based(和 `TripDay.day` 现有约定一致)。四个 provider(DeepSeek/Qwen/Zhipu/Moonshot)全部一致地把 day 1 理解成 `day: 0`,被校验层正确拦截(`Day 0 does not exist`),不会误改。已经修正提示词措辞明确"day 是 1-based,day 1 是第一天,永远不会是 0"。
3. **已知的路由覆盖率缺口(记录,当时未处理,阶段2/3也维持不处理)**:这条新路径是否被触发,取决于现有的 `intentClassifier.ts` 里 `adjust_trip` 的正则规则(`make|change|adjust|rebalance|swap|replace|less|more|lighten|shorten|extend`)。实测发现,很多用户会自然说出的"remove the Temple of Heaven from day 1"("remove"不在规则里)或"add X to day 1"(不一定命中 `add_poi` 的精确正则,反而更容易先命中 `add_location`)这类请求,目前会被分类成 `unclear`/`add_location`/`create_trip` 等其它类别,从而继续走旧的全量生成路径,不会经过这次新加的安全通道。这不是 bug——`intentClassifier.ts` 是全系统共用的分类器(还用来做 provider 路由和 token 预算选择),扩大它的匹配规则影响面超出这次迁移的范围,需要单独评估、单独测试,不应该顺手改掉。记录为后续阶段的候选项:要么谨慎扩大 `adjust_trip` 的正则覆盖(需要补足够的回归测试防止误伤其它路由决策),要么给"是否走 edit-intent 路径"单独做一个更精确的判定,不复用广义的 `ButlerIntent`。

### 6.2 阶段 2/3(`move_block` / `set_day_field` / `add_day` / `remove_day` / `replace_day_blocks`,真实 DeepSeek 调用验证)

1. **同样的分类器覆盖率缺口在阶段 2/3 的自然措辞下更明显**:用最直觉的英文表达("Add a new day after day 1 for a Suzhou excursion"、"remove day 2 entirely"、"completely redo the schedule for day 2")实测发现,这些请求分别被分类成 `add_location`、`unclear`、`create_trip`,全部没有落进 `adjust_trip`,因此走的是旧的全量生成路径,不会经过新增的 `add_day`/`remove_day`/`replace_day_blocks` 执行器——这与 6.1 记录的缺口是同一个根因,再次确认不应该在这几个阶段顺手扩大分类器覆盖。
2. **改用命中 `adjust_trip` 现有触发词(change/replace)的措辞后,三个新 op 全部端到端验证通过**:
   - `add_day`:"Change the schedule: put one extra day right after day 1 for a Suzhou garden excursion." → 正确插入 Suzhou 为新的 day 2,原 day 2(Great Wall)自动重新编号为 day 3,day 1 完全未被改动。
   - `remove_day`:"Change my itinerary and delete day 2 entirely, we're skipping the Great Wall." → 正确删除 day 2,剩下的 day 1 保留原样、编号不变。
   - `replace_day_blocks`:"Replace everything happening on day 2, rebalance it into a completely different set of stops." → 只重写了 day 2 的 blocks(替换成 Temple of Heaven / Hutong / Peking Opera 全新安排),day 1 完全未被改动。
3. **结论**:三个新执行器的解析、校验、重新编号逻辑在真实 provider 输出下工作正常;剩余风险 100% 集中在 6.1 已记录的分类器覆盖率缺口上,不是这几个 op 本身的问题。

### 6.3 阶段 4:`create_trip` 是否部分结构化的评估结论——暂不做

评估结论是**暂不做**,原因:

1. **`create_trip` 没有天然的"小意图"分解**。`adjust_trip` 能拆成 CRUD 是因为请求本身就是"改一个东西"(有明确的目标字段/目标天数);`create_trip` 是从零发明一整段内容,唯一能表达"生成前 3 天骨架、再补细节"的分解方式是让模型分两轮调用(先生成骨架 `days` 的浅层字段,再对每天单独生成 `blocks`),这实质上是引入一个全新的多轮编排状态机,而不是给 `TripEditIntent` 加一个新 op——工作量和这次 `adjust_trip` 迁移不在同一量级。
2. **`create_trip` 当前路径的失败模式已经被方案 A 的归一化加固覆盖**(`extractBlocksFromLegacyPeriods` 等),不存在方案 B 要解决的"模型悄悄改坏无关字段"的核心风险——`create_trip` 每次都是全新生成,没有"无关字段被误改"这个问题,所以方案 B 的核心收益(保护已有字段不被误碰)对 `create_trip` 不成立。
3. **建议**:如果未来要优化 `create_trip`,应该独立立项(比如"分步生成 + 逐天流式返回"改善首字延迟体验),而不是套用这次的 `TripEditIntent` 框架。本迁移到此为止,不再展开 `create_trip` 改动。

## 7. 分阶段实施计划(已全部完成)

### 阶段 1(已完成)：Tier 1 精确操作，先只做 `add_block` / `remove_block` / `update_block`
最高频、最容易验证正确性的三个操作，覆盖"加一个地点""删一个地点""改一下这个地点的描述/时段"这几类最常见的编辑请求（对应分类器的 `add_poi` 和一部分 `adjust_trip`）。

### 阶段 2(已完成)：补齐 `move_block` / `set_day_field` / `add_day` / `remove_day`
覆盖"调整顺序""改这天的节奏/住宿/交通说明""加一天/删一天"。`add_day`/`remove_day` 统一采用"执行后对所有天重新按顺序编号"的策略，模型只需要给出 `afterDay`（已存在的天数，或 0 表示插到最前面），不需要自己算下游天数。

### 阶段 3(已完成)：`replace_day_blocks`（Tier 2 逃生舱）
覆盖"重新安排今天的节奏"这类没法精确映射成单个 op 的请求，作为兜底路径。范围严格限定在一天以内，`blocks` 字段的校验沿用方案 A 的容错逻辑（默认值而非报错），不是 Tier 1 的严格校验。

### 阶段 4(已完成，评估类)：结论是 `create_trip` 暂不做部分结构化
详见 6.3。

每个阶段都独立有单元测试，阶段 2/3 新增的 5 个 op 均已用真实 provider 调用验证（详见 6.2），必要时可以只回退某一个阶段新增的 op，不影响已经验证过的其它 op。

## 8. 验收标准（贯穿所有阶段）

- 客户端（iOS/Android/Web）零改动，`patch.days` 字段的形状和现在完全一样。
- 每个 Tier 1 op 都有对应的单元测试（纯函数，不需要真的调 LLM 就能测）。
- 校验失败时不改变行程，assistantMessage 诚实说明没有成功，不能出现"界面说改好了但内容没变"的情况。
- `create_trip` 路径的现有行为（含方案 A 的归一化加固）完全不受影响。
- 至少完成阶段 1 后，用真实 provider（DeepSeek/Kimi/Qwen/GLM 至少各跑几次）验证输出 `TripEditIntent` 的稳定性，作为决定要不要继续阶段 2/3 的依据。

**全部四个阶段现已完成**：8 个 op（`add_block`/`remove_block`/`update_block`/`move_block`/`set_day_field`/`add_day`/`remove_day`/`replace_day_blocks`）均已实现、有单元测试覆盖、并用真实 DeepSeek 调用验证过端到端行为；阶段 4 的评估结论是 `create_trip` 暂不做部分结构化（见 6.3）。已知遗留风险仅剩 6.1/6.2 记录的 `intentClassifier.ts` 覆盖率缺口，不属于本迁移范围。
