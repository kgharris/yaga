package com.lnkranch.yaga.theory

import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ProgressionTest {

    @Test
    fun `Progression stores name and chords`() {
        val chords = listOf(RomanChord.II, RomanChord.V, RomanChord.I)
        val p = Progression("ii V I", chords)
        assertEquals("ii V I", p.name)
        assertEquals(chords, p.chords)
    }

    @Test
    fun `round-trip through JSON preserves name and chords`() {
        val p = Progression("ii V I", listOf(RomanChord.II, RomanChord.V, RomanChord.I))
        assertEquals(p, Json.decodeFromString<Progression>(Json.encodeToString(p)))
    }

    @Test
    fun `serialized JSON uses romanNumeral strings`() {
        val p = Progression("ii V I", listOf(RomanChord.II, RomanChord.V, RomanChord.I))
        val json = Json.encodeToString(p)
        assertTrue("\"ii\"" in json)
        assertTrue("\"V\"" in json)
        assertTrue("\"I\"" in json)
    }

    @Test
    fun `round-trip preserves special-character roman numerals`() {
        val p = Progression("Jazz", listOf(RomanChord.sharpIVdim7, RomanChord.bVII7))
        val json = Json.encodeToString(p)
        assertTrue("\"#IV°7\"" in json)
        assertTrue("\"♭VII7\"" in json)
        assertEquals(p, Json.decodeFromString<Progression>(json))
    }

    @Test
    fun `deserializing unknown roman numeral throws`() {
        val json = """{"name":"test","chords":["Xm7"]}"""
        assertThrows<SerializationException> { Json.decodeFromString<Progression>(json) }
    }
}
