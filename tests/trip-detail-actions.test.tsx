import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { TripDetail } from "@/components/trips/TripDetail";
import { initialTripState } from "@/lib/mock-ai/mockButler";

const stableSession = { user: { id: "user-1", email: "owner@example.com" } };

vi.mock("@/lib/supabase/useSupabaseSession", () => ({
  useSupabaseSession: () => ({
    configured: true,
    loading: false,
    session: stableSession,
  }),
}));

const remoteTrip = {
  id: "trip-1",
  owner_id: "user-1",
  title: "Beijing Loop",
  status: "ready" as const,
  share_token: null as string | null,
  current_canvas_version_id: null,
  created_at: "2026-01-01T00:00:00.000Z",
  updated_at: "2026-01-01T00:00:00.000Z",
};

const loadTripWithCanvas = vi.fn(async () => ({ trip: remoteTrip, canvas: null }));
const updateTripStatus = vi.fn(async () => undefined);
const createShareLink = vi.fn(async () => "share-token-123");
const revokeShareLink = vi.fn(async () => undefined);

vi.mock("@/lib/supabase/tripsRepository", () => ({
  loadTripWithCanvas: (...args: unknown[]) => loadTripWithCanvas(...(args as [never])),
  updateTripStatus: (...args: unknown[]) => updateTripStatus(...(args as [never])),
  createShareLink: (...args: unknown[]) => createShareLink(...(args as [never])),
  revokeShareLink: (...args: unknown[]) => revokeShareLink(...(args as [never])),
}));

describe("TripDetail archive and share actions", () => {
  it("keeps trip actions compact inside the live canvas summary when a saved canvas exists", async () => {
    loadTripWithCanvas.mockResolvedValueOnce({ trip: remoteTrip, canvas: initialTripState });

    render(<TripDetail tripId="trip-1" />);

    await screen.findByRole("heading", { name: /beijing loop/i });

    const compactActions = screen.getByLabelText(/trip actions and status/i);
    expect(compactActions).toHaveTextContent(/continue/i);
    expect(compactActions).toHaveTextContent(/trips/i);
    expect(compactActions).toHaveTextContent(/ready/i);
    expect(compactActions).toHaveTextContent(/archive/i);
    expect(compactActions).toHaveTextContent(/share/i);
    expect(screen.queryByLabelText(/current trip status/i)).not.toBeInTheDocument();
  });

  it("lets a signed-in owner create and revoke a share link", async () => {
    render(<TripDetail tripId="trip-1" />);

    await screen.findByRole("heading", { name: /beijing loop/i });

    fireEvent.click(screen.getByRole("button", { name: /get share link/i }));
    await screen.findByText(/share link created\./i);
    expect(createShareLink).toHaveBeenCalledWith("trip-1");
    expect(screen.getByText(/share-token-123/)).toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: /revoke share link/i }));
    await screen.findByText(/share link revoked\./i);
    expect(revokeShareLink).toHaveBeenCalledWith("trip-1");
  });

  it("lets a signed-in owner archive a trip", async () => {
    render(<TripDetail tripId="trip-1" />);

    await screen.findByRole("heading", { name: /beijing loop/i });

    fireEvent.click(screen.getByRole("button", { name: /^archive$/i }));
    await screen.findByText(/trip archived\./i);
    expect(updateTripStatus).toHaveBeenCalledWith("trip-1", "archived");
    expect(screen.getByRole("button", { name: /restore from archive/i })).toBeInTheDocument();
  });
});
