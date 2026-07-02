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
    expect(screen.getByLabelText(/chat starter state/i)).toHaveTextContent("Start with a ready-made China route");
  });

  it("promotes the latest structured nextStep into a primary action", () => {
    const onSend = vi.fn();
    render(<ChatPanel messages={[assistantMessage]} onSend={onSend} suggestions={["Make it easier", "Add food"]} />);

    const nextStepAction = screen.getByRole("button", { name: /add convenient hotel areas/i });
    expect(nextStepAction.closest(".chat-next-step-card")).toBeTruthy();

    fireEvent.click(nextStepAction);
    expect(onSend).toHaveBeenCalledWith("Add convenient hotel areas");
  });

  it("dismisses the next-step card without sending a message", () => {
    const onSend = vi.fn();
    render(<ChatPanel messages={[assistantMessage]} onSend={onSend} suggestions={[]} />);

    fireEvent.click(screen.getByRole("button", { name: /dismiss next step/i }));

    expect(screen.queryByLabelText(/primary next step/i)).not.toBeInTheDocument();
    expect(onSend).not.toHaveBeenCalled();
  });

  it("toggles feedback on an assistant response as a local-only action", () => {
    render(<ChatPanel messages={[assistantMessage]} onSend={() => undefined} suggestions={[]} />);

    const upvote = screen.getByRole("button", { name: /good response/i });
    expect(upvote).toHaveAttribute("aria-pressed", "false");

    fireEvent.click(upvote);
    expect(upvote).toHaveAttribute("aria-pressed", "true");

    fireEvent.click(upvote);
    expect(upvote).toHaveAttribute("aria-pressed", "false");
  });

  it("copies the assistant response text and shows a Copied confirmation", async () => {
    const writeText = vi.fn().mockResolvedValue(undefined);
    Object.assign(navigator, { clipboard: { writeText } });

    render(<ChatPanel messages={[assistantMessage]} onSend={() => undefined} suggestions={[]} />);

    fireEvent.click(screen.getByRole("button", { name: /copy response/i }));

    expect(await screen.findByText("Copied")).toBeInTheDocument();
    expect(writeText).toHaveBeenCalledWith(expect.stringContaining("Your first route is taking shape"));
  });

  it("sends the draft on Enter and inserts a newline on Shift+Enter", () => {
    const onSend = vi.fn();
    render(<ChatPanel messages={[]} onSend={onSend} suggestions={[]} />);

    const textarea = screen.getByLabelText(/ask visepanda/i);
    fireEvent.change(textarea, { target: { value: "Plan a trip to Xi'an" } });
    fireEvent.keyDown(textarea, { key: "Enter", shiftKey: true });
    expect(onSend).not.toHaveBeenCalled();

    fireEvent.keyDown(textarea, { key: "Enter" });
    expect(onSend).toHaveBeenCalledWith("Plan a trip to Xi'an");
  });
});
