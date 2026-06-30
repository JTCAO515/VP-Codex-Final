import { fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it } from "vitest";
import { ToolsBoard } from "@/components/tools/ToolsBoard";

describe("ToolsBoard", () => {
  afterEach(() => {
    window.history.replaceState(null, "", "/tools");
  });

  it("shows tips for the default category and switches when another category is selected", async () => {
    render(<ToolsBoard />);

    expect(await screen.findByRole("heading", { name: /visa and entry/i, level: 2 })).toBeInTheDocument();
    expect(await screen.findByText(/passport has at least 6 months/i)).toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: /^currency$/i }));

    expect(await screen.findByRole("heading", { name: /^currency$/i, level: 2 })).toBeInTheDocument();
    expect(await screen.findByText(/Live exchange-rate conversion isn't built in yet/i)).toBeInTheDocument();
  });

  it("preselects a category from the ?category= URL param", async () => {
    window.history.replaceState(null, "", "/tools?category=payment-setup");

    render(<ToolsBoard />);

    expect(await screen.findByRole("heading", { name: /^payment setup$/i, level: 2 })).toBeInTheDocument();
  });
});
