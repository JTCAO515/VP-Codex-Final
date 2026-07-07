"""Storage abstraction.

Two modes, chosen automatically:

* **Supabase mode** when SUPABASE_URL + SUPABASE_SERVICE_KEY are set — all
  reads/writes go via the PostgREST API.
* **Local JSON mode** otherwise — single file at data/auth.db.json.
  Atomic writes via tempfile+rename. Allowed only when APP_ENV != 'production'
  (production raises a 503-style RuntimeError).

The public surface is plain functions grouped under module-level namespaces:
storage.users, storage.itineraries, storage.favorites, storage.chat_sessions,
storage.chat_messages — each exposes the same shape across both modes so
callers don't branch.
"""
from __future__ import annotations

import json
import threading
import time
import urllib.parse
from datetime import datetime, timezone
from pathlib import Path

from . import config
from .common import http_request, random_id, write_json_atomic

_LOCK = threading.RLock()
_LOCAL_CACHE: dict | None = None
_LOCAL_PATH = config.LOCAL_DB_PATH


# ============================================================
# CREATE TABLE statements — put in README, run in Supabase SQL editor.
# ============================================================
SCHEMA_SQL = """
-- VisePanda v7.0 schema
create extension if not exists "uuid-ossp";
create extension if not exists "citext";

create table if not exists users (
  id uuid primary key default uuid_generate_v4(),
  email citext unique not null,
  password_hash text,
  google_id text unique,
  name text,
  avatar_url text,
  email_verified boolean not null default false,
  verify_code_hash text,
  verify_expires timestamptz,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists itineraries (
  user_id uuid primary key references users(id) on delete cascade,
  days jsonb not null default '[]'::jsonb,
  updated_at timestamptz not null default now()
);

create table if not exists favorites (
  id uuid primary key default uuid_generate_v4(),
  user_id uuid not null references users(id) on delete cascade,
  kind text not null,
  ref_id text not null,
  payload jsonb,
  created_at timestamptz not null default now(),
  unique(user_id, kind, ref_id)
);
create index if not exists favorites_user_idx on favorites(user_id);

create table if not exists chat_sessions (
  id uuid primary key default uuid_generate_v4(),
  user_id uuid not null references users(id) on delete cascade,
  title text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists chat_messages (
  id uuid primary key default uuid_generate_v4(),
  session_id uuid not null references chat_sessions(id) on delete cascade,
  role text not null check (role in ('user','assistant','system')),
  content text not null,
  created_at timestamptz not null default now()
);
create index if not exists chat_messages_session_idx on chat_messages(session_id, created_at);
"""


# ============================================================
# Local JSON helpers
# ============================================================

def _now() -> str:
    return datetime.now(timezone.utc).isoformat()


def _load_local() -> dict:
    global _LOCAL_CACHE
    if _LOCAL_CACHE is not None:
        return _LOCAL_CACHE
    if _LOCAL_PATH.exists():
        try:
            _LOCAL_CACHE = json.loads(_LOCAL_PATH.read_text(encoding="utf-8"))
            return _LOCAL_CACHE
        except (ValueError, OSError):
            pass
    _LOCAL_CACHE = {
        "users": [],
        "itineraries": {},      # user_id -> {days, updated_at}
        "favorites": [],
        "chat_sessions": [],
        "chat_messages": [],    # flat list with session_id
    }
    return _LOCAL_CACHE


def _save_local() -> None:
    if _LOCAL_CACHE is None:
        return
    if config.is_production():
        raise RuntimeError(
            "Local JSON storage is disabled in production; "
            "set SUPABASE_URL + SUPABASE_SERVICE_KEY."
        )
    write_json_atomic(_LOCAL_PATH, _LOCAL_CACHE)


def _local_writable() -> bool:
    if config.is_production() and not config.has_supabase():
        return False
    return True


# ============================================================
# Supabase REST helpers
# ============================================================

def _supa_headers(extra: dict | None = None) -> dict:
    h = {
        "apikey": config.SUPABASE_SERVICE_KEY,
        "Authorization": f"Bearer {config.SUPABASE_SERVICE_KEY}",
        "Content-Type": "application/json",
        "Accept": "application/json",
    }
    if extra:
        h.update(extra)
    return h


def _supa_url(table: str, query: str = "") -> str:
    base = f"{config.SUPABASE_URL}/rest/v1/{table}"
    return f"{base}?{query}" if query else base


def _supa_select(table: str, query: str) -> list[dict]:
    code, body, _ = http_request(_supa_url(table, query), headers=_supa_headers())
    if code != 200:
        return []
    try:
        return json.loads(body.decode("utf-8") or "[]")
    except ValueError:
        return []


def _supa_insert(table: str, record: dict) -> dict | None:
    code, body, _ = http_request(
        _supa_url(table),
        method="POST",
        headers=_supa_headers({"Prefer": "return=representation"}),
        data=record,
    )
    if code not in (200, 201):
        return None
    try:
        rows = json.loads(body.decode("utf-8") or "[]")
        return rows[0] if rows else None
    except ValueError:
        return None


