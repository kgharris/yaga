package com.lnkranch.yaga.theory

enum class ChordQuality(
    private val intervals: List<Int>,
    val symbol: String,
) {
    Maj7    (listOf(0, 4, 7, 11), "△7"),
    Min7    (listOf(0, 3, 7, 10), "m7"),
    Dom7    (listOf(0, 4, 7, 10), "7"),
    HalfDim7(listOf(0, 3, 6, 10), "ø7"),
    FullDim7(listOf(0, 3, 6,  9), "°7");

    enum class Tone { _root, _3rd, _5th, _7th, _9th, _11th, _13th }

    operator fun get(tone: Tone): Int? = intervals.getOrNull(tone.ordinal)
}
