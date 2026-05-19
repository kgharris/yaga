package com.lnkranch.yaga.theory

/**
 * A fully-specified key context: tonic name, mode, and canonical spelling for
 * all 12 chromatic semitones.
 *
 * [spell] is the single entry point for note-name resolution. All 12 semitones
 * are pre-computed at construction time from the key's accidental set, so there
 * is no per-call branching logic.
 *
 * 30 canonical instances live in the companion object — 15 major, 15 minor,
 * covering the full circle of fifths in both directions including the three
 * enharmonic pairs at the 6-/7-accidental boundary.
 */
data class Key(
    val tonicName: String,
    val mode: Mode,
    val tonicSemitone: Int,
    private val spelling: Map<Int, String>,
) {
    fun spell(semitone: Int): String = spelling.getValue(semitone.mod(12))

    fun diatonicSemitone(degree: Int): Int = when (mode) {
        is Mode.Major -> MAJOR_SCALE[degree - 1]
        is Mode.Minor -> MINOR_SCALE[degree - 1]
    }

    companion object {

        private val MAJOR_SCALE = intArrayOf(0, 2, 4, 5, 7, 9, 11)
        private val MINOR_SCALE = intArrayOf(0, 2, 3, 5, 7, 8, 10)

        // ── Chromatic defaults ────────────────────────────────────────────────
        //
        // Sharp-context default: non-accidental semitones spelled with sharps.
        // Used as the base for all sharp keys (1–7 sharps).
        private val SHARP_DEFAULTS = mapOf(
            0 to "C",  1 to "C#", 2 to "D",  3 to "D#", 4 to "E",  5 to "F",
            6 to "F#", 7 to "G",  8 to "G#", 9 to "A",  10 to "A#", 11 to "B",
        )

        // Flat-context default: non-accidental semitones spelled with flats.
        // Used as the base for all flat keys (0–7 flats; 0 = C major / A minor).
        private val FLAT_DEFAULTS = mapOf(
            0 to "C",  1 to "Db", 2 to "D",  3 to "Eb", 4 to "E",  5 to "F",
            6 to "Gb", 7 to "G",  8 to "Ab", 9 to "A",  10 to "Bb", 11 to "B",
        )

        // ── Accidental order ──────────────────────────────────────────────────
        //
        // Circle-of-fifths sharp order: F# C# G# D# A# E# B#
        // At 6 sharps E# (semitone 5) replaces F; at 7 sharps B# (semitone 0) replaces C.
        private val SHARP_ORDER = listOf(
            6 to "F#", 1 to "C#", 8 to "G#", 3 to "D#",
            10 to "A#", 5 to "E#", 0 to "B#",
        )

        // Circle-of-fifths flat order: Bb Eb Ab Db Gb Cb Fb
        // At 6 flats Cb (semitone 11) replaces B; at 7 flats Fb (semitone 4) replaces E.
        private val FLAT_ORDER = listOf(
            10 to "Bb", 3 to "Eb", 8 to "Ab", 1 to "Db",
            6 to "Gb", 11 to "Cb", 4 to "Fb",
        )

        private fun sharps(n: Int): Map<Int, String> =
            SHARP_DEFAULTS.toMutableMap()
                .also { m -> SHARP_ORDER.take(n).forEach { (s, name) -> m[s] = name } }

        private fun flats(n: Int): Map<Int, String> =
            FLAT_DEFAULTS.toMutableMap()
                .also { m -> FLAT_ORDER.take(n).forEach { (s, name) -> m[s] = name } }

        // ── 15 Major Keys ─────────────────────────────────────────────────────
        val C_MAJOR  = Key("C",  Mode.Major,  0, flats(0))
        val G_MAJOR  = Key("G",  Mode.Major,  7, sharps(1))
        val D_MAJOR  = Key("D",  Mode.Major,  2, sharps(2))
        val A_MAJOR  = Key("A",  Mode.Major,  9, sharps(3))
        val E_MAJOR  = Key("E",  Mode.Major,  4, sharps(4))
        val B_MAJOR  = Key("B",  Mode.Major, 11, sharps(5))
        val FS_MAJOR = Key("F#", Mode.Major,  6, sharps(6))
        val CS_MAJOR = Key("C#", Mode.Major,  1, sharps(7))
        val F_MAJOR  = Key("F",  Mode.Major,  5, flats(1))
        val BB_MAJOR = Key("Bb", Mode.Major, 10, flats(2))
        val EB_MAJOR = Key("Eb", Mode.Major,  3, flats(3))
        val AB_MAJOR = Key("Ab", Mode.Major,  8, flats(4))
        val DB_MAJOR = Key("Db", Mode.Major,  1, flats(5))
        val GB_MAJOR = Key("Gb", Mode.Major,  6, flats(6))
        val CB_MAJOR = Key("Cb", Mode.Major, 11, flats(7))

        // ── 15 Minor Keys ─────────────────────────────────────────────────────
        val A_MINOR  = Key("A",  Mode.Minor,  9, flats(0))
        val E_MINOR  = Key("E",  Mode.Minor,  4, sharps(1))
        val B_MINOR  = Key("B",  Mode.Minor, 11, sharps(2))
        val FS_MINOR = Key("F#", Mode.Minor,  6, sharps(3))
        val CS_MINOR = Key("C#", Mode.Minor,  1, sharps(4))
        val GS_MINOR = Key("G#", Mode.Minor,  8, sharps(5))
        val DS_MINOR = Key("D#", Mode.Minor,  3, sharps(6))
        val AS_MINOR = Key("A#", Mode.Minor, 10, sharps(7))
        val D_MINOR  = Key("D",  Mode.Minor,  2, flats(1))
        val G_MINOR  = Key("G",  Mode.Minor,  7, flats(2))
        val C_MINOR  = Key("C",  Mode.Minor,  0, flats(3))
        val F_MINOR  = Key("F",  Mode.Minor,  5, flats(4))
        val BB_MINOR = Key("Bb", Mode.Minor, 10, flats(5))
        val EB_MINOR = Key("Eb", Mode.Minor,  3, flats(6))
        val AB_MINOR = Key("Ab", Mode.Minor,  8, flats(7))
    }
}
