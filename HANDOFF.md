# VisePanda v3.0.8 — Handoff Document

> **Last Updated:** 2026-06-14
> **Status:** ⏸️ Paused — 14 iterations completed (v3.0.1 → v3.0.8)
> **Repo:** `git@github.com:JTCAO515/vise-panda-2.git` (SSH only — HTTPS times out)
> **Live URL:** https://www.go2china.space (Vercel auto-deploy on push)
> **Agent Memory Key:** `VisePanda`, `vise-panda-2`, `go2china`

---

## 1. Product Overview

**AI China Travel Platform** — Specialized AI travel planner for travelers to China.

Powered by DeepSeek V4 Flash + 36-city curated knowledge base, it covers destination recommendations, day-by-day itineraries, local food/hotel/transport tips, and travel toolkit. Not a generic AI assistant — a China-specialized AI travel planner with Panda × Chinese aesthetic.

## 2. Architecture

```
┌─ Frontend (SPA) ─────────────────────┐
│  index.html + app.js + app.css        │
│  Panda Chinese Style, Dark/Light      │
│  Leaflet (fallback) / AMap (Gaode)    │
│  SSE streaming chat                   │
│  Multi-bubble rendering (v3.0.6)      │
│  Inline city images (v3.0.7)          │
└────────┬───────────────┬──────────────┘
         │ fetch/SSE     │ static files
         ▼               ▼
┌─ Vercel WSGI (Python stdlib) ────────┐
│  api/index.py — all routes            │
│  api/prompt.py — system prompt        │
│  /api/chat    → SSE stream (DeepSeek) │
│  /api/cities  → city knowledge base   │
│  /api/map     → 36-city coordinates   │
│  /api/config  → AMap keys (env vars)  │
│  /static/     → city images (27 .jpg) │
│  data/        → JSON knowledge base   │
└────────┬──────────────────────────────┘
         │ LLM call
         ▼
┌─ DeepSeek V4 Flash ───────────────────┐
│  deepseek-chat model                  │
│  SSE streaming, temp=0.7, max=2048    │
└───────────────────────────────────────┘
```

### Key Design Decisions

| Decision | Choice | Why |
|----------|--------|-----|
| Backend | Python WSGI (stdlib only) | Zero pip deps, cold start fast on Vercel |
| Frontend | Vanilla JS SPA | No framework overhead, direct DOM control |
| LLM | DeepSeek V4 Flash | Cost-effective, fast streaming, Chinese travel expertise |
| Maps | AMap (Gaode) + Leaflet fallback | AMap for China (better data), Leaflet when no key |
| State | localStorage | No backend DB needed, simple persistence |
| Images | Static JPEGs from Wikimedia | Zero API cost, fast loading, CC-licensed |
| Chat | Multi-bubble SSE | Split sections into separate bubbles, inline images |

## 3. Current State

### ✅ Completed

| Phase | Feature | Version |
|-------|---------|:-------:|
| 🏗️ Core | WSGI backend, SPA frontend, routing | v3.0.1 |
| 🗺️ Maps | Leaflet dark maps, POI markers, AMap integration | v3.0.1 → v3.0.4 |
| 💬 Chat | SSE streaming, DeepSeek, markdown rendering | v3.0.1 |
| 🔍 FAQ | 10-category matching engine, query expansion | v3.0.2 |
| 📚 Knowledge | 36 cities, food/hotels/tips/pricing JSON | v3.0.1 |
| 🎨 Design | Panda × Chinese, dark/light dual themes | v3.0.1 |
| 📱 Mobile | Bottom nav, chat overlay, safe-area, dvh | v3.0.5 |
| 🧩 Multi-Bubble | Split responses into separate bubbles | v3.0.6 |
| 🖼️ Images | 27 city photos from Wikimedia Commons | v3.0.7 |
| 🔒 Security | Input validation, XSS protection, abort fix | v3.0.8 |
| 🇬🇧 English | English-native system prompt | v3.0.4 |
| 🚀 Deploy | Vercel WSGI auto-deploy from GitHub | v3.0.1 |

### 🟡 Known Quirks / Gotchas

| # | Issue | Impact |
|---|-------|--------|
| 1 | **Git push timeout** — SSH works but large image files (60MB+) cause intermittent push timeouts. Use `GIT_SSH_COMMAND="ssh -o ServerAliveInterval=120"` for large pushes | Low — retry works |
| 2 | **ESTIMATE_DATA only covers 7/36 cities** — Only Beijing, Shanghai, Chengdu, Guangzhou, Xi'an, Guilin, Hangzhou have pricing data | Low — other cities show blank estimates |
| 3 | **MAP_DATA POIs only for 8 cities** — Shenzhen onward have coordinates but no POI list | Low — map markers work |
| 4 | **syncOverlayMessages() defined twice** — Lines 95 and 133 in app.js. Second overwrites first. Works correctly but confusing | Very low |
| 5 | **Docstring says v3.0.5** — api/index.py line 2 has stale version in docstring | Cosmetic |
| 6 | **Vercel cold start** — First request after idle period takes ~3-5s due to Python WSGI cold start | Acceptable |

