import type {
  ExploreAttraction,
  ExploreCity,
  ExploreFoodSpot,
  ExploreRichMeta,
  ExploreStay,
} from "@/lib/explore/types";
import type { BookingCandidate, CanvasPatch, TripBlock, TripDay, TripState } from "@/lib/types/trip";

export type ExploreAddToTripCategory = "attraction" | "food" | "stay";

export interface ExploreAddToTripPayload {
  id: string;
  name: string;
  cityId: string;
  cityName: string;
  category: ExploreAddToTripCategory;
  context?: string;
  address?: string;
  phone?: string;
  openingHours?: string;
  mapUrl?: string;
  sourceLabel?: string;
  coordinates?: {
    lat: number;
    lng: number;
  };
  bookingCandidates?: BookingCandidate[];
}

type ExploreAddToTripItem = (ExploreAttraction | ExploreFoodSpot | ExploreStay) & ExploreRichMeta;

function bookingKindForCategory(category: ExploreAddToTripCategory): BookingCandidate["kind"] {
  if (category === "food") return "restaurant";
  if (category === "stay") return "hotel";
  return "ticket";
}

function bookingLabelForCategory(category: ExploreAddToTripCategory) {
  if (category === "food") return "Restaurant planning reference";
  if (category === "stay") return "Hotel area planning reference";
  return "Ticket planning reference";
}

function buildMapUrl(name: string, cityName: string, location?: ExploreRichMeta["location"]) {
  if (location) {
    const params = new URLSearchParams({
      position: `${location.lng},${location.lat}`,
      name,
    });
    return `https://uri.amap.com/marker?${params.toString()}`;
  }

  const params = new URLSearchParams({ query: `${cityName} ${name}` });
  return `https://ditu.amap.com/search?${params.toString()}`;
}

export function buildExploreAddToTripPayload(
  item: ExploreAddToTripItem,
  city: ExploreCity,
  category: ExploreAddToTripCategory,
  context?: string,
): ExploreAddToTripPayload {
  const provider = item.sourceLabel ?? "Explore";
  const bookingKind = bookingKindForCategory(category);

  return {
    id: item.id,
    name: item.name,
    cityId: item.cityId,
    cityName: city.name,
    category,
    context,
    phone: item.tel,
    openingHours: item.openHours,
    mapUrl: buildMapUrl(item.name, city.name, item.location),
    sourceLabel: provider,
    coordinates: item.location,
    bookingCandidates: [
      {
        id: `${item.id}-${bookingKind}`,
        kind: bookingKind,
        label: bookingLabelForCategory(category),
        provider,
        status: "info-only",
        note: "Added from Explore for planning context only; availability, inventory, payment, and checkout are not verified yet.",
        priceHint: item.pricePerPerson ? `Approx. ¥${item.pricePerPerson}/person` : undefined,
      },
    ],
  };
}

export function encodeExploreAddToTripPayload(payload: ExploreAddToTripPayload) {
  return JSON.stringify(payload);
}

export function parseExploreAddToTripPayload(raw: string | null): ExploreAddToTripPayload | null {
  if (!raw) return null;

  try {
    const parsed = JSON.parse(raw) as Partial<ExploreAddToTripPayload>;
    if (!parsed.id || !parsed.name || !parsed.cityName || !parsed.category) return null;
    if (!["attraction", "food", "stay"].includes(parsed.category)) return null;
    return parsed as ExploreAddToTripPayload;
  } catch {
    return null;
  }
}

function normalize(value: string) {
  return value
    .toLowerCase()
    .replace(/[（(].*?[）)]/g, "")
    .replace(/[^a-z0-9\u4e00-\u9fff]+/g, " ")
    .trim();
}

function matchesPayload(block: TripBlock, payload: ExploreAddToTripPayload) {
  const blockTitle = normalize(block.title);
  const payloadName = normalize(payload.name);
  if (!blockTitle || !payloadName) return false;
  return blockTitle.includes(payloadName) || payloadName.includes(blockTitle);
}

function enrichBlock(block: TripBlock, payload: ExploreAddToTripPayload): TripBlock {
  return {
    ...block,
    address: block.address ?? payload.address,
    phone: block.phone ?? payload.phone,
    openingHours: block.openingHours ?? payload.openingHours,
    mapUrl: block.mapUrl ?? payload.mapUrl,
    sourceLabel: block.sourceLabel ?? payload.sourceLabel,
    coordinates: block.coordinates ?? payload.coordinates,
    bookingCandidates: block.bookingCandidates ?? payload.bookingCandidates,
  };
}

function createExploreBlock(payload: ExploreAddToTripPayload): TripBlock {
  const context = payload.context ? ` (${payload.context})` : "";

  return {
    time: "Flexible",
    title: payload.name,
    description: `Added from Explore as a ${payload.category}${context} candidate for ${payload.cityName}. VisePanda can rebalance it into a specific time block.`,
    address: payload.address,
    phone: payload.phone,
    openingHours: payload.openingHours,
    mapUrl: payload.mapUrl,
    sourceLabel: payload.sourceLabel,
    coordinates: payload.coordinates,
    bookingCandidates: payload.bookingCandidates,
  };
}

function cloneDays(days: TripDay[]) {
  return days.map((day) => ({
    ...day,
    blocks: day.blocks.map((block) => ({ ...block })),
    food: [...day.food],
  }));
}

export function applyExplorePoiToPatch(
  patch: CanvasPatch,
  currentTrip: TripState,
  payload: ExploreAddToTripPayload | null,
): CanvasPatch {
  if (!payload) return patch;

  const sourceDays = patch.days?.length ? patch.days : currentTrip.days;
  if (sourceDays.length === 0) return patch;

  let foundMatch = false;
  const days = cloneDays(sourceDays).map((day) => ({
    ...day,
    blocks: day.blocks.map((block) => {
      if (!matchesPayload(block, payload)) return block;
      foundMatch = true;
      return enrichBlock(block, payload);
    }),
  }));

  if (!foundMatch) {
    const targetIndex = days.findIndex((day) => normalize(day.city) === normalize(payload.cityName));
    const resolvedIndex = targetIndex >= 0 ? targetIndex : 0;
    days[resolvedIndex] = {
      ...days[resolvedIndex],
      status: "revised",
      blocks: [...days[resolvedIndex].blocks, createExploreBlock(payload)],
    };
  }

  return {
    ...patch,
    days,
    reason: foundMatch
      ? `${patch.reason} Explore POI details were attached to the matching trip block.`
      : `${patch.reason} Explore POI was added as a flexible candidate block.`,
  };
}
