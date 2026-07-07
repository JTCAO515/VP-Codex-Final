// Offline evaluation tool for CanvasPatch quality (Issue #135). Not called
// from the request path — a CanvasPatch that already made it out of
// parseButlerPatch already passed that function's structural validation
// (has intent/assistantMessage/reason, days present when create_trip
// requires it). This scores the QUALITY of an already-valid patch: is it
// actually rich enough for the Trip UI to be useful, not just structurally
// parseable. Meant for manual spot-checks and test fixtures, the same way
// the real-provider verification scripts used throughout this project's
// migrations (CanvasPatch phases, staged generation) checked output quality
// by hand — this gives that same judgment a reusable, scriptable form.

import type { CanvasPatch, TripDay } from "@/lib/types/trip";

export interface PatchExecutabilityResult {
  /** 0-100. 100 means every check this patch is applicable for passed. */
  score: number;
  /** Human-readable problems found, empty when score is 100. */
  issues: string[];
}

function dayIssues(day: TripDay, expectBlocks: boolean): string[] {
  const issues: string[] = [];
  if (!day.city.trim()) {
    issues.push(`Day ${day.day} is missing a city.`);
  }
  if (expectBlocks && day.blocks.length === 0) {
    issues.push(`Day ${day.day} has no blocks.`);
  }
  day.blocks.forEach((block, index) => {
    if (!block.title.trim()) {
      issues.push(`Day ${day.day} block ${index} is missing a title.`);
    }
    if (!block.description.trim()) {
      issues.push(`Day ${day.day} block ${index} is missing a description.`);
    }
  });
  return issues;
}

/**
 * Evaluates whether a CanvasPatch has enough real content for the Trip UI to
 * usefully consume, beyond bare structural validity. Only checks what's
 * applicable to this patch — a reply with no `days` (e.g. add_alerts, or a
 * plain-text adjust_trip answer) is not penalized for missing itinerary
 * content it was never supposed to produce.
 */
export function evaluatePatchExecutability(patch: CanvasPatch): PatchExecutabilityResult {
  const issues: string[] = [];

  if (patch.intent === "create_trip") {
    if (!patch.days || patch.days.length === 0) {
      issues.push("create_trip patch has no days.");
    } else {
      // A skeleton (Trips staged generation, docs/planning/trips-staged-
      // generation-migration-plan.md) intentionally has empty blocks for
      // round 1 — that's not a quality problem, it's the contract.
      const expectBlocks = patch.generationStage !== "skeleton";
      patch.days.forEach((day) => issues.push(...dayIssues(day, expectBlocks)));
    }
  } else if (patch.days) {
    // adjust_trip (or any other intent) that did include days: same content
    // bar applies to whatever days are present, but no days at all is a
    // valid "this reply didn't change the itinerary" signal, not an issue.
    const expectBlocks = patch.generationStage !== "skeleton";
    patch.days.forEach((day) => issues.push(...dayIssues(day, expectBlocks)));
  }

  if (patch.assistantResponse) {
    if (!patch.assistantResponse.headline.trim()) {
      issues.push("assistantResponse is missing a headline.");
    }
    if (!patch.assistantResponse.nextStep.trim()) {
      issues.push("assistantResponse is missing a nextStep.");
    }
    // watchOut is optional by design (not every reply has a safety/caveat
    // worth flagging) so its absence is not counted as an issue.
  }

  const score = issues.length === 0 ? 100 : Math.max(0, 100 - issues.length * 15);

  return { score, issues };
}
