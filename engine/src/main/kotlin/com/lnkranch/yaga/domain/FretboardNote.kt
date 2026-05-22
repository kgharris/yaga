package com.lnkranch.yaga.domain

import com.lnkranch.yaga.theory.Chord
import com.lnkranch.yaga.theory.FretPosition

data class FretboardNote(
    val position: FretPosition,
    val noteName: String,
    val semitone: Int,
    val toneRole: Chord.Tone,
    val revealed: Boolean,
)
