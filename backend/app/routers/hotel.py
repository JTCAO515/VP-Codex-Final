from __future__ import annotations

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field

from app.db import session_scope
from app.models import EventLog, HotelBooking, Trip
from app.providers import hotel_provider

router = APIRouter()

ALLOWED_BOOKING_STATUSES = {"created", "confirmed", "failed", "cancelled"}


class HotelSearchIn(BaseModel):
    city: str
    check_in: str
    check_out: str
    adults: int = Field(ge=1, le=8, default=2)


@router.post("/hotel/search")
def hotel_search(payload: HotelSearchIn):
    offers = hotel_provider.search(payload.city, payload.check_in, payload.check_out, payload.adults)
    return {"offers": offers}


class HotelBookingIn(BaseModel):
    trip_id: str
    offer_id: str
    guest_info: dict = Field(default_factory=dict)


@router.post("/hotel/bookings")
def hotel_book(payload: HotelBookingIn):
    with session_scope() as db:
        trip = db.query(Trip).filter(Trip.id == payload.trip_id).one_or_none()
        if not trip:
            raise HTTPException(status_code=404, detail="Trip not found")

        booking = HotelBooking(trip_id=trip.id, offer_id=payload.offer_id, guest_info=payload.guest_info)
        db.add(booking)
        db.flush()
        db.add(
            EventLog(
                entity_type="HotelBooking",
                entity_id=booking.id,
                event_type="booking_created",
                payload={"offer_id": payload.offer_id},
            )
        )

        provider_res = hotel_provider.create_booking(payload.offer_id, payload.guest_info)
        booking.provider_payload = provider_res
        booking.status = provider_res.get("status", "confirmed")
        if booking.status not in ALLOWED_BOOKING_STATUSES:
            booking.status = "confirmed"
        db.add(
            EventLog(
                entity_type="HotelBooking",
                entity_id=booking.id,
                event_type="booking_confirmed" if booking.status == "confirmed" else "booking_failed",
                payload=provider_res,
            )
        )

        return {"booking": {"id": booking.id, "status": booking.status, "provider": provider_res}}


@router.get("/hotel/bookings/{booking_id}")
def hotel_booking_get(booking_id: str):
    with session_scope() as db:
        b = db.query(HotelBooking).filter(HotelBooking.id == booking_id).one_or_none()
        if not b:
            raise HTTPException(status_code=404, detail="Booking not found")
        return {"booking": {"id": b.id, "status": b.status, "offer_id": b.offer_id, "guest_info": b.guest_info}}


@router.post("/hotel/bookings/{booking_id}:cancel")
def hotel_booking_cancel(booking_id: str):
    with session_scope() as db:
        b = db.query(HotelBooking).filter(HotelBooking.id == booking_id).one_or_none()
        if not b:
            raise HTTPException(status_code=404, detail="Booking not found")
        if b.status == "cancelled":
            return {"booking": {"id": b.id, "status": b.status}}
        if b.status == "failed":
            raise HTTPException(status_code=409, detail="Cannot cancel a failed booking")
        b.status = "cancelled"
        db.add(
            EventLog(
                entity_type="HotelBooking",
                entity_id=b.id,
                event_type="booking_cancelled",
                payload={},
            )
        )
        return {"booking": {"id": b.id, "status": b.status}}
