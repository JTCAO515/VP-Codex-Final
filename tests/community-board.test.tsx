import { fireEvent, render, screen, within } from "@testing-library/react";
import { beforeEach, describe, expect, it } from "vitest";
import { CommunityBoard } from "@/components/community/CommunityBoard";

describe("CommunityBoard", () => {
  beforeEach(() => {
    window.localStorage.clear();
  });

  it("shows the membership levels on the community page", () => {
    render(<CommunityBoard />);

    expect(screen.getByText("Bamboo Guest")).toBeInTheDocument();
    expect(screen.getByText("Panda Explorer")).toBeInTheDocument();
    expect(screen.getByText("Silk Road Insider")).toBeInTheDocument();
    expect(screen.getByText("Dragon Pass")).toBeInTheDocument();
    expect(screen.getByText("VisePanda Concierge")).toBeInTheDocument();
  });

  it("publishes a local community post and supports reactions", () => {
    render(<CommunityBoard />);

    fireEvent.click(screen.getByRole("button", { name: /\+ share my trip/i }));
    fireEvent.change(screen.getByPlaceholderText(/relaxed shanghai food route/i), {
      target: { value: "My calm Beijing tea walk" },
    });
    fireEvent.change(screen.getByPlaceholderText(/what should other travelers know/i), {
      target: { value: "Start near Jingshan, then take a quiet hutong tea break before dinner." },
    });
    fireEvent.click(screen.getByRole("button", { name: /publish locally/i }));

    const post = screen.getByLabelText("My calm Beijing tea walk");
    expect(post).toBeInTheDocument();

    fireEvent.click(within(post).getByRole("button", { name: /like this post/i }));
    expect(within(post).getByRole("button", { name: /like this post/i })).toHaveTextContent("Heart 1");

    fireEvent.click(within(post).getByRole("button", { name: /save post/i }));
    expect(within(post).getByRole("button", { name: /save post/i })).toHaveTextContent("Save 1");

    fireEvent.change(within(post).getByLabelText(/comment on my calm beijing tea walk/i), {
      target: { value: "Great pacing." },
    });
    fireEvent.click(within(post).getByRole("button", { name: /^comment$/i }));
    expect(within(post).getByText(/great pacing/i)).toBeInTheDocument();
  });

  it("adds a local photo card from the Photos tab", () => {
    render(<CommunityBoard />);

    fireEvent.click(screen.getByRole("tab", { name: /photos/i }));
    fireEvent.click(screen.getByRole("button", { name: /\+ add photo card/i }));
    fireEvent.change(screen.getByPlaceholderText(/morning mist over west lake/i), {
      target: { value: "Lanterns by the old city wall" },
    });
    fireEvent.change(screen.getByPlaceholderText(/the bund/i), {
      target: { value: "Xi'an City Wall" },
    });
    fireEvent.click(screen.getByRole("button", { name: /publish photo card/i }));

    expect(screen.getByLabelText("Lanterns by the old city wall")).toBeInTheDocument();
  });
});
