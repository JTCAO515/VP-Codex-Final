import Link from 'next/link';

import { PageContainer } from '@/components/PageContainer';

export default async function TripDetailPage(props: { params: Promise<{ id: string }> }) {
  const { id } = await props.params;

  return (
    <PageContainer title={`行程详情：${id}`}>
      <div className="space-y-4">
        <div className="rounded-xl border bg-white p-5 shadow-sm">
          <p className="text-sm text-zinc-600">
            这里会接入受保护的 `/trips/:id` 详情与 snapshot 能力。当前先提供网页端路由占位。
          </p>
        </div>
        <Link className="inline-flex rounded-lg border px-4 py-2 text-sm text-zinc-700" href="/trips">
          返回我的行程
        </Link>
      </div>
    </PageContainer>
  );
}
