import { proxyApi } from '@/lib/api';

export async function GET(req: Request) {
  const url = new URL(req.url);
  const locale = url.searchParams.get('locale');
  const query = locale ? `?locale=${encodeURIComponent(locale)}` : '';

  return proxyApi(`/destinations${query}`, { method: 'GET' });
}
