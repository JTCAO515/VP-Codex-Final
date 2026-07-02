import type { ButlerToolContext, ButlerToolPoi } from "@/lib/ai/toolContext";
import type { CanvasPatch, TripBlock } from "@/lib/types/trip";

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

export function applyToolContextToPatch(patch: CanvasPatch, toolContext?: ButlerToolContext): CanvasPatch {
  if (!toolContext || !patch.days?.length || toolContext.pois.length === 0) return patch;

  return {
    ...patch,
    days: patch.days.map((day) => ({
      ...day,
      blocks: day.blocks.map((block) => {
        const matchedPoi = toolContext.pois.find((poi) => matchesPoi(block, poi));
        return matchedPoi ? enrichBlock(block, matchedPoi) : block;
      }),
    })),
  };
}
