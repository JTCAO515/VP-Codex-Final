"use client";

import type { Session } from "@supabase/supabase-js";
import { useEffect, useState } from "react";
import { getSupabaseBrowserClient } from "@/lib/supabase/client";
import { getCurrentSession, subscribeToAuthChanges } from "@/lib/supabase/auth";

export interface SupabaseSessionState {
  configured: boolean;
  loading: boolean;
  session: Session | null;
}

export function useSupabaseSession(): SupabaseSessionState {
  const configured = getSupabaseBrowserClient() !== null;
  const [session, setSession] = useState<Session | null>(null);
  const [loading, setLoading] = useState(configured);

  useEffect(() => {
    if (!configured) {
      setLoading(false);
      return;
    }

    let active = true;
    getCurrentSession().then((current) => {
      if (active) {
        setSession(current);
        setLoading(false);
      }
    });

    const unsubscribe = subscribeToAuthChanges((next) => {
      if (active) setSession(next);
    });

    return () => {
      active = false;
      unsubscribe();
    };
  }, [configured]);

  return { configured, loading, session };
}
