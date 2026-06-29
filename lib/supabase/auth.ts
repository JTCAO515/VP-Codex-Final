import type { Session } from "@supabase/supabase-js";
import { getSupabaseBrowserClient } from "@/lib/supabase/client";

export interface AuthResult {
  ok: boolean;
  message: string;
}

const NOT_CONFIGURED_MESSAGE =
  "Supabase is not configured yet. Add NEXT_PUBLIC_SUPABASE_URL and NEXT_PUBLIC_SUPABASE_ANON_KEY.";

export async function signUpWithPassword(email: string, password: string): Promise<AuthResult> {
  const client = getSupabaseBrowserClient();
  if (!client) return { ok: false, message: NOT_CONFIGURED_MESSAGE };

  const { error } = await client.auth.signUp({ email, password });
  if (error) return { ok: false, message: error.message };
  return { ok: true, message: "Account created. Check your email if confirmation is required, then sign in." };
}

export async function signInWithPassword(email: string, password: string): Promise<AuthResult> {
  const client = getSupabaseBrowserClient();
  if (!client) return { ok: false, message: NOT_CONFIGURED_MESSAGE };

  const { error } = await client.auth.signInWithPassword({ email, password });
  if (error) return { ok: false, message: error.message };
  return { ok: true, message: "Signed in." };
}

export async function signInWithGoogle(): Promise<AuthResult> {
  const client = getSupabaseBrowserClient();
  if (!client) return { ok: false, message: NOT_CONFIGURED_MESSAGE };

  const { error } = await client.auth.signInWithOAuth({
    provider: "google",
    options: {
      redirectTo: typeof window !== "undefined" ? window.location.origin : undefined,
    },
  });

  if (error) return { ok: false, message: error.message };
  return { ok: true, message: "Redirecting to Google..." };
}

export async function updateDisplayName(name: string): Promise<AuthResult> {
  const client = getSupabaseBrowserClient();
  if (!client) return { ok: false, message: NOT_CONFIGURED_MESSAGE };

  const { error } = await client.auth.updateUser({ data: { full_name: name } });
  if (error) return { ok: false, message: error.message };
  return { ok: true, message: "Name updated." };
}

export async function updatePassword(newPassword: string): Promise<AuthResult> {
  const client = getSupabaseBrowserClient();
  if (!client) return { ok: false, message: NOT_CONFIGURED_MESSAGE };

  const { error } = await client.auth.updateUser({ password: newPassword });
  if (error) return { ok: false, message: error.message };
  return { ok: true, message: "Password updated." };
}

export async function signOut(): Promise<void> {
  const client = getSupabaseBrowserClient();
  if (!client) return;
  await client.auth.signOut();
}

export async function getCurrentSession(): Promise<Session | null> {
  const client = getSupabaseBrowserClient();
  if (!client) return null;
  const { data } = await client.auth.getSession();
  return data.session;
}

export function subscribeToAuthChanges(callback: (session: Session | null) => void): () => void {
  const client = getSupabaseBrowserClient();
  if (!client) return () => {};

  const { data } = client.auth.onAuthStateChange((_event, session) => {
    callback(session);
  });

  return () => data.subscription.unsubscribe();
}
