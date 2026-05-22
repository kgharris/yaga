package com.lnkranch.yaga.theory

fun noteNameToSemitone(name: String): Int {
    val base = mapOf('C' to 0, 'D' to 2, 'E' to 4, 'F' to 5, 'G' to 7, 'A' to 9, 'B' to 11)
    val sharps = name.count { it == '#' }
    val flats  = name.count { it == 'b' }
    return ((base.getValue(name[0]) + sharps - flats) + SAFE_OCTAVE_MODULO_OFFSET) % SEMITONES_PER_OCTAVE
}

fun intervalFromRoot(noteSemitone: Int, rootSemitone: Int): Int =
    (noteSemitone - rootSemitone + SEMITONES_PER_OCTAVE) % SEMITONES_PER_OCTAVE

enum class IntervalRole { Root, Third, Fifth, Seventh, OtherExtension }

fun intervalRole(intervalFromRoot: Int?): IntervalRole? = when (intervalFromRoot) {
    null      -> null
    0         -> IntervalRole.Root
    3, 4      -> IntervalRole.Third
    6, 7      -> IntervalRole.Fifth
    9, 10, 11 -> IntervalRole.Seventh
    else      -> IntervalRole.OtherExtension
}
