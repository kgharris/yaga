package com.lnkranch.yaga.theory

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

/**
 * Verifies [Key.spell] for every canonical key context.
 *
 * Each row: (label, key, semitone, expected name).
 * Coverage targets per key:
 *   - Tonic note spelled correctly
 *   - At least one accidental note (sharp or flat)
 *   - One natural note that sits adjacent to an accidental (guard against over-application)
 * Special edge cases: E# (6-7 sharps), B# (7 sharps), Cb (6-7 flats), Fb (7 flats).
 */
class NoteSpellingTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    fun `spells semitone correctly in key`(
        label: String,
        key: Key,
        semitone: Int,
        expected: String,
    ) {
        assertEquals(expected, key.spell(semitone))
    }

    companion object {
        @JvmStatic
        fun cases(): Stream<Arguments> = Stream.of(

            // ── C major (0 acc) ────────────────────────────────────────────────
            row("C maj: tonic C",         Key.C_MAJOR,   0,  "C"),
            row("C maj: E natural",       Key.C_MAJOR,   4,  "E"),
            row("C maj: chromatic → Bb",  Key.C_MAJOR,  10,  "Bb"),

            // ── G major (1#: F#) ───────────────────────────────────────────────
            row("G maj: tonic G",         Key.G_MAJOR,   7,  "G"),
            row("G maj: F# accidental",   Key.G_MAJOR,   6,  "F#"),
            row("G maj: F natural guard", Key.G_MAJOR,   5,  "F"),

            // ── D major (2#: F#, C#) ───────────────────────────────────────────
            row("D maj: tonic D",         Key.D_MAJOR,   2,  "D"),
            row("D maj: C# accidental",   Key.D_MAJOR,   1,  "C#"),
            row("D maj: C natural guard", Key.D_MAJOR,   0,  "C"),

            // ── A major (3#: F#, C#, G#) ───────────────────────────────────────
            row("A maj: tonic A",         Key.A_MAJOR,   9,  "A"),
            row("A maj: G# accidental",   Key.A_MAJOR,   8,  "G#"),
            row("A maj: G natural guard", Key.A_MAJOR,   7,  "G"),

            // ── E major (4#: F#, C#, G#, D#) ──────────────────────────────────
            row("E maj: tonic E",         Key.E_MAJOR,   4,  "E"),
            row("E maj: D# accidental",   Key.E_MAJOR,   3,  "D#"),
            row("E maj: D natural guard", Key.E_MAJOR,   2,  "D"),

            // ── B major (5#: F#, C#, G#, D#, A#) ─────────────────────────────
            row("B maj: tonic B",         Key.B_MAJOR,  11,  "B"),
            row("B maj: A# accidental",   Key.B_MAJOR,  10,  "A#"),
            row("B maj: A natural guard", Key.B_MAJOR,   9,  "A"),

            // ── F# major (6#: F#, C#, G#, D#, A#, E#) ────────────────────────
            row("F# maj: tonic F#",       Key.FS_MAJOR,  6,  "F#"),
            row("F# maj: E# (was F)",     Key.FS_MAJOR,  5,  "E#"),  // special
            row("F# maj: B natural",      Key.FS_MAJOR, 11,  "B"),

            // ── C# major (7#: all + B#) ───────────────────────────────────────
            row("C# maj: tonic C#",       Key.CS_MAJOR,  1,  "C#"),
            row("C# maj: E# (was F)",     Key.CS_MAJOR,  5,  "E#"),  // special
            row("C# maj: B# (was C)",     Key.CS_MAJOR,  0,  "B#"),  // special

            // ── F major (1b: Bb) ───────────────────────────────────────────────
            row("F maj: tonic F",         Key.F_MAJOR,   5,  "F"),
            row("F maj: Bb accidental",   Key.F_MAJOR,  10,  "Bb"),
            row("F maj: B natural guard", Key.F_MAJOR,  11,  "B"),

            // ── Bb major (2b: Bb, Eb) ─────────────────────────────────────────
            row("Bb maj: tonic Bb",       Key.BB_MAJOR, 10,  "Bb"),
            row("Bb maj: Eb accidental",  Key.BB_MAJOR,  3,  "Eb"),
            row("Bb maj: E natural guard",Key.BB_MAJOR,  4,  "E"),

            // ── Eb major (3b: Bb, Eb, Ab) ─────────────────────────────────────
            row("Eb maj: tonic Eb",       Key.EB_MAJOR,  3,  "Eb"),
            row("Eb maj: Ab accidental",  Key.EB_MAJOR,  8,  "Ab"),
            row("Eb maj: A natural guard",Key.EB_MAJOR,  9,  "A"),

            // ── Ab major (4b: Bb, Eb, Ab, Db) ────────────────────────────────
            row("Ab maj: tonic Ab",       Key.AB_MAJOR,  8,  "Ab"),
            row("Ab maj: Db accidental",  Key.AB_MAJOR,  1,  "Db"),
            row("Ab maj: D natural guard",Key.AB_MAJOR,  2,  "D"),

            // ── Db major (5b: Bb, Eb, Ab, Db, Gb) ────────────────────────────
            row("Db maj: tonic Db",       Key.DB_MAJOR,  1,  "Db"),
            row("Db maj: Gb accidental",  Key.DB_MAJOR,  6,  "Gb"),
            row("Db maj: G natural guard",Key.DB_MAJOR,  7,  "G"),

            // ── Gb major (6b: Bb, Eb, Ab, Db, Gb, Cb) ────────────────────────
            row("Gb maj: tonic Gb",       Key.GB_MAJOR,  6,  "Gb"),
            row("Gb maj: Cb (was B)",     Key.GB_MAJOR, 11,  "Cb"),  // special
            row("Gb maj: F natural",      Key.GB_MAJOR,  5,  "F"),

            // ── Cb major (7b: all + Fb) ───────────────────────────────────────
            row("Cb maj: tonic Cb",       Key.CB_MAJOR, 11,  "Cb"),  // special
            row("Cb maj: Fb (was E)",     Key.CB_MAJOR,  4,  "Fb"),  // special
            row("Cb maj: Gb",             Key.CB_MAJOR,  6,  "Gb"),

            // ── A minor (0 acc) ────────────────────────────────────────────────
            row("A min: tonic A",         Key.A_MINOR,   9,  "A"),
            row("A min: G natural",       Key.A_MINOR,   7,  "G"),
            row("A min: chromatic → Bb",  Key.A_MINOR,  10,  "Bb"),

            // ── E minor (1#: F#) ───────────────────────────────────────────────
            row("E min: tonic E",         Key.E_MINOR,   4,  "E"),
            row("E min: F# accidental",   Key.E_MINOR,   6,  "F#"),
            row("E min: F natural guard", Key.E_MINOR,   5,  "F"),

            // ── B minor (2#: F#, C#) ──────────────────────────────────────────
            row("B min: tonic B",         Key.B_MINOR,  11,  "B"),
            row("B min: C# accidental",   Key.B_MINOR,   1,  "C#"),

            // ── F# minor (3#: F#, C#, G#) ────────────────────────────────────
            row("F# min: tonic F#",       Key.FS_MINOR,  6,  "F#"),
            row("F# min: G# accidental",  Key.FS_MINOR,  8,  "G#"),

            // ── C# minor (4#: F#, C#, G#, D#) ───────────────────────────────
            row("C# min: tonic C#",       Key.CS_MINOR,  1,  "C#"),
            row("C# min: D# accidental",  Key.CS_MINOR,  3,  "D#"),

            // ── G# minor (5#: F#, C#, G#, D#, A#) ───────────────────────────
            row("G# min: tonic G#",       Key.GS_MINOR,  8,  "G#"),
            row("G# min: A# accidental",  Key.GS_MINOR, 10,  "A#"),

            // ── D# minor (6#: F#, C#, G#, D#, A#, E#) ───────────────────────
            row("D# min: tonic D#",       Key.DS_MINOR,  3,  "D#"),
            row("D# min: E# (was F)",     Key.DS_MINOR,  5,  "E#"),  // special

            // ── A# minor (7#: all + B#) ───────────────────────────────────────
            row("A# min: tonic A#",       Key.AS_MINOR, 10,  "A#"),
            row("A# min: E# (was F)",     Key.AS_MINOR,  5,  "E#"),  // special
            row("A# min: B# (was C)",     Key.AS_MINOR,  0,  "B#"),  // special

            // ── D minor (1b: Bb) ───────────────────────────────────────────────
            row("D min: tonic D",         Key.D_MINOR,   2,  "D"),
            row("D min: Bb accidental",   Key.D_MINOR,  10,  "Bb"),

            // ── G minor (2b: Bb, Eb) ──────────────────────────────────────────
            row("G min: tonic G",         Key.G_MINOR,   7,  "G"),
            row("G min: Eb accidental",   Key.G_MINOR,   3,  "Eb"),

            // ── C minor (3b: Bb, Eb, Ab) ──────────────────────────────────────
            row("C min: tonic C",         Key.C_MINOR,   0,  "C"),
            row("C min: Ab accidental",   Key.C_MINOR,   8,  "Ab"),

            // ── F minor (4b: Bb, Eb, Ab, Db) ─────────────────────────────────
            row("F min: tonic F",         Key.F_MINOR,   5,  "F"),
            row("F min: Db accidental",   Key.F_MINOR,   1,  "Db"),

            // ── Bb minor (5b: Bb, Eb, Ab, Db, Gb) ────────────────────────────
            row("Bb min: tonic Bb",       Key.BB_MINOR, 10,  "Bb"),
            row("Bb min: Gb accidental",  Key.BB_MINOR,  6,  "Gb"),

            // ── Eb minor (6b: Bb, Eb, Ab, Db, Gb, Cb) ────────────────────────
            row("Eb min: tonic Eb",       Key.EB_MINOR,  3,  "Eb"),
            row("Eb min: Cb (was B)",     Key.EB_MINOR, 11,  "Cb"),  // special
            row("Eb min: B natural guard",Key.EB_MINOR,  4,  "E"),

            // ── Ab minor (7b: all + Fb) ───────────────────────────────────────
            row("Ab min: tonic Ab",       Key.AB_MINOR,  8,  "Ab"),
            row("Ab min: Cb (was B)",     Key.AB_MINOR, 11,  "Cb"),  // special
            row("Ab min: Fb (was E)",     Key.AB_MINOR,  4,  "Fb"),  // special
        )

        private fun row(label: String, key: Key, semitone: Int, expected: String): Arguments =
            Arguments.of(label, key, semitone, expected)
    }
}
