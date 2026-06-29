import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { AccountMenu } from "@/components/account/AccountMenu";

const stableSession = { user: { id: "user-1", email: "owner@example.com", user_metadata: {} } };

vi.mock("@/lib/supabase/useSupabaseSession", () => ({
  useSupabaseSession: () => ({ configured: true, loading: false, session: stableSession }),
}));

const updateDisplayName = vi.fn(async () => ({ ok: true, message: "Name updated." }));
const updatePassword = vi.fn(async () => ({ ok: true, message: "Password updated." }));
const signOut = vi.fn(async () => undefined);

vi.mock("@/lib/supabase/auth", () => ({
  signInWithPassword: vi.fn(),
  signUpWithPassword: vi.fn(),
  signInWithGoogle: vi.fn(),
  signOut: () => signOut(),
  updateDisplayName: (...args: unknown[]) => updateDisplayName(...(args as [never])),
  updatePassword: (...args: unknown[]) => updatePassword(...(args as [never])),
}));

describe("AccountMenu (signed in)", () => {
  it("lets a signed-in user change their name", async () => {
    render(<AccountMenu />);
    fireEvent.click(screen.getByRole("button", { name: /account/i }));

    await screen.findByText(/signed in as owner@example.com/i);

    fireEvent.click(screen.getByRole("button", { name: /change name/i }));
    fireEvent.change(screen.getByLabelText(/new name/i), { target: { value: "Jamie" } });
    fireEvent.click(screen.getByRole("button", { name: /^save$/i }));

    await screen.findByText(/^name updated\.$/i);
    expect(updateDisplayName).toHaveBeenCalledWith("Jamie");
  });

  it("lets a signed-in user log out", async () => {
    render(<AccountMenu />);
    fireEvent.click(screen.getByRole("button", { name: /account/i }));

    await screen.findByText(/signed in as owner@example.com/i);
    fireEvent.click(screen.getByRole("button", { name: /log out/i }));

    await screen.findByText(/^signed out\.$/i);
    expect(signOut).toHaveBeenCalled();
  });
});
