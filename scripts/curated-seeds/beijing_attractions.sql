-- Beijing curated_pois seed — attractions category, Phase 1 remainder (Issue #53).
-- Produced by the curate-city-pois pipeline (manual run, 2026-07-05):
-- AAAAA (5A) scenic-area registry (visitbeijing.com.cn official listing) for
-- the "hot list" signal + Wikivoyage "See" section for English editorial
-- source material -> Amap real-API verification (each candidate confirmed to
-- exist, open, and matched against the correct POI id/coordinates today).
-- No candidates were discarded this batch — all 8 verified cleanly.
-- Run this in the Supabase SQL Editor AFTER 0006_curated_pois.sql.

insert into public.curated_pois
  (city_id, category, amap_poi_id, name, name_en, editorial_summary, tags, list_badges, rank, source, source_url, verified_at)
values
  ('beijing', 'attractions', 'B000A8UIN8', '故宫博物院', 'Forbidden City',
   'The imperial palace of the Ming and Qing dynasties, now the world''s largest collection of preserved ancient wooden structures and a UNESCO World Heritage Site.',
   array['unesco-world-heritage', 'must-see', 'advance-booking-required'], array['aaaaa-scenic'], 1, 'official_list',
   'https://www.visitbeijing.com.cn/article/47QqdOB894T', now()),

  ('beijing', 'attractions', 'B000A45467', '八达岭长城', 'Badaling Great Wall',
   'The most-visited and most heavily restored section of the Great Wall, easily reached from central Beijing but often crowded.',
   array['unesco-world-heritage', 'must-see', 'crowded'], array['aaaaa-scenic'], 2, 'official_list',
   'https://www.visitbeijing.com.cn/article/47QqdOB894T', now()),

  ('beijing', 'attractions', 'B000A09F5A', '慕田峪长城', 'Mutianyu Great Wall',
   'A restored Great Wall section offering sweeping mountain views with noticeably fewer crowds than Badaling — a common alternative pick for first-time visitors.',
   array['unesco-world-heritage', 'scenic-views'], array['aaaaa-scenic'], 3, 'official_list',
   'https://en.wikivoyage.org/wiki/Beijing', now()),

  ('beijing', 'attractions', 'B000A81CB2', '天坛公园', 'Temple of Heaven',
   'A Ming and Qing dynasty imperial complex where emperors performed annual ceremonies praying for a good harvest; the surrounding park is a popular spot for locals practicing tai chi.',
   array['unesco-world-heritage', 'must-see'], array['aaaaa-scenic'], 4, 'official_list',
   'https://www.visitbeijing.com.cn/article/47QqdOB894T', now()),

  ('beijing', 'attractions', 'B000A7O1CU', '颐和园', 'Summer Palace',
   'A Qing dynasty imperial garden and lake built as a summer retreat for the emperors and their entourages, now a UNESCO World Heritage Site.',
   array['unesco-world-heritage', 'must-see', 'lake-views'], array['aaaaa-scenic'], 5, 'official_list',
   'https://en.wikivoyage.org/wiki/Beijing', now()),

  ('beijing', 'attractions', 'B0FFFAH7I9', '南锣鼓巷', 'Nanluoguxiang',
   'A historic hutong lane dating to the Yuan Dynasty, roughly 786 meters long, now lined with snack shops, cafes, and small stores — expect heavy foot traffic.',
   array['hutong', 'crowded', 'shopping-nearby'], array['local-favorite'], 6, 'official_list',
   'https://en.wikivoyage.org/wiki/Beijing', now()),

  ('beijing', 'attractions', 'B000A7BGMG', '雍和宫', 'Lama Temple (Yonghe Temple)',
   'Beijing''s largest and most active Tibetan Buddhist temple, a former imperial residence converted into a monastery in the 18th century.',
   array['temple', 'active-worship-site'], array['aaaa-scenic'], 7, 'official_list',
   'https://en.wikivoyage.org/wiki/Beijing', now()),

  ('beijing', 'attractions', 'B000A81FY5', '798艺术区', '798 Art Zone',
   'A former state-run electronics factory complex in Chaoyang District repurposed into a cluster of galleries, studios, and public art installations.',
   array['contemporary-art', 'photography-spot'], array['local-favorite'], 8, 'official_list',
   'https://en.wikivoyage.org/wiki/Beijing', now())
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
