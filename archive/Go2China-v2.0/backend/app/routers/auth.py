"""
认证路由：注册、邮箱登录、Google OAuth 登录、获取当前用户。
"""
from __future__ import annotations

import datetime as dt
import os

import httpx
from fastapi import APIRouter, Depends, Header, HTTPException, status
from pydantic import BaseModel, EmailStr, Field
from sqlalchemy.orm import Session

from app.auth_utils import (
    create_access_token,
    decode_access_token,
    hash_password,
    verify_password,
)
from app.db import get_db
from app.models import User

router = APIRouter(prefix="/auth", tags=["auth"])

GOOGLE_CLIENT_ID = os.getenv("GOOGLE_CLIENT_ID", "")


# ---- Pydantic Schemas ----

class RegisterIn(BaseModel):
    email: str = Field(..., min_length=3, max_length=255)
    password: str = Field(..., min_length=6, max_length=128)
    display_name: str | None = Field(default=None, max_length=100)


class LoginIn(BaseModel):
    email: str = Field(..., min_length=3, max_length=255)
    password: str = Field(..., min_length=1, max_length=128)


class GoogleLoginIn(BaseModel):
    id_token: str = Field(..., min_length=10)


class TokenOut(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user: dict


class UserOut(BaseModel):
    id: str
    email: str | None
    display_name: str | None
    avatar_url: str | None
    is_active: bool
    created_at: str | None

    model_config = {"from_attributes": True}


# ---- 辅助函数 ----

def _user_to_dict(u: User) -> dict:
    return {
        "id": u.id,
        "email": u.email,
        "display_name": u.display_name,
        "avatar_url": u.avatar_url,
        "is_active": u.is_active,
        "created_at": u.created_at.isoformat() if u.created_at else None,
    }


def _require_user(token: str, db: Session) -> User:
    """从 Bearer token 中解析 user_id 并返回 User 对象。"""
    if not token:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Missing authorization header")
    payload = decode_access_token(token)
    if not payload:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid or expired token")
    user_id = payload.get("sub")
    if not user_id:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token payload")
    user = db.query(User).filter(User.id == user_id).one_or_none()
    if not user:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="User not found")
    if not user.is_active:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Account disabled")
    return user


def get_current_user(
    authorization: str | None = Header(default=None),
    db: Session = Depends(get_db),
) -> User:
    """FastAPI dependency: 从 Authorization header 解析当前用户。"""
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Missing or invalid authorization")
    token = authorization[len("Bearer "):]
    return _require_user(token, db)


# ---- 路由 ----

@router.post("/register", response_model=TokenOut)
def register(payload: RegisterIn, db: Session = Depends(get_db)):
    """邮箱 + 密码注册。"""
    email = payload.email.strip().lower()
    existing = db.query(User).filter(User.email == email).one_or_none()
    if existing:
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="Email already registered")

    user = User(
        email=email,
        display_name=payload.display_name or email.split("@")[0],
        password_hash=hash_password(payload.password),
    )
    db.add(user)
    db.commit()
    db.refresh(user)

    token = create_access_token(user.id, user.email)
    return TokenOut(access_token=token, user=_user_to_dict(user))


@router.post("/login", response_model=TokenOut)
def login(payload: LoginIn, db: Session = Depends(get_db)):
    """邮箱 + 密码登录。"""
    email = payload.email.strip().lower()
    user = db.query(User).filter(User.email == email).one_or_none()
    if not user:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid email or password")
    if not user.password_hash:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="This account uses Google login. Please sign in with Google.",
        )
    if not verify_password(payload.password, user.password_hash):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid email or password")
    if not user.is_active:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Account disabled")

    user.last_login_at = dt.datetime.now(dt.timezone.utc)
    db.commit()

    token = create_access_token(user.id, user.email)
    return TokenOut(access_token=token, user=_user_to_dict(user))


@router.post("/google", response_model=TokenOut)
async def google_login(payload: GoogleLoginIn, db: Session = Depends(get_db)):
    """Google OAuth 登录：验证前端传来的 Google ID token，创建或查找用户，返回 JWT。"""
    if not GOOGLE_CLIENT_ID:
        raise HTTPException(
            status_code=status.HTTP_501_NOT_IMPLEMENTED,
            detail="Google OAuth not configured. Set GOOGLE_CLIENT_ID environment variable.",
        )

    # --- 验证 Google ID token ---
    verify_url = f"https://oauth2.googleapis.com/tokeninfo?id_token={payload.id_token}"
    try:
        async with httpx.AsyncClient(timeout=15) as client:
            r = await client.get(verify_url)
            r.raise_for_status()
            token_info = r.json()
    except httpx.HTTPError:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Failed to verify Google token")

    # 校验 audience
    if token_info.get("aud") != GOOGLE_CLIENT_ID:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid Google token audience")

    google_id = token_info.get("sub")
    email = (token_info.get("email") or "").strip().lower()
    display_name = token_info.get("name") or email.split("@")[0]
    avatar_url = token_info.get("picture")

    if not google_id or not email:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Incomplete Google token info")

    # --- 查找或创建用户 ---
    user = db.query(User).filter(User.google_id == google_id).one_or_none()
    if not user:
        # 尝试按 email 查找（可能之前邮箱注册过）
        user = db.query(User).filter(User.email == email).one_or_none()
        if user:
            # 关联 Google ID 到已有账号
            user.google_id = google_id
            user.avatar_url = user.avatar_url or avatar_url
            user.display_name = user.display_name or display_name
        else:
            # 创建新用户
            user = User(
                email=email,
                google_id=google_id,
                display_name=display_name,
                avatar_url=avatar_url,
            )
            db.add(user)

    user.last_login_at = dt.datetime.now(dt.timezone.utc)
    db.commit()
    db.refresh(user)

    token = create_access_token(user.id, user.email)
    return TokenOut(access_token=token, user=_user_to_dict(user))


@router.get("/me", response_model=UserOut)
def me(current_user: User = Depends(get_current_user)):
    """获取当前登录用户信息。"""
    return UserOut(
        id=current_user.id,
        email=current_user.email,
        display_name=current_user.display_name,
        avatar_url=current_user.avatar_url,
        is_active=current_user.is_active,
        created_at=current_user.created_at.isoformat() if current_user.created_at else None,
    )
