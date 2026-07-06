import { cookies } from 'next/headers';

import { AUTH_COOKIE_ACCESS_TOKEN } from '@/lib/auth';

export async function getServerAccessToken() {
  const cookieStore = await cookies();
  return cookieStore.get(AUTH_COOKIE_ACCESS_TOKEN)?.value ?? null;
}
