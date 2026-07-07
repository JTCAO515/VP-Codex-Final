"""TTS endpoint — Alibaba DashScope Qwen3-TTS proxy.

POST /api/tts  { text, voice?, model? } → audio/mpeg
Falls through to a 503 fallback signal so the client switches to Web Speech.
"""
from __future__ import annotations

import hashlib
import json
import time

from . import config
from .common import binary_response, error_response, http_request, json_response, parse_json_body

# Lightweight in-memory LRU cache (per worker — fine for our usage).
_CACHE: "dict[str, tuple[float, bytes]]" = {}
_CACHE_MAX = 100
_CACHE_TTL = 3600.0  # 1 hour


def _cache_key(text: str, voice: str, model: str) -> str:
    return hashlib.sha256(f"{model}|{voice}|{text}".encode("utf-8")).hexdigest()


def _cache_get(key: str) -> bytes | None:
    entry = _CACHE.get(key)
    if not entry:
        return None
    expires, data = entry
    if expires < time.time():
        _CACHE.pop(key, None)
        return None
    return data


def _cache_put(key: str, data: bytes) -> None:
    if len(_CACHE) >= _CACHE_MAX:
        # Evict the oldest.
        oldest = min(_CACHE.items(), key=lambda kv: kv[1][0])[0]
        _CACHE.pop(oldest, None)
    _CACHE[key] = (time.time() + _CACHE_TTL, data)


def _request_tts(text: str, voice: str, model: str) -> bytes | None:
    """Call DashScope and return MP3 bytes, or None on failure."""
    url = (f"{config.DASHSCOPE_BASE_URL}/api/v1/services/aigc/"
           "multimodal-generation/generation")
    payload = {
        "model": model,
        "input": {
            "messages": [
                {"role": "user", "content": [{"text": text}]},
            ],
        },
        "parameters": {
            "voice": voice,
            "audio_format": "mp3",
            "sample_rate": 24000,
        },
    }
    code, body, _ = http_request(
        url,
        method="POST",
        headers={
            "Authorization": f"Bearer {config.DASHSCOPE_API_KEY}",
            "X-DashScope-DataInspection": "disable",
        },
        data=payload,
        timeout=30,
    )
    if code != 200:
        return None
    try:
        data = json.loads(body.decode("utf-8"))
    except ValueError:
        return None
    # DashScope multimodal response may either include an audio URL
    # or inline base64 in choices[0].message.audio.data.
    try:
        msg = data["output"]["choices"][0]["message"]
        audio = msg.get("audio") or {}
    except (KeyError, IndexError, TypeError):
        audio = {}
    url_field = audio.get("url")
    if url_field:
        ac, abody, _ = http_request(url_field, timeout=30)
        if ac == 200 and abody:
            return abody
    # Inline base64 fallback.
    b64 = audio.get("data")
    if b64:
        import base64
        try:
            return base64.b64decode(b64)
        except Exception:  # noqa: BLE001
            return None
    return None


def handle(environ, start_response, path: str):
    method = environ.get("REQUEST_METHOD", "GET").upper()
    if method != "POST":
        return error_response(start_response, "Method not allowed",
                              "405 Method Not Allowed")
    body = parse_json_body(environ)
    text = (body.get("text") or "").strip()
    if not text:
        return error_response(start_response, "text is required")
    voice = (body.get("voice") or config.QWEN_TTS_VOICE).strip()
    model = (body.get("model") or config.QWEN_TTS_MODEL).strip()
    if not config.has_voice():
        return json_response(start_response, {
            "ok": False,
            "error": "tts_unavailable",
            "fallback": "web_speech",
        }, status="503 Service Unavailable")

    cache_key = _cache_key(text, voice, model)
    etag_value = f'W/"{cache_key[:16]}"'
    if_none_match = environ.get("HTTP_IF_NONE_MATCH", "")
    if if_none_match == etag_value and _cache_get(cache_key):
        start_response("304 Not Modified", [("ETag", etag_value), ("Content-Length", "0")])
        return [b""]

    audio = _cache_get(cache_key)
    if audio is None:
        audio = _request_tts(text, voice, model)
        if audio is None:
            return json_response(start_response, {
                "ok": False,
                "error": "tts_upstream_failed",
                "fallback": "web_speech",
            }, status="502 Bad Gateway")
        _cache_put(cache_key, audio)

    return binary_response(
        start_response,
        audio,
        "audio/mpeg",
        extra_headers=[
            ("ETag", etag_value),
            ("Cache-Control", "public, max-age=3600"),
        ],
    )
