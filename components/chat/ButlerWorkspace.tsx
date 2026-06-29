"use client";

import { useMemo, useState } from "react";
import { ChatPanel } from "@/components/chat/ChatPanel";
import { TripCanvas } from "@/components/canvas/TripCanvas";
import { applyCanvasPatch } from "@/lib/canvas/applyCanvasPatch";
import { createMockButlerPatch, initialTripState } from "@/lib/mock-ai/mockButler";
import type { ChatMessage, TripState } from "@/lib/types/trip";

const welcomeMessage: ChatMessage = {
  id: "assistant-welcome",
  role: "assistant",
  content:
    "Tell me where you are starting, how many days you have, and what you care about. I will keep the trip canvas updated as we talk.",
};

function createMessage(role: ChatMessage["role"], content: string): ChatMessage {
  return {
    id: `${role}-${Date.now()}-${Math.random().toString(16).slice(2)}`,
    role,
    content,
  };
}

export function ButlerWorkspace() {
  const [trip, setTrip] = useState<TripState>(initialTripState);
  const [messages, setMessages] = useState<ChatMessage[]>([welcomeMessage]);
  const [status, setStatus] = useState("Canvas ready for your first request.");

  const statusText = useMemo(() => status, [status]);

  function handleSend(message: string) {
    const patch = createMockButlerPatch(message, trip);
    const nextTrip = applyCanvasPatch(trip, patch);

    setTrip(nextTrip);
    setMessages((current) => [
      ...current,
      createMessage("user", message),
      createMessage("assistant", patch.assistantMessage),
    ]);
    setStatus(`VisePanda updated the canvas: ${patch.reason}`);
  }

  return (
    <section className="butler-workspace" aria-label="VisePanda AI Butler workspace">
      <div className="butler-workspace__canvas">
        <TripCanvas trip={trip} />
      </div>
      <div className="butler-workspace__chat">
        <ChatPanel messages={messages} onSend={handleSend} />
        <p className="workspace-status" role="status" aria-live="polite">
          {statusText}
        </p>
      </div>
    </section>
  );
}
