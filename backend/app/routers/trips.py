from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, Request
from sqlalchemy.orm import Session

from app.auth import get_principal
from app.db import get_db, session_scope
from app.models import ChatMessage
from app.models import Trip

router = APIRouter()


@router.get("/trips/{trip_id}")
def trip_get(trip_id: str):
    with session_scope() as db:
        t = db.query(Trip).filter(Trip.id == trip_id).one_or_none()
        if not t:
            raise HTTPException(status_code=404, detail="Trip not found")
        return {
            "trip": {
                "id": t.id,
                "user_id": t.user_id,
                "cities": t.cities,
                "start_date": t.start_date,
                "end_date": t.end_date,
                "party": t.party,
                "constraints": t.constraints,
                "current_itinerary": t.current_itinerary,
                "itinerary_versions": t.itinerary_versions,
            }
        }


@router.get("/trips")
def trips_list(request: Request, db: Session = Depends(get_db)):
    # requires login (no guest list server-side)
    principal = get_principal(request, guest_id=None)
    trips = (
        db.query(Trip)
        .filter(Trip.user_id == principal.user_id)
        .order_by(Trip.updated_at.desc())
        .limit(50)
        .all()
    )
    return {
        "trips": [
            {
                "id": t.id,
                "title": t.title,
                "updated_at": t.updated_at.isoformat() if t.updated_at else None,
                "cities": t.cities,
                "current_itinerary": t.current_itinerary,
            }
            for t in trips
        ]
    }


@router.get("/trips/{trip_id}/messages")
def trip_messages(trip_id: str, request: Request, db: Session = Depends(get_db)):
    principal = get_principal(request, guest_id=None)
    t = db.query(Trip).filter(Trip.id == trip_id, Trip.user_id == principal.user_id).one_or_none()
    if not t:
        raise HTTPException(status_code=404, detail="Trip not found")
    msgs = (
        db.query(ChatMessage)
        .filter(ChatMessage.trip_id == trip_id, ChatMessage.user_id == principal.user_id)
        .order_by(ChatMessage.created_at.asc())
        .limit(2000)
        .all()
    )
    return {
        "messages": [
            {"id": m.id, "role": m.role, "content": m.content, "created_at": m.created_at.isoformat()}
            for m in msgs
        ]
    }
