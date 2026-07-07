from sqlalchemy import Column, String, Text, Integer, Float
from sqlalchemy.dialects.postgresql import ARRAY, JSONB

from app.database import Base


class Destination(Base):
    __tablename__ = "destinations"

    id = Column(String(50), primary_key=True)  # e.g. "beijing"
    name = Column(String(100), nullable=False)
    name_cn = Column(String(100), nullable=False)
    description = Column(Text, nullable=False)
    image_url = Column(String(500), nullable=True)
    tags = Column(ARRAY(String), default=[])
    must_see = Column(JSONB, default=[])  # [{name, description, image_url}]
    must_eat = Column(JSONB, default=[])
    stay_tips = Column(Text, nullable=True)
    best_days = Column(Integer, default=3)
    budget_range = Column(String(50), nullable=True)
    latitude = Column(Float, nullable=True)
    longitude = Column(Float, nullable=True)
