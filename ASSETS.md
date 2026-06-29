# VisePanda Asset Inventory

> 合并来源：`VP-Claude-Web` · `VP-Hermes-Web` · `VP-Codex-Web`
> 合并目标：`VP-Hermes-Web`（本仓库）
> 说明：仅资产（图片、数据、文档、知识库、样式、静态资源），不含代码（Python/JS）

---

## 一、图片资源（Images）

### 1.1 品牌标识（SVG）

| 文件 | 路径 | 来源 | 说明 |
|------|------|------|------|
| `favicon.svg` | `web/favicon.svg` | VP-Claude-Web | 网站 favicon 图标 |
| `icon.svg` | `static/icon.svg` | VP-Hermes-Web | 静态资源目录下的品牌图标 |
| `bg-mountains.svg` | `web/assets/bg-mountains.svg` | VP-Claude-Web | 背景装饰图 — 山峦图案 SVG，用于页面背景 |

### 1.2 PWA 应用图标（PNG）

| 文件 | 路径 | 来源 | 说明 |
|------|------|------|------|
| `icon-192.png` | `web/icon-192.png` | VP-Hermes-Web | PWA 图标 192×192 |
| `icon-512.png` | `web/icon-512.png` | VP-Hermes-Web | PWA 图标 512×512 |
| `icon-maskable-512.png` | `web/icon-maskable-512.png` | VP-Hermes-Web | PWA 可适配图标 512×512 |

### 1.3 城市目的地图片（JPG）— static/img/city-*.jpg

共 **38 张**，覆盖中国主要旅游城市：

| 城市 | 文件 |
|------|------|
| 北京 | `city-beijing.jpg` |
| 长沙 | `city-changsha.jpg` |
| 成都 | `city-chengdu.jpg` |
| 重庆 | `city-chongqing.jpg` |
| 大理 | `city-dali.jpg` |
| 敦煌 | `city-dunhuang.jpg` |
| 福州 | `city-fuzhou.jpg` |
| 广州 | `city-guangzhou.jpg` |
| 桂林 | `city-guilin.jpg` |
| 贵阳 | `city-guiyang.jpg` |
| 海南 | `city-hainan.jpg` |
| 杭州 | `city-hangzhou.jpg` |
| 哈尔滨 | `city-harbin.jpg` |
| 呼和浩特 | `city-hohhot.jpg` |
| 香港 | `city-hongkong.jpg` |
| 黄山 | `city-huangshan.jpg` |
| 九寨沟 | `city-jiuzhaigou.jpg` |
| 昆明 | `city-kunming.jpg` |
| 兰州 | `city-lanzhou.jpg` |
| 拉萨 | `city-lasa.jpg` |
| 丽江 | `city-lijiang.jpg` |
| 洛阳 | `city-luoyang.jpg` |
| 澳门 | `city-macau.jpg` |
| 南昌 | `city-nanchang.jpg` |
| 南京 | `city-nanjing.jpg` |
| 青岛 | `city-qingdao.jpg` |
| 三亚 | `city-sanya.jpg` |
| 上海 | `city-shanghai.jpg` |
| 深圳 | `city-shenzhen.jpg` |
| 苏州 | `city-suzhou.jpg` |
| 台北 | `city-taipei.jpg` |
| 西藏 | `city-tibet.jpg` |
| 武汉 | `city-wuhan.jpg` |
| 厦门 | `city-xiamen.jpg` |
| 西安 | `city-xian.jpg` |
| 西宁 | `city-xining.jpg` |
| 云南 | `city-yunnan.jpg` |
| 张家界 | `city-zhangjiajie.jpg` |

### 1.4 美食图片（JPG）— static/img/food-*.jpg

| 文件 | 说明 |
|------|------|
| `food-beijing.jpg` | 北京烤鸭等北京美食 |
| `food-chengdu.jpg` | 川菜/成都美食 |
| `food-guangzhou.jpg` | 粤菜/广州美食 |
| `food-shanghai.jpg` | 上海美食 |

### 1.5 场景/灵感图片（JPG）

| 文件 | 说明 |
|------|------|
| `great-wall.jpg` | 长城景观（标志性地标） |
| `inspiration-first-time.jpg` | "初次访华" 灵感卡片图 |
| `inspiration-foodie.jpg` | "美食探索" 灵感卡片图 |
| `inspiration-hidden-gems.jpg` | "小众秘境" 灵感卡片图 |

### 1.6 品牌 Logo 图片（JPG）

| 文件 | 说明 |
|------|------|
| `logo-icon.jpg` | VisePanda Logo 图标版 |
| `logo-panda.jpg` | VisePanda 熊猫 Logo 完整版 |
| `og-image.jpg` | Open Graph 社交分享图 |
| `panda-base.jpg` | 熊猫形象基础图 |

