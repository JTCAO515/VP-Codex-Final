import { fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it } from "vitest";
import { ToolsBoard } from "@/components/tools/ToolsBoard";

describe("ToolsBoard", () => {
  afterEach(() => {
    window.history.replaceState(null, "", "/");
  });

  it("shows tips for the default category and switches when another category is selected", async () => {
    render(<ToolsBoard />);

    expect(await screen.findByRole("heading", { name: /visa and entry/i, level: 2 })).toBeInTheDocument();
    expect(await screen.findByText(/passport has at least 6 months/i)).toBeInTheDocument();
    expect(await screen.findByRole("heading", { name: /before departure/i })).toBeInTheDocument();
    expect(await screen.findByRole("heading", { name: /offline pocket notes/i })).toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: /^currency$/i }));

    expect(await screen.findByRole("heading", { name: /^currency$/i, level: 2 })).toBeInTheDocument();
    expect(await screen.findByText(/Live exchange-rate conversion isn't built in yet/i)).toBeInTheDocument();
    expect(await screen.findByText(/API priority/i)).toBeInTheDocument();
  });

  it("opens the category named in the category URL parameter", async () => {
    window.history.replaceState(null, "", "/tools?category=payment-setup");

    render(<ToolsBoard />);

    expect(await screen.findByRole("heading", { name: /payment setup/i, level: 2 })).toBeInTheDocument();
    expect((await screen.findAllByText(/Alipay or WeChat Pay/i)).length).toBeGreaterThan(0);
    expect(screen.getByRole("button", { name: /payment setup/i })).toHaveAttribute("data-active", "true");
  });

  it("falls back to the first category when the category URL parameter is invalid", async () => {
    window.history.replaceState(null, "", "/tools?category=unknown");

    render(<ToolsBoard />);

    expect(await screen.findByRole("heading", { name: /visa and entry/i, level: 2 })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /visa and entry/i })).toHaveAttribute("data-active", "true");
  });
});
