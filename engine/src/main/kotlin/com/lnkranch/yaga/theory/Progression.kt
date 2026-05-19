package com.lnkranch.yaga.theory

import kotlinx.serialization.Serializable

@Serializable
data class Progression(
    val name: String,
    val chords: List<RomanChord>,
)
