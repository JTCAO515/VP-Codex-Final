from datetime import datetime
from typing import Optional
from uuid import UUID

from pydantic import BaseModel


class UserUpdate(BaseModel):
    display_name: Optional[str] = None
    role: Optional[str] = None
    status: Optional[str] = None


class UserAdminResponse(BaseModel):
    id: UUID
    email: str
    display_name: Optional[str]
    role: str
    status: str
    created_at: datetime
    last_login_at: Optional[datetime]

    model_config = {"from_attributes": True}


class AdminStats(BaseModel):
    total: int
    active: int
    pending: int
    disabled: int


class UserListResponse(BaseModel):
    users: list[UserAdminResponse]
    total: int
    page: int
    page_size: int
