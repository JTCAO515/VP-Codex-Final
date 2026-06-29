import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { ShareView } from "@/components/share/ShareView";

const loadSharedTrip = vi.fn();

vi.mock("@/lib/supabase/tripsRepository", () => ({
  loadSharedTrip: (...args: unknown[]) => loadSharedTrip(...(args as [never])),
}));

describe("ShareView", () => {
  it("shows the shared trip title when the token resolves", async () => {
    loadSharedTrip.mockResolvedValueOnce({
      trip: {
        id: "trip-1",
        owner_id: "user-1",
        title: "Shared Beijing Trip",
        status: "shared",
        share_token: "token-123",
        current_canvas_version_id: null,
        created_at: "2026-01-01T00:00:00.000Z",
        updated_at: "2026-01-01T00:00:00.000Z",
      },
      canvas: null,
    });

    render(<ShareView shareToken="token-123" />);

    expect(await screen.findByRole("heading", { name: /shared beijing trip/i })).toBeInTheDocument();
    expect(screen.getByText(/read-only view/i)).toBeInTheDocument();
  });

  it("shows a not-available notice when the token does not resolve", async () => {
    loadSharedTrip.mockResolvedValueOnce(null);

    render(<ShareView shareToken="missing-token" />);

    expect(await screen.findByRole("heading", { name: /link not available/i })).toBeInTheDocument();
  });
});
