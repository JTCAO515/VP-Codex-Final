"""Itinerary endpoints.

GET  /api/itinerary             → { ok, days: [...] } (empty when unauthed)
PUT  /api/itinerary             body { days: [...] } → { ok } (auth required)
POST /api/itinerary/generate    body { cities: [...], day_count, pace?, travelers? }
                                 → { ok, days: [...] } via DeepSeek structured output
"""
from __future__ import annotations

import json
import re

from . import config, storage
from .auth import require_session
from .common import error_response, http_request, json_response, parse_json_body

GENERATE_SYSTEM_PROMPT = (
    "You are a meticulous China travel planner. Given a list of cities, a "
    "number of days, a travel pace, and a traveler count, output ONLY a "
    "single JSON object (no markdown fences, no commentary) with this exact "
    "shape: "
    '{"days":[{"day_index":1,"label":"Day 1","city":"<city name>",'
    '"stops":[{"time":"08:30","name":"<stop name>","desc":"<one short '
    'sentence>","tags":["<Category>","<duration or price>"]}]}]}. '
    "Distribute days across the given cities in order. Each day should have "
    "2-4 stops with realistic times. Keep names and descriptions concise."
)


def _local_generate(cities: list, day_count: int) -> list:
    names = [c.get("name", "City") for c in cities] or ["Beijing"]
    days = []
    for i in range(day_count):
        city = names[i % len(names)]
        days.append({
            "day_index": i + 1,
            "label": f"Day {i + 1}",
            "city": city,
            "stops": [
                {"time": "09:00", "name": f"Explore {city}",
                 "desc": "DeepSeek not configured — add stops manually.",
                 "tags": ["Landmark"]},
            ],
        })
    return days


def _generate(environ, start_response):
    body = parse_json_body(environ)
    cities = body.get("cities") or []
    day_count = max(1, min(30, int(body.get("day_count", 3) or 3)))
    pace = body.get("pace", "Relaxed")
    travelers = int(body.get("travelers", 2) or 2)

    if not config.has_deepseek():
        return json_response(start_response, {
            "ok": True,
            "days": _local_generate(cities, day_count),
            "provider": "local",
        })

    city_names = ", ".join(c.get("name", "") for c in cities) or "Beijing"
    user_prompt = (
        f"Cities (in visiting order): {city_names}. "
        f"Total days: {day_count}. Pace: {pace}. Travelers: {travelers}."
    )
    code, raw, _ = http_request(
        f"{config.DEEPSEEK_BASE_URL}/v1/chat/completions",
        method="POST",
        headers={"Authorization": f"Bearer {config.DEEPSEEK_API_KEY}"},
        data={
            "model": config.DEEPSEEK_MODEL,
            "messages": [
                {"role": "system", "content": GENERATE_SYSTEM_PROMPT},
                {"role": "user", "content": user_prompt},
            ],
            "temperature": 0.4,
            "max_tokens": 1800,
            "stream": False,
        },
        timeout=45,
    )
    if code != 200:
        return json_response(start_response, {
            "ok": True, "days": _local_generate(cities, day_count),
            "provider": "deepseek_unavailable",
        })
    try:
        data = json.loads(raw.decode("utf-8"))
        content = data["choices"][0]["message"]["content"]
        # Strip markdown fences if the model added them despite instructions.
        match = re.search(r"\{.*\}", content, re.DOTALL)
        parsed = json.loads(match.group(0) if match else content)
        days = parsed.get("days")
        if not isinstance(days, list) or not days:
            raise ValueError("no days in response")
    except Exception:  # noqa: BLE001
        return json_response(start_response, {
            "ok": True, "days": _local_generate(cities, day_count),
            "provider": "deepseek_bad_response",
        })
    return json_response(start_response, {"ok": True, "days": days, "provider": "deepseek"})


def handle(environ, start_response, path: str):
    method = environ.get("REQUEST_METHOD", "GET").upper()

    if path == "/api/itinerary/generate" and method == "POST":
        return _generate(environ, start_response)

    user = require_session(environ)
    if method == "GET":
        days = storage.itineraries.get(user["id"]) if user else []
        return json_response(start_response, {"ok": True, "days": days})
    if method == "PUT":
        if not user:
            return error_response(start_response, "Sign in to save your itinerary",
                                  status="401 Unauthorized", code="auth_required")
        body = parse_json_body(environ)
        days = body.get("days") or []
        if not isinstance(days, list):
            return error_response(start_response, "days must be a list")
        # Light validation: cap depth + sizes to avoid abuse.
        if len(days) > 60:
            return error_response(start_response, "Too many days (max 60)")
        ok = storage.itineraries.upsert(user["id"], days)
        if not ok:
            return error_response(start_response, "Could not save itinerary",
                                  status="503 Service Unavailable",
                                  code="storage_unavailable")
        return json_response(start_response, {"ok": True})
    return error_response(start_response, "Method not allowed",
                          status="405 Method Not Allowed")
