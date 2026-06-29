# VisePanda AI Butler Chat Design

## Goal

Build the first-stage product skeleton for VisePanda as an AI China travel butler. The first release focuses on the Chat / Butler workspace: a persistent conversation paired with a live trip canvas that updates as the user talks to the AI. Trips, Explore, Tools, and Account exist only as polished placeholders in this phase.

## Product Positioning

VisePanda is an English-native, mobile-first AI travel butler for foreigners visiting China. It helps users plan and adapt a China trip without learning Chinese apps or asking local contacts for every practical decision.

The product is not a generic chatbot, a static travel blog, or a booking platform. Its first screen should feel like an active planning desk: the user talks to a butler, and the butler turns the conversation into a visible itinerary with practical alerts.

Primary promise:

> You do not need to learn Chinese apps. Ask VisePanda.

## Primary Users

- First-time international visitors who need help with cities, routes, visa readiness, payment setup, local transport, and language friction.
- Independent travelers who want a practical China itinerary without agency overhead.
- Foreign residents, students, and business visitors in China who need short-trip ideas and on-the-ground tools.
- English-speaking planners who need a clear draft itinerary for someone else.

## First-Stage Scope

### In Scope

- A warm New Chinese visual product skeleton.
- Main Chat / AI Butler workspace.
- Desktop split layout: live trip canvas on the left, persistent chat on the right.
- Mobile layout optimized around the trip canvas, with chat available through a fixed input area or bottom panel.
- Mock AI pipeline that updates the canvas with structured itinerary patches.
- Day-by-day trip canvas.
- Butler Rails for practical reminders such as visa, payment, booking, weather, risk, language, and emergency needs.
- Placeholder tabs for Trips, Explore, Tools, and Account.
- Environment variable placeholders for later AI, Supabase, and third-party API integrations.
- Vercel-ready project shape.

### Out of Scope

- Real AI provider integration.
- Real Supabase persistence.
- Real authentication.
- Real Trip.com, Meituan, Amap, weather, translation, or speech integrations.
- Full Explore data browsing.
- Full Trips save/sync behavior.
- Full Tools implementations.
- Admin console.
- Payment or booking checkout.

## Information Architecture

The first-stage navigation contains five sections:

- Chat: the main AI Butler workspace and only fully designed first-stage experience.
- Trips: placeholder for saved itineraries and continuation.
- Explore: placeholder for discovering cities, attractions, dining, hotels, and local experiences. This replaces the older Cities concept.
- Tools: placeholder for translation, payment setup, visa, currency, metro, eSIM/VPN, and emergency tools.
- Account: placeholder for sign-in, sync, saved history, and preferences.

Chat is the default landing screen.

## Main Workspace

### Desktop Layout

The desktop workspace uses two primary regions:

- Left region, about 65 percent width: Trip Canvas.
- Right region, about 35 percent width: AI Butler Chat.

The left side is the product's visual proof of intelligence. It shows the user's evolving trip as structured, editable-looking content rather than a transcript.

The right side is a persistent butler conversation. It should remain visible while the canvas changes, so users understand the cause-and-effect relationship between chat and itinerary updates.

### Mobile Layout

The mobile experience is canvas-first:

- The trip canvas is the main screen.
- A fixed Ask input stays available near the bottom.
- Opening chat shows a focused chat panel or bottom sheet.
- After a user sends a message, the canvas visibly updates.
- Navigation remains present but should not compete with the core canvas and Ask input.

## Trip Canvas

The Trip Canvas is a mixed model: day-by-day itinerary plus butler reminders.

### Day-by-Day Itinerary

The main canvas should show:

- Trip summary: destination set, date or duration, pace, travel style, and current confidence.
- Day cards: Day 1, Day 2, Day 3, and so on.
- Each day includes city, pace, morning/afternoon/evening blocks, food suggestions, accommodation area, transport notes, and a short practical tip.
- Cards should support visual states such as newly added, revised, and needs confirmation.

Example day card content:

- Day 1: Beijing arrival
- Pace: Relaxed
- Morning: Arrive and settle near Wangfujing
- Afternoon: Temple of Heaven
- Evening: Easy hutong dinner
- Stay: Wangfujing or Dongcheng for metro access
- Note: Keep the first day light after a long flight

