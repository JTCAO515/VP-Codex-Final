import uuid
from datetime import datetime

from sqlalchemy import Column, String, Boolean, DateTime, Enum, Text, Integer, Float
from sqlalchemy.dialects.postgresql import UUID, ARRAY, JSONB
from sqlalchemy.orm import relationship

from app.database import Base


class User(Base):
    __tablename__ = "users"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    email = Column(String(255), unique=True, nullable=False, index=True)
    password_hash = Column(String(255), nullable=False)
    display_name = Column(String(100), nullable=True)
    avatar_url = Column(Text, nullable=True)
    role = Column(Enum("user", "admin", name="user_role"), default="user", nullable=False)
    status = Column(Enum("pending", "active", "disabled", name="user_status"), default="pending", nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)
    last_login_at = Column(DateTime, nullable=True)

    trips = relationship("Trip", back_populates="user", cascade="all, delete-orphan")
