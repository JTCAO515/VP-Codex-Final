"""Trips endpoint — list/create/update saved trips.

A trip owns its own itinerary (day_count + days[]) so Plan can be bound to a
specific trip instead of the single scratch itinerary in /api/itinerary.

GET    /api/trips           → { ok, trips: [...] }   (empty if unauthed)
GET    /api/trips/<id>      → { ok, trip }
POST   /api/trips           body { name, start_date?, day_count?, cities?, status? } → { ok, trip }
PUT    /api/trips/<id>      body { ...patch }                                         → { ok, trip }
DELETE /api/trips/<id>                                                                → 204
"""
from __future__ import annotations

from . import storage
from .auth import require_session
from .common import error_response, json_response, no_content, parse_json_body, random_id

PATCHABLE_FIELDS = (
    "name", "start_date", "day_count", "cities", "status", "progress", "days",
)


def _serialize(it: dict) -> dict:
    p = it.get("payload") or {}
    return {
        "id": p.get("id") or it.get("ref_id"),
        "name": p.get("name", "Untitled trip"),
        "start_date": p.get("start_date", ""),
        "day_count": int(p.get("day_count", 0) or 0),
        "cities": p.get("cities", []),
        "city_count": len(p.get("cities", [])),
        "status": p.get("status", "draft"),
        "progress": p.get("progress", 0),
        "days": p.get("days", []),
        "created_at": it.get("created_at"),
    }


def _find(user_id: str, trip_id: str) -> dict | None:
    items = storage.favorites.list(user_id, kind="trip")
    return next((it for it in items
                if (it.get("payload") or {}).get("id") == trip_id
                or it.get("ref_id") == trip_id), None)


def handle(environ, start_response, path: str):
    method = environ.get("REQUEST_METHOD", "GET").upper()
    user = require_session(environ)

    # GET list
    if path == "/api/trips" and method == "GET":
        if not user:
            return json_response(start_response, {"ok": True, "trips": []})
        items = storage.favorites.list(user["id"], kind="trip")
        return json_response(start_response, {
            "ok": True, "trips": [_serialize(it) for it in items],
        })

    # POST create
    if path == "/api/trips" and method == "POST":
        if not user:
            return error_response(start_response, "Sign in to save trips",
                                  status="401 Unauthorized", code="auth_required")
        body = parse_json_body(environ)
        name = (body.get("name") or "").strip() or "Untitled trip"
        day_count = max(1, min(60, int(body.get("day_count", 3) or 3)))
        trip_id = random_id()
        payload = {
            "id": trip_id,
            "name": name,
            "start_date": body.get("start_date", ""),
            "day_count": day_count,
            "cities": body.get("cities", []),
            "status": body.get("status", "planning"),
            "progress": int(body.get("progress", 0) or 0),
            "days": body.get("days") or [
                {"day_index": i + 1, "label": f"Day {i + 1}", "stops": []}
                for i in range(day_count)
            ],
        }
        item = storage.favorites.add(user["id"], "trip", trip_id, payload)
        if not item:
            return error_response(start_response, "Could not save trip",
                                  status="503 Service Unavailable",
                                  code="storage_unavailable")
        return json_response(start_response, {"ok": True, "trip": _serialize(item)})

    # GET /<id>
    if path.startswith("/api/trips/") and method == "GET":
        if not user:
            return error_response(start_response, "Not signed in",
                                  status="401 Unauthorized", code="auth_required")
        trip_id = path[len("/api/trips/"):].strip("/")
        existing = _find(user["id"], trip_id)
        if not existing:
            return error_response(start_response, "Trip not found", status="404 Not Found")
        return json_response(start_response, {"ok": True, "trip": _serialize(existing)})

    # PUT /<id>
    if path.startswith("/api/trips/") and method == "PUT":
        if not user:
            return error_response(start_response, "Not signed in",
                                  status="401 Unauthorized", code="auth_required")
        trip_id = path[len("/api/trips/"):].strip("/")
        body = parse_json_body(environ)
        existing = _find(user["id"], trip_id)
        if not existing:
            return error_response(start_response, "Trip not found", status="404 Not Found")
        payload = dict(existing.get("payload") or {})
        for k in PATCHABLE_FIELDS:
            if k in body:
                payload[k] = body[k]
        payload["id"] = trip_id
        item = storage.favorites.add(user["id"], "trip", trip_id, payload)
        return json_response(start_response, {"ok": True, "trip": _serialize(item)})

    # DELETE /<id>
    if path.startswith("/api/trips/") and method == "DELETE":
        if not user:
            return error_response(start_response, "Not signed in",
                                  status="401 Unauthorized", code="auth_required")
        trip_id = path[len("/api/trips/"):].strip("/")
        target = _find(user["id"], trip_id)
        if not target:
            return error_response(start_response, "Trip not found", status="404 Not Found")
        storage.favorites.remove(target["id"], user["id"])
        return no_content(start_response)

    return error_response(start_response, "Method not allowed",
                          status="405 Method Not Allowed")
