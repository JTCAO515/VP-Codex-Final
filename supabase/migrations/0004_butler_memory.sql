-- Butler 2.0 Phase B memory tables.
-- Supports service-side writes via service role, logged-in ownership via user_id,
-- and anonymous guest memory via opaque user_key/trip_key values.

create extension if not exists pgcrypto;

create table if not exists public.trip_memories (
  id uuid primary key default gen_random_uuid(),
  trip_id uuid null references public.trips(id) on delete cascade,
  trip_key text not null,
  user_id uuid null references public.users(id) on delete cascade,
  guest_key text null,
  kind text not null,
  body text not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index if not exists trip_memories_trip_key_updated_idx
  on public.trip_memories (trip_key, updated_at desc);

create table if not exists public.user_memories (
  id uuid primary key default gen_random_uuid(),
  user_id uuid null references public.users(id) on delete cascade,
  user_key text not null,
  memory_key text not null,
  memory_value text not null,
  confidence numeric(3,2) not null default 0 check (confidence >= 0 and confidence <= 1),
  evidence jsonb not null default '[]'::jsonb,
  source text not null check (source in ('explicit', 'inferred')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique (user_key, memory_key, memory_value)
);

create index if not exists user_memories_user_key_confidence_idx
  on public.user_memories (user_key, confidence desc, updated_at desc);

create table if not exists public.chat_logs (
  id uuid primary key default gen_random_uuid(),
  session_key text not null,
  user_key text not null,
  trip_key text null,
  user_id uuid null references public.users(id) on delete set null,
  guest_key text null,
  user_message text not null,
  assistant_message text not null,
  created_at timestamptz not null default now(),
  expires_at timestamptz not null default (now() + interval '30 days')
);

create index if not exists chat_logs_session_created_idx
  on public.chat_logs (session_key, created_at desc);

create index if not exists chat_logs_expires_at_idx
  on public.chat_logs (expires_at);

alter table public.trip_memories enable row level security;
alter table public.user_memories enable row level security;
alter table public.chat_logs enable row level security;

drop policy if exists "trip memories select own rows" on public.trip_memories;
create policy "trip memories select own rows"
  on public.trip_memories
  for select
  to authenticated
  using ((select auth.uid()) = user_id);

drop policy if exists "trip memories insert own rows" on public.trip_memories;
create policy "trip memories insert own rows"
  on public.trip_memories
  for insert
  to authenticated
  with check ((select auth.uid()) = user_id);

drop policy if exists "trip memories update own rows" on public.trip_memories;
create policy "trip memories update own rows"
  on public.trip_memories
  for update
  to authenticated
  using ((select auth.uid()) = user_id)
  with check ((select auth.uid()) = user_id);

drop policy if exists "user memories select own rows" on public.user_memories;
create policy "user memories select own rows"
  on public.user_memories
  for select
  to authenticated
  using ((select auth.uid()) = user_id);

drop policy if exists "user memories insert own rows" on public.user_memories;
create policy "user memories insert own rows"
  on public.user_memories
  for insert
  to authenticated
  with check ((select auth.uid()) = user_id);

drop policy if exists "user memories update own rows" on public.user_memories;
create policy "user memories update own rows"
  on public.user_memories
  for update
  to authenticated
  using ((select auth.uid()) = user_id)
  with check ((select auth.uid()) = user_id);

drop policy if exists "chat logs select own rows" on public.chat_logs;
create policy "chat logs select own rows"
  on public.chat_logs
  for select
  to authenticated
  using ((select auth.uid()) = user_id);

drop policy if exists "chat logs insert own rows" on public.chat_logs;
create policy "chat logs insert own rows"
  on public.chat_logs
  for insert
  to authenticated
  with check ((select auth.uid()) = user_id);

grant select, insert, update on public.trip_memories to authenticated;
grant select, insert, update on public.user_memories to authenticated;
grant select, insert on public.chat_logs to authenticated;
