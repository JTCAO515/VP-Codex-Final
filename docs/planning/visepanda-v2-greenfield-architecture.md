# VisePanda V2 — 绿地重构：产品结构与技术架构总体方案

状态：**SUPERSEDED** — 已被 `visepanda-v2-final-architecture.md`（定稿基线）取代。本文档仅作第一轮蒸馏（Fable 5 独立方案）的推导过程存档，请勿作为开发依据。

日期：2026-07-07
性质：绿地设计（greenfield）。**本方案不参考、不继承现有仓库的任何代码、数据、文档与架构决策**，以全新空白项目为基准，唯一继承的是操作者的产品想法本身：为来华外国旅行者提供 AI 旅行副驾，并以「信任漏斗」实现商业化。
优化目标：商业化成功最大化 = 获客成本最低 × 变现路径最短 × 数据资产复利最强 × 工程迭代成本最低。

---

## 0. 五条设计公理（所有决策的推导起点）

1. **规划已经商品化，执行没有。** ChatGPT/Gemini 免费就能生成「北京 5 日游」。任何以「AI 行程生成」为卖点的产品都没有定价权。外国人在中国的真实痛点是落地执行：支付装不上、没网、语言不通、打不到车、订不了票、出了问题没人帮。**规划是获客钩子，执行是留存与付费，定制是利润。**
2. **AI 输出不可信，必须结构化收口。** AI 只能产出「类型化、可校验的补丁（Patch）」，由确定性代码验证后应用到状态；AI 永远不直接改任何用户数据。这是产品不变量，写进架构而不是写进规范文档。
3. **数据资产是唯一复利项。** 代码会过时、模型会换代，但「外国人在中国哪里会卡住、怎么解决」的结构化知识库 + 用户问题遥测，是竞品拿不到、时间越久越厚的资产。每个功能必须回答：它给知识库贡献什么？
4. **一份领域模型，一份真理源。** 多端各写一份数据模型 = 每个功能付三倍成本 + 永久的一致性审计负担。领域 schema 只定义一次，所有端消费同一份。
5. **商业化是架构内建，不是后期外挂。** 权益（entitlement）、外跳追踪、人工任务、询价对象从第一天就是一等公民数据模型——即使前三个月一分钱不收。

---

## 1. 产品论题（Product Thesis）

### 1.1 用户

| 分层 | 特征 | 商业角色 |
|---|---|---|
| 首次来华自由行游客 | 7–14 天，144 小时过境或旅游签，重度依赖英文信息 | 免费用户主体，affiliate 点击来源，Human Task 尝鲜者 |
| 重复/商务访客 | 一年多次往返，有既定行为模式 | 定制服务与高客单价的唯一可行对象 |
| 出发前的「研究者」 | 还没订机票，在 Google/YouTube/Reddit 搜攻略 | SEO 获客入口，转化为注册用户 |

### 1.2 痛点排序（决定功能优先级）

1. 支付（Alipay/WeChat Pay 外卡绑定、拒付、现金焦虑）
2. 网络（GFW、eSIM、地图/搜索不可用）
3. 语言（点菜、打车、问路、酒店沟通）
4. 交通执行（打车、高铁购票、地铁码）
5. 门票预约（故宫式实名预约制）
6. 突发求助（丢东西、生病、行程崩了）
7. 行程规划（重要但已商品化，作为入口而非卖点）

### 1.3 竞争定位

| 竞品 | 能做 | 结构性做不了 | V2 对位 |
|---|---|---|---|
| 通用 AI（ChatGPT 等） | 免费生成行程 | 中国执行层：实时可订性、支付、人工兜底 | 用免费规划迎战，在执行层建立不可替代性 |
| 大 OTA（Trip.com/Klook） | 交易闭环、库存 | 不愿做碎片化救援、支付辅导、人工任务（单位经济学不成立） | 做他们不做的，把交易外跳给他们赚佣金 |
| 入境定制商（WildChina 等） | $2000+ 定制履约 | 线上获客贵、无工具产品 | 做他们的获客漏斗，收 lead fee，不抢履约 |
| 海外 AI 旅行应用（Mindtrip 等） | 泛全球规划体验 | 中国特殊性（防火墙内数据、中文电话、支付） | 垂直深度打泛化广度 |

