package com.lnkranch.yaga.theory

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class TheoryEngineTest {

    private val engine = TheoryEngine()

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    fun `resolve produces correct chord`(
        label: String,
        key: Key,
        degree: RomanChord,
        expectedSymbol: String,
        expectedRoot: String,
        expectedThird: String,
        expectedSeventh: String,
    ) {
        val result = engine.resolve(key, degree)
        assertAll(
            { assertEquals(expectedSymbol,  result.symbol,                         "$label: symbol") },
            { assertEquals(expectedRoot,    result[ChordQuality.Tone._root],       "$label: root") },
            { assertEquals(expectedThird,   result[ChordQuality.Tone._3rd],        "$label: third") },
            { assertEquals(expectedSeventh, result[ChordQuality.Tone._7th],        "$label: seventh") },
        )
    }

    @Test
    fun `querying absent tone returns null`() {
        val result = engine.resolve(Key.C_MAJOR, RomanChord.I)
        assertNull(result[ChordQuality.Tone._9th])
    }

    companion object {
        @JvmStatic
        fun cases(): Stream<Arguments> = Stream.of(

            // ── C major: all 7 diatonic degrees ──────────────────────────────
            row("C maj I",    Key.C_MAJOR, RomanChord.I,      "CMaj7", "C", "E",  "B"),
            row("C maj ii",   Key.C_MAJOR, RomanChord.II,     "Dm7",   "D", "F",  "C"),
            row("C maj iii",  Key.C_MAJOR, RomanChord.III,    "Em7",   "E", "G",  "D"),
            row("C maj IV",   Key.C_MAJOR, RomanChord.IV,     "FMaj7", "F", "A",  "E"),
            row("C maj V",    Key.C_MAJOR, RomanChord.V,      "G7",    "G", "B",  "F"),
            row("C maj vi",   Key.C_MAJOR, RomanChord.VI,     "Am7",   "A", "C",  "G"),
            row("C maj vii°", Key.C_MAJOR, RomanChord.VIIDIM, "Bø7",   "B", "D",  "A"),

            // ── Sharp key: G major V — verifies F# spelling ───────────────────
            row("G maj V",    Key.G_MAJOR, RomanChord.V,      "D7",    "D", "F#", "C"),

            // ── Flat key: Bb major ii — verifies Eb/Bb spelling ──────────────
            row("Bb maj ii",  Key.BB_MAJOR, RomanChord.II,    "Cm7",   "C", "Eb", "Bb"),

            // ── F# major: verifies E# spelling ───────────────────────────────
            row("F# maj V",   Key.FS_MAJOR, RomanChord.V,     "C#7",   "C#","E#", "B"),

            // ── Gb major: verifies Cb spelling ───────────────────────────────
            row("Gb maj V",   Key.GB_MAJOR, RomanChord.V,     "Db7",   "Db","F",  "Cb"),

            // ── A minor: all 7 diatonic degrees ──────────────────────────────
            row("A min i",    Key.A_MINOR, RomanChord.i,      "Am7",   "A", "C",  "G"),
            row("A min ii°",  Key.A_MINOR, RomanChord.iiDim,  "Bø7",   "B", "D",  "A"),
            row("A min III",  Key.A_MINOR, RomanChord.bIII,   "CMaj7", "C", "E",  "B"),
            row("A min iv",   Key.A_MINOR, RomanChord.iv,     "Dm7",   "D", "F",  "C"),
            row("A min v",    Key.A_MINOR, RomanChord.v,      "Em7",   "E", "G",  "D"),
            row("A min VI",   Key.A_MINOR, RomanChord.bVI,    "FMaj7", "F", "A",  "E"),
            row("A min VII",  Key.A_MINOR, RomanChord.bVII,   "G7",    "G", "B",  "F"),

            // ── E minor: verifies F# in minor key context ─────────────────────
            row("E min VII",  Key.E_MINOR, RomanChord.bVII,   "D7",    "D", "F#", "C"),

            // ── Eb minor: verifies Cb spelling in minor context ───────────────
            row("Eb min iv",  Key.EB_MINOR, RomanChord.iv,    "Abm7",  "Ab","Cb", "Gb"),

            // ── VI7 in minor: verifies degree-6 resolves from the minor scale ──
            row("A min VI7",      Key.A_MINOR, RomanChord.VI7,         "F7",   "F",  "A",  "Eb"),

            // ── Blues / chromatic: G major as reference key ───────────────────
            row("G maj I7",       Key.G_MAJOR, RomanChord.I7,          "G7",   "G",  "B",  "F"),
            row("G maj IV7",      Key.G_MAJOR, RomanChord.IV7,         "C7",   "C",  "E",  "Bb"),
            row("G maj V7",       Key.G_MAJOR, RomanChord.V7,          "D7",   "D",  "F#", "C"),
            row("G maj VI7",      Key.G_MAJOR, RomanChord.VI7,         "E7",   "E",  "G#", "D"),
            row("G maj ♭VII7",    Key.G_MAJOR, RomanChord.bVII7,       "F7",   "F",  "A",  "Eb"),
            row("G maj #IV°7",    Key.G_MAJOR, RomanChord.sharpIVdim7, "C#°7", "C#", "E",  "Bb"),
        )

        private fun row(
            label: String, key: Key, degree: RomanChord,
            symbol: String, root: String, third: String, seventh: String,
        ): Arguments = Arguments.of(label, key, degree, symbol, root, third, seventh)
    }
}
