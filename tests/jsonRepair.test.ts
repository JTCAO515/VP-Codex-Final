import { describe, expect, it } from "vitest";
import { extractJsonCandidate, findObjectEnd, repairTruncatedJson, safeParseLlmJson } from "@/lib/ai/jsonRepair";

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

describe("findObjectEnd", () => {
  it("finds the matching close brace for a complete object", () => {
    expect(findObjectEnd('{"a":1}')).toBe(7);
  });

  it("finds the matching brace even with trailing text after it", () => {
    const input = '{"a":[1,2]} and then some words, with a comma';
    expect(findObjectEnd(input)).toBe(11);
  });

  it("returns -1 for a genuinely truncated object (braces never balance)", () => {
    expect(findObjectEnd('{"a":[1,2')).toBe(-1);
  });

  it("ignores braces/brackets that appear inside string values", () => {
    const input = '{"a":"looks like { an object [ or array"}';
    expect(findObjectEnd(input)).toBe(input.length);
  });
});

describe("safeParseLlmJson", () => {
  it("regression: a complete object followed by chatter containing a comma must not corrupt an internal field (real Kimi K2.6 response shape, 2026-07-05)", () => {
    const raw =
      '{"intent":"create_trip","assistantMessage":"Planned","reason":"ok","days":[' +
      '{"day":1,"city":"Shanghai","blocks":[{"time":"Morning","title":"Bund"}]},' +
      '{"day":2,"city":"Shanghai","blocks":[{"time":"Morning","title":"Yu Garden"}]}]}\n\n' +
      "Check for validity:\n - All quotes are straight double quotes, no trailing commas.\n" +
      " - Proper nesting.\n\n Yes, this is valid JSON. I return exactly this.";
    const result = safeParseLlmJson(raw) as { days: Array<{ blocks: Array<{ title?: string }> }> };
    expect(result.days).toHaveLength(2);
    expect(result.days[0].blocks[0].title).toBe("Bund");
    expect(result.days[1].blocks[0].title).toBe("Yu Garden");
  });


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
