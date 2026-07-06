import { describe, expect, it } from "vitest";
import { summarizeUsers } from "../lib/dashboard";

describe("summarizeUsers", () => {
  it("counts total, active, pending and disabled users", () => {
    const result = summarizeUsers([
      { status: "active" },
      { status: "active" },
      { status: "pending" },
      { status: "disabled" }
    ] as Array<{ status: "active" | "pending" | "disabled" }>);

    expect(result).toEqual({
      total: 4,
      active: 2,
      pending: 1,
      disabled: 1
    });
  });
});
