"""
JWT token 创建/验证 & 密码哈希工具。
"""
from __future__ import annotations

import datetime as dt
import os
from typing import Any

from jose import JWTError, jwt
from passlib.context import CryptContext

# ---- 配置 ----
SECRET_KEY = os.getenv("JWT_SECRET_KEY", "visepanda-ai-dev-secret-change-in-production")
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = int(os.getenv("JWT_EXPIRE_MINUTES", "1440"))  # 默认 24h

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


# ---- 密码工具 ----

def hash_password(password: str) -> str:
    """对明文密码进行 bcrypt 哈希。"""
    return pwd_context.hash(password)


def verify_password(plain: str, hashed: str) -> bool:
    """验证明文密码与哈希是否匹配。"""
    return pwd_context.verify(plain, hashed)


# ---- JWT 工具 ----

def create_access_token(user_id: str, email: str | None = None) -> str:
    """为指定用户创建 JWT access token。"""
    expire = dt.datetime.now(dt.timezone.utc) + dt.timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    payload: dict[str, Any] = {
        "sub": user_id,
        "exp": expire,
        "iat": dt.datetime.now(dt.timezone.utc),
    }
    if email:
        payload["email"] = email
    return jwt.encode(payload, SECRET_KEY, algorithm=ALGORITHM)


def decode_access_token(token: str) -> dict[str, Any] | None:
    """解码并验证 JWT，成功返回 payload，失败返回 None。"""
    try:
        return jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
    except JWTError:
        return None
