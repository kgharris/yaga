package com.lnkranch.yaga.domain

data class ResolvedChord(
    val romanChord: String,
    val chordQuality: String,
    val symbol: String,
    val third: String,
    val seventh: String,
)
