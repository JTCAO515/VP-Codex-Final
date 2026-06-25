"""Trips endpoint — list/create/update saved trips.

GET    /api/trips           → { ok, trips: [...] }   (empty if unauthed)
POST   /api/trips           body { name, dates?, cities?, status? } → { ok, trip }
PUT    /api/trips/<id>      body { ...patch }                         → { ok, trip }
DELETE /api/trips/<id>                                                → 204
"""
from __future__ import annotations

from . import storage
from .auth import require_session
from .common import error_response, json_response, no_content, parse_json_body


def handle(environ, start_response, path: str):
    method = environ.get("REQUEST_METHOD", "GET").upper()
    user = require_session(environ)

    # GET list
    if path == "/api/trips" and method == "GET":
        if not user:
            return json_response(start_response, {"ok": True, "trips": []})
        items = storage.favorites.list(user["id"], kind="trip")
        trips = [
            {
                "id": (it.get("payload") or {}).get("id") or it.get("ref_id"),
                "name": (it.get("payload") or {}).get("name", "Untitled trip"),
                "dates": (it.get("payload") or {}).get("dates", ""),
                "cities": (it.get("payload") or {}).get("cities", []),
                "city_count": len((it.get("payload") or {}).get("cities", [])),
                "status": (it.get("payload") or {}).get("status", "draft"),
                "progress": (it.get("payload") or {}).get("progress", 0),
                "created_at": it.get("created_at"),
            }
            for it in items
        ]
        return json_response(start_response, {"ok": True, "trips": trips})

    # POST create
    if path == "/api/trips" and method == "POST":
        if not user:
            return error_response(start_response, "Sign in to save trips",
                                  status="401 Unauthorized", code="auth_required")
        body = parse_json_body(environ)
        name = (body.get("name") or "").strip() or "Untitled trip"
        from .common import random_id
        trip_id = random_id()
        payload = {
            "id": trip_id,
            "name": name,
            "dates": body.get("dates", ""),
            "cities": body.get("cities", []),
            "status": body.get("status", "draft"),
            "progress": int(body.get("progress", 0) or 0),
        }
        item = storage.favorites.add(user["id"], "trip", trip_id, payload)
        if not item:
            return error_response(start_response, "Could not save trip",
                                  status="503 Service Unavailable",
                                  code="storage_unavailable")
        return json_response(start_response, {"ok": True, "trip": payload})

    # PUT /<id>
    if path.startswith("/api/trips/") and method == "PUT":
        if not user:
            return error_response(start_response, "Not signed in",
                                  status="401 Unauthorized", code="auth_required")
        trip_id = path[len("/api/trips/"):].strip("/")
        body = parse_json_body(environ)
        items = storage.favorites.list(user["id"], kind="trip")
        existing = next((it for it in items
                         if (it.get("payload") or {}).get("id") == trip_id
                         or it.get("ref_id") == trip_id), None)
        if not existing:
            return error_response(start_response, "Trip not found", status="404 Not Found")
        payload = dict(existing.get("payload") or {})
        for k in ("name", "dates", "cities", "status", "progress"):
            if k in body:
                payload[k] = body[k]
        payload["id"] = trip_id
        storage.favorites.add(user["id"], "trip", trip_id, payload)
        return json_response(start_response, {"ok": True, "trip": payload})

    # DELETE /<id>
    if path.startswith("/api/trips/") and method == "DELETE":
        if not user:
            return error_response(start_response, "Not signed in",
                                  status="401 Unauthorized", code="auth_required")
        trip_id = path[len("/api/trips/"):].strip("/")
        items = storage.favorites.list(user["id"], kind="trip")
        target = next((it for it in items
                       if (it.get("payload") or {}).get("id") == trip_id
                       or it.get("ref_id") == trip_id), None)
        if not target:
            return error_response(start_response, "Trip not found", status="404 Not Found")
        storage.favorites.remove(target["id"], user["id"])
        return no_content(start_response)

    return error_response(start_response, "Method not allowed",
                          status="405 Method Not Allowed")
