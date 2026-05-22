from __future__ import annotations

from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.db import init_db
from app.models import Base
from app.routers.chat import router as chat_router
from app.routers.health import router as health_router
from app.routers.hotel import router as hotel_router
from app.routers.kb import router as kb_router
from app.routers.rfps import router as rfps_router
from app.routers.supplier_portal import router as supplier_portal_router
from app.routers.suppliers import router as suppliers_router
from app.routers.trips import router as trips_router
from app.routers.translate import router as translate_router


def create_app() -> FastAPI:
    @asynccontextmanager
    async def lifespan(app: FastAPI):
        init_db(Base.metadata)
        yield

    app = FastAPI(title="China Travel Agent MVP", version="2.1.0", lifespan=lifespan)

    # 便于本地静态页面/跨域联调（生产环境请收紧 allow_origins）
    app.add_middleware(
        CORSMiddleware,
        allow_origins=["*"],
        allow_credentials=False,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    app.include_router(health_router, tags=["health"])
    app.include_router(chat_router, tags=["chat"])
    app.include_router(trips_router, tags=["trips"])
    app.include_router(translate_router, tags=["translate"])
    app.include_router(kb_router, tags=["kb"])
    app.include_router(hotel_router, tags=["hotel"])
    app.include_router(suppliers_router, tags=["suppliers"])
    app.include_router(rfps_router, tags=["rfp"])
    app.include_router(supplier_portal_router, tags=["supplier"])

    return app


app = create_app()
