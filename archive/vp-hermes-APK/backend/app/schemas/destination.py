from typing import Optional

from pydantic import BaseModel


class DestinationItem(BaseModel):
    id: str
    name: str
    name_cn: str
    description: str
    image_url: Optional[str]
    tags: list[str]
    best_days: int
    budget_range: Optional[str]

    model_config = {"from_attributes": True}


class DestinationDetail(BaseModel):
    id: str
    name: str
    name_cn: str
    description: str
    image_url: Optional[str]
    tags: list[str]
    must_see: list[dict]
    must_eat: list[dict]
    stay_tips: Optional[str]
    best_days: int
    budget_range: Optional[str]
    latitude: Optional[float]
    longitude: Optional[float]

    model_config = {"from_attributes": True}


class DestinationListResponse(BaseModel):
    destinations: list[DestinationItem]
    total: int
