"""
Vercel Python Runtime entrypoint.

Maps /api/* -> FastAPI backend
Static files (index.html, admin.html) served from project root.
"""
from starlette.applications import Starlette
from starlette.routing import Mount
from starlette.staticfiles import StaticFiles

from backend.app.main import create_app

backend_app = create_app()
app = Starlette(
    routes=[
        Mount("/api", app=backend_app),
        Mount("/", app=StaticFiles(directory=".", html=True), name="static"),
    ]
)
