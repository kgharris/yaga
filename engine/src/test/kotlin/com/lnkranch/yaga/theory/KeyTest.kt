package com.lnkranch.yaga.theory

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import java.util.stream.IntStream

class KeyTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    fun `key has correct identity`(
        label: String,
        key: Key,
        expectedName: String,
        expectedMode: Mode,
    ) {
        assertAll(
            { assertEquals(expectedName,          key.tonicName,        "$label: tonicName") },
            { assertEquals(expectedMode,          key.mode,             "$label: mode") },
            { assertEquals(expectedMode.toString(), key.mode.toString(), "$label: mode.toString()") },
        )
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scaleCases")
    fun `diatonicSemitone returns correct offset for each degree`(
        label: String,
        key: Key,
        degree: Int,
        expectedSemitone: Int,
    ) {
        assertEquals(expectedSemitone, key.diatonicSemitone(degree), label)
    }

    companion object {
        //                     C major:  I   II  III IV   V   VI  VII
        private val MAJOR_OFFSETS = intArrayOf(0, 2, 4, 5, 7, 9, 11)
        //                     A minor:  i   ii  III iv   v   VI  VII
        private val MINOR_OFFSETS = intArrayOf(0, 2, 3, 5, 7, 8, 10)

        @JvmStatic
        fun scaleCases(): Stream<Arguments> = IntStream.rangeClosed(1, 7).boxed().flatMap { deg ->
            Stream.of(
                Arguments.of("C major deg $deg", Key.C_MAJOR, deg, MAJOR_OFFSETS[deg - 1]),
                Arguments.of("A minor deg $deg", Key.A_MINOR, deg, MINOR_OFFSETS[deg - 1]),
            )
        }

        @JvmStatic
        fun cases(): Stream<Arguments> = Stream.of(
            // Spot-check across the circle of fifths — one flat, one sharp, enharmonic pair
            Arguments.of("C major",  Key.C_MAJOR,  "C",  Mode.Major),
            Arguments.of("G major",  Key.G_MAJOR,  "G",  Mode.Major),
            Arguments.of("F# major", Key.FS_MAJOR, "F#", Mode.Major),
            Arguments.of("Gb major", Key.GB_MAJOR, "Gb", Mode.Major),
            Arguments.of("Cb major", Key.CB_MAJOR, "Cb", Mode.Major),
            Arguments.of("A minor",  Key.A_MINOR,  "A",  Mode.Minor),
            Arguments.of("E minor",  Key.E_MINOR,  "E",  Mode.Minor),
            Arguments.of("Eb minor", Key.EB_MINOR, "Eb", Mode.Minor),
        )
    }
}
