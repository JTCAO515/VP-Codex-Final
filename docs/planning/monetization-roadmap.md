# VisePanda 商业化路线规划（Monetization Roadmap）

状态：ACTIVE（V1 现有代码线语境）。商业模型与硬决策已整体吸收进 V2 定稿基线 `visepanda-v2-final-architecture.md`；若「新旧仓库取舍」决策落定为 V2 绿地重启，本文档随 V1 线一并归档。

日期：2026-07-07
性质：规划定稿，只规划不执行。执行前必须先定第 3 节的 4 个硬决策。
来源：操作者提出「跳转佣金 + 商家入驻」两条商业线 → Codex 初版方案（含 DeepSeek/Kimi/Qwen/GLM 四模型视角对抗评审）→ 架构师补全定稿。
关联文档：

- `final-product-positioning-moat-and-risk-assessment.md`（产品定位与护城河，含对「六个技术壁垒」自我欺骗的警告）
- `user-segmentation-trust-funnel-note.md`（信任漏斗的用户分层判定逻辑）
- `payment-integration-technical-plan.md`（StoreKit/订单/Human Task 支付技术方案）

关联 Issue：#167（Outbound Link Tracking）、#168（Partner Link Config）——本规划中唯二「现在可开工」的项。

---

## 1. 核心定位（一句话）

VisePanda 不做完整 OTA，不做大商城。定位是：

> **外国人来华旅行的 AI 决策入口 + 可信交易跳转层 + 本地人工服务分发层。**

商业模型是已定调的「信任漏斗」的收入版：

```
免费 AI 工具（Chat/Trips/Explore/Tools）→ 获客、建立使用习惯
        ↓
Human Task（按次付费人工帮助）→ 建立信任、识别高价值用户、采集真实卡点数据
        ↓
定制团 lead fee（旅行社/导游报价撮合）→ 真正的利润区
```

三条收入线的真实角色（预期要写实，不要自我欺骗）：

| 收入线 | 角色 | 毛利 | 备注 |
|---|---|---|---|
| Affiliate 外跳佣金 | 意图验证温度计 | 低 | 归因损耗大（见 3.2），财务上按点击价值建模，不按佣金建模 |
| Human Task | 信任建立器 + 数据采集器 | 中 | 不可规模化，但决定口碑；也是竞品（海外 AI planner）做不了的执行层 |
| 定制团 lead fee / take rate | 利润区 | 高 | 一单 $50-200 lead fee 或成交价 5-10%；只对重复/商务访客展示入口（见用户分层文档） |

---

## 2. 四模型对抗评审结论（Codex 轮，保留存档）

| 视角 | 主张 | 反对点 | 采纳 |
|---|---|---|---|
| DeepSeek 风格 | 先做交易闭环，尽快验证收入 | 闭环重、合规重、客服压力大 | 先做外跳 affiliate，不做自营支付闭环 |
| Kimi 风格 | 先做内容/信任，避免商业化伤害体验 | 纯内容变现慢 | 商业入口必须场景触发，不做广告墙 |
| Qwen 风格 | 商家入驻形成供给壁垒 | 冷启动难、审核成本高 | 先白名单小供给（人工服务/导游/旅行社） |
| GLM 风格 | 重视合规、资质、结算、风控 | 上线速度慢 | 商家服务先做 lead 分发，不承诺平台担保 |

---

## 3. 执行前必须定的 4 个硬决策（未定之前，除 #167/#168 外一律不开工）

### 3.1 Apple IAP 抽成 —— Human Task 定价模型的生死线

规则：实体/线下服务（旅行预订、人工电话服务）可走外部支付；纯数字服务必须走 IAP（15-30% 抽成）。Me 页已有 StoreKit placeholder（Trip Pass $9.99/$19.99、Human Task $9.99）。

- 若 Human Task 走 IAP：$9.99 到手 $7-8.5，再分给服务者，平台几乎不剩 → 定价需上调至 $14.99 起。
- 若定义为「人工线下/电话实体服务」走外部支付：合规上站得住，但支付体验差、有 App 审核风险。

**待决：二选一，直接决定单位经济学。**

### 3.2 外跳归因损耗 —— affiliate 收入按五折预估

Klook/Trip.com/Viator 的 affiliate 多为 last-click cookie 归因。iOS App 外跳 Safari 后：用户当场不买、次日自己打开 partner App 下单 → 佣金归零；ITP 清 cookie → 归零。行业经验真实归因率仅 40-60%。

**含义：** Phase 1 的 affiliate 是意图验证工具，不是收入来源。谈合作时优先争取 API / server-side tracking 级别（Klook 有 data feeds/API），纯 affiliate link 是保底。自有 clickId（#167）是将来升级合作等级的基础。

