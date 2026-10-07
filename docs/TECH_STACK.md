# TECH_STACK — Ledgr (working title)

## 1. Decision: Kotlin (native Android) over React Native

| Factor | Kotlin + Jetpack Compose | React Native |
|---|---|---|
| Target | Android only (v1) | Cross-platform, which isn't needed in v1 |
| On-device ML (v1.5/v2) | LiteRT/TFLite is native and direct | Needs native modules/bridging |
| Animations, haptics, SoundPool | First-class, low latency | Bridging and extra libraries |
| Offline database | Room (SQLite) is mature | Third-party libraries |
| Share/file intents, no-INTERNET manifest | Direct | Possible but heavier |
| Engine testing | Pure Kotlin/JVM module, fast tests | Separate TS package |
| Future iOS | Kotlin Multiplatform could share `:engine` | N/A |

**Decision: Kotlin + Jetpack Compose.** Revises the earlier suggestion of a TypeScript engine: the engine is now a pure Kotlin module.

## 2. Stack

- **Language:** Kotlin (latest stable), coroutines + Flow
- **UI:** Jetpack Compose (use the Compose BOM), Material 3, Navigation Compose
- **Architecture:** MVVM with unidirectional data flow, repository pattern
- **DI:** Hilt (or Koin if build speed matters more)
- **Database:** Room over SQLite
- **Settings:** Jetpack DataStore (Preferences)
- **Serialization:** kotlinx.serialization (JSON), kotlinx-datetime
- **Background work:** WorkManager (recurring entries, optional reminders)
- **Animation:** Compose animation APIs; Lottie Compose for confetti/check animations
- **Sound:** `SoundPool`; **haptics:** Compose `LocalHapticFeedback` / `HapticFeedbackConstants`
- **Charts:** custom Compose Canvas (keeps the 60-30-10 styling exact and avoids dependencies)
- **Images for sharing:** render a Compose summary to `Bitmap`, share through `FileProvider` + `Intent.ACTION_SEND`
- **File export/import:** Storage Access Framework (`CreateDocument` / `OpenDocument`)
- **Biometrics:** `androidx.biometric`
- **Build:** Gradle Kotlin DSL with a version catalog (`libs.versions.toml`); pin exact versions to the latest stable at build time
- **Min SDK 26, target the latest stable SDK**

**Testing:** JUnit 5, Kotest (property-based testing + assertions), Turbine (Flow tests), Room in-memory DB tests, Compose UI tests, screenshot tests optional.

## 3. Modules

```
:engine     pure Kotlin/JVM. Money, allocate(), splits, events, balance, simplification, bridge, budget math. NO Android imports.
:data       Room entities/DAOs, repositories, export/import, JSON schema versioning. Depends on :engine.
:core-ui    design tokens, theme, shared components, motion, sound, haptics. No business logic.
:app        feature packages (home, quickadd, groups, people, insights, settings, onboarding), navigation, DI wiring.
:ml         (v1.5+) interfaces + TFLite implementations. v1 ships only interfaces and a no-op/rule-based stub.
```

Dependency direction: `:app → :core-ui, :data → :engine`. `:engine` depends on nothing.

## 4. Data model (Room)

Tables:
- `events(id PK, scope, type, occurred_date, occurred_at, recorded_at, supersedes, payload_json)` with indexes on `(scope, occurred_date)`, `(type)`, and `supersedes`
- `persons(id, name, status, merged_into, is_me)`
- `groups(id, name, status, revision, created_at)` and `group_members(group_id, person_id)`
- `accounts(id, name, type, opening_balance)`
- `categories(id, name, icon, sort)`
- `budgets(month, category_id?, amount)`
- `recurring(id, template_payload_json, rule, next_due)`

Notes:
- `payload_json` is the type-specific event body (kotlinx.serialization of the sealed `Event` types in `:engine`) so new fields don't need migrations. Indexed columns cover query needs.
- VOIDs are ordinary event rows. The effective set is "events with no VOID targeting them".
- Never store balances or totals as authoritative data. Derived state is computed by `:engine` from effective events and exposed as `Flow`s. An in-memory cache is allowed and must be rebuildable.
- Room migrations are tested. Export schema version is separate (`schema_version` in the JSON file).
- Write path: repository validates through `:engine` (shares sum, nets sum to zero) **before** insert; edits are a single DB transaction (VOID + new event + revision bump).

## 5. Key technical choices

- **No INTERNET permission** in the manifest. Add a lint/CI check that fails the build if it appears.
- `android:allowBackup="false"`, with manual export as the backup path.
- Optional DB encryption via SQLCipher can be added in v1.1. The biometric app lock is v1.
- Money formatting via a single `Money` value class and an Indian-grouping formatter.
- IDs: UUIDv4 (or v7) generated on device.
- Time: store `occurred_date` (local date string) for month bucketing, plus epoch ms.
- All user-visible strings in `strings.xml` for future Hindi localization.
- Sound assets in `res/raw`, animations in `res/raw` (Lottie JSON), keep total asset size small.

## 6. ML plan (later, not v1)

- Keep the code seam now: `interface Categorizer { fun suggest(description: String): List<CategorySuggestion> }` and `interface ExpenseParser { fun parse(text: String): ParsedEntry? }` in `:ml`. v1 ships a no-op/keyword-based stub.
- v1.5: small text classifier trained on the user's own entries, exported to TFLite/LiteRT, running fully on-device.
- v2: token-tagging model (amount, item, people, split type) for English + Hinglish, outputting the same structured entry the manual form creates; name resolution stays in app code.
- Training code lives in a **separate Python repository** (`ledgr-ml`) with synthetic-data generation, a hand-labelled real test set, and an honest evaluation report.

## 7. Quality gates (CI)

- `./gradlew :engine:test` runs fast on the JVM without an emulator and must always pass.
- Property tests for the invariants in `ENGINE_SPEC.md` §13.
- Lint, ktlint/detekt, and a check that the merged manifest has no INTERNET permission.
- Compose UI tests for: quick add, group expense with each split type, settle-up, undo delete.
- Manual QA script: the Goa-trip scenario with hand-calculated expected balances.

## 8. Repository layout

```
/AGENTS.md
/docs/PRD.md  /docs/ENGINE_SPEC.md  /docs/DESIGN_SYSTEM.md  /docs/TECH_STACK.md  /docs/ASSETS.md
/engine  /data  /core-ui  /app  /ml
/gradle/libs.versions.toml
```
