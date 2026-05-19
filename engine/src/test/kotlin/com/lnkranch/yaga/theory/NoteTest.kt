package com.lnkranch.yaga.theory

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class NoteTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    fun `note has correct semitone`(label: String, note: Note, expectedSemitone: Int) {
        assertEquals(expectedSemitone, note.semitone)
    }

    companion object {
        @JvmStatic
        fun cases(): Stream<Arguments> = Stream.of(
            Arguments.of("C",  Note.C,  0),
            Arguments.of("Db", Note.Db, 1),
            Arguments.of("D",  Note.D,  2),
            Arguments.of("Eb", Note.Eb, 3),
            Arguments.of("E",  Note.E,  4),
            Arguments.of("F",  Note.F,  5),
            Arguments.of("Gb", Note.Gb, 6),
            Arguments.of("G",  Note.G,  7),
            Arguments.of("Ab", Note.Ab, 8),
            Arguments.of("A",  Note.A,  9),
            Arguments.of("Bb", Note.Bb, 10),
            Arguments.of("B",  Note.B,  11),
        )
    }
}
