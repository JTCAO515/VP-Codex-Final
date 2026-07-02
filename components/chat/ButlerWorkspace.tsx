"use client";

import type { Session } from "@supabase/supabase-js";
import { useEffect, useMemo, useRef, useState } from "react";
import { ChatPanel } from "@/components/chat/ChatPanel";
import { TripCanvas, type HighlightSignal } from "@/components/canvas/TripCanvas";
import { preferenceProfileSummary, updatePreferenceProfile, type UserPreferenceProfile } from "@/lib/ai/preferenceProfile";
import { applyCanvasPatch } from "@/lib/canvas/applyCanvasPatch";
import { getTripArchetype, TRIP_ARCHETYPES } from "@/lib/chat/archetypes";
import { diffTripState } from "@/lib/canvas/diffTripState";
import {
  applyExplorePoiToPatch,
  parseExploreAddToTripPayload,
  type ExploreAddToTripPayload,
} from "@/lib/explore/addToTrip";
import type { QuickActionKind } from "@/lib/canvas/quickActions";
import { createMockButlerPatch, initialTripState } from "@/lib/mock-ai/mockButler";
import { appendMessage, loadTripWithCanvas, saveTripCanvas } from "@/lib/supabase/tripsRepository";
import { useSupabaseSession } from "@/lib/supabase/useSupabaseSession";
import type { ButlerAlert, ChatMessage, TripState } from "@/lib/types/trip";

const GUEST_DRAFT_KEY = "visepanda:guest-draft";
const PREFERENCE_PROFILE_KEY = "visepanda:preference-profile";

interface GuestDraft {
  trip: TripState;
  messages: ChatMessage[];
  preferenceProfile?: UserPreferenceProfile;
}

const initialSuggestions = TRIP_ARCHETYPES.map((archetype) => archetype.title);

