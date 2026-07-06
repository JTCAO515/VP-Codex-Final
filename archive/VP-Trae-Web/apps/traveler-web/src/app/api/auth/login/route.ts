import { NextResponse } from 'next/server';

import { proxyApi } from '@/lib/api';

export async function POST(req: Request) {
  try {
    return proxyApi('/auth/login', {
      method: 'POST',
      headers: { 'content-type': 'application/json' },
      body: await req.text(),
    });
  } catch (error) {
    return NextResponse.json(
      {
        success: false,
        data: null,
        requestId: 'traveler-web-auth-login',
        error: {
          code: 'UPSTREAM_ERROR',
          message: error instanceof Error ? error.message : 'Failed to reach upstream API',
        },
      },
      { status: 502 },
    );
  }
}
