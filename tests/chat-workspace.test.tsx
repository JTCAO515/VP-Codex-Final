import { fireEvent, render, screen } from "@testing-library/react";
import { beforeEach, describe, expect, it } from "vitest";
import { ButlerWorkspace } from "@/components/chat/ButlerWorkspace";

describe("ButlerWorkspace", () => {
  beforeEach(() => {
    window.localStorage.clear();
    window.history.replaceState(null, "", "/chat");
  });

  it("starts without demo conversation and shows three first-run suggestions", () => {
    render(<ButlerWorkspace />);

    expect(screen.queryByText(/We're interested in history, culture, and good food/i)).not.toBeInTheDocument();
    expect(screen.getAllByRole("button").filter((button) => button.closest(".prompt-row"))).toHaveLength(3);
    expect(screen.getByRole("button", { name: /first china 10 days essentials/i })).toBeInTheDocument();
  });

  it("updates the canvas after a user asks for a first China trip", async () => {
    render(<ButlerWorkspace />);

    fireEvent.change(screen.getByLabelText(/ask visepanda/i), {
      target: { value: "I am visiting China for the first time for 5 days" },
    });
    fireEvent.click(screen.getByRole("button", { name: /send/i }));

    expect(await screen.findAllByText(/Beijing/i)).not.toHaveLength(0);
    expect(await screen.findAllByText(/Shanghai/i)).not.toHaveLength(0);
    expect(await screen.findByText(/VisePanda updated the canvas/i)).toBeInTheDocument();
  });

  it("prompts to configure Supabase when saving without project keys", async () => {
    render(<ButlerWorkspace />);

    fireEvent.click(screen.getByRole("button", { name: /save to trips/i }));

    expect(await screen.findByText(/Add Supabase project keys to enable saving trips\./i)).toBeInTheDocument();
  });

  it("persists a guest draft to localStorage and restores it after remount", async () => {
    const { unmount } = render(<ButlerWorkspace />);

    fireEvent.change(screen.getByLabelText(/ask visepanda/i), {
      target: { value: "I am visiting China for the first time for 5 days" },
    });
    fireEvent.click(screen.getByRole("button", { name: /send/i }));

    await screen.findByText(/VisePanda updated the canvas/i);
    expect(window.localStorage.getItem("visepanda:guest-draft")).toBeTruthy();

    unmount();
    render(<ButlerWorkspace />);

    expect(await screen.findByText(/Restored your guest draft trip/i)).toBeInTheDocument();
  });

  it("auto-sends an Explore add-to-trip draft message found in the ?add= URL param", async () => {
    window.history.replaceState(null, "", "/chat?add=" + encodeURIComponent("Add Forbidden City in Beijing to my trip."));

    render(<ButlerWorkspace />);

    expect(await screen.findByText("Add Forbidden City in Beijing to my trip.")).toBeInTheDocument();
    expect(await screen.findByText(/VisePanda updated the canvas/i)).toBeInTheDocument();
    expect(window.location.search).toBe("");
  });

  it("auto-sends an archetype starter found in the ?archetype= URL param", async () => {
    window.history.replaceState(null, "", "/chat?archetype=foodie-china");

    render(<ButlerWorkspace />);

    expect(await screen.findByText(/Start a Foodie China independent trip/i)).toBeInTheDocument();
    expect(await screen.findByText(/VisePanda updated the canvas/i)).toBeInTheDocument();
    expect(window.location.search).toBe("");
  });
});
