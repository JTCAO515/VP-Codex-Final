import type { ChatMessage, TripState } from "@/lib/types/trip";
import type { SavedTripStatus } from "@/lib/trips/mockTrips";

export interface UserRow {
  id: string;
  email: string;
  display_name: string | null;
  created_at: string;
}

export interface TripRow {
  id: string;
  owner_id: string;
  title: string;
  status: SavedTripStatus;
  share_token: string | null;
  current_canvas_version_id: string | null;
  created_at: string;
  updated_at: string;
}

export interface CanvasVersionRow {
  id: string;
  trip_id: string;
  canvas: TripState;
  last_updated_reason: string;
  created_at: string;
}

export interface MessageRow {
  id: string;
  trip_id: string;
  role: ChatMessage["role"];
  content: string;
  created_at: string;
}
