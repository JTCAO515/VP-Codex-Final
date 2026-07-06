from __future__ import annotations

import os

from fastapi import APIRouter

router = APIRouter()


@router.get("/public-config")
def public_config():
    """
    Frontend static sites can't read Vercel env vars directly.
    This endpoint exposes only public-safe values.
    Supports multiple env var naming conventions (Vite/Next/FastAPI).
    """
    import os
    supabase_url = (
        os.getenv("SUPABASE_URL")
        or os.getenv("NEXT_PUBLIC_SUPABASE_URL")
        or os.getenv("VITE_SUPABASE_URL")
        or ""
    )
    supabase_anon_key = (
        os.getenv("SUPABASE_ANON_KEY")
        or os.getenv("NEXT_PUBLIC_SUPABASE_ANON_KEY")
        or os.getenv("VITE_SUPABASE_ANON_KEY")
        or ""
    )
    return {
        "supabase_url": supabase_url,
        "supabase_anon_key": supabase_anon_key,
    }

