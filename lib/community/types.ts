export type CommunityPostType = "trip" | "photo" | "tip" | "question";

export type MemberTierId =
  | "bamboo-guest"
  | "panda-explorer"
  | "silk-road-insider"
  | "dragon-pass"
  | "visepanda-concierge";

export interface CommunityAuthor {
  id: string;
  displayName: string;
  avatarInitials: string;
  avatarId?: string;
  country: string;
  memberTierId: MemberTierId;
}

export interface CommunityPost {
  id: string;
  type: CommunityPostType;
  author: CommunityAuthor;
  title: string;
  body: string;
  cities: string[];
  daysCount?: number;
  coverEmoji: string;
  likes: number;
  comments: number;
  saved?: number;
  createdAt: string;
  tags: string[];
  tripId?: string;
}

export interface CommunityPhoto {
  id: string;
  author: CommunityAuthor;
  caption: string;
  city: string;
  locationName: string;
  emoji: string;
  likes: number;
  createdAt: string;
}

export interface CityHotSpot {
  id: string;
  cityId: string;
  cityName: string;
  name: string;
  chinese: string;
  category: "attraction" | "food" | "hidden";
  rating: number;
  reviewCount: number;
  tip: string;
  emoji: string;
}

export interface CommunityComment {
  id: string;
  postId: string;
  author: CommunityAuthor;
  body: string;
  createdAt: string;
}

export interface MemberTier {
  id: MemberTierId;
  name: string;
  shortName: string;
  audience: string;
  requirement: string;
  benefits: string[];
}
