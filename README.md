# Ember — Deep-Focus Candle Tracker

> **Ember** is a native Android deep-focus tracker where every focus session is a real candle that burns down as the countdown runs. 100% offline, private, and strictly focused. Built with Kotlin, Jetpack Compose, Material 3, Room, Hilt, DataStore, WorkManager, and Glance.

---

## 🕯️ Overview & Design Philosophy

Ember brings tactile visual mindfulness to deep work. Rather than an abstract digital timer, sessions are anchored to the physical metaphor of a burning candle:
- **Ivory wax cylinder**: Shrinks smoothly with progress from full height down to an 18% stub.
- **Dynamic wax pool & drips**: Molten wax forms an elliptical pool and procedurally drips down the sides based on a deterministic seed derived from the session ID.
- **Multilayered flickering flame**: Blue base, orange mantle, yellow core, and white-hot heart with natural sum-of-sines harmonic flicker, gentle sway, breathing ambient glow, and table reflections.
- **Transitions**: 1-second ignite animation from tiny spark to full flame; ember-like resting glow during pause; delicate smoke wisp upon completion; thick puff of smoke and cooler stub tone upon a broken session.
- **Strict accountability**: Leaving the app for another app blows out the candle (with a 10-second grace period) and marks the session broken.
- **100% Local & Private**: No accounts, no network access, zero `INTERNET` permission in `AndroidManifest.xml`.

---

## 📐 Key Architectural Decisions

### 1. Architecture: MVVM + Unidirectional Data Flow (UDF)
- **UI Layer**: 100% Jetpack Compose with reactive state flows (`StateFlow`). ViewModels expose immutable UI state and process discrete user intents.
- **Domain Layer**: Clean Kotlin data classes (`Session`, `Task`, `Category`, `UserSettings`) and pure domain utility functions (`calculateStreaks`, `getPlannedSessionLabel`, drip generator) that are completely decoupled from Android framework dependencies for instantaneous unit testing.
- **Data Layer**: Room SQLite database with KSP code generation, UUID string primary keys, update timestamps, and DataStore Preferences for reactive settings.
- **Dependency Injection**: Google Hilt 2.60.1 providing singleton database, DAO, repository, and clock bindings across Activities, ViewModels, Services, and Workers.

### 2. Zero Internet Permission & Cloud-Sync Readiness
- The application manifest deliberately omits `android.permission.INTERNET`.
- To allow cloud sync in a future update without schema migrations:
  - All Room entities (`SessionEntity`, `TaskEntity`, `CategoryEntity`) use UUIDv4 primary keys instead of auto-incrementing integers.
  - Entities track `createdAt`, `updatedAt`, and `isDeleted` soft-delete flags.
  - A pluggable `SyncService` interface is included in the domain layer, backed by `NoOpSyncService` in v1. Adding remote sync requires only swapping the repository sync implementation.

### 3. Strict Mode Lifecycle & Edge Cases
Strict mode prevents phone distraction by blowing out the candle if the user abandons Ember:
- **Screen Off / Lock NEVER breaks a session**: Tapping the power button or locking the screen is valid behavior (e.g. keeping the phone face down). PowerManager is checked via `isInteractive`.
- **System Overlays & Notification Shade**: Pulling down the notification shade or interacting with permission dialogs only triggers `Activity.onPause`, **not** `onStop`. Strict mode is only evaluated on `Activity.onStop`.
- **10-Second Grace Timer in Foreground Service**:
  - When `onStop` occurs and `PowerManager.isInteractive == true`, the Activity notifies `TimerService` via an intent.
  - The 10-second grace countdown runs in the foreground service (not the Activity), ensuring it cannot be killed by background process limits.
  - If the user returns within 10 seconds (`Activity.onStart`), the grace timer is silently cancelled.
  - If the grace timer expires, `PowerManager.isInteractive` is re-checked. If the screen is still on, the session is marked `BROKEN`, the flame snuffs out with a puff of smoke, and an alert notification is delivered.
- **Paused Sessions & Breaks**: Strict mode is intentionally suspended during `PAUSED` states and "Take five" break countdowns.
- **No Intrusive Permissions**: No Accessibility Services or `PACKAGE_USAGE_STATS` permissions are required, maintaining user privacy and battery life.

