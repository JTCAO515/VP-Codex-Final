import asyncio
import json
from typing import Optional

from fastapi import APIRouter, Depends, HTTPException, status
from pydantic import BaseModel
from sse_starlette.sse import EventSourceResponse
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.user import User
from app.core.deps import get_current_user

router = APIRouter(prefix="/api/chat", tags=["chat"])


class ChatRequest(BaseModel):
    message: str
    city_context: Optional[str] = None


@router.post("/message")
async def chat_message(req: ChatRequest):
    """
    Chat SSE 流式接口（当前为 mock 版本）。
    生产环境应接入 AI 模型（如 GPT/Claude）并流式返回。
    """
    city_info = f"关于 {req.city_context} " if req.city_context else ""

    async def event_generator():
        # 模拟流式输出
        tokens = [
            f"您好！{city_info}我来帮您规划旅行。",
            "根据您的要求，我建议的行程如下：",
        ]
        itinerary = {
            "days": [
                {
                    "day": 1,
                    "title": "抵达与探索",
                    "activities": [
                        "上午：抵达，入住酒店",
                        "下午：探索市区主要景点",
                        "晚上：品尝当地美食",
                    ]
                },
                {
                    "day": 2,
                    "title": "深度游览",
                    "activities": [
                        "上午：参观主要文化景点",
                        "下午：体验当地特色活动",
                        "晚上：夜市/文化表演",
                    ]
                },
                {
                    "day": 3,
                    "title": "自由活动与出发",
                    "activities": [
                        "上午：自由探索或购物",
                        "下午：准备出发",
                    ]
                }
            ]
        }

        for token in tokens:
            yield {"event": "message", "data": json.dumps({"type": "token", "content": token})}
            await asyncio.sleep(0.05)

        yield {"event": "message", "data": json.dumps({"type": "itinerary", "content": itinerary})}
        await asyncio.sleep(0.05)

        yield {"event": "message", "data": json.dumps({
            "type": "faq",
            "content": [
                {"q": "最佳旅行季节是什么时候？", "a": "春秋季（3-5月、9-11月）气候最宜人。"},
                {"q": "需要签证吗？", "a": "大部分国家需要提前申请中国签证，建议至少提前1个月。"},
            ]
        })}
        await asyncio.sleep(0.05)

        yield {"event": "message", "data": json.dumps({"type": "done"})}

    return EventSourceResponse(event_generator())