### 1.4 一句话定位

> **The execution copilot for foreigners in China.** 规划免费，执行可靠，出事有人管。

---

## 2. 产品结构（Feature Map）

按旅程阶段组织，而不是按功能类别组织。每个阶段有明确的商业动作。

```
阶段          平台重心    核心功能                              商业动作
─────────────────────────────────────────────────────────────────────
Dream/Plan   Web(SEO)   AI Copilot 对话 + 行程画布            获客；eSIM/机票外跳
(出发前2-8周)            城市/POI 知识页(programmatic SEO)
                        入境准备清单(签证/支付/网络/App)

Prepare      Web+App    准备清单执行(装App/绑卡/eSIM/下载离线包)  eSIM affiliate、保险外跳、
(出发前0-2周)            行程细化、预约提醒(故宫类实名票)          Trip Pass 转化点

Execute      App        Tools: 支付急救、翻译+出示卡、打车卡、    Human Task 付费、
(在华7-14天)             紧急求助、离线行程                      门票/体验外跳
                        行程执行视图(今天做什么/怎么去)
                        Human Help(人工任务一键求助)

Return       Web+App    行程回顾、下次来华、老用户识别            定制团询价(仅重复访客可见)、
(离境后)                 定制服务入口                            推荐分享(获客回环)
```

### 2.1 五个产品面（Surface）

1. **Copilot**（对话 + 画布）：唯一的 AI 交互面。对话产出结构化行程补丁，画布渲染行程状态。画布只读为主，所有修改经对话（保持单一修改路径）。
2. **Explore**：策展 POI 库的浏览面。每个 POI 带「实用元数据」：外卡可用性、英文菜单、预约要求、拥挤时段、雨天适性、地铁可达。这是知识库的直接产品化。
3. **Tools**：执行工具箱。支付急救指南、翻译+出示卡（Show to Local）、打车卡、紧急卡、汇率、eSIM 引导。全部离线可用。
4. **Human Help**：人工任务（打电话确认/翻译陪同/订位）+ 定制询价（Request Quote）。前者按次付费，后者 lead fee。
5. **Me**：账号、权益、AI 记忆（可见可删）、行程历史、离线包管理。

### 2.2 关键产品决策（与直觉相反的三条）

- **Web 先于 App。** 获客发生在出发前的 Google 搜索里，不在 App Store 里。SEO 是唯一免费且复利的获客渠道；App 是 Execute 阶段的工具，等 Web 验证 PMF 后再上。
- **离线优先是功能不是优化。** 用户落地时没网是常态（eSIM 未激活/GFW）。行程、工具卡、地图截图、应急卡必须本地可用。这直接决定数据架构（见 §4）。
- **Chat 里永不主动插商业内容。** 商业入口长在 Explore/行程块/Tools 场景后置位。Chat 只在用户显式问「怎么买/订」时给出经过外跳网关的链接。信任是唯一护城河的地基。

---

## 3. 技术架构

### 3.1 平台与语言策略（最重要的工程决策）

**TypeScript 单语言 monorepo：Next.js（Web）+ Expo/React Native（App）+ Node（服务端），共享同一份领域层。**

理由：
- 领域模型（TripState/Patch/POI/Task）只写一次（zod schema），Web/App/Server 三端直接 import，类型即契约。杀掉多端一致性审计这整类工作。
- Expo 的 OTA 更新（expo-updates）对「用户在旅途中、App 出 bug 必须当天修」的场景是刚需，原生双端做不到。
- LLM 编码代理（本项目的实际开发者是 AI agent）在 TypeScript 上的产出质量和生态工具链最成熟；单语言让同一个 agent 能跨端修问题。
- 放弃原生双端的代价（极致性能/平台特性）在本产品形态（内容+表单+对话）中几乎为零。

### 3.2 Monorepo 结构

