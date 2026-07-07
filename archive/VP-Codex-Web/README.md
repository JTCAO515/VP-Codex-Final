# VisePanda

VisePanda is an English-native China travel butler for international visitors. It combines pre-trip planning, translation, city intelligence, route help, visa readiness, saved trips, practical tools, and a streaming AI guide behind a small Vercel + Python WSGI deployment.

Active repository: `https://github.com/JTCAO515/VP-Codex-Web`

## Current Version

`v6.2.1` upgrades VisePanda from travel planner to all-in-one China travel butler. It consolidates the app into Chatbot, Dashboard, and Translation; adds DeepSeek health checks; introduces map, hotel, and deals API stubs; and keeps Phase 2 community out of scope.

## Documentation

- `HANDOFF.md`: full current handoff and next-agent guide.
- `CONTEXT.md`: product context, active architecture, and constraints.
- `PLAN.md`: active five-round iteration plan.
- `DESIGN.md`: current UI system and mobile interaction rules.
- `docs/ROADMAP.md`: phased product roadmap.
- `docs/PRD_USER_SYSTEM.md`: user-system product requirements.
- `docs/TECH_USER_SYSTEM.md`: user-system technical notes.

## Product Surface

- Chatbot: default first-screen AI travel guide with a clean conversation surface, DeepSeek status, generated follow-up suggestions, and a deterministic local fallback.
- Dashboard: planning command center with destination input, weather/location strip, recent questions, saved trips, hotels, map POI, deals, recommended cities, travel tools, and readiness checklist.
- Translate: native text translation, phrase cards, local history, and voice-readiness for taxi, hotel, dining, attractions, shopping, and emergency moments.
- Admin: minimal user management console gated by explicit admin credentials.

## Tech Stack

- Backend: Python WSGI, standard library only.
- Frontend: static HTML, CSS, and vanilla JavaScript.
- Deployment: Vercel `@vercel/python`, all routes through `api/index.py`.
- Storage: SQLite for users, sessions, password resets, and trips.
- Data: JSON datasets in `data/` plus image assets in `static/img/`.

## Local Run

```powershell
python -c "from api.index import app; from wsgiref.simple_server import make_server; server = make_server('127.0.0.1', 8765, app); print('http://127.0.0.1:8765'); server.serve_forever()"
```

Useful checks:

```powershell
python -m unittest discover -s tests -v
node --test web/tests/*.test.js
```

## Environment

- `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET`: optional; enables Google OAuth login.
- `GOOGLE_REDIRECT_URI`: optional; defaults to `/api/auth/google/callback` on the app base URL.
- `RESEND_API_KEY` and `EMAIL_FROM`: optional; sends email verification codes through Resend.
- `AUTH_EXPOSE_EMAIL_CODE=1`: test-only verification code exposure.
- `DEEPSEEK_API_KEY`: optional; enables remote AI answers for `/api/chat`.
- `DEEPSEEK_MODEL`: optional; defaults to `deepseek-v4-flash`.
- `AMAP_KEY`: optional; future server-side Amap integration. Do not expose it to frontend code.
- `AMAP_SECURITY_CODE`: optional; future server-side Amap security pairing.
- `OPENAI_COMPATIBLE_API_KEY`: optional key for another OpenAI-compatible chat completions provider.
- `OPENAI_COMPATIBLE_BASE_URL`: optional base URL for the compatible provider, for example a `/v1` endpoint.
- `OPENAI_COMPATIBLE_MODEL`: optional model name for the compatible provider.
- `AUTH_DB_PATH`: optional SQLite path for local or test storage.
- `ADMIN_EMAIL` and `ADMIN_PASSWORD`: optional admin seed. Weak defaults such as `admin123` are ignored.
- `AUTH_EXPOSE_RESET_TOKEN=1`: test-only reset token exposure.

## API

- `GET /api/health`
- `GET /api/config`
- `POST /api/chat`
- `GET /api/cities`
- `GET /api/cities/:id`
- `GET /api/map`
- `GET /api/maps/geocode?q=北京故宫`
- `GET /api/maps/place?type=hotel&lat=39.916&lng=116.397`
- `GET /api/maps/translate?name=故宫`
- `GET /api/hotels/search?city=北京&checkin=2026-06-28&checkout=2026-06-30`
- `GET /api/hotels/detail?id=bj-legend-001`
- `POST /api/hotels/book`
- `GET /api/deals/search?city=北京&type=food`
- `GET /api/deals/detail?id=bj-duck-001`
- `GET /api/translations`
- `GET /api/tools`
- `GET /api/tools/:id`
- `GET /api/visa/countries`
- `GET /api/visa/info?nationality=us`
- `POST /api/visa/generate`
- `POST /api/auth/register`
- `POST /api/auth/verify-email`
- `POST /api/auth/resend-verification`
- `GET /api/auth/google/start`
- `GET /api/auth/google/callback`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/auth/me`
- `POST /api/auth/update-profile`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`
- `GET /api/trips`
- `POST /api/trips`
- `DELETE /api/trips/:id`
- `GET /api/admin/users`
- `DELETE /api/admin/users/:id`

## Structure

```text
api/        WSGI router and API modules
data/       JSON knowledge and policy datasets
data/translations/ Native travel translation dictionaries
static/img/ Visual assets used by the app
tests/      Python contract tests
web/        Static app, PWA manifest, service worker, and Node tests
vercel.json Vercel routing configuration
```