def _supa_update(table: str, query: str, patch: dict) -> dict | None:
    code, body, _ = http_request(
        _supa_url(table, query),
        method="PATCH",
        headers=_supa_headers({"Prefer": "return=representation"}),
        data=patch,
    )
    if code not in (200, 204):
        return None
    try:
        rows = json.loads(body.decode("utf-8") or "[]")
        return rows[0] if rows else None
    except ValueError:
        return None


def _supa_upsert(table: str, record: dict, on_conflict: str) -> dict | None:
    qs = urllib.parse.urlencode({"on_conflict": on_conflict})
    code, body, _ = http_request(
        _supa_url(table, qs),
        method="POST",
        headers=_supa_headers({
            "Prefer": "return=representation,resolution=merge-duplicates",
        }),
        data=record,
    )
    if code not in (200, 201):
        return None
    try:
        rows = json.loads(body.decode("utf-8") or "[]")
        return rows[0] if rows else None
    except ValueError:
        return None


def _supa_delete(table: str, query: str) -> bool:
    code, _body, _ = http_request(
        _supa_url(table, query),
        method="DELETE",
        headers=_supa_headers(),
    )
    return code in (200, 204)


# ============================================================
# Users
# ============================================================

class _Users:
    @staticmethod
    def find_by_email(email: str) -> dict | None:
        email = (email or "").strip().lower()
        if not email:
            return None
        with _LOCK:
            if config.has_supabase():
                rows = _supa_select("users", f"email=eq.{urllib.parse.quote(email)}&limit=1")
                return rows[0] if rows else None
            for u in _load_local()["users"]:
                if (u.get("email") or "").lower() == email:
                    return dict(u)
        return None

    @staticmethod
    def find_by_id(uid: str) -> dict | None:
        if not uid:
            return None
        with _LOCK:
            if config.has_supabase():
                rows = _supa_select("users", f"id=eq.{urllib.parse.quote(uid)}&limit=1")
                return rows[0] if rows else None
            for u in _load_local()["users"]:
                if u.get("id") == uid:
                    return dict(u)
        return None

    @staticmethod
    def find_by_google_id(gid: str) -> dict | None:
        if not gid:
            return None
        with _LOCK:
            if config.has_supabase():
                rows = _supa_select("users", f"google_id=eq.{urllib.parse.quote(gid)}&limit=1")
                return rows[0] if rows else None
            for u in _load_local()["users"]:
                if u.get("google_id") == gid:
                    return dict(u)
        return None

    @staticmethod
    def create(record: dict) -> dict | None:
        record = {k: v for k, v in record.items() if v is not None}
        record.setdefault("email_verified", False)
        record.setdefault("created_at", _now())
        record.setdefault("updated_at", _now())
        with _LOCK:
            if config.has_supabase():
                return _supa_insert("users", record)
            if not _local_writable():
                return None
            data = _load_local()
            record.setdefault("id", random_id())
            data["users"].append(record)
            _save_local()
            return dict(record)

    @staticmethod
    def update(uid: str, patch: dict) -> dict | None:
        patch = {k: v for k, v in patch.items() if v is not None}
        patch["updated_at"] = _now()
        with _LOCK:
            if config.has_supabase():
                return _supa_update("users",
                                    f"id=eq.{urllib.parse.quote(uid)}", patch)
            if not _local_writable():
                return None
            data = _load_local()
            for i, u in enumerate(data["users"]):
                if u.get("id") == uid:
                    u.update(patch)
                    data["users"][i] = u
                    _save_local()
                    return dict(u)
        return None

    @staticmethod
    def delete(uid: str) -> bool:
        with _LOCK:
            if config.has_supabase():
                return _supa_delete("users", f"id=eq.{urllib.parse.quote(uid)}")
            if not _local_writable():
                return False
            data = _load_local()
            before = len(data["users"])
            data["users"] = [u for u in data["users"] if u.get("id") != uid]
            data["itineraries"].pop(uid, None)
            data["favorites"] = [f for f in data["favorites"] if f.get("user_id") != uid]
            sess_ids = {s["id"] for s in data["chat_sessions"] if s.get("user_id") == uid}
            data["chat_sessions"] = [s for s in data["chat_sessions"] if s.get("user_id") != uid]
            data["chat_messages"] = [m for m in data["chat_messages"] if m.get("session_id") not in sess_ids]
            _save_local()
            return len(data["users"]) != before


users = _Users()


# ============================================================
# Itineraries
# ============================================================

class _Itineraries:
    @staticmethod
    def get(user_id: str) -> list:
        with _LOCK:
            if config.has_supabase():
                rows = _supa_select("itineraries",
                                    f"user_id=eq.{urllib.parse.quote(user_id)}&limit=1")
                return rows[0]["days"] if rows else []
            entry = _load_local()["itineraries"].get(user_id)
            return list(entry["days"]) if entry else []

    @staticmethod
    def upsert(user_id: str, days: list) -> bool:
        record = {"user_id": user_id, "days": days, "updated_at": _now()}
        with _LOCK:
            if config.has_supabase():
                return _supa_upsert("itineraries", record, on_conflict="user_id") is not None
            if not _local_writable():
                return False
            _load_local()["itineraries"][user_id] = {"days": list(days), "updated_at": _now()}
            _save_local()
            return True


