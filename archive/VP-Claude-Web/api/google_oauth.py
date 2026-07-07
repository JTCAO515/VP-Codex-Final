"""Google OAuth 2.0 — authorization-code flow with PKCE-free server-side exchange."""
from __future__ import annotations

import json
import urllib.parse

from . import config
from .common import http_request

AUTH_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth"
TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token"
USERINFO_ENDPOINT = "https://www.googleapis.com/oauth2/v3/userinfo"
TOKENINFO_ENDPOINT = "https://oauth2.googleapis.com/tokeninfo"


def consent_url(state: str) -> str:
    params = {
        "client_id": config.GOOGLE_CLIENT_ID,
        "redirect_uri": config.GOOGLE_REDIRECT_URI,
        "response_type": "code",
        "scope": "openid email profile",
        "access_type": "online",
        "include_granted_scopes": "true",
        "state": state,
    }
    return f"{AUTH_ENDPOINT}?{urllib.parse.urlencode(params)}"


def exchange_code(code: str) -> dict | None:
    """Exchange the auth code for an id_token + access_token, then resolve a profile."""
    body = urllib.parse.urlencode({
        "code": code,
        "client_id": config.GOOGLE_CLIENT_ID,
        "client_secret": config.GOOGLE_CLIENT_SECRET,
        "redirect_uri": config.GOOGLE_REDIRECT_URI,
        "grant_type": "authorization_code",
    }).encode("utf-8")
    status, payload, _ = http_request(
        TOKEN_ENDPOINT,
        method="POST",
        headers={"Content-Type": "application/x-www-form-urlencoded"},
        data=body,
        timeout=15,
    )
    if status != 200:
        return None
    try:
        token_data = json.loads(payload.decode("utf-8"))
    except ValueError:
        return None
    id_token = token_data.get("id_token")
    access_token = token_data.get("access_token")
    profile = None
    if id_token:
        # Use tokeninfo for verification + identity in one step.
        status2, p2, _ = http_request(
            f"{TOKENINFO_ENDPOINT}?id_token={urllib.parse.quote(id_token)}",
            timeout=10,
        )
        if status2 == 200:
            try:
                tok = json.loads(p2.decode("utf-8"))
            except ValueError:
                tok = {}
            if tok.get("aud") == config.GOOGLE_CLIENT_ID and tok.get("sub"):
                profile = {
                    "id": tok["sub"],
                    "email": tok.get("email"),
                    "name": tok.get("name"),
                    "picture": tok.get("picture"),
                    "email_verified": tok.get("email_verified") in (True, "true"),
                }
    if not profile and access_token:
        status3, p3, _ = http_request(
            USERINFO_ENDPOINT,
            headers={"Authorization": f"Bearer {access_token}"},
            timeout=10,
        )
        if status3 == 200:
            try:
                ui = json.loads(p3.decode("utf-8"))
            except ValueError:
                ui = {}
            if ui.get("sub"):
                profile = {
                    "id": ui["sub"],
                    "email": ui.get("email"),
                    "name": ui.get("name"),
                    "picture": ui.get("picture"),
                    "email_verified": ui.get("email_verified", False),
                }
    return profile
