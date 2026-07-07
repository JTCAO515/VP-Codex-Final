"""
Mock email sender — 开发阶段将验证/重置 Token 打印到日志。
生产环境替换为真实 SMTP 发送。
"""

from app.config import settings


def send_verification_email(to_email: str, token: str) -> bool:
    """发送邮箱验证邮件（mock）。"""
    verify_url = f"https://visepanda.com/verify-email?token={token}"
    print(f"[EMAIL MOCK] To: {to_email}")
    print(f"[EMAIL MOCK] Subject: Verify your VisePanda account")
    print(f"[EMAIL MOCK] Body: Click {verify_url} to verify your email.")
    print(f"[EMAIL MOCK] Token: {token}")
    return True


def send_password_reset_email(to_email: str, token: str) -> bool:
    """发送密码重置邮件（mock）。"""
    reset_url = f"https://visepanda.com/reset-password?token={token}"
    print(f"[EMAIL MOCK] To: {to_email}")
    print(f"[EMAIL MOCK] Subject: Reset your VisePanda password")
    print(f"[EMAIL MOCK] Body: Click {reset_url} to reset your password.")
    print(f"[EMAIL MOCK] Token: {token}")
    return True