itineraries = _Itineraries()


# ============================================================
# Favorites
# ============================================================

class _Favorites:
    @staticmethod
    def list(user_id: str, kind: str | None = None) -> list:
        with _LOCK:
            if config.has_supabase():
                q = f"user_id=eq.{urllib.parse.quote(user_id)}"
                if kind:
                    q += f"&kind=eq.{urllib.parse.quote(kind)}"
                q += "&order=created_at.desc"
                return _supa_select("favorites", q)
            items = [f for f in _load_local()["favorites"] if f.get("user_id") == user_id]
            if kind:
                items = [f for f in items if f.get("kind") == kind]
            return sorted(items, key=lambda x: x.get("created_at", ""), reverse=True)

    @staticmethod
    def add(user_id: str, kind: str, ref_id: str, payload: dict | None = None) -> dict | None:
        record = {
            "user_id": user_id,
            "kind": kind,
            "ref_id": ref_id,
            "payload": payload or {},
            "created_at": _now(),
        }
        with _LOCK:
            if config.has_supabase():
                return _supa_upsert("favorites", record, on_conflict="user_id,kind,ref_id")
            if not _local_writable():
                return None
            data = _load_local()
            for f in data["favorites"]:
                if (f.get("user_id") == user_id and f.get("kind") == kind
                        and f.get("ref_id") == ref_id):
                    f["payload"] = payload or {}
                    _save_local()
                    return dict(f)
            record["id"] = random_id()
            data["favorites"].append(record)
            _save_local()
            return dict(record)

    @staticmethod
    def remove(fav_id: str, user_id: str) -> bool:
        with _LOCK:
            if config.has_supabase():
                return _supa_delete(
                    "favorites",
                    f"id=eq.{urllib.parse.quote(fav_id)}"
                    f"&user_id=eq.{urllib.parse.quote(user_id)}",
                )
            if not _local_writable():
                return False
            data = _load_local()
            before = len(data["favorites"])
            data["favorites"] = [
                f for f in data["favorites"]
                if not (f.get("id") == fav_id and f.get("user_id") == user_id)
            ]
            _save_local()
            return len(data["favorites"]) != before


favorites = _Favorites()


# ============================================================
# Chat sessions + messages
# ============================================================

class _ChatSessions:
    @staticmethod
    def list(user_id: str, limit: int = 20) -> list:
        with _LOCK:
            if config.has_supabase():
                q = (f"user_id=eq.{urllib.parse.quote(user_id)}"
                     f"&order=updated_at.desc&limit={int(limit)}")
                return _supa_select("chat_sessions", q)
            items = [s for s in _load_local()["chat_sessions"] if s.get("user_id") == user_id]
            return sorted(items, key=lambda x: x.get("updated_at", ""), reverse=True)[:limit]

    @staticmethod
    def create(user_id: str, title: str | None = None) -> dict | None:
        record = {
            "user_id": user_id,
            "title": (title or "")[:120] or None,
            "created_at": _now(),
            "updated_at": _now(),
        }
        with _LOCK:
            if config.has_supabase():
                return _supa_insert("chat_sessions", record)
            if not _local_writable():
                return None
            record["id"] = random_id()
            _load_local()["chat_sessions"].append(record)
            _save_local()
            return dict(record)

    @staticmethod
    def touch(session_id: str, title: str | None = None) -> None:
        patch = {"updated_at": _now()}
        if title:
            patch["title"] = title[:120]
        with _LOCK:
            if config.has_supabase():
                _supa_update("chat_sessions",
                             f"id=eq.{urllib.parse.quote(session_id)}", patch)
                return
            if not _local_writable():
                return
            for s in _load_local()["chat_sessions"]:
                if s.get("id") == session_id:
                    s.update(patch)
                    _save_local()
                    return


chat_sessions = _ChatSessions()


class _ChatMessages:
    @staticmethod
    def append(session_id: str, role: str, content: str) -> dict | None:
        record = {
            "session_id": session_id,
            "role": role,
            "content": content,
            "created_at": _now(),
        }
        with _LOCK:
            if config.has_supabase():
                return _supa_insert("chat_messages", record)
            if not _local_writable():
                return None
            record["id"] = random_id()
            _load_local()["chat_messages"].append(record)
            _save_local()
            return dict(record)

    @staticmethod
    def list(session_id: str) -> list:
        with _LOCK:
            if config.has_supabase():
                q = (f"session_id=eq.{urllib.parse.quote(session_id)}"
                     f"&order=created_at.asc")
                return _supa_select("chat_messages", q)
            items = [m for m in _load_local()["chat_messages"]
                     if m.get("session_id") == session_id]
            return sorted(items, key=lambda x: x.get("created_at", ""))


chat_messages = _ChatMessages()
