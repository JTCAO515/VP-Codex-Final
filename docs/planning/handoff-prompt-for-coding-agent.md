# VisePanda 实现交接提示词(直接复制给 coding agent)

> 用法:把下面「======」之间的整段内容,作为首条消息发给负责实现的
> coding agent。提示词自包含,但要求 agent 先读 repo 内引用的规格文档。

======================================================================

你是 VisePanda 项目的实现工程师。请严格按以下上下文、硬约束和任务规格,
依次完成三轮迭代(v0.2.7 → v0.2.8 → v0.2.9),每轮独立提交、可独立上线。

## 一、项目背景(60 秒)

- 产品:VisePanda——面向非跟团独立自由行(FIT)外国来华游客的一站式 AI
  旅游管家。左侧 Live Trip Canvas(行程实体),右侧 Chat(对话主线),
  AI 输出 `CanvasPatch` 经 `applyCanvasPatch` 更新画布。
- 技术栈:Next.js 15 App Router + React 19 + TypeScript,Vitest +
  Testing Library,部署 Vercel(域名 go2china.space),Supabase 可选
  (未配置时优雅降级 guest/mock)。
- AI 层:`lib/ai/orchestrator.ts` 多模型并行竞速(DeepSeek/Qwen/GLM/Kimi),
  18s 超时,失败回落 `lib/mock-ai/mockButler.ts`(目的地感知 skeleton)。
- 当前版本 v0.2.6;仓库根目录 `CLAUDE.md` 是项目记忆,先读它。版本号已多次因并行规划轮顺延,开工前务必先跑 `grep '"version"' package.json` 和 `git log -5 --oneline` 确认最新实际状态,不要直接信任本文档里写死的编号。

## 二、必读文档(按顺序,都在 repo 里)

1. `CLAUDE.md` —— 项目记忆与红线
2. `docs/planning/v0.2.4-interaction-deep-dive.md` —— **本次实现的交互
   验收标准**(变更摘要卡/patch 演出/组件规格/动效参数表/三轮吸收方案)
3. `docs/planning/v0.2.3-ui-optimization-roadmap.md` —— 差距审计与优先级
4. `docs/planning/ux-design-and-layout-spec.md` —— 布局蓝图
5. `AGENTS.md` —— 全部历史约束(尤其 provider 抽象、AI 管道规则)

## 三、硬约束(违反任何一条即返工)

1. 所有画布写入必须走 `handleSend → /api/chat → CanvasPatch →
   applyCanvasPatch`;任何按钮/快捷动作只发**预制意图字符串**,禁止直改
   `TripState`(唯一例外:undo 的本地快照回滚,见规格 1.4)。
2. 永不删除 mock/静态 fallback;新交互在无 key/无数据时必须优雅降级。
3. 所有 API key 仅服务端环境变量;禁止进浏览器 bundle 或写入仓库。
4. 视觉遵守 Warm New Chinese:纸色/墨色/朱砂/金/苔绿,禁玻璃拟态、禁阴影;
   金色禁作文字色;每屏最多一个朱砂主按钮。
5. 动效一律按规格文档第五部分参数表,全部尊重 `prefers-reduced-motion`。
6. 每轮迭代结束前:`npm run test` 与 `npm run build` 必须通过;版本号
   `package.json` +1(0.2.x 递增);**详细更新全部 7 个文档**(VERSIONING/
   CHANGELOG/HANDOFF/PLAN/PRD/DESIGN/AGENTS,重点 HANDOFF/PLAN/PRD);
   提交后推送 feature 分支 `claude/visepanda-phase-3-hym6z9` **并**
   `git push origin HEAD:main`。
7. 推送前先 `git fetch origin main` 检查是否有并行提交;若 main 领先,
   先 rebase/同步再推,严禁 force-push 覆盖他人提交。
8. 面向操作者的汇报用中文;代码/提交信息可英文。提交信息禁写模型名。

## 四、三轮任务规格(细节以 v0.2.4-interaction-deep-dive.md 为准)

### 第 1 轮 = v0.2.7 「Canvas 行动层 + 画布交互」
- `lib/trips/completeness.ts`:六维完成度纯函数(路线/酒店/餐食/交通/
  支付/签证,0–100)。
