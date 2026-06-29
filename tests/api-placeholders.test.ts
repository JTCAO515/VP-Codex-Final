import { describe, expect, it } from "vitest";
import { POST as postChat } from "@/app/api/chat/route";
import { GET as getExplore } from "@/app/api/explore/route";
import { GET as getTools } from "@/app/api/tools/route";
import { GET as getTrips } from "@/app/api/trips/route";

describe("first-stage API routes", () => {
  it("returns a mock canvas patch from chat", async () => {
    const response = await postChat(
      new Request("http://localhost/api/chat", {
        method: "POST",
        body: JSON.stringify({ message: "first time in China for 5 days" }),
      }),
    );

    const body = await response.json();

    expect(body.ok).toBe(true);
    expect(body.mode).toBe("mock");
    expect(body.patch.days.some((day: { city: string }) => day.city === "Beijing")).toBe(true);
  });

  it("returns placeholder status for reserved routes", async () => {
    const routes = [getTrips, getExplore, getTools];

    for (const route of routes) {
      const response = await route();
      const body = await response.json();

      expect(body.ok).toBe(true);
      expect(body.status).toBe("placeholder");
    }
  });
});
