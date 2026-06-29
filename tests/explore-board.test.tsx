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

  it("navigates to chat with an add-to-trip draft message when clicking Add to Trip", async () => {
    render(<ExploreBoard />);

    const item = (await screen.findByText("Forbidden City")).closest("li");
    expect(item).not.toBeNull();
    fireEvent.click(within(item as HTMLElement).getByRole("button", { name: /add to trip/i }));

    expect(window.location.href).toBe(
      `/chat?add=${encodeURIComponent("Add Forbidden City in Beijing to my trip.")}`,
    );
  });
});
