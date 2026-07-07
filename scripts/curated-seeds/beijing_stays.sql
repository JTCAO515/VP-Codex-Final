-- Beijing curated_pois seed — stays (hotels) category, Phase 1 remainder (Issue #53).
-- Produced by the curate-city-pois pipeline (manual run, 2026-07-05):
-- Well-known five-star / historic hotel signal (visitbeijing.com.cn, Beijing
-- municipal government archive, public press) -> Amap real-API verification.
-- One candidate was discarded per the compliance rule (verify or drop):
--   - 颐和安缦 (Aman Summer Palace): Amap has no POI registered under this
--     brand name — the only match at that address is "北京颐和宾馆" (Beijing
--     Yihe Hotel), a differently-named entity. Seeding it under the Aman
--     brand name against a mismatched Amap id/name would mislead users, so
--     it was dropped rather than force-matched.
-- Run this in the Supabase SQL Editor AFTER 0006_curated_pois.sql.

insert into public.curated_pois
  (city_id, category, amap_poi_id, name, name_en, editorial_summary, tags, list_badges, rank, source, source_url, verified_at)
values
  ('beijing', 'stays', 'B000A10FBB', '北京饭店', 'Beijing Hotel',
   'One of Beijing''s oldest hotels, opened in 1900 and long used to host state banquets and foreign dignitaries, a short walk from Tiananmen Square and Wangfujing.',
   array['historic', 'central-location', 'five-star'], array['time-honored-brand'], 1, 'official_list',
   'https://www.beijing.gov.cn/renwen/jrbj/202012/t20201228_2187022.html', now()),

  ('beijing', 'stays', 'B000A7BLUQ', '北京贵宾楼饭店', 'Grand Hotel Beijing',
   'A five-star hotel directly adjoining Tiananmen Square, historically used for hosting foreign heads of state and major diplomatic events.',
   array['historic', 'central-location', 'five-star'], array['time-honored-brand'], 2, 'official_list',
   'https://www.grandhotelbeijing.com/', now()),

  ('beijing', 'stays', 'B000AA0F26', '北京华尔道夫酒店', 'Waldorf Astoria Beijing',
   'A Waldorf Astoria property blending restored historic Beijing courtyard architecture with contemporary luxury, steps from Wangfujing.',
   array['luxury', 'central-location', 'international-brand'], array['five-star-luxury'], 3, 'official_list',
   'https://www.visitbeijing.com.cn/article/47QrsyENcj1', now()),

  ('beijing', 'stays', 'B000A85J5D', '北京国贸大酒店', 'China World Hotel',
   'A Shangri-La-managed luxury hotel inside the China World Trade Center complex, in the heart of Beijing''s CBD and adjoining upscale shopping.',
   array['luxury', 'cbd', 'shopping-nearby'], array['five-star-luxury'], 4, 'official_list',
   'https://www.visitbeijing.com.cn/article/47QrsyENcj1', now()),

  ('beijing', 'stays', 'B000A7BJH7', '北京东方君悦大酒店', 'Grand Hyatt Beijing',
   'A Hyatt-managed luxury hotel on East Chang''an Avenue, a few minutes'' walk from Tiananmen Square and the Forbidden City.',
   array['luxury', 'central-location', 'international-brand'], array['five-star-luxury'], 5, 'official_list',
   'https://www.visitbeijing.com.cn/article/47QrsyENcj1', now()),

  ('beijing', 'stays', 'B000A7ISXS', '北京王府半岛酒店', 'Peninsula Beijing',
   'A Peninsula Hotels property beside Wangfujing pedestrian street, known for its upscale shopping arcade and proximity to the Forbidden City.',
   array['luxury', 'central-location', 'shopping-nearby'], array['five-star-luxury'], 6, 'official_list',
   'https://www.visitbeijing.com.cn/article/47QrsyENcj1', now())
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
