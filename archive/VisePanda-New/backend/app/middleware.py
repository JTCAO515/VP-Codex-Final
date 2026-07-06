"""
v3.0 Production middleware: rate limiting, security headers, error recovery.
"""
from __future__ import annotations

import time
import os
from collections import defaultdict

from fastapi import Request, Response
from fastapi.responses import JSONResponse
from starlette.middleware.base import BaseHTTPMiddleware


# ── Rate Limiter ──────────────────────────────────────────────
# Simple in-memory sliding window. Vercel serverless means this resets
# per cold start — acceptable for MVP. Replace with Redis for scale.

class RateLimiter:
    def __init__(self, window: int = 60, max_requests: int = 30):
        self.window = window
        self.max_requests = max_requests
        self._buckets: dict[str, list[float]] = defaultdict(list)

    def is_allowed(self, key: str) -> bool:
        now = time.time()
        bucket = self._buckets[key]
        # Prune old entries
        bucket[:] = [t for t in bucket if now - t < self.window]
        if len(bucket) >= self.max_requests:
            return False
        bucket.append(now)
        return True


_limiter = RateLimiter(
    window=int(os.getenv("RATE_WINDOW", "60")),
    max_requests=int(os.getenv("RATE_MAX", "60")),
)

# Endpoints that skip rate limiting
_RATE_LIMIT_SKIP = {"/health", "/public-config", "/api/health", "/api/public-config"}

def _skip_rate_limit() -> bool:
    """Skip rate limiting during tests."""
    return os.getenv("AUTH_TEST_BYPASS") == "1" or os.getenv("PYTEST_CURRENT_TEST", "")


class SecurityMiddleware(BaseHTTPMiddleware):
    """Adds security headers to every response."""

    async def dispatch(self, request: Request, call_next):
        response: Response = await call_next(request)

        # Security headers
        response.headers["X-Content-Type-Options"] = "nosniff"
        response.headers["X-Frame-Options"] = "DENY"
        response.headers["X-XSS-Protection"] = "1; mode=block"
        response.headers["Referrer-Policy"] = "strict-origin-when-cross-origin"
        response.headers["Permissions-Policy"] = (
            "camera=(), microphone=(), geolocation=(), interest-cohort=()"
        )

        # CSP: allow self + Supabase + external CDNs
        is_prod = not os.getenv("IS_DEV", "")
        csp = (
            "default-src 'self'; "
            "script-src 'self' 'unsafe-inline' https://esm.sh; "
            "style-src 'self' 'unsafe-inline'; "
            "img-src 'self' data: https:; "
            "connect-src 'self' https://*.supabase.co https://api.deepseek.com; "
            "font-src 'self'; "
            "frame-src 'self' https://*.supabase.co; "
        )
        response.headers["Content-Security-Policy"] = csp

        # HSTS (1 year, include subdomains)
        if is_prod:
            response.headers["Strict-Transport-Security"] = (
                "max-age=31536000; includeSubDomains; preload"
            )

        return response


class RateLimitMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next):
        path = request.url.path
        if _skip_rate_limit() or path in _RATE_LIMIT_SKIP:
            return await call_next(request)

        client_ip = (
            request.headers.get("x-forwarded-for", "").split(",")[0].strip()
            or request.client.host
            if request.client
            else "unknown"
        )

        if not _limiter.is_allowed(client_ip):
            return JSONResponse(
                status_code=429,
                content={"detail": "Too many requests. Please slow down."},
                headers={"Retry-After": str(_limiter.window)},
            )

        return await call_next(request)


# ── Error Handlers ────────────────────────────────────────────

async def http_exception_handler(request: Request, exc):
    """Unified error response for HTTP exceptions."""
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "error": exc.detail if hasattr(exc, "detail") else str(exc),
            "path": request.url.path,
        },
    )


async def generic_exception_handler(request: Request, exc):
    """Catch-all for unhandled errors. Never leaks stack traces in production."""
    import logging
    logging.getLogger("vise").exception("Unhandled error on %s", request.url.path)
    is_prod = not os.getenv("IS_DEV", "")
    return JSONResponse(
        status_code=500,
        content={
            "error": "Internal server error" if is_prod else str(exc),
            "path": request.url.path,
        },
    )
