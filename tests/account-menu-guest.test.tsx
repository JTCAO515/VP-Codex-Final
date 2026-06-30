import { fireEvent, render, screen } from "@testing-library/react";
import { beforeEach, describe, expect, it } from "vitest";
import { AccountMenu } from "@/components/account/AccountMenu";

describe("AccountMenu (guest, Supabase not configured)", () => {
  beforeEach(() => {
    window.localStorage.clear();
  });

  it("shows guest mode messaging when the popover is opened", async () => {
    render(<AccountMenu />);

    fireEvent.click(screen.getByRole("button", { name: /account/i }));

    expect(await screen.findByText(/Supabase is not configured for this deployment yet/i)).toBeInTheDocument();
  });

  it("lets a guest choose a panda avatar locally", async () => {
    render(<AccountMenu />);

    fireEvent.click(screen.getByRole("button", { name: /account/i }));
    fireEvent.click(await screen.findByRole("button", { name: /red scarf/i }));

    expect(await screen.findByText(/red scarf selected/i)).toBeInTheDocument();
    expect(window.localStorage.getItem("visepanda:selected-avatar")).toBe("red-scarf");
  });
});
