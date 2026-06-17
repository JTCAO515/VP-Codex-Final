import Link from 'next/link';

import { PageContainer } from '@/components/PageContainer';

export default function HomePage() {
  return (
    <PageContainer title="游客前台">
      <div className="grid gap-4 md:grid-cols-3">
        <Link className="rounded-xl border bg-white p-5 shadow-sm transition hover:border-zinc-300" href="/destinations">
          <h2 className="text-lg font-medium">浏览目的地</h2>
          <p className="mt-2 text-sm text-zinc-600">直接查看目的地列表和详情，不需要先登录。</p>
        </Link>
        <Link className="rounded-xl border bg-white p-5 shadow-sm transition hover:border-zinc-300" href="/tools">
          <h2 className="text-lg font-medium">实用工具</h2>
          <p className="mt-2 text-sm text-zinc-600">查看旅行工具和内容指南，后续会接上详情页。</p>
        </Link>
        <Link className="rounded-xl border bg-white p-5 shadow-sm transition hover:border-zinc-300" href="/ai/plan-trip">
          <h2 className="text-lg font-medium">AI 行程规划</h2>
          <p className="mt-2 text-sm text-zinc-600">先规划行程，保存时再触发登录门禁。</p>
        </Link>
      </div>
    </PageContainer>
  );
}
