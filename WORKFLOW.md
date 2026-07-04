# WORKFLOW.md — 操作者工作流手册

你（操作者）是三个 AI 之间的信使。整个协作通过 GitHub 异步完成，你每轮只需复制粘贴固定话术，不需要做任何技术判断。

## 三个角色找谁干什么

| 角色 | 职责 | 你什么时候找它 |
|---|---|---|
| **Claude Code**（架构师/Reviewer/集成管理员） | 拆需求建 Issue、审核 PR、唯一合并权、维护全部共享文档、仲裁冲突、dev→main 发布 | 有新需求、要审核、问进度、出任何问题 |
| **Antigravity**（Android 工程师） | `android/` 全部开发：实现 Issue、gradle 单测+构建、Android 34 模拟器验收、断网验收、填 PR 模板 | 派 Android Issue、转驳回意见 |
| **Codex**（iOS 工程师） | `ios/` 全部开发：实现 Issue、Xcode 构建、模拟器验收、断网验收、附截图证据（iOS 无 CI，截图是硬要求）、填 PR 模板 | 派 iOS Issue、转驳回意见 |

## 你的循环（双泳道，Android 和 iOS 并行转，互不阻塞）

```
【派活】把 Issue 链接给对应工程师
   ↓
工程师开发，提 PR 到 dev（这期间你不用做任何事）
   ↓
工程师说"PR 已提交" → 你对 Claude Code 说【审核】
   ↓
Claude Code 二选一：
   ├── 合并 → 更新文档、关 Issue → 直接给你下一句【派活】话术
   └── 驳回 → 在 PR 上写明违反哪条防线 → 给你【转驳回】话术
   ↓
回到顶部继续
```

## 三句固定话术

**【派活】**（对 Antigravity 或 Codex）
```
做 Issue #N：https://github.com/JTCAO515/VP-Codex-Final/issues/N
规则：用独立 clone（非 iCloud 路径），开 agent/{android|ios}-issueN 分支，
完成后提 PR 到 dev，逐项填写 PR 模板。Issue 评论里有版本号和补充要求。
```

**【审核】**（对 Claude Code）
```
审核
```
（Claude Code 会自动扫描所有 open PR，逐个执行：CI 状态检查 → Scope 越界核对 → 五条硬规则审核 → 合并或驳回 → 告诉你下一步对谁说什么。）

**【转驳回】**（对 Antigravity 或 Codex）
```
你的 PR #X 被驳回，看 PR 里架构师的评论，修改后推到同一分支（不要开新分支）。
```

## 新需求怎么进入

直接告诉 Claude Code 你想要什么（一句话就行），它负责拆成带 Scope/Do-not-touch/Acceptance/版本号的 Issue，然后把【派活】话术准备好给你。

## 出问题找谁

一律先找 Claude Code：合并冲突、两个工程师意见不一致、构建怪错、不知道下一步干什么——它是仲裁者和调度台。

## 当前泳道状态（由 Claude Code 在每轮审核后更新）

- **Android 泳道**（Antigravity）：#3 Translator（v0.3.15）—— PR #10 被驳回（模板空白+CI 未绿），等修复重推；重复 PR #6 已关闭。#3 合并后才能开 #4
- **iOS 泳道**（Codex）：#5 已合并（PR #7）✅
- **Butler 泳道**（Codex）：#8 Phase A 骨架已合并（PR #9）✅ —— 下一步待架构师拆 Phase B Issue（记忆体系）

## 完整规则出处

- 协作权威规则：`AGENTS.md` 末尾"多 Agent GitHub 协作规则"章节
- 架构约束：`ARCHITECTURE.md`（CanvasPatch 管道、密钥边界、本地写入白名单）
- 接口契约：`API_SPEC.md`
- 移动端规范：`MOBILE_STANDARD.md`
- PR 自查模板：`.github/PULL_REQUEST_TEMPLATE.md`
