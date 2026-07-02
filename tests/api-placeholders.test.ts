import { afterEach, describe, expect, it, vi } from "vitest";
import { POST as postChat } from "@/app/api/chat/route";
import { GET as getExplore } from "@/app/api/explore/route";
import { GET as getTools } from "@/app/api/tools/route";
import { GET as getTrips } from "@/app/api/trips/route";

describe("first-stage API routes", () => {
  afterEach(() => {
    vi.unstubAllEnvs();
    vi.restoreAllMocks();
  });

  it("returns a mock canvas patch from chat", async () => {
    vi.stubEnv("DEEPSEEK_API_KEY", "");

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

  it("returns a DeepSeek canvas patch when the provider is configured", async () => {
    vi.stubEnv("DEEPSEEK_API_KEY", "test-key");
    vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response(
        JSON.stringify({
          choices: [
            {
              message: {
                content: JSON.stringify({
                  intent: "add_alerts",
                  assistantMessage: "I added the visa reminder.",
                  reason: "DeepSeek added a practical reminder.",
                  suggestions: ["Add payment setup?", "Keep the pace lighter?"],
                  butlerAlerts: [
                    {
                      type: "visa",
                      priority: "high",
                      title: "Check entry rules before booking",
                      body: "Rules depend on nationality and trip length.",
                      action: "Review visa checklist",
                    },
                  ],
                }),
              },
            },
          ],
        }),
        { headers: { "content-type": "application/json" } },
      ),
    );

    const response = await postChat(
      new Request("http://localhost/api/chat", {
        method: "POST",
        body: JSON.stringify({ message: "Make Day 1 less tiring" }),
      }),
    );

    const body = await response.json();

    expect(body.ok).toBe(true);
    expect(body.mode).toBe("deepseek");
    expect(body.patch.butlerAlerts[0].type).toBe("visa");
    expect(body.suggestions).toEqual(["Add payment setup?", "Keep the pace lighter?"]);
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
