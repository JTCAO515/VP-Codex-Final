# VisePanda — Phase 1 MVP

A real 5-route app shell (Chat / Trips / Explore / Tools / Account). Only **Chat**
is a full feature page: a streaming chat (right) drives a live Day-by-day Trip
Canvas with a Trip Summary card (left) and a row of Butler Rails reminders
(top). Trips/Explore/Tools/Account share a "coming soon" placeholder. See
`DESIGN.md` for the current visual system and
`docs/superpowers/specs/2026-06-29-mvp-trip-canvas-design.md` plus
`docs/superpowers/plans/2026-06-29-mvp-trip-canvas-v2.md` for the full design
and build history.

## Running locally

```bash
npm install
npm run dev
```

Visit `http://localhost:3000`. No environment variables are required — without
`DEEPSEEK_API_KEY` set, `/api/chat` serves deterministic mock replies so the
full interaction loop works out of the box. Copy `.env.example` to `.env.local`
and set `DEEPSEEK_API_KEY` to use real DeepSeek streaming instead.

## Tests

```bash
npm run test
```

Runs Vitest against the logic-only modules: the trip store reducers (including
the Trip Summary merge), the trip-instruction parser, the mock AI engine, and
the streaming helpers.

## Manual verification script

1. Start the app with no `DEEPSEEK_API_KEY` set.
2. Send: `I'm coming from the US, first time in China, 5 days, want to go to
   Beijing and Shanghai.`
   Expect: a Trip Summary card appears ("Beijing → Shanghai", 5 days), 5 Day
   cards appear below it alternating Beijing/Shanghai with morning/afternoon/
   evening activity blocks, and Butler Rails for visa, payment setup, and
   intercity transport appear at the top.
3. Send: `I don't want to be too tired, hotels should be convenient.`
   Expect: existing Day cards update in place — fewer activity blocks per day,
   hotel notes mention being near the metro — without the canvas resetting or
   duplicating days.
4. Click each of the four quick-reply chips once on a fresh message and confirm
   each sends that exact text and gets a reply.
5. Click "Edit" on the Trip Summary card.
   Expect (desktop): the chat input gets focus. Expect (mobile, via the
   "View itinerary" sheet): the sheet closes back to the chat screen.
6. Click "Trips", "Explore", "Tools", "Account" in the top nav.
   Expect: each is a real URL change showing the shared "coming soon" message
   with that nav item underlined.
7. Resize the browser below 768px width (or use devtools device toolbar).
   Expect: the layout switches to a full-screen chat with a "View itinerary"
   button bottom-right that opens a full-screen Trip Canvas sheet.
8. Reload the page.
   Expect: messages, Day cards, and the Trip Summary persist (loaded from
   `localStorage`).
