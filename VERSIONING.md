# VisePanda Versioning

## Current Versions

- Previous baseline: `v0.1.1`
- Current iteration: `v0.1.8`
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
