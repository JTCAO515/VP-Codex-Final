// Tolerant JSON extraction + truncation repair for LLM output (v0.3.17).
//
// Root cause this fixes: providers occasionally return JSON cut off mid-string
// when the completion hits max_tokens ("Unterminated string in JSON at
// position N" was observed in production on 2026-07-04, silently sinking an
// otherwise-healthy provider into the mock fallback). Instead of letting
// JSON.parse throw away the whole answer, we (1) strip markdown fences and
// leading chatter, (2) close any open string and bracket stack at the cut
// point, and (3) if that still fails, walk backwards to the last structural
// boundary and retry — salvaging the longest valid prefix of the patch.
//
// This is deliberately dependency-free and deterministic so it is trivial to
// unit test (tests/jsonRepair.test.ts).

/** Strip markdown fences / surrounding chatter and return the JSON candidate. */
export function extractJsonCandidate(raw: string): string {
  const trimmed = raw.trim();

  const fenceMatch = trimmed.match(/```(?:json)?\s*([\s\S]*?)(?:```|$)/i);
  const body = fenceMatch ? fenceMatch[1].trim() : trimmed;

  const start = body.indexOf("{");
  if (start === -1) return body;

  // Find the matching end by scanning; if the payload is truncated there may
  // be no matching close brace — return everything from the first brace and
  // let the repair pass handle it.
  return body.slice(start);
}

/**
 * Close an open string and any unbalanced brackets so a truncated JSON
 * document becomes parseable. Also trims dangling commas/colons/keys left at
 * the cut point.
 */
export function repairTruncatedJson(input: string): string {
  const stack: string[] = [];
  let inString = false;
  let escaped = false;

  for (const ch of input) {
    if (inString) {
      if (escaped) {
        escaped = false;
      } else if (ch === "\\") {
        escaped = true;
      } else if (ch === '"') {
        inString = false;
      }
      continue;
    }
    if (ch === '"') inString = true;
    else if (ch === "{") stack.push("}");
    else if (ch === "[") stack.push("]");
    else if (ch === "}" || ch === "]") {
      if (stack.length && stack[stack.length - 1] === ch) stack.pop();
    }
  }

  let repaired = input;
  if (escaped) repaired = repaired.slice(0, -1); // lone trailing backslash
  if (inString) repaired += '"';

  // Dangling artifacts at the cut point, in the order they can stack:
  // `"key":` -> null value; `"key"` (no colon yet) -> drop it; trailing comma -> drop.
  repaired = repaired.replace(/:\s*$/, ":null");
  repaired = repaired.replace(/,\s*"(?:[^"\\]|\\.)*"\s*$/, "");
  repaired = repaired.replace(/,\s*$/, "");

  while (stack.length) repaired += stack.pop();
  return repaired;
}

const MAX_BACKTRACK_STEPS = 24;

/**
 * Parse LLM JSON output tolerantly. Tries, in order: raw parse of the
 * extracted candidate, truncation repair, then progressively cutting back to
 * the previous structural boundary and re-repairing. Throws only when nothing
 * salvageable remains.
 */
export function safeParseLlmJson(raw: string): unknown {
  const candidate = extractJsonCandidate(raw);

  try {
    return JSON.parse(candidate);
  } catch {
    // fall through to repair
  }

  let work = candidate;
  for (let step = 0; step < MAX_BACKTRACK_STEPS; step++) {
    try {
      return JSON.parse(repairTruncatedJson(work));
    } catch {
      const cut = Math.max(work.lastIndexOf(","), work.lastIndexOf("{"), work.lastIndexOf("["));
      if (cut <= 0) break;
      work = work.slice(0, cut);
    }
  }

  throw new Error("Model output was not valid JSON even after truncation repair.");
}
