from __future__ import annotations

from contextlib import asynccontextmanager

import os

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware

from app.db import init_db
from app.models import Base
from app.middleware import SecurityMiddleware, RateLimitMiddleware
from app.middleware import http_exception_handler, generic_exception_handler
from app.routers.chat import router as chat_router
from app.routers.config import router as config_router
from app.routers.health import router as health_router
from app.routers.hotel import router as hotel_router
from app.routers.kb import router as kb_router
from app.routers.payments import router as payments_router
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

    app = FastAPI(
        title="China Travel Agent",
        version="3.0.0",
        lifespan=lifespan,
        docs_url=None if not os.getenv("IS_DEV") else "/docs",
        redoc_url=None,
    )

    # CORS — development: all origins; production: go2china.space only
    is_prod = not os.getenv("IS_DEV", "")
    allow_origins = (
        ["https://go2china.space", "https://www.go2china.space"]
        if is_prod
        else ["*"]
    )
    app.add_middleware(
        CORSMiddleware,
        allow_origins=allow_origins,
        allow_credentials=True if is_prod else False,
        allow_methods=["GET", "POST", "PUT", "DELETE", "OPTIONS"],
        allow_headers=["*"],
    )

    # Security & rate limiting (after CORS)
    app.add_middleware(SecurityMiddleware)
    app.add_middleware(RateLimitMiddleware)

    # Error handlers (production-safe: no stack traces)
    app.add_exception_handler(HTTPException, http_exception_handler)
    app.add_exception_handler(Exception, generic_exception_handler)

    app.include_router(health_router, tags=["health"])
    app.include_router(config_router, tags=["config"])
    app.include_router(chat_router, tags=["chat"])
    app.include_router(trips_router, tags=["trips"])
    app.include_router(translate_router, tags=["translate"])
    app.include_router(kb_router, tags=["kb"])
    app.include_router(hotel_router, tags=["hotel"])
    app.include_router(payments_router, tags=["payments"])
    app.include_router(suppliers_router, tags=["suppliers"])
    app.include_router(rfps_router, tags=["rfp"])
    app.include_router(supplier_portal_router, tags=["supplier"])

    return app


app = create_app()
