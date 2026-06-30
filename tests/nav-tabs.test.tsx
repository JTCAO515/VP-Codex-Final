import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { NavTabs } from "@/components/shell/NavTabs";

describe("NavTabs", () => {
  it("renders all primary tabs with icon svgs", () => {
    const { container } = render(<NavTabs activeTab="chat" />);

    expect(screen.getByRole("link", { name: /chat/i })).toHaveAttribute("href", "/chat");
    expect(screen.getByRole("link", { name: /trips/i })).toHaveAttribute("href", "/trips");
    expect(screen.getByRole("link", { name: /explore/i })).toHaveAttribute("href", "/explore");
    expect(screen.getByRole("link", { name: /tools/i })).toHaveAttribute("href", "/tools");
    expect(screen.getByRole("link", { name: /translate/i })).toHaveAttribute("href", "/translate");
    expect(container.querySelectorAll(".nav-tabs__link svg")).toHaveLength(5);
  });
});
