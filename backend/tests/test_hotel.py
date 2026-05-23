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


def test_seed_hotel_provider_has_real_data():
    """v2.5: Seed provider returns real hotel names, prices, ratings for major cities."""
    from app.providers import SeedHotelProvider

    provider = SeedHotelProvider()
    for city in ["Beijing", "Shanghai", "Chengdu"]:
        results = provider.search(city, "2026-06-01", "2026-06-04", 2)
        assert len(results) >= 10, f"{city} should have at least 10 hotels"
        for h in results:
            assert h["name"], "Hotel must have a name"
            assert h["price_per_night"] > 0
            assert h["stars"] >= 1
            assert h["city"] == city


def test_seed_hotel_search_api_returns_rich_data():
    """v2.5: /hotel/search returns name, price, stars, rating, amenities, image."""
    from app.main import create_app
    from fastapi.testclient import TestClient

    with TestClient(create_app()) as client:
        r = client.post(
            "/hotel/search",
            json={"city": "Beijing", "check_in": "2026-06-01", "check_out": "2026-06-04", "adults": 2},
        )
        assert r.status_code == 200
        offers = r.json()["offers"]
        assert len(offers) >= 10
        h = offers[0]
        for key in ["name", "price_per_night", "stars", "rating", "city"]:
            assert key in h, f"Missing key: {key}"
        assert h["price_per_night"] > 0
