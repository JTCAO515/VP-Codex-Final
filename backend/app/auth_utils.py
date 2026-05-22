"""
JWT token creation/verification & password hashing utilities.
Uses pure-Python hashlib (PBKDF2) — no native dependencies, works on Vercel.
"""
from __future__ import annotations

import datetime as dt
import hashlib
import hmac
import os
import secrets
from typing import Any

import jose
from jose import JWTError, jwt

# ---- Config ----
SECRET_KEY = os.getenv("JWT_SECRET_KEY", "visepanda-ai-dev-secret-change-in-production")
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = int(os.getenv("JWT_EXPIRE_MINUTES", "1440"))  # default 24h

# PBKDF2 params
_PBKDF2_ITERATIONS = 390000
_PBKDF2_HASH = "sha256"
_SALT_BYTES = 16
_KEY_LENGTH = 32


# ---- Password utilities (pure Python, no native deps) ----

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
        # Format: pbkdf2:sha256:iterations$salt$hash
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


# ---- JWT utilities ----

def create_access_token(user_id: str, email: str | None = None) -> str:
    """Create JWT access token for a user."""
    expire = dt.datetime.now(dt.timezone.utc) + dt.timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    payload: dict[str, Any] = {
        "sub": user_id,
        "exp": expire,
        "iat": dt.datetime.now(dt.timezone.utc),
    }
    if email:
        payload["email"] = email
    return jwt.encode(payload, SECRET_KEY, algorithm=ALGORITHM)


def decode_access_token(token: str) -> dict[str, Any] | None:
    """Decode and verify JWT. Returns payload or None."""
    try:
        return jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
    except JWTError:
        return None


def create_admin_token(email: str) -> str:
    """Create JWT token with admin role."""
    expire = dt.datetime.now(dt.timezone.utc) + dt.timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    payload: dict[str, Any] = {
        "sub": "admin",
        "email": email,
        "role": "admin",
        "exp": expire,
        "iat": dt.datetime.now(dt.timezone.utc),
    }
    return jwt.encode(payload, SECRET_KEY, algorithm=ALGORITHM)
