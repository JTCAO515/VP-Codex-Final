import { fireEvent, render, screen, within } from "@testing-library/react";
import { beforeEach, describe, expect, it } from "vitest";
import { CommunityBoard } from "@/components/community/CommunityBoard";

describe("CommunityBoard", () => {
  beforeEach(() => {
    window.localStorage.clear();
  });

  it("shows the membership levels on the community page", () => {
    render(<CommunityBoard />);

    expect(screen.getByText("Bamboo Guest")).toBeInTheDocument();
    expect(screen.getByText("Panda Explorer")).toBeInTheDocument();
    expect(screen.getByText("Silk Road Insider")).toBeInTheDocument();
    expect(screen.getByText("Dragon Pass")).toBeInTheDocument();
    expect(screen.getByText("VisePanda Concierge")).toBeInTheDocument();
  });

  it("filters hot spots by city and category, including practical category", () => {
    render(<CommunityBoard />);

    // Default active city is Beijing
    expect(screen.getByText("Jingshan Park sunrise")).toBeInTheDocument();
    expect(screen.getByText("Beijing Metro ticketing")).toBeInTheDocument();

    // Click Shanghai filter
    fireEvent.click(screen.getByRole("button", { name: "Shanghai" }));
    expect(screen.getByText("Alipay payment setup")).toBeInTheDocument();
    expect(screen.queryByText("Jingshan Park sunrise")).not.toBeInTheDocument();

    // Click Category "Practical Tips"
    fireEvent.click(screen.getByRole("button", { name: "Practical Tips" }));
    expect(screen.getByText("Alipay payment setup")).toBeInTheDocument();
    expect(screen.queryByText("Tianzifang art lanes")).not.toBeInTheDocument();
  });

  it("supports Add to Trip click by redirecting to chat with draft message", () => {
    const originalLocation = window.location;
    delete (window as any).location;
    window.location = { ...originalLocation, href: "" } as any;

    render(<CommunityBoard />);

    const addButton = screen.getAllByRole("button", { name: /\+ add to trip/i })[0];
    fireEvent.click(addButton);

    expect(window.location.href).toContain("/chat?add=");

    window.location = originalLocation;
  });
});
