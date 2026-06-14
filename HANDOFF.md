# VisePanda v3.0.1 — Handoff Document

> **Last Updated:** 2026-06-14  
> **Status:** ✅ All 3 Phases Complete  
> **Repo:** `JTCAO515/vise-panda-2` (SSH: `git@github.com:JTCAO515/vise-panda-2.git`)  
> **Live URL:** `https://vise-panda-2.vercel.app`  
> **Agent Memory Key:** `VisePanda v3.0.1` in `.hermes/skills/`

---

## 1. Product Overview

**VisePanda** is an AI-powered China travel planning platform. Users chat with a panda-themed AI assistant (DeepSeek V4 Flash) to get personalized day-by-day itineraries, food recommendations, hotel suggestions, and local tips across 36 Chinese cities.

**Core philosophy:** "Like chatting with a local friend who knows China." Not a generic AI chatbot — a curated knowledge base + structured output + China-specific expertise.

**Target users:** English-speaking foreigners visiting or living in China; high-end independent travelers to China.

---

## 2. Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Vercel Serverless                      │
│                                                          │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────┐  │
│  │  web/        │  │  api/        │  │  data/          │  │
│  │  index.html  │  │  index.py    │  │  cities.json    │  │
│  │  app.css     │  │  (WSGI,      │  │  food.json     │  │
│  │  app.js      │  │  stdlib)     │  │  hotels.json   │  │
│  └──────┬───────┘  └──────┬───────┘  │  tips.json     │  │
│         │                 │          │  tools.json     │  │
│         │  SSE Stream     │          └────────────────┘  │
│         ├─────────────────┤                              │
│         │                 ▼                              │
│         │     ┌───────────────────┐                      │
│         │     │  DeepSeek V4 Flash│                      │
│         │     │  (deepseek-chat)  │                      │
│         │     └───────────────────┘                      │
│         ▼                                                │
│  ┌─────────────┐                                         │
│  │  static/    │  (23 city images, legacy v2 files)      │
│  └─────────────┘                                         │
└─────────────────────────────────────────────────────────┘
```

### Key Design Decisions

| Decision | Choice | Why |
|----------|--------|-----|
| **Backend** | Python WSGI, stdlib only | Zero pip deps = zero Vercel build failures |
| **Frontend** | Single-page app, vanilla JS | No framework overhead, fast load |
| **LLM** | DeepSeek V4 Flash (`deepseek-chat`) | Best price/quality for travel content |
| **Database** | JSON files + localStorage | No server DB needed at current scale |
| **Design** | Panda × Chinese aesthetic | Stands out from generic AI tools |
| **Deploy** | GitHub → Vercel auto-deploy | Push to `main` = deploy automatically |

---

## 3. Current State (v3.0.1)

### Phase 1 — Foundation ✅
- [x] WSGI handler with routing, 404 handling, static file serving
- [x] 36-city knowledge base (cities/food/hotels/tips/tools) in JSON
- [x] SSE streaming chat with DeepSeek V4 Flash
- [x] System prompt with full knowledge injection (per-city food/hotels/tips/price estimates)
- [x] Dark/light theme CSS variable system

### Phase 2 — Experience ✅
- [x] Real city images on cards (8 cities with JPGs)
- [x] Hero decorations (bamboo/lanterns/clouds) with float animations
- [x] Chat suggestion chips (context-aware, 6 options)
- [x] Markdown rendering in chat (bold/lists/code/HR)
- [x] Stop streaming button
- [x] City detail modal (food/hotels/tips/highlights)
- [x] Chat history persistence (localStorage, Ctrl+Shift+C clears)
- [x] Card stagger entrance animations
- [x] 4-breakpoint responsive (1024/768/480/360px)
- [x] Multi-turn conversation city tracking (`currentCity`)
- [x] Light theme full component adaptation

### Phase 3 — Depth ✅
- [x] Trip persistence (auto-save itineraries to localStorage)
- [x] Trips tab (list/load/share/delete)
- [x] Trip share (copy clean text to clipboard with brand header)
- [x] Toast notifications
- [x] Leaflet dark map (8 cities, 33 POIs, color-coded markers)
- [x] Price estimate data (7 cities: budget/mid/luxury daily, flight, food)
- [x] Price estimates injected into system prompt
- [x] Trip validation API (`/api/validate`)

### Known Quirks / Gotchas

1. **Vercel .python-version:** Uses 3.12 (vercel.json says 3.11 but .python-version=3.12 wins)
2. **Git push only:** Vercel CLI OIDC tokens expire; always push via SSH to `git@github.com:JTCAO515/vise-panda-2.git`
3. **DeepSeek env vars:** Code checks `LLM_API_KEY` first, then `DEEPSEEK_API_KEY`. Configured in Vercel dashboard.
4. **City images:** Only 8 of 36 cities have real JPGs (`static/img/city-{name}.jpg`). Others show gradient fallback.
5. **Map data:** Only 8 major cities have coordinate/POI data. Other cities show no map section.
6. **Trip storage:** localStorage only (max ~5MB). Heavy use will hit quota — no server-side persistence.
7. **Static file detection:** `_serve_static` checks `web/` then `static/` — legacy v2 files in `static/` may conflict.

---

## 4. File Structure

```
vise-panda-2/
├── api/
│   └── index.py           # WSGI handler (~550 lines, stdlib only)
│                          # ALL routes: health, chat, cities, tools, estimate, validate, static
│
├── web/                   # ACTIVE frontend (v3.0.1)
│   ├── index.html         # SPA entry: Header, Hero, Chat, Trips, Cities views, Modals
│   ├── app.css            # Panda × China design system (~800 lines, CSS vars)
│   └── app.js             # Frontend logic (~950 lines, IIFE pattern)
│
├── data/                  # Knowledge base (JSON)
│   ├── cities.json        # 36 cities: name_cn, province, best_season, days, vibe, highlights
│   ├── food.json          # 31 cities: name_en/name_cn, description, price_range, must_try
│   ├── hotels.json        # 31 cities: budget/mid/luxury (range, desc, areas), tip
│   ├── tips.json          # 12 cities: local insider tips
│   └── tools.json         # 5 tools: packing, pricing, visa, phrases, emergency
│
├── static/                # LEGACY v2 files (keep for images only)
│   └── img/               # 17 JPGs: 8 city photos, 4 food, 5 brand/other
│
├── docs/                  # Legacy v2 documentation (not updated for v3)
│
├── vercel.json            # @vercel/python, all routes → api/index.py
├── .python-version        # 3.12
├── requirements.txt       # Empty (stdlib only)
│
├── PRD_PRODUCT_ANALYSIS.md  # Strategy canvas, competitive analysis, opportunity tree
├── PLAN.md                  # Phase 1-3 iteration roadmap
├── README.md                # Quickstart, API docs, project overview
└── DESIGN.md                # v2 design reference (not updated for v3)
```

---

## 5. API Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/health` | Health check → `{"status":"alive","version":"3.0.1"}` | None |
| POST | `/api/chat` | SSE streaming chat. Body: `{messages, city?}` | None |
| GET | `/api/cities` | List all 36 cities (summary: name, season, days, vibe, image, highlights) | None |
| GET | `/api/cities/:city` | City detail (food[], hotels{}, tips[], map{}, estimate{}) | None |
| GET | `/api/tools` | List 5 tools | None |
| GET | `/api/tools/:name` | Tool detail data | None |
| GET | `/api/estimate` | Price estimates for 7 major cities | None |
| POST | `/api/validate` | Validate trip. Body: `{city, days}` → `{valid, warnings, tips}` | None |
| GET | `/*` | Static files (web/ then static/) | None |

