"""
v3.2 Trip itinerary planner endpoints: save, load, share.
"""
from __future__ import annotations

import hashlib
import datetime as dt

from fastapi import APIRouter, Depends, HTTPException, Request
from pydantic import BaseModel, Field
from sqlalchemy.orm import Session

from app.auth import get_principal
from app.db import get_db
from app.models import Trip

router = APIRouter()

MAX_VERSIONS = 10


class ItinerarySaveIn(BaseModel):
    days: list = Field(default_factory=list)
    title: str | None = None


def _share_token(trip_id: str) -> str:
    """Derive a stable short share token from trip_id."""
    return hashlib.sha256(trip_id.encode()).hexdigest()[:12]


@router.put("/trips/{trip_id}/itinerary")
def itinerary_save(
    trip_id: str,
    payload: ItinerarySaveIn,
    request: Request,
    db: Session = Depends(get_db),
):
    principal = get_principal(request, guest_id=None)
    t = db.query(Trip).filter(
        Trip.id == trip_id, Trip.user_id == principal.user_id
    ).one_or_none()
    if not t:
        raise HTTPException(status_code=404, detail="Trip not found")

    versions = list(t.itinerary_versions or [])
    if t.current_itinerary:
        versions.insert(0, dict(t.current_itinerary))
        versions = versions[:MAX_VERSIONS]

    t.current_itinerary = {"days": payload.days}
    t.itinerary_versions = versions
    t.updated_at = dt.datetime.now(dt.timezone.utc)
    if payload.title:
        t.title = payload.title
    db.commit()

    return {"ok": True, "version": len(t.itinerary_versions) + 1}


@router.get("/trips/{trip_id}/itinerary")
def itinerary_load(
    trip_id: str,
    request: Request,
    db: Session = Depends(get_db),
):
    principal = get_principal(request, guest_id=None)
    t = db.query(Trip).filter(
        Trip.id == trip_id, Trip.user_id == principal.user_id
    ).one_or_none()
    if not t:
        raise HTTPException(status_code=404, detail="Trip not found")

    return {
        "itinerary": t.current_itinerary or {"days": []},
        "title": t.title,
        "cities": t.cities or [],
        "start_date": t.start_date,
        "end_date": t.end_date,
        "share_token": _share_token(t.id),
    }


@router.get("/shared/{share_token}")
def shared_view(share_token: str, db: Session = Depends(get_db)):
    """Public itinerary view — no auth required."""
    # Brute-force lookup: compute share tokens for recent trips.
    # For MVP scale this is fine; for production, cache or store token.
    trips = db.query(Trip).order_by(Trip.updated_at.desc()).limit(500).all()
    for t in trips:
        if _share_token(t.id) == share_token:
            return {
                "itinerary": t.current_itinerary or {"days": []},
                "title": t.title or "Untitled Trip",
                "cities": t.cities or [],
                "start_date": t.start_date,
                "end_date": t.end_date,
            }
    raise HTTPException(status_code=404, detail="Shared itinerary not found")
