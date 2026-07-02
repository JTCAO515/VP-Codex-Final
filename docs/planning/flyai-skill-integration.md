# VisePanda — FlyAI(飞猪)Skill 研究与集成规划(v0.2.6,纯规划 + 开发工具接入)

> 来源仓库:`https://github.com/alibaba-flyai/flyai-skill`(MIT 许可证)。
> 本文档基于对该仓库 README、`skills/flyai/SKILL.md`、`skills/flyai/references/*.md`
> 八份子命令文档,以及已发布 npm 包 `@fly-ai/flyai-cli@1.0.16` 的 `package.json`
> 和内嵌 README 的**逐字核实**,不做未经证实的能力假设。
> 本轮**不改产品运行时代码**;唯一新增的是一个开发工具向的 Claude Code
> 项目级 Skill(见第五部分),供后续 coding agent 会话在开发时调用。

---

## 第一部分:FlyAI 是什么(准确技术画像)

### 1.1 一句话

FlyAI 是阿里飞猪官方发布的 **Claude Code / OpenClaw 兼容 Agent Skill**,本质是
一个 Node.js CLI(`flyai`,npm 包 `@fly-ai/flyai-cli`),通过 **MCP
streamable_http 协议**连接飞猪官方托管的 MCP 服务,提供机票、火车票、酒店、
景点门票、演出赛事、签证、租车、邮轮等旅行品类的自然语言/结构化搜索,返回
单行 JSON(含预订跳转链接、图片、平台提示文案)。

### 1.2 安装与调用方式(逐字核实)

```bash
# 全局安装 CLI
npm i -g @fly-ai/flyai-cli

# 作为 Claude Code Skill 安装(官方文档写法)
cp -r /path/to/flyai-skill/skills/flyai ~/.claude/skills/flyai
# 或
npx skills add alibaba-flyai/flyai-skill
```

- `package.json` 描述原文:「飞猪酒店、机票查询命令行工具（streamable_http）」。
- `bin` 指向单文件打包产物 `dist/flyai-bundle.cjs`(esbuild 打包,可选混淆)。
- 依赖极简(仅 `commander`),说明核心搜索/鉴权逻辑不在开源包里,而是在
  远端 MCP 服务端——**CLI 本身只是一个瘦客户端**。

### 1.3 八个子命令(参数与输出已逐一核实)

| 子命令 | 必填参数 | 关键可选参数 | 输出关键字段 |
|---|---|---|---|
| `keyword-search` | `--query` | — | `itemList[].info.{jumpUrl,picUrl,price,star,tags,title}` |
| `ai-search` | `--query`(支持复杂自然语言,含预算/同行人/偏好) | — | `data`(结构随查询变化) |
| `search-flight` | `--origin` | `--destination --dep-date --journey-type --sort-type(1-8) --max-price` | `itemList[].{adultPrice,jumpUrl,journeys[].segments[].{depCityName,arrCityName,depDateTime,arrDateTime,duration,marketingTransportName,marketingTransportNo,seatClassName}}` |
| `search-train` | `--origin` | `--destination --dep-date --seat-class-name --max-price --sort-type` | 结构同 `search-flight`,`segments[].transportType="火车"` |
| `search-hotel` | `--dest-name` | `--poi-name --check-in-date --check-out-date --hotel-stars --hotel-bed-types --max-price --sort` | `itemList[].{name,address,mainPic,detailUrl,price,score,scoreDesc,star,latitude,longitude,interestsPoi}` |
| `search-poi` | `--city-name` | `--keyword --category(34 类可选,含历史古迹/自然风光/主题乐园等) --poi-level(1-5)` | `itemList[].{name,address,mainPic,jumpUrl,ticketInfo.{price,ticketName}}` |
| `search-marriott-hotel` | `--dest-name` | `--hotel-brands --max-price` | 同 `search-hotel` 结构,限万豪集团 |
| `search-marriott-package` | `--keyword` | `--sort-type` | 万豪套餐产品 |

`keyword-search` 的意图模式(patterns)额外覆盖:**签证/旅行证件**
(`visa`)、**电话卡/WiFi 租赁**(`SIM card / wifi rental`)、**租车/接送机**、
**邮轮**、**演出赛事门票**——这四类目前完全不在飞猪官方 README 首屏的
"8 大能力"总结里,但在 `SKILL.md` 的 `intents`/`patterns` 元数据里明确列出,
是本次深挖发现的重要补充。

### 1.4 认证模型与隐私(关键,决定集成路径)

摘自 `@fly-ai/flyai-cli` 包内 README(逐字):

> 默认凭证与签名字符串在**构建时**写入 `src/build-profile.ts`……运行时可用
> 环境变量 `FLYAI_SIGN_SECRET` 覆盖构建期写入的 `signSecret`……须与 MCP 侧
> 配置的密钥一致。

> 每个 MCP 请求会携带 `x-ff-ctx`:内容为 gzip 后的 JSON,若已配置
> `signSecret` 则再经 AES-256-GCM 加密后 Base64。典型用途包括**风控、滥用
> 检测与客户端环境一致性校验**。

