import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { TripCanvas } from "@/components/canvas/TripCanvas";
import { initialTripState } from "@/lib/mock-ai/mockButler";

describe("TripCanvas", () => {
  it("renders timeline day cards with morning afternoon and evening blocks without butler task cards", () => {
    render(<TripCanvas trip={initialTripState} />);

    expect(screen.getByLabelText(/live trip canvas/i)).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "China Trip Draft", level: 1 })).toBeInTheDocument();
    expect(screen.getByText("Taking shape")).toBeInTheDocument();
    // Six-dimension completeness (route/stay/food/transport/payment/visa): the
    // mock initial trip has an unresolved payment alert and no visa alert, so
    // 5 of 6 dimensions are complete (payment is the outstanding one).
    expect(screen.getByLabelText(/trip readiness/i)).toHaveTextContent("83%");
    expect(screen.getByText("Route")).toBeInTheDocument();
    expect(screen.getByText("Payment")).toBeInTheDocument();
    expect(screen.getByText("Visa")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /view details for day 1/i })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /view details for day 2/i })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /view details for day 3/i })).toBeInTheDocument();
    expect(screen.queryByLabelText(/day 1 itinerary details/i)).not.toBeInTheDocument();
    expect(screen.getAllByText(/^Morning$/i).length).toBeGreaterThan(0);
    expect(screen.getAllByText(/^Afternoon$/i).length).toBeGreaterThan(0);
    expect(screen.getAllByText(/^Evening$/i).length).toBeGreaterThan(0);
    expect(screen.queryByText("Butler reminders")).not.toBeInTheDocument();
    // Guards against the removed top-of-canvas five-task-card strip
    // (CanvasTaskStrip) reappearing. "Visa" now legitimately appears as a
    // completeness-checklist label (see assertion above), so this checks the
    // strip's own aria-label instead of a bare text match.
    expect(screen.queryByLabelText(/butler planning tasks/i)).not.toBeInTheDocument();
  });

  it("maps canvas confidence into traveler-facing status copy", () => {
    render(
      <TripCanvas
        trip={{
          ...initialTripState,
          summary: {
            ...initialTripState.summary,
            title: "Nanjing 3-Day Trip",
            confidence: "Ready to save",
          },
        }}
      />,
    );

    expect(screen.getByRole("heading", { name: "Nanjing 3-Day Trip", level: 1 })).toBeInTheDocument();
    expect(screen.getAllByText("Travel-ready").length).toBeGreaterThan(0);
    expect(screen.queryByText("Ready to save")).not.toBeInTheDocument();
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

    fireEvent.click(screen.getByRole("button", { name: /view details for day 4/i }));

    expect(screen.getByLabelText(/day 4 itinerary details/i)).toBeInTheDocument();
    expect(screen.getAllByText("Shanghai").length).toBeGreaterThan(0);
    expect(screen.getAllByText("The Bund walk").length).toBeGreaterThan(0);

    fireEvent.click(screen.getByRole("button", { name: /close day details/i }));

    expect(screen.queryByLabelText(/day 4 itinerary details/i)).not.toBeInTheDocument();
  });

  it("opens a read-only itinerary detail drawer without edit controls", () => {
    render(<TripCanvas trip={initialTripState} />);

    fireEvent.click(screen.getByRole("button", { name: /view details for day 1/i }));

    expect(screen.getByLabelText(/day 1 itinerary details/i)).toBeInTheDocument();
    expect(screen.getAllByText("Forbidden City (故宫)").length).toBeGreaterThan(0);
    expect(screen.getByText(/Start with the classic imperial axis/i)).toBeInTheDocument();
    expect(screen.getAllByText(/Beijing city-center hotel/i).length).toBeGreaterThan(0);
    expect(screen.queryByLabelText(/morning title/i)).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: /save day 1 changes/i })).not.toBeInTheDocument();
  });
});
