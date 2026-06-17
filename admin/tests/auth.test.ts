import { describe, expect, it } from "vitest";
import { canAccessAdmin } from "../lib/auth";

describe("canAccessAdmin", () => {
  it("returns true only for active admin", () => {
    expect(canAccessAdmin({ role: "admin", status: "active" })).toBe(true);
    expect(canAccessAdmin({ role: "user", status: "active" })).toBe(false);
    expect(canAccessAdmin({ role: "admin", status: "disabled" })).toBe(false);
  });
});
