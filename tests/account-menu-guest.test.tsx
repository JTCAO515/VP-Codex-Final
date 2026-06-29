import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { AccountMenu } from "@/components/account/AccountMenu";

describe("AccountMenu (guest, Supabase not configured)", () => {
  it("shows guest mode messaging when the popover is opened", async () => {
    render(<AccountMenu />);

    fireEvent.click(screen.getByRole("button", { name: /account/i }));

    expect(await screen.findByText(/Supabase is not configured for this deployment yet/i)).toBeInTheDocument();
  });
});
