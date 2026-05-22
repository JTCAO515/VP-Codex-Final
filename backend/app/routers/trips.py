from __future__ import annotations

from fastapi import APIRouter, HTTPException

from app.db import session_scope
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