### 3.3 跨境分账能力 —— take rate 的启动前置条件

外国用户付美元（IAP 解决收款端），但给国内导游/服务者分账需要国内实体 + 合规支付通道（对私打款、代扣税）。lead fee 模式规避此问题（商家付平台，B2B 发票）；take rate 需要真实分账能力。

**待决：公司实体结构没定之前，take rate 不排期。真正的门槛是法务不是单量。**

### 3.4 商业化启动触发条件 —— 按信号不按日历

产品仍在开发期（iOS 主线冲刺，Android 暂停）。商业化各阶段按触发条件解锁：

| 触发条件 | 解锁动作 |
|---|---|
| （现在，开发期） | #167 Outbound Tracking + #168 Partner Config（埋点越早数据越多） |
| iOS 上架 + 周活 ≥ 200 外国真实用户 | Explore/Trip block 静态 CTA（纯外跳，先不谈佣金） |
| 商业点击率数据 ≥ 2 周 | 拿数据谈 Klook/Trip.com 正式 affiliate（有数据谈判位不同） |
| Human Task 手动版收到 ≥ 20 个真实请求 | 建商家白名单后台 |
| 单城市定制询价 ≥ 5 次/月 | 接旅行社 lead fee |
| 月撮合订单 ≥ 50-100 单 且 3.3 已解决 | 才考虑 take rate 与自动结算 |

---

## 4. 商业线 1：跳转与佣金

### 4.1 三层递进

1. **外跳深链 + 埋点**（#167/#168）：所有外部跳转走 `/outbound` 网关，记录 userId/anonymousId、source（explore/trips/chat/tools）、intent（hotel/ticket/experience/transport/sim/insurance）、poiId、tripBlockId、partner、clickId、timestamp。
2. **正式 affiliate 合作**：优先顺序 Klook（体验/门票/交通/eSIM，最贴合外国人来华轻决策）→ Trip.com（酒店/火车/机票）→ Viator/GetYourGuide（英文体验库存）。支付/通信/保险类后置。
3. **佣金对账**：接 partner report 或手工 CSV，clickId/orderId 匹配。有真实佣金流水才建系统。

### 4.2 Outbound 数据的真正价值是喂产品，不是对账

「哪国游客在哪个城市的哪类 POI 上点了什么」反向喂 curated knowledge base（#53）和 TravelerFit 规则权重。大 OTA 有交易数据但没有「旅行中问题」数据——这是数据壁垒的形成方式。

### 4.3 入口铺放原则

不做商城 Tab。商业入口长在用户正在做的事情里：

- Trip block（景点/交通/酒店类型匹配的 CTA）
- Explore POI 卡片与详情页
- Tools 场景后置推荐（Translate 后 → Show to Local/Human help；Network 后 → eSIM；Entry Checklist 后 → 签证/保险外跳）

**Chat 入口单独立规矩（产品不变量级别）：** Chat 是信任核心，AI 回答夹带商业链接是毁信任最快的方式。规则：**Chat 只在用户显式问「怎么买/怎么订/在哪预约」时给跳转，绝不主动插入。** Explore/Trip block 是静态货架，容忍度高，商业入口放那里。

---

## 5. 商业线 2：商家入驻

### 5.1 入驻类型四档（全部白名单制，不开放注册）

| 档 | 类型 | 模式 | 风险 |
|---|---|---|---|
| 1 | 人工任务服务者（打电话确认/解释支付/陪同翻译） | 按次 Human Task | 最低，辅助服务非旅游产品 |
| 2 | 持证导游/本地向导（city walk/讲解/接送） | lead 分发，不碰全额托管；必须收资质（导游证/语言/城市/价格/退款规则） | 中，涉导游资质监管 |
| 3 | OTA/票务/体验供应商 | 标准 affiliate/API（走商业线 1，不进商家后台） | 低 |
| 4 | 旅行社/定制组团（5-14 天多城定制） | Request quote → 旅行社报价 → lead fee 或成交抽佣 | 售前重但客单高，是漏斗底部利润区 |

### 5.2 供给冷启动 = 创始人 concierge

白名单第一批服务者：**前 50 单 Human Task 操作者自己做**（打电话、翻译、订餐厅）。这不是权宜之计——是唯一能搞清「外国人真实卡点 + 愿付价格 + 服务 SOP」的方式。做完 50 单才知道招什么样的服务者、怎么定价、怎么写 SLA。

### 5.3 商家后台 MVP 边界

只做：商家资料/服务城市/类型/语言/价格区间/可接单时间/资质上传/联系方式/接单状态/审核状态。
不做（订单量达标前）：自助发布复杂套餐、即时库存、平台内全额支付、多商家竞价、自动结算。

