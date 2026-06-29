"use client";

import Link from "next/link";
import { FormEvent, useState } from "react";
import { signInWithMagicLink, signOut } from "@/lib/supabase/auth";
import { useSupabaseSession } from "@/lib/supabase/useSupabaseSession";

export function AccountPanel() {
  const { configured, loading, session } = useSupabaseSession();
  const [email, setEmail] = useState("");
  const [status, setStatus] = useState<string | null>(null);
  const [sending, setSending] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!email.trim()) return;
    setSending(true);
    const result = await signInWithMagicLink(email.trim());
    setStatus(result.message);
    setSending(false);
  }

  async function handleSignOut() {
    await signOut();
    setStatus("Signed out.");
  }

  return (
    <section className="placeholder-page" aria-labelledby="account-title">
      <div className="placeholder-page__copy">
        <p className="section-kicker">Account</p>
        <h1 id="account-title">Sign in to sync your trips</h1>

        {!configured && (
          <>
            <p>
              Supabase is not configured for this deployment yet. Guest mode stays available, and sign-in will
              activate once Supabase project keys are added.
            </p>
            <Link className="primary-link" href="/chat">
              Return to Chat
            </Link>
          </>
        )}

        {configured && loading && <p>Checking your session...</p>}

        {configured && !loading && session && (
          <>
            <p>Signed in as {session.user.email}. Trips you save in Chat will sync to your account.</p>
            <button className="primary-link" type="button" onClick={handleSignOut}>
              Sign out
            </button>
          </>
        )}

        {configured && !loading && !session && (
          <>
            <p>Sign in with a magic link. No password needed, guest mode stays available if you skip this.</p>
            <form className="chat-composer" onSubmit={handleSubmit}>
              <label htmlFor="account-email">Email</label>
              <input
                id="account-email"
                type="email"
                required
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                placeholder="you@example.com"
              />
              <button type="submit" disabled={sending || !email.trim()}>
                {sending ? "Sending..." : "Send magic link"}
              </button>
            </form>
            {status && (
              <p role="status" aria-live="polite">
                {status}
              </p>
            )}
            <Link className="primary-link" href="/chat">
              Continue as guest
            </Link>
          </>
        )}
      </div>
      <ul className="placeholder-list" aria-label="Account future scope">
        {["Guest mode", "Magic link sign in", "Trip sync", "Chat history", "Saved preferences"].map((item) => (
          <li key={item}>{item}</li>
        ))}
      </ul>
    </section>
  );
}
