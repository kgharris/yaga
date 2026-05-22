package com.lnkranch.yaga.domain

import com.lnkranch.yaga.theory.Chord
import com.lnkranch.yaga.theory.FretPosition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DrillDomainTest {

    // ── DrillMode ─────────────────────────────────────────────────────────────

    @Test
    fun `DrillMode valueOf round-trips for both values`() {
        assertEquals(DrillMode.Normal,  DrillMode.valueOf("Normal"))
        assertEquals(DrillMode.Reverse, DrillMode.valueOf("Reverse"))
    }

    @Test
    fun `DrillMode values are distinct`() {
        assertNotEquals(DrillMode.Normal, DrillMode.Reverse)
    }

    // ── DrillInputMode ────────────────────────────────────────────────────────

    @Test
    fun `DrillInputMode valueOf round-trips for both values`() {
        assertEquals(DrillInputMode.Buttons,  DrillInputMode.valueOf("Buttons"))
        assertEquals(DrillInputMode.Fretboard, DrillInputMode.valueOf("Fretboard"))
    }

    @Test
    fun `DrillInputMode values are distinct`() {
        assertNotEquals(DrillInputMode.Buttons, DrillInputMode.Fretboard)
    }

    // ── FretDot ───────────────────────────────────────────────────────────────

    private val samplePos = FretPosition(string = 0, fret = 3)
    private val sampleDot = FretDot(position = samplePos, label = "F", intervalFromRoot = 5, semitone = 5)

    @Test
    fun `FretDot equal copy has same hashCode`() {
        val copy = sampleDot.copy()
        assertEquals(sampleDot, copy)
        assertEquals(sampleDot.hashCode(), copy.hashCode())
    }

    @Test
    fun `FretDot same reference is equal`() {
        assertEquals(sampleDot, sampleDot)
    }

    @Test
    fun `FretDot is not equal to different type`() {
        assertFalse(sampleDot.equals("not a FretDot"))
    }

    @Test
    fun `FretDot inequality when each field differs`() {
        assertNotEquals(sampleDot, sampleDot.copy(position = FretPosition(1, 3)))
        assertNotEquals(sampleDot, sampleDot.copy(label = "F#"))
        assertNotEquals(sampleDot, sampleDot.copy(intervalFromRoot = 6))
        assertNotEquals(sampleDot, sampleDot.copy(semitone = 6))
    }

    @Test
    fun `FretDot with null intervalFromRoot is equal to copy with null`() {
        val a = FretDot(samplePos, "A", null, 9)
        val b = FretDot(samplePos, "A", null, 9)
        assertEquals(a, b)
    }

    @Test
    fun `FretDot null intervalFromRoot not equal to non-null`() {
        val withNull    = FretDot(samplePos, "A", null, 9)
        val withNonNull = FretDot(samplePos, "A", 9,    9)
        assertNotEquals(withNull, withNonNull)
        assertNotEquals(withNonNull, withNull)
    }

    @Test
    fun `FretDot toString contains label and semitone`() {
        val s = sampleDot.toString()
        assertTrue(s.contains("F"))
        assertTrue(s.contains("5"))
    }

    @Test
    fun `FretDot properties are accessible via getter`() {
        assertEquals(samplePos, sampleDot.position)
        assertEquals("F", sampleDot.label)
        assertEquals(5, sampleDot.intervalFromRoot)
        assertEquals(5, sampleDot.semitone)
    }

    @Test
    fun `FretDot component functions destructure correctly`() {
        val (pos, label, interval, sem) = sampleDot
        assertEquals(samplePos, pos)
        assertEquals("F", label)
        assertEquals(5, interval)
        assertEquals(5, sem)
    }

    // ── FretboardNote ─────────────────────────────────────────────────────────

    private val notePos  = FretPosition(string = 1, fret = 2)
    private val baseNote = FretboardNote(notePos, "C", 0, Chord.Tone._3rd, false)

    @Test
    fun `FretboardNote equal copy has same hashCode`() {
        val copy = baseNote.copy()
        assertEquals(baseNote, copy)
        assertEquals(baseNote.hashCode(), copy.hashCode())
    }

    @Test
    fun `FretboardNote same reference is equal`() {
        assertEquals(baseNote, baseNote)
    }

    @Test
    fun `FretboardNote is not equal to different type`() {
        assertFalse(baseNote.equals("not a FretboardNote"))
    }

    @Test
    fun `FretboardNote inequality when each field differs`() {
        assertNotEquals(baseNote, baseNote.copy(position = FretPosition(2, 2)))
        assertNotEquals(baseNote, baseNote.copy(noteName = "B"))
        assertNotEquals(baseNote, baseNote.copy(semitone = 11))
        assertNotEquals(baseNote, baseNote.copy(toneRole = Chord.Tone._7th))
        assertNotEquals(baseNote, baseNote.copy(revealed = true))
    }

    @Test
    fun `FretboardNote toString contains noteName`() {
        assertTrue(baseNote.toString().contains("C"))
    }

    @Test
    fun `FretboardNote properties are accessible via getter`() {
        assertEquals(notePos, baseNote.position)
        assertEquals("C", baseNote.noteName)
        assertEquals(0, baseNote.semitone)
        assertEquals(Chord.Tone._3rd, baseNote.toneRole)
        assertFalse(baseNote.revealed)
    }

    @Test
    fun `FretboardNote component functions destructure correctly`() {
        val (pos, name, sem, tone, rev) = baseNote
        assertEquals(notePos, pos)
        assertEquals("C", name)
        assertEquals(0, sem)
        assertEquals(Chord.Tone._3rd, tone)
        assertFalse(rev)
    }
}
