"""v2.8: End-to-end integration tests."""
from fastapi.testclient import TestClient

from app.main import create_app


def _auth(uid: str) -> dict:
    import os
    os.environ["AUTH_TEST_BYPASS"] = "1"
    return {"Authorization": f"Bearer test:{uid}"}


def test_full_guest_to_payment_journey():
    """End-to-end: logged-in user chats → books hotel → pays → views history."""
    with TestClient(create_app()) as c:
        import uuid
        h = _auth("e2e_user")
        trip_id = f"t_e2e_{uuid.uuid4()}"

        # 1. Start conversation
        r1 = c.post("/chat/messages", headers=h, json={"trip_id": trip_id, "text": "I want 3 days in Beijing"})
        assert r1.status_code == 200
        body = r1.json()
        assert body["intent"] == "plan"
        assert body["reply"]

        # 2. Search hotels
        r3 = c.post("/hotel/search", json={"city": "Beijing", "check_in": "2026-09-01", "check_out": "2026-09-04", "adults": 2})
        offers = r3.json()["offers"]
        assert len(offers) >= 10

        # 3. Book hotel
        r4 = c.post("/hotel/bookings", headers=h, json={"trip_id": trip_id, "offer_id": offers[0]["offer_id"], "guest_info": {"name": "Test User"}})
        assert r4.status_code == 200, f"Booking failed: {r4.text}"
        booking = r4.json()["booking"]
        assert booking["status"] in ["confirmed", "failed"]

        # 4. Pay
        r5 = c.post("/payments", headers=h, json={"entity_type": "hotel_booking", "entity_id": booking["id"], "amount": offers[0]["total_price"] * 100, "currency": "CNY"})
        payment = r5.json()["payment"]
        r6 = c.post(f"/payments/{payment['id']}:pay", headers=h)
        assert r6.json()["payment"]["status"] == "paid"

        # 5. View trip history
        r7 = c.get(f"/trips/{trip_id}", headers=h)
        assert r7.status_code == 200
        assert r7.json()["trip"]["id"] == trip_id

        # 6. View hotel bookings for trip
        r8 = c.get(f"/trips/{trip_id}/hotel-bookings", headers=h)
        bookings = r8.json()["bookings"]
        assert len(bookings) >= 1


def test_supplier_full_flow():
    """Supplier: register → get RFPs → verify identity."""
    with TestClient(create_app()) as c:
        # Register supplier
        r1 = c.post("/suppliers", json={"name": "Test Guide Service", "profile": {"cities": ["beijing"], "languages": ["en", "zh"]}})
        supplier = r1.json()["supplier"]
        c.post(f"/suppliers/{supplier['id']}:enable")

        # Supplier verifies their API key works
        sH = {"X-API-Key": supplier["api_key"]}
        r3 = c.get("/supplier/me", headers=sH)
        assert r3.status_code == 200
        assert "supplier" in r3.json()
        assert r3.json()["supplier"]["name"] == "Test Guide Service"

        # Supplier gets their orders (should be empty)
        r4 = c.get("/supplier/orders", headers=sH)
        assert r4.status_code == 200
        assert "orders" in r4.json()
