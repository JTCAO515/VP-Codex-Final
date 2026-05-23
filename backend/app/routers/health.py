from fastapi import APIRouter

router = APIRouter()


@router.get("/health")
def health():
    return {
        "ok": True,
        "version": "3.0.0",
        "timestamp": __import__("time").time(),
    }

