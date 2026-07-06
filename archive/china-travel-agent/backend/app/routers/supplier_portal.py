from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel, Field
from sqlalchemy import Text, cast
from sqlalchemy.orm import Session

from app.db import get_db
from app.models import EventLog, Quote, RFP, ServiceOrder
from app.security import require_supplier

router = APIRouter()


@router.get("/supplier/rfps")
def supplier_rfps(db: Session = Depends(get_db), supplier=Depends(require_supplier)):
    rfps = (
        db.query(RFP)
        .filter(cast(RFP.supplier_targets, Text).like(f'%"{supplier.id}"%'))  # JSON-as-text fallback
        .all()
    )
    return {
        "rfps": [
            {"id": r.id, "status": r.status, "service_types": r.service_types, "requirements": r.requirements}
            for r in rfps
        ]
    }


@router.get("/supplier/rfps/{rfp_id}")
def supplier_rfp_get(rfp_id: str, db: Session = Depends(get_db), supplier=Depends(require_supplier)):
    r = db.query(RFP).filter(RFP.id == rfp_id).one_or_none()
    if not r:
        raise HTTPException(status_code=404, detail="RFP not found")
    if supplier.id not in (r.supplier_targets or []):
        raise HTTPException(status_code=403, detail="Not assigned")
    quotes = db.query(Quote).filter(Quote.rfp_id == r.id, Quote.supplier_id == supplier.id).all()
    return {
        "rfp": {"id": r.id, "status": r.status, "service_types": r.service_types, "requirements": r.requirements},
        "my_quotes": [
            {"id": q.id, "price": q.price, "proposal": q.proposal, "status": q.status, "revision": q.revision}
            for q in quotes
        ],
    }


class QuoteCreateIn(BaseModel):
    price: dict = Field(default_factory=dict)
    included: list[str] = Field(default_factory=list)
    excluded: list[str] = Field(default_factory=list)
    proposal: dict = Field(default_factory=dict)


@router.post("/supplier/rfps/{rfp_id}/quotes")
def supplier_quote_create(
    rfp_id: str,
    payload: QuoteCreateIn,
    db: Session = Depends(get_db),
    supplier=Depends(require_supplier),
):
    r = db.query(RFP).filter(RFP.id == rfp_id).one_or_none()
    if not r:
        raise HTTPException(status_code=404, detail="RFP not found")
    if supplier.id not in (r.supplier_targets or []):
        raise HTTPException(status_code=403, detail="Not assigned")

    q = Quote(
        rfp_id=r.id,
        supplier_id=supplier.id,
        price=payload.price,
        included=payload.included,
        excluded=payload.excluded,
        proposal=payload.proposal,
        status="submitted",
        revision=1,
    )
    db.add(q)
    db.flush()
    db.add(EventLog(entity_type="Quote", entity_id=q.id, event_type="quote_submitted", payload={"rfp_id": r.id}))
    db.commit()
    return {"quote": {"id": q.id, "status": q.status, "revision": q.revision}}


@router.post("/supplier/quotes/{quote_id}:revise")
def supplier_quote_revise(
    quote_id: str,
    payload: QuoteCreateIn,
    db: Session = Depends(get_db),
    supplier=Depends(require_supplier),
):
    q = db.query(Quote).filter(Quote.id == quote_id).one_or_none()
    if not q:
        raise HTTPException(status_code=404, detail="Quote not found")
    if q.supplier_id != supplier.id:
        raise HTTPException(status_code=403, detail="Not owner")
    q.price = payload.price
    q.included = payload.included
    q.excluded = payload.excluded
    q.proposal = payload.proposal
    q.revision += 1
    q.status = "revised"
    db.add(EventLog(entity_type="Quote", entity_id=q.id, event_type="quote_revised", payload={"revision": q.revision}))
    db.commit()
    return {"quote": {"id": q.id, "status": q.status, "revision": q.revision}}


class OrderStatusIn(BaseModel):
    status: str


@router.post("/supplier/orders/{order_id}/status")
def supplier_order_status(
    order_id: str,
    payload: OrderStatusIn,
    db: Session = Depends(get_db),
    supplier=Depends(require_supplier),
):
    o = db.query(ServiceOrder).filter(ServiceOrder.id == order_id).one_or_none()
    if not o:
        raise HTTPException(status_code=404, detail="Order not found")
    if o.supplier_id != supplier.id:
        raise HTTPException(status_code=403, detail="Not owner")
    # 最小状态机校验（避免乱跳）
    allowed_transitions = {
        "created": {"accepted_by_supplier", "cancelled"},
        "accepted_by_supplier": {"in_progress", "cancelled"},
        "in_progress": {"completed", "cancelled"},
        "completed": set(),
        "cancelled": set(),
    }
    if payload.status not in allowed_transitions.get(o.status, set()):
        raise HTTPException(status_code=409, detail=f"Invalid transition {o.status} -> {payload.status}")
    o.status = payload.status
    db.add(EventLog(entity_type="ServiceOrder", entity_id=o.id, event_type="order_status_updated", payload={"status": o.status}))
    db.commit()
    return {"order": {"id": o.id, "status": o.status}}


class FulfillmentIn(BaseModel):
    fulfillment_info: dict = Field(default_factory=dict)


@router.post("/supplier/orders/{order_id}/fulfillment")
def supplier_order_fulfillment(
    order_id: str,
    payload: FulfillmentIn,
    db: Session = Depends(get_db),
    supplier=Depends(require_supplier),
):
    o = db.query(ServiceOrder).filter(ServiceOrder.id == order_id).one_or_none()
    if not o:
        raise HTTPException(status_code=404, detail="Order not found")
    if o.supplier_id != supplier.id:
        raise HTTPException(status_code=403, detail="Not owner")
    o.fulfillment_info = payload.fulfillment_info
    db.add(
        EventLog(
            entity_type="ServiceOrder",
            entity_id=o.id,
            event_type="fulfillment_updated",
            payload={"keys": list(payload.fulfillment_info.keys())},
        )
    )
    db.commit()
    return {"order": {"id": o.id, "status": o.status, "fulfillment_info": o.fulfillment_info}}


@router.get("/supplier/orders")
def supplier_orders(db: Session = Depends(get_db), supplier=Depends(require_supplier)):
    orders = db.query(ServiceOrder).filter(ServiceOrder.supplier_id == supplier.id).all()
    return {
        "orders": [
            {
                "id": o.id,
                "status": o.status,
                "rfp_id": o.rfp_id,
                "chosen_quote_id": o.chosen_quote_id,
                "fulfillment_info": o.fulfillment_info,
            }
            for o in orders
        ]
    }
