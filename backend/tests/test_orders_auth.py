import os

from fastapi.testclient import TestClient

from app.main import create_app


def _auth(user_id: str) -> dict:
    os.environ["AUTH_TEST_BYPASS"] = "1"
    return {"Authorization": f"Bearer test:{user_id}"}


def test_rfp_create_requires_login():
    with TestClient(create_app()) as c:
        r = c.post("/rfps", json={"trip_id": "t1", "service_types": ["guide"], "requirements": {"city": "beijing"}})
        assert r.status_code == 401


def test_hotel_booking_requires_login():
    with TestClient(create_app()) as c:
        r = c.post("/hotel/bookings", json={"trip_id": "t1", "offer_id": "offer_1", "guest_info": {}})
        assert r.status_code == 401


def test_orders_isolated_by_user_via_trip():
    """
    user A 创建 trip + booking；user B 不应读取 booking，也不应读取 trip detail。
    """
    with TestClient(create_app()) as c:
        import uuid
        trip_id = f"t_iso_{uuid.uuid4()}"
        # user A creates trip by chatting
        hA = _auth("userA")
        c.post("/chat/messages", headers=hA, json={"trip_id": trip_id, "text": "5 days in Beijing"})

        # booking under trip
        b = c.post(
            "/hotel/bookings",
            headers=hA,
            json={"trip_id": trip_id, "offer_id": "offer_1", "guest_info": {"name": "A"}},
        ).json()["booking"]

        # user B cannot read booking
        hB = _auth("userB")
        r = c.get(f"/hotel/bookings/{b['id']}", headers=hB)
        assert r.status_code in (401, 404)

        # user B cannot read trip
        r2 = c.get(f"/trips/{trip_id}", headers=hB)
        assert r2.status_code in (401, 404)
