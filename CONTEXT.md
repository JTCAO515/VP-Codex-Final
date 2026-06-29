# VisePanda Context

Last updated: 2026-06-23
Current version: v6.2.1
Repository: https://github.com/JTCAO515/VP-Codex-Web
Domain: https://go2china.space

## Product Definition

VisePanda is an English-native China travel butler for foreign visitors. It helps before the trip with planning and readiness, then helps during the trip with translation, meals, routes, rides, local tools, and practical city context.

The product should feel like a mobile travel companion, not a generic chatbot, not a pure booking site, and not a marketing landing page.

## Primary User Needs

- Decide when to visit China and which cities to choose.
- Understand visa, transit, phone, VPN, payment, culture, and safety readiness.
- Build a route and daily itinerary.
- Find foreigner-friendly hotel and transport guidance.
- Translate taxi, hotel, restaurant, attraction, shopping, and emergency situations.
- Understand dishes, signs, cultural customs, and common local app workflows.
- Save or resume trip context.

## Current Product Views

| View | Purpose | Current state |
| --- | --- | --- |
| Chatbot | AI consultation, itinerary planning, city questions, and follow-up prompts | Active |
| Dashboard | Command center for questions, saved trips, hotels, map POI, deals, cities, tools, and readiness | Active |
| Translate | Native text/phrase translation for on-trip situations | Active foundation |
| Account | Email/password, email verification, optional Google OAuth | Active |
| Admin | Minimal user management | Internal only |

## Product Phases

| Phase | Scope | Status |
| --- | --- | --- |
| Phase 1 | Pre-trip planning, entry readiness, itinerary, hotels, major transport | Foundation complete |
| Phase 1.5 | During-trip butler: translate, restaurants, local routes, taxis, group-buying guidance | Current focus |
| Phase 2 | Community, journals, companions, ratings, social feedback | Out of scope for this release |

## Current Architecture

```text
web/index.html + web/app.css + web/app.js
        |
        v
api/index.py
        |
        +-- api/auth.py
        +-- api/chat.py
        +-- api/cities.py
        +-- api/deals.py
        +-- api/health.py
        +-- api/hotels.py
        +-- api/maps.py
        +-- api/tools.py
        +-- api/translations.py
        +-- api/visa.py
        +-- api/config.py
        |
        v
data/*.json + data/translations/*.json + data/hotels/*.json + data/deals/*.json + SQLite + optional providers
```

## Key Decisions

- Keep the frontend lightweight: static HTML, CSS, vanilla JavaScript.
- Keep UI English-native.
- Treat mobile portrait as the primary surface.
- Keep primary navigation to three core tabs; aggregate service modules inside Dashboard.
- Keep Amap and supplier keys behind backend routes.
- Use curated JSON translation data before adding complex retrieval.
- Keep Phase 2 community documented only.
- Hide optional provider features until configured.
- Do not commit secrets.

## Current External Providers

| Provider | Use | Required for local use |
| --- | --- | --- |
| DeepSeek | Remote AI chat route | No |
| OpenAI-compatible provider | Optional alternate chat route | No |
| Resend | Email verification delivery | No |
| Google OAuth | Optional Google login | No |
| Browser speech APIs | Future voice translation STT/TTS | No |

## Known Constraints

- Translation is currently dictionary/phrase based, not full machine translation.
- Voice translation is a browser capability placeholder and needs a fuller STT/TTS path.
- SQLite is acceptable for MVP but should be revisited for scale.
- `web/app.js` is high-change and should eventually be split by feature.
- Real-device mobile QA remains important.
