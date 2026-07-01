import { describe, expect, it } from "vitest";
import { emptyPreferenceProfile, preferenceProfileSummary, updatePreferenceProfile } from "@/lib/ai/preferenceProfile";

describe("preference profile", () => {
  it("extracts traveler preferences from natural messages", () => {
    const profile = updatePreferenceProfile(
      emptyPreferenceProfile,
      "We are a family with kids on a student budget. Please keep it less tiring, no pork, and add local food plus history.",
    );

    expect(profile.party).toBe("family_with_kids");
    expect(profile.budget).toBe("economy");
    expect(profile.pace).toBe("light");
    expect(profile.dietaryRestrictions).toContain("no pork / halal-aware");
    expect(profile.interests).toEqual(expect.arrayContaining(["food", "history and culture"]));
    expect(preferenceProfileSummary(profile)).toEqual(expect.arrayContaining(["light pace", "economy budget"]));
  });
});
