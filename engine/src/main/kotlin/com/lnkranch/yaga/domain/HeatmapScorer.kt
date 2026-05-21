package com.lnkranch.yaga.domain

object HeatmapScorer {
    const val MISTAP_PENALTY_MS = 4_000L

    fun adjustedMs(elapsedMs: Long, misTapCount: Int): Long =
        elapsedMs + misTapCount * MISTAP_PENALTY_MS

    // Maps a list of raw adjusted-ms values to normalized [0.0, 1.0] scores where
    // 0.0 = fastest (best) and 1.0 = slowest (worst). When all values are identical,
    // every cell scores 0.0 (green) — nothing to penalise.
    fun normalize(values: List<Double>): List<Float> {
        if (values.isEmpty()) return emptyList()
        val min = values.min()
        val max = values.max()
        val range = max - min
        return values.map { if (range == 0.0) 0.0f else ((it - min) / range).toFloat() }
    }
}
