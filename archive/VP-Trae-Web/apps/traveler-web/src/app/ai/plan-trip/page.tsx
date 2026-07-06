'use client';

import { useEffect, useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';

import { ActionGuardButton } from '@/components/ActionGuardButton';
import { PageContainer } from '@/components/PageContainer';
import { AUTH_COOKIE_ACCESS_TOKEN } from '@/lib/auth';
import { clearPendingAction, readPendingAction, type PendingTripDraft } from '@/lib/pending-action';

type ApiEnvelope<T> = {
  success: boolean;
  data: T;
  requestId: string;
  error?: {
    code: string;
    message: string;
  };
};

type AITripPlanPayload = {
  answer: string;
  taskType: 'trip_planning';
  model: string;
  provider: string;
  routePolicy: string;
  promptTemplateVersion: string;
  logId: string;
  fallbackUsed: boolean;
  attemptCount: number;
};

type TripDetail = {
  id: string;
};

function getCookie(name: string) {
  if (typeof document === 'undefined') return null;
  const prefix = `${name}=`;
  return (
    document.cookie
      .split('; ')
      .find((part) => part.startsWith(prefix))
      ?.slice(prefix.length) ?? null
  );
}

function toIsoDate(offsetDays: number) {
  const date = new Date();
  date.setDate(date.getDate() + offsetDays);
  return date.toISOString().slice(0, 10);
}

function buildTripDraft(input: {
  destination: string;
  days: number;
  interests: string[];
  aiResult: AITripPlanPayload;
}): PendingTripDraft {
  return {
    title: `${input.destination}${input.days}日行程`,
    destination: input.destination,
    startDate: toIsoDate(7),
    endDate: toIsoDate(7 + input.days - 1),
    aiSummary: input.aiResult.answer,
    favorite: true,
    source: {
      type: 'task',
      invocationLogId: input.aiResult.logId,
      taskType: 'trip_planning',
    },
    days: Array.from({ length: input.days }, (_, index) => ({
      dayNumber: index + 1,
      title: `第 ${index + 1} 天`,
      items: [
        {
          type: 'itinerary',
          title: `${input.destination}行程建议`,
          startTime: '09:00',
          endTime: '18:00',
          notes:
            index === 0
              ? `关注兴趣：${input.interests.join('、') || '城市漫游'}`
              : `参考 AI 规划结果安排第 ${index + 1} 天行程`,
        },
      ],
    })),
  };
}

function buildPreviewSections(answer: string, totalDays: number) {
  const normalizedLines = answer
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean);

  if (!normalizedLines.length) {
    return Array.from({ length: totalDays }, (_, index) => ({
      title: `第 ${index + 1} 天`,
      lines: ['等待 AI 返回更具体的行程描述。'],
    }));
  }

  return Array.from({ length: totalDays }, (_, index) => {
    const slice = normalizedLines.slice(index * 2, index * 2 + 2);

    return {
      title: `第 ${index + 1} 天`,
      lines: slice.length ? slice : [`参考整段 AI 结果安排第 ${index + 1} 天行程。`],
    };
  });
}

