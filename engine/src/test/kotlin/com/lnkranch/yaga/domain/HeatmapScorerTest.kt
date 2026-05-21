package com.lnkranch.yaga.domain

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HeatmapScorerTest {

    // ── adjustedMs ────────────────────────────────────────────────────────────

    @Test
    fun `adjustedMs with no mistaps returns elapsed unchanged`() {
        assertEquals(5_000L, HeatmapScorer.adjustedMs(5_000L, 0))
    }

    @Test
    fun `adjustedMs adds 4 seconds per mistap`() {
        assertEquals(13_000L, HeatmapScorer.adjustedMs(5_000L, 2))
    }

    // ── normalize ─────────────────────────────────────────────────────────────

    @Test
    fun `normalize empty list returns empty`() {
        assertTrue(HeatmapScorer.normalize(emptyList()).isEmpty())
    }

    @Test
    fun `normalize single value scores zero`() {
        assertEquals(listOf(0.0f), HeatmapScorer.normalize(listOf(5_000.0)))
    }

    @Test
    fun `normalize all identical values scores all zero`() {
        assertEquals(
            listOf(0.0f, 0.0f, 0.0f),
            HeatmapScorer.normalize(listOf(5_000.0, 5_000.0, 5_000.0)),
        )
    }

    @Test
    fun `normalize maps min to 0, mid to 0_5, max to 1`() {
        val result = HeatmapScorer.normalize(listOf(1_000.0, 2_000.0, 3_000.0))
        assertAll(
            { assertEquals(0.0f, result[0], 0.001f) },
            { assertEquals(0.5f, result[1], 0.001f) },
            { assertEquals(1.0f, result[2], 0.001f) },
        )
    }
}
