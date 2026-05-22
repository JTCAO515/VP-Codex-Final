"""
Vercel Python Runtime entrypoint.

This file must expose a top-level ASGI app named `app`.
We mount the existing FastAPI backend under `/api` so that:
- GET /api/health
- POST /api/chat/messages
... work on the same Vercel domain as the static frontend.
"""

from starlette.applications import Starlette
from starlette.routing import Mount

from backend.app.main import create_app

backend_app = create_app()
app = Starlette(routes=[Mount("/api", app=backend_app)])