即:
1. 已发布的 npm 包**内嵌了阿里官方自己的默认 trial 凭证**(免 key 即可试用),
   这是官方给"任意开发者用 CLI 试用"准备的共享额度,不是分配给 VisePanda
   的专属配额。
2. 每次请求都带**设备指纹头**(机器信息、CPU/内存档位、语言、UA、
   `deviceId`),明确写着用于反滥用/风控——这是一个专门为区分"真实开发者
   本地 CLI/Agent 调用"与"批量自动化流量"设计的机制。
3. 可选 `FLYAI_API_KEY` / `FLYAI_SIGN_SECRET` 用于"增强结果",但**没有任何
   公开文档描述面向第三方网站的服务端合作申请流程**(对比 Dianping 开放平台
   有明确的开发者注册+应用审核路径,飞猪这边目前只字未提)。

---

## 第二部分:能不能直接接进 VisePanda 生产后端?(架构现实判断)

### 2.1 为什么不能把 CLI 直接塞进 `/api/*` 路由

- Vercel Serverless Function 每次冷启动是一个新的隔离沙箱,**无法保证全局
  npm 包持久安装**;在请求路径里 `npx`/`spawn` 一个 Node CLI 子进程,会带来
  不可控的冷启动延迟(与我们刚在 v0.2.2 修的"回复慢"问题背道而驰)、
  进程管理复杂度、以及对未经审计子进程输出的信任问题。
- 这不是"缺一个 HTTP 包装"就能解决的——CLI 本身就是通过 MCP
  streamable_http 协议与远端通信的瘦客户端,**没有一个稳定公开、面向第三方
  网站后端的 REST/HTTP 端点文档**。

### 2.2 为什么不直接提取内嵌凭证自己发请求

- 内嵌的 `defaultAuthorization`/`signSecret` 是阿里为"CLI/Agent 场景的共享
  试用额度"发放的,不是分配给 VisePanda 的专属 key;绕过官方合作渠道直接
  复用会:(a) 违反其风控设计初衷,(b) 面临随时被限流/封禁的风险,
  (c) 与我们在 Meituan/Dianping 集成上**已经确立的原则完全相悖**——
  「必须走官方开发者注册与应用审核,不能爬取/逆向」(见
  `HANDOFF.md` 大众点评申请教程一节)。同一原则适用于飞猪。

### 2.3 结论

**FlyAI 目前是一个"开发者/Agent 工具",不是"可直接嵌入生产网站后端的公开
API"。** 正确定位:先作为**开发阶段的研究/内容生产工具**接入本仓库
(第五部分,零风险、立即可用);生产级服务端集成(第四部分路径 B)列为
**待飞猪官方合作确认后的候选项**,与 Dianping/Meituan 走同一条"先申请、
再接入"的既定流程,本轮不写任何调用其真实数据的产品代码。

---

## 第三部分:与 VisePanda 现有数据源的关系(不是替代,是互补)

| 数据源 | 擅长 | 缺口 |
|---|---|---|
| 高德(Amap,已接入) | 地理位置、评分、营业时间、地址 | 无法预订、无门票/房价 |
| 大众点评/美团(规划中,待审批) | 评论深度、人均消费 | 同上,且仍在申请排队中 |
| **飞猪(FlyAI)** | **真实可预订的门票价/房价 + 预订跳转链接** + **城市间机票/火车票搜索**(现状完全空白) | 生产级服务端集成仍需官方合作 |

飞猪最大的独特价值不在"再来一个 POI 数据源",而在两个 VisePanda 现在**完全
没有**的能力:
1. **城市间交通搜索**(`search-flight`/`search-train`)——目前 Tools 的
   "地铁"分类只有市内静态文字,「西安到成都怎么去」这类问题完全答不出真实
   车次/价格,Canvas 的 `transport` 字段也只是 AI 编的文字建议。
2. **真实预订链接**(`jumpUrl`/`detailUrl`)——Explore/Canvas 目前所有
   "Add to Trip"最终都停在文字行程,没有一条路径能让用户真正点一下就去
   预订酒店或买门票。

---

## 第四部分:逐项功能映射(用户要求的"在什么现有功能里加入")

### 路径 A(本轮已落地):开发工具,不触碰生产代码

见第五部分。

### 路径 B(规划,需官方合作后才写代码)

