import { cookies } from 'next/headers';
import { NextResponse } from 'next/server';

import { AUTH_COOKIE_ACCESS_TOKEN } from '@/lib/auth';
import { proxyApi } from '@/lib/api';

function getAuthorizationHeader(token: string | undefined) {
  return token ? ({ authorization: `Bearer ${token}` } as Record<string, string>) : {};
}

export async function GET() {
  try {
    const accessToken = (await cookies()).get(AUTH_COOKIE_ACCESS_TOKEN)?.value;
    return proxyApi('/trips', {
      method: 'GET',
      headers: getAuthorizationHeader(accessToken),
    });
  } catch (error) {
    return NextResponse.json(
      {
        success: false,
        data: null,
        requestId: 'traveler-web-trips-list',
        error: {
          code: 'UPSTREAM_ERROR',
          message: error instanceof Error ? error.message : 'Failed to reach upstream API',
        },
      },
      { status: 502 },
    );
  }
}

export async function POST(req: Request) {
  try {
    const accessToken = (await cookies()).get(AUTH_COOKIE_ACCESS_TOKEN)?.value;
    return proxyApi('/trips', {
      method: 'POST',
      headers: {
        'content-type': 'application/json',
        ...getAuthorizationHeader(accessToken),
      },
      body: await req.text(),
    });
  } catch (error) {
    return NextResponse.json(
      {
        success: false,
        data: null,
        requestId: 'traveler-web-trips-create',
        error: {
          code: 'UPSTREAM_ERROR',
          message: error instanceof Error ? error.message : 'Failed to reach upstream API',
        },
      },
      { status: 502 },
    );
  }
}
