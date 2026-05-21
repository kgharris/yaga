package com.lnkranch.yaga.theory

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class ChordTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    fun `chord has correct tones`(
        label: String,
        chord: Chord,
        expectedSymbol: String,
        expectedRoot: Int,
        expectedThird: Int,
        expectedFifth: Int,
        expectedSeventh: Int,
    ) {
        assertAll(
            { assertEquals(expectedSymbol,   chord.symbol,                    "$label: symbol") },
            { assertEquals(expectedRoot,     chord[Chord.Tone._root],         "$label: root") },
            { assertEquals(expectedThird,    chord[Chord.Tone._3rd],          "$label: 3rd") },
            { assertEquals(expectedFifth,    chord[Chord.Tone._5th],          "$label: 5th") },
            { assertEquals(expectedSeventh,  chord[Chord.Tone._7th],          "$label: 7th") },
            { assertNull(                    chord[Chord.Tone._9th],          "$label: 9th absent") },
        )
    }

    @Test
    fun `chord intervals property is accessible and correct for each type`() {
        assertAll(
            { assertEquals(listOf(0, 4, 7, 11), Chord.Maj7.intervals,     "Maj7 intervals") },
            { assertEquals(listOf(0, 3, 7, 10), Chord.Min7.intervals,     "Min7 intervals") },
            { assertEquals(listOf(0, 4, 7, 10), Chord.Dom7.intervals,     "Dom7 intervals") },
            { assertEquals(listOf(0, 3, 6, 10), Chord.HalfDim7.intervals, "HalfDim7 intervals") },
            { assertEquals(listOf(0, 3, 6,  9), Chord.FullDim7.intervals, "FullDim7 intervals") },
        )
    }

    @Test
    fun `Chord all contains all five chord types`() {
        val all = Chord.all
        assertAll(
            { assertTrue(Chord.Maj7     in all, "Maj7 in all") },
            { assertTrue(Chord.Min7     in all, "Min7 in all") },
            { assertTrue(Chord.Dom7     in all, "Dom7 in all") },
            { assertTrue(Chord.HalfDim7 in all, "HalfDim7 in all") },
            { assertTrue(Chord.FullDim7 in all, "FullDim7 in all") },
            { assertEquals(5, all.size, "all has exactly 5 entries") },
        )
    }

    companion object {
        @JvmStatic
        fun cases(): Stream<Arguments> = Stream.of(
            //          label         chord              symbol  root  3rd  5th   7th
            Arguments.of("Maj7",     Chord.Maj7,     "△7",     0,   4,   7,   11),
            Arguments.of("Min7",     Chord.Min7,     "m7",     0,   3,   7,   10),
            Arguments.of("Dom7",     Chord.Dom7,     "7",      0,   4,   7,   10),
            Arguments.of("HalfDim7", Chord.HalfDim7, "ø7",    0,   3,   6,   10),
            Arguments.of("FullDim7", Chord.FullDim7, "°7",    0,   3,   6,    9),
        )
    }
}