### Chat Request Format
```json
{
  "messages": [
    {"role": "user", "content": "Plan 3 days in Beijing"},
    {"role": "assistant", "content": "..."}
  ],
  "city": "beijing"
}
```

### Chat Response Format (SSE)
```
event: message
data: {"token": "Based"}

event: message
data: {"token": " on"}

event: message
data: {"token": ""}

event: message
data: {"done": true}
```

---

## 6. Key Config

### Environment Variables (Vercel Dashboard)

| Variable | Value | Notes |
|----------|-------|-------|
| `LLM_API_KEY` | `sk-...` | DeepSeek API key (code checks this first) |
| `DEEPSEEK_API_KEY` | `sk-...` | Fallback variable name |
| `LLM_BASE_URL` | `https://api.deepseek.com` | OpenAI-compatible endpoint |
| `DEEPSEEK_MODEL` | `deepseek-chat` | DeepSeek V4 Flash model name |

### Vercel Config (`vercel.json`)
- Runtime: `@vercel/python`
- Max Lambda: 10MB
- All routes → `api/index.py`
- `.vercelignore` excludes unnecessary files

---

## 7. System Prompt Architecture

The system prompt is built dynamically in `_build_system_prompt(params)`:

1. **Identity:** "VisePanda, expert AI China travel planner"
2. **Core rules:** Use specific data, structured format, be honest
3. **Itinerary format:** Day-by-day with 🕐🍽️🏨💡 emoji markers
4. **Quick format:** For brief recommendations
5. **City knowledge:** When `city` param is set, injects:
   - City info (name_cn, province, season, days, vibe)
   - Food (6 items, ⭐ for must-try, with prices)
   - Hotels (budget/mid/luxury with areas and prices)
   - Local tips (up to 4)
   - **Price estimates** (budget/mid/luxury daily, flight, meal)
