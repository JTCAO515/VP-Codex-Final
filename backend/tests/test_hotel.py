from fastapi.testclient import TestClient

from app.main import create_app


def test_hotel_search_and_booking_flow():
    with TestClient(create_app()) as client:
        # create a trip via chat
        client.post("/chat/messages", json={"user_id": "u1", "trip_id": "t_hotel", "text": "3 days in Beijing"})

        offers = client.post(
            "/hotel/search",
            json={"city": "Beijing", "check_in": "2026-06-01", "check_out": "2026-06-04", "adults": 2},
        ).json()["offers"]
        assert len(offers) >= 1

        booking = client.post(
            "/hotel/bookings",
            json={"trip_id": "t_hotel", "offer_id": offers[0]["offer_id"], "guest_info": {"name": "John"}},
        ).json()["booking"]
        assert booking["status"] in ["confirmed", "failed"]

        got = client.get(f"/hotel/bookings/{booking['id']}").json()["booking"]
        assert got["id"] == booking["id"]

        cancelled = client.post(f"/hotel/bookings/{booking['id']}:cancel").json()["booking"]
        assert cancelled["status"] == "cancelled"
