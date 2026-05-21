package com.lnkranch.yaga.domain

import com.lnkranch.yaga.theory.Key
import com.lnkranch.yaga.theory.Mode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class KeyLookupTest {

    @Test
    fun `findKey returns correct major key`() {
        assertEquals(Key.G_MAJOR, findKey("G", Mode.Major))
    }

    @Test
    fun `findKey returns correct minor key`() {
        assertEquals(Key.D_MINOR, findKey("D", Mode.Minor))
    }

    @Test
    fun `findKey returns null for unknown tonic`() {
        assertNull(findKey("X", Mode.Major))
    }

    @Test
    fun `TONIC_NAMES contains all 12 chromatic pitch classes`() {
        assertEquals(12, TONIC_NAMES.size)
        assertTrue("C"  in TONIC_NAMES)
        assertTrue("F#" in TONIC_NAMES)
        assertTrue("Bb" in TONIC_NAMES)
    }
}
