import Link from 'next/link';

import { PageContainer } from '@/components/PageContainer';

export default function PlanTripPage() {
  return (
    <PageContainer title="AI 行程规划">
      <div className="space-y-4">
        <div className="rounded-xl border bg-white p-5 shadow-sm">
          <p className="text-sm text-zinc-600">
            这里会接入 `/ai/tasks/plan-trip`。当前先提供网页端入口和页面骨架，下一步补表单、结果展示和“保存时登录”门禁。
          </p>
        </div>
        <div className="flex gap-3">
          <Link className="rounded-lg border px-4 py-2 text-sm text-zinc-700" href="/destinations">
            先去看目的地
          </Link>
          <Link className="rounded-lg border px-4 py-2 text-sm text-zinc-700" href="/trips">
            查看我的行程
          </Link>
        </div>
      </div>
    </PageContainer>
  );
}
