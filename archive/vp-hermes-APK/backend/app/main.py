from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api import auth_router, dest_router, chat_router, trips_router, admin_router
from app.database import engine, Base
from app.config import settings
from app.seed.destinations import SEED_DESTINATIONS
from app.models.destination import Destination

app = FastAPI(title="VisePanda API", version="0.1.0")

# CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Routers
app.include_router(auth_router)
app.include_router(dest_router)
app.include_router(chat_router)
app.include_router(trips_router)
app.include_router(admin_router)


@app.on_event("startup")
def on_startup():
    """启动时创建表并导入种子数据。"""
    Base.metadata.create_all(bind=engine)
    _seed_destinations()


def _seed_destinations():
    """导入城市种子数据（如果表为空）。"""
    from sqlalchemy.orm import Session
    from app.database import SessionLocal

    db: Session = SessionLocal()
    try:
        count = db.query(Destination).count()
        if count > 0:
            return
        for data in SEED_DESTINATIONS:
            dest = Destination(**data)
            db.add(dest)
        db.commit()
        print(f"[SEED] Imported {len(SEED_DESTINATIONS)} destinations.")
    finally:
        db.close()


@app.get("/api/health")
def health():
    return {"status": "ok", "version": "0.1.0"}
