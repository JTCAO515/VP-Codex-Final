# VisePanda Changelog

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
