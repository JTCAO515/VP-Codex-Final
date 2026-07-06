import { describe, expect, it } from "vitest";
import { filterUserRows, toUserRow } from "../lib/users";

describe("toUserRow", () => {
  it("maps profile into list row", () => {
    const row = toUserRow({
      id: "1",
      email: "a@b.com",
      display_name: "Alice",
      avatar_url: null,
      role: "user",
      status: "active",
      created_at: "2026-06-17T00:00:00Z",
      updated_at: "2026-06-17T00:00:00Z",
      last_login_at: null
    });

    expect(row.title).toBe("Alice");
    expect(row.subtitle).toBe("a@b.com");
  });
});

describe("filterUserRows", () => {
  it("filters rows by email fragment", () => {
    const rows = [
      {
        id: "1",
        title: "Alice",
        subtitle: "alice@test.com",
        role: "user",
        status: "active",
        createdAt: "",
        lastLoginAt: ""
      },
      {
        id: "2",
        title: "Bob",
        subtitle: "bob@test.com",
        role: "admin",
        status: "disabled",
        createdAt: "",
        lastLoginAt: ""
      }
    ];

    expect(filterUserRows(rows, { q: "alice", role: "all", status: "all" })).toHaveLength(1);
  });
});
