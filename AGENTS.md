# AGENTS.md — build brief for Ledgr (working title)

You are building a fully offline Android expense app (personal + group money) in Kotlin and Jetpack Compose.

## Read first, in this order

1. `docs/PRD.md` — what to build and the acceptance criteria
2. `docs/ENGINE_SPEC.md` — the money logic (source of truth for all calculations)
3. `docs/ENGINE_API.md` — the exact Kotlin types and function signatures for `:engine`
4. `docs/GOLDEN_SCENARIOS.md` and `tools/reference_engine.py` — verified expected numbers; the Kotlin engine must match them exactly
5. `docs/TECH_STACK.md` — modules, libraries, data model
6. `docs/DESIGN_SYSTEM.md` — palette (60-30-10), fonts, motion, sound, haptics
7. `docs/UX_SPEC.md` — every screen, its states, validation, defaults and copy

If documents conflict, `ENGINE_SPEC.md` wins on logic, `ENGINE_API.md` on code shape, `DESIGN_SYSTEM.md` on visuals, `UX_SPEC.md` on screen behavior and wording, `PRD.md` on scope.

## Dev environment

- Android Studio with a **Pixel 10 virtual device** and a **physical Android phone** are available.
- Use the emulator for layout, themes, font scale and flows. Sound, haptics, biometrics, share sheet and performance must be verified on the physical phone.
- Keep the project buildable from the command line (`./gradlew :engine:test`, `./gradlew :app:assembleDebug`) so checks can run without the IDE.

## Non-negotiable rules

1. **Engine first.** `:engine` is pure Kotlin/JVM with no Android imports. Write its tests before building UI that depends on it.
2. **Integer paise only.** No Float/Double for money anywhere.
3. **Events are immutable.** Edit = VOID + new event with `supersedes`. Never update or delete event rows.
4. **Derived, not stored:** balances and analytics are computed from effective events.
5. **Reject invalid state:** a write that breaks an invariant (`ENGINE_SPEC.md` §13) must fail before reaching the database.
6. **No INTERNET permission,** no analytics or crash-reporting SDKs, `allowBackup=false`.
7. **Never confuse cash movement with spending** (`ENGINE_SPEC.md` §10).
8. **Design tokens only:** no hard-coded colors, text styles or dp values outside `:core-ui`. Respect the 60-30-10 usage rules.
9. **Sound, haptics and animation are optional and accessible:** settings toggles, reduce-motion support, never the only feedback.
10. Do not add features outside the PRD "Must/Should" list. Do not build v1.5/v2 items beyond the `:ml` interfaces.

## Milestones (finish and verify each before the next)

**M0 — Scaffold.** Gradle Kotlin DSL, version catalog, modules `:engine :data :core-ui :app :ml`, theme with all tokens (light/dark), CI task that fails if the merged manifest contains INTERNET.
_Done when:_ app launches to an empty themed Home; `./gradlew :engine:test` runs.

**M1 — Engine.** `Money`, `allocate`, all split types, itemized with proportional extras, event types, effective-event resolution, balance engine, simplification, original-flow view, bridge/personal spending, budget math, export model.
_Done when:_ all 14 example tests and all property-based invariants in `ENGINE_SPEC.md` pass, every scenario in `GOLDEN_SCENARIOS.md` matches exactly, and the API matches `ENGINE_API.md` (or deviations are logged in `docs/PROGRESS.md`).

**M2 — Data layer.** Room schema, repositories, validate-before-write, edit/void transactions, revision counter, JSON export/import (merge by ID), migration tests.
_Done when:_ export → import into a fresh DB reproduces identical balances and spending.

**M3 — Personal.** Quick add (numpad-first), edit/delete with Undo and edit trail, categories, accounts and transfers, history, Home with hero card, budget bar and safe-to-spend, person ledger (lend/borrow/settle).
_Done when:_ a personal expense takes ≤ 3 taps; card purchase + card bill payment counts spending once.

**M4 — Groups.** Create group, participants, "me" selection, add expense with every split type, multiple payers, balances, simplified view with original-flow toggle, settle-up and record settlement, group history with edit trail, archive/close rules.
_Done when:_ the Goa-trip scenario matches hand-calculated balances.

**M5 — Bridge and insights.** Keeper's personal spending reflects share, receivable/payable strip, Insights screen (month vs last, top categories, small-payment insight).
_Done when:_ ₹1,000 dinner for 4 paid by me shows spending +₹250, cash −₹1,000, receivable +₹750.

**M6 — Share, backup, handoff.** Text + image summary with revision number, share sheet, export/import UI, group handoff flow, biometric app lock.
_Done when:_ a shared summary is readable without the app and handoff reproduces identical balances.

**M7 — Polish.** All motion, reaction moments, sound and haptics from `DESIGN_SYSTEM.md`, onboarding, empty states, accessibility pass (TalkBack, font scale, reduce motion, contrast), performance pass, assets license file.
_Done when:_ every PRD acceptance criterion is checked off and all gates in `TECH_STACK.md` §7 pass.

## Definition of done (every milestone)

- Code compiles; unit and UI tests for the milestone pass; lint is clean.
- No violations of the non-negotiable rules above.
- Short note appended to `docs/PROGRESS.md`: what was built, deviations, open questions.

## When unsure

Prefer the simplest implementation that satisfies the spec. If a rule seems wrong or two docs conflict, state the question in `docs/PROGRESS.md` and continue with the most conservative interpretation instead of inventing new behavior.

## Suggested first prompt

"Read AGENTS.md and the four docs in /docs. Execute milestone M0, then M1. After M1, stop and summarise test results and any spec ambiguities before starting M2."
