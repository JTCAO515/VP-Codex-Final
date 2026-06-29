import type { TripInstructionBlock } from "./types";

const INSTRUCTION_FENCE = /```json-trip-instructions\s*([\s\S]*?)```/;

export interface ParsedReply {
  chatText: string;
  instructions: TripInstructionBlock | null;
}

export function parseAssistantReply(raw: string): ParsedReply {
  const match = raw.match(INSTRUCTION_FENCE);
  if (!match) {
    return { chatText: raw.trim(), instructions: null };
  }

  const chatText = raw.replace(match[0], "").trim();
  const jsonText = match[1].trim();

  try {
    const parsed = JSON.parse(jsonText);
    if (typeof parsed !== "object" || parsed === null || Array.isArray(parsed)) {
      return { chatText, instructions: null };
    }
    const days = Array.isArray(parsed.days) ? parsed.days : undefined;
    const rails = Array.isArray(parsed.rails) ? parsed.rails : undefined;
    const summary =
      typeof parsed.summary === "object" && parsed.summary !== null && !Array.isArray(parsed.summary)
        ? parsed.summary
        : undefined;
    return { chatText, instructions: { days, rails, summary } };
  } catch {
    return { chatText, instructions: null };
  }
}
