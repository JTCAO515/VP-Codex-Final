"""v2.6: Payment system tests."""
from fastapi.testclient import TestClient

from app.main import create_app


def _auth(uid: str) -> dict:
    import os
    os.environ["AUTH_TEST_BYPASS"] = "1"
    return {"Authorization": f"Bearer test:{uid}"}


def test_payment_create_requires_login():
    with TestClient(create_app()) as c:
        r = c.post("/payments", json={"entity_type": "hotel_booking", "entity_id": "x", "amount": 10000, "currency": "CNY"})
        assert r.status_code == 401


def test_payment_create_and_pay_flow():
    """Full payment lifecycle: create → pay → verify status."""
    with TestClient(create_app()) as c:
        import uuid
        trip_id = f"t_pay_{uuid.uuid4()}"
        h = _auth("u_pay")

        # Create trip
        c.post("/chat/messages", headers=h, json={"trip_id": trip_id, "text": "3 days in Beijing"})

        # Book hotel
        offers = c.post("/hotel/search", json={"city": "Beijing", "check_in": "2026-06-01", "check_out": "2026-06-04", "adults": 2}).json()["offers"]
        booking = c.post("/hotel/bookings", headers=h, json={"trip_id": trip_id, "offer_id": offers[0]["offer_id"], "guest_info": {"name": "Alice"}}).json()["booking"]

        # Create payment
        r = c.post("/payments", headers=h, json={
            "entity_type": "hotel_booking",
            "entity_id": booking["id"],
            "amount": offers[0]["total_price"] * 100,  # in cents
            "currency": "CNY",
        })
        assert r.status_code == 200
        payment = r.json()["payment"]
        assert payment["status"] == "pending_payment"
        assert payment["amount"] == offers[0]["total_price"] * 100

        # Pay
        r2 = c.post(f"/payments/{payment['id']}:pay", headers=h)
        assert r2.status_code == 200
        paid = r2.json()["payment"]
        assert paid["status"] == "paid"
        assert paid["paid_at"] is not None

        # Get status
        r3 = c.get(f"/payments/{payment['id']}", headers=h)
        assert r3.json()["payment"]["status"] == "paid"


def test_payment_refund_flow():
    """Pay then refund."""
    with TestClient(create_app()) as c:
        import uuid
        trip_id = f"t_ref_{uuid.uuid4()}"
        h = _auth("u_ref")

        c.post("/chat/messages", headers=h, json={"trip_id": trip_id, "text": "2 days in Shanghai"})
        offers = c.post("/hotel/search", json={"city": "Shanghai", "check_in": "2026-07-01", "check_out": "2026-07-03", "adults": 1}).json()["offers"]
        booking = c.post("/hotel/bookings", headers=h, json={"trip_id": trip_id, "offer_id": offers[0]["offer_id"], "guest_info": {"name": "Bob"}}).json()["booking"]

        p = c.post("/payments", headers=h, json={"entity_type": "hotel_booking", "entity_id": booking["id"], "amount": 50000, "currency": "CNY"}).json()["payment"]
        c.post(f"/payments/{p['id']}:pay", headers=h)

        # Refund
        r = c.post(f"/payments/{p['id']}:refund", headers=h)
        assert r.status_code == 200
        assert r.json()["payment"]["status"] == "refunded"


def test_payment_isolated_by_user():
    """User A's payment should not be accessible by user B."""
    with TestClient(create_app()) as c:
        import uuid
        trip_id = f"t_piso_{uuid.uuid4()}"
        hA = _auth("u_piso_A")
        hB = _auth("u_piso_B")

        c.post("/chat/messages", headers=hA, json={"trip_id": trip_id, "text": "1 day in Guangzhou"})
        offers = c.post("/hotel/search", json={"city": "Guangzhou", "check_in": "2026-08-01", "check_out": "2026-08-02", "adults": 1}).json()["offers"]
        booking = c.post("/hotel/bookings", headers=hA, json={"trip_id": trip_id, "offer_id": offers[0]["offer_id"], "guest_info": {}}).json()["booking"]
        p = c.post("/payments", headers=hA, json={"entity_type": "hotel_booking", "entity_id": booking["id"], "amount": 10000, "currency": "CNY"}).json()["payment"]

        # User B tries to access
        r = c.get(f"/payments/{p['id']}", headers=hB)
        assert r.status_code == 404
