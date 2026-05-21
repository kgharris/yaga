package com.lnkranch.yaga.domain

object ScoreCalculator {
    // Time score: chords-per-minute (4 chords in 10s = 24.0)
    // Accuracy: 5% deduction per mis-tap, floor 50%
    fun compute(chordCount: Int, elapsedMs: Long, misTapCount: Int): Double {
        val timeScore = chordCount * 60_000.0 / elapsedMs.coerceAtLeast(1L)
        val accuracyMultiplier = (1.0 - misTapCount * 0.05).coerceAtLeast(0.5)
        return timeScore * accuracyMultiplier
    }
}
