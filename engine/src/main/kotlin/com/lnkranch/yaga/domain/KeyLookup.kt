package com.lnkranch.yaga.domain

import com.lnkranch.yaga.theory.Key
import com.lnkranch.yaga.theory.Mode

val TONIC_NAMES = listOf("C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B")

fun findKey(tonicName: String, mode: Mode): Key? = when (mode) {
    Mode.Major -> majorKeys[tonicName]
    Mode.Minor -> minorKeys[tonicName]
}

private val majorKeys = mapOf(
    "C"  to Key.C_MAJOR,
    "C#" to Key.CS_MAJOR,
    "D"  to Key.D_MAJOR,
    "Eb" to Key.EB_MAJOR,
    "E"  to Key.E_MAJOR,
    "F"  to Key.F_MAJOR,
    "F#" to Key.FS_MAJOR,
    "G"  to Key.G_MAJOR,
    "Ab" to Key.AB_MAJOR,
    "A"  to Key.A_MAJOR,
    "Bb" to Key.BB_MAJOR,
    "B"  to Key.B_MAJOR,
)

private val minorKeys = mapOf(
    "C"  to Key.C_MINOR,
    "C#" to Key.CS_MINOR,
    "D"  to Key.D_MINOR,
    "Eb" to Key.EB_MINOR,
    "E"  to Key.E_MINOR,
    "F"  to Key.F_MINOR,
    "F#" to Key.FS_MINOR,
    "G"  to Key.G_MINOR,
    "Ab" to Key.AB_MINOR,
    "A"  to Key.A_MINOR,
    "Bb" to Key.BB_MINOR,
    "B"  to Key.B_MINOR,
)
