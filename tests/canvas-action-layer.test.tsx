import { fireEvent, render, screen, within } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { ButlerWorkspace } from "@/components/chat/ButlerWorkspace";

describe("Canvas Action Layer (v0.2.7)", () => {
  beforeEach(() => {
    window.localStorage.clear();
    window.history.replaceState(null, "", "/chat");
  });

  it("sends a day-specific message from a quick action and renders a Change Digest card", async () => {
    render(<ButlerWorkspace />);

    const day1Actions = screen.getByLabelText(/day 1 quick actions/i);
    fireEvent.click(within(day1Actions).getByRole("button", { name: /lighten/i }));

    expect(await screen.findByText(/Make Day 1 in Beijing lighter/i)).toBeInTheDocument();
    const digest = await screen.findByLabelText(/this update's changes/i);
    expect(within(digest).getByText(/Day 1/i)).toBeInTheDocument();
  });

  it("scrolls the target day into view when a Change Digest entry is clicked", async () => {
    const scrollSpy = vi.spyOn(Element.prototype, "scrollIntoView").mockImplementation(() => {});
    render(<ButlerWorkspace />);

    const day1Actions = screen.getByLabelText(/day 1 quick actions/i);
    fireEvent.click(within(day1Actions).getByRole("button", { name: /lighten/i }));

    const digest = await screen.findByLabelText(/this update's changes/i);
    scrollSpy.mockClear();
    fireEvent.click(within(digest).getAllByRole("button")[0]);

    expect(scrollSpy).toHaveBeenCalled();
    scrollSpy.mockRestore();
  });

  it("undo restores the canvas to the state before the last patch", async () => {
    render(<ButlerWorkspace />);

    const day1Card = screen.getByRole("button", { name: /view details for day 1/i }).closest("article")!;
    expect(within(day1Card).getByText(/pace: balanced/i)).toBeInTheDocument();

    const day1Actions = screen.getByLabelText(/day 1 quick actions/i);
    fireEvent.click(within(day1Actions).getByRole("button", { name: /lighten/i }));

    await screen.findByLabelText(/this update's changes/i);
    expect(within(day1Card).getByText(/pace: relaxed/i)).toBeInTheDocument();

    const digest = screen.getByLabelText(/this update's changes/i);
    fireEvent.click(within(digest).getByRole("button", { name: /undo/i }));

    expect(await screen.findByText(/Reverted to the previous version/i)).toBeInTheDocument();
    expect(within(day1Card).getByText(/pace: balanced/i)).toBeInTheDocument();
  });

  it("toggling a Before you fly checklist item marks it done and raises trip readiness", async () => {
    render(<ButlerWorkspace />);

    expect(screen.getByLabelText(/trip readiness/i)).toHaveTextContent("83%");

    const checklistItem = screen.getByText(/set up alipay before arrival/i).closest("label")!;
    const checkbox = within(checklistItem).getByRole("checkbox");
    expect(checkbox).not.toBeChecked();

    fireEvent.click(checkbox);

    expect(checkbox).toBeChecked();
    expect(screen.getByLabelText(/trip readiness/i)).toHaveTextContent("100%");
  });
});

afterEach(() => {
  vi.restoreAllMocks();
});
