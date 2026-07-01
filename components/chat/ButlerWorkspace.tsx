"use client";

import type { Session } from "@supabase/supabase-js";
import { useEffect, useMemo, useRef, useState } from "react";
import { ChatPanel } from "@/components/chat/ChatPanel";
import { TripCanvas } from "@/components/canvas/TripCanvas";
import { applyCanvasPatch } from "@/lib/canvas/applyCanvasPatch";
import { createMockButlerPatch, initialTripState } from "@/lib/mock-ai/mockButler";
import { appendMessage, loadTripWithCanvas, saveTripCanvas } from "@/lib/supabase/tripsRepository";
import { useSupabaseSession } from "@/lib/supabase/useSupabaseSession";
import type { ChatMessage, TripState } from "@/lib/types/trip";

const GUEST_DRAFT_KEY = "visepanda:guest-draft";

interface GuestDraft {
  trip: TripState;
  messages: ChatMessage[];
}

const initialSuggestions = [
  "Plan my first China trip",
  "Make this trip less tiring",
  "Add food-focused stops",
  "Keep hotels convenient",
];

function createMessage(role: ChatMessage["role"], content: string, response?: ChatMessage["response"]): ChatMessage {
  return {
    id: `${role}-${Date.now()}-${Math.random().toString(16).slice(2)}`,
    role,
    content,
    response,
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
  const hasLoadedDraftRef = useRef(false);
  const hasAppliedAddRef = useRef(false);
  const previousSessionRef = useRef<Session | null>(null);

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

  useEffect(() => {
    if (typeof window === "undefined" || hasLoadedDraftRef.current) return;
    hasLoadedDraftRef.current = true;
    if (new URLSearchParams(window.location.search).get("trip")) return;

    const raw = window.localStorage.getItem(GUEST_DRAFT_KEY);
    if (!raw) return;
    try {
      const draft = JSON.parse(raw) as GuestDraft;
      setTrip(draft.trip);
      setMessages(draft.messages);
      setStatus("Restored your guest draft trip.");
    } catch {
      window.localStorage.removeItem(GUEST_DRAFT_KEY);
    }
  }, []);

  useEffect(() => {
    if (typeof window === "undefined" || hasAppliedAddRef.current) return;
    const addParam = new URLSearchParams(window.location.search).get("add");
    if (!addParam) return;
    hasAppliedAddRef.current = true;
    window.history.replaceState(null, "", "/chat");
    void handleSend(addParam);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (typeof window === "undefined") return;
    if (session || tripId) {
      window.localStorage.removeItem(GUEST_DRAFT_KEY);
      return;
    }
    if (messages.length === 0) return;
    window.localStorage.setItem(GUEST_DRAFT_KEY, JSON.stringify({ trip, messages } satisfies GuestDraft));
  }, [trip, messages, session, tripId]);

  async function handleSaveToTrips(successMessage = "Saved this trip to your Trips library.") {
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
        ownerEmail: session.user.email ?? undefined,
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

      setStatus(successMessage);
    } catch (err) {
      console.error("[Save to Trips] failed:", err);
      setStatus("Saving failed. Please try again.");
    } finally {
      setSaving(false);
    }
  }

  useEffect(() => {
    const justSignedIn = !previousSessionRef.current && Boolean(session);
    previousSessionRef.current = session;
    if (!justSignedIn || loading || tripId || messages.length === 0) return;

    void handleSaveToTrips("Synced your guest draft trip to your account.");
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [session]);

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
      const modeNote =
        typeof body?.modelLabel === "string" && body.modelLabel
          ? body.modelLabel
          : body?.mode === "mock"
            ? "mock fallback"
            : (body?.mode ?? "mock fallback");

      setTrip(nextTrip);
      setMessages((current) => [...current, createMessage("assistant", patch.assistantMessage, patch.assistantResponse)]);
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
          <button type="button" onClick={() => handleSaveToTrips()} disabled={saving || busy}>
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
