import Link from 'next/link';

import { PageContainer } from '@/components/PageContainer';
import { fetchApiJson } from '@/lib/api';

type DestinationDetail = {
  id: string;
  name: string;
  summary: string;
  body: string;
  highlights: string[];
  publishedAt: string | null;
};

export default async function DestinationDetailPage(props: { params: Promise<{ id: string }> }) {
  const { id } = await props.params;
  const payload = await fetchApiJson<DestinationDetail>(`/destinations/${id}?locale=zh-CN`);
  const destination = payload.data;

  return (
    <PageContainer title={destination.name}>
      <div className="space-y-5">
        <div className="rounded-xl border bg-white p-5 shadow-sm">
          <p className="text-sm text-zinc-600">{destination.summary}</p>
          <div className="mt-4 whitespace-pre-wrap text-sm leading-7 text-zinc-800">{destination.body}</div>
        </div>

        <div className="rounded-xl border bg-white p-5 shadow-sm">
          <h2 className="text-base font-medium">亮点</h2>
          <ul className="mt-3 list-disc space-y-2 pl-5 text-sm text-zinc-700">
            {destination.highlights.map((highlight) => (
              <li key={highlight}>{highlight}</li>
            ))}
          </ul>
        </div>

        <div className="flex gap-3">
          <Link className="rounded-lg bg-zinc-900 px-4 py-2 text-sm text-white" href="/ai/plan-trip">
            用 AI 规划这次旅行
          </Link>
          <Link className="rounded-lg border px-4 py-2 text-sm text-zinc-700" href="/destinations">
            返回列表
          </Link>
        </div>
      </div>
    </PageContainer>
  );
}
