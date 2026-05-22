package com.lnkranch.yaga.theory

const val SEMITONES_PER_OCTAVE = 12
// Safe modulo offset: enough octaves to keep any chromatically-altered note positive.
const val SAFE_OCTAVE_MODULO_OFFSET = 10 * SEMITONES_PER_OCTAVE