### Butler Rails

Butler Rails are secondary but always visible enough to make the product feel like a true travel butler.

Initial rail categories:

- Visa and entry
- Payment setup
- Booking readiness
- Transport logic
- Weather and season
- Language and translation
- Risk and fatigue
- Emergency readiness

Each rail item includes:

- Category
- Title
- Priority: high, medium, or low
- Short explanation
- Suggested action

Example rail item:

```json
{
  "type": "payment",
  "priority": "high",
  "title": "Set up Alipay before arrival",
  "body": "Foreign cards can work, but setup is easier before your first taxi or meal.",
  "action": "Add payment setup to Tools"
}
```

## AI Butler Chat

The chat is a continuous planning assistant, not a standalone transcript box.

It should support these first-stage behaviors:

- Start from suggested prompts.
- Accept free-form user messages.
- Show assistant replies.
- Trigger visible canvas updates.
- Explain what changed in plain English.
- Keep recent context visible enough for the user to understand the planning flow.

Example prompt chips:

- Plan my first China trip
- Make this trip less tiring
- Add food-focused stops
- Keep hotels convenient
- Reduce the budget
- Add visa and payment reminders

## Mock AI Pipeline

The first-stage implementation uses a deterministic mock AI pipeline. This keeps the product usable without real keys and makes the canvas interaction testable.

Flow:

```text
User message
-> mock intent parser
-> assistant response text
-> structured canvas patch
-> canvas state update
-> visible change animation/state
```

The implementation should isolate this behind a provider interface so a real AI service can replace it later.

Suggested patch shape:

```json
{
  "intent": "adjust_trip",
  "assistantMessage": "I slowed the pace and moved the hotel area closer to metro access.",
  "tripSummary": {
    "title": "First China Trip",
    "durationDays": 5,
    "pace": "Relaxed",
    "travelerStyle": "First-time visitor"
  },
  "days": [
    {
      "day": 1,
      "city": "Beijing",
      "pace": "Relaxed",
      "blocks": [
        {
          "time": "Morning",
          "title": "Arrival and check-in",
          "description": "Stay near Wangfujing or Dongcheng for easy metro access."
        }
      ],
      "food": ["Easy hutong dinner"],
      "stay": "Wangfujing or Dongcheng",
      "transport": "Use metro or short taxi rides on arrival day",
      "note": "Keep the first day light after the flight."
    }
  ],
  "butlerAlerts": [
    {
      "type": "payment",
      "priority": "high",
      "title": "Set up Alipay before arrival",
      "body": "Payment setup prevents friction with taxis, restaurants, and small shops.",
      "action": "Review payment setup"
    }
  ]
}
```

First-stage mock parser behavior:

- Messages mentioning "first time", "first China trip", or "5 days" generate an initial Beijing + Shanghai style itinerary.
- Messages mentioning "less tiring", "slow", or "relaxed" reduce daily density and update the pace.
- Messages mentioning "budget" switch suggestions toward lower-cost food, transport, and hotel areas.
- Messages mentioning "food" add dining-focused blocks.
- Messages mentioning "hotel" or "convenient" update stay recommendations.
- Messages mentioning "visa", "payment", "translate", or "emergency" add matching butler alerts.

## Placeholder Tabs

### Trips Placeholder

Trips shows a polished empty state:

- Heading: Saved trips are coming next.
- Description: Future trips, canvas snapshots, and AI planning history will appear here.
- Primary action returns to Chat.

### Explore Placeholder

Explore shows the future scope:

- Cities
- Attractions
- Food
- Hotels
- Local experiences

It should state that third-party providers will connect later, without naming unverified integrations as live.

### Tools Placeholder

Tools shows placeholder cards for:

- Translate
- Payment setup
- Visa and entry
- Currency
- Metro
- eSIM/VPN
- Emergency

Each card is disabled or marked as coming soon.

### Account Placeholder

Account shows:

- Sign in and sync coming later.
- Supabase connection reserved.
- Guest mode available for the first-stage skeleton.

## Technical Direction

Recommended stack:

