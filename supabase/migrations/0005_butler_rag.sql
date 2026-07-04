-- Butler 2.0 Phase C RAG storage.
-- Service works without this table; static keyword RAG remains fallback.

create extension if not exists vector;

create table if not exists public.rag_documents (
  id uuid primary key default gen_random_uuid(),
  title text not null,
  body text not null,
  source_label text not null,
  category text not null default 'travel',
  embedding vector(1536),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index if not exists rag_documents_category_idx
  on public.rag_documents (category);

create index if not exists rag_documents_embedding_idx
  on public.rag_documents
  using ivfflat (embedding vector_cosine_ops)
  with (lists = 100);

alter table public.rag_documents enable row level security;

drop policy if exists "rag documents read authenticated" on public.rag_documents;
create policy "rag documents read authenticated"
  on public.rag_documents
  for select
  to authenticated
  using (true);

grant select on public.rag_documents to authenticated;