## 4. File Structure

```
vise-panda-2/
├── api/
│   ├── index.py          ACTIVE — WSGI handler (all routes, ~812 lines, stdlib only)
│   └── prompt.py         ACTIVE — System prompt template (~200 lines, English-native)
├── web/
│   ├── index.html        ACTIVE — SPA entry, header/footer/nav/chat-overlay
│   ├── app.js            ACTIVE — Frontend logic (~1382 lines, vanilla JS IIFE)
│   └── app.css           ACTIVE — Panda Chinese style system (~1000 lines)
├── data/
│   ├── cities.json       ACTIVE — 36-city knowledge base (attractions/food/tips)
│   ├── food.json         ACTIVE — City food data
│   ├── hotels.json       ACTIVE — Hotel pricing by tier
│   ├── tips.json         ACTIVE — Local travel tips
│   ├── faq.json          ACTIVE — 10-category FAQ matching data
│   └── tools.json        ACTIVE — Travel toolkit (packing/pricing/visa/phrases/emergency)
├── static/
│   └── img/              ACTIVE — 27 city images + food images + logo files
│       ├── city-{name}.jpg  27 city skyline photos
│       ├── food-{name}.jpg  4 food photos
│       ├── great-wall.jpg   Landmark photo
│       └── logo-*.jpg        Brand assets
├── vercel.json           ACTIVE — Vercel deployment config
├── CHANGELOG.md          ACTIVE — Version history (v3.0.1 → v3.0.8)
├── README.md             ACTIVE — English-native product README
├── PLAN.md               ACTIVE — Iteration roadmap (Iter 1-14)
├── PRD_PRODUCT_ANALYSIS.md  ACTIVE — Product strategy document
├── HANDOFF.md            THIS FILE
└── .vercel/              AUTO — Vercel build cache
```

## 5. API / Interface

| Method | Endpoint | Description | Notes |
|--------|----------|-------------|-------|
| GET | `/api/health` | Health check + version | Returns `{"status":"alive","version":"3.0.8"}` |
| POST | `/api/chat` | SSE streaming chat | Body: `{messages: [...], city?: string}`. Returns SSE stream with `token`, `split`, `image`, `faq`, `done`, `error` events |
| GET | `/api/cities` | List all cities | Returns summary with name_cn, best_season, vibe, highlights |
| GET | `/api/cities/:city` | City detail | Returns full city data + food + hotels + tips + map + estimates |
| GET | `/api/map` | 36-city coordinates | Returns `{cities: {name: {lat, lng}}}` |
| GET | `/api/config` | Client config | Returns `{amap_key, amap_security_code, use_amap, version}` |
| GET | `/api/estimate` | Price estimates | Returns estimates for 7 major cities |
| POST | `/api/validate` | Trip validation | Validates city + days, returns seasonal tips |
| GET | `/api/tools` | List travel tools | Returns `{tools: {name: description}}` |
| GET | `/api/tools/:name` | Tool detail | Returns tool data (packing/pricing/visa/phrases/emergency) |
| GET | `/*` | Static files | Serves from `web/` first, then `static/` |

### SSE Event Types

| Event | Payload | Description |
|-------|---------|-------------|
| `message` | `{"token": "..."}` | Streamed text token |
| `message` | `{"split": true}` | Multi-bubble boundary |
| `message` | `{"image": {"key": "...", "url": "...", "label": "..."}}` | Inline city image |
| `message` | `{"faq": {...}}` | FAQ match badge |
| `message` | `{"done": true}` | Stream complete |
| `error` | `{"error": "..."}` | Stream error |

## 6. Key Config

| Variable | Description | Source |
|----------|-------------|-------|
| `LLM_API_KEY` | DeepSeek API key | Vercel env |
| `LLM_MODEL` | Model name (default: `deepseek-chat`) | Vercel env |
| `LLM_BASE_URL` | API base URL (default: `https://api.deepseek.com/v1`) | Vercel env |
| `AMAP_KEY` | Gaode (AMap) JS API key | Vercel env |
| `AMAP_SECURITY_CODE` | Gaode security JS code | Vercel env |

Fallback env vars: `DEEPSEEK_API_KEY`, `DEEPSEEK_MODEL`, `DEEPSEEK_BASE_URL` also supported.

## 7. Core Logic / Data Flow

### Chat Flow

```
User Input → detectCity() → addMessage(user)
  → POST /api/chat {messages: [...], city: detectCity() || currentCity}
  → Backend: _handle_chat()
    → _match_faq(user_text) — optional FAQ expansion
    → _build_system_prompt(city, faq_match) — inject knowledge base
    → POST DeepSeek API (SSE stream)
    → Stream response with _yield_with_images() processing
      → ---SPLIT--- markers → split event
      → [img:city_key] markers → image event
      → regular text → token event
  → Frontend: SSE reader processes events
    → token → currentBubble += token
    → split → commit bubble, start new
    → image → insert image bubble
    → done → commit final bubbles
```

