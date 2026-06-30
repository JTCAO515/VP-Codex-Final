# VisePanda Versioning

## Current Versions

- Previous baseline: `v0.1.1`
- Current iteration: `v0.1.22`
- Production domain: `go2china.space`

## Rule

- Default product iteration format is `0.1.x`.
- Every iteration must update `package.json` and `CHANGELOG.md`.
- Use a custom version only when the user explicitly provides one.

## Release Notes

- `v0.1.1`: first AI Butler Chat MVP skeleton.
- `v0.1.2`: open ink-painting Chat workspace restyle with integrated canvas cards and a line-separated chat rail.
- `v0.1.3`: desktop-first Trip Canvas density pass with smaller title, one-line day summaries, and a detail drawer.
- `v0.1.4`: brand manual logo borrowing pass; header mark replaced with the panda icon while preserving the current visual direction.
- `v0.1.5`: fixed desktop landscape workspace; day cards show only one-sentence summaries, details open in a side drawer, and butler reminders live in the top task cards.
- `v0.1.6`: connected DeepSeek V4 Flash through the server chat route with mock fallback and server-only key handling.
- `v0.1.7`: removed the demo opening conversation, changed suggested prompts to a two-column layout, and returns two context-aware follow-up questions after each AI reply.
- `v0.1.8`: upgraded Trips from a placeholder into a desktop-first saved trips dashboard skeleton with mock trip cards, status filters, summary metrics, and Continue in Chat links.
- `v0.1.9`: redesigned Live Trip Canvas as a vertical day timeline with Morning/Afternoon/Evening blocks, removed the five butler task cards, and made each day editable through the detail drawer.
- `v0.1.10`: designed the Supabase schema (`users`, `trips`, `canvas_versions`, `messages`) with RLS policies and a matching TypeScript contract, ahead of real persistence work.
- `v0.1.11`: connected real Supabase magic-link auth and a first persistence loop — Save to Trips from Chat, real trip listing in Trips Dashboard, and Continue in Chat restoring a saved canvas — all degrading gracefully until a live Supabase project is configured.
- `v0.1.12`: implemented the guest draft to logged-in synced trip migration path (task 2.3) — guest chat drafts persist to `localStorage` and auto-save to Supabase the moment a user signs in.
- `v0.1.13`: implemented the trip detail page (task 3.4) — `/trips/[id]` shows the real saved canvas for signed-in Supabase trips or an example-trip summary otherwise, with a new "View details" entry from the Trips Dashboard.
- `v0.1.14`: implemented trip archive state and share links (task 3.5) — Trip Detail gained Mark as Ready/Archive/Restore actions plus Get share link/Revoke share link, backed by a new `0002_trip_archive_and_share.sql` migration and a public read-only `/share/[token]` page.
- `v0.1.15`: implemented the Explore skeleton and provider abstraction (tasks 4.1, 4.2) — `lib/explore` defines an `ExploreProvider` interface with a static implementation covering Beijing/Shanghai/Chengdu/Xi'an, and `ExploreBoard` replaces the Explore placeholder with city filters and Attractions/Food/Stays columns.
- `v0.1.16`: replaced the standalone `/account` page with a header icon + popover (task 2.5) — `AccountMenu` handles email/password sign-in/sign-up, Google OAuth sign-in, and post-login change-name/change-password/log-out, all without leaving the current page.
- `v0.1.17`: implemented the Explore "Add to Trip" flow (task 4.4) — every Explore attraction/food/stay item has an Add to Trip button that navigates to `/chat?add=<draft message>`, and `ButlerWorkspace` auto-sends that draft through the existing `/api/chat` → `CanvasPatch` pipeline on mount, keeping all canvas writes flowing through the AI pipeline.
- `v0.1.18`: upgraded `/tools` from a placeholder into a static-provider-driven skeleton (task 5.1) covering Visa and entry, Payment setup, Translate, Currency, Metro, eSIM/VPN, and Emergency, following the same provider-abstraction pattern as Explore.
- `v0.1.19`: implemented Tools category deep links (task 5.2), added icon-backed top navigation for Chat/Trips/Explore/Tools, and tightened desktop page density so Chat, Trips, Explore, and Tools stay locked to one viewport with internal scrolling.
- `v0.1.20`: improved Trips status clarity with a shared Draft/Ready/Shared/Archived guide on the dashboard and matching status guidance on Trip Detail.
- `v0.1.21`: expanded Explore static coverage to Guangzhou, Hangzhou, Suzhou, and Chongqing, and clarified that Add to Trip reopens Chat for AI route rebalancing.
- `v0.1.22`: practicalized Tools with structured checklist sections, offline pocket notes, and API priority metadata for every category.
