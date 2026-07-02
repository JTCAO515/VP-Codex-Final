# VisePanda Android (native, v0.3.3/v0.3.4 foundation)

Native Android client per `docs/planning/v0.3.2-android-planning-synthesis.md`.
Kotlin + Jetpack Compose + Material 3, Today-first five-surface navigation
(Today / Butler / Plan / Explore / Tools), backed by an in-memory mock trip
repository this round — no real network or Supabase sync yet.

## ✅ Build-verified 2026-07-02 — real device/emulator pass recorded below

This module was originally authored in a sandboxed session with no Android
SDK and no network access to `dl.google.com`, so none of it had ever been
compiled. That gap has now been closed: a follow-up session on a real macOS
(Apple Silicon) machine, with Android Studio's bundled JBR (JDK 21) as
`JAVA_HOME` and a real `ANDROID_HOME` SDK install, generated the Gradle
wrapper, ran `./gradlew :app:assembleDebug` to a real `BUILD SUCCESSFUL`,
and manually exercised the resulting APK on an `android-34`
`google_apis/arm64-v8a` emulator (Pixel 6 profile). Record:

- **Gradle wrapper**: generated with `gradle wrapper --gradle-version 8.9
  --distribution-type bin`. `gradlew`, `gradlew.bat`, and
  `gradle/wrapper/gradle-wrapper.jar` are now committed.
- **Real compile errors found and fixed** (these were genuine compiler
  output, not guesses):
  1. Kotlin 2.0+ split the Compose compiler out of the Kotlin Gradle plugin;
     `id("org.jetbrains.kotlin.plugin.compose") version "2.0.20"` had to be
     added to the root `build.gradle.kts` (`apply false`) and applied in
     `app/build.gradle.kts`, or project configuration fails outright with
     "the Compose Compiler Gradle plugin is required when compose is
     enabled".
  2. `VisePandaBottomBar.kt`: `val backStackEntry by
     navController.currentBackStackEntryAsState()` needs
     `import androidx.compose.runtime.getValue` for the `by` delegate to
     resolve — otherwise `compileDebugKotlin` fails with "has no method
     'getValue'".
  3. `DayDetailScreen.kt` and `PlanScreen.kt` both use `TopAppBar`, a
     Material 3 experimental API — each needed
     `@OptIn(ExperimentalMaterial3Api::class)` on the composable, or the
     experimental-API warning is promoted to a hard compile error.
  - No other files had either of these two problems (verified with a
    project-wide grep after the fix).
- **`assembleDebug` result**: `BUILD SUCCESSFUL`, real
  `app/build/outputs/apk/debug/app-debug.apk` produced (~17.5 MB).
- **Manual acceptance pass** (installed via `adb install`, driven via `adb
  shell input`/`screencap`, all screenshots visually confirmed, no
  `FATAL`/`AndroidRuntime` crash in `logcat` at any step):
  - All five bottom-nav surfaces switch correctly: Today (real content),
    Plan (day list), and the Butler / Explore / Tools honest placeholders
    all render.
  - Plan → Day Detail opens, shows Morning/Afternoon/Evening blocks, and
    the back arrow returns to Plan.
  - Taxi Driver Card opens from both Today and Day Detail, shows the large
    Chinese address, and "Copy Chinese address" actually copies (confirmed
    via the system clipboard toast showing the correct address text).
  - With `adb shell svc wifi disable` + `svc data disable` (real network
    off, not just airplane-mode UI), the app kept running on the mock data
    path with no crash; re-enabled afterward.
- **What this pass did *not* cover**: `Hilt`/`Room`/`KSP` were only
  exercised at compile time (no DI graph or DB migration issues surfaced
  because nothing in this round actually reads/writes Room yet — see
  "What's deliberately not here yet" below). Physical-device-specific
  concerns (real GPU, real network stack, different OEM skins) are still
  unverified — only the emulator was used.

## First build checklist (already done once — repeat on a fresh machine)

1. Install Android Studio or the standalone SDK command-line tools; make
   sure `sdk.dir` resolves (via `local.properties` or `ANDROID_HOME`).
2. Run `gradle wrapper --gradle-version 8.9 --distribution-type bin` from
   `android/` if `gradlew` is somehow missing (it's committed now, so this
   should normally be a no-op).
3. `./gradlew :app:assembleDebug`. If it fails again on a newer AGP/Kotlin
   toolchain, treat the version pins in `build.gradle.kts` (AGP 8.5.2,
   Kotlin 2.0.20, Compose BOM 2024.06.00, Hilt 2.51.1, Room 2.6.1,
   Navigation Compose 2.7.7) as a starting point to re-verify, not a
   guarantee — this is exactly the class of error already found and fixed
   once above.
4. Install on a device/emulator and walk the v0.3.3/v0.3.4 acceptance
   criteria in `docs/planning/v0.3.2-android-planning-synthesis.md` by
   hand — already done once per the record above; repeat after any
   navigation/theming/Taxi Driver Card change.

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
