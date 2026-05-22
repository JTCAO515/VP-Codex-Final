"""
JWT token creation/verification & password hashing utilities.
Zero native dependencies — pure Python stdlib, works on Vercel Serverless.
"""
from __future__ import annotations

import base64
import hashlib
import hmac
import json
import os
import secrets
import time
from typing import Any

# ---- Config ----
SECRET_KEY = os.getenv("JWT_SECRET_KEY", "visepanda-ai-dev-secret-change-in-production")
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = int(os.getenv("JWT_EXPIRE_MINUTES", "1440"))  # default 24h

# PBKDF2 params
_PBKDF2_ITERATIONS = 390000
_PBKDF2_HASH = "sha256"
_SALT_BYTES = 16
_KEY_LENGTH = 32


# ---- Password utilities (pure Python stdlib, no native deps) ----

def hash_password(password: str) -> str:
    """Hash a password with PBKDF2-SHA256 + random salt."""
    salt = secrets.token_hex(_SALT_BYTES)
    key = hashlib.pbkdf2_hmac(
        _PBKDF2_HASH,
        password.encode("utf-8"),
        salt.encode("ascii"),
        _PBKDF2_ITERATIONS,
        dklen=_KEY_LENGTH,
    )
    return f"pbkdf2:sha256:{_PBKDF2_ITERATIONS}${salt}${key.hex()}"


def verify_password(plain: str, hashed: str) -> bool:
    """Verify a password against a PBKDF2 hash."""
    if not hashed or not hashed.startswith("pbkdf2:sha256:"):
        return False
    try:
        prefix, rest = hashed.split("$", 1)
        _, _, iterations_str = prefix.split(":", 2)
        salt, stored_key_str = rest.split("$", 1)
        key = hashlib.pbkdf2_hmac(
            "sha256",
            plain.encode("utf-8"),
            salt.encode("ascii"),
            int(iterations_str),
            dklen=_KEY_LENGTH,
        )
        return hmac.compare_digest(key.hex(), stored_key_str)
    except Exception:
        return False


# ---- JWT utilities (pure Python stdlib, zero deps) ----

def _b64url_encode(data: bytes) -> str:
    return base64.urlsafe_b64encode(data).rstrip(b"=").decode("ascii")


def _b64url_decode(data: str) -> bytes:
    padding = 4 - len(data) % 4
    if padding != 4:
        data += "=" * padding
    return base64.urlsafe_b64decode(data.encode("ascii"))


def create_access_token(user_id: str, email: str | None = None) -> str:
    """Create a JWT access token using pure stdlib HMAC-SHA256."""
    expire = int(time.time()) + (ACCESS_TOKEN_EXPIRE_MINUTES * 60)
    payload: dict[str, Any] = {
        "sub": user_id,
        "exp": expire,
        "iat": int(time.time()),
    }
    if email:
        payload["email"] = email
    return _jwt_encode(payload, SECRET_KEY)


def decode_access_token(token: str) -> dict[str, Any] | None:
    """Decode and verify JWT. Returns payload or None."""
    try:
        return _jwt_decode(token, SECRET_KEY)
    except Exception:
        return None


def create_admin_token(email: str) -> str:
    """Create a JWT token with admin role."""
    expire = int(time.time()) + (ACCESS_TOKEN_EXPIRE_MINUTES * 60)
    payload: dict[str, Any] = {
        "sub": "admin",
        "email": email,
        "role": "admin",
        "exp": expire,
        "iat": int(time.time()),
    }
    return _jwt_encode(payload, SECRET_KEY)


def _jwt_encode(payload: dict, secret: str) -> str:
    """Pure Python JWT HS256 encode — no external dependencies."""
    header = {"alg": "HS256", "typ": "JWT"}
    seg1 = _b64url_encode(json.dumps(header, separators=(",", ":")).encode("utf-8"))
    seg2 = _b64url_encode(json.dumps(payload, separators=(",", ":")).encode("utf-8"))
    signing_input = f"{seg1}.{seg2}"
    sig = hmac.new(secret.encode("utf-8"), signing_input.encode("utf-8"), hashlib.sha256).digest()
    seg3 = _b64url_encode(sig)
    return f"{signing_input}.{seg3}"


def _jwt_decode(token: str, secret: str) -> dict:
    """Pure Python JWT HS256 decode — no external dependencies."""
    parts = token.split(".")
    if len(parts) != 3:
        raise ValueError("Invalid token format")
    signing_input = f"{parts[0]}.{parts[1]}"
    expected = hmac.new(secret.encode("utf-8"), signing_input.encode("utf-8"), hashlib.sha256).digest()
    actual = _b64url_decode(parts[2])
    if not hmac.compare_digest(expected, actual):
        raise ValueError("Invalid signature")

    payload_bytes = _b64url_decode(parts[1])
    payload = json.loads(payload_bytes)

    # Check expiration
    exp = payload.get("exp")
    if exp is not None and isinstance(exp, (int, float)) and time.time() > exp:
        raise ValueError("Token expired")

    return payload
