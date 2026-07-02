import { fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { ToolsBoard } from "@/components/tools/ToolsBoard";

vi.mock("@/lib/tools", async () => {
  const { createStaticToolsProvider } = await vi.importActual<typeof import("@/lib/tools/staticProvider")>(
    "@/lib/tools/staticProvider",
  );
  return {
    getToolsProvider: () => createStaticToolsProvider(),
  };
});

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

  it("renders a working RMB converter for the currency category", async () => {
    render(<ToolsBoard />);

    fireEvent.click(await screen.findByRole("button", { name: /^currency$/i }));

    expect(await screen.findByText(/quick rmb converter/i)).toBeInTheDocument();
    expect(screen.getByText(/¥100 ≈ 14 USD/i)).toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: /¥500/i }));

    expect(screen.getByText(/¥500 ≈ 70 USD/i)).toBeInTheDocument();
  });

  it("renders a conservative visa checker for entry planning", async () => {
    render(<ToolsBoard />);

    fireEvent.click(await screen.findByRole("button", { name: /^visa and entry$/i }));

    expect(await screen.findByText(/entry planning check/i)).toBeInTheDocument();
    expect(screen.getByText(/Confirm with an official embassy or consulate/i)).toBeInTheDocument();

    fireEvent.change(screen.getByLabelText(/passport nationality/i), { target: { value: "germany" } });

    expect(screen.getByText(/Likely short-stay visa-free for up to 30 days/i)).toBeInTheDocument();
  });

  it("renders a payment setup wizard with wallet and card choices", async () => {
    render(<ToolsBoard />);

    fireEvent.click(await screen.findByRole("button", { name: /^payment setup$/i }));

    expect(await screen.findByText(/payment setup path/i)).toBeInTheDocument();
    expect(screen.getByText(/Install Alipay before departure/i)).toBeInTheDocument();

    fireEvent.change(screen.getByLabelText(/^wallet$/i), { target: { value: "wechat-pay" } });
    fireEvent.change(screen.getByLabelText(/card brand/i), { target: { value: "amex" } });

    expect(screen.getByText(/Install WeChat before departure/i)).toBeInTheDocument();
    expect(screen.getByText(/Support varies; keep a Visa or Mastercard backup/i)).toBeInTheDocument();
  });
});
