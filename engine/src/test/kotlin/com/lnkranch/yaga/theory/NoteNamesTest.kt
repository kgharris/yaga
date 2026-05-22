package com.lnkranch.yaga.theory

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
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

    // -----------------------------------------------------------------------
    // intervalFromRoot
    // -----------------------------------------------------------------------

    @Test
    fun `intervalFromRoot E above C is 4`() {
        assertEquals(4, intervalFromRoot(noteSemitone = 4, rootSemitone = 0))
    }

    @Test
    fun `intervalFromRoot wraps correctly when noteSemitone is below rootSemitone`() {
        // C (0) above B (11): ascending interval is 1 semitone
        assertEquals(1, intervalFromRoot(noteSemitone = 0, rootSemitone = 11))
    }

    @Test
    fun `intervalFromRoot unison returns 0`() {
        assertEquals(0, intervalFromRoot(noteSemitone = 7, rootSemitone = 7))
    }

    // -----------------------------------------------------------------------
    // intervalRole
    // -----------------------------------------------------------------------

    @Test
    fun `intervalRole null returns null`() {
        assertNull(intervalRole(null))
    }

    @Test
    fun `intervalRole 0 returns Root`() {
        assertEquals(IntervalRole.Root, intervalRole(0))
    }

    @Test
    fun `intervalRole minor third 3 returns Third`() {
        assertEquals(IntervalRole.Third, intervalRole(3))
    }

    @Test
    fun `intervalRole major third 4 returns Third`() {
        assertEquals(IntervalRole.Third, intervalRole(4))
    }

    @Test
    fun `intervalRole diminished fifth 6 returns Fifth`() {
        assertEquals(IntervalRole.Fifth, intervalRole(6))
    }

    @Test
    fun `intervalRole perfect fifth 7 returns Fifth`() {
        assertEquals(IntervalRole.Fifth, intervalRole(7))
    }

    @Test
    fun `intervalRole diminished seventh 9 returns Seventh`() {
        // FullDim7 seventh interval — was incorrectly falling into 'else' before this fix
        assertEquals(IntervalRole.Seventh, intervalRole(9))
    }

    @Test
    fun `intervalRole minor seventh 10 returns Seventh`() {
        assertEquals(IntervalRole.Seventh, intervalRole(10))
    }

    @Test
    fun `intervalRole major seventh 11 returns Seventh`() {
        assertEquals(IntervalRole.Seventh, intervalRole(11))
    }

    @Test
    fun `intervalRole other extension returns OtherExtension`() {
        assertEquals(IntervalRole.OtherExtension, intervalRole(1))
        assertEquals(IntervalRole.OtherExtension, intervalRole(2))
        assertEquals(IntervalRole.OtherExtension, intervalRole(5))
        assertEquals(IntervalRole.OtherExtension, intervalRole(8))
    }

    // -----------------------------------------------------------------------
    // ResolvedChord semitone convenience properties
    // -----------------------------------------------------------------------

    @Test
    fun `ResolvedChord rootSemitone thirdSemitone seventhSemitone match note names`() {
        // G7: root G(7), major third B(11), minor seventh F(5)
        val g7 = ResolvedChord(
            romanNumeral = "V7",
            chord = Chord.Dom7,
            symbol = "G7",
            tones = mapOf(
                Chord.Tone._root to "G",
                Chord.Tone._3rd  to "B",
                Chord.Tone._5th  to "D",
                Chord.Tone._7th  to "F",
            ),
        )
        assertEquals(7,  g7.rootSemitone)
        assertEquals(11, g7.thirdSemitone)
        assertEquals(5,  g7.seventhSemitone)
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
