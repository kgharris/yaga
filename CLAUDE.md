# YAGA — Claude Code Guide

## Project

Android app (Kotlin + Jetpack Compose) for drilling chord tone identification. Player sees a chord symbol in key context and taps the 3rd and 7th on a 12-button note grid, as fast and accurately as possible.

## Modules

- `engine/` — pure Kotlin, zero Android dependencies; subject to 100%/100% Jacoco enforcement
  - `engine/.../theory/` — music theory (Key, Chord, ResolvedChord, FretboardLocator, etc.)
  - `engine/.../domain/` — drill app logic (ScoreCalculator, DrillMode, DrillSession, data shapes, etc.)
- `app/` — Android glue only (ViewModel lifecycle, Compose UI, Room/DataStore wiring); no coverage requirement

**Rule:** any pure Kotlin class with no Android imports belongs in `engine/`, not `app/`. The module boundary enforces this — `engine/` is a JVM-only module that physically cannot compile Android APIs. If logic can live in `engine/`, it must.

## Tech Stack

- Language: Kotlin
- UI: Jetpack Compose
- Architecture: MVVM
- Storage: Room
- Build: Gradle 9 with Kotlin DSL (`.gradle.kts`)
- Package: `com.lnkranch.yaga`

## Running Tests

```bash
./gradlew :engine:test                          # tests only
./gradlew :engine:test :engine:jacocoTestReport # tests + coverage
```

Coverage report: `engine/build/reports/jacoco/test/html/index.html`

Tests are run via the Bash tool (not PowerShell — gradlew.bat has a classpath issue in PS).

## Coverage Requirements

Enforced on the `engine` module. The theory engine is pure deterministic logic — untested branches are silent wrong-note bugs.

| Metric       | Minimum |
|--------------|---------|
| Instruction  | 100%    |
| Branch       | 100%    |

UI code (`app` module) is not subject to these thresholds.

## Architecture Constraints

- The theory engine must remain free of Android dependencies
- `TheoryEngine.resolve(key: Key, chord: RomanChord): ChordResult` is the single public entry point
- Access chord tones via typed enum: `result[ChordQuality.Tone._3rd]`, `result[ChordQuality.Tone._7th]`
- Future drill types (additional chord tones: b9, 9, 11, #11, 13) must fit the same engine interface — don't foreclose that
- Audio input is a planned future input subsystem; tap logic should stay cleanly separated from drill/scoring logic so it can be swapped out
- No network dependency

## Music Theory Invariants

- Note spelling is key-appropriate (Bb not A# in F major); the engine enforces this via a letter-skipping rule
- Enharmonic equivalents are not interchangeable — the app presents and expects the canonical spelling for the key
- Mode is implied by the progression, not selected independently by the user
- `Mode` is the correct term (not `Scale`) — sealed class with `object Major` and `object Minor`

## Drill Behaviour

- The drill deduplicates the progression's chord list (`.distinct()`, preserving first-occurrence order) before drilling — intentional, to avoid drilling the same chord repeatedly in repetition-heavy forms like the blues
- **Normal mode**: player must tap 3rd then 7th; tapping 7th first is a mistap
- **Reverse mode**: player must tap 7th then 3rd; tapping 3rd first is a mistap
- Normal and Reverse are tracked as separate modes with independent personal best history, keyed by `(progressionId, tonicName, drillMode)`

## Scoring

Current formula: `(chords / minutes) × accuracy_multiplier` where accuracy starts at 1.0 and loses 5% per mistap (floor 0.5). No bonuses.

A heatmap of performance per chord per key is the intended long-term feedback mechanism (planned post-MVP). The per-session data model will need to be extended to store **per-chord timing** to support this — don't design session storage in a way that forecloses it.

## Identifier Style

- Use underscore-prefixed identifiers for names that start with digits: `_3rd`, `_5th`, `_7th` (not backtick-escaped)

## Code Style

- No magic numbers or string literals — every domain-meaningful constant must be a named `const val`. Define it at the closest logical owner: companion object for class-scoped values, top-level in the relevant file for file-scoped values, a dedicated `*Constants.kt` for shared cross-file values.


## Work Commands

When asked "what's next", "pick up next task", or "what's ready":
1. Check for in-progress work first: `bd list --status in_progress`
2. If any in-progress issues exist, show them with `bd show <id>` and suggest resuming — these represent interrupted work from a prior session
3. Then run `bd ready` to show available unclaimed issues
4. Present both lists and let the user choose

When asked to "work on `<id>`" or the user selects an issue:
1. Run `bd update <id> --claim`
2. Run `bd show <id>` to read the full description and design
3. Create a feature branch: `git checkout -b <id>-<short-slug>`
4. Route by issue type:
   - **Bug** — implement directly, then proceed to step 5
   - **Feature** — read the description and design fields, draft an implementation plan (files × steps), spawn agents serially with a build gate between each step
5. Verify the build (use Bash tool, not PowerShell — gradlew.bat has a classpath issue in PS):
   - Always: `./gradlew :app:compileDebugKotlin`
   - If engine touched: `./gradlew :engine:test :engine:jacocoTestReport`
6. Squash agent commits on the feature branch, then push to Gitea (origin)
7. Run `bd close <id>` when work is complete

When work reveals follow-up tasks:
- File new issues with `bd create`
- Add dependencies with `bd dep add` where ordering matters

## Beads Issue Tracker

This project uses **bd (beads)** for issue tracking.

```bash
bd ready                     # Find available work
bd show <id>                 # View issue details
bd update <id> --claim       # Claim work
bd close <id>                # Complete work
bd create "title" -t bug|feature|task|chore -p 0-4  # File a new issue
bd dep add <child> <parent>  # Add dependency
```

Use `bd` for all task tracking — do not use markdown TODO lists or TaskCreate.
