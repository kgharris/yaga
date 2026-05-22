package com.lnkranch.yaga.domain

import com.lnkranch.yaga.theory.FretPosition

data class FretDot(
    val position: FretPosition,
    val label: String,
    val intervalFromRoot: Int?,   // semitone offset 0–11 from chord root; null = non-chord tone
    val semitone: Int,            // absolute pitch class 0–11
)
