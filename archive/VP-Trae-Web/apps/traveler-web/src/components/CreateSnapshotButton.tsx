'use client';

import { useRouter } from 'next/navigation';
import { useState } from 'react';

type ApiEnvelope<T> = {
  success: boolean;
  data: T;
  requestId: string;
  error?: {
    code: string;
    message: string;
  };
};

type TripSnapshotSummary = {
  id: string;
  tripId: string;
  version: number;
  reason: string | null;
  createdAt: string;
};

export function CreateSnapshotButton(props: { tripId: string }) {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  async function handleCreateSnapshot() {
    setLoading(true);
    setError(null);
    setSuccessMessage(null);

    try {
      const response = await fetch(`/api/trips/${props.tripId}/snapshot`, {
        method: 'POST',
        headers: { 'content-type': 'application/json' },
        body: JSON.stringify({ reason: '网页端手动创建快照' }),
      });

      const payload = (await response.json()) as ApiEnvelope<TripSnapshotSummary>;

      if (!response.ok || !payload.success) {
        setError(payload.error?.message ?? '创建快照失败');
        return;
      }

      setSuccessMessage(`已创建 v${payload.data.version} 快照`);
      router.refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : '创建快照失败');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="space-y-3">
      <button
        className="rounded-lg border px-4 py-2 text-sm text-zinc-700 disabled:cursor-not-allowed disabled:opacity-50"
        disabled={loading}
        onClick={() => void handleCreateSnapshot()}
        type="button"
      >
        {loading ? '创建中…' : '生成快照'}
      </button>

      {successMessage ? <div className="rounded-md border border-emerald-200 bg-emerald-50 p-2 text-sm text-emerald-700">{successMessage}</div> : null}
      {error ? <div className="rounded-md border border-red-200 bg-red-50 p-2 text-sm text-red-700">{error}</div> : null}
    </div>
  );
}