- `lib/canvas/diffTripState.ts`:day 级 + alert 级浅 diff 纯函数。
- TripSummary 内完成度进度条(6 段配色,patch 后 300ms 动画;窄屏折为
  环形+chips)。
- Day 卡快捷动作:减负/加美食/换上午/加休息,预制意图**必须带天数**;
  移动端按钮 ≥44px 常驻。
- 变更摘要卡(Change Digest):回复块尾部渲染 diff 条目;点条目画布滚动
  定位 + 金色脉冲 600ms;无变化不渲染。
- patch 演出:新卡 240ms 淡入(stagger 40ms)、revised 卡金色脉冲、自动
  滚到第一张变更卡。
- 撤销:摘要卡"撤销"quiet 按钮 → 预制 undo 意图;AI 失败时用本地上一份
  快照回滚。
- 出发准备区:画布底部复选清单(alert 加可选 `done?: boolean`,向后兼容),
  勾选回写完成度。
- 测试:completeness、diff、快捷意图含天数、undo 回滚、摘要卡渲染。

### 第 2 轮 = v0.2.8 「Chat 体验重塑 + 内联工具卡」
- `MessageBlock` 组件:headline(serif 18/600)→ body → ✓highlights(苔绿)
  → ⚠watchOut(琥珀 8% 底)→ nextStep 朱砂主按钮;逐条 60ms stagger
  伪流式,总 <400ms。
- composer:Enter 发送 / Shift+Enter 换行(桌面);自动长高至 5 行;发送后
  清空+焦点保留+滚到底;busy 时按钮 spinner、禁重复提交;占位文案 8s 轮换。
- 等待叙事:乐观气泡 → 100ms 打字指示 + 画布骨架 → 3s/8s 递进文案 →
  失败时明示"离线草稿引擎"。
- `ask_factual` 快通道:意图分类命中签证/支付/eSIM/汇率/地铁/应急时,
  `/api/chat` 直接返回 `lib/tools` 静态数据构造的内联工具卡(<150ms,
  跳过 LLM);卡带「加入提醒」「标记完成」,回写完成度/准备区。
- 实体 chip + 双向悬停联动(仅桌面):回复中画布 POI 名渲染为虚线 chip,
  点击/悬停画布对应卡高亮(共享 highlightDayNumber 状态)。
- 测试:快通道绕过 LLM 与 <150ms、分块渲染快照、Enter 行为、chip 联动。

### 第 3 轮 = v0.2.9 「设计系统收口 + Tools 交互组件」
- token 层:globals.css 顶部显式 token(色/间距 4-32/圆角 8/12/999/动效
  120/240ms)+ 动效工具类落地参数表;全站文本对 --paper ≥4.5:1。
- 组件库归一:Button(primary/ghost/quiet)/Pill/Card/Sheet/Toast/
  ProgressMeter/MessageBlock/ToolCard/EmptyState;R1/R2 产物收编;新界面
  禁手写同类样式。
- Tools 三件套 widget(`ToolCategory` 加可选 `interactive` 描述符,缺数据
  整体降级静态清单):汇率换算器(复用 `/api/exchange-rate`,含离线快照)、
  签证资格问答器(静态决策树:护照国→免签/过境/需签)、支付设置向导
  (卡组织→分步引导+费率警示)。
- 移动 Chat sheet:composer 常驻底导航上方,聚焦上滑 70dvh(把手可拖),
  发送后半收 30dvh 露出画布演出;`visualViewport` 适配键盘。
- 测试:换算断言、决策树多国用例、widget 降级、reduced-motion 快照。

## 五、每轮验收(操作者视角)

- v0.2.7:点"减负"→ 3s 内对应 Day 卡脉冲、摘要卡列出改动、完成度动画;
  点摘要条目画布定位;点撤销恢复上一版。
- v0.2.8:问 "Do I need a visa?" 秒出工具卡并可加入提醒;任何发送 100ms
  内有可见反馈;悬停摘要条目画布亮卡。
- v0.2.9:三个工具双端可完成真实任务;新组件 ≥3 页复用无样式漂移。

若规格与仓库现状冲突,以 AGENTS.md 既有约束优先,并在 HANDOFF.md 记录
冲突与决策。开始前先跑一遍 `npm run test` 确认基线绿色。

======================================================================
