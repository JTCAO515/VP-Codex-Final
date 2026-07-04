import { describe, expect, it } from "vitest";
import { extractJsonCandidate, repairTruncatedJson, safeParseLlmJson } from "@/lib/ai/jsonRepair";

describe("extractJsonCandidate", () => {
  it("returns clean JSON unchanged", () => {
    expect(extractJsonCandidate('{"a":1}')).toBe('{"a":1}');
  });

  it("strips markdown fences", () => {
    expect(extractJsonCandidate('```json\n{"a":1}\n```')).toBe('{"a":1}');
  });

  it("strips leading chatter before the first brace", () => {
    expect(extractJsonCandidate('Sure! Here is the patch: {"a":1}')).toBe('{"a":1}');
  });
});

describe("repairTruncatedJson", () => {
  it("closes a string cut mid-value and the open braces", () => {
    expect(JSON.parse(repairTruncatedJson('{"a":"hello wor'))).toEqual({ a: "hello wor" });
  });

  it("closes an array cut mid-list", () => {
    expect(JSON.parse(repairTruncatedJson('{"a":[1,2'))).toEqual({ a: [1, 2] });
  });

  it("nulls a dangling key left before the cut", () => {
    expect(JSON.parse(repairTruncatedJson('{"a":1,"b":'))).toEqual({ a: 1, b: null });
  });

  it("drops a trailing key that never got a colon", () => {
    expect(JSON.parse(repairTruncatedJson('{"a":1,"b"'))).toEqual({ a: 1 });
  });

  it("drops a trailing comma", () => {
    expect(JSON.parse(repairTruncatedJson('{"a":1,'))).toEqual({ a: 1 });
  });
});

describe("safeParseLlmJson", () => {
  it("parses valid JSON directly", () => {
    expect(safeParseLlmJson('{"intent":"adjust_trip"}')).toEqual({ intent: "adjust_trip" });
  });

  it("recovers a patch truncated mid-string (the production failure shape)", () => {
    const truncated =
      '{"intent":"create_trip","assistantMessage":"Planned your Beijing days","reason":"ok","days":[{"day":1,"city":"Beijing","note":"Start at an easy pa';
    const result = safeParseLlmJson(truncated) as Record<string, unknown>;
    expect(result.intent).toBe("create_trip");
    expect(result.assistantMessage).toBe("Planned your Beijing days");
    expect(Array.isArray(result.days)).toBe(true);
  });

  it("recovers fenced output with trailing chatter", () => {
    expect(safeParseLlmJson('```json\n{"a":1}\n```\nHope this helps!')).toEqual({ a: 1 });
  });

  it("backtracks past an unrecoverable tail to the last structural boundary", () => {
    const messy = '{"a":1,"b":{"c":[1,2,{"d":"x';
    const result = safeParseLlmJson(messy) as Record<string, unknown>;
    expect(result.a).toBe(1);
  });

  it("throws when nothing salvageable remains", () => {
    expect(() => safeParseLlmJson("complete nonsense with no json at all")).toThrow();
  });
});
