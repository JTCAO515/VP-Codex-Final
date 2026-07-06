-- Beijing curated_pois seed — experiences category, Phase 1 remainder (Issue #53).
-- Produced manually (2026-07-07): this category (bath/massage/spa + teahouse
-- + sports-leisure, Amap types 071400|080300|080500|050600) has no strong
-- official ranking list the way food (Black Pearl) and attractions (AAAAA
-- scenic-area registry) do — this was flagged as a real possibility in
-- Issue #53/#61, not worked around by inventing a fake list. Candidates were
-- chosen for genuine notability (state-recognized cultural heritage status,
-- documented foreign-visitor history, or being a well-known destination in
-- its niche) rather than a formal registry, and marked 'llm_seed' — except
-- the Lao She Teahouse, which does have a citable official/cultural pedigree.
-- No candidates were discarded this batch — all 4 verified cleanly.
-- Run this in the Supabase SQL Editor AFTER 0006_curated_pois.sql.

insert into public.curated_pois
  (city_id, category, amap_poi_id, name, name_en, editorial_summary, tags, list_badges, rank, source, source_url, verified_at)
values
  ('beijing', 'experiences', 'B000A36908', '老舍茶馆(前门店)', 'Lao She Teahouse',
   'A working teahouse and stage for traditional Chinese performance (Peking opera excerpts, kung fu, magic, face-changing) that has hosted foreign heads of state since 1988 — a well-worn but genuine window into teahouse culture.',
   array['live-performance', 'tourist-oriented', 'english-friendly-staff'], array[]::text[], 1, 'official_list',
   'https://s.visitbeijing.com.cn/attraction/117834', now()),

  ('beijing', 'experiences', 'B000A28714', '北京按摩医院(西城院区)', 'Beijing Massage Hospital (Xicheng)',
   'A hospital-grade traditional massage institution founded in 1958; its "tuina manipulation therapy" is listed as a Beijing Municipal Intangible Cultural Heritage item — a more clinical, less spa-like experience than a typical tourist massage parlor.',
   array['traditional-medicine', 'intangible-cultural-heritage', 'clinical-setting'], array['beijing-municipal-ich'], 2, 'official_list',
   'https://zyj.beijing.gov.cn/ylfw/zyyy/201912/t20191219_1314514.html', now()),

  ('beijing', 'experiences', 'B0JKRMRMAK', '健桥盲人按摩', 'Jianqiao Blind Massage Center',
   'A massage clinic staffed by visually-impaired therapists trained through a dedicated vocational program — practical foot/therapeutic massage with a socially meaningful staffing model common across China''s blind-massage tradition.',
   array['traditional-medicine', 'foot-massage'], array[]::text[], 3, 'llm_seed',
   null, now()),

  ('beijing', 'experiences', 'B0FFFGRG67', '九华山庄温泉主题公园', 'Jiuhua Resort Hot Spring',
   'A hot-spring resort in Xiaotangshan, the natural-hot-spring district on Beijing''s northern outskirts — a half-day or overnight escape from the city, popular with both locals and visitors seeking a slower pace.',
   array['hot-spring', 'half-day-trip', 'relaxation'], array[]::text[], 4, 'llm_seed',
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
