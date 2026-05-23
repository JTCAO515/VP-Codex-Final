from fastapi.testclient import TestClient

from app.main import create_app


def test_chat_message_smoke():
    with TestClient(create_app()) as client:
        r = client.post("/chat/messages", json={"guest_id": "g1", "trip_id": "t1", "text": "I want 5 days in Beijing"})
        assert r.status_code == 200
        body = r.json()
        assert "reply" in body
        assert "actions" in body
        assert body["actions"][0]["type"] in ["ask", "generate_itinerary_v1"]


def test_chat_generates_itinerary_when_slots_present():
    with TestClient(create_app()) as client:
        r = client.post("/chat/messages", json={"guest_id": "g1", "trip_id": "t2", "text": "5 days in Beijing"})
        body = r.json()
        assert any(a["type"] == "itinerary_updated" for a in body["actions"])
        it = [a for a in body["actions"] if a["type"] == "itinerary_updated"][0]["itinerary"]
        assert it["version"] == "v1"
        assert len(it["days"]) == 5


def test_chat_asks_only_one_question_for_missing_key_slot():
    with TestClient(create_app()) as client:
        r = client.post("/chat/messages", json={"guest_id": "g3", "trip_id": "t3", "text": "I want to travel"})
        body = r.json()
        asks = [a for a in body["actions"] if a.get("type") == "ask"]
        assert len(asks) == 1
        assert 2 <= len(asks[0].get("options", [])) <= 4
        assert "slots" in body
        assert "_confidence" in body["slots"]


def test_rfp_intent_prioritizes_party_question():
    """
    当用户表达“定制/导游/门票”等RFP意图但未提供人数时，应优先追问 party（高影响槽位）。
    """
    with TestClient(create_app()) as client:
        import uuid
        trip_id = f"t4-{uuid.uuid4()}"
        guest_id = f"g4-{uuid.uuid4()}"
        r = client.post(
            "/chat/messages",
            json={"guest_id": guest_id, "trip_id": trip_id, "text": "I need a private guide and tickets in Beijing"},
        )
        body = r.json()
        # 可能先问 cities（若未抽取到），若抽取到 Beijing 则应问 party
        ask = [a for a in body["actions"] if a.get("type") == "ask"][0]
        assert ask.get("slot_key") in ["cities", "party"]


def test_negation_removes_previous_city():
    with TestClient(create_app()) as client:
        client.post("/chat/messages", json={"guest_id": "g5", "trip_id": "t5", "text": "5 days in Beijing"})
        r = client.post("/chat/messages", json={"guest_id": "g5", "trip_id": "t5", "text": "Actually not Beijing, Shanghai"})
        slots = r.json()["slots"]
        assert "Shanghai" in slots.get("cities", [])
        assert "Beijing" not in slots.get("cities", [])


def test_llm_config_nuwa_defaults(monkeypatch):
    """v2.4: NUWA defaults when LLM_ENABLED=1 but no custom URL."""
    import os
    from app.llm_provider import load_llm_config

    monkeypatch.setenv("LLM_ENABLED", "1")
    # Don't set LLM_BASE_URL — should default to nuwa
    monkeypatch.delenv("LLM_BASE_URL", raising=False)
    monkeypatch.setenv("LLM_API_KEY", "test_key")
    monkeypatch.setenv("LLM_MODEL", "")

    cfg = load_llm_config()
    assert cfg.enabled is True
    assert "nuwaflux.com" in cfg.base_url
    assert cfg.model != ""
    assert cfg.api_key == "test_key"


def test_llm_orchestrate_prompt_exists():
    """v2.4: orchestration system prompt is non-empty and contains key directives."""
    from app.prompts import chat_orchestrate_system_prompt

    prompt = chat_orchestrate_system_prompt()
    assert len(prompt) > 500
    assert "reply" in prompt
    assert "itinerary" in prompt
    assert "slots" in prompt
    assert "少问" in prompt or "ask" in prompt.lower()


def test_llm_orchestrate_fallback_to_rule():
    """v2.4: when LLM is disabled or fails, fall back to rule-based orchestration."""
    from unittest.mock import patch
    from app.orchestrator import orchestrate

    # LLM disabled should use rule path
    with patch("app.orchestrator.build_provider", return_value=None):
        result = orchestrate("I want 3 days in Shanghai", prior_slot_state=None, use_llm=False)
        assert result.reply is not None
        assert result.intent == "plan"
        # Should fall back to rule extraction
        assert "Shanghai" in str(result.slots.get("cities", []))


def test_negation_removes_must_see_poi():
    with TestClient(create_app()) as client:
        # add must_see
        client.post("/chat/messages", json={"guest_id": "g6", "trip_id": "t6", "text": "I must see the Forbidden City in Beijing"})
        # negation should remove it
        r = client.post("/chat/messages", json={"guest_id": "g6", "trip_id": "t6", "text": "Actually not Forbidden City"})
        slots = r.json()["slots"]
        ms = (slots.get("constraints") or {}).get("must_see", [])
        assert "Forbidden City" not in ms
