"use client";

import { useMemo, useState } from "react";
import { ChatPanel } from "@/components/chat/ChatPanel";
import { TripCanvas } from "@/components/canvas/TripCanvas";
import { applyCanvasPatch } from "@/lib/canvas/applyCanvasPatch";
import { createMockButlerPatch, initialTripState } from "@/lib/mock-ai/mockButler";
import type { ChatMessage, TripState } from "@/lib/types/trip";

const initialSuggestions = [
  "Plan my first China trip",
  "Make this trip less tiring",
  "Add food-focused stops",
  "Keep hotels convenient",
];

function createMessage(role: ChatMessage["role"], content: string): ChatMessage {
  return {
    id: `${role}-${Date.now()}-${Math.random().toString(16).slice(2)}`,
    role,
    content,
  };
}

export function ButlerWorkspace() {
  const [trip, setTrip] = useState<TripState>(initialTripState);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [suggestions, setSuggestions] = useState(initialSuggestions);
  const [status, setStatus] = useState("Canvas ready for your first request.");
  const [busy, setBusy] = useState(false);

  const statusText = useMemo(() => status, [status]);

  async function handleSend(message: string) {
    setBusy(true);
    const nextMessages = [...messages, createMessage("user", message)];
    setMessages(nextMessages);

    try {
      const response = await fetch("/api/chat", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ message, messages: nextMessages, trip }),
      });
      const body = await response.json();
      const patch = body?.patch ?? createMockButlerPatch(message, trip);
      const nextTrip = applyCanvasPatch(trip, patch);
      const modeNote = body?.mode === "deepseek" ? "DeepSeek V4 Flash" : "mock fallback";

      setTrip(nextTrip);
      setMessages((current) => [...current, createMessage("assistant", patch.assistantMessage)]);
      setSuggestions(Array.isArray(body?.suggestions) ? body.suggestions.slice(0, 2) : initialSuggestions.slice(0, 2));
      setStatus(`VisePanda updated the canvas with ${modeNote}: ${patch.reason}`);
    } catch {
      const patch = createMockButlerPatch(message, trip);
      const nextTrip = applyCanvasPatch(trip, patch);

      setTrip(nextTrip);
      setMessages((current) => [...current, createMessage("assistant", patch.assistantMessage)]);
      setSuggestions(["Can you make one day lighter?", "What should we book first?"]);
      setStatus(`VisePanda updated the canvas with mock fallback: ${patch.reason}`);
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="butler-workspace" aria-label="VisePanda AI Butler workspace">
      <div className="butler-workspace__canvas">
        <TripCanvas trip={trip} />
      </div>
      <div className="butler-workspace__chat">
        <ChatPanel messages={messages} onSend={handleSend} busy={busy} suggestions={suggestions} />
        <p className="workspace-status" role="status" aria-live="polite">
          {statusText}
        </p>
      </div>
    </section>
  );
}
