-- Beijing curated_pois seed — shopping category, Phase 1 remainder (Issue #53).
-- Produced manually (2026-07-07): government pedestrian-street/tourism-
-- leisure-district designations (北京市商务局/beijing.gov.cn, 2025-07-24
-- recheck announcement) for the two officially-ranked entries, general
-- popularity/recognition signal for the rest -> Amap real-API verification.
-- One candidate was discarded: 南锣鼓巷 (Nanluoguxiang) — real Amap POI
-- B0FFFAH7I9, but it's already seeded under 'attractions' in
-- beijing_attractions.sql; re-inserting it here under 'shopping' with the
-- same amap_poi_id would silently overwrite that row's category via the
-- ON CONFLICT clause (unique index is city_id+amap_poi_id, not +category),
-- so it's intentionally left out of this batch.
-- Run this in the Supabase SQL Editor AFTER 0006_curated_pois.sql.

insert into public.curated_pois
  (city_id, category, amap_poi_id, name, name_en, editorial_summary, tags, list_badges, rank, source, source_url, verified_at)
values
  ('beijing', 'shopping', 'B000A48169', '王府井步行街', 'Wangfujing Pedestrian Street',
   'Beijing''s best-known shopping street, officially designated a National Model Pedestrian Street in 2021 and reconfirmed in 2025 — department stores, flagship brand stores, and street snacks side by side.',
   array['pedestrian-street', 'must-see', 'central-location'], array['national-model-pedestrian-street'], 1, 'official_list',
   'https://www.beijing.gov.cn/renwen/sy/whkb/202107/t20210729_2450873.html', now()),

  ('beijing', 'shopping', 'B0FFFT5A1C', '三里屯太古里', 'Sanlitun Taikoo Li',
   'An open-air designer-mall complex and one of Beijing''s officially recognized national-level tourism-leisure districts — flagship international fashion stores, Beijing''s biggest Apple Store, and a lively nightlife scene.',
   array['upscale', 'nightlife', 'international-brands'], array['national-tourism-leisure-district'], 2, 'official_list',
   'https://finance.sina.cn/2025-07-24/detail-infhqhuw4099662.d.html', now()),

  ('beijing', 'shopping', 'B000A8UR3U', '前门大街', 'Qianmen Street',
   'A restored century-old commercial street just south of Tiananmen, lined with time-honored Chinese brands (old pharmacies, tea shops, silk stores) in a Ming-Qing streetscape — a national-level tourism-leisure district.',
   array['historic', 'time-honored-brands', 'walkable'], array['national-tourism-leisure-district'], 3, 'official_list',
   'https://www.jiemian.com/article/6791142.html', now()),

  ('beijing', 'shopping', 'B000A19E0A', '秀水街', 'Silk Street Market',
   'A multi-floor indoor market that has served international visitors for nearly 40 years, known for silk, tea, ceramics, pearls, and tailored clothing — bargaining is expected.',
   array['souvenirs', 'bargaining-expected', 'tourist-oriented'], array[]::text[], 4, 'llm_seed',
   null, now()),

  ('beijing', 'shopping', 'B0FFIPP0IM', '潘家园旧货市场', 'Panjiayuan Antique Market',
   'China''s largest flea market for antiques, folk crafts, and collectibles, busiest on weekend mornings when tens of thousands of shoppers (including many foreign buyers) come to browse and haggle.',
   array['antiques', 'weekend-only-at-full-scale', 'bargaining-expected'], array[]::text[], 5, 'llm_seed',
   null, now()),

  ('beijing', 'shopping', 'B000A9R4FE', '北京SKP', 'Beijing SKP',
   'One of mainland China''s highest-grossing luxury department stores, anchoring the CBD shopping district with international designer flagships and an experiential "SKP-S" annex.',
   array['luxury', 'department-store', 'english-friendly-staff'], array[]::text[], 6, 'llm_seed',
   null, now()),

  ('beijing', 'shopping', 'B000A7CTMA', '北京市百货大楼', 'Beijing Department Store (Wangfujing)',
   'A state-owned department store on Wangfujing Street operating continuously since the 1950s — a straightforward, no-frills option for everyday goods and gifts in the heart of the shopping district.',
   array['historic', 'everyday-goods', 'central-location'], array[]::text[], 7, 'llm_seed',
   null, now())
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
