from __future__ import annotations

import os
import tempfile
from contextlib import contextmanager
from pathlib import Path

from sqlalchemy import create_engine
from sqlalchemy.orm import Session, sessionmaker

# SQLite path strategy:
# - Allow override via SQLITE_PATH
# - On Vercel/serverless, only temp dir is writable (tempfile.gettempdir() => "/tmp")
# - On local dev, keep DB inside backend/ by default
_override = os.getenv("SQLITE_PATH")
if _override:
    DB_PATH = Path(_override)
elif os.getenv("VERCEL"):
    DB_PATH = Path(tempfile.gettempdir()) / "visepanda.sqlite3"
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
    # Best-effort create the db file to avoid \"unable to open database file\" on some platforms
    try:
        DB_PATH.touch(exist_ok=True)
    except Exception:
        pass
    base_metadata.create_all(bind=engine)
