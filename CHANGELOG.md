# VisePanda Changelog

## v0.1.46 - 2026-07-01

**Documentation-only iteration — no code changes.** A strategic product-expansion plan covering seven new tracks. Full deep-dive in `docs/planning/v0.1.46-product-expansion.md`; summaries and decisions recorded across the seven workflow docs.

- **Guiding principle changed** (`PRD.md`, `DESIGN.md`): optimize for user experience and answer quality first — **token cost is explicitly not a constraint**. The v0.1.45 "route factual messages away from the LLM to save cost" rationale is reframed: the intent classifier now exists for quality/correctness (route to the right specialist model / verified source), not cost savings. Larger models, multi-model ensembles, and refine-and-verify loops are all in scope.
- **Multi-model Chinese LLM orchestration defined** (§1, ADR-044): a provider-agnostic orchestrator over DeepSeek, Qwen (Aliyun Bailian), Zhipu GLM, Moonshot Kimi, and Baidu ERNIE, with intent-based routing, parallel ensemble + judge for high-stakes answers, a refine-and-verify loop, and a graceful fallback chain that still ends at the mock Butler.
- **Native iOS + Android apps planned** (§2, ADR-045): React Native + Expo recommended (genuinely native, reuses the TypeScript domain layer and API routes) over dual Swift/Kotlin; offline-first travel cache, native camera/mic/push, and a separate China distribution track requiring ICP 备案 / 软著. Plan only — not executed.
- **Tools functional upgrade planned** (§3, ADR-046): the six static Tools become interactive — visa eligibility/transit checker, payment setup wizard, full currency converter, Amap-backed metro route planner, actionable eSIM/VPN options, and one-tap Emergency (call buttons, GPS embassy locator, phrase TTS, share-location).
- **Professional Account UI + lead capture planned** (§4, ADR-047): a dedicated `/account` center (professional, formal, trust-signaling) plus progressive-profiling lead capture (留资) with a prioritized field list — contact (incl. WeChat), trip qualification (nationality/dates/party/budget/cities/purpose), and enrichment with explicit consent.
- **Admin backend + LLM customer briefs planned** (§5, ADR-048): a role-gated `/admin` area where staff see leads and chat conversations, with a multi-model pipeline that distills each customer's data + conversation into a structured `CustomerBrief` (summary, trip intent, budget signal, readiness-to-book score, key preferences, open questions, objections, suggested next action).
- **Supabase schema additions planned** (§6): `0004_leads_and_admin.sql` — `leads`, `lead_events`, `customer_briefs`, `profiles`, and an admin `role`; RLS for admin-wide read; `SUPABASE_SERVICE_ROLE_KEY` server-side only.
- **Frontend & visual optimization track planned** (§7, ADR-049): design tokens + a reusable component library, motion/feedback, empty/error states, accessibility, responsive/tablet polish, performance, and a brand illustration system.
- **Consolidated build sequence recorded** (`PLAN.md` 阶段十三–十八): multi-LLM → Tools functional → Account+leads → admin backend → design system → native apps, with parallelizable legal/ops lead-time work started early.
- **New env keys reserved (server-side only):** `ZHIPU_API_KEY`, `MOONSHOT_API_KEY`, `ERNIE_API_KEY`/`BAIDU_*`, `MINIMAX_API_KEY`. Standing security constraints reaffirmed (mock fallback preserved; service-role key never in browser; only `NEXT_PUBLIC_AMAP_MAPS_KEY` is public).

## v0.1.45 - 2026-07-01

**Documentation-only iteration — no code changes.** This is a macro product-planning pass that distills three research threads (Amap/Meituan data enrichment, Chat×Explore×Trips fusion, and chat-efficiency/UX overhaul) into one coherent seven-iteration roadmap.

