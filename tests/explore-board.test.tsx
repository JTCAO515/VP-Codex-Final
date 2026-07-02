import { fireEvent, render, screen, within } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { ExploreBoard } from "@/components/explore/ExploreBoard";

describe("ExploreBoard", () => {
  const originalLocation = window.location;

  beforeEach(() => {
    Object.defineProperty(window, "location", {
      configurable: true,
      value: { ...originalLocation, href: "" },
    });
  });

  afterEach(() => {
    Object.defineProperty(window, "location", {
      configurable: true,
      value: originalLocation,
    });
  });

  it("shows attractions, food, and stays for the default city and switches on city change", async () => {
    render(<ExploreBoard />);

    expect(await screen.findByRole("heading", { name: /beijing/i, level: 2 })).toBeInTheDocument();
    expect(await screen.findByText("Forbidden City")).toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: /^chengdu$/i }));

    expect(await screen.findByRole("heading", { name: /chengdu/i, level: 2 })).toBeInTheDocument();
    expect(await screen.findByText("Chengdu Research Base of Giant Panda Breeding")).toBeInTheDocument();
  });

  it("navigates to chat with an add-to-trip draft message and POI payload", async () => {
    render(<ExploreBoard />);

    const item = (await screen.findByText("Forbidden City")).closest("li");
    expect(item).not.toBeNull();
    fireEvent.click(within(item as HTMLElement).getByRole("button", { name: /add to trip/i }));

    const url = new URL(window.location.href, "http://localhost");
    const poi = JSON.parse(url.searchParams.get("poi") ?? "{}");

    expect(url.pathname).toBe("/chat");
    expect(url.searchParams.get("add")).toBe(
      "Add Forbidden City in Beijing to my trip and ask VisePanda to rebalance the route around it.",
    );
    expect(poi).toMatchObject({
      id: "beijing-forbidden-city",
      name: "Forbidden City",
      category: "attraction",
      cityName: "Beijing",
    });
    expect(poi.mapUrl).toContain("ditu.amap.com/search");
    expect(poi.bookingCandidates[0]).toMatchObject({
      kind: "ticket",
      status: "info-only",
    });
  });

  it("explains that Add to Trip will be re-planned by the AI butler", async () => {
    render(<ExploreBoard />);

    expect(await screen.findByText(/VisePanda will reopen Chat and rebalance the route/i)).toBeInTheDocument();
  });
});
