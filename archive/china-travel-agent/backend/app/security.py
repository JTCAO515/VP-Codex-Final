from __future__ import annotations

from fastapi import Depends, Header, HTTPException
from sqlalchemy.orm import Session

from app.db import get_db
from app.models import Supplier


def require_supplier(
    db: Session = Depends(get_db),
    x_api_key: str | None = Header(default=None, alias="X-API-Key"),
) -> Supplier:
    if not x_api_key:
        raise HTTPException(status_code=401, detail="Missing X-API-Key")
    supplier = db.query(Supplier).filter(Supplier.api_key == x_api_key).one_or_none()
    if not supplier:
        raise HTTPException(status_code=401, detail="Invalid API key")
    if supplier.status != "active":
        raise HTTPException(status_code=403, detail="Supplier not active")
    return supplier