- Next.js with React and TypeScript.
- Vercel deployment.
- Supabase reserved for future persistence and auth.
- CSS tokens plus component-level styling. Tailwind is acceptable if kept disciplined and token-driven.
- Minimal dependencies to preserve load speed.
- API route placeholders for future provider integration.

Reasoning:

- The new interaction model is stateful and component-heavy, which fits React better than the older single-file vanilla JavaScript approach.
- Vercel support is strongest with Next.js.
- Supabase fits the future account, trip, and chat-history needs.
- TypeScript makes the canvas patch contract safer as the product grows.

## Proposed Project Structure

```text
app/
  page.tsx
  layout.tsx
  globals.css
  chat/
    page.tsx
components/
  shell/
  chat/
  canvas/
  placeholders/
lib/
  mock-ai/
  canvas/
  env/
  types/
app/api/
  chat/route.ts
  trips/route.ts
  explore/route.ts
  tools/route.ts
docs/
  superpowers/specs/
```

The exact structure can be adjusted during implementation planning, but the first-stage code should keep chat, canvas, mock AI, and placeholder tabs separated.

## Environment Placeholders

The first-stage app should define placeholders for:

- `NEXT_PUBLIC_SUPABASE_URL`
- `NEXT_PUBLIC_SUPABASE_ANON_KEY`
- `SUPABASE_SERVICE_ROLE_KEY`
- `AI_API_KEY`
- `AI_BASE_URL`
- `AI_MODEL`
- `AMAP_API_KEY`
- `CTRIP_AID`
- `CTRIP_SID`
- `MEITUAN_UNION_API_KEY`
- `MEITUAN_UNION_API_SECRET`

Missing variables should never break the skeleton.

## Visual Direction

The visual style is warm New Chinese.

Core direction:

- Warm paper, cinnabar, muted gold, ink brown, and soft stone colors.
- Chinese ink landscape background.
- Text may sit directly on the background where readability is controlled by composition, spacing, and subtle overlays.
- Avoid translucent glass chat panels.
- Avoid dark ink-wash heaviness from early versions.
- Avoid decorative clutter such as excessive stamps, ornaments, or calligraphy.
- Use solid paper-like cards for structured itinerary content.
- Keep the interface app-like and usable, not a marketing landing page.

The background should create the first impression. The workspace content should feel placed onto a refined travel desk, not trapped inside floating translucent boxes.

## Loading and Stability Requirements

- The app should render useful skeleton content before any provider keys exist.
- The mock AI path must work offline from external providers.
- Large background imagery should be optimized and not block the core interface.
- Mobile width around 390px must avoid horizontal overflow.
- Chat input and mobile navigation must not cover core canvas content.
- All placeholder routes should be reachable and visually consistent.

## Accessibility Requirements

- Navigation uses semantic links or buttons with clear labels.
- Chat input has an explicit label.
- Assistant updates should be announced in an accessible status region where practical.
- Color contrast must remain readable over the background.
- Keyboard users can navigate chat input, prompt chips, and tabs.
- Motion should respect reduced-motion preferences.

## Testing Strategy

First-stage tests should cover:

- Mock AI parser returns expected patches for key message categories.
- Canvas state applies patches without losing existing trip data.
- Chat submission adds user and assistant messages.
- Placeholder tabs render without requiring keys.
- Mobile layout has no obvious overflow at 390px width.
- Environment variables can be absent without runtime failure.

Recommended checks:

- Unit tests for mock AI and canvas patch logic.
- Component tests where practical for chat/canvas rendering.
- Browser smoke test for desktop and mobile layouts before claiming completion.

## Approval State

Approved product decisions:

- Positioning emphasizes AI butler over generic travel chat.
- Main workspace uses a persistent right-side chat and live left-side trip canvas.
- Canvas model is mixed: day-by-day itinerary plus butler reminders.
- Explore replaces Cities and will later cover cities, attractions, dining, hotels, and experiences.
- First stage focuses on Chat / Butler only; other tabs are placeholders.
- Technical route should fit Vercel, Supabase, mobile use, stability, and loading speed.
- Visual direction is warm New Chinese with ink landscape background and no translucent chat boxes.

