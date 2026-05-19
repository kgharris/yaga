package com.lnkranch.yaga.theory

import kotlinx.serialization.Serializable

@Serializable(with = RomanChordSerializer::class)
enum class RomanChord(
    val degree: Int,
    val alteration: Int,
    val quality: ChordQuality,
    val romanNumeral: String,
) {
    // ── Diatonic major ───────────────────────────────────────────────────────
    I      (1, 0,  ChordQuality.Maj7,     "I"),
    II     (2, 0,  ChordQuality.Min7,     "ii"),
    III    (3, 0,  ChordQuality.Min7,     "iii"),
    IV     (4, 0,  ChordQuality.Maj7,     "IV"),
    V      (5, 0,  ChordQuality.Dom7,     "V"),
    VI     (6, 0,  ChordQuality.Min7,     "vi"),
    VIIDIM (7, 0,  ChordQuality.HalfDim7, "vii°"),

    // ── Diatonic minor ───────────────────────────────────────────────────────
    i      (1, 0,  ChordQuality.Min7,     "i"),
    iiDim  (2, 0,  ChordQuality.HalfDim7, "ii°"),
    bIII   (3, 0,  ChordQuality.Maj7,     "III"),
    iv     (4, 0,  ChordQuality.Min7,     "iv"),
    v      (5, 0,  ChordQuality.Min7,     "v"),
    bVI    (6, 0,  ChordQuality.Maj7,     "VI"),
    bVII   (7, 0,  ChordQuality.Dom7,     "VII"),

    // ── Blues / chromatic ────────────────────────────────────────────────────
    I7          (1,  0, ChordQuality.Dom7,     "I7"),
    IV7         (4,  0, ChordQuality.Dom7,     "IV7"),
    V7          (5,  0, ChordQuality.Dom7,     "V7"),
    VI7         (6,  0, ChordQuality.Dom7,     "VI7"),
    bVII7       (7, -1, ChordQuality.Dom7,     "♭VII7"),
    sharpIVdim7 (4, +1, ChordQuality.FullDim7, "#IV°7");
}
