package com.lnkranch.yaga.theory

fun noteNameToSemitone(name: String): Int {
    val base = mapOf('C' to 0, 'D' to 2, 'E' to 4, 'F' to 5, 'G' to 7, 'A' to 9, 'B' to 11)
    val sharps = name.count { it == '#' }
    val flats  = name.count { it == 'b' }
    return ((base.getValue(name[0]) + sharps - flats) + 120) % 12
}
