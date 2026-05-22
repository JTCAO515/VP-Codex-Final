from __future__ import annotations

import json
from pathlib import Path

from fastapi import APIRouter, Query

router = APIRouter()

SEED_PATH = Path(__file__).resolve().parents[2] / "seed_kb.json"


def _load_seed() -> list[dict]:
    if not SEED_PATH.exists():
        return []
    return json.loads(SEED_PATH.read_text(encoding="utf-8"))


@router.get("/kb/search")
def kb_search(q: str = Query(min_length=1), limit: int = 5):
    items = _load_seed()
    ql = q.lower()
    hits: list[dict] = []
    for it in items:
        hay = " ".join(
            [
                it.get("title_zh", ""),
                it.get("title_en", ""),
                it.get("pinyin", ""),
                " ".join(it.get("keywords", [])),
                it.get("brief", ""),
            ]
        ).lower()
        if ql in hay:
            hits.append(it)
        if len(hits) >= limit:
            break
    return {"query": q, "items": hits}

