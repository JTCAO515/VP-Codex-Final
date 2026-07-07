from datetime import datetime
from typing import Optional
from uuid import UUID

from pydantic import BaseModel


class TripCreate(BaseModel):
    title: str
    cities: list[str]
    days: int
    content: dict


class TripResponse(BaseModel):
    id: UUID
    title: str
    cities: list[str]
    days: int
    content: dict
    created_at: datetime
    updated_at: datetime

    model_config = {"from_attributes": True}


class TripListResponse(BaseModel):
    trips: list[TripResponse]
    total: int