---

## 二、知识库数据（Data / JSON）

### 2.1 城市数据

| 文件 | 来源 | 说明 |
|------|------|------|
| `data/cities.json` | VP-Hermes-Web | 中国城市信息数据库（城市名、描述、标签、亮点等） |
| `data/city_images.json` | VP-Hermes-Web | 城市与图片的映射关系配置 |

### 2.2 旅行信息

| 文件 | 来源 | 说明 |
|------|------|------|
| `data/food.json` | VP-Hermes-Web | 中国美食数据库（每道菜的描述、地区、推荐） |
| `data/hotels.json` | VP-Hermes-Web | 酒店推荐数据（根目录版本） |
| `data/hotels/hotels.json` | VP-Codex-Web | 酒店详细数据（子目录增强版） |
| `data/tips.json` | VP-Hermes-Web | 旅行小贴士数据库 |
| `data/tools.json` | VP-Hermes-Web | 实用工具/App 推荐数据 |

### 2.3 签证与 FAQs

| 文件 | 来源 | 说明 |
|------|------|------|
| `data/visa_policies.json` | VP-Hermes-Web | 中国签证政策数据库（免签/落地签/口岸签证等） |
| `data/faq.json` | VP-Hermes-Web | 常见问题问答库 |

### 2.4 优惠/套餐

| 文件 | 来源 | 说明 |
|------|------|------|
| `data/deals/deals.json` | VP-Codex-Web | 旅行优惠/套餐数据库 |

### 2.5 翻译/多语言知识库

| 文件 | 来源 | 说明 |
|------|------|------|
| `data/translations/attractions.json` | VP-Claude-Web | 景点相关中文翻译/短语 |
| `data/translations/culture.json` | VP-Claude-Web | 文化相关中文翻译/短语 |
| `data/translations/dining.json` | VP-Claude-Web | 餐饮相关中文翻译/短语 |
| `data/translations/phrases.json` | VP-Claude-Web | 日常实用中文常用句 |

### 2.6 PWA 清单

| 文件 | 来源 | 说明 |
|------|------|------|
| `web/manifest.json` | VP-Hermes-Web | Web App Manifest（浏览器安装配置） |
| `static/manifest.json` | VP-Hermes-Web | Manifest 副本（静态目录） |

---

## 三、文档体系（Documentation）

### 3.1 根目录核心文档

| 文件 | 来源 | 说明 |
|------|------|------|
| `README.md` | VP-Hermes-Web | 项目总览 README |
| `AGENTS.md` | VP-Hermes-Web | AI Agent 操作指南（给 Claude/Codex 的指令） |
| `CONTEXT.md` | VP-Codex-Web（更新版） | 项目上下文和背景信息 |
| `DESIGN.md` | VP-Codex-Web（更新版） | 产品设计文档 |
| `HANDOFF.md` | VP-Codex-Web（更新版） | 项目交接文档 |
| `PLAN.md` | VP-Codex-Web（更新版） | 执行计划 |
| `CHANGELOG.md` | VP-Hermes-Web | 变更日志 |
| `OPTIMIZATION_REPORT.md` | VP-Codex-Web（更新版） | 性能优化报告 |
| `PRD_PRODUCT_ANALYSIS.md` | VP-Codex-Web（更新版） | 产品需求与竞品分析 |
| `PROMPT_v6.2.0_Iteration.md` | VP-Hermes-Web（独有） | Prompt 迭代历史记录 |
| `PROMPT_VisePanda_Positioning_Update.md` | VP-Hermes-Web（独有） | 品牌定位更新 Prompt |

### 3.2 ADR（架构决策记录）

路径：`docs/adr/`

| 文件 | 主题 |
|------|------|
| `0001-zero-dep-wsgi.md` | 零依赖 WSGI 后端架构 |
| `0002-deepseek-v4-flash.md` | 采用 DeepSeek V4 Flash 模型 |
| `0003-curated-knowledge-base.md` | 精选知识库策略 |
| `0004-trip-persistence-timeline.md` | 旅行行程持久化与时间线 |
| `0005-city-comparison.md` | 城市对比功能 |
| `0006-visa-kit-mvp.md` | 签证工具 MVP |

### 3.3 UI/UX 设计文档（共 16 份）

