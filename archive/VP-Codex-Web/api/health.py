import json
import os
import time
import urllib.error
import urllib.request


_CACHE = {"checked_at": 0, "payload": None}
_TTL_SECONDS = 60


def deepseek_health():
    api_key = os.environ.get("DEEPSEEK_API_KEY")
    model = os.environ.get("DEEPSEEK_MODEL", "deepseek-v4-flash")
    if not api_key:
        return {
            "provider": "deepseek",
            "status": "unconfigured",
            "model": model,
            "message": "DEEPSEEK_API_KEY is not configured.",
        }

    now = time.time()
    if _CACHE["payload"] and now - _CACHE["checked_at"] < _TTL_SECONDS:
        return _CACHE["payload"]

    payload = {
        "provider": "deepseek",
        "status": "configured",
        "model": model,
        "message": "DeepSeek key is configured; live check not completed.",
    }
    try:
        request = urllib.request.Request(
            "https://api.deepseek.com/models",
            headers={"Authorization": f"Bearer {api_key}"},
            method="GET",
        )
        with urllib.request.urlopen(request, timeout=4) as response:
            data = json.loads(response.read().decode("utf-8"))
            models = {item.get("id") for item in data.get("data", [])}
            payload = {
                "provider": "deepseek",
                "status": "available" if model in models else "configured",
                "model": model,
                "message": "DeepSeek API is reachable." if model in models else "DeepSeek is reachable, but the configured model was not listed.",
            }
    except urllib.error.HTTPError as exc:
        payload = {
            "provider": "deepseek",
            "status": "error",
            "model": model,
            "message": f"DeepSeek returned HTTP {exc.code}.",
        }
    except Exception:
        payload = {
            "provider": "deepseek",
            "status": "error",
            "model": model,
            "message": "DeepSeek health check could not complete.",
        }

    _CACHE["checked_at"] = now
    _CACHE["payload"] = payload
    return payload


def payload():
    return {
        "ok": True,
        "service": "VisePanda",
        "version": "6.2.1",
        "llm": deepseek_health(),
    }
