from __future__ import annotations

import os

from fastapi import APIRouter

router = APIRouter()


@router.get("/public-config")
def public_config():
    """
    前端静态站点无法直接读取 Vercel 环境变量，因此通过后端提供一个“可公开”的配置端点。
    注意：只返回 anon key，不返回 service role key。
    """
    return {
        "supabase_url": os.getenv("SUPABASE_URL", ""),
        "supabase_anon_key": os.getenv("SUPABASE_ANON_KEY", ""),
    }

