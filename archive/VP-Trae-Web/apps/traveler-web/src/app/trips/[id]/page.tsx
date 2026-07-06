import { CreateSnapshotButton } from '@/components/CreateSnapshotButton';
import { PageContainer } from '@/components/PageContainer';
import { fetchApiJson } from '@/lib/api';
import { requireTravelerSession } from '@/lib/guards';

type TripDetail = {
  id: string;
  title: string;
  destination: string;
  startDate: string;
  endDate: string;
  status: string;
  aiSummary: string;
  isFavorite: boolean;
  snapshotCount: number;
  latestSnapshotId: string | null;
  days: Array<{
    id: string;
    dayNumber: number;
    title: string;
    items: Array<{
      id: string;
      type: string;
      title: string;
      startTime: string;
      endTime: string;
      notes: string;
    }>;
  }>;
};

export default async function TripDetailPage(props: { params: Promise<{ id: string }> }) {
  const accessToken = await requireTravelerSession('/trips');
  const { id } = await props.params;
  const payload = await fetchApiJson<TripDetail>(`/trips/${id}`, {
    headers: {
      authorization: `Bearer ${accessToken}`,
    },
  });
  const trip = payload.data;

  return (
    <PageContainer title={trip.title}>
      <div className="space-y-4">
        <div className="rounded-xl border bg-white p-5 shadow-sm">
          <p className="text-sm text-zinc-600">
            {trip.destination} · {trip.startDate} ~ {trip.endDate}
          </p>
          <div className="mt-4 whitespace-pre-wrap text-sm leading-7 text-zinc-800">{trip.aiSummary}</div>
        </div>

        <div className="grid gap-4 md:grid-cols-[minmax(0,1fr)_280px]">
          <div className="rounded-xl border bg-white p-5 shadow-sm">
            <h2 className="text-base font-medium">资产信息</h2>
            <dl className="mt-4 grid gap-3 text-sm text-zinc-700">
              <div className="flex items-center justify-between gap-4 border-b pb-3">
                <dt className="text-zinc-500">状态</dt>
                <dd>{trip.status}</dd>
              </div>
              <div className="flex items-center justify-between gap-4 border-b pb-3">
                <dt className="text-zinc-500">快照数量</dt>
                <dd>{trip.snapshotCount}</dd>
              </div>
              <div className="flex items-center justify-between gap-4">
                <dt className="text-zinc-500">最近快照</dt>
                <dd className="truncate text-right">{trip.latestSnapshotId ?? '暂无'}</dd>
              </div>
            </dl>
          </div>

          <div className="rounded-xl border bg-white p-5 shadow-sm">
            <h2 className="text-base font-medium">快照</h2>
            <p className="mt-2 text-sm leading-6 text-zinc-600">
              在调整行程前先创建一个快照，后面就能把“当前版本”明确沉淀下来。
            </p>
            <div className="mt-4">
              <CreateSnapshotButton tripId={trip.id} />
            </div>
          </div>
        </div>

        <div className="space-y-3">
          {trip.days.map((day) => (
            <section key={day.id} className="rounded-xl border bg-white p-5 shadow-sm">
              <h2 className="text-base font-medium">
                第 {day.dayNumber} 天 · {day.title}
              </h2>
              <ul className="mt-3 space-y-3 text-sm text-zinc-700">
                {day.items.map((item) => (
                  <li key={item.id} className="rounded-lg bg-zinc-50 p-3">
                    <div className="font-medium">{item.title}</div>
                    <div className="mt-1 text-xs text-zinc-500">
                      {item.startTime} - {item.endTime} · {item.type}
                    </div>
                    <div className="mt-2">{item.notes}</div>
                  </li>
                ))}
              </ul>
            </section>
          ))}
        </div>
      </div>
    </PageContainer>
  );
}
