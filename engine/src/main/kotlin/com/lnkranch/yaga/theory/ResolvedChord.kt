package com.lnkranch.yaga.theory

data class ResolvedChord(
    val romanNumeral: String,
    val chord: Chord,
    val symbol: String,
    internal val tones: Map<Chord.Tone, String>,
) {
    operator fun get(tone: Chord.Tone): String? = tones[tone]
    val root: String    get() = tones[Chord.Tone._root] ?: ""
    val third: String   get() = tones[Chord.Tone._3rd]  ?: ""
    val seventh: String get() = tones[Chord.Tone._7th]  ?: ""

    val rootSemitone:    Int get() = noteNameToSemitone(root)
    val thirdSemitone:   Int get() = noteNameToSemitone(third)
    val seventhSemitone: Int get() = noteNameToSemitone(seventh)
}
