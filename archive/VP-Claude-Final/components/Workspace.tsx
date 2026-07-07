"use client";

import { useRef, useState } from "react";
import { useIsMobile } from "@/hooks/useIsMobile";
import { ButlerRails } from "./ButlerRails";
import { TripCanvas } from "./TripCanvas";
import { ChatPanel } from "./ChatPanel";
import { MobileCanvasSheet } from "./MobileCanvasSheet";

export function Workspace() {
  const isMobile = useIsMobile();
  const [canvasOpen, setCanvasOpen] = useState(false);
  const chatInputRef = useRef<HTMLInputElement>(null);

  function focusChatInput() {
    chatInputRef.current?.focus();
  }

  return (
    <div className="flex h-full flex-col">
      <ButlerRails />
      {isMobile ? (
        <div className="relative flex-1">
          <ChatPanel ref={chatInputRef} />
          <button
            onClick={() => setCanvasOpen(true)}
            className="absolute bottom-4 right-4 rounded-full bg-ink-cinnabar px-4 py-2 text-sm font-semibold text-ink-paper shadow-lg"
          >
            View itinerary
          </button>
          <MobileCanvasSheet open={canvasOpen} onClose={() => setCanvasOpen(false)} />
        </div>
      ) : (
        <div className="flex flex-1">
          <div className="flex-[1.4] border-r border-ink-umber/15">
            <TripCanvas onEditSummary={focusChatInput} />
          </div>
          <div className="flex-1">
            <ChatPanel ref={chatInputRef} />
          </div>
        </div>
      )}
    </div>
  );
}
