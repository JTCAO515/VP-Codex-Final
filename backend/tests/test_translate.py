import base64

from fastapi.testclient import TestClient

from app.main import create_app


def test_translate_text_smoke():
    with TestClient(create_app()) as client:
        r = client.post("/translate/text", json={"source_lang": "en", "target_lang": "zh", "text": "Hello"})
        assert r.status_code == 200
        assert "translated_text" in r.json()


def test_translate_image_smoke():
    with TestClient(create_app()) as client:
        fake_img = base64.b64encode(b"fake").decode("ascii")
        r = client.post(
            "/translate/image",
            json={"source_lang": "en", "target_lang": "zh", "image_base64": fake_img},
        )
        assert r.status_code == 200
        body = r.json()
        assert "lines" in body
        assert len(body["lines"]) >= 1
