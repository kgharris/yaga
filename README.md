# YAGA — Yet Another Guitar App

An Android app for drilling chord tone identification. Given a chord symbol in a key context, tap the 3rd and 7th as fast and accurately as possible.

Designed for intermediate-to-advanced players building functional harmonic fluency.

## Features

- All 12 tonic keys; major and minor mode is embedded in the progression
- Built-in progressions (ii–V–I, I–vi–ii–V, blues, etc.) plus a custom progression builder
- Two drill modes, tracked independently:
  - **Normal** — tap 3rd → 7th
  - **Reverse** — tap 7th → 3rd
- Two input modes:
  - **Buttons** — 12-button note grid
  - **Fretboard** — tap notes directly on a fretboard diagram
- Count-up timer with score based on speed and accuracy
- Personal best tracking per progression / key / drill mode
- Heatmap showing your slowest chords across sessions

## Scoring

```
score = (chords / minutes) × accuracy_multiplier
```

Accuracy starts at 1.0 and loses 5% per mistap, with a floor of 0.5. Normal and Reverse modes have independent personal bests.

## Building

Open in Android Studio and run the `app` configuration, or:

```bash
./gradlew assembleDebug
```
