export const API_BASE_URL = process.env.API_BASE_URL ?? 'http://localhost:3000';

export type ApiEnvelope<T> = {
  success: boolean;
  data: T;
  requestId: string;
  error?: {
    code: string;
    message: string;
  };
};

export async function fetchApiJson<T>(path: string, init?: RequestInit): Promise<ApiEnvelope<T>> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers: {
      ...(init?.headers ?? {}),
    },
    cache: 'no-store',
  });

  const payload = (await response.json()) as ApiEnvelope<T>;

  if (!response.ok || payload.success === false) {
    throw new Error(payload.error?.message ?? `Request failed: ${path}`);
  }

  return payload;
}

export async function proxyApi(path: string, init?: RequestInit) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers: {
      ...(init?.headers ?? {}),
    },
    cache: 'no-store',
  });

  const text = await response.text();

  return new Response(text, {
    status: response.status,
    headers: {
      'content-type': response.headers.get('content-type') ?? 'application/json; charset=utf-8',
    },
  });
}
