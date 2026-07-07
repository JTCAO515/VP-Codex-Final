---
name: curate-city-pois
description: 为指定城市采集 VisePanda curated_pois 精选场所数据(黑珍珠/米其林/A级景区等公开榜单 + Wikivoyage,LLM 结构化,高德查证锚定),输出 Supabase seed。用法:/curate-city-pois <cityId> [category]。cityId 须在 AMAP_CITY_MAP 内;扩新城市先扩 lib/explore/amapSearch.ts 的 AMAP_CITY_MAP。
---

# curate-city-pois — 城市精选 POI 采集管线

为 `$ARGUMENTS` 指定的城市(可选第二参数限定品类 food/attractions/hotels/shopping/experiences)执行完整采集管线,产出 `scripts/curated-seeds/<cityId>.json` + 对应 Supabase upsert SQL。规格背景见 `docs/planning/explore-dianping-redesign.md` §7.2。

## 合规红线(先读,违反任何一条立即停止)

1. **禁止**爬取或引用大众点评/美团/小红书的页面内容、评分、评价文本(民事诉讼史 + 刑事先例)。黑珍珠榜单是美团发布的,但**入选名单本身是公开新闻事实**——只允许通过新闻报道/官方发布会通稿采集"哪些店入选了哪年榜单"这一事实,不允许访问点评/美团站内页面抓任何数据。
2. 只采事实:场所名、榜单入选事实、地址/营业时间/票价等官网公开信息。**任何评价性文本必须是 LLM 基于源材料重新撰写的英文 editorial_summary**,不得复制任何平台的评论。
3. Wikivoyage/Wikipedia 内容按 CC BY-SA 使用,`source` 字段记 `wikivoyage`,App 致谢页需统一署名(检查 `ios/`/`android/` About 页是否已有,没有则在产出报告里提醒架构师)。
4. 每条记录必须能通过高德真实 API 查证存在;查不到的**丢弃**,不入库。

## 管线步骤

### 1. 榜单信号采集(WebSearch)
针对目标城市逐榜搜索公开名单(每类取最新年份):
- `黑珍珠餐厅指南 <城市> <年份> 名单` → food,badge `black-pearl-<year>`
- `米其林指南 <城市> 星级餐厅 名单` → food,badge `michelin-<stars>-star`(注意米其林只覆盖北上广蓉等少数城市,没有就跳过)
- `<城市> 5A 4A 级景区 名录` → attractions,badge `aaaaa-scenic`/`aaaa-scenic`
- `<城市> 本地人 打卡 榜单 推荐`(公开媒体报道) → 各品类,badge `local-favorite`
- `<城市> 老字号 名录` → food/shopping,badge `time-honored-brand`

每个候选记录:名称(中文)、榜单出处 URL、badge。目标每品类 15-40 个候选。

### 2. Wikivoyage 补充(WebFetch)
拉取 `https://en.wikivoyage.org/wiki/<City>` 的 See/Eat/Buy/Sleep/Do 分区,提取场所名 + 英文描述片段(这是 editorial_summary 的最佳源材料,天然英文游客视角)。与榜单候选合并去重。

### 3. LLM 结构化
把每个候选的源材料(榜单事实 + Wikivoyage 片段 + 官网事实)交给 LLM 结构化为:
```json
{
  "city_id": "chengdu",
  "category": "food",
  "name": "陈麻婆豆腐(骡马市店)",
  "name_en": "Chen Mapo Tofu (Luomashi)",
  "editorial_summary": "The birthplace of mapo tofu — this 150-year-old institution still serves the fiery original. English picture menu available.",
  "tags": ["spicy", "english-menu", "time-honored"],
  "list_badges": ["black-pearl-2026", "time-honored-brand"],
  "source": "official_list",
  "source_url": "<榜单报道URL>",
  "rank": 1
}
```
硬约束:editorial_summary 只基于给定源材料改写,禁止补充源材料里没有的"事实";tags 只打有依据的。

### 4. 高德查证(真实 API)
对每条用 `AMAP_API_KEY`(在 `.env.local`)调高德文本搜索(`keywords=名称&city=城市`),确认存在 → 回填 `amap_poi_id`、坐标、地址;查不到或明显歇业 → 丢弃并记入报告。

### 5. 产出
- `scripts/curated-seeds/<cityId>.json`(全量结构化数据)
- `scripts/curated-seeds/<cityId>.sql`(Supabase `curated_pois` upsert,on conflict (amap_poi_id) do update)
- 终端报告:候选数 → 查证通过数 → 丢弃清单(附原因),各品类分布,badge 分布

## 城市扩张路线

Phase 1:beijing/shanghai/chengdu → Phase 2:xian/guangzhou/hangzhou/suzhou/chongqing/nanjing → Phase 3:新城市(先扩 `AMAP_CITY_MAP` + 移动端城市选择器,再跑本 skill)。

## 不做的事

- 不直接写 Supabase 生产库(seed 文件交架构师抽验后手动导入)
- 不修改 `lib/`/`app/` 运行时代码(那是 Issue #49 的 API 合并部分)
- 不采集/存储任何个人信息
