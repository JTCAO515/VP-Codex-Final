from app.schemas.auth import (
    RegisterRequest, RegisterResponse,
    LoginRequest, TokenResponse,
    VerifyEmailRequest, VerifyEmailResponse,
    ForgotPasswordRequest, ForgotPasswordResponse,
    ResetPasswordRequest, ResetPasswordResponse,
    UserResponse,
)
from app.schemas.user import UserAdminResponse, UserListResponse, AdminStats, UserUpdate
from app.schemas.destination import DestinationItem, DestinationDetail, DestinationListResponse
from app.schemas.trip import TripCreate, TripResponse, TripListResponse
