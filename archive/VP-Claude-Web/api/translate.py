"""Translate endpoint — DeepSeek-as-translator.

POST /api/translate
  body: { text, direction: 'en->zh' | 'zh->en', mode?: 'casual'|'formal' }
  → { ok, source, target, pinyin?, note?, provider }
"""
from __future__ import annotations

import json

from . import config
from .common import error_response, http_request, json_response, parse_json_body


def _prompt(text: str, direction: str, mode: str) -> str:
    if direction == "en->zh":
        src, tgt = "English", "Chinese"
        pinyin_line = "line 2 the Hanyu pinyin (with tone marks) of the translation"
    else:
        src, tgt = "Chinese", "English"
        pinyin_line = "line 2 empty"
    tone = "Use natural, conversational phrasing." if mode != "formal" else "Use polite, formal phrasing."
    return (
        f"Translate the following from {src} to {tgt}. {tone} "
        f"Output exactly three lines: "
        f"line 1 the translation in {tgt}; "
        f"{pinyin_line}; "
        f"line 3 a one-sentence cultural note IF the choice of words is non-obvious else empty. "
        f"Do not add any other text.\n\n"
        f"---\n{text}\n---"
    )


def _local_translate(text: str, direction: str) -> dict:
    return {
        "source": text,
        "target": text if direction == "zh->en" else "（请先配置 DEEPSEEK_API_KEY）",
        "pinyin": None,
        "note": "DeepSeek API key not configured.",
        "provider": "local",
    }


def _deepseek_translate(text: str, direction: str, mode: str) -> dict:
    code, body, _ = http_request(
        f"{config.DEEPSEEK_BASE_URL}/v1/chat/completions",
        method="POST",
        headers={"Authorization": f"Bearer {config.DEEPSEEK_API_KEY}"},
        data={
            "model": config.DEEPSEEK_MODEL,
            "messages": [
                {"role": "system", "content": "You are a precise English↔Chinese translator."},
                {"role": "user", "content": _prompt(text, direction, mode)},
            ],
            "temperature": 0.2,
            "max_tokens": 400,
            "stream": False,
        },
        timeout=30,
    )
    if code != 200:
        return {**_local_translate(text, direction), "provider": "deepseek_unavailable"}
    try:
        data = json.loads(body.decode("utf-8"))
        raw = data["choices"][0]["message"]["content"]
    except (ValueError, KeyError, IndexError):
        return {**_local_translate(text, direction), "provider": "deepseek_bad_response"}
    lines = [ln.strip() for ln in raw.strip().splitlines()]
    target = lines[0] if lines else ""
    pinyin = lines[1] if len(lines) > 1 and lines[1] else None
    note = lines[2] if len(lines) > 2 and lines[2] else None
    return {"source": text, "target": target, "pinyin": pinyin, "note": note,
            "provider": "deepseek"}


def handle(environ, start_response, path: str):
    method = environ.get("REQUEST_METHOD", "GET").upper()
    if method != "POST":
        return error_response(start_response, "Method not allowed",
                              "405 Method Not Allowed")
    body = parse_json_body(environ)
    text = (body.get("text") or "").strip()
    direction = body.get("direction") or "en->zh"
    mode = body.get("mode") or "casual"
    if not text:
        return error_response(start_response, "text is required")
    if direction not in ("en->zh", "zh->en"):
        return error_response(start_response, "direction must be 'en->zh' or 'zh->en'")
    result = (_deepseek_translate(text, direction, mode)
              if config.has_deepseek()
              else _local_translate(text, direction))
    return json_response(start_response, {"ok": True, "direction": direction, **result})