### 4. Custom Compose Canvas Rendering (No 3rd-Party Libraries)
Ember contains zero 3rd-party charting or animation libraries. Everything is custom-drawn with Compose Canvas:
- **Zero Allocations in Draw Path**: Matrix transformations, Paths, and Paint objects are remembered or created once to ensure steady 60fps rendering without GC pressure.
- **Candle Cylinder & Shading**: Custom radial and linear gradient brushes simulating vertical wax translucency, subsurface scattering, and top wax pool depth.
- **Procedural Wax Drips**: Drips are calculated deterministically using a seeded pseudo-random distribution. As burn progress advances past each drip's threshold, the drip extends downward with a rounded tear tip.
- **Natural Flame Flicker Math**: Flicker is synthesized using a sum of three harmonically independent sine waves (`sin(t * 7.1) * 0.5 + sin(t * 13.7) * 0.3 + sin(t * 23.3) * 0.2`) combined with gentle horizontal sway and breathing ambient halo radii.
- **System "Remove Animations" Support**: When system animations are disabled in Android Accessibility Settings (`Settings.Global.TRANSITION_ANIMATION_SCALE == 0`), the flame renders in a calm, static configuration and transitions are instantaneous.
- **Rolling Digits (Odometer)**: Custom vertical reel animation using Compose `AnimatedContent` with fixed tabular figure widths, preventing digit layout jitter.
- **Today Horizontal Timeline**: Custom canvas day strip auto-scaling to the day's active hours, with category-colored focus blocks, dashed ash-gray broken blocks, hour grid lines, and a live amber current-time marker.
- **Weekly Bar Chart & Monthly Heatmap**: Custom Canvas widgets with goal indicator lines, glowing amber intensity scaling, and touch-interactive inspection tooltips.

### 5. Mid-Session Reboot & Crash Recovery
- Timer state persists epoch start timestamps and accumulated focused seconds.
- In the event of a device reboot or process termination:
  - `BootReceiver` listens to `ACTION_BOOT_COMPLETED`.
  - Stored sessions are inspected. If an uncompleted active session is found, it is reconciled against monotonic clock time and marked broken with the burned minutes safely recorded.

---

## 🛠️ Tech Stack & Versions

| Component | Library / Version |
| :--- | :--- |
| **Language** | Kotlin 2.3.20 |
| **UI Framework** | 100% Jetpack Compose (BOM 2026.03.01) |
| **Material Design** | Material 3 (1.4.0) with dark warm candlelit custom theme |
| **Target SDK / Min SDK**| Target SDK 36 (Android 16), Min SDK 26 (Android 8.0 Oreo) |
| **Build System** | AGP 9.0.1, Gradle Kotlin DSL, Version Catalog (`libs.versions.toml`) |
| **Dependency Injection**| Dagger Hilt 2.60.1 |
| **Local Storage** | Room 2.7.0 (KSP) SQLite with schema versioning |
| **Preferences** | AndroidX DataStore Preferences 1.1.1 |
| **Background Work** | WorkManager 2.10.0 (Daily 9:00 AM reminders) |
| **App Widget** | AndroidX Glance 1.1.1 (Home-screen daily progress & streak) |
| **Audio** | Android MediaPlayer with AudioFocusRequest & Ringer Mode checking |
| **Compiler Optimizations** | R8 Full Mode minification & resource shrinking |

---

## 🎨 Design System

Ember uses a custom dark warm aesthetic designed to feel like a quiet candlelit study room:
- **Background**: Near-black warm gradient (`#0A0705` top to `#14100C` bottom) with a warm radial glow behind the candle.
- **Accents**: Amber Accent (`#E8A15A`), Warm Glow (`#FFB74D`), White Flame Core (`#FFFFFF`), Flame Blue Base (`#4FC3F7`).
- **Typography**: Clean geometric sans with light tabular digits for the rolling timer, uppercase letter-spaced amber section labels (`EMBER`), and bold cream headers.
- **Pills & Buttons**: Fully rounded pill buttons (`#EBDCC3` primary fill with `#1A120C` dark text; `#1C1815` secondary fill with cream text; `#6E675F` ash gray for broken states).
- **Categories**: 5 built-in categories (Work, Study, Coding, Writing, Reading) with custom warm hex accents, plus support for adding custom categories and colors.

---

## 🧪 Testing & Verification

