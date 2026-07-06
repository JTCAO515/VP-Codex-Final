export type PendingTripDraft = {
  title: string;
  destination: string;
  startDate: string;
  endDate: string;
  aiSummary: string;
  favorite: boolean;
  source: {
    type: 'task';
    invocationLogId: string;
    taskType: 'trip_planning';
  };
  days: Array<{
    dayNumber: number;
    title: string;
    items: Array<{
      type: string;
      title: string;
      startTime: string;
      endTime: string;
      notes: string;
    }>;
  }>;
};

export type PendingAction = {
  type: 'save-trip';
  redirectTo: '/ai/plan-trip';
  tripDraft: PendingTripDraft;
};

const PENDING_ACTION_KEY = 'vp_pending_action';

export function writePendingAction(action: PendingAction) {
  if (typeof window === 'undefined') return;
  window.sessionStorage.setItem(PENDING_ACTION_KEY, JSON.stringify(action));
}

export function readPendingAction(): PendingAction | null {
  if (typeof window === 'undefined') return null;
  const raw = window.sessionStorage.getItem(PENDING_ACTION_KEY);
  if (!raw) return null;

  try {
    return JSON.parse(raw) as PendingAction;
  } catch {
    return null;
  }
}

export function clearPendingAction() {
  if (typeof window === 'undefined') return;
  window.sessionStorage.removeItem(PENDING_ACTION_KEY);
}
