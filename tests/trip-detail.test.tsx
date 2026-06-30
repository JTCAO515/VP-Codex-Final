import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { TripDetail } from "@/components/trips/TripDetail";

describe("TripDetail", () => {
  it("shows the example trip summary with a guest notice when Supabase is not configured", async () => {
    render(<TripDetail tripId="first-china-beijing-shanghai" />);

    expect(await screen.findByRole("heading", { name: /beijing -> shanghai first china trip/i })).toBeInTheDocument();
    expect(screen.getByText(/this is an example trip/i)).toBeInTheDocument();
    expect(screen.getByText(/forbidden city/i)).toBeInTheDocument();
    expect(screen.getByText(/ready means this itinerary is prepared for review/i)).toBeInTheDocument();
  });

  it("shows a not-found notice for an unknown trip id", async () => {
    render(<TripDetail tripId="does-not-exist" />);

    expect(await screen.findByRole("heading", { name: /trip not found/i })).toBeInTheDocument();
  });
});
