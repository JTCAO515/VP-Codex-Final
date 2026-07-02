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

  it("has no manual Save to Trips button (chats auto-save)", () => {
    render(<ButlerWorkspace />);

    expect(screen.queryByRole("button", { name: /save to trips/i })).not.toBeInTheDocument();
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

  it("writes an Explore POI payload into the trip canvas when opening from Add to Trip", async () => {
    const params = new URLSearchParams({
      add: "Add Test Tea House in Beijing to my trip.",
      poi: JSON.stringify({
        id: "beijing-test-tea-house",
        name: "Test Tea House",
        cityId: "beijing",
        cityName: "Beijing",
        category: "attraction",
        phone: "010-12345678",
        openingHours: "09:00-18:00",
        mapUrl: "https://uri.amap.com/marker?position=116.4%2C39.9&name=Test+Tea+House",
        sourceLabel: "Amap",
        coordinates: { lat: 39.9, lng: 116.4 },
        bookingCandidates: [
          {
            id: "beijing-test-tea-house-ticket",
            kind: "ticket",
            label: "Ticket planning reference",
            provider: "Amap",
            status: "info-only",
            note: "Added from Explore for planning context only.",
          },
        ],
      }),
    });
    window.history.replaceState(null, "", `/chat?${params.toString()}`);

    render(<ButlerWorkspace />);

    expect(await screen.findByText("Add Test Tea House in Beijing to my trip.")).toBeInTheDocument();
    expect(await screen.findByText("Test Tea House")).toBeInTheDocument();
    expect(await screen.findAllByText(/Explore POI was added as a flexible candidate block/i)).not.toHaveLength(0);
  });

  it("auto-sends an archetype starter found in the ?archetype= URL param", async () => {
    window.history.replaceState(null, "", "/chat?archetype=foodie-china");

    render(<ButlerWorkspace />);

    expect(await screen.findByText(/Start a Foodie China independent trip/i)).toBeInTheDocument();
    expect(await screen.findByText(/VisePanda updated the canvas/i)).toBeInTheDocument();
    expect(window.location.search).toBe("");
  });
});
