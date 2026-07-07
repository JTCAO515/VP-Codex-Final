# Show to Local 多场景卡组 — 功能规格

版本：草案 v1
日期：2026-07-06
状态：规格文档，未排期实现（等待架构师/操作者确认后拆 Issue）
适用范围：iOS 优先设计（2026-07-06 起 iOS 为主线平台），Android 追平

## 0. 背景

四模型对抗性评审第一轮里，Kimi 和 GLM 独立收敛到同一个建议：现有 Show to Local（DayDetail 里给司机看的中文地址大字卡）不该止步于地址场景，应该扩成一组固定场景的卡片——过敏/忌口给服务员看、症状给医生/药店店员看、地址给司机看。这是评审里罕见的"低成本、高共识"建议：UI 交互模式已经在生产环境验证过（大字卡+朗读+复制），只是换内容，不需要新建交互范式。

本文档只定义规格，不涉及实现。

## 1. 现状（不改动的部分）

**iOS**（`ios/VisePandaIOS/Views/Trips/DayDetailView.swift`）：
- `LocalAddressCard { title, address, detail }` — 每个有地址的 Trip block 旁边有一个 "Show to Local" 按钮，弹出 `ShowToLocalSheet`：大字中文地址 + `AVSpeechSynthesizer` 朗读 + 复制到剪贴板。
- 触发点唯一：DayDetail 页面里逐个 block。

**Android**（`android/app/src/main/kotlin/.../ui/components/TravelTalkCard.kt`）：
- `TravelTalkCardButton(block: TripBlock)` 弹出 `TravelTalkCardDialog`：52sp 大字中文地址 + `TextToSpeech` 朗读 + 复制。
- 触发点：Trips 时间线卡片 + DayDetail。

两端的核心交互（大字 + 朗读 + 复制）已经一致，本次只是把"卡片能装的内容"从 1 种扩到 3 种，不改动交互范式本身。

## 2. 新增的三种卡片类型

### 2.1 Address（现有，不改动语义，只是纳入统一类型系统）

- 数据来源：`TripBlock.chineseAddress ?? TripBlock.address`（iOS）/ 对应 Android `TripBlock` 字段。
- 触发点：不变（DayDetail 逐 block、Android Trips 时间线卡片）。

### 2.2 Allergy / Dietary（新增，P0）

**目的**：给餐厅服务员看，说明忌口/过敏，不需要用户临场描述。

**数据模型**（新增，轻量，本地存储即可，不需要后端）：
```
enum DietaryRestriction: String, CaseIterable {
  case peanut, treeNut, seafood, dairy, gluten, spicyIntolerant, vegetarian, halal, kosher
}
```
用户在 Me 或 Translate 里勾选（多选），本地持久化（iOS `@AppStorage` / Android `DataStore`，和现有语言偏好存储方式一致，不新建存储层）。

**卡片内容**：每个 `DietaryRestriction` 对应一句**人工预翻译、固定文案**的中文短语（不用 LLM 现场生成——过敏场景翻译错一个字有真实健康风险，必须是审校过的静态文案，这是四模型评审里反复强调的原则："医疗/过敏信息不能用未经校验的 AI 生成内容"）。用户可多选，卡片把选中的几条拼成一张（例如同时勾选 peanut + spicyIntolerant，卡片显示两行中文）。

参考文案基调（需要人工审校，不是最终版）：
- peanut → "我对花生过敏，请不要放花生，谢谢。"
- seafood → "我对海鲜过敏，请不要放海鲜类食材。"
- spicyIntolerant → "我不能吃辣，请做无辣或微辣。"
- vegetarian → "我吃素，请不要放肉类和肉汤。"
- halal → "我只吃清真食品，请确认食材和调料符合清真要求。"

**触发点**：
- Tools → Translate 页面（现有 Quick Phrases 区域旁新增入口）。
- Trips：如果某个 block 被标记为用餐相关（`TripBlock` 已有 `food` 分类信息可复用判断），在该 block 旁增加同样的 Show to Local 入口，直接跳过来。

### 2.3 Symptom（新增，P0）

**目的**：给药店店员/医生看，说明症状，不需要用户临场描述，且不构成诊断建议。

**数据模型**：
```
enum CommonSymptom: String, CaseIterable {
  case headache, stomachache, diarrhea, fever, nausea, allergicReaction, difficultyBreathing
}
```
单次选择（不像过敏是长期偏好，症状是临场选的，不持久化，每次用完即弃）。

