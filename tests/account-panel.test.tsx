import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { AccountPanel } from "@/components/account/AccountPanel";

describe("AccountPanel", () => {
  it("shows guest mode messaging when Supabase is not configured", async () => {
    render(<AccountPanel />);

    expect(await screen.findByText(/Supabase is not configured for this deployment yet/i)).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /return to chat/i })).toHaveAttribute("href", "/chat");
  });
});
