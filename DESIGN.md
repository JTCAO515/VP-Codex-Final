# VisePanda Design Reference

Last updated: 2026-06-29 (rev. 2 — supersedes the rev. 1 "dark mask on full-bleed mountain" treatment below the divider)
Scope: Phase 1 (App shell with Chat/Trips/Explore/Tools/Account routes; Chat is the only fully built page)

This is the living style reference. The original decision record (with options considered and rejected) lives in `docs/superpowers/specs/2026-06-29-mvp-trip-canvas-design.md` — read that for the *first-round* reasoning. This file reflects the **rev. 2** direction, locked from a concrete reference mockup the user supplied on 2026-06-29, which is more specific than the CSS-mockup options from the first brainstorming round and takes precedence wherever the two disagree.

## Rev. 2 Visual Direction (current)

The reference mockup resolves the "text directly on the ink-wash background" brief differently than the rev. 1 dark-mask approach: the painted mountains live in a **decorative side margin**, not full-bleed behind every line of text. The actual content area is a flat, light cream surface. Cards don't float as glass/blur panels and don't use a dark gradient mask — they sit at almost the same tone as the page and are separated by **thin hairline borders and divider lines**, so they read as "part of the same paper" rather than "panels stacked on top of an image." Text is dark ink on light paper, not light text on a dark mask.

### Page chrome

- **Top nav** (`components/TopNav.tsx`, rendered once in `app/layout.tsx` for every route): logo mark + "VisePanda" wordmark + "AI China Travel Butler" tagline on the left; five nav items — Chat, Trips, Explore, Tools, Account — right of center. Active item gets a `2px` cinnabar underline. Only **Chat** (`/`) is a fully built page in Phase 1; Trips/Explore/Tools/Account (`/trips`, `/explore`, `/tools`, `/account`) are real routes that render a shared "coming soon" placeholder — they are not decorative dead links, but they are not feature pages yet either.
- **Side ornament**: a fixed decorative ink-wash strip (mountains, a boat, drifting birds — see `components/SideOrnament.tsx`) anchored to the left edge of the viewport, ~160px wide, fading to transparent at its inner edge so it never overlaps the content column. A fainter mirrored strip on the right edge is optional polish, not required. This replaces rev. 1's full-viewport `BackgroundLayer` — the painting frames the page, it does not sit behind the text.
- **Content column**: max-width centered column with a flat `ink-cream` background, no texture, sitting visually on top of/between the side ornaments.

### Cards and dividers

