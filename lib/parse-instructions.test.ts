import { describe, it, expect } from "vitest";
import { parseAssistantReply } from "./parse-instructions";

describe("parseAssistantReply", () => {
  it("returns the trimmed text unchanged when there is no instruction block", () => {
    const result = parseAssistantReply("  Hello, where would you like to go?  ");
    expect(result).toEqual({
      chatText: "Hello, where would you like to go?",
      instructions: null,
    });
  });

  it("extracts a valid instruction block and strips it from the chat text", () => {
    const raw = [
      "Here is your 1-day Beijing plan.",
      "",
      "```json-trip-instructions",
      JSON.stringify({ days: [{ day: 1, action: "upsert", data: { city: "Beijing" } }] }),
      "```",
    ].join("\n");

    const result = parseAssistantReply(raw);

    expect(result.chatText).toBe("Here is your 1-day Beijing plan.");
    expect(result.instructions).toEqual({
      days: [{ day: 1, action: "upsert", data: { city: "Beijing" } }],
      rails: undefined,
      summary: undefined,
    });
  });

  it("extracts a summary field alongside days/rails", () => {
    const raw = [
      "Here you go.",
      "```json-trip-instructions",
      JSON.stringify({ summary: { route: ["Beijing", "Shanghai"], days: 5 } }),
      "```",
    ].join("\n");

    const result = parseAssistantReply(raw);

    expect(result.instructions).toEqual({
      days: undefined,
      rails: undefined,
      summary: { route: ["Beijing", "Shanghai"], days: 5 },
    });
  });

  it("falls back to null instructions when the JSON is malformed, but still strips the block", () => {
    const raw = ["Here is your plan.", "```json-trip-instructions", "{ this is not valid json", "```"].join("\n");

    const result = parseAssistantReply(raw);

    expect(result.chatText).toBe("Here is your plan.");
    expect(result.instructions).toBeNull();
  });

  it("falls back to null instructions when the parsed JSON is not an object", () => {
    const raw = ["Some text.", "```json-trip-instructions", "[1, 2, 3]", "```"].join("\n");

    const result = parseAssistantReply(raw);

    expect(result.chatText).toBe("Some text.");
    expect(result.instructions).toBeNull();
  });

  it("ignores a summary field that is not a plain object", () => {
    const raw = [
      "Some text.",
      "```json-trip-instructions",
      JSON.stringify({ summary: [1, 2, 3] }),
      "```",
    ].join("\n");

    const result = parseAssistantReply(raw);

    expect(result.instructions?.summary).toBeUndefined();
  });

  it("treats missing days/rails/summary as undefined rather than throwing", () => {
    const raw = ["No changes this turn.", "```json-trip-instructions", "{}", "```"].join("\n");

    const result = parseAssistantReply(raw);

    expect(result.instructions).toEqual({ days: undefined, rails: undefined, summary: undefined });
  });
});
