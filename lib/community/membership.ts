import type { MemberTier, MemberTierId } from "@/lib/community/types";

export const memberTiers: MemberTier[] = [
  {
    id: "bamboo-guest",
    name: "Bamboo Guest",
    shortName: "Guest",
    audience: "Browsing and trying VisePanda",
    requirement: "Guest mode",
    benefits: ["Read community posts", "Preview hot spots", "Use public trip inspiration"],
  },
  {
    id: "panda-explorer",
    name: "Panda Explorer",
    shortName: "Explorer",
    audience: "Signed-in travelers",
    requirement: "Create an account",
    benefits: ["Save trips", "Choose a panda avatar", "Post and comment in the community"],
  },
  {
    id: "silk-road-insider",
    name: "Silk Road Insider",
    shortName: "Insider",
    audience: "Active sharers",
    requirement: "Share useful trips, photos, or tips",
    benefits: ["Trusted badge", "Higher post visibility", "Early access to city guides"],
  },
  {
    id: "dragon-pass",
    name: "Dragon Pass",
    shortName: "Dragon",
    audience: "Future paid members",
    requirement: "Planned subscription tier",
    benefits: ["Premium route reviews", "Priority AI runs", "Member-only itineraries"],
  },
  {
    id: "visepanda-concierge",
    name: "VisePanda Concierge",
    shortName: "Concierge",
    audience: "High-touch travelers",
    requirement: "Planned managed travel tier",
    benefits: ["Human-assisted planning", "Booking support", "VIP community trust mark"],
  },
];

export function getMemberTier(id: MemberTierId): MemberTier {
  return memberTiers.find((tier) => tier.id === id) ?? memberTiers[0];
}
