# VisePanda — UX Layout & Frontend Design Specification

> Documentation-only. No code changes.
> Companion to `v0.1.52-product-interaction-blueprint.md` (positioning, journey,
> page roles) and `v0.1.53-one-stop-fit-blueprint.md` (plugin/technical
> architecture). Those two answer **what** and **why**; this document answers
> **how it should look, lay out, and feel** — concrete page composition,
> component-level interaction mechanics, and the frontend visual system for the
> one-stop FIT travel-butler positioning.

---

## Part I — Macro: the product as one spatial system

### 1. The single-surface mental model

VisePanda is one workspace, not six tabs. The macro layout expresses the
positioning ("one-stop butler with a single memory") through a persistent
spatial hierarchy that never changes underneath the traveler:

```
┌─────────────────────────────────────────────────────────────┐
│  TOP STRIP  brand · trip title · language · account          │  ← identity/trust
├─────────────────────────────────────────────────────────────┤
│                                                              │
│   PRIMARY WORKSPACE (context-dependent)                      │
│   ┌───────────────────────┬───────────────────────────────┐ │
│   │  LIVE TRIP CANVAS      │  COMMAND CENTER (Chat)         │ │
│   │  (source of truth)     │  (intent in, decisions out)   │ │
│   └───────────────────────┴───────────────────────────────┘ │
│                                                              │
├─────────────────────────────────────────────────────────────┤
│  FLOATING TRANSLATE  (camera/mic, always reachable)          │  ← in-China utility
├─────────────────────────────────────────────────────────────┤
│  BOTTOM NAV  Chat · Trips · Tools · Community                │  ← mode switch
└─────────────────────────────────────────────────────────────┘
```

Macro rules:

- **Canvas + Chat are the home**, not a landing grid. Everything else is a
  supporting surface reached from here.
- **The trip is always visible or one tap away.** The Canvas is the anchor; other
  surfaces are overlays/sheets that return the traveler to the Canvas.
- **Two device modes, one layout language:** desktop = side-by-side Canvas|Chat;
  mobile = Canvas as the scrollable base with Chat as a docked bottom composer
  that expands into a sheet. Same information, reflowed — never a different app.

### 2. Information architecture (what lives where)

| Layer | Surface | Persistence | Reached from |
|---|---|---|---|
| Command | Chat composer + log | session + Supabase | bottom nav / everywhere |
| Truth | Trip Canvas (days, summary, completeness) | Supabase / Offline Vault | always visible |
| Operational | Tools widgets, Day operational data | static + live plugins | inline in Chat + Tools tab |
| Utility | Translate (OCR/voice/text) | ephemeral | floating FAB + Day detail |
| Discovery | Explore POIs | live Amap/Dianping | inside Chat + Explore page |
| Memory | Trips library, status, share | Supabase | bottom nav |
| Trust | Account, preferences, consent, leads | Supabase / local | top strip |
| Proof | Community | local/Supabase | bottom nav (secondary) |
| Ops | Admin (role-gated) | Supabase server | not in traveler nav |

### 3. The five-anxiety layout principle

Each traveler anxiety must have a **visible resolution path within one tap of the
Canvas**. This drives where controls live:

- Entry (visa) → completeness blocker chip on Canvas + inline visa card in Chat.
- Payment → prep checklist item + payment wizard card.
- Connectivity → Tools eSIM widget surfaced when trip dates are set.
- Language → floating Translate FAB + "Show taxi driver" card on each Day.
- Itinerary → the Canvas itself + day quick-actions.

If a resolution is buried more than one tap from the Canvas, the layout has failed.

---

## Part II — Micro: page-by-page layout & interaction

Notation: `�usable on mobile`, `◧ desktop two-column`, states listed as
`[empty] [loading] [ready] [error]`.

### 1. Home (acquisition / first-start) ◧▸

**Goal:** answer "which China trip should I start?" in one screen, no typing.

```
┌───────────────────────────────────────────┐
│ brand mark        lang · account          │
│                                           │
│   "Your AI China Travel Butler"           │  ← one-line trust promise
│   plan · book · translate · in one place  │
│                                           │
│   ┌─────────┐ ┌─────────┐ ┌─────────┐     │  ← ARCHETYPE STARTS (primary)
│   │ First   │ │ Foodie  │ │ History │     │    each = big tappable card
│   │ 10 days │ │ 3 cities│ │ & nature│     │    → /chat?archetype=…
│   └─────────┘ └─────────┘ └─────────┘     │
│   [ or tell me your own trip → ]          │  ← free-text entry (secondary)
│                                           │
│   small proof row: real POIs · offline    │
│   tools · live translation                │
└───────────────────────────────────────────┘
```

Interaction:
- Signed-in with an active trip → skip marketing, route to `/chat` with the trip loaded (show a "Resume [trip title]" card instead of archetypes).
- Archetype tap → Chat opens with the seeded intent already sending (skeleton canvas visible immediately, not a blank screen).
- Layout: archetypes are the visual hero, not a 6-feature grid. Feature discovery moves into contextual moments, not the landing page.

