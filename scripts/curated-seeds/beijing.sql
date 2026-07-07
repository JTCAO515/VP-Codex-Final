-- Beijing curated_pois seed — pilot batch, food category (Issue #49).
-- Produced by the curate-city-pois pipeline (manual first run, 2026-07-05):
-- Black Pearl Guide 2025/2026 candidates -> Amap real-API verification.
-- Two candidates were discarded per the compliance rule (verify or drop):
--   - 楚膳四季·24节气餐厅(亮马桥威斯汀店): found on Amap but flagged
--     "暂停营业" (temporarily closed) — not seeded.
--   - 鮨藤本 Sushi Fujimoto: zero Amap results under any keyword variant
--     tried — could not verify it exists as a real, findable POI — not seeded.
-- Run this in the Supabase SQL Editor AFTER 0006_curated_pois.sql.

insert into public.curated_pois
  (city_id, category, amap_poi_id, name, name_en, editorial_summary, tags, list_badges, rank, source, source_url, verified_at)
values
  ('beijing', 'food', 'B0FFGPXKK4', '雪崴(中国红街店)', 'Xue Wei (China Red Street)',
   'An eight-time Black Pearl-listed Chinese fine-dining restaurant known for its refined, contemporary take on Chinese classics.',
   array['fine-dining', 'time-honored'], array['black-pearl-2025'], 1, 'official_list',
   'https://www.visitbeijing.com.cn/article/4KzKb2d0LkU', now()),

  ('beijing', 'food', 'B0FFFDTV3B', '利苑酒家(金宝大厦店)', 'Lei Garden (Jinbao Tower)',
   'A long-running Cantonese institution and eight-time Black Pearl honoree, prized for classic dim sum and roast meats.',
   array['cantonese', 'dim-sum', 'time-honored'], array['black-pearl-2025'], 2, 'official_list',
   'https://www.visitbeijing.com.cn/article/4KzKb2d0LkU', now()),

  ('beijing', 'food', 'B000A93876', '北京四季酒店·MIO', 'Four Seasons Beijing · MIO',
   'The Four Seasons Beijing''s Italian restaurant, an eight-time Black Pearl-listed hotel dining destination.',
   array['italian', 'hotel-dining', 'english-menu'], array['black-pearl-2025'], 3, 'official_list',
   'https://www.visitbeijing.com.cn/article/4KzKb2d0LkU', now()),

  ('beijing', 'food', 'B0M69HBK11', '北京四季酒店·采逸轩', 'Four Seasons Beijing · Cai Yi Xuan',
   'The Four Seasons Beijing''s Cantonese restaurant, an eight-time Black Pearl-listed hotel dining destination.',
   array['cantonese', 'hotel-dining', 'english-menu'], array['black-pearl-2025'], 4, 'official_list',
   'https://www.visitbeijing.com.cn/article/4KzKb2d0LkU', now()),

  ('beijing', 'food', 'B0I0Y7WWTD', '兰斋Lamdre植物料理餐厅', 'Lamdre',
   'A plant-based fine-dining restaurant upgraded to two Michelin stars, offering a refined vegetarian tasting menu.',
   array['vegetarian-friendly', 'fine-dining', 'michelin'], array['black-pearl-2025', 'michelin-2-star'], 5, 'official_list',
   'https://www.visitbeijing.com.cn/article/4KzKb2d0LkU', now()),

  ('beijing', 'food', 'B0FFM71N8A', '京季荣派官府菜', 'Jingji Rongpai Imperial Cuisine',
   'An imperial-court-style Chinese restaurant upgraded to three Black Pearl diamonds in the 2026 guide.',
   array['fine-dining', 'imperial-cuisine'], array['black-pearl-2026'], 6, 'official_list',
   'https://news.qq.com/rain/a/20260128A079PZ00', now()),

  ('beijing', 'food', 'B0JDA1CNVA', '1996川菜.主厨餐厅', '1996 Sichuan Cuisine · Chef''s Restaurant',
   'A chef-driven Sichuan restaurant upgraded to two Black Pearl diamonds in the 2026 guide.',
   array['spicy', 'sichuan', 'fine-dining'], array['black-pearl-2026'], 7, 'official_list',
   'https://news.qq.com/rain/a/20260128A079PZ00', now())
on conflict (city_id, amap_poi_id) do update set
  name = excluded.name,
  name_en = excluded.name_en,
  editorial_summary = excluded.editorial_summary,
  tags = excluded.tags,
  list_badges = excluded.list_badges,
  rank = excluded.rank,
  source = excluded.source,
  source_url = excluded.source_url,
  verified_at = excluded.verified_at,
  updated_at = now();
