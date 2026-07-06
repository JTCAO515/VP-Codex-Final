import { describe, expect, it } from "vitest";
import { normalizeUpdatePayload } from "../lib/user-api";

describe("normalizeUpdatePayload", () => {
  it("keeps role and status only when valid", () => {
    expect(
      normalizeUpdatePayload({ role: "admin", status: "active", display_name: "Alice" })
    ).toEqual({ role: "admin", status: "active", display_name: "Alice" });
  });

  it("drops unknown values and keeps supported nullable fields", () => {
    expect(
      normalizeUpdatePayload({
        role: "owner",
        status: "paused",
        display_name: null,
        avatar_url: "https://example.com/avatar.png",
        email: "ignored@example.com"
      })
    ).toEqual({
      display_name: null,
      avatar_url: "https://example.com/avatar.png"
    });
  });
});