```
visepanda/
├── packages/
│   ├── domain/           # 唯一真理源：zod schemas + 纯函数
│   │   ├── trip/         #   TripState, TripPatch, applyPatch(), diffTrips()
│   │   ├── poi/          #   POI, PoiFacts(实用元数据), TravelerFit 推导
│   │   ├── task/         #   HumanTask 状态机, Quote
│   │   ├── commerce/     #   Entitlement, Partner, OutboundClick
│   │   └── telemetry/    #   统一事件 schema
│   ├── api-client/       # 由服务端 router 类型生成的客户端(tRPC)
│   ├── ai/               # 提示词模板、模型路由、输出校验、评估集
│   └── ui/               # 跨端设计 token + 基础组件(web/native 双实现同一接口)
├── apps/
│   ├── web/              # Next.js 15: 营销页+SEO页+完整产品(PWA)
│   ├── mobile/           # Expo RN: Execute 阶段为中心的 App
│   ├── server/           # 领域 API(tRPC)+LLM编排+队列 worker
│   └── ops/              # 运营台: 知识库编辑、人工任务调度、商家白名单、漏斗看板
├── infra/                # IaC、migration、seed
└── evals/                # AI 回归评估集(golden set)与跑分脚本
```

### 3.3 基础设施选型（可直接落地的具体清单）

| 层 | 选型 | 理由 |
|---|---|---|
| 托管 | Vercel（web/server）+ EAS（mobile 构建） | 零运维，agent 可自助部署 |
| 数据库 | Postgres（Supabase）+ Drizzle ORM | Auth/Storage/RLS 一体；Drizzle 的 schema 可与 zod 互推 |
| 认证 | Supabase Auth（email + Google/Apple） | 外国用户无微信登录负担 |
| 队列/缓存 | Upstash Redis + QStash | serverless 友好，人工任务派单、LLM 重试队列 |
| LLM | 模型路由层（见 §5），Claude/GPT 主力 + DeepSeek/Qwen 降本 | 面向海外用户，主力模型必须是海外可访问的强模型 |
| 支付 | Stripe（全球卡）+ RevenueCat（IAP 抽象） | 见 §6.4 支付路由决策 |
| 分析 | PostHog（自建漏斗）+ Sentry | 事件 schema 自持，不锁死在第三方 |
| 地图 | Mapbox（海外）+ 高德静态数据（国内 POI 坐标） | GCJ-02 坐标转换在 domain 层统一处理 |

### 3.4 服务端架构

单体优先（modular monolith），按领域模块划分而不是微服务：

```
apps/server/
├── modules/
│   ├── copilot/      # LLM 编排: 意图路由→检索→生成→Patch校验→应用
│   ├── trip/         # 行程 CRUD = 仅接受 Patch, 事件溯源存储
│   ├── knowledge/    # POI/Facts 读写, 编辑工作流, 版本化
│   ├── task/         # HumanTask 状态机: requested→quoted→paid→fulfilling→done→reviewed
│   ├── commerce/     # entitlement 校验, outbound 网关, partner 配置, 对账
│   ├── identity/     # 用户/记忆(可见可删)/分层判定(首次vs重复访客)
│   └── telemetry/    # 事件摄入, 漏斗物化视图
└── workers/          # 异步: LLM 重试, 通知, 对账, SEO 页面再生成
```

**规则：模块间只能通过显式导出的服务接口调用，禁止跨模块碰表。** 这让未来任何模块可独立拆出，也让多个编码 agent 并行开发时冲突面最小。

---

## 4. 数据架构（事件溯源核心）

### 4.1 行程 = 事件日志

```
trips        (id, owner, head_version, snapshot_jsonb, updated_at)
trip_events  (trip_id, version, patch_jsonb, source, created_at)
              -- source: user_chat | user_manual | system | ai_copilot
```

行程的每次变更是一条 append-only 的 Patch 事件；`trips.snapshot` 是物化头。由此免费获得：

