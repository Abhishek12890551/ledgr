# Ledgr (working title)

A fully offline Android app for personal and group money. Your personal spending shows what you actually consumed, not what you paid for others.

This folder is the complete documentation pack. Put it at the root of the project repo.

## Where to start

| If you want to… | Open |
|---|---|
| Hand the project to an AI coding agent | `AGENTS.md` (it lists everything to read, in order) |
| Understand what we're building and why | `docs/PRD.md` |
| See the money rules and algorithms | `docs/ENGINE_SPEC.md` |
| See the exact Kotlin API | `docs/ENGINE_API.md` |
| Check expected numbers | `docs/GOLDEN_SCENARIOS.md`, `tools/reference_engine.py` |
| See architecture, stack and data model | `docs/TECH_STACK.md` |
| See colors, fonts, motion, sound | `docs/DESIGN_SYSTEM.md` |
| See every screen, state and the wording | `docs/UX_SPEC.md` |
| Track fonts, sounds, animation licences | `docs/ASSETS.md` |
| Test demand before investing more | `docs/VALIDATION_KIT.md` |
| Publish the app | `docs/RELEASE.md` |
| Plan the on-device models | `docs/ML_PLAN.md` |
| Log progress and decisions | `docs/PROGRESS.md` |

## Order of work

1. Optional in parallel: validation (`VALIDATION_KIT.md` Parts A and B) while the engine is built.
2. Build milestones M0 to M7 from `AGENTS.md`.
3. Prototype test with friends (`VALIDATION_KIT.md` Part C).
4. Decide: product, portfolio project, or pivot.
5. Release (`RELEASE.md`) and later the ML work (`ML_PLAN.md`).

## Before the agent starts
- Create a Git repo and commit this folder.
- Download the fonts, and source or create the sounds and animations (`docs/ASSETS.md`).
- Choose the app name and check that it's available.
- Android Studio with the Pixel 10 emulator and a physical phone for sound, haptics and performance checks.
