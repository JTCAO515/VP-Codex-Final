from datetime import datetime
from typing import Optional
from uuid import UUID

from pydantic import BaseModel, EmailStr


# --- Register ---
class RegisterRequest(BaseModel):
    email: EmailStr
    password: str
    display_name: Optional[str] = None


class RegisterResponse(BaseModel):
    id: UUID
    email: str
    display_name: Optional[str]
    status: str
    message: str = "Please check your email to verify your account."


# --- Login ---
class LoginRequest(BaseModel):
    email: EmailStr
    password: str


class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"


# --- Email Verification ---
class VerifyEmailRequest(BaseModel):
    token: str


class VerifyEmailResponse(BaseModel):
    message: str = "Email verified successfully."


# --- Forgot Password ---
class ForgotPasswordRequest(BaseModel):
    email: EmailStr


class ForgotPasswordResponse(BaseModel):
    message: str = "If the email exists, a reset link has been sent."


# --- Reset Password ---
class ResetPasswordRequest(BaseModel):
    token: str
    new_password: str


class ResetPasswordResponse(BaseModel):
    message: str = "Password reset successfully."


# --- User Info ---
class UserResponse(BaseModel):
    id: UUID
    email: str
    display_name: Optional[str]
    avatar_url: Optional[str]
    role: str
    status: str
    created_at: datetime
    last_login_at: Optional[datetime]

    model_config = {"from_attributes": True}