| VisePanda 现有功能 | 对应文件 | FlyAI 子命令 | 集成方式 |
|---|---|---|---|
| Tools「地铁」分类(目前纯静态文字) | `lib/tools/staticProvider.ts`、未来 `components/tools/widgets/*` | `search-train`、`search-flight` | 新增"城市间交通"能力,和 v0.2.8 规划的 Tools widget 化(阶段十五)合并实现;拿到 key 前先做静态"即将支持真实车次查询"占位 |
| Tools「签证入境」「eSIM/VPN」分类 | `lib/tools/staticProvider.ts` | `keyword-search`(意图含 visa / SIM card / wifi rental) | 作为签证规则/电话卡信息的候选数据源之一,与既有静态清单并列，不替代 |
| Explore 住宿列(目前只有 Amap 位置信息) | `lib/explore/amapProvider.ts`、`lib/explore/types.ts` 的 `ExploreStay` | `search-hotel` | 补齐 `price`/`score`/`detailUrl`/`mainPic`,精确对应既有 `ExploreRichMeta` 的 `pricePerPerson`/`rating`/`bookingUrl`/`photoUrl` 字段(无需改类型定义,字段已预留) |
| Explore 景点列(目前只有 Amap 评分/地址) | 同上,`ExploreAttraction` | `search-poi` | `ticketInfo.price`/`jumpUrl` 补齐真实门票价与预订链接 |
| Chat 工具调用(`lib/ai/toolContext.ts`,目前只接 Amap) | `lib/ai/toolContext.ts`、未来 `lib/ai/orchestrator.ts` 工具循环 | `search-hotel`/`search-poi`/`search-flight`/`search-train` | 作为 Amap 之外新增的工具函数,和已规划的 `search_dianping`(阶段十二 v0.1.52)并列注册 |
| Canvas Day 详情 `stay`/`transport` 字段 | `lib/types/trip.ts` 的 `TripBlock`(已有可选富字段) | `search-hotel` + `search-flight`/`search-train` | 把纯文字酒店/交通建议升级为真实可预订选项,复用已规划的 `bookingUrl` 字段(v0.1.53 蓝图里speculative 的"Ctrip/Trip.com Booking Plugin"现在有了一个真实候选实现) |
| `docs/planning/mock-inventory.md` | 见下 | — | 新增第 23 行「真实预订数据(机票/火车票/酒店/门票)」,状态 🔴,目标「飞猪官方合作确认后」 |

---

## 第五部分:本轮实际落地——项目级 Claude Code Skill(零风险,立即可用)

### 5.1 做了什么

把上游 `skills/flyai/`(`SKILL.md` + 8 份 `references/*.md`)**原文 vendor**
进本仓库 `.claude/skills/flyai/`(MIT 许可证允许直接引用/再分发,已保留
LICENSE 出处说明)。这样任何以后打开本仓库的 Claude Code 会话都能直接使用
`flyai` 技能,而不需要每次单独安装到用户主目录(在本项目的沙箱/容器化
运行环境里,`~/.claude/skills/` 不会持久化,只有提交进 repo 的
`.claude/skills/` 才会跟着仓库一起持续可用)。

### 5.2 用途边界(写入 AGENTS.md,防止误用)

- **仅供开发阶段使用**:coding agent 可以在本机/沙箱里运行
  `npm i -g @fly-ai/flyai-cli` 后调用 `flyai <command>`,用于:
  1. 查真实酒店/景点/门票/车次数据,**丰富 Explore/Tools 静态 fallback 内容**
     (比如给某城市 stays 补一条更贴近真实价格区间的示例文案);
  2. 撰写行程文案/验证物流合理性时做事实核查(例如确认"西安到成都高铁约
     几小时"这类经验性说法);
  3. 为路径 B 的真实 provider 设计数据模型时,提供准确的样例 JSON 结构。
- **禁止**:任何 `/api/*` 路由或 `lib/**/*.ts` 生产代码调用 `flyai` CLI 或
  提取其内嵌凭证发起请求。生产级集成必须等飞猪官方合作确认(参考路径 B)。

### 5.3 文件清单

```
.claude/skills/flyai/
├── SKILL.md                          # 原文 vendor,含 8 子命令用法+输出格式规则
├── LICENSE-NOTICE.md                 # 本次新增:来源、许可证、用途边界说明
└── references/
    ├── keyword-search.md
    ├── ai-search.md
    ├── search-flight.md
    ├── search-train.md
    ├── search-hotel.md
    ├── search-poi.md
    ├── search-marriott-hotel.md
    └── search-marriott-package.md
```

---

## 第六部分:与既有 mock/占位清单的关系

已同步更新 `docs/planning/mock-inventory.md`,新增:

| # | 领域 | 现状 | 真实替代 | 状态 | 目标 |
|---|---|---|---|---|---|
| 23 | 真实预订数据(机票/火车票/酒店/门票) | 完全不存在;Canvas 的 stay/transport 字段全部是 AI 生成文字 | FlyAI(飞猪)`search-hotel`/`search-poi`/`search-flight`/`search-train`,需官方生产合作 | 🔴 | 飞猪官方合作确认后 |

---

## 第七部分:后续需要用户做的事(非技术门槛,标注清楚)

1. 若希望推进路径 B(生产级真实预订数据),需要联系飞猪/阿里官方
   (`SKILL.md` 维护者邮箱 `flyai@alibaba-inc.com`,`homepage: https://open.fly.ai/`)
   询问面向第三方网站的服务端 API 合作方案与费率,而不是复用 CLI 内嵌的
   共享试用凭证。这与此前 Dianping 开放平台的申请建议同一性质,可以一并
   跟进。
2. 本轮**不需要**任何 Vercel 环境变量配置——路径 A 的 Skill 只在开发阶段的
   本地/沙箱环境使用 `npm i -g @fly-ai/flyai-cli`,不涉及生产部署。
