import type { UserProfile, UserRole, UserStatus } from "./types";

export type UserUpdatePayload = Partial<
  Pick<UserProfile, "display_name" | "avatar_url" | "role" | "status">
>;

const VALID_ROLES: UserRole[] = ["user", "admin"];
const VALID_STATUSES: UserStatus[] = ["pending", "active", "disabled"];

function normalizeOptionalText(value: unknown) {
  if (value === null) {
    return null;
  }

  if (typeof value !== "string") {
    return undefined;
  }

  const normalized = value.trim();
  return normalized.length > 0 ? normalized : null;
}

export function normalizeUpdatePayload(input: Record<string, unknown>): UserUpdatePayload {
  const next: UserUpdatePayload = {};

  if (typeof input.role === "string" && VALID_ROLES.includes(input.role as UserRole)) {
    next.role = input.role as UserRole;
  }

  if (typeof input.status === "string" && VALID_STATUSES.includes(input.status as UserStatus)) {
    next.status = input.status as UserStatus;
  }

  const displayName = normalizeOptionalText(input.display_name);
  if (displayName !== undefined) {
    next.display_name = displayName;
  }

  const avatarUrl = normalizeOptionalText(input.avatar_url);
  if (avatarUrl !== undefined) {
    next.avatar_url = avatarUrl;
  }

  return next;
}
