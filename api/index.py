"""
Vercel Python Runtime entrypoint — API only.
Vercel serves static files (index.html, admin.html) natively from root.
"""
from starlette.applications import Starlette
from starlette.routing import Mount

from backend.app.main import create_app

backend_app = create_app()
app = Starlette(routes=[Mount("/api", app=backend_app)])
