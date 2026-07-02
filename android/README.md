# VisePanda Android (native, v0.3.3/v0.3.4 foundation)

Native Android client per `docs/planning/v0.3.2-android-planning-synthesis.md`.
Kotlin + Jetpack Compose + Material 3, Today-first five-surface navigation
(Today / Butler / Plan / Explore / Tools), backed by an in-memory mock trip
repository this round — no real network or Supabase sync yet.

## ⚠️ Not build-verified in this environment — read before trusting the code

This module was authored in a sandboxed session with **no Android SDK
installed and no network access to `dl.google.com` / Google's Maven
repository** (confirmed blocked by the session's egress policy — see
`curl "$HTTPS_PROXY/__agentproxy/status"` for the recorded `403` on
`dl.google.com`). That means:

- **No `./gradlew assembleDebug` was run.** Gradle could not even resolve
  the Android Gradle Plugin in this sandbox, so dependency resolution,
  Compose compilation, KSP (Room/Hilt annotation processing), and packaging
  were never exercised.
- All Kotlin/Gradle files were written by hand against current (2024-era)
  AndroidX/Compose/Hilt/Room APIs from training knowledge, then reviewed
  manually for structural correctness. The one local verification tool
  available (`kotlinc` 1.3.31 from `apt`) is too old to validate modern
  Kotlin syntax (it doesn't even support trailing commas, which Kotlin 2.0
  uses idiomatically) and was **not** used as a real signal — see AGENTS.md's
  v0.3.3 note for the full story if you're picking this up.
- **The Gradle wrapper JAR is not present.** `gradlew`/`gradlew.bat` and
  `gradle/wrapper/gradle-wrapper.jar` are standard Gradle-generated binaries
  this sandbox could not produce without network access. Generate them
  yourself on a machine with SDK/network access:

  ```bash
  cd android
  gradle wrapper --gradle-version 8.9 --distribution-type bin
  ```

  (or open the `android/` directory in Android Studio, which will offer to
  do this for you).

## First build checklist (do this before writing any more Android code)

1. Install Android Studio or the standalone SDK command-line tools; make
   sure `sdk.dir` resolves (via `local.properties` or `ANDROID_HOME`).
2. Run `gradle wrapper --gradle-version 8.9 --distribution-type bin` from
   `android/` (see above) if `gradlew` is missing.
3. `./gradlew :app:assembleDebug` and fix whatever the compiler actually
   flags — treat every version pin in `build.gradle.kts` (AGP 8.5.2, Kotlin
   2.0.20, Compose BOM 2024.06.00, Hilt 2.51.1, Room 2.6.1, Navigation
   Compose 2.7.7) as a starting point to verify against whatever is current
   when you run this, not a guarantee.
4. Once it builds, install on a device/emulator and walk the v0.3.3/v0.3.4
   acceptance criteria in
   `docs/planning/v0.3.2-android-planning-synthesis.md` by hand: switch all
   five bottom-nav surfaces, open a Day Detail, show a taxi driver card, and
   confirm no crash with the network disabled.

## What's actually implemented this round

- App shell: `MainActivity` → `VisePandaTheme` → `VisePandaApp` (Compose
  `NavHost` + bottom `NavigationBar`).
- Five-surface navigation: Today (real content), Plan + Day Detail (real
  content, mock data), Butler / Explore / Tools (honest placeholders — see
  their screen files for which future version fills each in).
- `data/model/` — Kotlin mirror of `lib/types/trip.ts`, plus a native port
  of `lib/mock-ai/mockButler.ts`'s `initialTripState`
  (`MockTripData.kt`) and `lib/trips/completeness.ts`
  (`TripCompleteness.kt`) kept behaviorally identical (same rounding, same
  vacuous-completeness rule) so the readiness percentage never disagrees
  with the web app for the same trip.
- `data/repository/TripRepository.kt` — the interface every screen depends
  on; `MockTripRepository` is the only implementation this round. Swapping
  in a Room/Supabase-backed implementation in v0.3.5 should not require
  changing this interface or any screen.
- `data/local/` (Room) and `data/datastore/` (DataStore) — defined per the
  v0.3.3 scope ("interfaces defined but only minimally wired"), not yet
  consumed by the repository. This is intentional, not an oversight — see
  each file's doc comment.
- Taxi Driver Card (`ui/components/TaxiDriverCard.kt`) — a single shared
  implementation used from Today and Day Detail, with a visible button
  (never a hidden gesture/shake trigger — that was explicitly removed in
  the v0.3.2 synthesis) and a copy-to-clipboard action for the Chinese
  address.
- Warm New Chinese color palette ported to Compose (`ui/theme/`), Dynamic
  Color intentionally disabled — see `Color.kt`'s doc comment.

## What's deliberately not here yet

No network calls, no Supabase, no real AI Butler responses, no map, no
camera/microphone, no permissions requested. See
`docs/planning/v0.3.2-android-planning-synthesis.md` for the full
v0.3.5–v0.3.7 roadmap that fills these in.
