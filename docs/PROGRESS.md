# PROGRESS

Running log, newest entry first. The build agent appends one entry per milestone. The owner records decisions here too.

## Entry template

```
### {date} — {milestone or decision title}
**Built:** what was implemented
**Tests:** what ran, pass/fail counts, anything skipped and why
**Deviations from the docs:** what differs and why (also update the doc if the change is permanent)
**Open questions / ambiguities:** things the spec didn't cover
**Next:** what comes next
```

## Decisions log (owner)

| Date | Decision | Evidence / reason |
|---|---|---|
| 2026-10-07 | Native Kotlin + Jetpack Compose | Android-only, offline, on-device ML, sound/haptics (see `TECH_STACK.md`) |
| 2026-10-07 | Keeper model for groups in v1 | No server; others get read-only summaries |
| 2026-10-07 | Simplified balances on by default, original flow available | Owner decision |
| 2026-10-07 | Extras on itemized bills split proportionally | Owner decision |
| 2026-10-07 | Palette: Emerald, Mist, Sand, Clay, Espresso | Owner's Coolors palette |
| | Validation outcome (continue as product / portfolio project / pivot) | Fill in after `VALIDATION_KIT.md` Parts A-B |
| | App name | |

## Log

### 2026-10-07 — M0 Scaffold
**Built:** Scaffolded the project structure (`:engine`, `:data`, `:core-ui`, `:app`, `:ml`). Created version catalog in `libs.versions.toml`. Implemented theme tokens from `docs/DESIGN_SYSTEM.md` in `:core-ui`. Moved app package to `app.ledgr`. Configured `jvmToolchain(17)` for all modules. Created a custom Gradle task to enforce the no INTERNET permission rule in `app/build.gradle.kts`. Built an empty themed Home screen in `app`.
**Tests:** Ran `./gradlew :engine:test` successfully. Ran `./gradlew :app:assembleDebug` and manually verified that the INTERNET permission check accurately blocks the build when the permission is introduced, and succeeds when it is removed.
**Deviations from the docs:** Replaced `Icons.Filled.Add` with text `+` to avoid requiring material-icons-extended, ensuring an empty themed home screen without adding extra dependencies right away. Handled Gradle configuration cache errors caused by module plugin conflicts by avoiding explicit `kotlin.android` alias in submodules and instead using a `kotlin { jvmToolchain(17) }` block, since `agp 9.4.1` with `kotlin-compose` automatically sets up Kotlin safely. Corrected the `ksp` plugin version to `2.2.10-2.0.2` instead of `2.2.10-1.0.30` which could not be found. 
**Open questions / ambiguities:** `material-icons-extended` was not explicitly listed in `TECH_STACK.md` versions block, so we used a text placeholder for the floating action button on the Home screen to stay conservative.
**Next:** M1 — Engine logic.