### 5.4 定制团的战略位置

对标 WildChina / China Highlights（客单 $2000+、纯人工售前）：VisePanda 的免费 AI 行程本质是他们的获客漏斗前端。Quote Request 把 AI 行程转成结构化需求发给旅行社，lead fee 他们付得起也愿意付。**定制服务入口只对重复/商务访客展示**（判定逻辑见 `user-segmentation-trust-funnel-note.md`，一次性游客在数学上不成立）。

---

## 6. 获客侧：友链互推的方向修正

早期最缺的是用户不是佣金。互推对象与结构：

- China travel 类 YouTube/TikTok 创作者：他们缺工具推荐，我们缺流量
- 签证代办/留学机构：用户时序在我们上游
- 酒店英文前台：放二维码，解决老外求助场景

结构是**双向联盟：我们给他们导流赚佣金，他们给我们导用户**。此项优先级高于接 Viator 等第二梯队 affiliate。

---

## 7. 竞品与护城河

| 竞品类 | 能做 | 做不了 | 我们的对位 |
|---|---|---|---|
| 海外 AI travel planner（Mindtrip/Layla 等） | 行程推荐 | 中国落地执行（支付/防火墙/中文电话） | 护城河在执行层不在推荐层 → Human Task 是战略功能不只是收入线 |
| 大 OTA（Trip.com/Klook） | 交易、库存、可复制 AI 推荐 | 不愿做细碎救援/支付辅导/人工任务 | 壁垒是「旅行中问题解决网络」，不是推荐列表 |
| 中国入境定制商（WildChina/China Highlights） | 高客单定制履约 | 线上获客成本高、无 AI 工具 | 我们做他们的获客漏斗，收 lead fee，不抢履约 |

---

## 8. 不做清单（anti-goals，比做什么更重要）

- 不做机票比价（红海、佣金薄、做不过 Skyscanner）
- 不做自营库存、不做平台内全额支付闭环
- 不碰签证代办资质业务（只做 checklist + 外跳）
- 不做开放商家入驻（白名单直到月订单 100+）
- 代付模式维持否决（此前已定）
- Chat 内不做任何主动商业推荐（产品不变量）
- 不提前建设：CTA/商家后台/佣金对账/dashboard 全部等触发条件（3.4）

---

## 9. Issue 排期（分三批）

**第一批：现在可开（开发期，不依赖用户量）**
1. #167 Outbound Link Tracking（/outbound 网关 + outbound_clicks 表 + 白名单校验）
2. #168 Partner Link Config（配置文件，4 家 partner pending 占位，两个纯函数）

**第二批：iOS 上架 + 有真实用户后**
3. Explore/Trip Block Booking CTA（合并为一个 issue，一个 PR 的量）
4. Human Task Request MVP（表单 + 人工处理，操作者自己接单）
5. Attribution 查询（先 SQL 顶着，不做 dashboard UI）

**第三批：数据验证后**
6. Merchant White-list Onboarding（内部表格起步）
7. Quote Request for Custom Trip
8. 佣金对账系统（有真实佣金流水才做）

---

## 10. KPI（第一阶段不看 GMV，看商业意图密度）

- 每 100 活跃用户的商业点击次数
- 入口点击率排序：Trips / Explore / Chat / Tools
- 品类强度：酒店 / 门票 / 体验 / eSIM / 人工任务
- 点击外跳后的回流率（用户是否回来继续用 app）
- Human Task 提交率与完成率
- 分城市商业价值（上海/北京/成都/深圳/广州/杭州/苏州/西安/重庆）
- **信任漏斗转化率（模型验证指标）：free user → 首次商业点击 → 首次付费（Human Task）→ 定制询价，每层转化**

---

## 11. 风险清单

1. **大公司覆盖**：OTA 可复制 AI 行程推荐 → 壁垒必须落在「旅行中问题解决网络」（执行层）
2. **商家质量**：开放入驻毁信任 → 白名单 + 慢扩
3. **合规**：导游/旅行社/支付/保险不做模糊承诺；文案明确「平台推荐/跳转/线索分发」，不替代旅行社责任
4. **体验**：商业入口过多即广告墙 → 原则：用户没有明确意图不展示交易，用户正在解决问题才展示下一步
5. **归因损耗**（3.2）与 **Apple 抽成**（3.1）：财务模型的两个已知折价项，预估时不得忽略

---

## 12. 一句话路线

先把 VisePanda 做成「外国人来华旅行的可信决策入口」，用外跳埋点验证商业意图，用 Human Task 验证愿付费需求并建立信任，再用白名单商家供给扩展成服务网络，最终靠定制团 lead fee 盈利。不先做大商城，先做每个旅行问题背后的下一步。
