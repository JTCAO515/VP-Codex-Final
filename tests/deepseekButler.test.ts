import { describe, expect, it, vi } from "vitest";
import { requestButlerPatch } from "@/lib/ai/deepseekButler";
import { initialTripState } from "@/lib/mock-ai/mockButler";

function jsonResponse(body: unknown, init?: ResponseInit) {
  return new Response(JSON.stringify(body), {
    headers: { "content-type": "application/json" },
    status: init?.status ?? 200,
  });
}

describe("requestButlerPatch", () => {
  it("calls DeepSeek V4 Flash and returns a structured canvas patch", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse({
        choices: [
          {
            message: {
              content: JSON.stringify({
                intent: "adjust_trip",
                assistantMessage: "I updated the route with a calmer Beijing day.",
                reason: "Used DeepSeek V4 Flash to refine the itinerary.",
                tripSummary: { confidence: "Refined" },
              }),
            },
          },
        ],
      }),
    );

    const result = await requestButlerPatch({
      currentTrip: initialTripState,
      env: { DEEPSEEK_API_KEY: "test-key" },
      fetchImpl,
      message: "Make the Beijing day calmer",
    });

    expect(result.mode).toBe("deepseek");
    expect(result.patch.assistantMessage).toContain("calmer Beijing");
    expect(result.patch.tripSummary?.confidence).toBe("Refined");
    expect(fetchImpl).toHaveBeenCalledWith(
      "https://api.deepseek.com/chat/completions",
      expect.objectContaining({
        method: "POST",
        headers: expect.objectContaining({
          Authorization: "Bearer test-key",
        }),
      }),
    );
    const body = JSON.parse(fetchImpl.mock.calls[0][1].body);
    expect(body.model).toBe("deepseek-v4-flash");
    expect(body.response_format).toEqual({ type: "json_object" });
    expect(body.max_tokens).toBe(2200);
    expect(body.messages[0].content).toContain("json");
  });

  it("falls back to mock mode when DeepSeek is not configured", async () => {
    const result = await requestButlerPatch({
      currentTrip: initialTripState,
      env: {},
      fetchImpl: vi.fn(),
      message: "first time in China for 5 days",
    });

    expect(result.mode).toBe("mock");
    expect(result.patch.days?.some((day) => day.city === "Beijing")).toBe(true);
  });
});
