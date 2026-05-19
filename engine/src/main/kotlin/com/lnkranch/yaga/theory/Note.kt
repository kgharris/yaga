package com.lnkranch.yaga.theory

/**
 * The 12 chromatic pitch classes, identified by semitone (0 = C … 11 = B).
 *
 * Names use the flat-side spelling by convention. Note-name resolution for any
 * key context is handled entirely by [Key.spell] — this enum carries no
 * spelling logic of its own.
 */
enum class Note(val semitone: Int) {
    C(0), Db(1), D(2), Eb(3), E(4), F(5),
    Gb(6), G(7), Ab(8), A(9), Bb(10), B(11);
}