- **变更摘要**（digest）：diff 两个版本即可
- **撤销/回滚**：回放事件
- **多端同步**：客户端按 version 拉增量，离线队列写回
- **AI 行为审计**：source 字段天然记录哪些改动来自 AI
- **分析**：什么样的用户在什么阶段改什么，直接查事件表

### 4.2 知识库 = 产品资产，不是配置

```
pois          (id, city, category, names, geo, source_ids)
poi_facts     (poi_id, fact_type, value_jsonb, confidence, source, verified_at, version)
              -- fact_type: payment_acceptance | english_menu | booking_required
              --            | crowd_pattern | rainy_fit | metro_access | ...
knowledge_gaps(question_pattern, frequency, city, status)
              -- 从用户真实提问中挖出来的知识缺口，驱动编辑排期
```

要点：
- fact 与 POI 分离且带 `source/verified_at/confidence`——知识必须可追溯、可过期、可复核。到期未复核的 fact 自动降级不展示（宁缺毋滥，缺数据不硬凑）。
- **知识库同时喂三个消费者**：AI 检索（RAG）、Explore 展示、programmatic SEO 页面。一份编辑投入，三处产出——这是获客成本结构的核心。
- `knowledge_gaps` 由 Copilot 未答好的问题聚类生成，运营台按频次排编辑优先级。**用户用得越多，知识库越准，SEO 页越多，获客越便宜**——这就是增长飞轮的数据层实现。

### 4.3 统一遥测事件（第一天就定 schema）

```
events (id, user_id?, anon_id, surface, action, entity_type, entity_id,
        intent?, partner?, click_id?, props_jsonb, created_at)
```

所有商业分析（意图密度、漏斗转化、城市价值）都从这一张表物化，不散落各处。

### 4.4 商业对象

```
entitlements    (user_id, kind: free|trip_pass|task_credit, expires_at, source)
human_tasks     (id, user_id, city, kind, description, status, price, assignee,
                 transcript_jsonb, review)      -- 状态机见 §3.4
quotes          (id, user_id, trip_snapshot, requirements, status, agency_id, lead_fee)
partners        (key, hosts[], categories[], cities[], tracking_param, status)
outbound_clicks (click_id, user_id?, source, intent, entity_id, partner, created_at)
```

`human_tasks.transcript` 是被严重低估的资产：每单人工任务的处理记录，聚类后就是下一批知识库 fact 和下一个自动化工具的需求说明书。

---

## 5. AI 架构

### 5.1 Patch 管道（产品不变量的代码化）

```
用户消息
  → 意图路由(小模型): chat_only | trip_edit | trip_create | question | commerce_intent
  → 检索: 知识库 facts + 用户记忆 + 当前行程快照
  → 生成(强模型): 结构化输出 = { assistantMessage, patch?, citations[] }
  → 校验: zod parse + 业务规则(日期连续/城市存在/块完整) → 不合格自动重试(降级模板兜底)
  → 应用: trip_events append → 新快照 → 客户端增量同步
```

- **AI 永不直接写库**，只产出 Patch；应用层是确定性代码。
- 引用（citations）指向知识库 fact id：用户看到的每条「实用建议」可溯源，运营可纠错。
- commerce_intent 是显式意图类型：只有路由到它，回答才允许携带外跳链接（经 outbound 网关）。Chat 不插广告在管道层强制，不靠提示词自觉。

### 5.2 模型路由与成本

| 任务 | 模型档位 | 说明 |
|---|---|---|
| 意图分类、字段抽取 | 廉价档（DeepSeek/Qwen/Haiku 级） | 毫秒级、允许重试 |
| 行程生成、复杂问答 | 强档（Claude/GPT 主力） | 海外用户可达性优先 |
| 翻译/出示卡 | 廉价档 + 本地词库兜底 | 离线时用预置卡 |

- 每层有 fallback 链；单用户单日 token 预算按权益分层（free 有硬顶，pass 放宽）——成本控制是权益系统的一部分。
- 两轮生成策略：先骨架（快速首屏）后逐日补全（后台队列），对话体感与成本双优。

### 5.3 评估先行（evals/ 是一等目录）

