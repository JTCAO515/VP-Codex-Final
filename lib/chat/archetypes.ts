export type TripArchetypeId = "first-china-10-days" | "foodie-china" | "history-nature";

export interface TripArchetype {
  id: TripArchetypeId;
  title: string;
  subtitle: string;
  prompt: string;
}

export const TRIP_ARCHETYPES: TripArchetype[] = [
  {
    id: "first-china-10-days",
    title: "First China 10 Days Essentials",
    subtitle: "Beijing, Xi'an, Shanghai with practical arrival setup.",
    prompt:
      "Start a First China 10 Days Essentials independent trip for a foreign FIT traveler. Build a suggested draft canvas with Beijing, Xi'an, and Shanghai, balanced pacing, convenient hotel areas, transit notes, payment setup, translation support, and first-time China booking reminders.",
  },
  {
    id: "foodie-china",
    title: "Foodie China",
    subtitle: "Regional food, markets, translation help, and low-friction routes.",
    prompt:
      "Start a Foodie China independent trip for a foreign FIT traveler. Build a suggested draft canvas focused on Chengdu, Shanghai, and Guangzhou food neighborhoods, reliable restaurant areas, market visits, menu translation needs, payment setup, and balanced pacing.",
  },
  {
    id: "history-nature",
    title: "History & Nature",
    subtitle: "Imperial cities, landscapes, booking windows, and recovery time.",
    prompt:
      "Start a History & Nature independent China trip for a foreign FIT traveler. Build a suggested draft canvas with Beijing, Xi'an, Zhangjiajie, and Guilin, including booking constraints, transit logic, hotel area suggestions, translation support, and rest buffers.",
  },
];

export function getTripArchetype(id: string | null): TripArchetype | undefined {
  return TRIP_ARCHETYPES.find((archetype) => archetype.id === id);
}
