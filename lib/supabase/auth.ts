import type { Session } from "@supabase/supabase-js";
import { getSupabaseBrowserClient } from "@/lib/supabase/client";

export interface MagicLinkResult {
  ok: boolean;
  message: string;
}

export async function signInWithMagicLink(email: string): Promise<MagicLinkResult> {
  const client = getSupabaseBrowserClient();
  if (!client) {
    return { ok: false, message: "Supabase is not configured yet. Add NEXT_PUBLIC_SUPABASE_URL and NEXT_PUBLIC_SUPABASE_ANON_KEY." };
  }

  const { error } = await client.auth.signInWithOtp({
    email,
    options: {
      emailRedirectTo: typeof window !== "undefined" ? `${window.location.origin}/account` : undefined,
    },
  });

  if (error) return { ok: false, message: error.message };
  return { ok: true, message: "Magic link sent. Check your email to finish signing in." };
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
