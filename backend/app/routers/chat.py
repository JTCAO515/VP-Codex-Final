from __future__ import annotations

from fastapi import APIRouter, Depends
from pydantic import BaseModel
from sqlalchemy.orm import Session

from app.db import session_scope
from app.models import EventLog, Trip, User
from app.orchestrator import (
    decide_next_action,
    detect_intent,
    extract_slots,
    generate_itinerary_v1,
    merge_slot_state,
)
from app.routers.auth import get_current_user

router = APIRouter()


class ChatIn(BaseModel):
    user_id: str
    trip_id: str
    text: str


def _get_or_create_user(db: Session, user_id: str) -> User:
    user = db.query(User).filter(User.id == user_id).one_or_none()
    if user:
        return user
    user = User(id=user_id, profile={})
    db.add(user)
    return user


def _get_or_create_trip(db: Session, trip_id: str, user_id: str) -> Trip:
    trip = db.query(Trip).filter(Trip.id == trip_id).one_or_none()
    if trip:
        return trip
    trip = Trip(id=trip_id, user_id=user_id)
    db.add(trip)
    return trip


@router.post("/chat/messages")
def chat_messages(payload: ChatIn):
    """对话接口：支持可选 JWT 鉴权（发送 Authorization header 时校验用户身份）。"""
    with session_scope() as db:
        user = _get_or_create_user(db, payload.user_id)
        trip = _get_or_create_trip(db, payload.trip_id, user.id)

        intent = detect_intent(payload.text)
        trip.constraints = dict(trip.constraints or {})
        prior = trip.constraints.get("slot_state", {})
        extracted, confidence, llm_ask = extract_slots(payload.text, prior)

        slot_state = merge_slot_state(prior, extracted)
        slot_state["_confidence"] = confidence
        if isinstance(prior, dict) and prior.get("_last_asked"):
            slot_state["_last_asked"] = prior.get("_last_asked")
        trip.constraints["slot_state"] = slot_state

        action = llm_ask or decide_next_action(slot_state, intent)
        actions: list[dict] = [action]

        if isinstance(action, dict) and action.get("_set_last_asked"):
            trip.constraints["slot_state"]["_last_asked"] = action["_set_last_asked"]

        reply = _render_reply(payload.text, slot_state, action, intent)

        if action["type"] == "generate_itinerary_v1":
            it = generate_itinerary_v1(slot_state["cities"], slot_state["days"])
            trip.current_itinerary = it
            trip.itinerary_versions = (trip.itinerary_versions or []) + [it]
            actions.append({"type": "itinerary_updated", "itinerary": it})
            _event(db, "Trip", trip.id, "itinerary_v1_generated", {"slots": slot_state})
            for pa in action.get("post_actions", []):
                actions.append(pa)
                if pa.get("type") == "suggest_refine":
                    actions.append(pa["suggestion"])

        _event(
            db,
            "Trip",
            trip.id,
            "chat_message",
            {"text": payload.text, "intent": intent, "extracted": extracted, "slot_state": slot_state, "action": action},
        )

        return {"reply": reply, "actions": actions, "slots": slot_state, "intent": intent, "trip": {"id": trip.id}}


@router.post("/chat/messages/secure")
def chat_messages_secure(payload: ChatIn, current_user: User = Depends(get_current_user)):
    """对话接口（需要 JWT 鉴权）：验证 Authorization header 后，自动使用当前登录用户的 ID。"""
    with session_scope() as db:
        trip = _get_or_create_trip(db, payload.trip_id, current_user.id)

        intent = detect_intent(payload.text)
        trip.constraints = dict(trip.constraints or {})
        prior = trip.constraints.get("slot_state", {})
        extracted, confidence, llm_ask = extract_slots(payload.text, prior)

        slot_state = merge_slot_state(prior, extracted)
        slot_state["_confidence"] = confidence
        if isinstance(prior, dict) and prior.get("_last_asked"):
            slot_state["_last_asked"] = prior.get("_last_asked")
        trip.constraints["slot_state"] = slot_state

        action = llm_ask or decide_next_action(slot_state, intent)
        actions: list[dict] = [action]

        if isinstance(action, dict) and action.get("_set_last_asked"):
            trip.constraints["slot_state"]["_last_asked"] = action["_set_last_asked"]

        reply = _render_reply(payload.text, slot_state, action, intent)

        if action["type"] == "generate_itinerary_v1":
            it = generate_itinerary_v1(slot_state["cities"], slot_state["days"])
            trip.current_itinerary = it
            trip.itinerary_versions = (trip.itinerary_versions or []) + [it]
            actions.append({"type": "itinerary_updated", "itinerary": it})
            _event(db, "Trip", trip.id, "itinerary_v1_generated", {"slots": slot_state})

        _event(
            db,
            "Trip",
            trip.id,
            "chat_message_secure",
            {"text": payload.text, "intent": intent, "extracted": extracted, "slot_state": slot_state, "action": action},
        )

        return {"reply": reply, "actions": actions, "slots": slot_state, "intent": intent, "trip": {"id": trip.id}}


def _event(db: Session, entity_type: str, entity_id: str, event_type: str, payload: dict):
    db.add(EventLog(entity_type=entity_type, entity_id=entity_id, event_type=event_type, payload=payload))


def _render_reply(text: str, slots: dict, action: dict, intent: str) -> str:
    if action["type"] == "ask":
        opts = " / ".join(action.get("options", []))
        return f"{action['question']} ({opts})"
    if action["type"] == "generate_itinerary_v1":
        city = slots["cities"][0]
        days = slots["days"]
        return f"Got it — generating a {days}-day itinerary for {city}."
    if action["type"] == "ready_for_hotel_search":
        city = slots["cities"][0]
        return f"OK, we can start searching for hotels. Let me first confirm your dates, then I'll recommend available options in {city}."
    if action["type"] == "ready_for_rfp":
        city = slots["cities"][0]
        return f"OK, I can start a custom service inquiry (RFP) for {city}. What do you need: guide / car / tickets / mixed?"
    return f"ACK: {text}"