export default function PlanTripPage() {
  const router = useRouter();
  const [destination, setDestination] = useState('北京');
  const [days, setDays] = useState(3);
  const [interests, setInterests] = useState('历史, 美食');
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saveError, setSaveError] = useState<string | null>(null);
  const [result, setResult] = useState<AITripPlanPayload | null>(null);
  const [restoredPendingAction, setRestoredPendingAction] = useState(false);

  const isAuthenticated = Boolean(getCookie(AUTH_COOKIE_ACCESS_TOKEN));
  const parsedInterests = useMemo(
    () =>
      interests
        .split(',')
        .map((item) => item.trim())
        .filter(Boolean),
    [interests],
  );

  const tripDraft = useMemo(
    () =>
      result
        ? buildTripDraft({
            destination,
            days,
            interests: parsedInterests,
            aiResult: result,
          })
        : null,
    [days, destination, parsedInterests, result],
  );
  const previewSections = useMemo(
    () => (result ? buildPreviewSections(result.answer, days) : []),
    [days, result],
  );

  async function saveTrip(draft: PendingTripDraft) {
    setSaving(true);
    setSaveError(null);

    try {
      const response = await fetch('/api/trips', {
        method: 'POST',
        headers: { 'content-type': 'application/json' },
        body: JSON.stringify(draft),
      });
      const payload = (await response.json()) as ApiEnvelope<TripDetail>;

      if (!response.ok || !payload.success) {
        setSaveError(payload.error?.message ?? '保存失败');
        return;
      }

      clearPendingAction();
      router.push(`/trips/${payload.data.id}`);
      router.refresh();
    } catch (err) {
      setSaveError(err instanceof Error ? err.message : '保存失败');
    } finally {
      setSaving(false);
    }
  }

  useEffect(() => {
    if (!isAuthenticated || saving || restoredPendingAction) return;

    const pending = readPendingAction();
    if (!pending || pending.type !== 'save-trip') return;

    clearPendingAction();
    setRestoredPendingAction(true);
    void saveTrip(pending.tripDraft);
  }, [isAuthenticated, restoredPendingAction, saving]);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLoading(true);
    setError(null);
    setSaveError(null);

    try {
      const response = await fetch('/api/ai/plan-trip', {
        method: 'POST',
        headers: { 'content-type': 'application/json' },
        body: JSON.stringify({
          destination,
          days,
          interests: parsedInterests,
          locale: 'zh-CN',
        }),
      });

      const payload = (await response.json()) as ApiEnvelope<AITripPlanPayload>;

      if (!response.ok || !payload.success) {
        setError(payload.error?.message ?? 'AI 规划失败');
        return;
      }

      setResult(payload.data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'AI 规划失败');
    } finally {
      setLoading(false);
    }
  }

  return (
    <PageContainer title="AI 行程规划">
      <div className="grid gap-4 lg:grid-cols-[380px_minmax(0,1fr)]">
        <form className="space-y-4 rounded-xl border bg-white p-5 shadow-sm" onSubmit={handleSubmit}>
          <label className="block">
            <span className="text-sm text-zinc-700">目的地</span>
            <input
              className="mt-1 w-full rounded-md border px-3 py-2"
              value={destination}
              onChange={(event) => setDestination(event.target.value)}
            />
          </label>

          <label className="block">
            <span className="text-sm text-zinc-700">天数</span>
            <input
              className="mt-1 w-full rounded-md border px-3 py-2"
              max={14}
              min={1}
              onChange={(event) => setDays(Number(event.target.value))}
              type="number"
              value={days}
            />
          </label>

          <label className="block">
            <span className="text-sm text-zinc-700">兴趣</span>
            <input
              className="mt-1 w-full rounded-md border px-3 py-2"
              value={interests}
              onChange={(event) => setInterests(event.target.value)}
              placeholder="历史, 美食, Citywalk"
            />
          </label>

          {error ? <div className="rounded-md border border-red-200 bg-red-50 p-2 text-sm text-red-700">{error}</div> : null}

          <button
            className="w-full rounded-md bg-zinc-900 px-3 py-2 text-sm font-medium text-white disabled:opacity-50"
            disabled={loading}
            type="submit"
          >
            {loading ? '生成中…' : '生成行程'}
          </button>
        </form>

        <div className="space-y-4">
          <div className="rounded-xl border bg-white p-5 shadow-sm">
            <h2 className="text-base font-medium">规划结果</h2>
            {result ? (
              <div className="mt-4 space-y-4">
                <div className="flex flex-wrap gap-2 text-xs text-zinc-500">
                  <span className="rounded-full bg-zinc-100 px-3 py-1">模型：{result.model}</span>
                  <span className="rounded-full bg-zinc-100 px-3 py-1">服务商：{result.provider}</span>
                  <span className="rounded-full bg-zinc-100 px-3 py-1">策略：{result.routePolicy}</span>
                  <span className="rounded-full bg-zinc-100 px-3 py-1">尝试次数：{result.attemptCount}</span>
                </div>

                <div className="rounded-lg bg-zinc-50 p-4 text-sm leading-7 text-zinc-700">
                  <p className="whitespace-pre-wrap">{result.answer}</p>
                </div>
              </div>
            ) : (
              <p className="mt-2 whitespace-pre-wrap text-sm leading-7 text-zinc-700">
                提交表单后，这里会展示 AI 返回的行程建议。
              </p>
            )}
          </div>

          {result && tripDraft ? (
            <>
              <div className="rounded-xl border bg-white p-5 shadow-sm">
                <h2 className="text-base font-medium">行程预览</h2>
                <div className="mt-4 grid gap-4 md:grid-cols-2">
                  {previewSections.map((section) => (
                    <section key={section.title} className="rounded-lg border bg-zinc-50 p-4">
                      <h3 className="text-sm font-medium text-zinc-900">{section.title}</h3>
                      <ul className="mt-3 space-y-2 text-sm leading-6 text-zinc-700">
                        {section.lines.map((line) => (
                          <li key={`${section.title}-${line}`} className="rounded-md bg-white p-3">
                            {line}
                          </li>
                        ))}
                      </ul>
                    </section>
                  ))}
                </div>
              </div>

              <div className="rounded-xl border bg-white p-5 shadow-sm">
                <div className="flex flex-wrap items-center gap-3">
                  <ActionGuardButton
                    isAuthenticated={isAuthenticated}
                    nextPath="/ai/plan-trip"
                    onAuthorized={() => void saveTrip(tripDraft)}
                    tripDraft={tripDraft}
                  />
                  <span className="text-xs text-zinc-500">
                    {isAuthenticated ? '已登录，点击后会直接保存。' : '未登录时会先跳到登录页，登录后自动继续保存。'}
                  </span>
                </div>
                {saveError ? (
                  <div className="mt-3 rounded-md border border-red-200 bg-red-50 p-2 text-sm text-red-700">{saveError}</div>
                ) : null}
              </div>
            </>
          ) : null}
        </div>
      </div>
    </PageContainer>
  );
}
