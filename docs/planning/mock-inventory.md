# VisePanda — Mock / Placeholder / Local-Simulation Inventory

> Purpose: list **every** place that currently uses a mock, placeholder, static
> stub, or local (localStorage-only) simulation, and the plan to replace each
> with a real tool / real data / real workflow. Per the standing security rule,
> "replace" means **make the real path primary while keeping the mock as a
> graceful fallback** — we never delete the fallback, we demote it.
>
> Status legend: 🟢 real primary live · 🟡 real primary partial · 🔴 still mock/placeholder.
> Last updated: v0.1.51.

## Summary table

| # | Area | File(s) | Current state | Real replacement | Status | Target |
|---|---|---|---|---|---|---|
| 1 | AI Butler chat | `lib/ai/orchestrator.ts`, `lib/mock-ai/mockButler.ts` | Multi-LLM orchestrator is primary; DeepSeek/Qwen/Zhipu/Moonshot keys are configured in Vercel; mock canvas patch remains fallback | Real Chinese LLMs (DeepSeek/Qwen/GLM/Kimi/ERNIE) via orchestrator | 🟢 | v0.1.47 done; v0.1.48 configured defaults |
| 2 | Butler suggestions | `lib/ai/butlerPrompt.ts` | Model-generated when live; mock heuristics as fallback | Model output | 🟢 | done |
| 3 | Explore POIs | `lib/explore/amapProvider.ts`, `staticProvider.ts`, `amapSearch.ts` | Amap live search primary with rich metadata; 8-city static fallback | Dianping/Meituan review depth | 🟡 | Dianping/Meituan later |
| 4 | Explore rich fields (rating/price/hours/photos) | `lib/explore/amapProvider.ts`, `lib/explore/amapSearch.ts` | Amap `extensions=all` rich fields captured and rendered when present | Add Dianping review counts and booking links | 🟡 | Dianping/Meituan later |
| 5 | Tools — Currency | `lib/tools/liveToolsProvider.ts` | Live ExchangeRate-API rate injected | Live (add converter widget) | 🟡 | 阶段十五 |
| 6 | Tools — Visa/Payment/Metro/eSIM/Emergency | `lib/tools/staticProvider.ts` | Static text checklists only | Interactive widgets (eligibility checker, wizards, route planner, call buttons) | 🔴 | 阶段十五 |
| 7 | Community feed/photos/likes | `lib/community/mockData.ts`, `components/community/*` | Fully local mock + localStorage | Supabase `community_*` tables + Storage | 🔴 | 阶段十一 |
| 8 | Membership tiers | `lib/community/membership.ts` | Static tier definitions, no real progression | Supabase-backed member progression | 🔴 | 阶段十一 / 十六 |
| 9 | Account avatar | `components/account/AccountMenu.tsx`, `lib/account/avatars.ts` | localStorage only (`visepanda:selected-avatar`) | Supabase profile + Storage upload | 🔴 | 阶段十六 |
| 10 | Account UI | `components/account/AccountMenu.tsx` | Minimal popover, no `/account` page | Professional `/account` center | 🔴 | 阶段十六 |
| 11 | Lead capture (留资) | — (does not exist) | None | Supabase `leads` + progressive profiling | 🔴 | 阶段十六 |
| 12 | Preference profile | `lib/ai/preferenceProfile.ts`, `components/chat/ButlerWorkspace.tsx` | Guest localStorage profile + Chat prompt injection | Supabase `profiles` table for logged-in cross-device sync | 🟡 | Supabase migration later |
| 13 | Trips persistence | `lib/supabase/tripsRepository.ts`, `lib/trips/mockTrips.ts` | Real Supabase; mock trips when guest/unconfigured | Real Supabase (primary already) | 🟢 | done (guest fallback intentional) |
| 14 | Guest draft | `components/chat/ButlerWorkspace.tsx` | localStorage `visepanda:guest-draft` | Supabase on sign-in (already migrates) | 🟢 | done |
| 15 | Translate text/OCR/TTS | `app/api/translate/*` | Qwen live + DeepSeek text fallback | Live | 🟢 | done |
| 16 | Translate voice STT input | `components/translate/UnifiedTranslator.tsx` | Record → `/api/translate/stt` live; some UI marked "Coming soon" historically | Live STT | 🟡 | 阶段十 task 10.5 |
| 17 | Admin backend | — (does not exist) | None | `/admin` + LLM `CustomerBrief` | 🔴 | 阶段十七 |
| 18 | Native iOS/Android apps | — (does not exist) | None | React Native + Expo | 🔴 | 阶段十四 |
| 19 | Map rendering | — (does not exist) | None | Amap JS map widget | 🔴 | 阶段十二 v0.1.52 |
| 20 | Reserved-but-unused env keys | `lib/env/placeholders.ts` | `CTRIP_AID/SID`, `MEITUAN_UNION_*` declared, no code | Trip.com / Meituan Union integrations | 🔴 | later |
| 21 | Placeholder page component | `components/placeholders/PlaceholderPage.tsx` | Generic placeholder scaffold | Replace wherever still mounted | 🟡 | as pages mature |
| 22 | Supabase not deployed | all Supabase code | Degrades to guest/mock when keys absent | Real Supabase project + migrations run | 🟡 | user action |
| 23 | Real booking data (flights/trains/hotels/attraction tickets) | Canvas `stay`/`transport` fields, Explore stays/attractions | Fully AI-generated text; no real price, no booking link | FlyAI (飞猪) `search-hotel`/`search-poi`/`search-flight`/`search-train` via official Fliggy server-side partnership | 🔴 | pending official Fliggy partnership; dev-tool available now via `.claude/skills/flyai/` |

