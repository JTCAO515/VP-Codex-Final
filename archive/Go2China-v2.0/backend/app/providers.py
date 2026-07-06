from __future__ import annotations

from dataclasses import dataclass


class TranslationProvider:
    def translate(self, source_lang: str, target_lang: str, text: str) -> str:
        raise NotImplementedError


class StubTranslationProvider(TranslationProvider):
    def translate(self, source_lang: str, target_lang: str, text: str) -> str:
        return f"[{source_lang}->{target_lang}] {text}"


translation_provider: TranslationProvider = StubTranslationProvider()


class OCRProvider:
    def extract_text_lines(self, content: bytes) -> list[str]:
        raise NotImplementedError


class StubOCRProvider(OCRProvider):
    def extract_text_lines(self, content: bytes) -> list[str]:
        # 最小可用：返回固定示例。真实实现可替换为 Tesseract/云OCR。
        return ["Kung Pao Chicken 58", "Mapo Tofu 42", "Jasmine Tea 18"]


ocr_provider: OCRProvider = StubOCRProvider()


class HotelProvider:
    def search(self, city: str, check_in: str, check_out: str, adults: int) -> list[dict]:
        raise NotImplementedError

    def create_booking(self, offer_id: str, guest_info: dict) -> dict:
        raise NotImplementedError


class StubHotelProvider(HotelProvider):
    def search(self, city: str, check_in: str, check_out: str, adults: int) -> list[dict]:
        return [
            {
                "offer_id": f"stub-{city}-1",
                "hotel_name": f"{city} Central Hotel",
                "price": {"amount": 680, "currency": "CNY", "unit": "night"},
                "pay_type": "pay_at_hotel",
                "cancel_policy": "Free cancellation before 18:00 one day prior",
                "notes": ["Passport required for check-in"],
            },
            {
                "offer_id": f"stub-{city}-2",
                "hotel_name": f"{city} Boutique Stay",
                "price": {"amount": 920, "currency": "CNY", "unit": "night"},
                "pay_type": "guarantee",
                "cancel_policy": "Non-refundable",
                "notes": ["Late arrival notice recommended"],
            },
        ]

    def create_booking(self, offer_id: str, guest_info: dict) -> dict:
        # stub: always confirm
        return {"provider_booking_ref": f"PB-{offer_id}-0001", "status": "confirmed"}


hotel_provider: HotelProvider = StubHotelProvider()

