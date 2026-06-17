import { AppHeader } from '@/components/AppHeader';

export function PageContainer(props: { title: string; children: React.ReactNode }) {
  return (
    <div className="min-h-screen bg-zinc-50 text-zinc-950">
      <AppHeader />
      <main className="mx-auto max-w-6xl px-4 py-6">
        <h1 className="mb-4 text-2xl font-semibold">{props.title}</h1>
        {props.children}
      </main>
    </div>
  );
}
