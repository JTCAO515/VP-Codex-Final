import { fireEvent, render, screen, within } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import TripsPage from "@/app/trips/page";

describe("TripsPage", () => {
  it("renders a saved trips dashboard instead of a placeholder", () => {
    render(<TripsPage />);

    expect(screen.queryByText(/saved trips are coming next/i)).not.toBeInTheDocument();
    expect(screen.getByRole("heading", { name: /your trips/i })).toBeInTheDocument();
    expect(screen.getByText("Beijing -> Shanghai First China Trip")).toBeInTheDocument();
    expect(screen.getByText("Chengdu Food Weekend")).toBeInTheDocument();
    expect(screen.getByText("Shanghai Business + Culture")).toBeInTheDocument();
    expect(screen.getAllByRole("link", { name: /continue in chat/i })[0]).toHaveAttribute("href", "/chat");
  });

  it("filters saved trips by status", () => {
    render(<TripsPage />);

    fireEvent.click(screen.getByRole("button", { name: /^ready$/i }));

    expect(screen.getByText("Beijing -> Shanghai First China Trip")).toBeInTheDocument();
    expect(screen.queryByText("Chengdu Food Weekend")).not.toBeInTheDocument();
    expect(screen.queryByText("Shanghai Business + Culture")).not.toBeInTheDocument();

    const stats = screen.getByLabelText(/trip library summary/i);
    expect(within(stats).getByText("1")).toBeInTheDocument();
  });
});
