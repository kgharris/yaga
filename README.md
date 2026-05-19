# YAGA — Yet Another Guitar App

An Android app for drilling chord tone identification. Given a chord symbol in a key context, tap the 3rd and 7th as fast and accurately as possible.

Designed for intermediate-to-advanced players building functional harmonic fluency.

## Features

- 12 tonic keys × major and minor modes
- Built-in progressions (ii–V–I, I–vi–ii–V, blues, etc.) plus user-defined custom progressions
- Two drill modes tracked separately on the leaderboard:
  - **Normal** — tap 3rd → 7th
  - **Reverse** — tap 7th → 3rd
- Count-up timer with score based on speed and accuracy
- Personal best tracking per progression / key / mode

## Architecture

The project is split into two Gradle modules:

```
engine/   Pure Kotlin theory engine — zero Android dependencies
app/      Android UI (Jetpack Compose, MVVM, Room)
```

The engine exposes a single entry point:

```kotlin
TheoryEngine().resolve(key: Key, chord: RomanChord): ChordResult
```

It handles tonic resolution, diatonic chord quality derivation, and key-appropriate note spelling (Bb not A# in the key of F).

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material3 |
| Architecture | MVVM + StateFlow |
| Storage | Room 2.7 |
| Build | Gradle 9 + KSP |

## Building

Open in Android Studio and run the `app` configuration, or:

```bash
./gradlew assembleDebug
```

## Tests

The engine module enforces 100% instruction and branch coverage:

```bash
./gradlew :engine:test                          # run tests
./gradlew :engine:test :engine:jacocoTestReport # tests + coverage report
```

Coverage report: `engine/build/reports/jacoco/test/html/index.html`

> Tests must be run via the Bash tool (not PowerShell) — `gradlew.bat` has a classpath issue in PS.

## Scoring

```
score = (chords / minutes) × accuracy_multiplier
```

- **Time score** — chords per minute; the primary driver
- **Accuracy multiplier** — starts at 1.0, −5% per mistap, floor 0.5

Scores are tracked independently for Normal and Reverse modes, so improving at one doesn't affect the other's personal best.

## License

Apache 2.0 — see [LICENSE](LICENSE).
