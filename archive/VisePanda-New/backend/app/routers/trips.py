from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, Request
from sqlalchemy.orm import Session

from app.auth import get_principal
from app.db import get_db, session_scope
from app.models import ChatMessage, HotelBooking, RFP, ServiceOrder, Trip

router = APIRouter()


@router.get("/trips/{trip_id}")
def trip_get(trip_id: str, request: Request, db: Session = Depends(get_db)):
    principal = get_principal(request, guest_id=None)
    t = db.query(Trip).filter(Trip.id == trip_id, Trip.user_id == principal.user_id).one_or_none()
    if not t:
        raise HTTPException(status_code=404, detail="Trip not found")
    return {
        "trip": {
            "id": t.id,
            "title": t.title,
            "updated_at": t.updated_at.isoformat() if t.updated_at else None,
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


@router.get("/trips/{trip_id}/hotel-bookings")
def trip_hotel_bookings(trip_id: str, request: Request, db: Session = Depends(get_db)):
    principal = get_principal(request, guest_id=None)
    t = db.query(Trip).filter(Trip.id == trip_id, Trip.user_id == principal.user_id).one_or_none()
    if not t:
        raise HTTPException(status_code=404, detail="Trip not found")
    bookings = db.query(HotelBooking).filter(HotelBooking.trip_id == trip_id).order_by(HotelBooking.created_at.desc()).all()
    return {
        "bookings": [
            {"id": b.id, "status": b.status, "offer_id": b.offer_id, "created_at": b.created_at.isoformat()}
            for b in bookings
        ]
    }


@router.get("/trips/{trip_id}/rfps")
def trip_rfps(trip_id: str, request: Request, db: Session = Depends(get_db)):
    principal = get_principal(request, guest_id=None)
    t = db.query(Trip).filter(Trip.id == trip_id, Trip.user_id == principal.user_id).one_or_none()
    if not t:
        raise HTTPException(status_code=404, detail="Trip not found")
    rfps = db.query(RFP).filter(RFP.trip_id == trip_id, RFP.user_id == principal.user_id).order_by(RFP.created_at.desc()).all()
    return {
        "rfps": [
            {
                "id": r.id,
                "status": r.status,
                "service_types": r.service_types,
                "requirements": r.requirements,
                "created_at": r.created_at.isoformat(),
            }
            for r in rfps
        ]
    }


@router.get("/trips/{trip_id}/service-orders")
def trip_service_orders(trip_id: str, request: Request, db: Session = Depends(get_db)):
    principal = get_principal(request, guest_id=None)
    t = db.query(Trip).filter(Trip.id == trip_id, Trip.user_id == principal.user_id).one_or_none()
    if not t:
        raise HTTPException(status_code=404, detail="Trip not found")
    rfps = db.query(RFP).filter(RFP.trip_id == trip_id, RFP.user_id == principal.user_id).all()
    rfp_ids = [r.id for r in rfps]
    if not rfp_ids:
        return {"orders": []}
    orders = db.query(ServiceOrder).filter(ServiceOrder.rfp_id.in_(rfp_ids)).order_by(ServiceOrder.created_at.desc()).all()
    return {
        "orders": [
            {
                "id": o.id,
                "status": o.status,
                "supplier_id": o.supplier_id,
                "rfp_id": o.rfp_id,
                "chosen_quote_id": o.chosen_quote_id,
                "fulfillment_info": o.fulfillment_info,
                "created_at": o.created_at.isoformat(),
            }
            for o in orders
        ]
    }
