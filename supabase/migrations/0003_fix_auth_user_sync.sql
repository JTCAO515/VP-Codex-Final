-- VisePanda schema fix: sync auth.users → public.users automatically.
--
-- Root cause: trips.owner_id FK references public.users(id), but a newly
-- signed-in user only exists in auth.users — not public.users — causing
-- every "Save to Trips" insert to fail with a FK violation.
--
-- Fix 1: trigger that upserts a public.users row whenever a user signs up.
-- Fix 2: RLS policies so authenticated users can insert/update their own row
--        (needed for existing users who signed up before this migration).
-- Run this in the Supabase SQL Editor.

-- ── Trigger function (runs as SECURITY DEFINER → bypasses RLS) ──────────
create or replace function public.handle_new_auth_user()
returns trigger
language plpgsql
security definer set search_path = public
as $$
begin
  insert into public.users (id, email)
  values (new.id, new.email)
  on conflict (id) do update
    set email = excluded.email;
  return new;
end;
$$;

-- Drop if already exists, then recreate
drop trigger if exists on_auth_user_created on auth.users;

create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_auth_user();

-- ── RLS policies so existing users can upsert their own row ─────────────
drop policy if exists "users insert own row" on public.users;
create policy "users insert own row" on public.users
  for insert with check (auth.uid() = id);

drop policy if exists "users update own row" on public.users;
create policy "users update own row" on public.users
  for update using (auth.uid() = id);