function createMessage(
  role: ChatMessage["role"],
  content: string,
  response?: ChatMessage["response"],
  changeDigest?: ChatMessage["changeDigest"],
): ChatMessage {
  return {
    id: `${role}-${Date.now()}-${Math.random().toString(16).slice(2)}`,
    role,
    content,
    response,
    changeDigest,
    createdAt: new Date().toISOString(),
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
  const [saveNote, setSaveNote] = useState("");
  const [preferenceProfile, setPreferenceProfile] = useState<UserPreferenceProfile | undefined>();
  const [busy, setBusy] = useState(false);
  const [saving, setSaving] = useState(false);
  const [tripId, setTripId] = useState<string | null>(null);
  const [highlightSignal, setHighlightSignal] = useState<HighlightSignal | null>(null);
  const [undoMessageId, setUndoMessageId] = useState<string | undefined>(undefined);
  const persistedMessageCount = useRef(0);
  const lastAutoSavedCount = useRef(0);
  const hasLoadedDraftRef = useRef(false);
  const hasAppliedAddRef = useRef(false);
  const hasAppliedArchetypeRef = useRef(false);
  const previousSessionRef = useRef<Session | null>(null);
  // Single-slot snapshot of the trip as it was immediately before the most
  // recent applied patch, so Undo can deterministically restore it. See
  // ADR-070 (DESIGN.md v0.2.7) for why undo is a local restore rather than an
  // AI-mediated round trip.
  const undoSnapshotRef = useRef<TripState | null>(null);

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
    const params = new URLSearchParams(window.location.search);
    if (params.get("trip") || params.get("add") || params.get("archetype")) return;

    const raw = window.localStorage.getItem(GUEST_DRAFT_KEY);
    if (!raw) return;
    try {
      const draft = JSON.parse(raw) as GuestDraft;
      setTrip(draft.trip);
      setMessages(draft.messages);
      setPreferenceProfile(draft.preferenceProfile);
      setStatus("Restored your guest draft trip.");
    } catch {
      window.localStorage.removeItem(GUEST_DRAFT_KEY);
    }
  }, []);

  useEffect(() => {
    if (typeof window === "undefined") return;
    const raw = window.localStorage.getItem(PREFERENCE_PROFILE_KEY);
    if (!raw) return;
    try {
      setPreferenceProfile(JSON.parse(raw) as UserPreferenceProfile);
    } catch {
      window.localStorage.removeItem(PREFERENCE_PROFILE_KEY);
    }
  }, []);

  useEffect(() => {
    if (typeof window === "undefined" || hasAppliedAddRef.current) return;
    const params = new URLSearchParams(window.location.search);
    const addParam = params.get("add");
    if (!addParam) return;
    const explorePoi = parseExploreAddToTripPayload(params.get("poi"));
    hasAppliedAddRef.current = true;
    window.history.replaceState(null, "", "/chat");
    void handleSend(addParam, { explorePoi });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (typeof window === "undefined" || hasAppliedArchetypeRef.current) return;
    const params = new URLSearchParams(window.location.search);
    if (params.get("trip") || params.get("add")) return;
    const archetype = getTripArchetype(params.get("archetype"));
    if (!archetype) return;
    hasAppliedArchetypeRef.current = true;
    window.history.replaceState(null, "", "/chat");
    setStatus(`Starting ${archetype.title} with VisePanda.`);
    void handleSend(archetype.prompt);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (typeof window === "undefined") return;
    if (session || tripId) {
      window.localStorage.removeItem(GUEST_DRAFT_KEY);
      return;
    }
    if (messages.length === 0) return;
    window.localStorage.setItem(GUEST_DRAFT_KEY, JSON.stringify({ trip, messages, preferenceProfile } satisfies GuestDraft));
  }, [trip, messages, preferenceProfile, session, tripId]);

  useEffect(() => {
    if (typeof window === "undefined") return;
    if (!preferenceProfile) return;
    window.localStorage.setItem(PREFERENCE_PROFILE_KEY, JSON.stringify(preferenceProfile));
  }, [preferenceProfile]);

  async function handleSaveToTrips(
    successMessage = "Saved this trip to your Trips library.",
    notify: (message: string) => void = setStatus,
  ) {
    if (!configured) {
      notify("Add Supabase project keys to enable saving trips.");
      return;
    }
    if (loading) return;
    if (!session) {
      notify("Sign in from Account to save this trip.");
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

      notify(successMessage);
    } catch (err) {
      console.error("[Save to Trips] failed:", err);
      notify("Saving failed. Please try again.");
    } finally {
      setSaving(false);
    }
  }

  useEffect(() => {
    const justSignedIn = !previousSessionRef.current && Boolean(session);
    previousSessionRef.current = session;
    if (!justSignedIn || loading || tripId || messages.length === 0) return;

    // Claim the current message count so the auto-save effect does not also fire
    // for the same messages on sign-in (avoids a duplicate save).
    lastAutoSavedCount.current = messages.length;
    void handleSaveToTrips("Synced your guest draft trip to your account.");
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [session]);

  // Auto-save every chat: whenever a new assistant reply lands and the user is
  // signed in, persist silently to Trips. No manual Save button (guests keep the
  // localStorage draft automatically). Runs only when Supabase is configured.
  useEffect(() => {
    if (typeof window === "undefined") return;
    if (!configured || loading || !session) return;
    if (busy || saving) return;
    if (messages.length === 0 || messages[messages.length - 1].role !== "assistant") return;
    if (messages.length === lastAutoSavedCount.current) return;
    lastAutoSavedCount.current = messages.length;
    void handleSaveToTrips("Saved to your Trips.", setSaveNote);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [messages, session, configured, loading, busy, saving]);

  function applyPatchAndDigest(patch: Parameters<typeof applyCanvasPatch>[1], response?: ChatMessage["response"]) {
    const previousTrip = trip;
    const nextTrip = applyCanvasPatch(previousTrip, patch);
    const digest = diffTripState(previousTrip, nextTrip);

    undoSnapshotRef.current = previousTrip;
    setTrip(nextTrip);
    const assistantMessage = createMessage("assistant", patch.assistantMessage, response, digest.length ? digest : undefined);
    setMessages((current) => [...current, assistantMessage]);
    if (digest.length > 0) setUndoMessageId(assistantMessage.id);
    return nextTrip;
  }

  async function handleSend(
    message: string,
    options?: {
      explorePoi?: ExploreAddToTripPayload | null;
    },
  ) {
    setBusy(true);
    const nextPreferenceProfile = updatePreferenceProfile(preferenceProfile, message);
    setPreferenceProfile(nextPreferenceProfile);
    const nextMessages = [...messages, createMessage("user", message)];
    setMessages(nextMessages);

    try {
      const response = await fetch("/api/chat", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ message, messages: nextMessages, trip, preferenceProfile: nextPreferenceProfile }),
      });
      const body = await response.json();
      const patch = applyExplorePoiToPatch(
        body?.patch ?? createMockButlerPatch(message, trip),
        trip,
        options?.explorePoi ?? null,
      );
      const modeNote =
        typeof body?.modelLabel === "string" && body.modelLabel
          ? body.modelLabel
          : body?.mode === "mock"
            ? "mock fallback"
            : (body?.mode ?? "mock fallback");

      applyPatchAndDigest(patch, patch.assistantResponse);
      setSuggestions(Array.isArray(body?.suggestions) ? body.suggestions.slice(0, 2) : initialSuggestions.slice(0, 2));
      setStatus(`VisePanda updated the canvas with ${modeNote}: ${patch.reason}`);
    } catch {
      const patch = applyExplorePoiToPatch(
        createMockButlerPatch(message, trip),
        trip,
        options?.explorePoi ?? null,
      );
      applyPatchAndDigest(patch);
      setSuggestions(["Can you make one day lighter?", "What should we book first?"]);
      setStatus(`VisePanda updated the canvas with mock fallback: ${patch.reason}`);
    } finally {
      setBusy(false);
    }
  }

  function handleQuickAction(message: string, _kind: QuickActionKind) {
    void handleSend(message);
  }

  function handleSelectDay(dayNumber: number) {
    setHighlightSignal((current) => ({ dayNumber, nonce: (current?.nonce ?? 0) + 1 }));
  }

  // Undo is a deterministic local restore, not an AI round trip: asking a
  // model to "undo the last change" without feeding it the authoritative
  // prior TripState as context cannot reliably reconstruct the exact prior
  // itinerary (models improvise a plausible-but-different result instead of
  // restoring it). A local restore is instant and always correct. See
  // ADR-070 (DESIGN.md v0.2.7).
  function handleUndo() {
    const previousTrip = undoSnapshotRef.current;
    if (!previousTrip) return;
    setTrip(previousTrip);
    setMessages((current) => [
      ...current,
      createMessage("assistant", "Reverted to the previous version of your itinerary."),
    ]);
    undoSnapshotRef.current = null;
    setUndoMessageId(undefined);
    setStatus("Reverted the last change.");
  }

  // Prep-checklist toggles are operational bookkeeping (did the traveler
  // finish this task?), not itinerary content, so they update TripState
  // directly instead of going through the AI pipeline. See AGENTS.md v0.2.7.
  function handleToggleAlertDone(alert: ButlerAlert) {
    setTrip((current) => ({
      ...current,
      alerts: current.alerts.map((existing) =>
        existing.type === alert.type && existing.title === alert.title
          ? { ...existing, done: !existing.done }
          : existing,
      ),
    }));
  }

  // Trip title is a label the traveler fully controls, not itinerary content
  // the AI plans — same "operational, not content" reasoning as alert.done.
  function handleRenameTrip(nextTitle: string) {
    setTrip((current) => ({ ...current, summary: { ...current.summary, title: nextTitle } }));
  }

  function handleAddDay() {
    const nextDayNumber = trip.days.length + 1;
    const lastCity = trip.days[trip.days.length - 1]?.city ?? trip.summary.destinations[0] ?? "the same city";
    void handleSend(`Add Day ${nextDayNumber} to this trip, continuing in or near ${lastCity}.`);
  }

  function handleRebalanceRoute() {
    void handleSend("Rebalance the route across all days to reduce backtracking and even out the pace.");
  }

  return (
    <section className="butler-workspace" aria-label="VisePanda AI Butler workspace">
      <div className="butler-workspace__canvas">
        <TripCanvas
          busy={busy}
          highlightSignal={highlightSignal}
          onAddDay={handleAddDay}
          onQuickAction={handleQuickAction}
          onRebalanceRoute={handleRebalanceRoute}
          onRenameTrip={handleRenameTrip}
          onToggleAlertDone={handleToggleAlertDone}
          trip={trip}
        />
      </div>
      <div className="butler-workspace__chat">
        <ChatPanel
          messages={messages}
          onSelectDay={handleSelectDay}
          onSend={handleSend}
          onUndo={handleUndo}
          busy={busy}
          suggestions={suggestions}
          profileChips={preferenceProfileSummary(preferenceProfile)}
          undoMessageId={undoMessageId}
        />
        <p className="workspace-status" role="status" aria-live="polite">
          {statusText}
          {saveNote ? <span className="workspace-autosave"> · {saveNote}</span> : null}
        </p>
      </div>
    </section>
  );
}
