from __future__ import annotations

import datetime as dt

from fastapi import APIRouter, HTTPException, Request
from pydantic import BaseModel
from sqlalchemy.orm import Session

from app.db import session_scope
from app.auth import get_principal
from app.models import ChatMessage, EventLog, Trip, User
from app.orchestrator import (
    decide_next_action,
    detect_intent,
    extract_slots,
    generate_itinerary_v1,
    merge_slot_state,
    orchestrate,
)

router = APIRouter()


class ChatIn(BaseModel):
    trip_id: str
    text: str
    guest_id: str | None = None
    user_id: str | None = None  # 兼容：仅无 token 且无 guest_id 时使用（不推荐）


@router.post("/chat/messages")
def chat_messages(payload: ChatIn, request: Request):
    with session_scope() as db:
        # identity priority:
        # 1) Bearer token -> logged-in user (supabase sub)
        # 2) guest_id -> guest user (guest:<guest_id>)
        # 3) legacy user_id -> compatibility (discouraged)
        try:
            principal = get_principal(request, guest_id=payload.guest_id)
        except HTTPException:
            if payload.user_id:
                principal = type("P", (), {"mode": "legacy", "user_id": payload.user_id, "guest_id": None})()
            else:
                return {
                    "reply": "Missing identity (login or guest_id).",
                    "actions": [
                        {
                            "type": "ask",
                            "question": "Sign in or continue as guest?",
                            "options": ["Continue as guest", "Sign in with Google"],
                        }
                    ],
                    "slots": {},
                    "intent": "plan",
                    "trip": {"id": payload.trip_id},
                }

        user = _get_or_create_user(db, principal.user_id)
        trip = _get_or_create_trip(db, payload.trip_id, user.id)

        intent = detect_intent(payload.text)
        # prior slot_state (for LLM incremental extraction)
        trip.constraints = dict(trip.constraints or {})
        prior = trip.constraints.get("slot_state", {})

        # v2.4: Use unified orchestrate() — single LLM call or rule-based fallback
        result = orchestrate(payload.text, prior)

        # Trip.constraints["slot_state"] 作为"多轮补齐"的记忆
        slot_state = merge_slot_state(prior, result.slots)
        # Handle clear/remove from LLM result
        if result.clear:
            slot_state["_clear"] = list(set(slot_state.get("_clear", []) + result.clear))
        if result.remove:
            slot_state["_remove"] = dict(slot_state.get("_remove") or {})
            for k, v in result.remove.items():
                slot_state["_remove"][k] = v
        slot_state = merge_slot_state(slot_state, {})  # apply clear/remove
        slot_state["_confidence"] = result.confidence
        trip.constraints["slot_state"] = slot_state

        # Build actions list
        actions: list[dict] = []
        if result.ask:
            actions.append(dict(result.ask, type="ask"))
        # Add actions from result that are NOT already represented by ask
        if result.actions:
            for a in result.actions:
                if a.get("type") != "ask":
                    actions.append(a)

        reply = result.reply

        # Persist chat history (only for real users in DB mode; guest can still be stored if DB supports it)
        db.add(ChatMessage(user_id=user.id, trip_id=trip.id, role="user", content=payload.text))

        # 若生成了行程，写入 trip
        if result.itinerary:
            it = result.itinerary
            it["version"] = it.get("version", "v1")
            trip.current_itinerary = it
            trip.itinerary_versions = (trip.itinerary_versions or []) + [it]
            actions.append({"type": "itinerary_updated", "itinerary": it})
            _event(db, "Trip", trip.id, "itinerary_v1_generated", {"slots": slot_state})

        # Trip title & updated_at
        trip.updated_at = dt.datetime.now(dt.timezone.utc)
        if not trip.title:
            # simple title heuristic
            city = slot_state.get("cities", [None])[0]
            days = slot_state.get("days")
            if city and days:
                trip.title = f"{city} · {days} days"
            elif city:
                trip.title = f"{city} trip"
            else:
                trip.title = (payload.text[:24] + "…") if len(payload.text) > 24 else payload.text

        db.add(ChatMessage(user_id=user.id, trip_id=trip.id, role="assistant", content=reply))

        _event(
            db,
            "Trip",
            trip.id,
            "chat_message",
            {"text": payload.text, "intent": result.intent, "slot_state": slot_state},
        )

        return {
            "reply": reply,
            "actions": actions,
            "slots": slot_state,
            "intent": result.intent,
            "trip": {
                "id": trip.id,
                "title": trip.title,
                "updated_at": trip.updated_at.isoformat() if trip.updated_at else None,
            },
        }


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
        return f"OK，我们可以开始找酒店了。先确认一下日期与人数，然后我给你推荐 {city} 的可订房型。"
    if action["type"] == "ready_for_rfp":
        city = slots["cities"][0]
        return f"OK，我可以为 {city} 发起定制服务询价（RFP）。你更需要：导游 / 包车 / 门票代订 / 混合？"
    return f"ACK: {text}"