**卡片内容**：同样是**人工预翻译的固定文案**，不经 LLM：
- headache → "我头疼。"
- stomachache → "我肚子疼。"
- diarrhea → "我拉肚子。"
- fever → "我发烧了。"
- allergicReaction → "我可能过敏了，身上起了疹子/肿了。"
- difficultyBreathing → "我呼吸困难，请帮我叫救护车。"（这条应该同时触发页面上的紧急电话按钮，不能只给一张卡片就完事）

**边界**（沿用项目一贯的医疗免责原则）：
- 卡片底部必须有一行小字免责声明："这不是医疗诊断，请尽快就医。"
- `difficultyBreathing` 这类高风险症状选中后，卡片旁必须联动展示中国紧急电话（120/110），不能让用户以为"给店员看了卡片"就等于处理完了。

**触发点**：Tools → Emergency 分类页面内（`ios/VisePandaIOS/Views/Tools/ToolsView.swift` 已有 `id: "emergency"` 分类，目前是静态提示页——这个卡片是把它从纯静态说明升级为交互组件的第一步，参照 Visa Checker/Payment Wizard 已经验证过的"静态分类升级为交互 Widget"模式，不是新范式）。

### 2.4 Hotel Check-in Credential（评估后决定：本轮不做）

Kimi/GLM 原始建议里还提到"入住凭证给前台看"这第四种卡片。评估后**建议排除在本轮之外**，原因：现有数据模型里没有任何结构化的酒店预订信息（`TripBlock`/`TripDay` 只有 `stay: String` 一个自由文本字段，没有确认号/入住日期/房型等结构化字段），要做这张卡片需要先建一个新的 Booking 数据模型，这不是"低成本复用现有交互"，而是新开一块工程范围，和本次"低成本高共识"的初衷不符。等 Codex 那份文档里提到的 Arrival Setup / Booking 数据模型工作真的排上日程时再重新评估这张卡。

## 3. 统一组件契约（跨端一致，供 iOS/Android 分别实现时对齐字段）

```
LocalDisplayCard {
  kind: "address" | "allergy" | "symptom"
  headline: string       // 大字中文正文（可能是多行，如多选过敏项）
  detail: string?        // 次要说明（address 场景下是英文原地址；allergy/symptom 场景通常为空）
  disclaimer: string?    // 仅 symptom 场景使用："这不是医疗诊断，请尽快就医。"
  showEmergencyAction: bool  // symptom 里选了高风险项时为 true，联动紧急电话按钮
}
```

三种卡片复用同一个渲染组件（iOS 侧建议把现有 `ShowToLocalSheet` 泛化为接受 `LocalDisplayCard`，而不是新建三个独立 View；Android 侧同理泛化 `TravelTalkCardDialog`）。

## 4. 验收标准

- 三种卡片类型共用同一套大字/朗读/复制交互，没有为每种类型重新实现一遍 UI。
- Allergy/Symptom 的中文文案是人工审校过的静态字符串，不经过任何 LLM 现场生成或改写。
- Symptom 里的高风险症状（如呼吸困难）必须联动紧急电话展示，不能只给一张卡片。
- Allergy 偏好本地持久化，跨 App 重启保留；Symptom 选择不持久化，每次临场选。
- iOS 先实现（按 2026-07-06 的平台优先级决定），Android 按同一套字段和文案追平，不允许 Android 自创文案或字段命名。
- 现有 Address 卡片的行为/触发点保持完全不变，这次改动只是新增两种卡片类型和把组件泛化，不是重做。

## 5. 明确不做的部分（避免范围蔓延）

- 不做 Hotel Check-in Credential（见 2.4，理由已说明）。
- 不做"用户自定义症状/过敏文本"——所有文案是固定枚举 + 预翻译静态文案，不接受用户自由输入后现场翻译成给本地人看的卡片（那是 Translate 功能本身该做的事，不属于这个规格）。
- 不在这一版做过敏/症状的多语言支持扩展（现有 Translate 已支持 7 语言，这里为了控制范围，先只保证卡片本身的中文产出是对的，用户端界面文案跟随 App 现有语言设置即可，不需要额外新增翻译工作）。

## 6. 与其他待办的关系

- 这个规格是"回应四模型评审第一轮的低成本高共识建议"的具体化，不是新提案。
- 依赖：无。可以独立于 CanvasPatch 架构评估、iOS 主线化的组织调整立即排期。
- 不依赖：Arrival Setup / Booking 数据模型（那是 Hotel Check-in Credential 需要的前置工作，本规格明确不做那张卡）。
