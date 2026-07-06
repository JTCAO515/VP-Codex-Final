import type { UserRow } from "../components/user-table";
import type { UserProfile, UserRole, UserStatus } from "./types";

function formatDateTime(value: string | null) {
  if (!value) {
    return "从未登录";
  }

  return new Intl.DateTimeFormat("zh-CN", {
    dateStyle: "medium",
    timeStyle: "short",
    timeZone: "UTC"
  }).format(new Date(value));
}

export function toUserRow(profile: UserProfile): UserRow {
  return {
    id: profile.id,
    title: profile.display_name?.trim() || "Unnamed User",
    subtitle: profile.email,
    role: profile.role,
    status: profile.status,
    createdAt: formatDateTime(profile.created_at),
    lastLoginAt: formatDateTime(profile.last_login_at)
  };
}

type UserRowFilterInput = {
  q: string;
  role: UserRole | "all";
  status: UserStatus | "all";
};

export function filterUserRows(rows: UserRow[], input: UserRowFilterInput) {
  const query = input.q.trim().toLowerCase();

  return rows.filter((row) => {
    const matchQuery =
      query === "" || row.title.toLowerCase().includes(query) || row.subtitle.toLowerCase().includes(query);
    const matchRole = input.role === "all" || row.role === input.role;
    const matchStatus = input.status === "all" || row.status === input.status;

    return matchQuery && matchRole && matchStatus;
  });
}
