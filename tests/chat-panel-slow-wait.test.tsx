import { act, render, screen } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { ChatPanel } from "@/components/chat/ChatPanel";

describe("ChatPanel slow-wait indicator (v0.3.20)", () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it("shows the default thinking copy while busy, then escalates after 15s", () => {
    render(<ChatPanel messages={[]} onSend={() => {}} suggestions={[]} busy />);

    expect(screen.getByText("Checking the practical travel details...")).toBeTruthy();

    act(() => {
      vi.advanceTimersByTime(15000);
    });

    expect(screen.getByText(/Still working/)).toBeTruthy();
  });

  it("resets the slow-wait copy once busy clears", () => {
    const { rerender } = render(<ChatPanel messages={[]} onSend={() => {}} suggestions={[]} busy />);
    act(() => {
      vi.advanceTimersByTime(15000);
    });
    expect(screen.getByText(/Still working/)).toBeTruthy();

    rerender(<ChatPanel messages={[]} onSend={() => {}} suggestions={[]} busy={false} />);
    expect(screen.queryByText(/Still working/)).toBeNull();
  });
});
