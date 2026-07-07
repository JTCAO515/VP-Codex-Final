from typing import Optional

from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.orm import Session
from sqlalchemy import func

from app.database import get_db
from app.models.user import User
from app.schemas.user import UserAdminResponse, UserListResponse, AdminStats, UserUpdate
from app.core.deps import get_current_admin

router = APIRouter(prefix="/api/admin", tags=["admin"])


@router.get("/me", response_model=UserAdminResponse)
def admin_me(
    admin: User = Depends(get_current_admin),
):
    """返回当前管理员信息。"""
    return admin


@router.get("/stats", response_model=AdminStats)
def get_stats(
    admin: User = Depends(get_current_admin),
    db: Session = Depends(get_db),
):
    """Dashboard 用户统计。"""
    total = db.query(func.count(User.id)).scalar() or 0
    active = db.query(func.count(User.id)).filter(User.status == "active").scalar() or 0
    pending = db.query(func.count(User.id)).filter(User.status == "pending").scalar() or 0
    disabled = db.query(func.count(User.id)).filter(User.status == "disabled").scalar() or 0
    return AdminStats(total=total, active=active, pending=pending, disabled=disabled)


@router.get("/users", response_model=UserListResponse)
def list_users(
    search: Optional[str] = Query(None),
    role: Optional[str] = Query(None),
    status: Optional[str] = Query(None),
    page: int = Query(1, ge=1),
    page_size: int = Query(20, ge=1, le=100),
    admin: User = Depends(get_current_admin),
    db: Session = Depends(get_db),
):
    """用户列表（分页+搜索+筛选）。"""
    query = db.query(User)

    if search:
        like = f"%{search}%"
        query = query.filter(User.email.ilike(like))
    if role:
        query = query.filter(User.role == role)
    if status:
        query = query.filter(User.status == status)

    total = query.count()
    users = query.order_by(User.created_at.desc()).offset((page - 1) * page_size).limit(page_size).all()
    items = [UserAdminResponse.model_validate(u) for u in users]

    return UserListResponse(users=items, total=total, page=page, page_size=page_size)


@router.get("/users/{user_id}", response_model=UserAdminResponse)
def get_user(
    user_id: str,
    admin: User = Depends(get_current_admin),
    db: Session = Depends(get_db),
):
    """用户详情。"""
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found")
    return user


@router.patch("/users/{user_id}", response_model=UserAdminResponse)
def update_user(
    user_id: str,
    req: UserUpdate,
    admin: User = Depends(get_current_admin),
    db: Session = Depends(get_db),
):
    """编辑用户（display_name/role/status）。"""
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found")
    if req.display_name is not None:
        user.display_name = req.display_name
    if req.role is not None:
        user.role = req.role
    if req.status is not None:
        user.status = req.status
    db.commit()
    db.refresh(user)
    return user
