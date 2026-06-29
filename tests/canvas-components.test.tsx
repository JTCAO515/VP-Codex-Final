import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { TripCanvas } from "@/components/canvas/TripCanvas";
import { initialTripState } from "@/lib/mock-ai/mockButler";

describe("TripCanvas", () => {
  it("renders the trip summary, day cards, and integrated butler task reminders", () => {
    render(<TripCanvas trip={initialTripState} />);

    expect(screen.getByLabelText(/live trip canvas/i)).toBeInTheDocument();
    expect(screen.getByText("China Trip Draft")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /view day 1 details/i })).toBeInTheDocument();
    expect(screen.queryByLabelText(/day 1 itinerary details/i)).not.toBeInTheDocument();
    expect(screen.getAllByText(/Beijing/i).length).toBeGreaterThan(0);
    expect(screen.getByText("Set up Alipay before arrival")).toBeInTheDocument();
    expect(screen.queryByRole("heading", { name: /practical reminders/i })).not.toBeInTheDocument();
  });

  it("switches the detail drawer when a day summary is selected", () => {
    const trip = {
      ...initialTripState,
      days: [
        ...initialTripState.days,
        {
          ...initialTripState.days[0],
          day: 2,
          city: "Shanghai",
          blocks: [
            {
              time: "Morning" as const,
              title: "The Bund walk",
              description: "Start with a simple riverfront orientation.",
            },
          ],
        },
      ],
    };

    render(<TripCanvas trip={trip} />);

    fireEvent.click(screen.getByRole("button", { name: /view day 2 details/i }));

    expect(screen.getByLabelText(/day 2 itinerary details/i)).toBeInTheDocument();
    expect(screen.getAllByText("Shanghai").length).toBeGreaterThan(0);
    expect(screen.getByText("The Bund walk")).toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: /close day details/i }));

    expect(screen.queryByLabelText(/day 2 itinerary details/i)).not.toBeInTheDocument();
  });
});