- **Card surface**: `bg-ink-paper` (`#f7f0e2`, barely distinguishable from the page's `ink-cream` `#f5ecdd`) with `border border-ink-umber/15` and `rounded-lg` (8–10px). No drop shadow beyond a barely-visible `shadow-sm`. This is what "merge into the background" means here: low contrast between card and page, definition comes from the hairline border, not from elevation.
- **Left/right division** (Trip Canvas vs. Chat on desktop): a single `border-l border-ink-umber/15` vertical line. No background-color difference between the two halves.
- **Butler Rails**: small bordered cards in a horizontal row (icon + title + one-line detail + a status tag at the bottom edge, e.g. "Not started" / "In progress" / "Optimizing"), not a dark-mask strip. Status tag color follows severity (`info` → `ink-umber/60` muted gray-brown text, `warning` → `ink-gold`, `urgent` → `ink-cinnabar`).
- **Day timeline**: a vertical line runs down the left edge of the Day card list; each `DayCard` has a circular day-number badge (`bg-ink-cinnabar text-ink-paper rounded-full`, ~40px) overlapping the line at its top-left corner. Inside each card, activities render as up to three time-period blocks (Morning/Afternoon/Evening), each with a small placeholder image tile (a flat-color rectangle with a location-pin glyph and the `imageHint` label — there is no real photo pipeline in Phase 1) and a one-line title. A compact footer row under the blocks shows hotel/transport/pace/budget as icon+text pairs, plus "Map" / "Notes" links (both visual-only in Phase 1 — no real map or notes feature yet).
- **Trip summary card**: sits above the Day list inside Trip Canvas. Shows a small static map-thumbnail placeholder (flat illustration, not a real map render), the route as `City → City → ...`, a date range, traveler count, day count, and an "Edit" affordance. "Edit" focuses the chat input (`<input>`'s `.focus()`) rather than opening a separate editor — there is no trip-editing UI in Phase 1, the chat *is* the editor.

### Chat panel

- Messages: no bubble shape for either role. User messages right-aligned, AI messages left-aligned, both on the plain card-less content column — differentiate by alignment and a subtle `text-ink-umber/70` vs `text-ink-umber` weight, not by background.
- **Quick-reply chips** under the message list, above the input: `rounded-full border border-ink-cinnabar/40 text-ink-cinnabar text-sm px-3 py-1`, one per common follow-up ("Less tiring", "Food-focused", "Adjust pace", "Add a day"). Clicking a chip calls the same `sendUserMessage` used by the text input, with the chip's label as the message text — these are real, not decorative.
- **Input row**: text input + a disabled-looking attachment (paperclip) icon + a disabled-looking image icon, both non-interactive placeholders for a future upload feature (`title="Coming soon"`, no `onClick`), + a solid `ink-cinnabar` Send button.

## Color Tokens (`tailwind.config.ts`)

| Token | Hex | Use |
|---|---|---|
| `ink-cream` | `#f5ecdd` | Page background |
| `ink-paper` | `#f7f0e2` | Card surfaces (near-identical to page bg by design) |
| `ink-umber` | `#6e5634` | Primary text color, borders at low opacity (`/15`), muted secondary text (`/60`–`/70`) |
| `ink-ochre` | `#9c7d54` | Side-ornament mountain silhouette (mid layer) |
| `ink-cinnabar` | `#a23728` | Brand accent: active nav underline, day badges, Send button, quick-reply chip borders/text, urgent status tags |
| `ink-gold` | `#b8862c` | Secondary accent: warning-level status tags |

There is no more "light text on dark mask" — text is `ink-umber` (dark) on `ink-cream`/`ink-paper` (light) everywhere in Phase 1.

## Typography

- Display/heading font: `Caveat` (Google Font via `next/font/google`), Tailwind `font-display`. Used for the wordmark, "Live Trip Canvas" section title, and Day headers.
- Body font: system UI stack, Tailwind `font-body`. Used for everything else — chosen for guaranteed legibility with mixed English/Chinese place names over decorative flourish.

## Component Inventory (Phase 1, rev. 2)

| Component | File | Responsibility |
|---|---|---|
| `TopNav` | `components/TopNav.tsx` | Shared header: wordmark + 5-item nav with active-route underline |
| `SideOrnament` | `components/SideOrnament.tsx` | Fixed decorative ink-wash side strip, no props (replaces rev. 1's `BackgroundLayer`) |
| `ButlerRails` | `components/ButlerRails.tsx` | Reads `rails` from the store, renders the bordered-card reminder row |
| `TripSummaryCard` | `components/TripSummaryCard.tsx` | Reads `summary` from the store, renders route/dates/travelers/days + Edit |
| `TripCanvas` | `components/TripCanvas.tsx` | Renders `TripSummaryCard` + the Day timeline (or empty state) |
| `DayCard` | `components/DayCard.tsx` | Pure presentational render of one `DayCard`, including the time-period blocks |
| `ChatPanel` | `components/ChatPanel.tsx` | Message list + quick-reply chips + input row, calls `sendUserMessage` |
| `ChatMessage` | `components/ChatMessage.tsx` | Pure presentational render of one `ChatMessage` |
| `MobileCanvasSheet` | `components/MobileCanvasSheet.tsx` | Full-screen mobile-only wrapper around `TripCanvas` |
| `Workspace` | `components/Workspace.tsx` | Chat-route layout switch (desktop split vs. mobile stacked) |

`BackgroundLayer` from rev. 1 is removed in favor of `SideOrnament` — if you find a reference to `BackgroundLayer` anywhere, it's stale.

## Deferred Visual Work (Phase 2+)

- Real map render for the Trip Summary card's thumbnail (currently a flat placeholder illustration).
- Real photos for Day card activity blocks (currently flat-color placeholder tiles with a label).
- Per-city/scene variation of the side ornament art (confirmed direction, no art pipeline yet).
- Real attachment/image upload from the chat input (currently non-interactive placeholder icons).
- Building out Trips/Explore/Tools/Account beyond their shared "coming soon" placeholder.
