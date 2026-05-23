from __future__ import annotations

import json
import os
from dataclasses import dataclass
from typing import Any

import httpx


@dataclass
class LLMConfig:
    """
    OpenAI 兼容接口配置（也适用于大多数“OpenAI API兼容”的国产/私有模型网关）。

    运行时可通过环境变量配置：
    - LLM_BASE_URL: 例如 https://api.openai.com/v1 或你的网关地址
    - LLM_API_KEY: 令牌
    - LLM_MODEL: 例如 gpt-4.1-mini / deepseek-chat 等
    - LLM_ENABLED: "1" 启用；未启用则回退到规则抽取
    """

    enabled: bool
    base_url: str
    api_key: str
    model: str
    timeout_s: float = 20.0


def load_llm_config() -> LLMConfig:
    enabled = os.getenv("LLM_ENABLED", "").strip() == "1"

    # Default to DeepSeek API when enabled but no explicit base_url
    default_base = "https://api.deepseek.com"
    base_url = os.getenv("LLM_BASE_URL", "").strip()
    if not base_url:
        base_url = default_base

    api_key = os.getenv("LLM_API_KEY", "")

    default_model = "deepseek-v4-flash"
    model = os.getenv("LLM_MODEL", "").strip()
    if not model:
        model = default_model

    return LLMConfig(enabled=enabled, base_url=base_url, api_key=api_key, model=model)


class LLMProvider:
    async def chat_json(self, system: str, user: str) -> dict[str, Any]:
        raise NotImplementedError


class OpenAICompatibleProvider(LLMProvider):
    def __init__(self, cfg: LLMConfig):
        self.cfg = cfg

    async def chat_json(self, system: str, user: str) -> dict[str, Any]:
        if not self.cfg.api_key:
            raise RuntimeError("LLM_API_KEY is missing")

        url = f"{self.cfg.base_url}/chat/completions"
        headers = {"Authorization": f"Bearer {self.cfg.api_key}"}
        payload = {
            "model": self.cfg.model,
            "messages": [
                {"role": "system", "content": system},
                {"role": "user", "content": user},
            ],
            "temperature": 0,
            "response_format": {"type": "json_object"},
        }

        async with httpx.AsyncClient(timeout=self.cfg.timeout_s) as client:
            r = await client.post(url, headers=headers, json=payload)
            r.raise_for_status()
            data = r.json()
            content = data["choices"][0]["message"]["content"]
            return json.loads(content)

    def chat_json_sync(self, system: str, user: str) -> dict[str, Any]:
        """
        同步版本：便于在同步 FastAPI 路由中使用。
        """
        if not self.cfg.api_key:
            raise RuntimeError("LLM_API_KEY is missing")

        url = f"{self.cfg.base_url}/chat/completions"
        headers = {"Authorization": f"Bearer {self.cfg.api_key}"}
        payload = {
            "model": self.cfg.model,
            "messages": [
                {"role": "system", "content": system},
                {"role": "user", "content": user},
            ],
            "temperature": 0,
            "response_format": {"type": "json_object"},
        }

        with httpx.Client(timeout=self.cfg.timeout_s) as client:
            r = client.post(url, headers=headers, json=payload)
            r.raise_for_status()
            data = r.json()
            content = data["choices"][0]["message"]["content"]
            return json.loads(content)


def build_provider() -> LLMProvider | None:
    cfg = load_llm_config()
    if not cfg.enabled:
        return None
    return OpenAICompatibleProvider(cfg)
