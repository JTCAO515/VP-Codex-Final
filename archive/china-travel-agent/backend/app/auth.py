from __future__ import annotations

import os
import time
from dataclasses import dataclass
from typing import Any

import httpx
from fastapi import HTTPException, Request
from jose import jwt


@dataclass(frozen=True)
class Principal:
    mode: str  # "user" | "guest"
    user_id: str
    guest_id: str | None = None


_JWKS_CACHE: dict[str, Any] = {"ts": 0.0, "jwks": None}


def _supabase_url() -> str:
    v = os.getenv("SUPABASE_URL", "").strip().rstrip("/")
    return v


def _jwks_url() -> str:
    # Supabase exposes JWKS at /auth/v1/certs
    base = _supabase_url()
    return f"{base}/auth/v1/certs"


def _issuer() -> str | None:
    v = os.getenv("SUPABASE_JWT_ISSUER", "").strip()
    if v:
        return v
    base = _supabase_url()
    return f"{base}/auth/v1" if base else None


def _audience() -> str | None:
    v = os.getenv("SUPABASE_JWT_AUD", "").strip()
    return v or None


def _get_jwks() -> dict:
    base = _supabase_url()
    if not base:
        raise HTTPException(status_code=500, detail="SUPABASE_URL not configured")

    now = time.time()
    if _JWKS_CACHE["jwks"] and (now - float(_JWKS_CACHE["ts"])) < 300:
        return _JWKS_CACHE["jwks"]

    r = httpx.get(_jwks_url(), timeout=10.0)
    r.raise_for_status()
    jwks = r.json()
    _JWKS_CACHE["jwks"] = jwks
    _JWKS_CACHE["ts"] = now
    return jwks


def _verify_supabase_jwt(token: str) -> dict:
    try:
        header = jwt.get_unverified_header(token)
    except Exception:
        raise HTTPException(status_code=401, detail="Invalid bearer token")

    kid = header.get("kid")
    if not kid:
        raise HTTPException(status_code=401, detail="Invalid bearer token (no kid)")

    jwks = _get_jwks()
    keys = jwks.get("keys") or []
    key = next((k for k in keys if k.get("kid") == kid), None)
    if not key:
        raise HTTPException(status_code=401, detail="Unknown token key id")

    issuer = _issuer()
    audience = _audience()
    options = {"verify_aud": bool(audience)}
    try:
        claims = jwt.decode(
            token,
            key,
            algorithms=["RS256"],
            issuer=issuer,
            audience=audience,
            options=options,
        )
        return claims
    except Exception:
        raise HTTPException(status_code=401, detail="Token verification failed")


def get_principal(request: Request, guest_id: str | None) -> Principal:
    """
    - Logged-in: Authorization: Bearer <supabase_access_token> => user_id=claims.sub
    - Guest: no Authorization => require guest_id => user_id="guest:<guest_id>"
    """
    auth = request.headers.get("authorization") or request.headers.get("Authorization")
    if auth and auth.lower().startswith("bearer "):
        token = auth.split(" ", 1)[1].strip()
        claims = _verify_supabase_jwt(token)
        sub = claims.get("sub")
        if not sub:
            raise HTTPException(status_code=401, detail="Token missing sub")
        return Principal(mode="user", user_id=str(sub))

    # no bearer token
    if guest_id is None:
        # endpoints that require login can pass guest_id=None
        raise HTTPException(status_code=401, detail="Login required")
    if not guest_id:
        raise HTTPException(status_code=400, detail="guest_id required for guest mode")
    return Principal(mode="guest", user_id=f"guest:{guest_id}", guest_id=guest_id)