- **Unit Tests**:
  - `TimerEngineTest`: State machine transitions (RUNNING, PAUSED, OVERTIME, RESET, STRICT_GRACE), monotonic clock advancement, midnight crossing, and reboot recovery.
  - `SessionStatsAndRulesTest`: "Session X of N" plan labels, daily goal progress, and multi-day streak calculations.
- **UI Tests**:
  - `ComposeUiTest`: Start session sheet form validation, category selection, session card rendering, and CandleView composition.
- **Lint & Release Verification**:
  - Zero lint errors (`./gradlew lintDebug`).
  - Production R8 minification verified with `./gradlew assembleRelease` yielding a 2.6 MB optimized APK.

---

## 💎 UI Architecture & Refinement Pass

### 1. Typography & Variable Font Loading
- **Variable Font Family**: `Manrope` (`res/font/manrope.ttf`) and `Fraunces` (`res/font/fraunces.ttf`) are loaded dynamically using Android Compose's variable font engine via `FontVariation.Settings(FontVariation.weight(w))` for exact weight rendering:
  - ExtraLight 200 (Timer digits: 88sp, letterSpacing -2sp, `fontFeatureSettings = "tnum"`)
  - Light 300 (Stats totals: 48sp, tnum)
  - Normal 400 (Body text: 15sp)
  - Medium 500 (Captions: 12sp, Status: 13sp, Subtitles: 14sp)
  - SemiBold 600 (Buttons: 16sp, Task title: 28sp)
  - Bold 700 (Top label: 11sp uppercase amber, 2.5sp letterSpacing)
- **Centralized Design System (`Type.kt`)**: Replaced all ad-hoc `TextStyle` and `fontSize` declarations across all screens with semantic `EmberTheme.typography.*`.
- **Zero-Jitter Timer Reel**: Each rolling-digit slot is locked to a measured fixed width (52dp) with tabular numerals (`tnum`), and colons are rendered separately with 50% opacity and vertical centering.
- **Dynamic Debug Typography Switcher**: Debug builds feature an in-app toggle in Settings to seamlessly switch between **Manrope (Default modern sans)** and **Fraunces (Variable serif)** across the timer and headings.

### 2. Form Ergonomics & Start Sheet Layout
- **Fixed Action Bar**: "Light the candle" button is anchored outside the scrollable form within a dedicated bottom bar applying `Modifier.safeDrawingPadding()`, `Modifier.navigationBarsPadding()`, and `Modifier.imePadding()`.
- **Keyboard Auto-Scroll**: Uses `BringIntoViewRequester` on focused text fields so the active input is never obscured by the IME.
- **Single-Row Duration Chips**: Preset chips (25 min, 30 min, 90 min, Custom) occupy a single row, allowing the entire form and primary button to fit without scrolling on screens as small as 5.5 inches (720x1280).

### 3. Display Cutout & Edge-to-Edge Protection
- Fully removed immersive/hidden-system-bar mode. Status bar remains transparent with light icons during deep-focus sessions.
- In `MainActivity`, `layoutInDisplayCutoutMode` is set to `LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES`.
- All root composables apply `Modifier.safeDrawingPadding()`, guaranteeing complete protection against camera punch-holes, notches, and navigation bars on both gesture and 3-button navigation.
- Focus screen buttons are elevated 32dp above system bars for comfortable thumb reach.

---

## 🚀 How to Build and Run

### 1. Build Debug APK
```bash
./gradlew assembleDebug
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

### 2. Build Minified Release APK
```bash
./gradlew assembleRelease
```
Output: `app/build/outputs/apk/release/app-release-unsigned.apk`

### 3. Run Unit Tests
```bash
./gradlew test
```

### 4. Run Lint Checks
```bash
./gradlew lintDebug
```

### 5. Accessing Candle Lab (Debug Feature)
In debug builds (`BuildConfig.DEBUG == true`), a "Candle Lab" button appears in the top-right corner of the Today screen.
The Candle Lab provides:
- Live progress slider ($0.0$ to $1.0$) to observe wax melting and drip formation in real time.
- Direct phase triggers: `IDLE`, `IGNITING`, `BURNING`, `PAUSED`, `OVERTIME`, `FINISHED`, `BROKEN`.
- Drip Seed randomizer to preview wax patterns across different sessions.
- Preset jump buttons (Full, Mid, Stub).
