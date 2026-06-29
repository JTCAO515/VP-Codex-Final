import { getSupabaseBrowserClient } from "@/lib/supabase/client";
import type { CanvasVersionRow, MessageRow, TripRow } from "@/lib/supabase/schema";
import type { ChatMessage, TripState } from "@/lib/types/trip";

export interface SaveTripCanvasInput {
  tripId?: string;
  ownerId: string;
  title: string;
  status: TripRow["status"];
  trip: TripState;
}

export interface SaveTripCanvasResult {
  tripId: string;
  canvasVersionId: string;
}

export interface RemoteTrip {
  trip: TripRow;
  canvas: TripState | null;
}

export async function saveTripCanvas(input: SaveTripCanvasInput): Promise<SaveTripCanvasResult> {
  const client = getSupabaseBrowserClient();
  if (!client) throw new Error("Supabase is not configured.");

  let tripId = input.tripId;

  if (tripId) {
    const { error } = await client
      .from("trips")
      .update({ title: input.title, status: input.status, updated_at: new Date().toISOString() })
      .eq("id", tripId);
    if (error) throw error;
  } else {
    const { data, error } = await client
      .from("trips")
      .insert({ owner_id: input.ownerId, title: input.title, status: input.status })
      .select("id")
      .single();
    if (error) throw error;
    tripId = data.id as string;
  }

  const { data: versionData, error: versionError } = await client
    .from("canvas_versions")
    .insert({
      trip_id: tripId,
      canvas: input.trip,
      last_updated_reason: input.trip.lastUpdatedReason,
    })
    .select("id")
    .single();
  if (versionError) throw versionError;

  const canvasVersionId = versionData.id as string;

  const { error: linkError } = await client
    .from("trips")
    .update({ current_canvas_version_id: canvasVersionId })
    .eq("id", tripId);
  if (linkError) throw linkError;

  return { tripId, canvasVersionId };
}

export async function listTripsForOwner(ownerId: string): Promise<TripRow[]> {
  const client = getSupabaseBrowserClient();
  if (!client) return [];

  const { data, error } = await client
    .from("trips")
    .select("*")
    .eq("owner_id", ownerId)
    .order("updated_at", { ascending: false });
  if (error) throw error;
  return (data ?? []) as TripRow[];
}

export async function loadTripWithCanvas(tripId: string): Promise<RemoteTrip | null> {
  const client = getSupabaseBrowserClient();
  if (!client) return null;

  const { data: trip, error: tripError } = await client.from("trips").select("*").eq("id", tripId).single();
  if (tripError) throw tripError;

  if (!trip.current_canvas_version_id) return { trip: trip as TripRow, canvas: null };

  const { data: version, error: versionError } = await client
    .from("canvas_versions")
    .select("*")
    .eq("id", trip.current_canvas_version_id)
    .single();
  if (versionError) throw versionError;

  return { trip: trip as TripRow, canvas: (version as CanvasVersionRow).canvas };
}

export async function appendMessage(tripId: string, message: ChatMessage): Promise<MessageRow> {
  const client = getSupabaseBrowserClient();
  if (!client) throw new Error("Supabase is not configured.");

  const { data, error } = await client
    .from("messages")
    .insert({ trip_id: tripId, role: message.role, content: message.content })
    .select("*")
    .single();
  if (error) throw error;
  return data as MessageRow;
}
