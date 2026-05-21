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
        expectedFifth: String,
        expectedSeventh: String,
    ) {
        val result = engine.resolve(key, degree)
        assertAll(
            { assertEquals(expectedSymbol,  result.symbol,             "$label: symbol") },
            { assertEquals(expectedRoot,    result[Chord.Tone._root],  "$label: root") },
            { assertEquals(expectedThird,   result[Chord.Tone._3rd],   "$label: third") },
            { assertEquals(expectedFifth,   result[Chord.Tone._5th],   "$label: fifth") },
            { assertEquals(expectedSeventh, result[Chord.Tone._7th],   "$label: seventh") },
        )
    }

    @Test
    fun `querying absent tone returns null`() {
        val result = engine.resolve(Key.C_MAJOR, RomanChord.I)
        assertNull(result[Chord.Tone._9th])
    }

    @Test
    fun `resolved chord convenience properties match tones`() {
        val result = engine.resolve(Key.C_MAJOR, RomanChord.II)
        assertAll(
            { assertEquals("D", result.root,    "root convenience property") },
            { assertEquals("F", result.third,   "third convenience property") },
            { assertEquals("C", result.seventh, "seventh convenience property") },
        )
    }

    @Test
    fun `resolved chord romanNumeral and chord fields are correct`() {
        val result = engine.resolve(Key.C_MAJOR, RomanChord.I)
        assertAll(
            { assertEquals("I",        result.romanNumeral, "romanNumeral") },
            { assertEquals(Chord.Maj7, result.chord,        "chord") },
        )
    }

    @Test
    fun `resolved chord tones map is accessible and contains expected entries`() {
        val result = engine.resolve(Key.C_MAJOR, RomanChord.II)
        val tones = result.tones
        assertAll(
            { assertEquals("D", tones[Chord.Tone._root], "tones map root") },
            { assertEquals("F", tones[Chord.Tone._3rd],  "tones map third") },
            { assertEquals("C", tones[Chord.Tone._7th],  "tones map seventh") },
        )
    }

    @Test
    fun `resolved chord convenience properties return empty string when tone absent`() {
        val sparse = ResolvedChord(
            romanNumeral = "I",
            chord        = Chord.Maj7,
            symbol       = "C△7",
            tones        = emptyMap(),
        )
        assertAll(
            { assertEquals("", sparse.root,    "root absent → empty string") },
            { assertEquals("", sparse.third,   "third absent → empty string") },
            { assertEquals("", sparse.seventh, "seventh absent → empty string") },
        )
    }

    companion object {

        // ── Test-only keys for double-accidental coverage ─────────────────────
        //
        // These keys are not on the standard circle of fifths. They exist solely
        // to drive spellTone() into diff=2 (##) and diff=9/10 (bb/bbb) paths,
        // which are unreachable with the 30 canonical keys and current RomanChord set.

        // B# major (enharmonic C major, spelled sharply): gives D##, F##, A## on I△7.
        private val KEY_BS_MAJOR = Key(
            tonicName = "B#", mode = Mode.Major, tonicSemitone = 0,
            spelling = mapOf(
                0 to "B#", 1 to "C#", 2 to "D",  3 to "D#", 4 to "E",
                5 to "E#", 6 to "F#", 7 to "G",  8 to "G#", 9 to "A",
                10 to "A#", 11 to "B",
            ),
        )

        // Variant Bb major where the #IV semitone (4) is spelled Fb instead of E.
        // sharpIVdim7 resolves to Fb°7 with Abb, Cbb, Ebbb as chord tones.
        private val KEY_FB_CONTEXT = Key(
            tonicName = "Bb", mode = Mode.Major, tonicSemitone = 10,
            spelling = mapOf(
                0 to "C",  1 to "Db", 2 to "D",  3 to "Eb", 4 to "Fb",
                5 to "F",  6 to "Gb", 7 to "G",  8 to "Ab", 9 to "A",
                10 to "Bb", 11 to "Cb",
            ),
        )

        @JvmStatic
        fun cases(): Stream<Arguments> = Stream.of(

            // ── C major: all 7 diatonic degrees ──────────────────────────────
            row("C maj I",    Key.C_MAJOR, RomanChord.I,      "C△7",  "C", "E",  "G", "B"),
            row("C maj ii",   Key.C_MAJOR, RomanChord.II,     "Dm7",  "D", "F",  "A", "C"),
            row("C maj iii",  Key.C_MAJOR, RomanChord.III,    "Em7",  "E", "G",  "B", "D"),
            row("C maj IV",   Key.C_MAJOR, RomanChord.IV,     "F△7",  "F", "A",  "C", "E"),
            row("C maj V",    Key.C_MAJOR, RomanChord.V,      "G7",   "G", "B",  "D", "F"),
            row("C maj vi",   Key.C_MAJOR, RomanChord.VI,     "Am7",  "A", "C",  "E", "G"),
            row("C maj vii°", Key.C_MAJOR, RomanChord.VIIDIM, "Bø7",  "B", "D",  "F", "A"),

            // ── Sharp key: G major V — verifies F# spelling ───────────────────
            row("G maj V",    Key.G_MAJOR, RomanChord.V,      "D7",   "D", "F#", "A",  "C"),

            // ── Flat key: Bb major ii — verifies Eb/Bb spelling ──────────────
            row("Bb maj ii",  Key.BB_MAJOR, RomanChord.II,    "Cm7",  "C", "Eb", "G",  "Bb"),

            // ── F# major: verifies E# spelling ───────────────────────────────
            row("F# maj V",   Key.FS_MAJOR, RomanChord.V,     "C#7",  "C#","E#", "G#", "B"),

            // ── Gb major: verifies Cb spelling ───────────────────────────────
            row("Gb maj V",   Key.GB_MAJOR, RomanChord.V,     "Db7",  "Db","F",  "Ab", "Cb"),

            // ── A minor: all 7 diatonic degrees ──────────────────────────────
            row("A min i",    Key.A_MINOR, RomanChord.i,      "Am7",  "A", "C",  "E",  "G"),
            row("A min ii°",  Key.A_MINOR, RomanChord.iiDim,  "Bø7",  "B", "D",  "F",  "A"),
            row("A min III",  Key.A_MINOR, RomanChord.bIII,   "C△7",  "C", "E",  "G",  "B"),
            row("A min iv",   Key.A_MINOR, RomanChord.iv,     "Dm7",  "D", "F",  "A",  "C"),
            row("A min v",    Key.A_MINOR, RomanChord.v,      "Em7",  "E", "G",  "B",  "D"),
            row("A min VI",   Key.A_MINOR, RomanChord.bVI,    "F△7",  "F", "A",  "C",  "E"),
            row("A min VII",  Key.A_MINOR, RomanChord.bVII,   "G7",   "G", "B",  "D",  "F"),

            // ── E minor: verifies F# in minor key context ─────────────────────
            row("E min VII",  Key.E_MINOR, RomanChord.bVII,   "D7",   "D", "F#", "A",  "C"),

            // ── Eb minor: verifies Cb spelling in minor context ───────────────
            row("Eb min iv",  Key.EB_MINOR, RomanChord.iv,    "Abm7", "Ab","Cb", "Eb", "Gb"),

            // ── VI7 in minor: verifies degree-6 resolves from the minor scale ──
            row("A min VI7",      Key.A_MINOR,  RomanChord.VI7,         "F7",   "F",  "A",  "C",  "Eb"),

            // ── Blues / chromatic: G major as reference key ───────────────────
            row("G maj I7",       Key.G_MAJOR,  RomanChord.I7,          "G7",   "G",  "B",  "D",  "F"),
            row("G maj IV7",      Key.G_MAJOR,  RomanChord.IV7,         "C7",   "C",  "E",  "G",  "Bb"),
            row("G maj V7",       Key.G_MAJOR,  RomanChord.V7,          "D7",   "D",  "F#", "A",  "C"),
            row("G maj VI7",      Key.G_MAJOR,  RomanChord.VI7,         "E7",   "E",  "G#", "B",  "D"),
            row("G maj ♭VII7",    Key.G_MAJOR,  RomanChord.bVII7,       "F7",   "F",  "A",  "C",  "Eb"),
            row("G maj #IV°7",    Key.G_MAJOR,  RomanChord.sharpIVdim7, "C#°7", "C#", "E",  "G",  "Bb"),

            // ── Double-accidental cases: exercises ## and bb paths in spellTone ─
            //
            // B# as tonic forces diff=2 on every chord tone of the Maj7 quality.
            row("B# maj I (##)",  KEY_BS_MAJOR,   RomanChord.I,           "B#△7", "B#", "D##", "F##", "A##"),
            //
            // Fb as root (sharpIVdim7 in the custom Bb-spelled key) forces diff=10
            // on the 5th (Cbb) and diff=9 on the 7th (Ebbb).
            row("Fb #IV°7 (bb)",  KEY_FB_CONTEXT, RomanChord.sharpIVdim7, "Fb°7", "Fb", "Abb", "Cbb", "Ebbb"),
        )

        private fun row(
            label: String, key: Key, degree: RomanChord,
            symbol: String, root: String, third: String, fifth: String, seventh: String,
        ): Arguments = Arguments.of(label, key, degree, symbol, root, third, fifth, seventh)
    }
}
