from __future__ import annotations

import os
from contextlib import contextmanager
from pathlib import Path

from sqlalchemy import create_engine
from sqlalchemy.pool import StaticPool
from sqlalchemy.orm import Session, sessionmaker

DB_PATH = Path(__file__).resolve().parents[2] / "data.sqlite3"
DEFAULT_SQLITE_URL = f"sqlite:///{DB_PATH}"


def _is_pytest() -> bool:
    return "PYTEST_CURRENT_TEST" in os.environ


def _build_engine():
    # 1) Prefer external DB (Supabase Postgres) for persistence on Vercel
    db_url = os.getenv("DATABASE_URL", "").strip()
    if db_url:
        return create_engine(db_url, pool_pre_ping=True)

    # 2) Test mode: in-memory sqlite for isolation
    if _is_pytest():
        return create_engine(
            "sqlite+pysqlite:///:memory:",
            connect_args={"check_same_thread": False},
            poolclass=StaticPool,
        )

    # 3) Local dev fallback
    return create_engine(DEFAULT_SQLITE_URL, connect_args={"check_same_thread": False})


engine = _build_engine()
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
    if not os.getenv("DATABASE_URL"):
        DB_PATH.parent.mkdir(parents=True, exist_ok=True)
    base_metadata.create_all(bind=engine)
