# VisePanda Design Reference

Last updated: 2026-06-29
Scope: Phase 1 (Trip Canvas + Butler Rails workspace)

This is the living style reference. The one-time decision record with the options that were considered and rejected lives in `docs/superpowers/specs/2026-06-29-mvp-trip-canvas-design.md` — read that for *why*, read this for *what to use*.

## Layout

### Desktop (≥768px)

```
┌─────────────────────────────────────────────┐
│  Butler Rails — full width, always visible    │
├───────────────────────────┬───────────────────┤
│  Trip Canvas (flex: 1.4)  │  Chat (flex: 1)   │
└───────────────────────────┴───────────────────┘
```

No visible border/container line between canvas and chat beyond a 1px low-opacity divider (`border-ink-paper/10`). Breakpoint: `768px`, implemented via `hooks/useIsMobile.ts`, not CSS-only media queries, because layout structure (not just styling) changes below the breakpoint.

### Mobile (<768px)

Chat is full-screen and primary. Butler Rails collapse to a horizontally-scrollable strip. A floating "View itinerary" button (bottom-right) opens the Trip Canvas as a full-screen sheet (`components/MobileCanvasSheet.tsx`) with a "Back to chat" control. Canvas and chat are never simultaneously visible on mobile — this was a deliberate choice (chat is the primary interaction; the plan is a confirmatory view), not a fallback.

## Color Tokens (`tailwind.config.ts`)

| Token | Hex | Use |
|---|---|---|
| `ink-cream` | `#f0e6d2` | Background base / mobile sheet background |
| `ink-paper` | `#f7f0e2` | Primary text on dark-masked areas |
| `ink-ochre` | `#9c7d54` | Background mountain silhouette (mid) |
| `ink-umber` | `#6e5634` | Background mountain silhouette (dark), mobile sheet text |
| `ink-cinnabar` | `#a23728` | Primary action color (Send button, floating "View itinerary" button), urgent accents |
| `ink-gold` | `#b8862c` | Reserved accent, not yet used in Phase 1 components |

Background composition: warm gradient (`from-ink-cream via-#e3d3b0 to-#cbb487`) with an SVG mountain silhouette layer at ~60% opacity and a small sun/seal accent circle in cinnabar — this is the "medium ink wash" option approved in brainstorming over a more minimal (90% negative space) and a more decorative (30-40% negative space) alternative. It is intentionally a placeholder for commissioned art (Phase 2) — `components/BackgroundLayer.tsx` takes no props specifically so the swap requires no consumer changes.

## Typography

- Display/heading font: `Caveat` (Google Font, loaded via `next/font/google`), exposed as Tailwind `font-display`. Used for Day headers ("Day 1 · Beijing") and sheet titles.
- Body font: system UI stack (`ui-sans-serif, system-ui, -apple-system, sans-serif`), exposed as `font-body`. Used for all data fields and chat text — chosen over a stylized body font specifically because the product mixes English/Chinese place names and needs guaranteed legibility over decorative flourish.

## The "No Dialog Box" Rule

This is the core visual constraint for Phase 1 and must not be silently dropped when adding new UI:

- **Never** give a text block a solid or semi-transparent rectangular background with a visible edge (no `bg-white/80`, no `backdrop-blur` card, no `border` + `shadow` combo that reads as a floating panel).
- **Always** use a directional gradient mask instead: `bg-gradient-to-{b,r,l} from-black/NN to-transparent`, with text color `ink-paper` (light) sitting directly on top. The opacity (`/30`, `/40`, `/45`, `/60`) is the only thing that varies — use it to signal hierarchy/severity, not different container shapes.
- Distinguish message authorship (user vs. AI in chat) by mask width/opacity/alignment (`ChatMessage.tsx`: user messages are right-aligned, narrower, lighter mask; AI messages are left-aligned, wider, stronger mask) — never by giving one of them a bubble shape the other doesn't have.
- Distinguish Rail severity (`info`/`warning`/`urgent`) the same way: `ButlerRails.tsx`'s `SEVERITY_GRADIENT` map only changes opacity, never shape or border.

If a future component seems to need a hard-edged container to be legible, that's a signal to revisit the background art's contrast in that region first, not to reach for a card background.

## Component Inventory (Phase 1)

| Component | File | Responsibility |
|---|---|---|
| `BackgroundLayer` | `components/BackgroundLayer.tsx` | Fixed full-viewport ink-wash background, no props |
| `ButlerRails` | `components/ButlerRails.tsx` | Reads `rails` from the store, renders the top reminder strip |
| `TripCanvas` | `components/TripCanvas.tsx` | Reads `days` from the store, renders the Day card list or empty state |
| `DayCard` | `components/DayCard.tsx` | Pure presentational render of one `DayCard` |
| `ChatPanel` | `components/ChatPanel.tsx` | Message list + input, calls `sendUserMessage` |
| `ChatMessage` | `components/ChatMessage.tsx` | Pure presentational render of one `ChatMessage` |
| `MobileCanvasSheet` | `components/MobileCanvasSheet.tsx` | Full-screen mobile-only wrapper around `TripCanvas` |
| `Workspace` | `components/Workspace.tsx` | Top-level layout switch (desktop split vs. mobile stacked) |

## Deferred Visual Work (Phase 2+)

- Per-city/scene dynamic background (confirmed direction, no art pipeline yet).
- Any treatment for a `risk`/`urgent` Rail that needs more visual urgency than opacity alone can give — revisit if Phase 1 user testing shows reminders are being missed.
