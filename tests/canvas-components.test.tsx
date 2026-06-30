import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { TripCanvas } from "@/components/canvas/TripCanvas";
import { initialTripState } from "@/lib/mock-ai/mockButler";

describe("TripCanvas", () => {
  it("renders timeline day cards with morning afternoon and evening blocks without butler task cards", () => {
    render(<TripCanvas trip={initialTripState} />);

    expect(screen.getByLabelText(/live trip canvas/i)).toBeInTheDocument();
    expect(screen.getByText("China Trip Draft")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /open day 1 itinerary drawer/i })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /open day 2 itinerary drawer/i })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /open day 3 itinerary drawer/i })).toBeInTheDocument();
    expect(screen.queryByLabelText(/day 1 itinerary details/i)).not.toBeInTheDocument();
    expect(screen.getAllByText(/^Morning$/i).length).toBeGreaterThan(0);
    expect(screen.getAllByText(/^Afternoon$/i).length).toBeGreaterThan(0);
    expect(screen.getAllByText(/^Evening$/i).length).toBeGreaterThan(0);
    expect(screen.getByText("Set up Alipay before arrival")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /review payment setup/i })).toHaveAttribute(
      "href",
      "/tools?category=payment-setup",
    );
    expect(screen.queryByText("Visa")).not.toBeInTheDocument();
    expect(screen.queryByRole("heading", { name: /practical reminders/i })).not.toBeInTheDocument();
  });

  it("switches the detail drawer when a day summary is selected", () => {
    const trip = {
      ...initialTripState,
      days: [
        ...initialTripState.days,
        {
          ...initialTripState.days[0],
          day: 4,
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

    fireEvent.click(screen.getByRole("button", { name: /open day 4 itinerary drawer/i }));

    expect(screen.getByLabelText(/day 4 itinerary details/i)).toBeInTheDocument();
    expect(screen.getAllByText("Shanghai").length).toBeGreaterThan(0);
    expect(screen.getAllByText("The Bund walk").length).toBeGreaterThan(0);

    fireEvent.click(screen.getByRole("button", { name: /close day details/i }));

    expect(screen.queryByLabelText(/day 4 itinerary details/i)).not.toBeInTheDocument();
  });

  it("edits a day itinerary from the drawer and updates the canvas card", () => {
    render(<TripCanvas trip={initialTripState} />);

    fireEvent.click(screen.getByRole("button", { name: /open day 1 itinerary drawer/i }));
    fireEvent.change(screen.getByLabelText(/morning title/i), {
      target: { value: "Forbidden City private guide" },
    });
    fireEvent.change(screen.getByLabelText(/hotel/i), {
      target: { value: "Beijing boutique courtyard hotel" },
    });
    fireEvent.click(screen.getByRole("button", { name: /save day 1 changes/i }));

    expect(screen.queryByLabelText(/day 1 itinerary details/i)).not.toBeInTheDocument();
    expect(screen.getAllByText("Forbidden City private guide").length).toBeGreaterThan(0);
    expect(screen.getByText(/Beijing boutique courtyard hotel/i)).toBeInTheDocument();
  });
});
