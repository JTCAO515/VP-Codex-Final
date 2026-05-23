"""
v3.6 User center endpoints: profile, documents, budget.
"""
from __future__ import annotations

import datetime as dt

from fastapi import APIRouter, Depends, HTTPException, Request
from pydantic import BaseModel, Field
from sqlalchemy.orm import Session

from app.auth import get_principal
from app.db import get_db
from app.models import HotelBooking, ServiceOrder, Trip, User

router = APIRouter()


@router.get("/user/profile")
def user_profile(request: Request, db: Session = Depends(get_db)):
    principal = get_principal(request, guest_id=None)
    user = db.query(User).filter(User.id == principal.user_id).one_or_none()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    trips = (
        db.query(Trip)
        .filter(Trip.user_id == user.id)
        .order_by(Trip.updated_at.desc())
        .all()
    )

    trip_ids = [t.id for t in trips]
    bookings = (
        db.query(HotelBooking)
        .filter(HotelBooking.trip_id.in_(trip_ids))
        .count()
        if trip_ids
        else 0
    )
    orders = (
        db.query(ServiceOrder)
        .filter(ServiceOrder.rfp_id.in_(
            [t.id for t in trips]
        ))
        .count()
        if trip_ids
        else 0
    )

    total_days = sum(
        len((t.current_itinerary or {}).get("days", [])) for t in trips
    )

    cities_set = set()
    for t in trips:
        for c in (t.cities or []):
            cities_set.add(c)

    return {
        "profile": {
            "id": user.id,
            "created_at": user.created_at.isoformat() if user.created_at else None,
        },
        "stats": {
            "total_trips": len(trips),
            "total_bookings": bookings,
            "total_orders": orders,
            "total_days_planned": total_days,
            "cities_visited": list(cities_set)[:20],
        },
        "trips": [
            {
                "id": t.id,
                "title": t.title,
                "cities": t.cities,
                "start_date": t.start_date,
                "end_date": t.end_date,
                "updated_at": t.updated_at.isoformat() if t.updated_at else None,
                "day_count": len((t.current_itinerary or {}).get("days", [])),
            }
            for t in trips[:50]
        ],
    }


class DocumentSaveIn(BaseModel):
    doc_type: str = Field(description="passport | visa | insurance | other")
    label: str = ""
    data: dict = Field(default_factory=dict)


@router.get("/user/documents")
def user_documents(request: Request, db: Session = Depends(get_db)):
    principal = get_principal(request, guest_id=None)
    user = db.query(User).filter(User.id == principal.user_id).one_or_none()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    docs = (user.profile or {}).get("documents", [])
    # Strip sensitive fields
    safe = []
    for d in docs:
        item = {"doc_type": d.get("doc_type"), "label": d.get("label", ""), "id": d.get("id", "")}
        if d.get("doc_type") == "passport":
            data = d.get("data", {})
            item["summary"] = {
                "nationality": data.get("nationality", ""),
                "expiry": data.get("expiry", ""),
            }
        else:
            item["data"] = d.get("data", {})
        safe.append(item)
    return {"documents": safe}


@router.post("/user/documents")
def user_document_save(
    payload: DocumentSaveIn,
    request: Request,
    db: Session = Depends(get_db),
):
    principal = get_principal(request, guest_id=None)
    user = db.query(User).filter(User.id == principal.user_id).one_or_none()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    profile = dict(user.profile or {})
    docs = list(profile.get("documents", []))
    import uuid

    doc_id = str(uuid.uuid4())[:8]
    docs.append({
        "id": doc_id,
        "doc_type": payload.doc_type,
        "label": payload.label,
        "data": payload.data,
        "created_at": dt.datetime.now(dt.timezone.utc).isoformat(),
    })
    profile["documents"] = docs
    user.profile = profile
    db.commit()
    return {"ok": True, "doc_id": doc_id}


@router.delete("/user/documents/{doc_id}")
def user_document_delete(
    doc_id: str,
    request: Request,
    db: Session = Depends(get_db),
):
    principal = get_principal(request, guest_id=None)
    user = db.query(User).filter(User.id == principal.user_id).one_or_none()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    profile = dict(user.profile or {})
    docs = [d for d in profile.get("documents", []) if d.get("id") != doc_id]
    profile["documents"] = docs
    user.profile = profile
    db.commit()
    return {"ok": True}
