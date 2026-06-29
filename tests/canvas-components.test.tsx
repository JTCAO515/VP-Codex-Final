import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { TripCanvas } from "@/components/canvas/TripCanvas";
import { initialTripState } from "@/lib/mock-ai/mockButler";

describe("TripCanvas", () => {
  it("renders the trip summary, day cards, and butler reminders", () => {
    render(<TripCanvas trip={initialTripState} />);

    expect(screen.getByLabelText(/live trip canvas/i)).toBeInTheDocument();
    expect(screen.getByText("China Trip Draft")).toBeInTheDocument();
    expect(screen.getByText(/Day 1/i)).toBeInTheDocument();
    expect(screen.getAllByText(/Beijing/i).length).toBeGreaterThan(0);
    expect(screen.getByText("Set up Alipay before arrival")).toBeInTheDocument();
  });
});