| 文件 | 路径 | 来源 | 说明 |
|------|------|------|------|
| `design-principles.md` | `docs/uiux/` | VP-Hermes-Web | 设计原则 |
| `design-tokens.md` | `docs/uiux/` | VP-Hermes-Web | 设计 Token 系统 |
| `assets.md` | `docs/uiux/` | VP-Hermes-Web | UI 资产说明 |
| `accessibility.md` | `docs/uiux/` | VP-Hermes-Web | 无障碍设计 |
| `interactions.md` | `docs/uiux/` | VP-Hermes-Web | 交互动效规范 |
| `navigation.md` | `docs/uiux/` | VP-Hermes-Web | 导航架构 |
| `responsive.md` | `docs/uiux/` | VP-Hermes-Web | 响应式设计 |
| `translation-ui.md` | `docs/uiux/` | VP-Hermes-Web | 多语言 UI 方案 |
| `visebits.md` | `docs/uiux/` | VP-Hermes-Web | ViseBits 组件说明 |
| `README.md` | `docs/uiux/` | VP-Hermes-Web | UI/UX 文档索引 |
| `dashboard.md` | `docs/uiux/` | VP-Codex-Web（独有） | Dashboard 设计 |
| `components/button.md` | `docs/uiux/components/` | VP-Hermes-Web | 按钮组件规范 |
| `components/city-card.md` | `docs/uiux/components/` | VP-Hermes-Web | 城市卡片组件规范 |
| `components/README.md` | `docs/uiux/components/` | VP-Hermes-Web | 组件文档索引 |

### 3.4 Agent 文档

| 文件 | 路径 | 说明 |
|------|------|------|
| `domain.md` | `docs/agents/domain.md` | AI Agent 领域模型 |
| `issue-tracker.md` | `docs/agents/issue-tracker.md` | Issue 管理策略 |
| `triage-labels.md` | `docs/agents/triage-labels.md` | Triage 标签系统 |

### 3.5 规划与路线图

| 文件 | 路径 | 说明 |
|------|------|------|
| `ITERATIONS.md` | `docs/ITERATIONS.md` | 迭代总览 |
| `ITERATION_PLAN.md` | `docs/ITERATION_PLAN.md` | 迭代计划 |
| `ITERATION_LOG.md` | `docs/ITERATION_LOG.md` | 迭代日志 |
| `NEW_PLAN.md` | `docs/NEW_PLAN.md` | 新计划 |
| `ROADMAP.md` | `docs/ROADMAP.md` | 产品路线图 |
| `VISION_80_ITERATIONS.md` | `docs/VISION_80_ITERATIONS.md` | 80次迭代愿景 |
| `BRAINSTORM_NEW_ANGLES.md` | `docs/BRAINSTORM_NEW_ANGLES.md` | 新方向头脑风暴 |

### 3.6 分析与报告

| 文件 | 说明 |
|------|------|
| `docs/OPTIMIZATION_REPORT_v2.md` | 优化报告 V2 |
| `docs/REVIEW_v611.md` | v6.1.1 版本回顾 |
| `docs/PRD_USER_SYSTEM.md` | 用户系统 PRD |
| `docs/TECH_USER_SYSTEM.md` | 用户系统技术设计 |
| `docs/AI_FIRST_REDESIGN.md` | AI-first 重新设计 |
| `docs/commercial-upgrade-plan.md` | 商业化升级方案 |

### 3.7 时间线文档（按日期）

| 文件 | 说明 |
|------|------|
| `docs/2026-06-19-editorial-atlas-spec.md` | Editorial Atlas 设计规格 |
| `docs/2026-06-19-iteration-roadmap-text.md` | 迭代路线图 |
| `docs/2026-06-19-review-and-plan.md` | 回顾与计划 |
| `docs/2026-06-19-tdd-implementation-plan.md` | TDD 实施计划 |
| `docs/2026-06-20-agent-transfer-index.md` | Agent 交接索引 |
| `docs/2026-06-20-engineering-handoff-notes.md` | 工程交接笔记 |
| `docs/2026-06-20-first-week-takeover-checklist.md` | 接管第一周清单 |
| `docs/2026-06-20-high-risk-files-guide.md` | 高风险文件指南 |
| `docs/2026-06-20-module-ownership-guide.md` | 模块所有权指南 |
| `docs/2026-06-20-next-2-4-weeks-priority-guide.md` | 未来2-4周优先级 |
| `docs/2026-06-20-production-regression-manual.md` | 生产回归测试手册 |
| `docs/2026-06-20-technical-debt-boundaries.md` | 技术债务边界 |
| `docs/2026-06-21-codex-takeover-note.md` | Codex 接管笔记 |

### 3.8 Superpowers 文档

路径：`docs/superpowers/`

**Plans（6 份）：**
- `2026-06-19-vp-hermes-foundation-editorial-atlas.md`
- `2026-06-20-agent-handoff-final-layer.md`
- `2026-06-20-agent-transfer-index.md`
- `2026-06-20-handoff-doc-restructure.md`
- `2026-06-20-handoff-package-expansion.md`
- `2026-06-20-production-stability-pass.md`

