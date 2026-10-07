# Ledgr

A fully offline, privacy-first Android app for personal and group money management. Your personal spending shows what you actually consumed, not just what you paid for others.

## Features (Engine & Data Layer)
- **Immutable Event Sourcing:** Financial events are immutable. Edits are handled by voiding and superseding events, maintaining a perfect financial history.
- **Offline First:** Fully offline data storage using Room (SQLite). No INTERNET permission required. No cloud syncing, maximizing privacy.
- **Precision Mathematics:** Uses 64-bit Long integers for "paise" to avoid all floating-point rounding errors. Deterministic tie-breaking on remainder allocation.
- **Comprehensive Splits:** Supports equal, exact, percentage, shares, and complex itemized splits with proportional taxes/discounts.
- **Group & Personal:** Flawless cross-boundary logic between group ledgers and your personal bridge (tracking what you owe vs what you personally consumed).

## Current Status
- **Milestone 0 (Scaffold):** Completed. Modules structured, Gradle Kotlin DSL configured, CI task for zero internet permission added.
- **Milestone 1 (Engine):** Completed. 100% test coverage against PRD Golden Scenarios. Property-based invariants mathematically verified. 
- **Milestone 2 (Data Layer):** Completed. Room entities perfectly mimic engine event sealed classes using JSON polymorphic payloads.

## Tech Stack
- **Language:** Kotlin (latest stable), Coroutines & Flow
- **UI:** Jetpack Compose (upcoming in M3/M4)
- **Database:** Room (SQLite) with KSP
- **Architecture:** Unidirectional data flow / MVVM 
- **Testing:** Kotest (Property-based tests), JUnit 5, Robolectric

## Setup and Development

Ledgr uses Gradle and relies on Android Studio.

### Running the Engine Tests (Pure JVM)
The core engine mathematical logic runs instantly without an emulator.
```bash
./gradlew :engine:test
```

### Running Data Layer Tests (Robolectric)
```bash
./gradlew :data:test
```

### Documentation
All specifications and rules are located in the `docs/` folder. Start with `AGENTS.md` and `docs/PRD.md` to understand the overarching system architecture.
