'use client';

import { useRouter } from 'next/navigation';

import type { PendingTripDraft } from '@/lib/pending-action';
import { writePendingAction } from '@/lib/pending-action';

export function ActionGuardButton(props: {
  isAuthenticated: boolean;
  nextPath: '/ai/plan-trip';
  tripDraft: PendingTripDraft;
  onAuthorized: () => void;
}) {
  const router = useRouter();

  return (
    <button
      className="rounded-lg bg-zinc-900 px-4 py-2 text-sm text-white disabled:cursor-not-allowed disabled:opacity-50"
      onClick={() => {
        if (props.isAuthenticated) {
          props.onAuthorized();
          return;
        }

        writePendingAction({
          type: 'save-trip',
          redirectTo: props.nextPath,
          tripDraft: props.tripDraft,
        });
        router.push(`/login?next=${encodeURIComponent(props.nextPath)}`);
      }}
      type="button"
    >
      保存为 Trip
    </button>
  );
}
