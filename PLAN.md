# VisePanda — 执行计划

## 阶段一：AI Butler Chat MVP 骨架

- [x] 任务 1.1：建立 Next.js + React + TypeScript 项目骨架，适配 Vercel 部署。
- [x] 任务 1.2：定义 Trip Canvas、Butler Alert、Chat Message、Canvas Patch 等核心类型。
- [x] 任务 1.3：实现 mock AI butler pipeline，让用户消息可以生成结构化 canvas patch。
- [x] 任务 1.4：实现 Live Trip Canvas，包括 trip summary、day summary cards、顶部任务/提醒卡。
- [x] 任务 1.5：实现 Chat / AI Butler 主工作台，桌面为左画布右聊天。
- [x] 任务 1.6：实现 Trips、Explore、Tools、Account 占位页。
- [x] 任务 1.7：加入 warm New Chinese 水墨背景和纸卡视觉系统。
- [x] 任务 1.8：加入单元测试、组件测试、API 测试、Playwright 桌面/移动烟测。
- [x] 任务 1.9：将 Chat 工作台调整为桌面横屏一屏固定布局，页面本身不纵向滚动。
- [x] 任务 1.10：Trip Canvas 主界面只显示每日一句摘要，完整每日行程通过右侧抽屉查看。
- [x] 任务 1.11：移除独立 Practical Reminder 区块，将管家提醒合并到顶部五张任务卡。
- [x] 任务 1.12：移除 Chat 默认演示对话，建议问题改为两列布局，并在每次 AI 回复后生成 2 个上下文建议问题。

## 阶段二：AI Provider 与 Supabase 接入

- [x] 任务 2.1：接入 DeepSeek V4 Flash 真实 AI provider，保留 mock fallback。
- [ ] 任务 2.2：设计 Supabase schema：users、trips、messages、canvas_versions。
- [ ] 任务 2.3：实现 guest draft 到 logged-in synced trip 的迁移路径。
- [ ] 任务 2.4：实现基础 auth，将 Account 占位页升级为真实登录/同步入口。

## 阶段三：Explore 与第三方 Provider

- [ ] 任务 3.1：将 Explore 从占位页升级为城市、景点、美食、住宿入口。
- [ ] 任务 3.2：设计 provider abstraction，避免 UI 直接依赖 Amap、Trip.com、Meituan。
- [ ] 任务 3.3：先接 verified/static provider，再接真实第三方 API。
- [ ] 任务 3.4：把 Explore 结果加入 Chat 工作台的 Add to Trip / Add to Canvas 流程。

## 阶段四：Tools 与落地旅行能力

- [ ] 任务 4.1：实现签证/入境、支付设置、翻译、汇率、地铁、eSIM/VPN、应急工具页面。
- [ ] 任务 4.2：让顶部任务/提醒卡可以深链到对应工具。
- [ ] 任务 4.3：补充移动端工具入口和离线可读内容。

## 阶段五：场景化视觉与体验增强

- [ ] 任务 5.1：实现 destination-aware background switching。
- [ ] 任务 5.2：规划北京时切换为长城/故宫风格水墨背景。
- [ ] 任务 5.3：规划上海时切换为外滩/江南园林风格水墨背景。
- [ ] 任务 5.4：根据 active trip canvas destination state 平滑切换背景，避免做成手动主题选择器。

## 关键约束

- 技术选型：Next.js App Router、React、TypeScript、Vercel、Supabase 预留。
- AI 约束：DeepSeek V4 Flash 只在服务端 API route 调用；真实 key 不进入浏览器、不写入仓库。
- Fallback 约束：缺少 `DEEPSEEK_API_KEY`、API 失败或模型输出不合法时必须回落到 mock provider。
- Chat 约束：真实 AI 接入后 `/chat` 不再显示演示对话；建议问题必须避免被遮挡，并随上下文刷新。
- 当前重点：Chat / AI Butler 工作台。
- 当前限制：Trips、Explore、Tools、Account 仍只做占位。
- 视觉约束：warm New Chinese、水墨背景、实底纸卡；不要半透明玻璃聊天框。
- 桌面布局约束：当前阶段优先电脑横屏端，Chat 工作台固定为一屏，局部内容用内部滚动和抽屉承载。
- 行程画布约束：主画布不要直接展开 Day 详情；每日卡片只显示一句当日总结，详情必须进入抽屉查看。
- 文档约束：每一次迭代都必须同步更新 `PLAN.md`、`PRD.md`、`DESIGN.md`、`AGENTS.md`、`HANDOFF.md`。
- 测试约束：最终完成前必须跑 `npm run test`、`npm run build`、`npm run test:e2e`。

## 里程碑

- M1：AI Butler Chat MVP 骨架完成（2026-06-29，已完成）。
- M2：真实 AI provider 完成（2026-06-29，已完成）；Supabase schema 待排期。
- M3：Explore provider abstraction 完成（待排期）。
- M4：Tools 第一批真实工具完成（待排期）。
- M5：目的地感知背景切换完成（待排期）。

## 风险

- 已知风险 1：真实 AI 输出需要严格结构化，否则 canvas patch 可能不稳定。
- 已知风险 2：DeepSeek key、限流、网络和模型输出质量会影响实时体验，因此必须保留 mock fallback。
- 已知风险 3：Supabase schema 一旦过早固定，后续 Trips/Canvas 迭代会受限。
- 已知风险 4：第三方 API 能力边界必须先验证，不能伪造未确认的 Trip.com、Meituan、Amap 能力。
- 已知风险 5：水墨背景如果过重，会影响文字可读性和移动端性能。
- 待验证假设 1：用户会更喜欢“右侧持续聊天 + 左侧实时画布”而不是传统单列聊天。
- 待验证假设 2：顶部任务/提醒卡可以有效表达签证、支付、预订、风险等管家价值。
