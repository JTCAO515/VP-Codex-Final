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


class SeedHotelProvider(HotelProvider):
    """v2.5: Realistic hotel data from seed (60+ Chinese hotels across major cities)."""

    def __init__(self):
        from app.seed_hotels import get_hotels_for_city
        self._get = get_hotels_for_city

    def search(self, city: str, check_in: str, check_out: str, adults: int) -> list[dict]:
        hotels = self._get(city)
        if not hotels:
            # Try case-insensitive
            hotels = self._get(city.title()) or self._get(city.capitalize())
        results = []
        for h in hotels:
            # Calculate total price
            try:
                nights = max(1, (__import__("datetime").datetime.strptime(check_out, "%Y-%m-%d") - __import__("datetime").datetime.strptime(check_in, "%Y-%m-%d")).days)
            except Exception:
                nights = 1
            results.append({
                "offer_id": f"seed-{city.lower()}-{h['name_en'].lower().replace(' ', '-')[:40]}",
                "name": h["name"],
                "name_en": h.get("name_en", ""),
                "stars": h["stars"],
                "price_per_night": h["price_per_night"],
                "total_price": h["price_per_night"] * nights,
                "currency": "CNY",
                "rating": h.get("rating", 4.0),
                "review_count": h.get("review_count", 0),
                "address": h.get("address", ""),
                "city": city,
                "image_url": h.get("image_url", ""),
                "amenities": h.get("amenities", []),
                "cancel_policy": h.get("cancel_policy", ""),
                "pay_type": "guarantee",
            })
        return results

    def create_booking(self, offer_id: str, guest_info: dict) -> dict:
        return {"provider_booking_ref": f"SEED-{offer_id}-{__import__('uuid').uuid4().hex[:8]}", "status": "confirmed"}


def get_hotel_provider() -> HotelProvider:
    """Select hotel provider based on environment."""
    import os
    provider_name = os.getenv("HOTEL_PROVIDER", "seed").lower()
    if provider_name == "seed":
        return SeedHotelProvider()
    if provider_name == "stub":
        return StubHotelProvider()
    # Default to seed
    return SeedHotelProvider()

