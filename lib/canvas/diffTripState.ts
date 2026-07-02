import type { ChangeDigestEntry, TripDay, TripState } from "@/lib/types/trip";

function dayContentKey(day: TripDay): string {
  return JSON.stringify({
    blocks: day.blocks.map((block) => [block.time, block.title, block.description]),
    food: day.food,
    stay: day.stay,
    transport: day.transport,
    note: day.note,
  });
}

function dayLabel(day: TripDay): string {
  const firstBlock = day.blocks[0];
  const detail = firstBlock ? firstBlock.title : day.city;
  return `${detail} (${day.city})`;
}

function alertKey(alert: TripState["alerts"][number]): string {
  return `${alert.type}:${alert.title}`;
}

/**
 * Pure day-level + alert-level diff between two TripState snapshots, used to
 * render the post-patch "Change Digest" card. Returns an empty array when the
 * patch made no visible change (the card should not render in that case).
 */
export function diffTripState(previous: TripState, next: TripState): ChangeDigestEntry[] {
  const entries: ChangeDigestEntry[] = [];

  const previousDays = new Map(previous.days.map((day) => [day.day, day]));
  const nextDays = new Map(next.days.map((day) => [day.day, day]));

  for (const [dayNumber, day] of nextDays) {
    const previousDay = previousDays.get(dayNumber);
    if (!previousDay) {
      entries.push({ kind: "added", dayNumber, label: `Day ${dayNumber} added · ${dayLabel(day)}` });
      continue;
    }
    if (dayContentKey(previousDay) !== dayContentKey(day)) {
      entries.push({ kind: "revised", dayNumber, label: `Day ${dayNumber} updated · ${dayLabel(day)}` });
    }
  }

  for (const [dayNumber, day] of previousDays) {
    if (!nextDays.has(dayNumber)) {
      entries.push({ kind: "removed", dayNumber, label: `Day ${dayNumber} removed · ${dayLabel(day)}` });
    }
  }

  const previousAlertKeys = new Set(previous.alerts.map(alertKey));
  for (const alert of next.alerts) {
    if (!previousAlertKeys.has(alertKey(alert))) {
      entries.push({ kind: "alert", label: `New reminder · ${alert.title}` });
    }
  }

  entries.sort((a, b) => (a.dayNumber ?? Number.MAX_SAFE_INTEGER) - (b.dayNumber ?? Number.MAX_SAFE_INTEGER));

  return entries;
}