- golden set：≥100 条真实问题（含中英混杂、模糊需求、越界请求），每次提示词/模型变更跑回归。
- 可执行性评分：Patch 的结构完整度自动打分，上线门槛。
- 越界红线集：政治敏感/签证法律承诺/医疗建议，必须回答「引导到官方渠道」，回归中锁死。

### 5.4 用户记忆

- 记忆 = 显式结构化偏好（饮食/节奏/预算/兴趣），不存原始对话。
- Me 页可见、可逐条删除，写入时告知。回答用到记忆时展示「基于你的偏好」提示——透明是信任漏斗的地基。

---

## 6. 商业化架构（内建）

### 6.1 收入线与产品对象的一一映射

| 收入线 | 数据对象 | 定价 | 角色 |
|---|---|---|---|
| Affiliate 外跳 | outbound_clicks + partners | 佣金（预期按五折归因折价） | 意图温度计，非利润 |
| Trip Pass | entitlements | $9.99/7天、$19.99/14天 | 覆盖 LLM 成本 + 解锁离线包/优先人工响应 |
| Human Task | human_tasks | $14.99 起/次（若走 IAP 则含抽成定价） | 信任建立器 + 数据采集器 |
| 定制询价 | quotes | lead fee $50–200 或成交 5–10% | 利润区，仅重复访客可见入口 |

### 6.2 信任漏斗的系统实现

```
匿名访客(SEO页) → 注册(存行程) → 商业点击(outbound) → 首次付费(Task/Pass)
    → 复访识别(identity 模块: 账号年龄+行程数+记忆跨度) → 定制询价入口可见
```

每层转化率是核心 KPI 物化视图，第一天就建。

### 6.3 供给侧

- 冷启动 = 创始人 concierge：前 50 单 Human Task 由操作者亲自处理，ops 台先只有「任务列表 + 手动标记状态」。50 单之后才有资格设计服务者招募与 SOP。
- 商家白名单四档（人工服务者/持证导游/OTA affiliate/定制旅行社），全部运营台内部录入，无公开注册。
- take rate 与平台内分账：等法务实体与跨境分账通道就绪才排期（lead fee 模式在此之前覆盖定制线收入）。

### 6.4 支付路由决策（绿地版直接定死）

- **Human Task / 定制 lead fee = 真实世界人工服务 → Stripe 外部支付**（Apple 对 physical services 不强制 IAP）。
- **Trip Pass（数字权益）→ IAP（RevenueCat 抽象）+ Web 端 Stripe 双轨**，Web 购买价格更优（合规地引导 Web 转化）。
- 给服务者/商家的分账：lead fee 阶段 = B2B 月结发票，无分账系统。

### 6.5 获客引擎（架构承载）

1. **Programmatic SEO**：`/[city]/[poi]`、`/guides/[question]` 页面从知识库自动生成 + 编辑润色，Next.js ISR。知识库每新增一批 fact，SEO 面自动扩大。
2. **行程分享页**：每个行程可生成公开只读页（去隐私），朋友打开即着陆页——用户即渠道。
3. **创作者双向联盟**：给 China travel YouTuber 专属深链（带归因）+ 我们给其内容导流，结构写进 partners 表（partner 不只是 OTA）。
4. **App Store**：只承接「已在华、急需工具」的搜索流量，不作为主获客。

---

## 7. 路线图（触发条件驱动，非日历驱动）

### Phase 0 — Web MVP（目标：6–8 周内可公开）

范围（刀刃上的最小集）：
- Copilot 对话 + 行程画布（Patch 管道完整，两轮生成）
- 知识库：北京 + 上海两城，每城 5 类 × 头部 POI，全部人工核实 fact
- 入境准备清单 + 支付/eSIM/网络三篇深度指南（同时是 SEO 页）
- Human Task 表单（人工处理，Stripe 支付链接）
- outbound 网关 + 统一遥测 + PostHog 漏斗
- programmatic SEO 首批 ~200 页

明确不做：App、商家后台、佣金对账、多语言 UI（先英文单语）。

