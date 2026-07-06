# 品牌命名规范:Copilot(用户可见) vs Butler(内部命名)

日期:2026-07-07
性质:规范文档,后续 PR 命名审查依据(对应 Issue #117)

## 背景

2026-07-06/07,操作者直接指令(先派给 Codex 做 iOS,后确认扩大到 Web/Android)把 VisePanda 的 AI 助手用户可见名从 "Butler" 统一改成 "Copilot"。iOS(PR #115)、Web(dev commit `1463275`)已完成。Android 待"暂停派工"政策解除后补(Issue #118)。

这条边界从一开始就是刻意划定的:**只改用户看得到的文案,不改内部协议/代码命名**——因为内部命名(类型名、字段名、文件名、微服务名)贯穿整个代码库和跨端契约,强行重命名的风险和收益完全不成比例,纯粹是一次品牌层面的文案调整,不是产品重新定义。

## 用户可见:统一使用 Copilot

- `VisePanda`
- `China Travel AI Copilot`(完整品牌语,用于标题/首屏/App 副标题)
- `Copilot`(简称,用于按钮、提示语、正文引用)

覆盖范围:页面标题、导航栏/Header 文案、按钮/CTA 标签(`Ask Copilot`、`Ask Copilot to schedule`)、状态/错误提示(`Live Copilot unavailable`)、aria-label、权限用途说明(iOS `NSSpeechRecognitionUsageDescription` 一类)、i18n 文案 key 的英文值。

## 内部工程命名:允许继续使用,不强制重命名

以下命名在改名前就存在,改名时**刻意保留**,今后新增代码时也可以继续沿用同一风格,不需要为了"看起来统一"而去追加重命名 PR:

- 微服务名:`butler-service`(Java 后端,`AGENTS.md`/`API_SPEC.md` 里的 Butler 2.0 灰度开关说明同样保留)
- Web 类型/字段:`ButlerAlert`、`butlerAlerts`
- Web 函数/文件:`requestOrchestratedButlerPatch`、`tryButlerService`、`createMockButlerPatch`、`lib/mock-ai/mockButler.ts`
- Web 组件:`ButlerWorkspace`、`ButlerReminders`(文件名、组件名、CSS class 如 `butler-workspace`/`butler-reminders`)
- iOS 类型:`ButlerChatRequest`、`ButlerChatResponse`、`ButlerExploreRef`
- 后端路由:`/butler/memory/profile`

**为什么保留**:这些是三端共享的契约名或历史代码结构的一部分。重命名会牵扯 Web/iOS/Android 三端同步改动、可能影响正在进行中的功能分支(比如这次改名就和同期在做的 Trips 分阶段生成 iOS PR 撞了一次合并冲突,靠人工核实才没出岔子),而重命名本身对用户不可见、对产品没有任何功能收益——纯粹是无谓的重构风险。除非未来有独立的、经过明确讨论的理由(比如要把 `butler-service` 整个下线重写),否则不应该顺手在别的 PR 里"顺便"把这些名字也改掉。

## 禁止新增的命名

不要在任何用户可见文案里新造 Butler 的变体,包括但不限于:

- `AI Butler`
- `Panda Butler`
- `Butler Assistant`

这些都已经被 Copilot 取代,新增功能时不应该再引入。

## 如何使用这份文档

后续 PR 如果涉及产品命名相关的文案改动,可以直接引用本文档判断:
- 改的是用户可见文案 → 用 Copilot 系列命名。
- 改的是类型名/字段名/文件名/服务名等内部标识符 → 保持现状,不必因为品牌改名而顺带重命名。

本文档不要求、也不建议对现有代码做任何改动。