- **Product positioning sharpened** (`PRD.md`): VisePanda is reframed around the five concrete fears a Western traveler has before a China trip — visa eligibility, payment, connectivity, language, and itinerary anxiety — and every feature is mapped to the fear it resolves. Chat becomes the single thread that holds the other surfaces together.
- **Chat Intelligence pipeline defined** (`PRD.md`, `DESIGN.md`): a five-stage `Input → Classify → Route → Handle → Normalize` pipeline replaces the current "every message is a full LLM call" model. A 10-intent taxonomy (`create_trip`, `adjust_trip`, `add_poi`, `ask_factual`, `ask_recommendation`, `preference_signal`, `concern`, `logistics`, `add_location`, `unclear`) routes factual and preference messages to non-LLM handlers, cutting cost and latency for an estimated 30–40% of traffic.
- **Response normalization schema defined**: a structured `{headline, body, highlights, watchOut, nextStep}` contract replaces the single free-text `assistantMessage` blob so every Butler reply is scannable and consistent.
- **Preference Profile + intent distillation defined**: a `UserPreferenceProfile` extracted silently from natural conversation (no interrogation form), with a strict "one clarifying question per turn, only when a gap would produce a wrong itinerary" rule.
- **Data-fusion architecture defined** (`DESIGN.md`): a tool-calling Butler that calls Amap/Dianping live during planning (`search_pois`, `get_poi_detail`, `search_dianping`), an `ExploreRichMeta` model for ratings/price/hours/photos, a backwards-compatible rich `TripBlock`, and a two-key Amap split (server-side POI key vs. public JS-map key).
- **Seven-iteration roadmap added** (`PLAN.md` 阶段十二): v0.1.46 Chat Intelligence Layer → v0.1.47 Preference Profile → v0.1.48 Amap Enrichment → v0.1.49 Tool-Calling Butler → v0.1.50 Onboarding + Canvas Quick-Actions → v0.1.51 Navigation Restructure → v0.1.52 Dianping/Meituan + Map.
- **UX audit added** (`PRD.md`): user-journey walkthroughs (first-run, ask-visa, refine-day, translate-menu), a redundancy-removal list (provider-status jargon, duplicate labels, developer-facing confidence copy), and a navigation restructure from 6 flat tabs to 4 tabs + a floating Translate action.
- **Meituan/Dianping API application guide recorded** (`HANDOFF.md`): step-by-step registration path on 大众点评开放平台, credential requirements, realistic review timelines, and the recommendation to start the application now because of the multi-week queue.
- **ADR-037 through ADR-042 added** (`DESIGN.md`): intent routing, preference profile, tool-calling loop, rich data model, two-key Amap split, and navigation restructure decisions.
- **Security constraints reaffirmed**: all keys (`DEEPSEEK_API_KEY`, `DASHSCOPE_API_KEY`, `AMAP_API_KEY`, future `DIANPING_APP_KEY`/`DIANPING_APP_SECRET`) stay server-side only; mock/static fallback is never removed; `NEXT_PUBLIC_AMAP_MAPS_KEY` is the only new public key and is domain-whitelisted.

## v0.1.44 - 2026-07-01

- Moved the 6-tab navigation to a **fixed bottom bar** on mobile (`position: fixed; bottom: 0`), so thumbs can reach tabs without stretching to the top of the screen. The header now shows only the brand mark, language switcher, and account icon on mobile.
- Converted the day detail drawer from a narrow right-side panel (`min(430px, 34vw)` = ~133px on a 390px phone) into a **full-width bottom sheet** (80dvh, slides up from the bottom with rounded top corners and a drag-handle hint).
- Constrained the **account menu popover** to `calc(100vw - 28px)` on mobile, preventing it from overflowing the left edge of narrow screens.
- Made **explore city filter pills** scroll horizontally on mobile instead of wrapping into multiple rows.
- Forced **explore POI columns** (Attractions / Food / Stays) to a single column on mobile instead of the auto-fit multi-column grid.
- **Trip detail header** now stacks vertically on mobile (title above action links) instead of trying to fit both on one row.
- **Trip summary actions** (compact controls inside the Live Trip Canvas on Trip Detail) switch to left-aligned and wrap freely on mobile.
- Enlarged **tool card** touch targets on mobile (`min-height: 52px`).
- Added `padding-bottom: calc(64px + env(safe-area-inset-bottom))` to `.app-shell` so page content is never hidden behind the fixed bottom nav on any screen, including notch devices.
- Removed the circular border from nav icons inside the bottom bar; added a 2px active-state line at the top of the active tab for visual feedback.
- Shrunk brand mark logo and title text slightly on mobile to free header height.

## v0.1.43 - 2026-07-01

- Fixed `/api/translate/text` so text translation no longer fails outright when the Qwen/DashScope route is unavailable; the route now falls back to the existing DeepSeek server-side provider before returning a provider-unavailable error.
- Updated the unified Translator UI to show a clearer configuration message when neither Qwen nor DeepSeek translation providers are available.
- Changed Trip Canvas day cards from `Edit` to `View details`; the day drawer is now read-only and shows itinerary details instead of editable form fields.
- Reworked real Trip Detail pages with saved canvases so Continue/Trips/status/archive/share controls live as compact buttons/status copy inside the Live Trip Canvas summary card instead of taking over the top of the page.
- Added tests for Qwen-to-DeepSeek translation fallback, read-only day details, and compact Trip Detail canvas actions.

## v0.1.42 - 2026-07-01

- Simplified `/translate` from four separate cards into one locale-aware translator workspace.
- Translation direction now follows the active website language and Chinese, supporting English, Spanish, Arabic, Japanese, Korean, French, and Chinese through the existing Qwen text route.
- Image translation now exposes two clear actions: Upload Image and Take Photo. Take Photo is visible but disabled on desktop because it is intended for mobile camera capture.
- Voice translation is reduced to one Record button; audio upload and public audio URL controls were removed from the page UI.
- Common phrases and special terms now sit in a horizontal support rail below the two equal source/output text panels.
- Reduced heavy translucent panel styling on Translator so the ink landscape background remains visible, using hairline dividers and very low-opacity paper backing instead.
- Replaced the Account avatar picker artwork with six new panda PNG assets while keeping the existing six avatar IDs stable for localStorage and community compatibility.
- Desktop landscape `/translate` is locked to one viewport with internal overflow only; 1440x900 verification showed no body/html page scroll.
- Updated `/api/translate/text` so non-English target locales are prompted correctly instead of defaulting every non-Chinese target to English.

