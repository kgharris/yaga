package com.lnkranch.yaga.domain

object ScoreCalculator {
    private const val MS_PER_MINUTE = 60_000.0
    private const val MISTAP_PENALTY_FACTOR = 0.05
    private const val ACCURACY_MULTIPLIER_FLOOR = 0.5

    // Time score: chords-per-minute (4 chords in 10s = 24.0)
    // Accuracy: 5% deduction per mis-tap, floor 50%
    fun compute(chordCount: Int, elapsedMs: Long, misTapCount: Int): Double {
        val timeScore = chordCount * MS_PER_MINUTE / elapsedMs.coerceAtLeast(1L)
        val accuracyMultiplier = (1.0 - misTapCount * MISTAP_PENALTY_FACTOR).coerceAtLeast(ACCURACY_MULTIPLIER_FLOOR)
        return timeScore * accuracyMultiplier
    }
}
