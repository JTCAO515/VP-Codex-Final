"""STT endpoint — Alibaba DashScope Qwen3-ASR proxy.

POST /api/stt
  body: multipart/form-data with `file` field (audio/webm or audio/wav)
  → { ok, text, language?, provider }
"""
from __future__ import annotations

import base64
import json

from . import config
from .common import error_response, http_request, json_response, parse_multipart


def _request_asr(audio_bytes: bytes, content_type: str) -> dict | None:
    url = (f"{config.DASHSCOPE_BASE_URL}/api/v1/services/aigc/"
           "multimodal-generation/generation")
    # DashScope multimodal expects base64 audio in a content list.
    b64 = base64.b64encode(audio_bytes).decode("ascii")
    payload = {
        "model": config.QWEN_ASR_MODEL,
        "input": {
            "messages": [{
                "role": "user",
                "content": [
                    {"audio": f"data:{content_type};base64,{b64}"},
                    {"text": "Transcribe the audio. Output only the transcript."},
                ],
            }],
        },
        "parameters": {"result_format": "message"},
    }
    code, body, _ = http_request(
        url,
        method="POST",
        headers={
            "Authorization": f"Bearer {config.DASHSCOPE_API_KEY}",
            "X-DashScope-DataInspection": "disable",
        },
        data=payload,
        timeout=45,
    )
    if code != 200:
        return None
    try:
        data = json.loads(body.decode("utf-8"))
        choices = data["output"]["choices"]
        msg = choices[0]["message"]
        content = msg.get("content", "")
        if isinstance(content, list):
            # join text parts
            text = "".join(p.get("text", "") for p in content if isinstance(p, dict))
        else:
            text = str(content)
        return {"text": text.strip()}
    except (ValueError, KeyError, IndexError, TypeError):
        return None


def handle(environ, start_response, path: str):
    method = environ.get("REQUEST_METHOD", "GET").upper()
    if method != "POST":
        return error_response(start_response, "Method not allowed",
                              "405 Method Not Allowed")
    if not config.has_voice():
        return json_response(start_response, {
            "ok": False,
            "error": "stt_unavailable",
            "fallback": "web_speech",
        }, status="503 Service Unavailable")
    fields = parse_multipart(environ)
    f = fields.get("file") or fields.get("audio")
    if not f or not f.get("data"):
        return error_response(start_response, "file field required (multipart audio)")
    result = _request_asr(f["data"], f.get("content_type") or "audio/webm")
    if not result:
        return json_response(start_response, {
            "ok": False,
            "error": "stt_upstream_failed",
            "fallback": "web_speech",
        }, status="502 Bad Gateway")
    return json_response(start_response, {
        "ok": True,
        "text": result["text"],
        "provider": "qwen3-asr",
    })
