import uuid

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.user import User
from app.models.trip import Trip
from app.schemas.trip import TripCreate, TripResponse, TripListResponse
from app.core.deps import get_current_user

router = APIRouter(prefix="/api/trips", tags=["trips"])


@router.get("", response_model=TripListResponse)
def list_trips(
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    """获取当前用户的行程列表。"""
    trips = db.query(Trip).filter(Trip.user_id == user.id).order_by(Trip.created_at.desc()).all()
    return TripListResponse(
        trips=[TripResponse.model_validate(t) for t in trips],
        total=len(trips),
    )


@router.post("", response_model=TripResponse, status_code=status.HTTP_201_CREATED)
def create_trip(
    req: TripCreate,
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    """创建/保存行程。"""
    trip = Trip(
        id=uuid.uuid4(),
        user_id=user.id,
        title=req.title,
        cities=req.cities,
        days=req.days,
        content=req.content,
    )
    db.add(trip)
    db.commit()
    db.refresh(trip)
    return trip


@router.delete("/{trip_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_trip(
    trip_id: uuid.UUID,
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    """删除行程（仅本人可删）。"""
    trip = db.query(Trip).filter(Trip.id == trip_id, Trip.user_id == user.id).first()
    if not trip:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Trip not found")
    db.delete(trip)
    db.commit()
