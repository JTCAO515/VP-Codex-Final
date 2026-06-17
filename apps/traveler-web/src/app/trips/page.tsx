import Link from 'next/link';

import { PageContainer } from '@/components/PageContainer';

export default function TripsPage() {
  return (
    <PageContainer title="我的行程">
      <div className="space-y-4">
        <div className="rounded-xl border bg-white p-5 shadow-sm">
          <p className="text-sm text-zinc-600">
            这里会接入受保护的 `/trips` 资产列表。当前先保留网页端入口和页面壳，下一步补登录校验与列表加载。
          </p>
        </div>
        <Link className="inline-flex rounded-lg border px-4 py-2 text-sm text-zinc-700" href="/login">
          先去登录
        </Link>
      </div>
    </PageContainer>
  );
}
