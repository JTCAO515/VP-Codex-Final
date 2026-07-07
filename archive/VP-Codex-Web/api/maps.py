from http import HTTPStatus

from api.common import error_response, json_response, query_params


KNOWN_PLACES = {
    "beijing forbidden city": {"name": "故宫", "english": "Forbidden City", "pinyin": "Gu Gong", "lat": 39.9163, "lng": 116.3972},
    "北京故宫": {"name": "故宫", "english": "Forbidden City", "pinyin": "Gu Gong", "lat": 39.9163, "lng": 116.3972},
    "故宫": {"name": "故宫", "english": "Forbidden City", "pinyin": "Gu Gong", "lat": 39.9163, "lng": 116.3972},
    "the bund": {"name": "外滩", "english": "The Bund", "pinyin": "Wai Tan", "lat": 31.2400, "lng": 121.4900},
    "外滩": {"name": "外滩", "english": "The Bund", "pinyin": "Wai Tan", "lat": 31.2400, "lng": 121.4900},
    "terracotta warriors": {"name": "兵马俑", "english": "Terracotta Warriors", "pinyin": "Bing Ma Yong", "lat": 34.3840, "lng": 109.2780},
    "兵马俑": {"name": "兵马俑", "english": "Terracotta Warriors", "pinyin": "Bing Ma Yong", "lat": 34.3840, "lng": 109.2780},
}

POIS = {
    "hotel": [
        {"id": "hotel-beijing-legend", "name": "Beijing Legend Hotel", "nameCn": "北京华府酒店", "distance": "0.8 km", "tags": ["Accepts foreign guests", "English front desk", "Foreign cards"]},
        {"id": "hotel-hutong-view", "name": "Hutong View Courtyard", "nameCn": "胡同景观院落酒店", "distance": "1.6 km", "tags": ["Passport check-in", "Metro nearby"]},
    ],
    "food": [
        {"id": "food-roast-duck", "name": "Roast Duck House", "nameCn": "北京烤鸭店", "distance": "0.9 km", "tags": ["Picture menu", "Signature duck", "Card friendly"]},
        {"id": "food-noodle-lane", "name": "Noodle Lane", "nameCn": "面巷", "distance": "1.3 km", "tags": ["Casual", "Low spice", "Good for solo travelers"]},
    ],
    "attraction": [
        {"id": "poi-forbidden-city", "name": "Forbidden City", "nameCn": "故宫", "distance": "0.2 km", "tags": ["Passport required", "Reserve ahead"]},
        {"id": "poi-jingshan", "name": "Jingshan Park", "nameCn": "景山公园", "distance": "0.7 km", "tags": ["Skyline view", "Easy walk"]},
    ],
}


def _find_place(query):
    normalized = query.strip().lower()
    return KNOWN_PLACES.get(normalized) or KNOWN_PLACES.get(query) or next(
        (place for key, place in KNOWN_PLACES.items() if normalized in key.lower() or key.lower() in normalized),
        None,
    )


def _geocode(environ, start_response):
    params = query_params(environ)
    query = (params.get("q") or "").strip()
    if not query:
        return error_response(start_response, HTTPStatus.BAD_REQUEST, "query_required", "Search text is required.", environ)
    match = _find_place(query) or {"name": query, "english": query, "pinyin": "", "lat": 39.9042, "lng": 116.4074}
    return json_response(start_response, {
        "query": query,
        "provider": "amap-proxy-stub",
        "location": {"lat": match["lat"], "lng": match["lng"]},
        "place": {"name": match["name"], "english": match["english"], "pinyin": match["pinyin"]},
    }, environ=environ)


def _place(environ, start_response):
    params = query_params(environ)
    poi_type = (params.get("type") or "attraction").strip().lower()
    places = POIS.get(poi_type, POIS["attraction"])
    return json_response(start_response, {
        "type": poi_type,
        "origin": {"lat": params.get("lat"), "lng": params.get("lng")},
        "provider": "amap-proxy-stub",
        "places": places,
    }, environ=environ)


def _translate(environ, start_response):
    name = (query_params(environ).get("name") or "").strip()
    if not name:
        return error_response(start_response, HTTPStatus.BAD_REQUEST, "name_required", "Place name is required.", environ)
    match = _find_place(name) or {"name": name, "english": name, "pinyin": ""}
    return json_response(start_response, {
        "name": match["name"],
        "english": match["english"],
        "pinyin": match["pinyin"],
        "notes": "Travel dictionary translation for map and driver-facing use.",
    }, environ=environ)


def dispatch(method, path_parts, environ, start_response):
    if method != "GET":
        return error_response(start_response, HTTPStatus.METHOD_NOT_ALLOWED, "method_not_allowed", "Method not allowed.", environ)
    action = path_parts[2] if len(path_parts) > 2 else ""
    if action == "geocode":
        return _geocode(environ, start_response)
    if action == "place":
        return _place(environ, start_response)
    if action == "translate":
        return _translate(environ, start_response)
    return error_response(start_response, HTTPStatus.NOT_FOUND, "not_found", "Map endpoint not found.", environ)
