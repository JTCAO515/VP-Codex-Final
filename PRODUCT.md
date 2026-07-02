# VisePanda Product Context

Current handoff version: `v0.3.2`. This file is active product context for future design, native mobile, and frontend work; `v0.2.11` was the configuration pass that introduced it, and `v0.3.2` is the Android planning synthesis that bridges strategy into native APK implementation.

VisePanda is an English-native AI travel butler for independent foreign travelers in China. The active product mainline is a native Android APK first, with web retained as backend/admin/share/proof and iOS planned separately after Android shape is proven. It should feel like a focused travel operations desk: the traveler sees what to do today, asks the Butler for changes, keeps the plan trustworthy, and resolves practical anxieties such as entry, payment, connectivity, language, local transit, and itinerary execution inside the same mobile product surface.

## Audience

- First-time foreign visitors to China who need confidence around entry rules, payments, connectivity, transport, translation, and itinerary pacing.
- Independent FIT travelers who do not want a group tour but need help navigating local Chinese services.
- Long-stay foreigners in China planning weekend or regional trips.

## Product Register

- Calm, practical, trustworthy, and precise.
- Travel-facing and operational, not a generic dashboard or a marketing landing page.
- English-first, with Chinese support where it helps travelers execute tasks locally.
- Visual expression may feel warm, crafted, and China-aware, but task clarity wins over decoration.

## Core Loop

1. Traveler starts from an archetype, free-form intent, or contextual tool need.
2. Chat extracts preferences and asks at most one useful clarifying question when needed.
3. The Butler uses local tools, live providers, and model fallback to produce a structured response.
4. The Live Trip Canvas becomes the source of truth for itinerary content.
5. Small actions, tool cards, and readiness cues guide the next step without making the traveler leave the flow.

## Design Guardrails

- Preserve the existing Warm New Chinese / travel desk direction unless a later design pass explicitly replaces it.
- Use the project `DESIGN.md` and `app/globals.css` tokens before borrowing outside styles.
- Do not introduce glassmorphism, generic purple gradients, oversized SaaS hero sections, decorative blobs, or fake clickable controls.
- For product workspaces, prefer compact, scan-friendly layouts with internal scrolling.
- Use motion only to explain state change, loading, focus, or canvas updates.
- Use real disabled controls with `title="Coming soon"` for visible future affordances that are not implemented yet.
