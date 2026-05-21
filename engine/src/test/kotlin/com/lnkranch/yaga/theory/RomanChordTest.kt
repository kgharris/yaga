package com.lnkranch.yaga.theory

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class RomanChordTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    fun `scale degree has correct properties`(
        label: String,
        degree: RomanChord,
        expectedDegree: Int,
        expectedAlteration: Int,
        expectedChord: Chord,
        expectedRomanNumeral: String,
    ) {
        assertAll(
            { assertEquals(expectedDegree,       degree.degree,       "$label: degree") },
            { assertEquals(expectedAlteration,   degree.alteration,   "$label: alteration") },
            { assertEquals(expectedChord,        degree.chord,        "$label: chord") },
            { assertEquals(expectedRomanNumeral, degree.romanNumeral, "$label: romanNumeral") },
        )
    }

    companion object {
        @JvmStatic
        fun cases(): Stream<Arguments> = Stream.of(
            //          label            degree                    deg  alt  chord                  romanNumeral
            // ── Diatonic major ──────────────────────────────────────────────────────────────────────
            Arguments.of("I",            RomanChord.I,          1, 0,  Chord.Maj7,     "I"),
            Arguments.of("ii",           RomanChord.II,         2, 0,  Chord.Min7,     "ii"),
            Arguments.of("iii",          RomanChord.III,        3, 0,  Chord.Min7,     "iii"),
            Arguments.of("IV",           RomanChord.IV,         4, 0,  Chord.Maj7,     "IV"),
            Arguments.of("V",            RomanChord.V,          5, 0,  Chord.Dom7,     "V"),
            Arguments.of("vi",           RomanChord.VI,         6, 0,  Chord.Min7,     "vi"),
            Arguments.of("vii°",         RomanChord.VIIDIM,     7, 0,  Chord.HalfDim7, "vii°"),
            // ── Diatonic minor ──────────────────────────────────────────────────────────────────────
            Arguments.of("i",            RomanChord.i,          1, 0,  Chord.Min7,     "i"),
            Arguments.of("ii°",          RomanChord.iiDim,      2, 0,  Chord.HalfDim7, "ii°"),
            Arguments.of("III",          RomanChord.bIII,       3, 0,  Chord.Maj7,     "III"),
            Arguments.of("iv",           RomanChord.iv,         4, 0,  Chord.Min7,     "iv"),
            Arguments.of("v",            RomanChord.v,          5, 0,  Chord.Min7,     "v"),
            Arguments.of("VI",           RomanChord.bVI,        6, 0,  Chord.Maj7,     "VI"),
            Arguments.of("VII",          RomanChord.bVII,       7, 0,  Chord.Dom7,     "VII"),
            // ── Blues / chromatic ───────────────────────────────────────────────────────────────────
            Arguments.of("I7",           RomanChord.I7,         1, 0,  Chord.Dom7,     "I7"),
            Arguments.of("IV7",          RomanChord.IV7,        4, 0,  Chord.Dom7,     "IV7"),
            Arguments.of("V7",           RomanChord.V7,         5, 0,  Chord.Dom7,     "V7"),
            Arguments.of("VI7",          RomanChord.VI7,        6, 0,  Chord.Dom7,     "VI7"),
            Arguments.of("♭VII7",        RomanChord.bVII7,      7, -1, Chord.Dom7,     "♭VII7"),
            Arguments.of("#IV°7",        RomanChord.sharpIVdim7,4, +1, Chord.FullDim7, "#IV°7"),
        )
    }
}
