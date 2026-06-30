import { fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it } from "vitest";
import { ToolsBoard } from "@/components/tools/ToolsBoard";

describe("ToolsBoard", () => {
  afterEach(() => {
    window.history.replaceState(null, "", "/tools");
  });

  it("shows category cards only until a card is opened", async () => {
    render(<ToolsBoard />);

    expect(await screen.findByRole("button", { name: /^visa and entry$/i })).toBeInTheDocument();
    expect(screen.queryByRole("heading", { name: /visa and entry/i, level: 2 })).not.toBeInTheDocument();
    expect(screen.queryByText(/passport has at least 6 months/i)).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: /^translate$/i })).not.toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: /^currency$/i }));

    expect(await screen.findByRole("heading", { name: /^currency$/i, level: 2 })).toBeInTheDocument();
    expect(await screen.findByText(/Check a current exchange-rate reference/i)).toBeInTheDocument();
  });

  it("preselects a category from the ?category= URL param", async () => {
    window.history.replaceState(null, "", "/tools?category=payment-setup");

    render(<ToolsBoard />);

    expect(await screen.findByRole("heading", { name: /^payment setup$/i, level: 2 })).toBeInTheDocument();
  });

  it("does not render the old Translate tools category", async () => {
    render(<ToolsBoard />);

    expect(await screen.findByRole("button", { name: /^visa and entry$/i })).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: /^translate$/i })).not.toBeInTheDocument();
    expect(screen.queryByText(/Open Translator/i)).not.toBeInTheDocument();
  });

  it("does not show provider implementation metadata in the page UI", async () => {
    render(<ToolsBoard />);

    fireEvent.click(await screen.findByRole("button", { name: /^currency$/i }));

    expect(screen.queryByText(/Live tools provider/i)).not.toBeInTheDocument();
    expect(screen.queryByText(/travel tool categories with practical checklists/i)).not.toBeInTheDocument();
    expect(screen.queryByText(/Translation API is the next candidate/i)).not.toBeInTheDocument();
    expect(screen.queryByText(/Exchange-rate API \/ Machine translation API/i)).not.toBeInTheDocument();
    expect(screen.queryByText(/Exchange-rate API/i)).not.toBeInTheDocument();
    expect(screen.queryByText(/Transit data API/i)).not.toBeInTheDocument();
  });
});
