import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ButlerWorkspace } from "@/components/chat/ButlerWorkspace";

describe("ButlerWorkspace", () => {
  it("updates the canvas after a user asks for a first China trip", async () => {
    render(<ButlerWorkspace />);

    fireEvent.change(screen.getByLabelText(/ask visepanda/i), {
      target: { value: "I am visiting China for the first time for 5 days" },
    });
    fireEvent.click(screen.getByRole("button", { name: /send/i }));

    expect(await screen.findAllByText(/Beijing/i)).not.toHaveLength(0);
    expect(await screen.findAllByText(/Shanghai/i)).not.toHaveLength(0);
    expect(await screen.findByText(/VisePanda updated the canvas/i)).toBeInTheDocument();
  });
});
