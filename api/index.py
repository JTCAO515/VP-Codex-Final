"""
Vercel Python Runtime entrypoint.
Uses Starlette Mount to strip /api prefix before routing to FastAPI.
"""
import sys
from pathlib import Path

# Ensure backend/ is in Python path so that "from app.xyz import ..."
# works inside backend/app/*.py modules
_project_root = Path(__file__).resolve().parent.parent
_backend_dir = _project_root / "backend"
if str(_backend_dir) not in sys.path:
    sys.path.insert(0, str(_backend_dir))

from starlette.applications import Starlette
from starlette.routing import Mount

from app.main import create_app

backend_app = create_app()
app = Starlette(routes=[Mount("/api", app=backend_app)])
