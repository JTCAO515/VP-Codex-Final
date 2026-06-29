import { createMockButlerPatch } from "@/lib/mock-ai/mockButler";
import type { CanvasPatch, TripState } from "@/lib/types/trip";

type FetchLike = typeof fetch;

interface RequestButlerPatchInput {
  currentTrip: TripState;
  env?: NodeJS.ProcessEnv | Record<string, string | undefined>;
  fetchImpl?: FetchLike;
  message: string;
}

export interface ButlerPatchResult {
  mode: "deepseek" | "mock";
  patch: CanvasPatch;
  fallbackReason?: string;
}

const DEFAULT_BASE_URL = "https://api.deepseek.com";
const DEFAULT_MODEL = "deepseek-v4-flash";
const allowedIntents = new Set<CanvasPatch["intent"]>(["create_trip", "adjust_trip", "add_alerts"]);

function readConfig(env: RequestButlerPatchInput["env"] = process.env) {
  const apiKey = env.DEEPSEEK_API_KEY?.trim() || env.AI_API_KEY?.trim();
  const baseUrl = (env.DEEPSEEK_BASE_URL?.trim() || env.AI_BASE_URL?.trim() || DEFAULT_BASE_URL).replace(/\/$/, "");
  const model = env.DEEPSEEK_MODEL?.trim() || env.AI_MODEL?.trim() || DEFAULT_MODEL;

  return { apiKey, baseUrl, model };
}

function buildSystemPrompt() {
  return [
    "You are VisePanda, an AI China travel butler for foreign travelers.",
    "Return only valid json for a live itinerary canvas patch.",
    'Example json shape: {"intent":"adjust_trip","assistantMessage":"...","reason":"...","tripSummary":{"confidence":"Refined"},"days":[],"butlerAlerts":[]}.',
    "The json shape must be: intent, assistantMessage, reason, optional tripSummary, optional days, optional butlerAlerts.",
    "Keep the plan practical for China travel: routing, visas, payment, booking, transport, food, stay areas, and fatigue.",
    "Use concise English. Do not include markdown.",
  ].join(" ");
}

function buildUserPrompt(message: string, currentTrip: TripState) {
  return JSON.stringify({
    userMessage: message,
    currentTrip,
    patchRules: {
      intent: ["create_trip", "adjust_trip", "add_alerts"],
      confidence: ["Draft", "Refined", "Ready to save"],
      pace: ["Light", "Balanced", "Relaxed", "Packed"],
      alertPriority: ["high", "medium", "low"],
      alertType: ["visa", "payment", "booking", "transport", "weather", "language", "risk", "emergency"],
    },
  });
}

function parseDeepSeekPatch(content: string): CanvasPatch {
  const parsed = JSON.parse(content) as Partial<CanvasPatch>;

  if (!parsed.intent || !allowedIntents.has(parsed.intent)) {
    throw new Error("DeepSeek response did not include a valid intent.");
  }

  if (typeof parsed.assistantMessage !== "string" || !parsed.assistantMessage.trim()) {
    throw new Error("DeepSeek response did not include assistantMessage.");
  }

  if (typeof parsed.reason !== "string" || !parsed.reason.trim()) {
    throw new Error("DeepSeek response did not include reason.");
  }

  return {
    intent: parsed.intent,
    assistantMessage: parsed.assistantMessage,
    reason: parsed.reason,
    tripSummary: parsed.tripSummary,
    days: Array.isArray(parsed.days) ? parsed.days : undefined,
    butlerAlerts: Array.isArray(parsed.butlerAlerts) ? parsed.butlerAlerts : undefined,
  };
}

function createFallback(message: string, currentTrip: TripState, fallbackReason?: string): ButlerPatchResult {
  return {
    fallbackReason,
    mode: "mock",
    patch: createMockButlerPatch(message, currentTrip),
  };
}

export async function requestButlerPatch({
  currentTrip,
  env = process.env,
  fetchImpl = fetch,
  message,
}: RequestButlerPatchInput): Promise<ButlerPatchResult> {
  const config = readConfig(env);
  const trimmed = message.trim();

  if (!trimmed) {
    return createFallback(message, currentTrip, "Empty message.");
  }

  if (!config.apiKey) {
    return createFallback(trimmed, currentTrip, "DeepSeek API key is not configured.");
  }

  try {
    const response = await fetchImpl(`${config.baseUrl}/chat/completions`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${config.apiKey}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        model: config.model,
        messages: [
          { role: "system", content: buildSystemPrompt() },
          { role: "user", content: buildUserPrompt(trimmed, currentTrip) },
        ],
        max_tokens: 2200,
        response_format: { type: "json_object" },
        temperature: 0.4,
      }),
    });

    if (!response.ok) {
      return createFallback(trimmed, currentTrip, `DeepSeek returned HTTP ${response.status}.`);
    }

    const data = await response.json();
    const content = data?.choices?.[0]?.message?.content;

    if (typeof content !== "string") {
      return createFallback(trimmed, currentTrip, "DeepSeek response did not include message content.");
    }

    return {
      mode: "deepseek",
      patch: parseDeepSeekPatch(content),
    };
  } catch (error) {
    const message = error instanceof Error ? error.message : "Unknown DeepSeek provider error.";

    return createFallback(trimmed, currentTrip, message);
  }
}
