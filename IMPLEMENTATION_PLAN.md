# Ember - Implementation Plan

## Overview
Ember is a native Android deep-focus tracker where each session is a real candle that melts down over time. Built with 100% Jetpack Compose, Material 3 base with custom dark warm theme, MVVM + UDF, Room (KSP), Hilt DI, DataStore, WorkManager, and Glance. No third-party animation or chart libraries; all visuals are custom-rendered via Compose Canvas.

Package name: `app.ember.focus`
Min SDK: 26, Target/Compile SDK: 35.

---

## Architecture & Technology Stack
- **Language & UI**: Kotlin 2.0+, 100% Jetpack Compose, Material 3 base with custom warm candlelit theme.
- **Architecture**: Clean MVVM + Unidirectional Data Flow (UDF), Kotlin Coroutines + StateFlow/SharedFlow.
- **Dependency Injection**: Hilt (Dagger).
- **Persistence**: Room (KSP) with UUID string PKs, timestamps, soft-delete, and exportSchema enabled. DataStore for user preferences.
- **Background & Lifecycle**: Foreground Service with low-importance persistent notification, exact alarms (`AlarmManager`), WorkManager for daily reminders.
- **Graphics & Motion**: Custom Compose Canvas drawing (candle body, melted wax pool, procedural drips, layered teardrop flame with noise/flicker, smoke wisps, odometer rolling digits, weekly bar chart, monthly glowing heatmap).
- **Home Widget**: Jetpack Glance widget (2x2 and 4x2 sizes).
- **Permissions**: STRICT ZERO-INTERNET policy. No `INTERNET` permission declared in AndroidManifest.xml.

---

## Implementation Phases

### Phase 1: Project Scaffold, Theme & Foundations
- Initialize Gradle Kotlin DSL project with version catalog (`libs.versions.toml`).
- Configure plugins: Android Application, Kotlin Android, Compose, Hilt, KSP.
- Set up Design System:
  - Color palette: Near-black warm gradient background (`#0A0705` to `#14100C`), Amber accent (`#E8A15A`), Cream text (`#F2E6D0`), Muted text (`#8C8176`), Ash gray (`#6E675F`), Pill button styles.
  - Typography: Inter/geometric sans font family, custom thin-weight tabular digits style, uppercase amber section labels.
  - Shapes & elevation: Pill shapes, rounded cards, dark warm glows.
- Data Layer:
  - Room entities: `SessionEntity`, `TaskEntity`, `CategoryEntity` with UUID PKs, timestamps, and `isDeleted`.
  - Room DAOs and `EmberDatabase` with schema export and initial pre-seeded categories.
  - Repository interfaces and implementations: `SessionRepository`, `TaskRepository`, `CategoryRepository`, `SettingsRepository`.
  - `SyncService` interface with `NoOpSyncService` implementation (cloud-ready).
  - Injectable `Clock` interface (`SystemClockImpl` and test doubles).
- Single-Activity setup with Compose Navigation skeleton: Onboarding, Today, Focus, Stats, Settings, CandleLab.

### Phase 2: Candle Renderer, Flame & Candle Lab
- Procedural `CandleView(state: CandleState)`:
  - **Body**: Ivory/cream wax cylinder with radial/linear vertical shading and highlight, resting on a brass dish. Height scales smoothly from 100% down to ~18% stub.
  - **Top Surface**: Elliptical melted wax pool expanding and dipping as wax consumes.
  - **Drips**: Deterministic wax drip streaks running down cylinder sides, seeded by session ID.
  - **Flame**: Layered teardrop (bright white/yellow core, vibrant amber/orange outer mantle, soft blue base). Continuous smooth noise flicker (sum of sines) for sway, scale, and luminance. Dynamic halo glow breathing in sync. Curved dark wick.
  - **States/Phases**: Idle (unlit full candle), Ignite animation (~1s spark to flame), Running, Paused (ember-like faint glow), Overtime (stub on puddle with steady flame), Finished (flame shrinks, gentle curved smoke wisp rises and fades), Broken (quick 0.3s snuff, dark puff of smoke, cooler muted candle body).
  - Respect system "remove animations" / reduced motion.
- Debug "Candle Lab" screen: Interactive sliders for progress (0..1), phase selector (IDLE, IGNITING, RUNNING, PAUSED, OVERTIME, FINISHED, BROKEN), drip seed picker, toggle for flicker speed.

