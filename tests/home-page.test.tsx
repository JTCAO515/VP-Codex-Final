import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { HomePage } from "@/components/home/HomePage";

describe("HomePage archetype starts", () => {
  it("links three FIT archetypes into Chat with archetype parameters", () => {
    render(<HomePage />);

    expect(screen.getByRole("link", { name: /first china 10 days essentials/i })).toHaveAttribute(
      "href",
      "/chat?archetype=first-china-10-days",
    );
    expect(screen.getByRole("link", { name: /foodie china/i })).toHaveAttribute("href", "/chat?archetype=foodie-china");
    expect(screen.getByRole("link", { name: /history & nature/i })).toHaveAttribute(
      "href",
      "/chat?archetype=history-nature",
    );
  });
});
