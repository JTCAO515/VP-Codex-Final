"""v2.6: Payment API router."""
from __future__ import annotations

import datetime as dt

from fastapi import APIRouter, HTTPException, Request
from pydantic import BaseModel, Field

from app.auth import get_principal
from app.db import session_scope
from app.models import EventLog, Payment, User

router = APIRouter()

ALLOWED_STATUSES = {"pending_payment", "paid", "refunding", "refunded", "expired"}
ALLOWED_ENTITIES = {"hotel_booking", "service_order"}


class PaymentCreateIn(BaseModel):
    entity_type: str
    entity_id: str
    amount: int = Field(ge=1)  # in cents
    currency: str = "CNY"


@router.post("/payments")
def payment_create(payload: PaymentCreateIn, request: Request):
    """Create a payment. Requires login."""
    principal = get_principal(request, guest_id=None)
    if payload.entity_type not in ALLOWED_ENTITIES:
        raise HTTPException(status_code=400, detail=f"Invalid entity_type: {payload.entity_type}")

    with session_scope() as db:
        user = db.query(User).filter(User.id == principal.user_id).one_or_none()
        if not user:
            user = User(id=principal.user_id, profile={})
            db.add(user)
            db.flush()

        payment = Payment(
            entity_type=payload.entity_type,
            entity_id=payload.entity_id,
            user_id=principal.user_id,
            amount=payload.amount,
            currency=payload.currency,
            status="pending_payment",
            provider="mock",
        )
        db.add(payment)
        db.flush()
        db.add(EventLog(entity_type="Payment", entity_id=payment.id, event_type="payment_created", payload={"amount": payload.amount}))
        return {"payment": _serialize(payment)}


@router.get("/payments/{payment_id}")
def payment_get(payment_id: str, request: Request):
    """Get payment status. Requires login, user-scoped."""
    principal = get_principal(request, guest_id=None)
    with session_scope() as db:
        p = db.query(Payment).filter(Payment.id == payment_id, Payment.user_id == principal.user_id).one_or_none()
        if not p:
            raise HTTPException(status_code=404, detail="Payment not found")
        return {"payment": _serialize(p)}


@router.post("/payments/{payment_id}:pay")
def payment_pay(payment_id: str, request: Request):
    """Simulate payment success. Requires login, user-scoped."""
    principal = get_principal(request, guest_id=None)
    with session_scope() as db:
        p = db.query(Payment).filter(Payment.id == payment_id, Payment.user_id == principal.user_id).one_or_none()
        if not p:
            raise HTTPException(status_code=404, detail="Payment not found")
        if p.status != "pending_payment":
            raise HTTPException(status_code=400, detail=f"Cannot pay: current status is {p.status}")

        p.status = "paid"
        p.paid_at = dt.datetime.now(dt.timezone.utc)
        p.provider_ref = f"MOCK-{payment_id[:12]}"
        db.add(EventLog(entity_type="Payment", entity_id=p.id, event_type="payment_paid", payload={}))
        return {"payment": _serialize(p)}


@router.post("/payments/{payment_id}:refund")
def payment_refund(payment_id: str, request: Request):
    """Refund a paid payment. Requires login, user-scoped."""
    principal = get_principal(request, guest_id=None)
    with session_scope() as db:
        p = db.query(Payment).filter(Payment.id == payment_id, Payment.user_id == principal.user_id).one_or_none()
        if not p:
            raise HTTPException(status_code=404, detail="Payment not found")
        if p.status != "paid":
            raise HTTPException(status_code=400, detail=f"Cannot refund: current status is {p.status}")

        p.status = "refunded"
        db.add(EventLog(entity_type="Payment", entity_id=p.id, event_type="payment_refunded", payload={}))
        return {"payment": _serialize(p)}


def _serialize(p: Payment) -> dict:
    return {
        "id": p.id,
        "entity_type": p.entity_type,
        "entity_id": p.entity_id,
        "amount": p.amount,
        "currency": p.currency,
        "status": p.status,
        "provider": p.provider,
        "provider_ref": p.provider_ref,
        "paid_at": p.paid_at.isoformat() if p.paid_at else None,
        "created_at": p.created_at.isoformat() if p.created_at else None,
    }
