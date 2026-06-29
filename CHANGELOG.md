# VisePanda Changelog

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
