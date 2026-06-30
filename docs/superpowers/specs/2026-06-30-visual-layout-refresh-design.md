# VisePanda v0.1.33 Visual Layout Refresh Design

## Goal

Refresh the desktop landscape interface so VisePanda feels more like a warm New Chinese AI travel desk: compact, readable, distinctive, and less like a generic dashboard. This iteration changes visual hierarchy and layout density only. It does not change AI, Supabase, provider, or persistence behavior.

## Design Direction

- Warm paper base with ink-line structure, cinnabar accents, muted gold, and sage details.
- No glassmorphism. Text, cards, and inputs sit directly on the paper/ink background with enough solid paper fill for readability.
- Typography should feel more deliberate: restrained serif for page titles and route/day emphasis, system sans for UI labels, and smaller uppercase/kicker labels for navigation context.
- Desktop horizontal one-page frame remains the priority. Long content scrolls inside page sections instead of making the whole page grow vertically.

## Layout Rules

1. Reduce global chrome height: tighter header, smaller logo, calmer nav spacing.
2. Use a consistent compact header pattern on Chat, Trips, Explore, Tools, Translate, and Community.
3. Increase usable content height by shrinking title, summary, filter, and metadata blocks.
4. Use fine dividers and paper cards with 6-8px radius. Avoid heavy nested card stacks.
5. Unify text fields, textareas, and form controls as solid paper writing areas with clear focus states.
6. Keep mobile usable but defer detailed portrait optimization.

## Page Treatment

### Chat

- Left canvas keeps the live itinerary as the hero workspace.
- `Live Trip Canvas` becomes smaller and less dominant.
- Day cards remain Morning / Afternoon / Evening based, but blocks become denser and more polished.
- Chat rail uses tighter prompt chips, lighter message cards, and a more deliberate composer.

### Trips / Explore / Tools

- Headers become shallow bands.
- Filter rows and category cards use the same pill/card rhythm.
- Main content areas receive more vertical room and internal scrolling.

### Translate / Community / Account

- Translator and Community adopt the same page frame, tabs, and text-field language.
- Visible mojibake in page-level labels is cleaned where it affects the interface.
- Account popover uses the same paper-card and input styling.

## Verification

- Run `npm run test`.
- Run `npm run build`.
- Run `npm run test:e2e` because this changes frontend layout.
- Verify rendered desktop pages with Playwright/browser screenshots.
