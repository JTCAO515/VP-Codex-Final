import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { PlaceholderPage } from "@/components/placeholders/PlaceholderPage";

describe("PlaceholderPage", () => {
  it("renders a polished placeholder with a return action", () => {
    render(
      <PlaceholderPage
        eyebrow="Explore"
        title="Explore is coming next."
        description="Cities, attractions, dining, hotels, and local experiences will connect here."
        items={["Cities", "Attractions", "Food", "Hotels", "Local experiences"]}
      />,
    );

    expect(screen.getByText("Explore is coming next.")).toBeInTheDocument();
    expect(screen.getByText("Local experiences")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /return to chat/i })).toHaveAttribute("href", "/chat");
  });
});