## Replacement principles

1. **Real primary, mock fallback.** Every item keeps its mock/static path as the
   graceful fallback. The work is to add (or finish) the real path and make it
   primary, not to delete the fallback.
2. **Provider abstraction stays.** Explore/Tools/AI all route through their
   provider/orchestrator entry points; swapping in real data never touches the
   rendering components.
3. **Keys are the gate.** Several items are "code-complete, awaiting keys"
   (multi-LLM, Amap enrichment). Those move 🟡→🟢 the moment the user adds the
   server-side env var — no code change required.
4. **Sequence follows PLAN.** The target column maps each item to its phase in
   `PLAN.md` / `docs/planning/v0.1.46-product-expansion.md`.

## What changed in v0.1.47

- Item 1 & 2 moved 🔴→🟢 (code): the Butler now runs a real multi-model
  orchestrator (DeepSeek/Qwen/GLM/Kimi/ERNIE) with intent routing, a high-stakes
  ensemble, and a full fallback chain ending at the mock Butler. It goes fully
  live for any provider whose key the user adds; with zero keys it behaves
  exactly as before (mock).

## What changed in v0.1.48

- Item 1 is now production-configured for the user's Vercel keys: DeepSeek v4
  flash, Qwen 3.6 Flash, Zhipu GLM5, and Moonshot Kimi 2.5 are the active
  Butler defaults. Mock fallback is still retained for local/no-key scenarios.

## What changed in v0.1.49-v0.1.51

- Items 3 and 4 now use Amap `extensions=all` and render optional rich POI
  metadata in Explore.
- Item 12 moved 🔴→🟡: guest preference memory now exists locally and is injected
  into Chat; Supabase cross-device profile persistence is still planned.

## What changed in v0.2.6

- Added item 23: real booking data (flights/trains/hotels/attraction tickets)
  via FlyAI (飞猪), researched in `docs/planning/flyai-skill-integration.md`.
  Currently 🔴 — no production code calls it; the CLI is architecturally a
  developer/agent tool (MCP streamable_http client with device-fingerprinted
  anti-abuse headers and Alibaba's own embedded trial credentials), not a
  documented third-party server API, so it cannot move past 🔴 until an
  official Fliggy server-side partnership is confirmed (same principle as the
  standing Dianping/Meituan guidance: apply officially, never scrape/reverse
  engineer). A dev-tool path is available immediately via the vendored
  `.claude/skills/flyai/` Claude Code skill for research/content-curation use
  during development only.
