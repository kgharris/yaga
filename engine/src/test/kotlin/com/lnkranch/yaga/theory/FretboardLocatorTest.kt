package com.lnkranch.yaga.theory

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class FretboardLocatorTest {

    // OPEN_SEMITONES = [4, 11, 7, 2, 9, 4]  (strings 0..5: high-E, B, G, D, A, low-E)

    // -----------------------------------------------------------------------
    // Open-string / fret-0 coverage
    // -----------------------------------------------------------------------

    @Test
    fun `open high-E string is returned when windowStart is 0 and noteSemitone is 4`() {
        // String 0 open = semitone 4 (E). At fret 0, (4 + 0) % 12 == 4.
        val positions = FretboardLocator.locateNote(noteSemitone = 4, windowStart = 0)
        assertTrue(FretPosition(string = 0, fret = 0) in positions,
            "Expected high-E open (string 0, fret 0) for semitone 4")
    }

    @Test
    fun `open low-E string is returned when windowStart is 0 and noteSemitone is 4`() {
        // String 5 open = semitone 4 (E). At fret 0, (4 + 0) % 12 == 4.
        val positions = FretboardLocator.locateNote(noteSemitone = 4, windowStart = 0)
        assertTrue(FretPosition(string = 5, fret = 0) in positions,
            "Expected low-E open (string 5, fret 0) for semitone 4")
    }

    @Test
    fun `windowStart 0 includes fret 0 open strings in results`() {
        // E (semitone 4) at window 0: strings 0 and 5 both open at fret 0.
        // Also string 1 (open=11): fret 5 → (11+5)%12=4 ✓
        // Also string 3 (open=2):  fret 2 → (2+2)%12=4  ✓
        // String 2 (open=7):  fret 9 → outside window 0..5
        // String 4 (open=9):  fret 7 → outside window 0..5
        val positions = FretboardLocator.locateNote(noteSemitone = 4, windowStart = 0)
        val expected = listOf(
            FretPosition(0, 0),
            FretPosition(1, 5),
            FretPosition(3, 2),
            FretPosition(5, 0),
        )
        assertEquals(expected.toSet(), positions.toSet())
        assertTrue(positions.all { it.fret >= 0 }, "All frets must be >= windowStart (0)")
    }

    // -----------------------------------------------------------------------
    // Known count for a specific note + window
    // -----------------------------------------------------------------------

    @Test
    fun `C natural semitone 0 at windowStart 0 returns exactly 3 positions`() {
        // String 0 (4):  need fret 8  → outside 0..5
        // String 1 (11): need fret 1  → (11+1)%12=0 ✓
        // String 2 (7):  need fret 5  → (7+5)%12=0  ✓
        // String 3 (2):  need fret 10 → outside 0..5
        // String 4 (9):  need fret 3  → (9+3)%12=0  ✓
        // String 5 (4):  need fret 8  → outside 0..5
        val positions = FretboardLocator.locateNote(noteSemitone = 0, windowStart = 0)
        assertEquals(3, positions.size, "C natural should have 3 positions in frets 0..5")
        assertEquals(
            setOf(FretPosition(1, 1), FretPosition(2, 5), FretPosition(4, 3)),
            positions.toSet()
        )
    }

    // -----------------------------------------------------------------------
    // Window boundary: only positions within [windowStart, windowStart+5] are returned
    // -----------------------------------------------------------------------

    @Test
    fun `positions from a lower window are not included when windowStart shifts up`() {
        // C (semitone 0) at windowStart=0 → strings 1(fret 1), 2(fret 5), 4(fret 3).
        // At windowStart=6 those low frets are excluded; next occurrences fall at
        //   string 0 (open=4):  fret 8  → (4+8)%12=0  ✓  in 6..11
        //   string 3 (open=2):  fret 10 → (2+10)%12=0 ✓  in 6..11
        //   string 5 (open=4):  fret 8  → (4+8)%12=0  ✓  in 6..11
        //   string 1 (open=11): fret 13 → outside 6..11
        //   string 2 (open=7):  fret 17 → outside 6..11
        //   string 4 (open=9):  fret 15 → outside 6..11
        val positionsLow  = FretboardLocator.locateNote(noteSemitone = 0, windowStart = 0)
        val positionsHigh = FretboardLocator.locateNote(noteSemitone = 0, windowStart = 6)
        // The two windows share no frets, so no position can appear in both.
        val sharedFrets = positionsLow.map { it.fret }.intersect(positionsHigh.map { it.fret }.toSet())
        assertTrue(sharedFrets.isEmpty(),
            "Windows 0..5 and 6..11 must not share any fret values, shared: $sharedFrets")
        // High window should contain exactly the three strings that land in 6..11.
        assertEquals(
            setOf(FretPosition(0, 8), FretPosition(3, 10), FretPosition(5, 8)),
            positionsHigh.toSet()
        )
    }

    // -----------------------------------------------------------------------
    // Multiple strings in the same window
    // -----------------------------------------------------------------------

    @Test
    fun `note appearing on multiple strings in the same window returns all of them`() {
        // C (semitone 0) at window 0: strings 1, 2, 4 all produce C within frets 0..5.
        val positions = FretboardLocator.locateNote(noteSemitone = 0, windowStart = 0)
        val strings = positions.map { it.string }
        assertTrue(1 in strings, "String 1 (B string) should produce C in window 0..5")
        assertTrue(2 in strings, "String 2 (G string) should produce C in window 0..5")
        assertTrue(4 in strings, "String 4 (A string) should produce C in window 0..5")
        assertEquals(3, positions.size, "Exactly 3 strings produce C in frets 0..5")
    }

    // -----------------------------------------------------------------------
    // windowStart > 0 — results must all have fret >= windowStart
    // -----------------------------------------------------------------------

    @Test
    fun `windowStart greater than 0 produces only frets at or above windowStart`() {
        // E (semitone 4) at windowStart=5: window covers frets 5..10.
        // String 0 (4): fret 0 → excluded (below window); next fret=12 → excluded (above)
        // String 1 (11): fret 5  → (11+5)%12=4 ✓
        // String 2 (7):  fret 9  → (7+9)%12=4  ✓
        // String 3 (2):  fret 2  → excluded; next fret=14 → excluded
        // String 4 (9):  fret 7  → (9+7)%12=4  ✓
        // String 5 (4):  fret 0  → excluded; next fret=12 → excluded
        val windowStart = 5
        val positions = FretboardLocator.locateNote(noteSemitone = 4, windowStart = windowStart)
        assertTrue(positions.isNotEmpty(), "Expected at least one result for E in window 5..10")
        assertTrue(positions.all { it.fret >= windowStart },
            "All frets must be >= windowStart ($windowStart), got $positions")
        assertEquals(
            setOf(FretPosition(1, 5), FretPosition(2, 9), FretPosition(4, 7)),
            positions.toSet()
        )
    }

    @Test
    fun `windowStart greater than 0 does not include fret 0 matches`() {
        // E open strings (string 0 and string 5) are at fret 0; with windowStart=1
        // those fret-0 matches must be absent.
        val positions = FretboardLocator.locateNote(noteSemitone = 4, windowStart = 1)
        assertTrue(FretPosition(0, 0) !in positions,
            "Fret 0 on string 0 must not appear when windowStart=1")
        assertTrue(FretPosition(5, 0) !in positions,
            "Fret 0 on string 5 must not appear when windowStart=1")
    }

    // -----------------------------------------------------------------------
    // Semitone modulo normalisation — caller may pass values >= 12
    // -----------------------------------------------------------------------

    @Test
    fun `noteSemitone values greater than 11 are normalised by mod 12`() {
        // semitone 12 == semitone 0 (C)
        val positions12 = FretboardLocator.locateNote(noteSemitone = 12, windowStart = 0)
        val positions0  = FretboardLocator.locateNote(noteSemitone = 0,  windowStart = 0)
        assertEquals(positions0.toSet(), positions12.toSet(),
            "semitone 12 should resolve identically to semitone 0")
    }

    // -----------------------------------------------------------------------
    // semitoneAt
    // -----------------------------------------------------------------------

    @Test
    fun `semitoneAt open strings match OPEN_SEMITONES`() {
        FretboardLocator.OPEN_SEMITONES.forEachIndexed { string, expected ->
            assertEquals(expected, FretboardLocator.semitoneAt(string, 0),
                "Open semitone mismatch for string $string")
        }
    }

    @Test
    fun `semitoneAt advances by 1 semitone per fret`() {
        // High E (string 0): open = 4 (E), fret 1 = 5 (F), fret 2 = 6 (F#)
        assertEquals(5, FretboardLocator.semitoneAt(0, 1))
        assertEquals(6, FretboardLocator.semitoneAt(0, 2))
    }

    @Test
    fun `semitoneAt wraps at 12`() {
        // G string (string 2): open = 7. 7 + 5 = 12 → 0 (C)
        assertEquals(0, FretboardLocator.semitoneAt(2, 5))
    }

    // -----------------------------------------------------------------------
    // notesForChord
    // -----------------------------------------------------------------------

    @Test
    fun `notesForChord returns positions for both third and seventh`() {
        // G7 at position 1: third = B (semitone 11), seventh = F (semitone 5)
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
        val notes = FretboardLocator.notesForChord(g7, playingPosition = 1)

        val thirds   = notes.filter { it.tone == Chord.Tone._3rd }
        val sevenths = notes.filter { it.tone == Chord.Tone._7th }

        assertTrue(thirds.isNotEmpty(),   "Expected at least one third position")
        assertTrue(sevenths.isNotEmpty(), "Expected at least one seventh position")
        thirds.forEach   { assertAll({ assertEquals("B", it.noteName) }, { assertEquals(11, it.semitone) }) }
        sevenths.forEach { assertAll({ assertEquals("F", it.noteName) }, { assertEquals(5,  it.semitone) }) }
    }

    @Test
    fun `notesForChord positions match locateNote for the same window`() {
        // Dm7 at position 3: third = F (5), seventh = C (0)
        val dm7 = ResolvedChord(
            romanNumeral = "ii",
            chord = Chord.Min7,
            symbol = "Dm7",
            tones = mapOf(
                Chord.Tone._root to "D",
                Chord.Tone._3rd  to "F",
                Chord.Tone._5th  to "A",
                Chord.Tone._7th  to "C",
            ),
        )
        val playingPosition = 3
        val windowStart = playingPosition - 1
        val notes = FretboardLocator.notesForChord(dm7, playingPosition)

        val expectedThirdPositions   = FretboardLocator.locateNote(5, windowStart).toSet()
        val expectedSeventhPositions = FretboardLocator.locateNote(0, windowStart).toSet()
        val actualThirdPositions     = notes.filter { it.tone == Chord.Tone._3rd   }.map { it.position }.toSet()
        val actualSeventhPositions   = notes.filter { it.tone == Chord.Tone._7th }.map { it.position }.toSet()

        assertEquals(expectedThirdPositions,   actualThirdPositions,   "Third positions should match locateNote")
        assertEquals(expectedSeventhPositions, actualSeventhPositions, "Seventh positions should match locateNote")
    }

    // -----------------------------------------------------------------------
    // OPEN_SEMITONES constant sanity check
    // -----------------------------------------------------------------------

    @Test
    fun `OPEN_SEMITONES has six entries matching standard tuning`() {
        // Standard tuning from high to low: E(4), B(11), G(7), D(2), A(9), E(4)
        val expected = intArrayOf(4, 11, 7, 2, 9, 4)
        assertEquals(6, FretboardLocator.OPEN_SEMITONES.size)
        expected.forEachIndexed { i, v ->
            assertEquals(v, FretboardLocator.OPEN_SEMITONES[i], "OPEN_SEMITONES[$i]")
        }
    }
}
