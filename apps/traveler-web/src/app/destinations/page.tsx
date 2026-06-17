import Link from 'next/link';

import { PageContainer } from '@/components/PageContainer';
import { fetchApiJson } from '@/lib/api';

type DestinationSummary = {
  id: string;
  slug: string;
  name: string;
  summary: string;
  locale: string;
};

export default async function DestinationsPage() {
  const payload = await fetchApiJson<DestinationSummary[]>('/destinations?locale=zh-CN');

  return (
    <PageContainer title="目的地">
      <div className="space-y-3">
        {payload.data.map((item) => (
          <Link
            key={item.id}
            className="block rounded-xl border bg-white p-5 shadow-sm transition hover:border-zinc-300"
            href={`/destinations/${item.id}`}
          >
            <h2 className="text-lg font-medium">{item.name}</h2>
            <p className="mt-2 text-sm text-zinc-600">{item.summary}</p>
          </Link>
        ))}
      </div>
    </PageContainer>
  );
}