### Image Resolution

AI outputs `[img:beijing]` or `[img:food-chengdu]` in stream. Backend resolves:
1. `{key}.jpg` (exact match)
2. `city-{key}.jpg` (city photo)
3. `food-{key}.jpg` (food photo)
→ Found: emit `image` event → Frontend renders inline
→ Not found: emit `token` with `[Label]` text fallback

## 8. Frontend / UI Component Map

```
App (VP IIFE)
├── Header
│   ├── Nav bar (desktop): Home, Chat, Map, Cities
│   ├── Version badge (v3.0.8)
│   └── Theme toggle (🌙/☀️)
├── Views
│   ├── view-home: Hero + City cards grid (top 8)
│   ├── view-chat: Full chat container + input
│   ├── view-map: Full China overview (AMap/Leaflet)
│   ├── view-trips: Saved itineraries list
│   ├── view-cities: All 36 city cards grid
│   └── view-tools: Travel toolkit cards
├── Overlays
│   ├── chat-overlay (mobile): Slide-up chat panel
│   ├── city-detail-overlay: City modal with sections
│   └── city-detail-panel: Detail sections
├── Chat Container
│   ├── Messages (msg-user, msg-bot, msg-image)
│   ├── Multi-bubble rendering (bubble-spacer)
│   └── Typing indicator + stop button
├── Bottom Nav (mobile): Home/Chat/Map/Trips/Tools
└── Footer: Version + clear chat
```

## 9. Dependencies

| Layer | Technology | Version |
|-------|-----------|---------|
| Backend | Python 3.11 (stdlib only) | — |
| Frontend | Vanilla JS + CSS3 + HTML5 | — |
| LLM | DeepSeek V4 Flash | `deepseek-chat` |
| Maps | AMap JS API v2 / Leaflet + CartoDB dark | — |
| Deployment | Vercel Serverless | `@vercel/python` |
| Images | Wikimedia Commons (CC-licensed) | 27 city JPEGs |

Zero pip dependencies. Pure Python stdlib for backend (`urllib`, `json`, `os`, `re`, `ssl`, `pathlib`).

## 10. Next Steps

| Pri | Feature | Complexity | Notes |
|:---:|---------|:----------:|-------|
| 🟡 | **Fill remaining 9 city images** — huangshan, jiuzhaigou, lanzhou, guiyang, xining, hohhot, nanchang, yunnan, zhangjiajie | Low | Failed during v3.0.7 batch due to Wikimedia rate limiting |
| 🟡 | **Expand ESTIMATE_DATA to all 36 cities** — Only 7 have pricing info | Low | Add budget/mid/luxury ranges for remaining cities |
| 🟢 | **PWA support** — manifest.json exists (`static/manifest.json`), sw.js exists. Need to wire up service worker registration | Medium | Improve offline UX |
| 🟢 | **Trip sharing** — Export itinerary as text card / shareable link | Medium | Social sharing feature |
| 🟢 | **Expand knowledge base** — 36 → 50+ cities | Medium | More coverage |
| 🟢 | **WeChat Mini Program / Telegram Bot** | High | Cross-platform expansion |
| 🟢 | **Update docstring** — api/index.py line 2 says v3.0.5, should match actual | Trivial | Cosmetic |

## 11. Troubleshooting

| Problem | Cause | Fix |
|---------|-------|-----|
| Git push hangs/timeout | SSH upload of 60MB+ image files is slow | `GIT_SSH_COMMAND="ssh -o ServerAliveInterval=120" git push origin main` or retry |
| Vercel HTTPS checkout fails | HTTPS git auth needs token | Use SSH remote: `git@github.com:JTCAO515/vise-panda-2.git` |
| AMap not loading | Missing `AMAP_KEY` or `AMAP_SECURITY_CODE` env vars | Check Vercel env vars; app falls back to Leaflet |
| Chat response empty | LLM API key missing or expired | Check `LLM_API_KEY` in Vercel env |
| City image not showing | AI used `[img:unknown_city]` where no image exists | Backend falls back to text label `[City Name]` |

## 12. References

| Resource | Link |
|----------|------|
| Live site | https://www.go2china.space |
| GitHub repo | `git@github.com:JTCAO515/vise-panda-2.git` |
| DeepSeek API | https://api.deepseek.com |
| AMap (Gaode) | https://console.amap.com/ |
| Vercel dashboard | https://vercel.com/jtcao515/vise-panda-2 |
| Design reference | Linear, Vercel, Stripe design systems |
| CHANGELOG | `CHANGELOG.md` — full version history |
| PLAN | `PLAN.md` — iteration roadmap |
| PRD | `PRD_PRODUCT_ANALYSIS.md` — product strategy |

---

*End of Handoff. To resume: `cd ~/projects/vise-panda-2`, then send "HANDOFF.md" to resume from this snapshot.*
