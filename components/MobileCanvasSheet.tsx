"use client";

import { TripCanvas } from "./TripCanvas";

export function MobileCanvasSheet({ open, onClose }: { open: boolean; onClose: () => void }) {
  if (!open) return null;

  return (
    <div className="fixed inset-0 z-20 bg-ink-cream">
      <div className="flex items-center justify-between border-b border-ink-umber/15 px-4 py-3">
        <p className="font-display text-xl text-ink-umber">Your itinerary</p>
        <button onClick={onClose} className="text-sm text-ink-cinnabar underline">
          Back to chat
        </button>
      </div>
      <div className="h-[calc(100%-56px)]">
        <TripCanvas onEditSummary={onClose} />
      </div>
    </div>
  );
}
