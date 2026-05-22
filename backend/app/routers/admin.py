"""
管理员后台 API：用户管理、统计数据。
使用环境变量 ADMIN_EMAIL / ADMIN_PASSWORD 鉴权，返回 JWT Admin Token。
"""
from __future__ import annotations

import os

import datetime as dt

from fastapi import APIRouter, Depends, Header, HTTPException, Query, status
from pydantic import BaseModel, Field
from sqlalchemy import func, text
from sqlalchemy.orm import Session

from app.auth_utils import create_access_token, decode_access_token
from app.db import get_db, session_scope
from app.models import EventLog, HotelBooking, RFP, ServiceOrder, Trip, User

router = APIRouter(prefix="/admin", tags=["admin"])

ADMIN_EMAIL = os.getenv("ADMIN_EMAIL", "admin@visepanda.com")
ADMIN_PASSWORD = os.getenv("ADMIN_PASSWORD", "admin123")


# ---- Schemas ----

class AdminLoginIn(BaseModel):
    email: str
    password: str


class AdminTokenOut(BaseModel):
    access_token: str
    token_type: str = "bearer"
    admin: dict


class UserListItem(BaseModel):
    id: str
    email: str | None
    display_name: str | None
    avatar_url: str | None
    google_id: str | None = None
    is_active: bool
    created_at: str | None
    last_login_at: str | None

    model_config = {"from_attributes": True}


class UserListOut(BaseModel):
    users: list[UserListItem]
    total: int
    page: int
    page_size: int


class DashboardStats(BaseModel):
    total_users: int
    active_users: int
    total_trips: int
    total_rfps: int
    total_hotel_bookings: int
    total_service_orders: int
    new_users_today: int
    new_users_this_week: int


# ---- Admin Auth ----

def require_admin(authorization: str | None = Header(default=None)) -> dict:
    """验证 Admin JWT Token。"""
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Missing or invalid token")
    token = authorization[len("Bearer "):]
    payload = decode_access_token(token)
    if not payload:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid or expired token")
    if payload.get("role") != "admin":
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Admin access required")
    return payload


@router.post("/login", response_model=AdminTokenOut)
def admin_login(payload: AdminLoginIn):
    """Admin login - uses credentials from environment variables."""
    if payload.email.strip().lower() != ADMIN_EMAIL.lower():
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid credentials")
    if payload.password != ADMIN_PASSWORD:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid credentials")

    token = create_access_token("admin", ADMIN_EMAIL)
    # 在 payload 中注入 role=admin
    import datetime as _dt
    from jose import jwt
    from app.auth_utils import SECRET_KEY, ALGORITHM, ACCESS_TOKEN_EXPIRE_MINUTES

    expire = _dt.datetime.now(_dt.timezone.utc) + _dt.timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    admin_payload = {
        "sub": "admin",
        "email": ADMIN_EMAIL,
        "role": "admin",
        "exp": expire,
        "iat": _dt.datetime.now(_dt.timezone.utc),
    }
    token = jwt.encode(admin_payload, SECRET_KEY, algorithm=ALGORITHM)

    return AdminTokenOut(
        access_token=token,
        admin={"email": ADMIN_EMAIL, "role": "admin"},
    )


# ---- User Management ----

@router.get("/users", response_model=UserListOut)
def list_users(
    q: str | None = Query(default=None, description="Search by email or name"),
    page: int = Query(default=1, ge=1),
    page_size: int = Query(default=20, ge=1, le=100),
    status_filter: str | None = Query(default=None, alias="status", description="active|disabled|all"),
    sort: str = Query(default="created_at", alias="sort_by"),
    order: str = Query(default="desc", alias="order"),
    _admin: dict = Depends(require_admin),
    db: Session = Depends(get_db),
):
    """User list with search, filter, sort, and pagination."""
    query = db.query(User)

    # 搜索
    if q:
        like = f"%{q}%"
        query = query.filter(
            (User.email.ilike(like)) | (User.display_name.ilike(like))
        )

    # 状态筛选
    if status_filter == "active":
        query = query.filter(User.is_active == True)
    elif status_filter == "disabled":
        query = query.filter(User.is_active == False)

    # 排序
    sort_col = getattr(User, sort, User.created_at)
    if order == "asc":
        query = query.order_by(sort_col.asc())
    else:
        query = query.order_by(sort_col.desc())

    total = query.count()
    users = query.offset((page - 1) * page_size).limit(page_size).all()

    return UserListOut(
        users=[
            UserListItem(
                id=u.id,
                email=u.email,
                display_name=u.display_name,
                avatar_url=u.avatar_url,
                google_id=u.google_id,
                is_active=u.is_active,
                created_at=u.created_at.isoformat() if u.created_at else None,
                last_login_at=u.last_login_at.isoformat() if u.last_login_at else None,
            )
            for u in users
        ],
        total=total,
        page=page,
        page_size=page_size,
    )


@router.get("/users/{user_id}")
def get_user_detail(
    user_id: str,
    _admin: dict = Depends(require_admin),
    db: Session = Depends(get_db),
):
    """获取单个用户详情。"""
    user = db.query(User).filter(User.id == user_id).one_or_none()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    trips = db.query(Trip).filter(Trip.user_id == user.id).count()
    rfps = db.query(RFP).filter(RFP.user_id == user.id).count()

    return {
        "user": {
            "id": user.id,
            "email": user.email,
            "display_name": user.display_name,
            "avatar_url": user.avatar_url,
            "google_id": user.google_id,
            "is_active": user.is_active,
            "profile": user.profile,
            "created_at": user.created_at.isoformat() if user.created_at else None,
            "last_login_at": user.last_login_at.isoformat() if user.last_login_at else None,
        },
        "stats": {
            "trips": trips,
            "rfps": rfps,
        },
    }


@router.post("/users/{user_id}/toggle")
def toggle_user_status(
    user_id: str,
    _admin: dict = Depends(require_admin),
    db: Session = Depends(get_db),
):
    """封禁/解封用户。"""
    user = db.query(User).filter(User.id == user_id).one_or_none()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    user.is_active = not user.is_active
    db.add(
        EventLog(
            entity_type="User",
            entity_id=user.id,
            event_type="user_toggled",
            payload={"is_active": user.is_active, "by": "admin"},
        )
    )
    db.commit()

    return {
        "user": {
            "id": user.id,
            "email": user.email,
            "is_active": user.is_active,
        }
    }


# ---- Dashboard Stats ----

@router.get("/stats", response_model=DashboardStats)
def dashboard_stats(
    _admin: dict = Depends(require_admin),
    db: Session = Depends(get_db),
):
    """后台仪表盘统计数据。"""
    now = dt.datetime.now(dt.timezone.utc)
    today_start = now.replace(hour=0, minute=0, second=0, microsecond=0)
    week_start = today_start - dt.timedelta(days=today_start.weekday())

    total_users = db.query(User).count()
    active_users = db.query(User).filter(User.is_active == True).count()
    total_trips = db.query(Trip).count()
    total_rfps = db.query(RFP).count()
    total_hotel_bookings = db.query(HotelBooking).count()
    total_service_orders = db.query(ServiceOrder).count()
    new_users_today = db.query(User).filter(User.created_at >= today_start).count()
    new_users_this_week = db.query(User).filter(User.created_at >= week_start).count()

    return DashboardStats(
        total_users=total_users,
        active_users=active_users,
        total_trips=total_trips,
        total_rfps=total_rfps,
        total_hotel_bookings=total_hotel_bookings,
        total_service_orders=total_service_orders,
        new_users_today=new_users_today,
        new_users_this_week=new_users_this_week,
    )
