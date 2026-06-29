import { describe, expect, it } from "vitest";
import { getEnvironmentStatus } from "@/lib/env/placeholders";

describe("getEnvironmentStatus", () => {
  it("reports missing keys without throwing", () => {
    const result = getEnvironmentStatus({});

    expect(result.length).toBeGreaterThan(5);
    expect(result.every((item) => item.configured === false)).toBe(true);
  });

  it("marks configured keys", () => {
    const result = getEnvironmentStatus({ AI_API_KEY: "test-key" });

    expect(result.find((item) => item.key === "AI_API_KEY")?.configured).toBe(true);
  });
});
