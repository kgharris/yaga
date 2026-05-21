package com.lnkranch.yaga.theory

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class NoteNamesTest {

    @ParameterizedTest(name = "{0} → {1}")
    @MethodSource("cases")
    fun `noteNameToSemitone returns correct semitone`(name: String, expected: Int) {
        assertEquals(expected, noteNameToSemitone(name))
    }

    companion object {
        @JvmStatic
        fun cases(): Stream<Arguments> = Stream.of(
            // Natural notes
            Arguments.of("C",    0),
            Arguments.of("D",    2),
            Arguments.of("E",    4),
            Arguments.of("F",    5),
            Arguments.of("G",    7),
            Arguments.of("A",    9),
            Arguments.of("B",    11),
            // Single sharp / flat
            Arguments.of("F#",   6),
            Arguments.of("Bb",   10),
            Arguments.of("Eb",   3),
            // Single accidental requiring +120 wrap (base - flat goes negative before mod)
            Arguments.of("Cb",   11),   // (0 - 1 + 120) % 12 = 11
            Arguments.of("Fb",   4),    // (5 - 1 + 120) % 12 = 4
            Arguments.of("B#",   0),    // (11 + 1 + 120) % 12 = 0
            Arguments.of("E#",   5),
            // Double sharp / flat — exercises the ## and bb code paths
            Arguments.of("C##",  2),
            Arguments.of("Cbb",  10),
        )
    }
}