### Phase 3: Timer Engine, Strict Mode & Recovery
- `TimerEngine` state machine:
  - States: `IDLE`, `RUNNING`, `PAUSED`, `OVERTIME`, `COMPLETED`, `BROKEN`.
  - Monotonic clock timing (`SystemClock.elapsedRealtime`) with wall-clock epoch timestamps for persistence and recovery.
  - Periodic heartbeat (every 2-3s) persisted to Room/DataStore.
  - Reboot detection: on launch, reconcile state; if reboot occurred during active session, mark as `BROKEN` with counted time up to last heartbeat.
  - Overtime handling: At 00:00, transition to `OVERTIME`, count up (`+MM:SS`), trigger quiet notification.
- Foreground Service (`TimerService`):
  - Correct Android 14+ foreground service type (`specialUse` with timer description).
  - Ongoing, low-importance notification with current remaining/overtime countdown and quick actions.
  - Exact alarm scheduled for planned end time via `AlarmManager.setExactAndAllowWhileIdle`.
- Strict Mode:
  - Activity `onStop` triggers 10s grace timer inside `TimerService` only if screen is on (`PowerManager.isInteractive`).
  - Screen locking / display off does NOT break session.
  - Cancels silently if user returns within 10s.
  - If grace expires and interactive is true: snuffs candle, transitions to `BROKEN`, fires notification ("Your candle went out · X min counted").
  - Unit tests covering state machine, pause, overtime, reboot reconciliation, strict mode grace timer, and counted minutes.

### Phase 4: Focus Screen UI & Odometer Rolling Digits
- Header: Amber uppercase "EMBER" label, bold task title, muted goal/session counter ("session X of N").
- Large timer display: Custom vertical rolling odometer animation for digits (`MM:SS` or `H:MM:SS`), thin weight, tabular figures.
- Status label: dynamic subtitle ("Focus · ends at 10:25", "Paused", "In flow · +X min", etc.).
- Bottom controls: Dark "Pause / Resume" pill, cream "Reset" pill (or "Finish" in overtime). Ambient sound quick toggle icon.
- Immersive mode (hide system bars) and `FLAG_KEEP_SCREEN_ON` support.
- Reflection sheet: 1-5 ember rating, optional note, goal achieved toggle.
- Break screen: "Take five" countdown (5 min default, 15 min every 4th session) with Skip button.

### Phase 5: Today Screen & Timeline
- Date header with day navigation (prev/next) and calendar picker.
- Total deep-work banner: "Xh Ym", goal progress indicator, current streak with flame icon, completed vs broken counts.
- Canvas Day Timeline: Horizontal strip mapping 24-hour sessions to blocks, auto-fitted viewport (min 6h), current time marker, category colored blocks, broken sessions in dashed ash gray. Tap block to scroll/select session.
- Session List: Chronological cards with time range, duration, task, category chip, rating embers, broken tag.
- Session detail/edit bottom sheet or dialog (edit task, category, rating, note; delete with confirmation).
- Start flow: "Light a candle" button opening setup sheet (task autocomplete, category selector + add category, goal, presets 25/30/90 min & custom slider, planned sessions count).

### Phase 6: Stats Screen & Custom Canvas Charts
- Tabs: Week | Month.
- Week View: Custom Canvas bar chart of daily deep-work with dashed goal line, weekly total, daily average, best day.
- Month View: Custom Canvas monthly heatmap calendar where daily blocks glow with amber intensity proportional to deep work minutes.
- Additional metrics: Category distribution (horizontal stacked/proportional bars), average rating, longest session, completion rate.
- Streak computation: Daily goal threshold logic (today does not break until midnight).
- Interactive touch inspection on charts.

### Phase 7: Settings, Reminders, Audio, Glance Widget & Onboarding
- Settings screen: Daily goal, default duration, strict mode toggle, break lengths, keep screen on toggle, category manager (CRUD), About.
- Ambient audio: Synthesized/packaged soft crackling fire loop, played via `MediaPlayer`/`ExoPlayer` with audio focus and ringer handling.
- Daily reminder: WorkManager periodic request, 9:00 AM default, checks if session completed today, notifications permission request.
- Glance Home Widget: 2x2 and 4x2 layout showing today's hours, progress ring/bar, streak.
- Onboarding flow: 3 calm screens explaining the candle, strict mode, and setting daily goal.

### Phase 8: Verification, Polish, Accessibility & README
- Accessibility audit: 48dp minimum targets, content descriptions, TalkBack descriptions.
- Lint and compile check: Zero errors, clean release build with R8.
- Adaptive launcher icon and splash screen.
- Verify end-to-end on emulator.
- Write comprehensive `README.md`.
