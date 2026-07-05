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
// v0.3.19 addition: a *complete* JSON object can also be followed by trailing
// human-language commentary (verified with a real Kimi K2.6 response,
// 2026-07-05 — reasoning models sometimes self-narrate "Yes, this is valid
// JSON..." after the answer). If that trailing text happens to contain a
// comma, the backtrack loop below finds it before any comma inside the JSON
// body, cuts there, and can silently corrupt a field deep inside the object
// (a real repro: a day block's `"title":"Yu Garden"` was chopped down to
// nothing while the outer `days` array length looked unchanged). `findObjectEnd`
// closes this hole by scanning for the brace that actually matches the
// leading `{` and cutting there first — only genuinely truncated payloads
// (no matching end brace) fall through to the backtrack loop.
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

/**
 * Scan a string that starts with `{` for the brace that closes it (tracking
 * nested `{}`/`[]` and skipping over string contents/escapes). Returns the
 * index just past that closing brace, or -1 if the input ends before the
 * braces balance out (a genuine truncation, not trailing extra text).
 */
export function findObjectEnd(input: string): number {
  if (input[0] !== "{") return -1;
  const stack: string[] = [];
  let inString = false;
  let escaped = false;

  for (let i = 0; i < input.length; i++) {
    const ch = input[i];
    if (inString) {
      if (escaped) escaped = false;
      else if (ch === "\\") escaped = true;
      else if (ch === '"') inString = false;
      continue;
    }
    if (ch === '"') inString = true;
    else if (ch === "{" || ch === "[") stack.push(ch === "{" ? "}" : "]");
    else if (ch === "}" || ch === "]") {
      if (!stack.length || stack[stack.length - 1] !== ch) return -1; // mismatched — not a clean object
      stack.pop();
      if (stack.length === 0) return i + 1;
    }
  }
  return -1; // ran off the end still open — genuinely truncated
}

const MAX_BACKTRACK_STEPS = 24;

/**
 * Parse LLM JSON output tolerantly. Tries, in order: raw parse of the
 * extracted candidate, truncation repair, then progressively cutting back to
 * the previous structural boundary and re-repairing. Throws only when nothing
 * salvageable remains.
 */
export function safeParseLlmJson(raw: string): unknown {
  let candidate = extractJsonCandidate(raw);

  // A complete object followed by trailing chatter (e.g. a reasoning model
  // narrating "Yes, this is valid JSON") must be cut exactly at the matching
  // brace — before the backtrack loop below ever runs, since that loop hunts
  // for the last comma/brace and trailing text can contain one that shadows
  // a real comma inside the object, corrupting it instead of just trimming it.
  const objectEnd = findObjectEnd(candidate);
  if (objectEnd !== -1) {
    candidate = candidate.slice(0, objectEnd);
  }

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
