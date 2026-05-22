from __future__ import annotations

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field

from app.db import session_scope
from app.models import EventLog, Supplier

router = APIRouter()


class SupplierCreateIn(BaseModel):
    name: str
    profile: dict = Field(default_factory=dict)  # cities/languages/tags/service_types


@router.post("/suppliers")
def supplier_create(payload: SupplierCreateIn):
    with session_scope() as db:
        s = Supplier(name=payload.name, profile=payload.profile, status="pending")
        db.add(s)
        db.flush()
        db.add(EventLog(entity_type="Supplier", entity_id=s.id, event_type="supplier_created", payload={}))
        return {"supplier": {"id": s.id, "status": s.status, "api_key": s.api_key}}


@router.post("/suppliers/{supplier_id}:enable")
def supplier_enable(supplier_id: str):
    with session_scope() as db:
        s = db.query(Supplier).filter(Supplier.id == supplier_id).one_or_none()
        if not s:
            raise HTTPException(status_code=404, detail="Supplier not found")
        s.status = "active"
        db.add(EventLog(entity_type="Supplier", entity_id=s.id, event_type="supplier_enabled", payload={}))
        return {"supplier": {"id": s.id, "status": s.status}}


@router.get("/suppliers/{supplier_id}")
def supplier_get(supplier_id: str):
    with session_scope() as db:
        s = db.query(Supplier).filter(Supplier.id == supplier_id).one_or_none()
        if not s:
            raise HTTPException(status_code=404, detail="Supplier not found")
        return {"supplier": {"id": s.id, "name": s.name, "status": s.status, "profile": s.profile}}

