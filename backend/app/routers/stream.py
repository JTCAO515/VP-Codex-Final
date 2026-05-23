"""
v3.3 Streaming chat endpoint with SSE (Server-Sent Events).
"""
from __future__ import annotations

import json
import os
from typing import AsyncGenerator

import httpx
from fastapi import APIRouter, Request
from fastapi.responses import StreamingResponse
from pydantic import BaseModel

from app.llm_provider import load_llm_config

router = APIRouter()


class StreamChatIn(BaseModel):
    trip_id: str
    text: str
    guest_id: str | None = None
    history: list[dict] | None = None  # [{role, content}, ...]


@router.post("/chat/stream")
async def chat_stream(payload: StreamChatIn, request: Request):
    """SSE streaming chat. Returns tokens as they arrive."""
    cfg = load_llm_config()
    if not cfg.enabled or not cfg.api_key:
        return StreamingResponse(
            _error_stream("LLM not configured"),
            media_type="text/event-stream",
        )

    # Build messages from history
    messages = [{"role": "system", "content": _system_prompt()}]
    if payload.history:
        messages.extend(payload.history)
    messages.append({"role": "user", "content": payload.text})

    async def generate() -> AsyncGenerator[str, None]:
        url = f"{cfg.base_url}/chat/completions"
        headers = {"Authorization": f"Bearer {cfg.api_key}"}
        body = {
            "model": cfg.model,
            "messages": messages,
            "temperature": 0.7,
            "stream": True,
            "max_tokens": 2048,
        }

        try:
            async with httpx.AsyncClient(timeout=60.0) as client:
                async with client.stream("POST", url, headers=headers, json=body) as resp:
                    if resp.status_code != 200:
                        error_text = await resp.aread()
                        yield f"data: {json.dumps({'error': f'LLM error: {resp.status_code}'})}\n\n"
                        yield "data: [DONE]\n\n"
                        return

                    buffer = ""
                    async for chunk in resp.aiter_bytes():
                        buffer += chunk.decode("utf-8", errors="ignore")
                        while "\n" in buffer:
                            line, buffer = buffer.split("\n", 1)
                            line = line.strip()
                            if not line or not line.startswith("data: "):
                                continue
                            data_str = line[6:]
                            if data_str == "[DONE]":
                                yield "data: [DONE]\n\n"
                                return
                            try:
                                data = json.loads(data_str)
                                delta = data.get("choices", [{}])[0].get("delta", {})
                                content = delta.get("content", "")
                                if content:
                                    yield f"data: {json.dumps({'token': content})}\n\n"
                            except (json.JSONDecodeError, KeyError, IndexError):
                                continue
        except Exception as e:
            yield f"data: {json.dumps({'error': str(e)})}\n\n"
        yield "data: [DONE]\n\n"

    return StreamingResponse(generate(), media_type="text/event-stream")


async def _error_stream(msg: str):
    yield f"data: {json.dumps({'error': msg})}\n\n"
    yield "data: [DONE]\n\n"


def _system_prompt() -> str:
    return (
        "You are a helpful China travel assistant. Be concise and friendly. "
        "Suggest specific places, foods, and practical tips. "
        "When relevant, structure your response with quick follow-up suggestions "
        "separated by '---SUGGESTIONS---' on its own line, with each suggestion on a new line starting with '- '. "
        "Example:\n"
        "Here's my recommendation...\n"
        "---SUGGESTIONS---\n"
        "- Show me hotels near there\n"
        "- What's the best time to visit?\n"
        "- Any local food I should try?"
    )
