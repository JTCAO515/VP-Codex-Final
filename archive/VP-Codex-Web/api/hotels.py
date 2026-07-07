import json
from http import HTTPStatus

from api.common import DATA_DIR, error_response, json_response, query_params


def _load_hotels():
    with (DATA_DIR / "hotels" / "hotels.json").open("r", encoding="utf-8") as handle:
        return json.load(handle)["hotels"]


def _matches_city(hotel, city):
    if not city:
        return True
    haystack = " ".join([hotel.get("city", ""), hotel.get("cityCn", ""), hotel.get("name", ""), hotel.get("nameCn", "")]).lower()
    return city.lower() in haystack


def _search(environ, start_response):
    params = query_params(environ)
    city = (params.get("city") or "").strip()
    hotels = [hotel for hotel in _load_hotels() if _matches_city(hotel, city)]
    return json_response(start_response, {
        "city": city or "China",
        "checkin": params.get("checkin", ""),
        "checkout": params.get("checkout", ""),
        "hotels": hotels,
        "note": "Booking integration stub: records intent and highlights foreigner-friendly requirements.",
    }, environ=environ)


def _detail(environ, start_response):
    hotel_id = (query_params(environ).get("id") or "").strip()
    hotel = next((item for item in _load_hotels() if item["id"] == hotel_id), None)
    if not hotel:
        return error_response(start_response, HTTPStatus.NOT_FOUND, "not_found", "Hotel not found.", environ)
    return json_response(start_response, {"hotel": hotel}, environ=environ)


def _book(environ, start_response):
    return json_response(start_response, {
        "ok": True,
        "status": "intent_recorded",
        "message": "Booking intent recorded. Supplier booking integration is planned for a later iteration.",
    }, status=HTTPStatus.ACCEPTED, environ=environ)


def dispatch(method, path_parts, environ, start_response):
    action = path_parts[2] if len(path_parts) > 2 else ""
    if action == "search" and method == "GET":
        return _search(environ, start_response)
    if action == "detail" and method == "GET":
        return _detail(environ, start_response)
    if action == "book" and method == "POST":
        return _book(environ, start_response)
    return error_response(start_response, HTTPStatus.NOT_FOUND, "not_found", "Hotel endpoint not found.", environ)
