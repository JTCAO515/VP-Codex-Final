import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ButlerReminders } from "@/components/canvas/ButlerReminders";
import type { ButlerAlert } from "@/lib/types/trip";

describe("ButlerReminders", () => {
  it("links alert types that have a matching Tools category and renders others as plain text", () => {
    const alerts: ButlerAlert[] = [
      {
        type: "visa",
        priority: "high",
        title: "Check entry rules before booking",
        body: "Visa-free and transit rules depend on nationality.",
        action: "Review visa checklist",
      },
      {
        type: "booking",
        priority: "low",
        title: "Confirm attraction bookings",
        body: "Two tickets still need confirmation.",
        action: "Confirm tickets",
      },
    ];

    render(<ButlerReminders alerts={alerts} />);

    expect(screen.getByRole("link", { name: /review visa checklist/i })).toHaveAttribute(
      "href",
      "/tools?category=visa-and-entry",
    );
    expect(screen.getByText("Confirm tickets")).toBeInTheDocument();
    expect(screen.queryByRole("link", { name: /confirm tickets/i })).not.toBeInTheDocument();
  });

  it("renders nothing when there are no alerts", () => {
    const { container } = render(<ButlerReminders alerts={[]} />);
    expect(container).toBeEmptyDOMElement();
  });
});
