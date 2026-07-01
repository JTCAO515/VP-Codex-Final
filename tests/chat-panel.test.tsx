import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { ChatPanel } from "@/components/chat/ChatPanel";
import type { ChatMessage } from "@/lib/types/trip";

const assistantMessage: ChatMessage = {
  id: "assistant-1",
  role: "assistant",
  content: "I built a draft.",
  response: {
    headline: "Your first route is taking shape",
    body: "This draft keeps the pace balanced.",
    highlights: ["Beijing first", "Shanghai last"],
    nextStep: "Add convenient hotel areas",
  },
};

describe("ChatPanel interaction shell", () => {
  it("renders three first-run chips when the conversation is empty", () => {
    render(<ChatPanel messages={[]} onSend={() => undefined} suggestions={["First China 10 Days Essentials", "Foodie China", "History & Nature"]} />);

    const starterButtons = screen.getAllByRole("button").filter((button) => button.closest(".prompt-row"));
    expect(starterButtons).toHaveLength(3);
    expect(screen.getByRole("button", { name: /first china 10 days essentials/i })).toBeInTheDocument();
  });

  it("promotes the latest structured nextStep into a primary action", () => {
    const onSend = vi.fn();
    render(<ChatPanel messages={[assistantMessage]} onSend={onSend} suggestions={["Make it easier", "Add food"]} />);

    const nextStepAction = screen.getByRole("button", { name: /add convenient hotel areas/i });
    expect(nextStepAction.closest(".chat-next-step-card")).toBeTruthy();

    fireEvent.click(nextStepAction);
    expect(onSend).toHaveBeenCalledWith("Add convenient hotel areas");
  });
});
