# VisePanda Changelog

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
