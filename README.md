# VisePanda

VisePanda is an English-native AI China travel butler for international visitors. This repo is being rebuilt in phases, starting with the Chat / AI Butler workspace: a live trip canvas paired with a persistent planning conversation.

## Phase One Scope

- Chat / AI Butler is the primary experience.
- The left side of the desktop workspace is a live trip canvas.
- The right side is a persistent AI butler chat.
- Trips, Explore, Tools, and Account are placeholders in this phase.
- External keys are placeholders only; missing keys must not break the app.
- The visual system uses a warm New Chinese ink landscape background and solid paper-like UI surfaces.
- Destination-aware background switching is reserved for a future iteration, such as Great Wall or Forbidden City ink scenes for Beijing and Bund or Jiangnan garden scenes for Shanghai.

## Tech Stack

- Next.js App Router
- React
- TypeScript
- Vercel-ready deployment shape
- Supabase, AI, map, booking, and local provider keys reserved for later phases

## Local Run

```bash
npm install
npm run dev
```

Open `http://127.0.0.1:3000`.

## Checks

```bash
npm run test
npm run build
npm run test:e2e
```

## Environment

Copy `.env.example` to `.env.local` only when a later phase needs live providers. Phase one works without any configured keys.

Reserved variables cover future Supabase, AI provider, Amap, Trip.com, and Meituan integrations. The first-stage skeleton treats every missing key as expected and keeps the mock butler flow available.
