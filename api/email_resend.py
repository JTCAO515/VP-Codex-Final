"""Tiny Resend client. Only one method: send the email-verification code."""
from __future__ import annotations

from . import config
from .common import http_request

VERIFY_HTML = """\
<div style="font-family:'Inter',sans-serif;color:#2c2c2c;padding:24px;max-width:480px;margin:0 auto;background:#f5f0e8">
  <h1 style="font-family:'Newsreader',Georgia,serif;font-weight:500;font-size:22px;letter-spacing:0.02em;">Your VisePanda code</h1>
  <p>Use this code to verify your email:</p>
  <div style="font-family:'IBM Plex Mono',monospace;font-size:32px;letter-spacing:8px;font-weight:600;color:#1e6f9f;background:#ede5d4;padding:16px 20px;text-align:center;border-radius:2px;margin:24px 0;">{code}</div>
  <p style="color:#5a5550;font-size:14px;">The code expires in 10 minutes. If you didn't request it, you can ignore this email.</p>
  <p style="color:#a8a195;font-size:12px;margin-top:32px;">VisePanda — your China travel butler.</p>
</div>
"""


def send_verify(email: str, code: str) -> bool:
    if not config.has_email():
        return False
    payload = {
        "from": config.RESEND_FROM,
        "to": [email],
        "subject": "Your VisePanda code",
        "html": VERIFY_HTML.format(code=code),
        "text": f"Your VisePanda verification code: {code}\nIt expires in 10 minutes.",
    }
    code_status, _body, _hdrs = http_request(
        "https://api.resend.com/emails",
        method="POST",
        headers={"Authorization": f"Bearer {config.RESEND_API_KEY}"},
        data=payload,
        timeout=10,
    )
    return 200 <= code_status < 300
