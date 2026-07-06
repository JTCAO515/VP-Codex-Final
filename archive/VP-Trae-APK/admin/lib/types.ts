export type UserRole = "user" | "admin";

export type UserStatus = "pending" | "active" | "disabled";

export type UserProfile = {
  id: string;
  email: string;
  display_name: string | null;
  avatar_url: string | null;
  role: UserRole;
  status: UserStatus;
  created_at: string;
  updated_at: string;
  last_login_at: string | null;
};

export type AdminAccessInput = Pick<UserProfile, "role" | "status">;
