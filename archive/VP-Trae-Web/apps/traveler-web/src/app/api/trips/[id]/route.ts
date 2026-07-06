import { cookies } from 'next/headers';
import { NextResponse } from 'next/server';

import { AUTH_COOKIE_ACCESS_TOKEN } from '@/lib/auth';
import { proxyApi } from '@/lib/api';

export async function GET(_: Request, ctx: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await ctx.params;
    const accessToken = (await cookies()).get(AUTH_COOKIE_ACCESS_TOKEN)?.value;
    return proxyApi(`/trips/${id}`, {
      method: 'GET',
      headers: accessToken ? ({ authorization: `Bearer ${accessToken}` } as Record<string, string>) : {},
    });
  } catch (error) {
    return NextResponse.json(
      {
        success: false,
        data: null,
        requestId: 'traveler-web-trip-detail',
        error: {
          code: 'UPSTREAM_ERROR',
          message: error instanceof Error ? error.message : 'Failed to reach upstream API',
        },
      },
      { status: 502 },
    );
  }
}
