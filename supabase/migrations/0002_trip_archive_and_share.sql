-- VisePanda Supabase schema update (task 3.5)
-- Adds an "archived" trip status and read-only public access for shared trips.
-- Run this in the Supabase SQL Editor after 0001_init_trip_schema.sql has already been applied.

alter table public.trips drop constraint trips_status_check;
alter table public.trips
  add constraint trips_status_check check (status in ('draft', 'ready', 'shared', 'archived'));

create policy "anyone reads shared trips" on public.trips
  for select using (share_token is not null);

create policy "anyone reads canvas of shared trips" on public.canvas_versions
  for select using (
    exists (
      select 1 from public.trips
      where trips.id = canvas_versions.trip_id
        and trips.share_token is not null
    )
  );
