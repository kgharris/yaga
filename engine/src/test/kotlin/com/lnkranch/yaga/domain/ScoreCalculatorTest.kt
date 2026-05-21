package com.lnkranch.yaga.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class ScoreCalculatorTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    fun `compute returns expected score`(
        label: String,
        chordCount: Int,
        elapsedMs: Long,
        misTapCount: Int,
        expected: Double,
    ) {
        assertEquals(expected, ScoreCalculator.compute(chordCount, elapsedMs, misTapCount), 0.001)
    }

    companion object {
        @JvmStatic
        fun cases(): Stream<Arguments> = Stream.of(
            // 4 chords in 60s, no mistaps → 4.0 cpm × 1.0
            Arguments.of("no mistaps",      4, 60_000L, 0,  4.0),
            // 2 mistaps → accuracy = 0.9
            Arguments.of("2 mistaps",       4, 60_000L, 2,  3.6),
            // 11 mistaps → 1.0 − 0.55 = 0.45 < 0.5, floored to 0.5
            Arguments.of("accuracy floor",  4, 60_000L, 11, 2.0),
            // elapsedMs = 0 → coerced to 1 ms
            Arguments.of("zero elapsed",    1, 0L,       0,  60_000.0),
        )
    }
}
