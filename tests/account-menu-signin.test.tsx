import { fireEvent, render, screen, within } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { AccountMenu } from "@/components/account/AccountMenu";

vi.mock("@/lib/supabase/useSupabaseSession", () => ({
  useSupabaseSession: () => ({ configured: true, loading: false, session: null }),
}));

const signInWithPassword = vi.fn(async () => ({ ok: true, message: "Signed in." }));
const signUpWithPassword = vi.fn(async () => ({ ok: true, message: "Account created." }));
const signInWithGoogle = vi.fn(async () => ({ ok: true, message: "Redirecting to Google..." }));

vi.mock("@/lib/supabase/auth", () => ({
  signInWithPassword: (...args: unknown[]) => signInWithPassword(...(args as [never])),
  signUpWithPassword: (...args: unknown[]) => signUpWithPassword(...(args as [never])),
  signInWithGoogle: () => signInWithGoogle(),
  signOut: vi.fn(),
  updateDisplayName: vi.fn(),
  updatePassword: vi.fn(),
}));

describe("AccountMenu (configured, signed out)", () => {
  it("lets a guest sign in with email and password", async () => {
    const { container } = render(<AccountMenu />);
    fireEvent.click(screen.getByRole("button", { name: /account/i }));

    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: "user@example.com" } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: "secret123" } });
    const form = container.querySelector("form") as HTMLFormElement;
    fireEvent.click(within(form).getByRole("button", { name: /^sign in$/i }));

    await screen.findByText(/^signed in\.$/i);
    expect(signInWithPassword).toHaveBeenCalledWith("user@example.com", "secret123");
  });

  it("offers a Google sign-in option", async () => {
    render(<AccountMenu />);
    fireEvent.click(screen.getByRole("button", { name: /account/i }));

    fireEvent.click(screen.getByRole("button", { name: /continue with google/i }));

    await screen.findByText(/redirecting to google/i);
    expect(signInWithGoogle).toHaveBeenCalled();
  });
});
