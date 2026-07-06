import type { AdminAccessInput } from "./types";

export const ADMIN_AUTH_COOKIE = "vp-admin-auth";

export function canAccessAdmin(input: AdminAccessInput) {
  return input.role === "admin" && input.status === "active";
}
