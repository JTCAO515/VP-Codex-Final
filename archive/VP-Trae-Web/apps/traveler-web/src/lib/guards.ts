import { redirect } from 'next/navigation';

import { getServerAccessToken } from '@/lib/server-auth';

export async function requireTravelerSession(nextPath: string) {
  const accessToken = await getServerAccessToken();

  if (!accessToken) {
    redirect(`/login?next=${encodeURIComponent(nextPath)}`);
  }

  return accessToken;
}
