from __future__ import annotations

import os
from contextlib import contextmanager
from pathlib import Path

from sqlalchemy import create_engine
from sqlalchemy.orm import Session, sessionmaker

# Vercel: use /tmp/ (the only writable directory in serverless)
if os.getenv("VERCEL"):
    DB_PATH = Path("/tmp/data.sqlite3")
else:
    DB_PATH = Path(__file__).resolve().parents[1] / "data.sqlite3"

SQLALCHEMY_DATABASE_URL = f"sqlite:///{DB_PATH}"

engine = create_engine(
    SQLALCHEMY_DATABASE_URL,
    connect_args={"check_same_thread": False},
)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


@contextmanager
def session_scope():
    db: Session = SessionLocal()
    try:
        yield db
        db.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


def get_db():
    """
    FastAPI dependency: provides a DB session per request.
    """
    db: Session = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def init_db(base_metadata) -> None:
    DB_PATH.parent.mkdir(parents=True, exist_ok=True)
    base_metadata.create_all(bind=engine)
