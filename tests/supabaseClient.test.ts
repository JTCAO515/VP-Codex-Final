import { describe, expect, it } from "vitest";
import { isSupabaseConfigured } from "@/lib/supabase/client";

describe("isSupabaseConfigured", () => {
  it("is false when the project url or anon key is missing", () => {
    expect(isSupabaseConfigured({})).toBe(false);
    expect(isSupabaseConfigured({ NEXT_PUBLIC_SUPABASE_URL: "https://example.supabase.co" } as NodeJS.ProcessEnv)).toBe(
      false,
    );
  });

  it("is true once both the url and anon key are present", () => {
    const env = {
      NEXT_PUBLIC_SUPABASE_URL: "https://example.supabase.co",
      NEXT_PUBLIC_SUPABASE_ANON_KEY: "anon-key",
    } as NodeJS.ProcessEnv;

    expect(isSupabaseConfigured(env)).toBe(true);
  });
});
