from fastapi.testclient import TestClient

from app.main import create_app


def test_kb_search():
    with TestClient(create_app()) as client:
        r = client.get("/kb/search", params={"q": "宫保"})
        assert r.status_code == 200
        items = r.json()["items"]
        assert len(items) >= 1
