from typing import Optional

from fastapi import APIRouter, Depends, Query, HTTPException, status
from sqlalchemy.orm import Session
from sqlalchemy import or_

from app.database import get_db
from app.models.destination import Destination
from app.schemas.destination import DestinationItem, DestinationDetail, DestinationListResponse

router = APIRouter(prefix="/api/destinations", tags=["destinations"])


@router.get("", response_model=DestinationListResponse)
def list_destinations(
    search: Optional[str] = Query(None),
    tag: Optional[str] = Query(None),
    page: int = Query(1, ge=1),
    page_size: int = Query(20, ge=1, le=50),
    db: Session = Depends(get_db),
):
    """城市列表，支持搜索和标签筛选。"""
    query = db.query(Destination)

    if search:
        like = f"%{search}%"
        query = query.filter(
            or_(Destination.name.ilike(like), Destination.name_cn.ilike(like))
        )
    if tag:
        query = query.filter(Destination.tags.any(tag))

    total = query.count()
    destinations = query.order_by(Destination.id).offset((page - 1) * page_size).limit(page_size).all()

    items = [
        DestinationItem(
            id=d.id, name=d.name, name_cn=d.name_cn,
            description=d.description, image_url=d.image_url,
            tags=d.tags, best_days=d.best_days, budget_range=d.budget_range,
        )
        for d in destinations
    ]
    return DestinationListResponse(destinations=items, total=total)


@router.get("/{dest_id}", response_model=DestinationDetail)
def get_destination(dest_id: str, db: Session = Depends(get_db)):
    """城市详情。"""
    d = db.query(Destination).filter(Destination.id == dest_id).first()
    if not d:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Destination not found")
    return DestinationDetail(
        id=d.id, name=d.name, name_cn=d.name_cn,
        description=d.description, image_url=d.image_url,
        tags=d.tags, must_see=d.must_see, must_eat=d.must_eat,
        stay_tips=d.stay_tips, best_days=d.best_days,
        budget_range=d.budget_range,
        latitude=d.latitude, longitude=d.longitude,
    )
