-- VisePanda Editorial knowledge base (Issue #49, Explore Dianping-style redesign).
-- Curated POIs are an editorial overlay on top of live Amap data: every row
-- anchors to a real, Amap-verified POI (amap_poi_id) and adds a foreign-
-- traveler-facing perspective Amap itself doesn't provide (English summary,
-- practical tags, public-list badges). Populated by the curate-city-pois
-- skill; never write user-generated or scraped review content here (ADR-120
-- honesty principle — editorial content must never impersonate real reviews).

create table if not exists public.curated_pois (
  id uuid primary key default gen_random_uuid(),
  city_id text not null,
  category text not null,
  amap_poi_id text not null,
  name text not null,
  name_en text,
  editorial_summary text,
  tags text[] not null default '{}',
  list_badges text[] not null default '{}',
  photo_url text,
  rank integer not null default 0,
  source text not null default 'llm_seed',
  source_url text,
  verified_at timestamptz,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint curated_pois_source_check
    check (source in ('wikivoyage', 'official_list', 'llm_seed'))
);

create unique index if not exists curated_pois_city_amap_poi_idx
  on public.curated_pois (city_id, amap_poi_id);

create index if not exists curated_pois_city_category_rank_idx
  on public.curated_pois (city_id, category, rank);

alter table public.curated_pois enable row level security;

-- Editorial content is public read (no auth required) — it's marketing
-- copy, not user data. Writes only ever happen via the service role
-- (curate-city-pois skill output reviewed and imported by the architect).
drop policy if exists "curated pois public read" on public.curated_pois;
create policy "curated pois public read"
  on public.curated_pois
  for select
  to anon, authenticated
  using (true);

grant select on public.curated_pois to anon, authenticated;
