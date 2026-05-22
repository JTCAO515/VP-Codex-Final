from __future__ import annotations

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field

from app.db import session_scope
from app.models import EventLog, Quote, RFP, ServiceOrder, Supplier, Trip, User

router = APIRouter()


def _match_suppliers(db, rfp: RFP) -> list[str]:
    """
    最小匹配：按城市（rfp.requirements.city）与语言（requirements.language）过滤 active 供应商。
    """
    req = rfp.requirements or {}
    city = (req.get("city") or "").lower()
    lang = (req.get("language") or "").lower()
    q = db.query(Supplier).filter(Supplier.status == "active")
    hits: list[str] = []
    for s in q.all():
        prof = s.profile or {}
        cities = [c.lower() for c in prof.get("cities", [])]
        langs = [l.lower() for l in prof.get("languages", [])]
        if city and cities and city not in cities:
            continue
        if lang and langs and lang not in langs:
            continue
        hits.append(s.id)
    return hits[:50]


class RfpCreateIn(BaseModel):
    user_id: str
    trip_id: str
    service_types: list[str] = Field(default_factory=list)
    requirements: dict = Field(default_factory=dict)  # city/language/budget_range/preferences


@router.post("/rfps")
def rfp_create(payload: RfpCreateIn):
    with session_scope() as db:
        user = db.query(User).filter(User.id == payload.user_id).one_or_none()
        if not user:
            user = User(id=payload.user_id, profile={})
            db.add(user)
        trip = db.query(Trip).filter(Trip.id == payload.trip_id).one_or_none()
        if not trip:
            trip = Trip(id=payload.trip_id, user_id=user.id)
            db.add(trip)

        rfp = RFP(
            user_id=user.id,
            trip_id=trip.id,
            service_types=payload.service_types,
            requirements=payload.requirements,
            status="open",
        )
        db.add(rfp)
        db.flush()

        targets = _match_suppliers(db, rfp)
        rfp.supplier_targets = targets
        db.add(EventLog(entity_type="RFP", entity_id=rfp.id, event_type="rfp_created", payload={"targets": targets}))

        return {"rfp": {"id": rfp.id, "status": rfp.status, "supplier_targets": targets}}


@router.get("/rfps/{rfp_id}")
def rfp_get(rfp_id: str):
    with session_scope() as db:
        rfp = db.query(RFP).filter(RFP.id == rfp_id).one_or_none()
        if not rfp:
            raise HTTPException(status_code=404, detail="RFP not found")
        quotes = db.query(Quote).filter(Quote.rfp_id == rfp.id).all()
        return {
            "rfp": {
                "id": rfp.id,
                "status": rfp.status,
                "service_types": rfp.service_types,
                "requirements": rfp.requirements,
                "supplier_targets": rfp.supplier_targets,
            },
            "quotes": [
                {
                    "id": q.id,
                    "supplier_id": q.supplier_id,
                    "price": q.price,
                    "included": q.included,
                    "excluded": q.excluded,
                    "proposal": q.proposal,
                    "status": q.status,
                    "revision": q.revision,
                }
                for q in quotes
            ],
        }


class ServiceOrderCreateIn(BaseModel):
    rfp_id: str
    chosen_quote_id: str


@router.post("/service-orders")
def service_order_create(payload: ServiceOrderCreateIn):
    with session_scope() as db:
        q = db.query(Quote).filter(Quote.id == payload.chosen_quote_id).one_or_none()
        if not q or q.rfp_id != payload.rfp_id:
            raise HTTPException(status_code=400, detail="Invalid quote")
        order = ServiceOrder(rfp_id=payload.rfp_id, chosen_quote_id=q.id, supplier_id=q.supplier_id, status="created")
        db.add(order)
        db.flush()
        db.add(
            EventLog(
                entity_type="ServiceOrder",
                entity_id=order.id,
                event_type="service_order_created",
                payload={"rfp_id": payload.rfp_id, "quote_id": q.id},
            )
        )
        return {"order": {"id": order.id, "status": order.status, "supplier_id": order.supplier_id}}


@router.get("/service-orders/{order_id}")
def service_order_get(order_id: str):
    with session_scope() as db:
        o = db.query(ServiceOrder).filter(ServiceOrder.id == order_id).one_or_none()
        if not o:
            raise HTTPException(status_code=404, detail="Order not found")
        return {
            "order": {
                "id": o.id,
                "status": o.status,
                "supplier_id": o.supplier_id,
                "rfp_id": o.rfp_id,
                "chosen_quote_id": o.chosen_quote_id,
                "fulfillment_info": o.fulfillment_info,
            }
        }


class RfpShortlistIn(BaseModel):
    shortlisted_quote_ids: list[str] = Field(default_factory=list)


@router.post("/rfps/{rfp_id}:shortlist")
def rfp_shortlist(rfp_id: str, payload: RfpShortlistIn):
    with session_scope() as db:
        rfp = db.query(RFP).filter(RFP.id == rfp_id).one_or_none()
        if not rfp:
            raise HTTPException(status_code=404, detail="RFP not found")
        # 仅做轻量记录：把 shortlist 存在 requirements 中，避免额外建表
        rfp.requirements = dict(rfp.requirements or {})
        rfp.requirements["shortlisted_quote_ids"] = payload.shortlisted_quote_ids
        rfp.status = "shortlisted"
        db.add(
            EventLog(
                entity_type="RFP",
                entity_id=rfp.id,
                event_type="rfp_shortlisted",
                payload={"shortlisted_quote_ids": payload.shortlisted_quote_ids},
            )
        )
        return {"rfp": {"id": rfp.id, "status": rfp.status, "shortlisted_quote_ids": payload.shortlisted_quote_ids}}
