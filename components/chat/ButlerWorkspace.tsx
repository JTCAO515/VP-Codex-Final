"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { ChatPanel } from "@/components/chat/ChatPanel";
import { TripCanvas } from "@/components/canvas/TripCanvas";
import { applyCanvasPatch } from "@/lib/canvas/applyCanvasPatch";
import { createMockButlerPatch, initialTripState } from "@/lib/mock-ai/mockButler";
import { appendMessage, loadTripWithCanvas, saveTripCanvas } from "@/lib/supabase/tripsRepository";
import { useSupabaseSession } from "@/lib/supabase/useSupabaseSession";
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

function confidenceToTripStatus(confidence: TripState["summary"]["confidence"]): "draft" | "ready" | "shared" {
  if (confidence === "Ready to save") return "ready";
  return "draft";
}

export function ButlerWorkspace() {
  const { configured, loading, session } = useSupabaseSession();

  const [trip, setTrip] = useState<TripState>(initialTripState);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [suggestions, setSuggestions] = useState(initialSuggestions);
  const [status, setStatus] = useState("Canvas ready for your first request.");
  const [busy, setBusy] = useState(false);
  const [saving, setSaving] = useState(false);
  const [tripId, setTripId] = useState<string | null>(null);
  const persistedMessageCount = useRef(0);

  const statusText = useMemo(() => status, [status]);

  useEffect(() => {
    if (typeof window === "undefined" || !configured) return;
    const tripParam = new URLSearchParams(window.location.search).get("trip");
    if (!tripParam) return;

    loadTripWithCanvas(tripParam)
      .then((remote) => {
        if (remote?.canvas) {
          setTrip(remote.canvas);
          setTripId(tripParam);
          setStatus("Loaded your saved trip from Trips.");
        }
      })
      .catch(() => {
        setStatus("Could not load that saved trip. Starting from a fresh canvas.");
      });
  }, [configured]);

  async function handleSaveToTrips() {
    if (!configured) {
      setStatus("Add Supabase project keys to enable saving trips.");
      return;
    }
    if (loading) return;
    if (!session) {
      setStatus("Sign in from Account to save this trip.");
      return;
    }

    setSaving(true);
    try {
      const result = await saveTripCanvas({
        tripId: tripId ?? undefined,
        ownerId: session.user.id,
        title: trip.summary.title,
        status: confidenceToTripStatus(trip.summary.confidence),
        trip,
      });

      setTripId(result.tripId);
      if (typeof window !== "undefined") {
        window.history.replaceState(null, "", `/chat?trip=${result.tripId}`);
      }

      const newMessages = messages.slice(persistedMessageCount.current);
      for (const message of newMessages) {
        await appendMessage(result.tripId, message);
      }
      persistedMessageCount.current = messages.length;

      setStatus("Saved this trip to your Trips library.");
    } catch {
      setStatus("Saving failed. Please try again.");
    } finally {
      setSaving(false);
    }
  }

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
        <div className="workspace-save-row">
          <button type="button" onClick={handleSaveToTrips} disabled={saving || busy}>
            {saving ? "Saving..." : "Save to Trips"}
          </button>
        </div>
        <p className="workspace-status" role="status" aria-live="polite">
          {statusText}
        </p>
      </div>
    </section>
  );
}
