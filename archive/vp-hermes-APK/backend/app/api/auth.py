import uuid
from datetime import datetime, timedelta

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.user import User
from app.models.auth_token import EmailVerification, PasswordReset
from app.schemas.auth import (
    RegisterRequest, RegisterResponse,
    LoginRequest, TokenResponse,
    VerifyEmailRequest, VerifyEmailResponse,
    ForgotPasswordRequest, ForgotPasswordResponse,
    ResetPasswordRequest, ResetPasswordResponse,
    UserResponse,
)
from app.core.security import hash_password, verify_password, create_access_token
from app.core.deps import get_current_user
from app.core.email import send_verification_email, send_password_reset_email

router = APIRouter(prefix="/api/auth", tags=["auth"])


@router.post("/register", response_model=RegisterResponse, status_code=status.HTTP_201_CREATED)
def register(req: RegisterRequest, db: Session = Depends(get_db)):
    """邮箱注册。"""
    existing = db.query(User).filter(User.email == req.email).first()
    if existing:
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="Email already registered")

    user = User(
        id=uuid.uuid4(),
        email=req.email,
        password_hash=hash_password(req.password),
        display_name=req.display_name,
        status="pending",
    )
    db.add(user)
    db.flush()

    # 创建验证 Token
    vtoken = str(uuid.uuid4())
    verification = EmailVerification(
        id=uuid.uuid4(),
        user_id=user.id,
        token=vtoken,
        expires_at=datetime.utcnow() + timedelta(hours=24),
    )
    db.add(verification)
    db.commit()

    send_verification_email(req.email, vtoken)

    return RegisterResponse(
        id=user.id,
        email=user.email,
        display_name=user.display_name,
        status=user.status,
    )


@router.post("/login", response_model=TokenResponse)
def login(req: LoginRequest, db: Session = Depends(get_db)):
    """邮箱密码登录。"""
    user = db.query(User).filter(User.email == req.email).first()
    if not user or not verify_password(req.password, user.password_hash):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid email or password")
    if user.status == "disabled":
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Account is disabled")

    user.last_login_at = datetime.utcnow()
    db.commit()

    token = create_access_token({"sub": str(user.id)})
    return TokenResponse(access_token=token)


@router.post("/logout")
def logout(user: User = Depends(get_current_user)):
    """登出（JWT 无状态，客户端丢弃 Token 即可）。"""
    return {"message": "Logged out successfully."}


@router.post("/verify-email", response_model=VerifyEmailResponse)
def verify_email(req: VerifyEmailRequest, db: Session = Depends(get_db)):
    """邮箱验证。"""
    verification = db.query(EmailVerification).filter(
        EmailVerification.token == req.token,
        EmailVerification.used == False,
    ).first()
    if not verification:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid or expired token")
    if verification.expires_at < datetime.utcnow():
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Token expired")

    user = db.query(User).filter(User.id == verification.user_id).first()
    if user:
        user.status = "active"
    verification.used = True
    db.commit()

    return VerifyEmailResponse()


@router.post("/forgot-password", response_model=ForgotPasswordResponse)
def forgot_password(req: ForgotPasswordRequest, db: Session = Depends(get_db)):
    """忘记密码 — 发送重置邮件。"""
    user = db.query(User).filter(User.email == req.email).first()
    # 无论邮箱是否存在，都返回同样信息（防止枚举）
    if not user:
        return ForgotPasswordResponse()

    rtoken = str(uuid.uuid4())
    reset = PasswordReset(
        id=uuid.uuid4(),
        user_id=user.id,
        token=rtoken,
        expires_at=datetime.utcnow() + timedelta(hours=1),
    )
    db.add(reset)
    db.commit()

    send_password_reset_email(req.email, rtoken)
    return ForgotPasswordResponse()


@router.post("/reset-password", response_model=ResetPasswordResponse)
def reset_password(req: ResetPasswordRequest, db: Session = Depends(get_db)):
    """重置密码。"""
    reset = db.query(PasswordReset).filter(
        PasswordReset.token == req.token,
        PasswordReset.used == False,
    ).first()
    if not reset:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid or expired token")
    if reset.expires_at < datetime.utcnow():
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Token expired")

    user = db.query(User).filter(User.id == reset.user_id).first()
    if user:
        user.password_hash = hash_password(req.new_password)
    reset.used = True
    db.commit()

    return ResetPasswordResponse()


@router.get("/me", response_model=UserResponse)
def get_me(user: User = Depends(get_current_user)):
    """获取当前用户信息。"""
    return user
