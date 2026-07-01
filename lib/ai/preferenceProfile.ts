export interface UserPreferenceProfile {
  pace?: "light" | "balanced" | "packed";
  budget?: "economy" | "mid" | "luxury";
  party?: "solo" | "couple" | "family_with_kids" | "group";
  dietaryRestrictions: string[];
  cuisinePreferences: string[];
  interests: string[];
  profileConfidence: "low" | "medium" | "high";
}

export const emptyPreferenceProfile: UserPreferenceProfile = {
  dietaryRestrictions: [],
  cuisinePreferences: [],
  interests: [],
  profileConfidence: "low",
};

function unique(values: string[]): string[] {
  return Array.from(new Set(values.filter(Boolean))).slice(0, 8);
}

export function updatePreferenceProfile(
  current: UserPreferenceProfile | undefined,
  message: string,
): UserPreferenceProfile {
  const profile = current ?? emptyPreferenceProfile;
  const normalized = message.toLowerCase();
  const dietaryRestrictions = [...profile.dietaryRestrictions];
  const cuisinePreferences = [...profile.cuisinePreferences];
  const interests = [...profile.interests];
  let pace = profile.pace;
  let budget = profile.budget;
  let party = profile.party;

  if (/\b(less tiring|slow|slower|easy|light pace|too much walking|can't walk|cannot walk)\b/.test(normalized)) pace = "light";
  if (/\b(packed|as much as possible|full schedule)\b/.test(normalized)) pace = "packed";
  if (/\b(balanced|not too rushed)\b/.test(normalized)) pace = "balanced";

  if (/\b(student|cheap|budget|save money|lower cost|economy)\b/.test(normalized)) budget = "economy";
  if (/\b(mid[- ]?range|moderate|comfortable)\b/.test(normalized)) budget = "mid";
  if (/\b(luxury|five star|5 star|premium|high end)\b/.test(normalized)) budget = "luxury";

  if (/\b(with kids|with my family|family|children|toddler)\b/.test(normalized)) party = "family_with_kids";
  if (/\b(couple|honeymoon|partner|wife|husband|girlfriend|boyfriend)\b/.test(normalized)) party = "couple";
  if (/\b(group|friends)\b/.test(normalized)) party = "group";
  if (/\b(solo|alone|by myself)\b/.test(normalized)) party = "solo";

  if (/\b(vegetarian|vegan)\b/.test(normalized)) dietaryRestrictions.push("vegetarian");
  if (/\b(no pork|halal|muslim)\b/.test(normalized)) dietaryRestrictions.push("no pork / halal-aware");
  if (/\b(no seafood|seafood allergy|allergic to seafood)\b/.test(normalized)) dietaryRestrictions.push("no seafood");
  if (/\b(spicy|hotpot|sichuan|street food|foodie|local food)\b/.test(normalized)) cuisinePreferences.push("local food");
  if (/\b(no spicy|not spicy)\b/.test(normalized)) dietaryRestrictions.push("low spice");

  if (/\b(history|museum|temple|ancient|culture)\b/.test(normalized)) interests.push("history and culture");
  if (/\b(nature|mountain|lake|park|scenery)\b/.test(normalized)) interests.push("nature");
  if (/\b(food|restaurant|eat|cuisine|market)\b/.test(normalized)) interests.push("food");
  if (/\b(shopping|mall|market|souvenir)\b/.test(normalized)) interests.push("shopping");

  const signalCount = [pace, budget, party].filter(Boolean).length + dietaryRestrictions.length + cuisinePreferences.length + interests.length;

  return {
    pace,
    budget,
    party,
    dietaryRestrictions: unique(dietaryRestrictions),
    cuisinePreferences: unique(cuisinePreferences),
    interests: unique(interests),
    profileConfidence: signalCount >= 5 ? "high" : signalCount >= 2 ? "medium" : "low",
  };
}

export function preferenceProfileSummary(profile?: UserPreferenceProfile): string[] {
  if (!profile) return [];
  return [
    profile.pace ? `${profile.pace} pace` : "",
    profile.budget ? `${profile.budget} budget` : "",
    profile.party?.replaceAll("_", " ") ?? "",
    ...profile.dietaryRestrictions,
    ...profile.interests.slice(0, 3),
  ].filter(Boolean);
}