### 2. Chat Command Center + Live Canvas (the core) ◧▸

Desktop two-column; mobile stacked (Canvas scrolls, Chat docks).

```
DESKTOP ◧
┌──────────────── Canvas (60%) ─────────────┬──── Chat (40%) ─────┐
│ [Trip title]        completeness ▓▓▓▓░ 68%│ remembered: Foodie ·│
│ Day 1 Beijing  ● Taking shape             │ 2 ppl · mid budget  │
│  ┌ Morning  Forbidden City   ★4.8 ¥60 ┐   │─────────────────────│
│  │ Afternoon …           [Lighten][Swap]│   │ You: less walking   │
│  └ Evening …                            ┘   │ VP ▸ headline       │
│ Day 2 …                                    │   body…             │
│                                            │   ✓ highlight        │
│ [＋ add day]      [prepare this trip →]    │   ⚠ watch-out        │
│                                            │ ▸ NEXT: add a rest? │  ← primary chip
│                                            │ [suggestion][sugg.] │
│                                            │ [type…]        [↑]  │
└────────────────────────────────────────────┴─────────────────────┘
```

Micro-interaction mechanics:
- **Structured reply rendering:** the Butler's `assistantResponse` renders as
  distinct blocks — headline (bold), body (1 paragraph), highlights (✓ list,
  sage), watchOut (⚠ amber), nextStep (promoted to the primary action chip).
  Never a wall of text.
- **nextStep is a button, not prose.** Tapping it sends the follow-up intent.
- **Canvas patches animate:** changed day cards briefly highlight (revised =
  gold left-border pulse; new = fade-in) so the traveler sees what the butler
  changed without re-reading.
- **Day quick-actions** (Lighten / Add food / Swap morning / Add rest) sit on the
  card, send prebuilt Butler intents through the normal pipeline (never mutate
  canvas directly).
- **Remembered-preferences rail** is a thin chip row at the top of Chat, editable
  by tap — visible memory, never a form.
- **Inline cards:** factual asks (visa/payment/eSIM) render a compact tool card in
  the chat log with an "Add reminder" action, no tab switch.
- **Model/provider status is subtle** — a tiny label ("Zhipu GLM-5"); travelers
  care about confidence, not routing.

Mobile ▸:
```
Canvas scrolls as the base; a docked composer bar sits above the bottom nav.
Tapping it expands a chat sheet (70dvh) over the canvas; sending collapses it
so the canvas change is visible. Day detail opens as a full bottom sheet.
```

### 3. Trip Canvas — Day card & Day detail (operational) ▸

Day card (compact) → Day detail (operational drawer/sheet):

```
DAY DETAIL SHEET
┌───────────────────────────────────────┐
│ Day 2 · Xi'an           ● Looking good │
│ ─────────────────────────────────────  │
│ Morning · Terracotta Army             │
│   ★4.7 (12k) · ¥120 · 08:30–17:00     │  ← rich POI fields (optional)
│   兵马俑  秦始皇陵…                     │  ← Chinese name/address
│   [📍 Map] [📞 Call] [🎫 Book on Ctrip]│  ← plugin CTAs
│   [🚕 Show taxi driver]                │  ← full-screen bilingual address card
│   why: matches your history interest   │  ← preference rationale
│ ─────────────────────────────────────  │
│ transit → 22 min drive to next stop    │  ← Map plugin injected TransitBlock
└───────────────────────────────────────┘
```

- All rich fields optional → degrade to today's text when data absent.
- "Show taxi driver" = full-screen, large bilingual address + static map + offline-safe.
- Booking CTA appears only when the Booking plugin matches a Ctrip product.

### 4. Explore (discovery, demoted) ▸

- Reached primarily from Chat ("Browse Chengdu") but retains a page.
- Card layout upgraded to rich: left photo thumbnail, name, ★rating, ¥price,
  hours; "In your trip" badge when the POI is already on the Canvas.
- Add-to-Trip becomes precise: **Add to Day N / Replace morning / Use as dinner**
  (a small menu, not one generic button).
- Preference re-ranking: interests float matching POIs up (no extra API call).

### 5. Tools (operational widgets) ▸

- Grid of 6 category cards → each opens a drawer that now hosts an **interactive
  widget above the static checklist** (widget primary, checklist as reference):
  - Visa: passport-select questionnaire → eligibility + transit calculator.
  - Payment: card-tier selector → Alipay/WeChat setup steps + fee warnings.
  - Currency: live converter (amount in, reverse, offline snapshot).
  - Metro: from/to station → route/fare/time (Amap transit) + bilingual station names.
  - eSIM/VPN: provider comparison table + purchase links.
  - Emergency: one-tap 110/120/119, GPS embassy locator, phrase TTS, share-location.
- Every tool offers "Save to trip prep" so results attach to the Canvas checklist.

### 6. Translate (global utility) ▸

- **Floating FAB** (bottom-right, above nav) reachable from any surface → opens a
  bottom sheet already in OCR-camera mode; text/voice as segmented options.
