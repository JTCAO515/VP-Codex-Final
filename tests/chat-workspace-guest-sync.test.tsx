import { fireEvent, render, screen } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { ButlerWorkspace } from "@/components/chat/ButlerWorkspace";

const sessionState: { configured: boolean; loading: boolean; session: { user: { id: string; email: string } } | null } = {
  configured: true,
  loading: false,
  session: null,
};

vi.mock("@/lib/supabase/useSupabaseSession", () => ({
  useSupabaseSession: () => sessionState,
}));

const saveTripCanvas = vi.fn(async () => ({ tripId: "trip-1", canvasVersionId: "version-1" }));
const appendMessage = vi.fn(async () => ({}));
const loadTripWithCanvas = vi.fn(async () => null);

vi.mock("@/lib/supabase/tripsRepository", () => ({
  saveTripCanvas: (...args: unknown[]) => saveTripCanvas(...(args as [never])),
  appendMessage: (...args: unknown[]) => appendMessage(...(args as [never])),
  loadTripWithCanvas: (...args: unknown[]) => loadTripWithCanvas(...(args as [never])),
}));

describe("ButlerWorkspace guest draft sync", () => {
  beforeEach(() => {
    window.localStorage.clear();
    sessionState.session = null;
    saveTripCanvas.mockClear();
  });

  it("auto-saves the guest draft once the user signs in", async () => {
    const { rerender } = render(<ButlerWorkspace />);

    fireEvent.change(screen.getByLabelText(/ask visepanda/i), {
      target: { value: "I am visiting China for the first time for 5 days" },
    });
    fireEvent.click(screen.getByRole("button", { name: /send/i }));
    await screen.findByText(/VisePanda updated the canvas/i);

    expect(saveTripCanvas).not.toHaveBeenCalled();

    sessionState.session = { user: { id: "user-1", email: "guest@example.com" } };
    rerender(<ButlerWorkspace />);

    await screen.findByText(/Synced your guest draft trip to your account\./i);
    expect(saveTripCanvas).toHaveBeenCalledTimes(1);
  });
});