## v0.1.41 - 2026-07-01

- Fixed "Saving failed" error on Save to Trips in Chat.
- Root cause: `trips.owner_id` FK references `public.users(id)`, but Supabase Auth users only exist in `auth.users` — no `public.users` row → FK violation on every insert.
- Fix 1 (DB): migration `0003_fix_auth_user_sync.sql` adds a trigger that auto-upserts a `public.users` row whenever a new `auth.users` entry is created, plus INSERT/UPDATE RLS policies so existing users can upsert their own row.
- Fix 2 (code): `saveTripCanvas` now upserts the caller's `public.users` row (using `ownerEmail` from the session) before inserting the trip, covering users who signed up before the trigger was applied.
- Fix 3 (code): `appendMessage` failures are now non-fatal (logged as warnings, don't block the save success message).
- Added `console.error` logging in the save catch block for easier diagnosis of future failures.

## v0.1.40 - 2026-07-01

- Tightened the standalone landing home page into a single-viewport desktop layout.
- Reduced hero, header, CTA, and feature-card spacing so the home page reads faster without pushing key content below the fold.
- Made the six feature cards more compact while preserving the route entry points for Chat, Trips, Explore, Tools, Translate, and Community.
- Preserved the v0.1.38 home-page structure, brand mark behavior, LanguageSwitcher, and Account menu entry.

## v0.1.39 - 2026-07-01

- Replaced background image with new golden-line Chinese landscape (mountains, pagodas, traditional buildings on warm cream paper).
- Reduced background gradient overlay opacity across all body and destination-scene variants so the new line-art image shows clearly (was ~0.58, now ~0.16–0.18).
- Day drawer panel overlay also lightened to match.

## v0.1.38 - 2026-06-30

- New landing home page (`/`) replaces the redirect-to-chat default — brand hero with "Start Planning →" CTA and 6 feature cards (Chat, Trips, Explore, Tools, Translate, Community).
- Home page is standalone (no AppShell/NavTabs); has its own minimal top bar with LanguageSwitcher and AccountMenu.
- Brand-mark in AppShell now links to `/` instead of `/chat`.
- Compact interior page headers: section kicker/h1/subtitle made tighter across Trips, Explore, Tools, Community, and Translator via CSS overrides.
- Feature cards on home page each show icon, translated nav label, and one-line description; responsive 3→2→1 column grid.

## v0.1.37 - 2026-06-30

- Added multi-language (i18n) system supporting English, Spanish, Arabic, Japanese, Korean, and French.
- Language preference stored in localStorage (`visepanda:locale`); persists across sessions.
- Custom React context (`I18nProvider`) and `useTranslation()` hook — no external library required.
- Locale bundles dynamically imported per language (only the active locale is loaded).
- Arabic (`ar`) sets `document.documentElement.dir="rtl"` automatically; all other locales use LTR.
- `[dir="rtl"]` CSS overrides for Arabic: card accent borders, day notes, tool modal tips, explore add-note, and phrase-book notes flip to the right side.
- `LanguageSwitcher` component added to the app header (left of the account menu) — compact EN/ES/AR/JA/KO/FR selector.
- Navigation labels (Chat, Trips, Explore, Tools, Translate, Community) translated in all 6 locales.
- Trips Dashboard: heading, filter pills, card labels, status copy, empty state all translated.
- Tools Board: section heading, "Open checklist →", badge labels, "Close", "Offline pocket notes" all translated.
- Explore Board: section heading, column headers (Attractions/Food/Stays), "Add to Trip" translated.
- Community Board: section heading, tab labels (Feed/Hot Spots/Photos) translated.
- Translator Page: section heading and subtitle translated.

## v0.1.36 - 2026-06-30

- Mobile layout overhaul: all pages now scroll naturally on phones (390px viewport).
- Nav bar becomes a 6-icon bottom strip on mobile, fitting all tabs including Community.
- Translator switches to single-column stack on mobile (fixed CSS cascade bug where desktop 2×2 grid overrode the mobile rule).
- Day block thumbnail placeholder hidden on mobile (title already shown in block text).
- Trip status guide hidden on mobile to bring trip cards immediately into view; trip meta table hidden to reduce card height.
- Trip filter pills horizontally scrollable on mobile (no more wrapping).
- Community membership strip horizontally scrollable on mobile (full tier names visible).
- Community board body and all page containers flow with natural height on mobile (no more overflow:hidden clipping).
- Trips summary stats remain 3-column on mobile.
- Added late-cascade `@media (max-width: 760px)` block at end of globals.css to override warm visual system styles that weren't guarded by a min-width breakpoint.

## v0.1.35 - 2026-06-30

- English-only UI pass: all Chinese-only UI labels replaced with English across Tools badges, Community HotSpots filters and actions, Community Photos default emoji, mock trip highlights, and mock butler itinerary attraction names.
- All attraction and place names in mock data now use "English Name (中文名)" bilingual format (e.g. "Forbidden City (故宫)", "The Bund (外滩)").
- Tool card badges changed to English: Required, Pre-trip, Live, Transit, Connectivity, Emergency.

## v0.1.34 - 2026-06-30

- Redesigned Tools page with 6 modal-overlay card dialogs: each tool now appears as a themed card (icon, accent color) in a 3×2 grid; clicking opens a floating modal with tips, checklist sections, and offline notes. ESC and backdrop click close the modal. URL deep links (`?category=<id>`) preserved.
- Fixed Trips dashboard filter button visibility: ALL/DRAFT/READY/SHARED/ARCHIVED filter pills are now always visible above the scrollable trip library, with the status guide moved inside the scroll area. Grid reduced from 5 rows to 4.
- Redesigned Translator as a single-page 2×2 grid: Text (top-left), OCR (top-right), Voice (bottom-left), Phrases (bottom-right) are all visible simultaneously — no tab navigation. Each panel scrolls independently.
- Updated CSS throughout: `.tools-grid` (3×2 card grid), `.tool-card` with per-card accent theming, `.tool-modal-overlay` + `.tool-modal`, `.translator-grid` (2×2 panel grid), `.translator-grid__panel`, and responsive overrides for mobile.
- Updated `tests/tools-board.test.tsx` to mock the static provider directly and updated `tests/translator-page.test.tsx` for the single-page layout.

## v0.1.33 - 2026-06-30

- Added a desktop-first visual layout refresh across Chat, Trips, Explore, Tools, Translate, Community, and Account.
- Tightened the global shell, header, navigation, page titles, card rhythm, and one-page desktop workspace behavior.
- Added a Warm New Chinese visual-system override with warmer paper tones, ink-line dividers, smaller serif headings, compact cards, and unified paper-style inputs.
- Rebalanced Chat so the Live Trip Canvas title is smaller, the right prompt chips stay in two compact rows, the composer/save/status controls no longer overlap, and the page stays locked to one viewport.
- Improved Trips, Explore, Tools, Translate, and Community content areas so headers and filters take less vertical space while long content scrolls inside each page.
- Cleaned visible mojibake from Translator and Community page-level labels, and normalized Translator Text/OCR/Voice controls to stable English labels without changing the Qwen API flow.
- Added `docs/superpowers/specs/2026-06-30-visual-layout-refresh-design.md` to record the approved visual refresh direction.

## v0.1.32 - 2026-06-30

- Removed the old Translate category from `/tools`; translation now remains a dedicated `/translate` main navigation page.
- Converted Tools categories into compact name-only cards. Details stay hidden until a category card is opened, and URL deep links such as `/tools?category=currency` still open the matching drawer.
- Removed Tools provider implementation/status metadata from the page UI, including live-provider labels, coverage copy, next-integration copy, and candidate API-source strings.
- Removed the visible Tools API-priority block from category drawers so users see travel checklists rather than implementation planning text.
- Updated the retained `ButlerReminders` helper so `language` alerts route to `/translate` instead of the removed `/tools?category=translate` category.
- Updated Tools tests to cover the six-category provider, card-first drawer interaction, Translate removal, and hidden provider metadata.

## v0.1.31 - 2026-06-30

- Upgraded the Community page from static mock display to a local interactive MVP.
- Feed now supports local post publishing, type/city filters, likes, saves, comments, read-more detail, and localStorage persistence.
- Photos now supports local photo-card publishing and local likes; real image upload remains planned for Supabase Storage.
- Added a five-level membership system: Bamboo Guest, Panda Explorer, Silk Road Insider, Dragon Pass, and VisePanda Concierge.
- Community authors now show panda avatars and membership badges.
- Added six bundled panda avatar SVG assets and a reusable avatar registry in `lib/account/avatars.ts`.
- Account trigger now displays the selected panda avatar, and the Account popover lets users choose a panda avatar stored in localStorage.
- Added tests for Community MVP interactions and Account avatar selection.

## v0.1.30 - 2026-06-30

- Upgraded the Translator stack to **Aliyun Bailian Qwen** across text translation, OCR, TTS, and STT.
- `/api/translate/text` now uses `qwen-mt-flash` via the DashScope OpenAI-compatible chat completions endpoint, returning translation and optional pinyin JSON.
- `/api/translate/ocr` now uses `qwen3.5-ocr` via the DashScope OpenAI-compatible multimodal endpoint; OCR.space is no longer used.
- Added `/api/translate/tts`, using `qwen3-tts-instruct-flash` through the DashScope multimodal generation endpoint and returning a temporary Qwen audio URL.
- Added `/api/translate/stt`, using `qwen3-asr-flash` with `input_audio` through the DashScope OpenAI-compatible chat completions endpoint.
- Added a **Voice** tab to `/translate` for recording, uploading audio, or pasting a public audio URL; recognized speech is automatically sent through the text translation route.
- Replaced browser `speechSynthesis` in Text, OCR, and Phrase Book with server-side Qwen TTS requests so API keys remain server-only.
- Added `lib/aliyun/qwen.ts` as the shared Bailian/Qwen helper and updated environment placeholders for `DASHSCOPE_API_KEY`, endpoint overrides, and optional model overrides.
- Added tests for the Qwen translator API routes and the Voice tab; full Vitest suite now covers 30 files and 69 tests.

## v0.1.29 - 2026-06-30

- Added **Community page** (`/community`) as the 6th main navigation tab (Globe icon), implementing the Phase 11 framework.
- Three-tab layout: **动态 Feed** (shared trip posts and tips), **热门 Hot Spots** (city-level attraction/food/hidden-gem rankings), **照片 Photos** (photo wall grid).
- `CommunityFeed`: 6 mock posts (trips + tips) with author avatars, city tags, excerpts, hashtags, like/comment counts, and a "Share My Trip" CTA placeholder.
- `CommunityHotSpots`: 12 community-rated hot spots across 5 cities (Beijing, Shanghai, Chengdu, Xi'an, Hangzhou) with star ratings, review counts, traveler tips, and "Add to Trip" buttons (routes to `/chat?add=…` via existing AI pipeline).
- `CommunityPhotos`: 8 mock photo cards with emoji covers, location labels, captions, and like counts; upload CTA placeholder for future Supabase Storage integration.
- Static data in `lib/community/types.ts` and `lib/community/mockData.ts`; no real API yet — community Supabase tables (`posts`, `photos`, `likes`) and high-de/美团 API integration planned for Phase 11.
- Added CSS classes for `.community-board`, `.community-tabs`, `.community-feed`, `.community-post-card`, `.community-photos`, `.community-photo-card`, `.community-hotspots`, `.community-hotspot-card`.
- Updated `tests/nav-tabs.test.tsx` to 6 tabs/SVGs. All 64 tests pass.

## v0.1.28 - 2026-06-30

- Added a full-featured **Translator page** (`/translate`) as the fifth main navigation tab (Languages icon).
- **Text translation** (`components/translate/TextTranslator.tsx`): bidirectional EN↔ZH text translation via `/api/translate/text` (DeepSeek server proxy, `DEEPSEEK_API_KEY`); returns translation + pinyin; supports Web Speech API TTS (zh-CN, rate 0.85) and clipboard copy; Ctrl+Enter shortcut.
- **OCR scan translation** (`components/translate/OcrTranslator.tsx`): upload or camera-capture image → client-side Canvas API resize (max 1200 px) → `/api/translate/ocr` (OCR.space, `OCR_SPACE_API_KEY` or free "helloworld" key, language `chs`, engine 2) → auto-translates recognized Chinese text to English; TTS for Chinese output; drag-and-drop zone.
- **Phrase book** (`components/translate/PhraseBook.tsx`): 44 static phrases across 6 categories (Greetings, Dining, Transport, Shopping, Emergency, Hotel) and 28 special terms across 3 categories (Attractions, Dishes, Signs); each item shows Chinese, pinyin, English, optional notes/context, and a TTS speak button.
- New server routes: `app/api/translate/text/route.ts` and `app/api/translate/ocr/route.ts`; both keys are server-only, never exposed to the browser.
- Static phrase data in `lib/translate/phrases.ts` and `lib/translate/types.ts`.
- `ToolsBoard` Translate category gains a data-driven CTA link (`/translate`) via new optional `cta?: { label; href }` field on `ToolCategory`.
- Removed `ButlerReminders` rendering from `TripCanvas` (component file kept); updated canvas tests accordingly.
- Added CSS classes for `.translator-page`, `.translator-tabs`, `.text-translator`, `.ocr-translator`, `.phrase-book`, `.tools-category-cta`.
- Community page planning added to PLAN.md (Phase 11), PRD.md, DESIGN.md, AGENTS.md, and HANDOFF.md — no implementation yet.
- Added `tests/translate-api.test.ts` and `tests/phrase-book.test.tsx`; updated `tests/nav-tabs.test.tsx` to 5 tabs/SVGs and `tests/canvas-components.test.tsx` to remove ButlerReminders assertions. All 64 tests pass.

## v0.1.27 - 2026-06-30

- Connected real-time exchange-rate data via ExchangeRate-API: new `/api/exchange-rate` server-side route fetches CNY-base rates hourly using `EXCHANGE_RATE_API_KEY` (env var, never exposed to browser); new `lib/tools/liveToolsProvider.ts` wraps the static provider and injects live rate rows into the `currency` category when the API is reachable, falling back to static text when it is not.
- Connected Amap live POI data for Explore: new `/api/explore/amap` server-side route fetches tourist attractions (`types=110000`), restaurants (`050000`), and hotels (`100000`) per city using `AMAP_API_KEY`; new `lib/explore/amapProvider.ts` wraps the static provider and falls back city-by-city to static data when the API is unreachable.
- `lib/tools/index.ts` now returns `createLiveToolsProvider()` instead of `createStaticToolsProvider()`; `lib/explore/index.ts` now returns `createAmapExploreProvider()` instead of `createStaticExploreProvider()`. Static providers are fully preserved as fallbacks.
- All 53 existing tests continue to pass because live API calls fail gracefully (try/catch → null/empty) in the Vitest environment, leaving static data as the result.

## v0.1.26 - 2026-06-30

- Added `ButlerReminders` — a lightweight alert list rendered below the day timeline in `TripCanvas` (not at the top, not the removed five-card grid).
- Each `ButlerAlert` maps by `type` to a Tools category id (`visa`→`visa-and-entry`, `payment`→`payment-setup`, `language`→`translate`, `transport`→`metro`, `risk`/`emergency`→`emergency`) and renders as an `<a href="/tools?category=<id>">` link; unmapped types (`booking`, `weather`) render as plain text.
- `CanvasTaskStrip.tsx` remains unused — not restored, per the existing constraint against the removed top task-card grid.
- Added `tests/butler-reminders.test.tsx` covering mapped/unmapped alert rendering and updated `tests/canvas-components.test.tsx` since reminders are now intentionally rendered with working deep links.

## v0.1.25 - 2026-06-30

- Added Tools provider readiness metadata so the Tools layer documents static mode, coverage, candidate live data sources, limitations, and the first integration priority.
- `ToolsBoard` now renders the provider status alongside category content without hardcoding provider-specific copy in the component.
- Added provider status tests for the Tools abstraction.

## v0.1.24 - 2026-06-30

- Added Explore provider readiness metadata for static mode, current coverage, candidate data providers, limitations, and the next integration target.
- `ExploreBoard` now shows the provider status so users and future agents can see that the page is static today and prepared for POI/place-detail provider validation.
- Added provider status tests for the Explore abstraction.

## v0.1.23 - 2026-06-30

- Added destination-aware background scene selection for the Trip Canvas route.
- `TripCanvas` now syncs the active destination scene to the document body, and CSS switches the ink-wash atmosphere for Beijing, Shanghai/Jiangnan, Hangzhou/Suzhou, Chongqing, or the default China ink landscape.
- Added tests for the destination-to-scene mapper.

## v0.1.22 - 2026-06-30

- Implemented the Tools practicalization pass: every static Tools category now has structured sections, offline pocket notes, and an API priority note.
- `lib/tools/types.ts` and `lib/tools/staticProvider.ts` now model `sections`, `offlineTips`, and `apiPriority` so future real providers can fill the same fields without changing `ToolsBoard`.
- `components/tools/ToolsBoard.tsx` renders category tips, grouped checklist sections, offline-readable notes, and the next API integration priority.
- Expanded Tools tests to require structured sections, offline content, and API priority metadata.

## v0.1.21 - 2026-06-30

- Expanded Explore static data with Guangzhou, Hangzhou, Suzhou, and Chongqing across city summaries, attractions, food, and stays.
- Updated Explore Add to Trip messaging so every item sends the user back to Chat with a request for VisePanda to rebalance the route around the selected place.
- Added a visible note explaining that Add to Trip reopens Chat and updates the canvas through the AI planning pipeline.
- Expanded Explore provider and board tests for the new cities and rebalanced Add to Trip message.

## v0.1.20 - 2026-06-30

- Added a Trips status guide that explains Draft, Ready, Shared, and Archived states in plain language.
- Trip Detail now shows the current status meaning and next recommended action for both real Supabase-backed trips and example trips.
- Shared status copy now lives in `lib/trips/mockTrips.ts` so dashboard cards and detail pages use the same language.
- Expanded Trips Dashboard and Trip Detail tests for the status guide behavior.

## v0.1.19 - 2026-06-30

- Implemented task 5.2: Tools category deep links.
- `components/tools/ToolsBoard.tsx` now reads `/tools?category=<tool-category-id>` on mount and opens the matching category, falling back to the default category for invalid values.
- Category clicks now update the URL with `history.replaceState`, making selected Tools categories copyable and reusable from future Chat/Canvas reminder entry points.
- Added a subtle active background highlight for the selected Tools category.
- Expanded `tests/tools-board.test.tsx` to cover URL-selected categories and invalid-category fallback.
- Replaced the letter placeholders in the top navigation with Lucide icons for Chat, Trips, Explore, and Tools.
- Tightened desktop page density across Chat, Trips, Explore, and Tools: smaller headers, slimmer summary cards, tighter spacing, and internal scroll containers so the page itself stays locked to one viewport.
- Added `tests/nav-tabs.test.tsx` to cover the icon-backed primary navigation.

## v0.1.18 - 2026-06-29

- Implemented task 5.1: upgraded `/tools` from a placeholder into a real static-provider-driven skeleton covering 7 categories — Visa and entry, Payment setup, Translate, Currency, Metro, eSIM/VPN, and Emergency.
- Added `lib/tools/types.ts` (`ToolsProvider` interface, `ToolCategory` type), `lib/tools/staticProvider.ts` (static reference checklists per category), and `lib/tools/index.ts` (`getToolsProvider()` factory — the only entry point components may call).
- Added `components/tools/ToolsBoard.tsx` and replaced the Tools placeholder page with it: a category list and a detail panel showing that category's summary and tips.
- Currency/Translate copy explicitly states that live exchange-rate conversion and machine translation aren't wired up yet, to avoid implying real-time data.
- Added `tests/tools-provider.test.ts` and `tests/tools-board.test.tsx` covering the static provider's category list and the board's category-switch interaction.

## v0.1.17 - 2026-06-29

- Implemented task 4.4: Explore "Add to Trip" flow.
- `components/explore/ExploreBoard.tsx`: every attraction/food/stay item now has an "Add to Trip" button that navigates to `/chat?add=<encoded draft message>` (e.g. "Add Forbidden City in Beijing to my trip.").
- `components/chat/ButlerWorkspace.tsx`: added a one-time mount effect that reads the `add` URL param, clears it via `history.replaceState`, and calls the existing `handleSend` so the new content always goes through `/api/chat` → `CanvasPatch` → `applyCanvasPatch`, never a direct UI-side canvas write.
- Added `.explore-add-button` styles in `app/globals.css`.
- Added a navigation test in `tests/explore-board.test.tsx` and an auto-send test in `tests/chat-workspace.test.tsx`.

## v0.1.16 - 2026-06-29

- Implemented task 2.5: replaced the standalone `/account` page with a header icon + popover, and switched login from magic link to email/password and Google OAuth.
- Removed `app/account/page.tsx` and `components/account/AccountPanel.tsx`; removed `"account"` from `NavTabs`'s `AppTab` union and tab list.
- Added `components/account/AccountMenu.tsx`: a header icon button that toggles a popover. Shows guest messaging when Supabase isn't configured, a sign-in/sign-up email+password form plus a "Continue with Google" button when signed out, and Change name / Change password / Log out actions when signed in. Mounted in `AppShell`'s header next to `NavTabs`.
- `lib/supabase/auth.ts`: removed `signInWithMagicLink`; added `signInWithPassword`, `signUpWithPassword`, `signInWithGoogle`, `updateDisplayName`, `updatePassword`.
- Added `.account-menu*` styles in `app/globals.css` for the trigger button and popover.
- Added `tests/account-menu-guest.test.tsx`, `tests/account-menu-signin.test.tsx`, `tests/account-menu-signedin.test.tsx` covering the unconfigured, signed-out, and signed-in states; removed `tests/account-panel.test.tsx`.

## v0.1.15 - 2026-06-29

- Implemented tasks 4.1 and 4.2: Explore skeleton and provider abstraction.
- Added `lib/explore/types.ts`: `ExploreCity`, `ExploreAttraction`, `ExploreFoodSpot`, `ExploreStay` domain types and the `ExploreProvider` interface.
- Added `lib/explore/staticProvider.ts`: `createStaticExploreProvider()` with static data covering Beijing, Shanghai, Chengdu, and Xi'an.
- Added `lib/explore/index.ts`: `getExploreProvider()` factory — the only entry point components are allowed to call; swapping in a real Amap/Trip.com/Meituan provider later only requires changing this file.
- Added `components/explore/ExploreBoard.tsx` and replaced the Explore placeholder page with it: city filter buttons, a city summary card, and an Attractions/Food/Stays column layout that reloads when the active city changes.
- Added `tests/explore-provider.test.ts` and `tests/explore-board.test.tsx` covering the static provider's filtering behavior and the board's city-switch interaction.

## v0.1.14 - 2026-06-29

- Implemented task 3.5: trip archive state and share links.
- Added `supabase/migrations/0002_trip_archive_and_share.sql`: extends the `trips.status` check constraint to allow `archived`, and adds RLS policies so anyone can read a `trips`/`canvas_versions` row once `share_token` is set.
- Added `updateTripStatus`, `createShareLink`, `revokeShareLink`, and `loadSharedTrip` to `lib/supabase/tripsRepository.ts`.
- `TripDetail` now exposes Mark as Ready / Archive / Restore from archive buttons, plus Get share link / Revoke share link actions with a live status message and the full share URL.
- Added `app/share/[token]/page.tsx` and `components/share/ShareView.tsx`: a public, unauthenticated, read-only page that renders a shared trip's saved canvas without exposing chat history.
- `lib/trips/mockTrips.ts` gained an `archived` status and a fourth example trip; `TripsDashboard` filters now include "Archived".
- Added `tests/trip-detail-actions.test.tsx` and `tests/share-view.test.tsx` covering the new archive/share flows and the public share page.

## v0.1.13 - 2026-06-29

- Implemented task 3.4: trip detail page (`/trips/[id]`).
- Added `components/trips/TripDetail.tsx`: shows the real saved canvas (via `TripCanvas`) for signed-in Supabase-backed trips, falls back to an example-trip summary for mock trips, and shows a not-found notice otherwise.
- Added a "View details" link on each Trips Dashboard card alongside the existing "Continue in Chat" link.
- Added `tests/trip-detail.test.tsx` covering the mock-trip and not-found paths.

## v0.1.12 - 2026-06-29

- Implemented task 2.3: guest draft to logged-in synced trip migration path.
- `ButlerWorkspace` now persists an in-progress guest (not signed in, not yet saved) trip draft to `localStorage` under `visepanda:guest-draft` and restores it on remount.
- When a guest signs in via magic link while a local draft exists, the draft is now automatically saved to Supabase (`saveTripCanvas` + `appendMessage`) without the user needing to click "Save to Trips" again.
- The local draft is cleared once a trip is associated with a signed-in session (either restored from Supabase or freshly saved).
- Added `tests/chat-workspace-guest-sync.test.tsx` covering the auto-save-on-sign-in flow, and a guest-draft persistence/restore test in `tests/chat-workspace.test.tsx`.

## v0.1.11 - 2026-06-29

- Added a Supabase browser client and a `isSupabaseConfigured` guard so missing project keys never crash the app.
- Added magic-link sign-in/sign-out (`lib/supabase/auth.ts`) and a `useSupabaseSession` hook.
- Replaced the Account placeholder with `AccountPanel`: email magic-link form when Supabase is configured, guest-mode messaging when it is not.
- Added `lib/supabase/tripsRepository.ts` with `saveTripCanvas`, `listTripsForOwner`, `loadTripWithCanvas`, and `appendMessage`, all RLS-scoped to the signed-in user.
- Added a "Save to Trips" action in the Chat workspace that writes the current canvas to `trips` + `canvas_versions` and syncs chat history to `messages`.
- Trips Dashboard now loads real saved trips for signed-in users when Supabase is configured, and falls back to the existing mock trips otherwise.
- "Continue in Chat" now passes the saved trip id so the Chat workspace can restore that canvas via `/chat?trip=<id>`.
- No live Supabase project is connected yet; all of this activates once `NEXT_PUBLIC_SUPABASE_URL`, `NEXT_PUBLIC_SUPABASE_ANON_KEY`, and the migration are in place.

## v0.1.10 - 2026-06-29

- Designed the Supabase schema for `users`, `trips`, `canvas_versions`, and `messages` (task 2.2).
- Added `supabase/migrations/0001_init_trip_schema.sql` with table definitions, indexes, foreign keys, and row-level security policies scoped to trip owners.
- Added `lib/supabase/schema.ts` with TypeScript row types matching the migration, reusing `TripState`, `ChatMessage`, and `SavedTripStatus`.
- No live Supabase project is connected yet; this is the schema contract that task 3.3 persistence work will implement against.

## v0.1.9 - 2026-06-29

- Redesigned Live Trip Canvas day cards into a vertical Day 1 / Day 2 / Day 3 timeline.
- Added Morning / Afternoon / Evening blocks directly inside each day card.
- Removed the five top butler task cards from the canvas surface.
- Upgraded the day detail drawer into a local editor for city, day blocks, hotel, transport, and notes.
- Added component coverage for the new editable canvas workflow.

## v0.1.8 - 2026-06-29

- Upgraded `/trips` from a placeholder page into a saved trips dashboard skeleton.
- Added three mock trip cards with route, date, length, traveler, status, highlights, butler task count, and summary copy.
- Added All / Draft / Ready / Shared filters and summary metrics for the currently visible trips.
- Added Continue in Chat links so saved-trip work can return to the AI Butler flow.
- Added Trips Dashboard component tests.

## v0.1.7 - 2026-06-29

- Removed the default demo conversation from the chat page now that live AI is connected.
- Changed suggested prompts to a stable two-column layout instead of a clipped horizontal row.
- Added two context-aware follow-up questions to `/api/chat` responses.
- Updated the chat panel so suggestions refresh after each AI answer.

## v0.1.6 - 2026-06-29

- Added a DeepSeek V4 Flash provider for `/api/chat`.
- Routed chat submissions through the server API so provider keys stay server-side.
- Kept deterministic mock fallback for missing keys, API failures, or invalid model output.
- Updated environment placeholders to use `DEEPSEEK_API_KEY`, `DEEPSEEK_BASE_URL`, and `DEEPSEEK_MODEL`.

## v0.1.5 - 2026-06-29

- Removed day itinerary details from the main Trip Canvas surface.
- Kept each day card to a one-sentence daily summary with a details action.
- Changed the day detail view into a closed-by-default side drawer.
- Fixed the desktop landscape workspace to one viewport with internal scrolling areas.
- Removed the standalone Practical Reminder rail and merged butler reminders into the top task cards.

## v0.1.4 - 2026-06-29

- Replaced the temporary `VP` header mark with the panda icon from the supplied brand manual.
- Kept the current warm New Chinese interface direction instead of applying the full brand manual system.

## v0.1.3 - 2026-06-29

- Reduced the Live Trip Canvas heading so it takes less desktop workspace.
- Changed day cards into one-line itinerary summaries for faster scanning.
- Added a click-through day detail drawer for daily blocks, food, stay, transport, and notes.
- Kept this iteration focused on desktop landscape layout; mobile portrait refinement is deferred.

## v0.1.2 - 2026-06-29

- Restyled the Chat workspace toward the approved open ink-painting concept.
- Removed the large glass-like chat container and shifted the right side to an open conversation rail.
- Added a thin vertical divider between the live trip canvas and the chat rail.
- Added the canvas task strip for visa, payment, booking, pace, and food-focused butler work.
- Updated the trip summary and day cards to feel more integrated with the paper background.
- Recorded the production domain as `go2china.space`.

## v0.1.1 - 2026-06-29

- First working AI Butler Chat MVP skeleton.
- Added the two-column Chat + Live Trip Canvas workspace.
- Added mock canvas patching, trip cards, butler alerts, placeholder tabs, tests, and Vercel-ready structure.

## Versioning Rule

- Default iteration format is `0.1.x`.
- Each product iteration must update `package.json` and this changelog.
- Use a custom version only when the user explicitly provides one.