**Specs（6 份）：**
- `2026-06-20-agent-handoff-final-layer-design.md`
- `2026-06-20-agent-transfer-index-design.md`
- `2026-06-20-handoff-doc-restructure-design.md`
- `2026-06-20-handoff-package-expansion-design.md`
- `2026-06-20-production-stability-pass-design.md`
- `2026-06-24-visepanda-v7-design.md`（来自 VP-Claude-Web）

### 3.9 其他文档

| 文件 | 说明 |
|------|------|
| `docs/VERCEL_KEYS_GUIDE.md` | VP-Claude-Web → Vercel Key 配置指南 |
| `docs/handoff/claude-handoff.md` | VP-Claude-Web 版本的交接文档（异于根目录） |

### 3.10 Openspec 规格文档

路径：`openspec/changes/trip-map-visualization/`

| 文件 | 说明 |
|------|------|
| `proposal.md` | 地图可视化功能提案 |
| `design.md` | 设计文档 |
| `tasks.md` | 任务分解 |
| `specs/map-visualization/spec.md` | 详细规格说明 |
| `.openspec.yaml` | Openspec 配置 |

### 3.11 Claude 配置（.claude/）

| 路径 | 说明 |
|------|------|
| `.claude/commands/opsx/apply.md` | Openspec apply 命令 |
| `.claude/commands/opsx/archive.md` | Openspec archive 命令 |
| `.claude/commands/opsx/explore.md` | Openspec explore 命令 |
| `.claude/commands/opsx/propose.md` | Openspec propose 命令 |
| `.claude/skills/openspec-apply-change/SKILL.md` | Openspec 技能 |
| `.claude/skills/openspec-archive-change/SKILL.md` | Openspec 技能 |
| `.claude/skills/openspec-explore/SKILL.md` | Openspec 技能 |
| `.claude/skills/openspec-propose/SKILL.md` | Openspec 技能 |

---

## 四、样式与前端资源（CSS & HTML）

### 4.1 样式表

| 文件 | 来源 | 说明 |
|------|------|------|
| `web/app.css` | VP-Codex-Web（更新版） | 主应用样式表（全站样式） |
| `web/visebits.css` | VP-Hermes-Web（独有） | ViseBits 组件额外样式 |

> 注：VP-Claude-Web 的 11 模块化 CSS（`web/css/`）未合并，已整合为 `app.css`。

### 4.2 HTML 模板

| 文件 | 来源 | 说明 |
|------|------|------|
| `web/index.html` | VP-Hermes-Web（更新版） | 主页面（AI 聊天入口） |
| `web/admin.html` | VP-Hermes-Web | 管理后台页面 |

### 4.3 工具脚本

| 文件 | 来源 | 说明 |
|------|------|------|
| `scripts/generate_city_images.sh` | VP-Hermes-Web | 城市图片生成脚本 |

---

## 五、合并汇总

### 5.1 各类资产总数

| 资产类型 | 数量 |
|----------|:----:|
| SVG 图片 | 3 |
| PNG 图标 | 3 |
| 城市 JPG 图片 | 38 |
| 美食 JPG 图片 | 4 |
| 场景/灵感 JPG 图片 | 4 |
| 品牌 Logo JPG 图片 | 4 |
| **图片小计** | **56** |
| 知识库 JSON（数据） | 15 |
| 翻译 JSON | 4 |
| PWA Manifest JSON | 2 |
| **数据小计** | **21** |
| Root Markdown 文档 | 11 |
| docs/ Markdown 文档 | 64 |
| CSS 样式表 | 2 |
| HTML 模板 | 2 |
| Shell 脚本 | 1 |
| Openspec 文件 | 5 |
| .claude 配置 | 8 |
| **文档小计** | **93** |

### 5.2 各仓库贡献

| 仓库 | 核心贡献 |
|------|----------|
| **VP-Claude-Web** | favicon.svg, bg-mountains.svg, 翻译知识库（4份JSON）, v7 设计/计划文档, VERCEL_KEYS_GUIDE |
| **VP-Hermes-Web** | 16 份 UI/UX 设计文档（核心设计体系）, PROMPT 迭代记录, visebits.css, 品牌定位文档 |
| **VP-Codex-Web** | 优惠套餐数据库, 酒店详细数据, dashboard 设计文档, 更新版根目录核心文档（CONTEXT/DESIGN/HANDOFF/PLAN） |

---

*生成日期：2026-06-29*
*用途：VisePanda 项目重做时的资产索引参考*
