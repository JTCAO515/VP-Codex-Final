-- VisePanda Supabase schema (task 2.2)
-- Tables: users, trips, messages, canvas_versions.
-- Not yet applied to a live project; this is the schema contract for task 3.3 persistence work.

create table if not exists public.users (
  id uuid primary key references auth.users (id) on delete cascade,
  email text unique not null,
  display_name text,
  created_at timestamptz not null default now()
);

create table if not exists public.trips (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null references public.users (id) on delete cascade,
  title text not null,
  status text not null default 'draft' check (status in ('draft', 'ready', 'shared')),
  share_token text unique,
  current_canvas_version_id uuid,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index if not exists trips_owner_id_idx on public.trips (owner_id);

create table if not exists public.canvas_versions (
  id uuid primary key default gen_random_uuid(),
  trip_id uuid not null references public.trips (id) on delete cascade,
  canvas jsonb not null,
  last_updated_reason text not null default '',
  created_at timestamptz not null default now()
);

create index if not exists canvas_versions_trip_id_idx on public.canvas_versions (trip_id, created_at desc);

alter table public.trips
  add constraint trips_current_canvas_version_fk
  foreign key (current_canvas_version_id) references public.canvas_versions (id) on delete set null;

create table if not exists public.messages (
  id uuid primary key default gen_random_uuid(),
  trip_id uuid not null references public.trips (id) on delete cascade,
  role text not null check (role in ('user', 'assistant')),
  content text not null,
  created_at timestamptz not null default now()
);

create index if not exists messages_trip_id_idx on public.messages (trip_id, created_at asc);

alter table public.users enable row level security;
alter table public.trips enable row level security;
alter table public.canvas_versions enable row level security;
alter table public.messages enable row level security;

create policy "users read own row" on public.users
  for select using (auth.uid() = id);

create policy "owner reads own trips" on public.trips
  for select using (auth.uid() = owner_id);

create policy "owner writes own trips" on public.trips
  for insert with check (auth.uid() = owner_id);

create policy "owner updates own trips" on public.trips
  for update using (auth.uid() = owner_id);

create policy "owner reads own canvas versions" on public.canvas_versions
  for select using (
    auth.uid() = (select owner_id from public.trips where trips.id = canvas_versions.trip_id)
  );

create policy "owner writes own canvas versions" on public.canvas_versions
  for insert with check (
    auth.uid() = (select owner_id from public.trips where trips.id = canvas_versions.trip_id)
  );

create policy "owner reads own messages" on public.messages
  for select using (
    auth.uid() = (select owner_id from public.trips where trips.id = messages.trip_id)
  );

create policy "owner writes own messages" on public.messages
  for insert with check (
    auth.uid() = (select owner_id from public.trips where trips.id = messages.trip_id)
  );
