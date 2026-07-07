import { dirname } from "node:path";
import { fileURLToPath } from "node:url";

const rootDir = dirname(fileURLToPath(import.meta.url));

// Mobile clients (Android/iOS) call VISEPANDA_API_BASE_URL (go2china.space)
// for everything, including butler-service-specific paths like
// /butler/memory/profile — there is no separate mobile-facing base URL for
// butler-service. This rewrite is the same greylist switch already used by
// lib/ai/butlerServiceGateway.ts for /api/chat: inert (no rewrite, so
// /butler/* 404s as it always has) when BUTLER_SERVICE_URL is unset, and
// forwards to the real deployment once it's configured. Discovered as a real
// gap in PR #39 (iOS #14): the endpoint existed on butler-service (PR #37)
// but go2china.space had never forwarded anything under /butler/*.
const butlerServiceUrl = process.env.BUTLER_SERVICE_URL;

/** @type {import('next').NextConfig} */
const nextConfig = {
  allowedDevOrigins: ["127.0.0.1"],
  devIndicators: false,
  outputFileTracingRoot: rootDir,
  poweredByHeader: false,
  async rewrites() {
    if (!butlerServiceUrl) return [];
    const destinationBase = butlerServiceUrl.replace(/\/+$/, "");
    return [{ source: "/butler/:path*", destination: `${destinationBase}/butler/:path*` }];
  },
};

export default nextConfig;
