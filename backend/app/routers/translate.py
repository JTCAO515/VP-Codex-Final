from __future__ import annotations

import base64

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel

from app.providers import ocr_provider, translation_provider

router = APIRouter()


class TranslateTextIn(BaseModel):
    source_lang: str
    target_lang: str
    text: str


@router.post("/translate/text")
def translate_text(payload: TranslateTextIn):
    translated = translation_provider.translate(payload.source_lang, payload.target_lang, payload.text)
    return {"translated_text": translated}


class TranslateImageIn(BaseModel):
    source_lang: str
    target_lang: str
    image_base64: str  # base64 of image bytes (png/jpg)


@router.post("/translate/image")
def translate_image(payload: TranslateImageIn):
    try:
        content = base64.b64decode(payload.image_base64)
    except Exception as e:
        raise HTTPException(status_code=400, detail="Invalid base64 image") from e

    lines = ocr_provider.extract_text_lines(content)
    translated_lines = [
        {
            "source": line,
            "translated": translation_provider.translate(payload.source_lang, payload.target_lang, line),
        }
        for line in lines
    ]
    return {"lines": translated_lines}