6. **Popular cities guide:** 8-city quick reference table

**Total system prompt length:** ~2,500 chars for Beijing (varies by city data)

---

## 8. Frontend Component Map

```
index.html
├── #header (fixed, glassmorphism)
│   ├── .brand (🐼 VisePanda)
│   ├── .header-nav
│   │   ├── Home
│   │   ├── Chat
│   │   ├── Trips
│   │   └── Cities
│   └── .theme-toggle (🌙/☀️)
│
├── #main
│   ├── #view-home
│   │   ├── .hero (panda float, deco elements, CTA)
│   │   └── .city-grid (8 cards with skeleton loading)
│   │
│   ├── #view-chat
│   │   ├── #chat-messages (scrollable, markdown rendered)
│   │   ├── #chat-suggestions (context-aware chips)
│   │   └── .chat-input-bar (textarea + Send/Stop)
│   │
│   ├── #view-trips
│   │   └── #trips-grid (saved itinerary cards)
│   │
│   └── #view-cities
│       └── #cities-grid (all 36 city cards)
│
├── #city-detail-overlay (modal: info/food/hotels/tips/map/estimate)
├── #trip-detail-overlay (modal for trip view)
└── #footer
```

---

## 9. Data Flow

```
User types message
  → detectCity() checks for city names in input
  → Send message + city to /api/chat
    → API builds system prompt with city knowledge
    → DeepSeek V4 Flash generates SSE stream
    → Frontend renders markdown tokens in real-time
  → After response completes:
    → autoSaveTrip() detects if it's an itinerary (Day markers)
    → If yes: saves to localStorage, shows "Saved!" note
    → updateSuggestions() refreshes chips based on context
```

---

## 10. Next Steps (Phase 4+)

| Priority | Feature | Complexity | Notes |
|----------|---------|------------|-------|
| 🔴 | **Knowledge expansion** (36→50+ cities) | Medium | Need data for ~20 more cities + POI coordinates |
| 🔴 | **Server-side trip persistence** | Medium | Move from localStorage to JSON project DB or Supabase |
| 🟡 | **Trip comparison** (2-3 itineraries side by side) | Medium | Chat refinement feature |
| 🟡 | **Social sharing** (Twitter/X,小红书 format export) | Low | Format itinerary text for social platforms |
| 🟡 | **PWA** (offline, service worker, manifest) | Medium | Use existing `static/sw.js` as base |
| 🟢 | **More map POIs** (remaining cities) | Low | Add coordinates and POIs to MAP_DATA |
| 🟢 | **City food images** (static/img/food-*.jpg for more cities) | Low | Currently only 4 cities have food photos |
| 🟢 | **i18n** (Chinese language mode) | Medium | Toggle between EN/CN interfaces |

### Quick Wins (can be done in 1 iteration)
1. Add remaining 28 city coordinates + basic POIs
2. Fix README.md to reflect current v3.0.1 state (it's still showing "To Build")
3. Update DESIGN.md for v3.0.1 design system

---

## 11. Troubleshooting

| Problem | Cause | Fix |
|---------|-------|-----|
| Vercel deploy fails | .python-version != vercel.json runtime | Keep both at 3.12 |
| Chat returns empty | DeepSeek API key not set | Check Vercel env vars for LLM_API_KEY |
| City images not showing | Path mismatch | Images at `static/img/city-{name}.jpg`, served as `/static/img/...` |
| Map not rendering | Leaflet CDN blocked | Check browser console; may need fallback CDN |
| Trip not auto-saving | Content doesn't match itinerary pattern | Check regex: `/\*\*Day \d+|Day \d+:/i` |
| Git push fails (HTTPS timeout) | China GFW blocks GitHub HTTPS | Use SSH: `git@github.com:JTCAO515/vise-panda-2.git` |

---

## 12. References

- **PRD + Strategy:** `PRD_PRODUCT_ANALYSIS.md` (19KB — strategic canvas, competitive analysis, opportunity tree)
- **Iteration Plan:** `PLAN.md` (6KB — full roadmap with all 15 iterations)
- **Design System:** CSS variables in `web/app.css` (dark + light themes)
- **Popular Design References:** Hermes skill `popular-web-designs` (54 design systems: Linear, Vercel, Stripe, etc.)
- **Vercel Python Deployment:** Hermes skill `vercel-python-deployment`

---

*End of Handoff. For questions, check `PLAN.md` or search session history for "VisePanda".*
