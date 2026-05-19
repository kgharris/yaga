package com.lnkranch.yaga.theory

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class ChordQualityTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    fun `chord quality has correct tones`(
        label: String,
        quality: ChordQuality,
        expectedSymbol: String,
        expectedRoot: Int,
        expectedThird: Int,
        expectedFifth: Int,
        expectedSeventh: Int,
    ) {
        assertAll(
            { assertEquals(expectedSymbol,   quality.symbol,                        "$label: symbol") },
            { assertEquals(expectedRoot,     quality[ChordQuality.Tone._root],      "$label: root") },
            { assertEquals(expectedThird,    quality[ChordQuality.Tone._3rd],       "$label: 3rd") },
            { assertEquals(expectedFifth,    quality[ChordQuality.Tone._5th],       "$label: 5th") },
            { assertEquals(expectedSeventh,  quality[ChordQuality.Tone._7th],       "$label: 7th") },
            { assertNull(                    quality[ChordQuality.Tone._9th],       "$label: 9th absent") },
        )
    }

    companion object {
        @JvmStatic
        fun cases(): Stream<Arguments> = Stream.of(
            //          label         quality                  symbol  root  3rd  5th   7th
            Arguments.of("Maj7",     ChordQuality.Maj7,     "Maj7",   0,   4,   7,   11),
            Arguments.of("Min7",     ChordQuality.Min7,     "m7",     0,   3,   7,   10),
            Arguments.of("Dom7",     ChordQuality.Dom7,     "7",      0,   4,   7,   10),
            Arguments.of("HalfDim7", ChordQuality.HalfDim7, "ø7",    0,   3,   6,   10),
            Arguments.of("FullDim7", ChordQuality.FullDim7, "°7",    0,   3,   6,    9),
        )
    }
}
