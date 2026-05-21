package com.lnkranch.yaga.theory

sealed class Chord(val intervals: List<Int>, val symbol: String) {
    object Maj7     : Chord(listOf(0, 4, 7, 11), "△7")
    object Min7     : Chord(listOf(0, 3, 7, 10), "m7")
    object Dom7     : Chord(listOf(0, 4, 7, 10), "7")
    object HalfDim7 : Chord(listOf(0, 3, 6, 10), "ø7")
    object FullDim7 : Chord(listOf(0, 3, 6,  9), "°7")

    enum class Tone { _root, _3rd, _5th, _7th, _9th, _11th, _13th }

    operator fun get(tone: Tone): Int? = intervals.getOrNull(tone.ordinal)

    companion object {
        val all: List<Chord> = listOf(Maj7, Min7, Dom7, HalfDim7, FullDim7)
    }
}