- Returns the traveler to the exact prior context after use (no stranding).
- Entry points from Day detail: "show Chinese address", "speak this".

### 7. Trips (memory & readiness) ▸

- Trip cards show **user-facing status** (Taking shape / Ready for review /
  Travel-ready / Archived) and **readiness blockers** (missing hotel area, no
  payment setup, visa not checked) as small chips.
- Detail page keeps itinerary primary with compact actions (as repaired v0.1.43).

### 8. Account (trust center) ◧▸

- Keep the quick popover; add a dedicated `/account` structured like an airline/bank account center: profile completeness meter, security, preferences (editable `UserPreferenceProfile`), travel documents (opt-in, encrypted), notifications, privacy/data (export/delete/consent).
- Lead capture is progressive and contextual — appears at high-intent moments (after a strong draft, before concierge help), each field explains why it's asked.

### 9. Admin (ops, role-gated) ◧

- Not in traveler nav. Two-pane: leads/conversations list ‖ selected customer's
  raw data + LLM `CustomerBrief` card (summary, budget signal, readiness score,
  open questions, suggested next action). Server-side only.

---

## Part III — Frontend visual design system

### 1. Design tokens (formalize the Warm New Chinese system)

```
Color   --paper #F6F1E7   --paper-soft #EFE7D6   --ink #2A2622
        --cinnabar #A33A2D (primary accent, use sparingly)
        --gold #B68634 (ratings, highlights)   --sage #667B5C (success/price)
        semantic: --ok=sage  --warn=#B8862E  --danger=#9D2F24  --info=#4A6080
Type    Serif display (headings, compact), humanist sans (body/UI)
        scale 12 / 14 / 16 / 20 / 28 (tight, workspace-dense)
Space   4-based scale: 4 8 12 16 24 32
Radius  8 (cards) · 12 (sheets) · 999 (pills)
Shadow  hairline ink dividers + very low-opacity paper backing (no glassmorphism)
Motion  120ms micro · 240ms sheet · reduced-motion honored
```

### 2. Reusable component library (stop hand-rolling per page)

`Button` (primary cinnabar / ghost / quiet), `Card` (paper), `Field`, `Pill`
(status/filter/preference chips), `Modal`, `Sheet` (mobile bottom sheet),
`Toast`, `RatingStars`, `PriceLevel`, `StatCard`, `ProgressMeter`
(completeness), `POICard`, `ToolCard`, `MessageBlock` (headline/body/highlights/
watchOut/nextStep renderer). One implementation, themed by tokens.

### 3. Visual hierarchy rules per surface

- **Canvas** is calm and structural (paper, ink lines); the only saturated color
  is a status dot and the revised-day gold pulse.
- **Chat** leads with the headline; watchOut is the only amber; nextStep is the
  only cinnabar button in the reply → the eye always finds the next action.
- **Tools/Emergency** may use stronger semantic color (danger red for emergency).
- **Ratings** use gold, **prices** use sage — consistent everywhere they appear.

### 4. Motion & feedback

- Canvas patch: changed cards fade/slide in (240ms), revised cards pulse gold border once.
- Send: optimistic user bubble appears instantly; a typing shimmer holds the assistant slot.
- Loading: skeletons for canvas days and POI cards, never blank space.
- Success: toast for Save / Add-to-trip / Mark-done.
- All motion respects `prefers-reduced-motion`.

### 5. Mobile-first specifics

- Bottom nav fixed with safe-area insets (already established v0.1.44).
- Day detail and Chat are bottom sheets with drag handles; content never hides behind the fixed nav (shell bottom padding).
- Touch targets ≥ 44px; horizontal scroll only for filter/preference pill rows.
- Translate FAB clears the nav and safe area.

### 6. Accessibility & i18n

- Contrast audit against paper backgrounds; visible focus states; full keyboard nav.
- RTL correctness for Arabic (already supported); all new components mirror.
- Traveler-facing copy only (no "provider/API/metadata/confidence") per the UX writing rules.

---

## Part IV — How this maps to the existing roadmap

This spec does not add new roadmap phases; it is the **design contract** the
already-planned phases should build against:

| Existing phase (v0.1.52/53 roadmap) | This spec's governing section |
|---|---|
| Canvas Action Layer | II.2 mechanics, II.3, III.1 completeness meter |
| Inline Tool Cards | II.2 inline cards, III.2 `MessageBlock`/`ToolCard` |
| TripBlock POI Embedding / Day detail | II.3 Day detail layout, III.2 `POICard` |
| Translate Everywhere | II.6 FAB layout, III.5 |
| Tools Widgets | II.5 widget-over-checklist layout |
| Account Center + Lead capture | II.8, III trust tone |
| Admin + Customer Brief | II.9 two-pane layout |

Guardrails (unchanged from prior blueprints): Chat stays the spine; quick
actions send structured intents, never direct canvas mutation; rich fields
optional; keys server-side; fallbacks preserved; favor small contextual controls
over large new panels; demote any page that doesn't answer a traveler question.