### Phase 1 —— 触发：周活 ≥ 200 真实外国用户 或 Human Task ≥ 20 单

- Expo App 上线（Execute 场景：离线行程 + Tools + Human Help）
- 知识库扩到 6 城；knowledge_gaps 驱动编辑排期
- 拿真实点击数据谈 Klook/Trip.com 正式 affiliate
- ops 台成型（任务调度 + 知识编辑工作流）

### Phase 2 —— 触发：单城定制询价 ≥ 5 次/月 且 复访用户占比可测

- Quote 市场（白名单旅行社，lead fee）
- 服务者网络（从创始人 concierge 的 SOP 招募第一批）
- Trip Pass 正式定价实验
- take rate 仅当法务实体就绪

---

## 8. 工程流程（为多 Agent 开发设计）

本项目的实际开发主体是 LLM 编码代理，流程按此优化：

1. **Schema 先行**：任何功能先改 `packages/domain` 的 zod schema + 纯函数 + 单测，PR 单独合并；前后端实现是消费 schema 的后续 PR。契约漂移在编译期死掉。
2. **模块所有权**：架构师 agent 拥有 domain/server 模块边界；编码 agent 按 surface（web/mobile/ops）领活。跨模块改动必须先过架构师 PR。
3. **每 PR 硬门槛**：typecheck + 单测 + 契约测试（api-client 对 server router 的类型一致性）+ evals 回归（凡碰提示词/模型）。
4. **Trunk-based + feature flag**：不留长命分支；商业入口全部 flag 控制，触发条件满足才翻开。
5. **每周一次「知识库质量审查」**：抽样 fact 复核 + gaps 清理，这是运营节拍不是工程节拍，但入 ops 台工作流。

---

## 9. 反目标（Anti-goals，立约）

- 不做原生三端（Web/iOS/Android 各一份代码）——本方案存在的首要原因
- 不做 OTA 交易闭环、不做自营库存、不做平台内全额托管支付
- 不做开放商家注册（白名单直到月订单 100+）
- 不做代付（已否决的方向，绿地版继续否决）
- 不做机票比价、不碰签证代办资质业务
- Chat 不做任何主动商业推荐（管道层强制）
- 不在触发条件满足前建设：商家后台/佣金对账/dashboard UI/多语言
- 不追求「AI 功能酷炫」：凡不能回答「给知识库/漏斗贡献什么」的 AI 功能一律不排期

---

## 10. 风险与对冲

| 风险 | 对冲 |
|---|---|
| 入境政策变化影响客流 | 产品同时服务商务访客（政策弹性小）；知识库资产不随客流贬值 |
| 大模型能力被平台化（OS 级助手吃掉入口） | 壁垒放在知识库 + 人工网络 + 执行工具，不放在对话本身 |
| 归因损耗吃掉 affiliate | affiliate 只当温度计；收入结构以 Task/lead fee 为主 |
| 人工任务规模化亏损 | 前 50 单创始人自营算清单位经济学再招人；定价含 buffer |
| 单人运营带宽 | ops 台把「知识编辑/任务调度」做成流水线；Phase 1 前不承诺 SLA |
| 中国数据合规（PIPL） | 用户主体是外国人、服务器在境外；境内仅静态 POI 数据，不存境内个人数据 |

---

## 11. 一页总结

**产品**：外国人来华的执行副驾——规划免费获客，执行工具留存，人工兜底付费，定制询价盈利。
**架构**：TS 单语言 monorepo（Next.js + Expo + Node），domain 包单一真理源，行程事件溯源，AI 只出 Patch。
**数据**：知识库（可溯源 fact + 缺口挖掘）一份投入喂三处（AI/Explore/SEO），遥测单表物化全部漏斗。
**商业**：四条收入线各有一等数据对象；信任漏斗每层转化可测；支付 Stripe 为主 IAP 为辅；触发条件驱动，不按日历烧钱。
**工程**：schema 先行、模块边界、契约测试、evals 回归——为 LLM 代理开发而设计的流程。
