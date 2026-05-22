package com.lnkranch.yaga.theory

data class FretPosition(val string: Int, val fret: Int)

data class LocatedTone(
    val position: FretPosition,
    val noteName: String,
    val semitone: Int,
    val tone: Chord.Tone,
)

object FretboardLocator {
    // Strings indexed 0 (high E) through 5 (low E), matching display order (high E at top).
    val OPEN_SEMITONES: IntArray = intArrayOf(4, 11, 7, 2, 9, 4)

    fun semitoneAt(string: Int, fret: Int): Int = (OPEN_SEMITONES[string] + fret) % 12

    fun locateNote(noteSemitone: Int, windowStart: Int): List<FretPosition> {
        val results = mutableListOf<FretPosition>()
        for (string in 0..5) {
            for (fret in windowStart until windowStart + 6) {
                if ((OPEN_SEMITONES[string] + fret) % 12 == noteSemitone % 12) {
                    results += FretPosition(string, fret)
                }
            }
        }
        return results
    }

    fun notesForChord(chord: ResolvedChord, playingPosition: Int): List<LocatedTone> {
        val windowStart = playingPosition - 1
        val thirdPositions   = locateNote(chord.thirdSemitone,   windowStart)
        val seventhPositions = locateNote(chord.seventhSemitone, windowStart)
        return thirdPositions.map   { LocatedTone(it, chord.third,   chord.thirdSemitone,   Chord.Tone._3rd) } +
               seventhPositions.map { LocatedTone(it, chord.seventh, chord.seventhSemitone, Chord.Tone._7th) }
    }
}
