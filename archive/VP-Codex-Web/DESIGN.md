# VisePanda Design System

Last updated: 2026-06-23
Current version: v6.2.1

## Design Direction

VisePanda should feel like a practical China travel butler for foreigners: calm, fast, mobile-first, and useful in real-world moments such as taxi rides, hotel check-in, restaurant ordering, attraction signs, and emergency help.

The active direction:

- English-native UI.
- Mobile portrait first.
- Light sky-blue workspace with warm orange primary actions.
- Icon-only desktop rail.
- Bottom mobile app tabs.
- Clear task surfaces: Chatbot, Dashboard, Translation.
- Dashboard aggregates cities, map POI, tools, trips, hotels, and deals.
- Real travel imagery and concise operational copy.

## Tokens

Source of truth: `web/app.css`.

Important colors:

| Token | Use |
| --- | --- |
| `--bg` | Page background |
| `--surface` | Cards, controls, panels |
| `--surface-soft` | Soft tool areas |
| `--text` | Primary copy |
| `--text-muted` | Secondary copy |
| `--border` | Default borders |
| `--brand` | Sky-blue brand accent |
| `--accent` | Orange primary actions |
| `--success` | Ready/check states |
| `--danger` | Error states |

## Navigation

Primary tabs:

1. Chatbot
2. Dashboard
3. Translation

Desktop:

- Fixed left icon rail.
- Icon color should distinguish each module.
- Text labels are visually hidden but available to assistive technology.

Mobile:

- Fixed bottom tab bar.
- Three icon targets only, optimized for thumb reach.
- Hide tab labels visually to preserve space.
- Hide nav while composing in chat.

## Chatbot Design

Chatbot should feel like Claude/Gemini after a conversation starts:

- No visible mode/model/depth controls.
- No preset panel after the first message.
- Conversation, input, and follow-up suggestions only.
- Keep status text minimal and hide it when empty.
- Show DeepSeek connectivity as a compact status surface; local fallback must remain clear.

## Dashboard Design

Dashboard is the service hub:

- Weather/location strip first.
- Quick planner immediately below.
- Hotels, Map, and Deals as high-frequency action cards.
- My trips, recommended cities, and tools as compact modules.
- Mobile stacks into one column; desktop can use multi-column card grids.
- Use light motion only where it helps hierarchy, and respect reduced motion.

## Translate Design

Translate is a Phase 1.5 during-trip surface. It must feel operational, not decorative.

Required areas:

- Voice card: clear browser-capability status and future push-to-talk affordance.
- Text translator: large input, clear output, simple direction swap.
- Phrase library: tappable cards for taxi, hotel, dining, attractions, shopping, emergency, and culture.
- History: local recent translations with clear-history control.

Principles:

- UI copy must be English-native.
- Chinese text appears as translated content, not as interface chrome.
- Cards should be easy to show to another person.
- Emergency phrases should be direct and visible.
- Avoid implying full offline machine translation until implemented.

## Mobile Rules

- No horizontal overflow at 390px.
- Bottom nav must not cover core controls.
- Textareas must remain usable with keyboard.
- Translation cards should stack vertically.
- Phrase cards should be scannable and tappable.
- Tap targets should be at least 44px where practical.

## Phase 2 Boundary

Do not design community, journals, companion matching, or social feedback UI in this release. Mention them only in planning documents.

## Cache Rule

When CSS or JS changes, update:

- `web/index.html`
- `web/admin.html`
- `web/sw.js`
- `web/tests/stability-ui.test.js`

Current frontend cache marker:

```text
20260623-v621-travel-butler-translate
```

Current service worker cache:

```text
visepanda-shell-v621-travel-butler-translate
```
