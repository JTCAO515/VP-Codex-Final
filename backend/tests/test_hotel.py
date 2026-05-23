from fastapi.testclient import TestClient

from app.main import create_app


def test_hotel_search_and_booking_flow():
    with TestClient(create_app()) as client:
        import os
        import uuid
        os.environ["AUTH_TEST_BYPASS"] = "1"
        headers = {"Authorization": "Bearer test:u1"}
        trip_id = f"t_hotel_{uuid.uuid4()}"
        # create a trip via chat (login mode so we can book)
        client.post("/chat/messages", headers=headers, json={"trip_id": trip_id, "text": "3 days in Beijing"})

        offers = client.post(
            "/hotel/search",
            json={"city": "Beijing", "check_in": "2026-06-01", "check_out": "2026-06-04", "adults": 2},
        ).json()["offers"]
        assert len(offers) >= 1

        booking = client.post(
            "/hotel/bookings",
            headers=headers,
            json={"trip_id": trip_id, "offer_id": offers[0]["offer_id"], "guest_info": {"name": "Alice"}},
        ).json()["booking"]
        assert booking["status"] in ["confirmed", "failed"]

        got = client.get(f"/hotel/bookings/{booking['id']}", headers=headers).json()["booking"]
        assert got["id"] == booking["id"]

        cancelled = client.post(f"/hotel/bookings/{booking['id']}:cancel", headers=headers).json()["booking"]
        assert cancelled["status"] == "cancelled"
