import json
from http import HTTPStatus

from api.common import DATA_DIR, error_response, json_response, query_params


def _load_deals():
    with (DATA_DIR / "deals" / "deals.json").open("r", encoding="utf-8") as handle:
        return json.load(handle)["deals"]


def _search(environ, start_response):
    params = query_params(environ)
    city = (params.get("city") or "").strip().lower()
    deal_type = (params.get("type") or "").strip().lower()
    deals = []
    for deal in _load_deals():
        city_match = not city or city in " ".join([deal.get("city", ""), deal.get("cityCn", "")]).lower()
        type_match = not deal_type or deal_type == deal.get("type", "").lower()
        if city_match and type_match:
            deals.append(deal)
    return json_response(start_response, {
        "city": params.get("city") or "China",
        "type": params.get("type") or "all",
        "deals": deals,
        "note": "Group-buying integration stub with foreigner usability labels.",
    }, environ=environ)


def _detail(environ, start_response):
    deal_id = (query_params(environ).get("id") or "").strip()
    deal = next((item for item in _load_deals() if item["id"] == deal_id), None)
    if not deal:
        return error_response(start_response, HTTPStatus.NOT_FOUND, "not_found", "Deal not found.", environ)
    return json_response(start_response, {"deal": deal}, environ=environ)


def dispatch(method, path_parts, environ, start_response):
    if method != "GET":
        return error_response(start_response, HTTPStatus.METHOD_NOT_ALLOWED, "method_not_allowed", "Method not allowed.", environ)
    action = path_parts[2] if len(path_parts) > 2 else ""
    if action == "search":
        return _search(environ, start_response)
    if action == "detail":
        return _detail(environ, start_response)
    return error_response(start_response, HTTPStatus.NOT_FOUND, "not_found", "Deal endpoint not found.", environ)
