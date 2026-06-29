# Dashboard IA Notes

Version: v6.2.1

The Dashboard is now VisePanda's service hub. The primary navigation has three tabs only:

- Chatbot: AI consultation, itinerary planning, city questions, and follow-up prompts.
- Dashboard: hotels, maps, deals, saved trip summaries, recommended cities, travel tools, and readiness.
- Translation: voice-readiness, text lookup, and the travel phrase library.

## Mobile Layout

Use a single-column stack on portrait phones:

1. Weather/location strip.
2. Quick planner.
3. Three action cards: Hotels, Map, Deals.
4. Recent questions and saved trip summary.
5. Hotels, map POI, deals, cities, tools, trips.

The bottom tab bar remains three icon-first controls. High-frequency actions stay in thumb reach, but dense planning information lives in Dashboard cards instead of extra tabs.

## API Contracts

- `GET /api/maps/geocode`
- `GET /api/maps/place`
- `GET /api/maps/translate`
- `GET /api/hotels/search`
- `GET /api/hotels/detail`
- `POST /api/hotels/book`
- `GET /api/deals/search`
- `GET /api/deals/detail`

Current data is intentionally stubbed and safe to ship. Real Amap, hotel, or group-buying credentials must stay behind backend routes.
