from fastapi.testclient import TestClient

from app.main import create_app


def test_rfp_quote_and_service_order_flow():
    with TestClient(create_app()) as client:
        # supplier onboarding
        s = client.post(
            "/suppliers", json={"name": "Best Guide", "profile": {"cities": ["beijing"], "languages": ["en"]}}
        ).json()["supplier"]
        client.post(f"/suppliers/{s['id']}:enable")

        # create trip
        client.post("/chat/messages", json={"user_id": "u2", "trip_id": "t_rfp", "text": "5 days in Beijing"})

        # create rfp
        rfp = client.post(
            "/rfps",
            json={
                "user_id": "u2",
                "trip_id": "t_rfp",
                "service_types": ["guide", "tickets"],
                "requirements": {"city": "beijing", "language": "en", "budget_range": "mid"},
            },
        ).json()["rfp"]
        assert s["id"] in rfp["supplier_targets"]

        # supplier sees rfp
        headers = {"X-API-Key": s["api_key"]}
        rfps = client.get("/supplier/rfps", headers=headers).json()["rfps"]
        assert any(x["id"] == rfp["id"] for x in rfps)

        # supplier submits quote
        q = client.post(
            f"/supplier/rfps/{rfp['id']}/quotes",
            headers=headers,
            json={"price": {"amount": 2000, "currency": "CNY", "unit": "day"}, "proposal": {"summary": "Custom plan"}},
        ).json()["quote"]

        # user checks rfp with quotes
        rfp_full = client.get(f"/rfps/{rfp['id']}").json()
        assert any(x["id"] == q["id"] for x in rfp_full["quotes"])

        # create service order
        order = client.post("/service-orders", json={"rfp_id": rfp["id"], "chosen_quote_id": q["id"]}).json()["order"]
        assert order["status"] == "created"

        # supplier updates status and fulfillment
        client.post(f"/supplier/orders/{order['id']}/status", headers=headers, json={"status": "accepted_by_supplier"})
        res = client.post(
            f"/supplier/orders/{order['id']}/fulfillment",
            headers=headers,
            json={"fulfillment_info": {"meeting_point": "Hotel lobby", "time": "09:00"}},
        ).json()
        assert res["order"]["fulfillment_info"]["meeting_point"] == "Hotel lobby"

        # supplier orders list
        orders = client.get("/supplier/orders", headers=headers).json()["orders"]
        assert any(x["id"] == order["id"] for x in orders)
