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

## 6. 分阶段实施计划

### 阶段 1：Tier 1 精确操作，先只做 `add_block` / `remove_block` / `update_block`
最高频、最容易验证正确性的三个操作，覆盖"加一个地点""删一个地点""改一下这个地点的描述/时段"这几类最常见的编辑请求（对应分类器的 `add_poi` 和一部分 `adjust_trip`）。

### 阶段 2：补齐 `move_block` / `set_day_field` / `add_day` / `remove_day`
覆盖"调整顺序""改这天的节奏/住宿/交通说明""加一天/删一天"。

### 阶段 3：`replace_day_blocks`（Tier 2 逃生舱）
覆盖"重新安排今天的节奏"这类没法精确映射成单个 op 的请求，作为兜底路径。

### 阶段 4（如果前三阶段验证顺利）：评估 `create_trip` 是否也能部分结构化
比如"先生成骨架（每天去哪个城市、大致节奏），再逐天补细节"——这是一个更大的改动，需要重新评估，本文档不展开，先把 `adjust_trip` 这条路径做扎实。

每个阶段都应该独立提测试、独立可以回滚（如果某个阶段的 provider 输出质量不理想，可以只回退这一个阶段新增的 op，不影响已经验证过的其它 op）。

## 7. 验收标准（贯穿所有阶段）

- 客户端（iOS/Android/Web）零改动，`patch.days` 字段的形状和现在完全一样。
- 每个 Tier 1 op 都有对应的单元测试（纯函数，不需要真的调 LLM 就能测）。
- 校验失败时不改变行程，assistantMessage 诚实说明没有成功，不能出现"界面说改好了但内容没变"的情况。
- `create_trip` 路径的现有行为（含方案 A 的归一化加固）完全不受影响。
- 至少完成阶段 1 后，用真实 provider（DeepSeek/Kimi/Qwen/GLM 至少各跑几次）验证输出 `TripEditIntent` 的稳定性，作为决定要不要继续阶段 2/3 的依据。
