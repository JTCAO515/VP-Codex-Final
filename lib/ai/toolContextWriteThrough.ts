import type { ButlerToolContext, ButlerToolPoi } from "@/lib/ai/toolContext";
import type { AssistantResponse, CanvasPatch, ExploreRef, TripBlock } from "@/lib/types/trip";

function normalize(value: string) {
  return value
    .toLowerCase()
    .replace(/[（(].*?[）)]/g, "")
    .replace(/[^a-z0-9\u4e00-\u9fff]+/g, " ")
    .trim();
}

function matchesPoi(block: TripBlock, poi: ButlerToolPoi) {
  const blockTitle = normalize(block.title);
  const poiName = normalize(poi.name);
  if (!blockTitle || !poiName) return false;
  return blockTitle.includes(poiName) || poiName.includes(blockTitle);
}

function enrichBlock(block: TripBlock, poi: ButlerToolPoi): TripBlock {
  return {
    ...block,
    address: block.address ?? poi.address,
    phone: block.phone ?? poi.phone,
    openingHours: block.openingHours ?? poi.openHours,
    mapUrl: block.mapUrl ?? poi.mapUrl,
    sourceLabel: block.sourceLabel ?? poi.sourceLabel,
    coordinates: block.coordinates ?? poi.coordinates,
    bookingCandidates: block.bookingCandidates ?? poi.bookingCandidates,
  };
}

function toNumber(value: string | undefined): number | undefined {
  if (!value) return undefined;
  const n = Number(value);
  return Number.isFinite(n) ? n : undefined;
}

function poiMentionedInText(poi: ButlerToolPoi, text: string): boolean {
  const poiName = normalize(poi.name);
  if (!poiName) return false;
  return text.includes(poiName);
}

/**
 * Chat↔Explore bridge (Issue #50): finds which real toolContext POIs the
 * Butler's answer text actually names, so the client can render them as
 * clickable Explore refs. Matching only ever narrows toolContext down to a
 * subset — it never invents a POI the model didn't already have real data
 * for, which is what keeps this anti-hallucination (mirrors ADR-120).
 */
export function buildExploreRefs(
  assistantResponse: AssistantResponse | undefined,
  toolContext: ButlerToolContext | undefined,
): ExploreRef[] {
  if (!assistantResponse || !toolContext || toolContext.pois.length === 0) return [];

  const haystack = normalize(
    [assistantResponse.headline, assistantResponse.body, ...assistantResponse.highlights].join(" "),
  );
  if (!haystack) return [];

  return toolContext.pois
    .filter((poi) => poiMentionedInText(poi, haystack))
    .map((poi) => ({
      amapPoiId: poi.id,
      name: poi.name,
      cityId: toolContext.cityId,
      category: toolContext.category,
      rating: toNumber(poi.rating),
      pricePerPerson: toNumber(poi.pricePerPerson),
    }));
}

export function applyToolContextToPatch(patch: CanvasPatch, toolContext?: ButlerToolContext): CanvasPatch {
  const exploreRefs = buildExploreRefs(patch.assistantResponse, toolContext);
  const withRefs: CanvasPatch =
    exploreRefs.length > 0 && patch.assistantResponse
      ? { ...patch, assistantResponse: { ...patch.assistantResponse, exploreRefs } }
      : patch;

  if (!toolContext || !withRefs.days?.length || toolContext.pois.length === 0) return withRefs;

  return {
    ...withRefs,
    // Defensive ?? [] — parse-boundary normalization (butlerPrompt.normalizeDays)
    // should guarantee blocks exists, but this ran against raw LLM output in
    // production once and crashed; cheap insurance stays (v0.3.18).
    days: withRefs.days.map((day) => ({
      ...day,
      blocks: (day.blocks ?? []).map((block) => {
        const matchedPoi = toolContext.pois.find((poi) => matchesPoi(block, poi));
        return matchedPoi ? enrichBlock(block, matchedPoi) : block;
      }),
    })),
  };
}
