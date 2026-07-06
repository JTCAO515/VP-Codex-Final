export const AUTH_COOKIE_ACCESS_TOKEN = 'vp_access_token';
export const AUTH_COOKIE_ROLE = 'vp_role';

export type AuthUser = {
  id: string;
  email: string;
  role: string;
  status: string;
};

export type AuthSessionPayload = {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  sessionId: string;
  user: AuthUser;
};
