package com.lnkranch.yaga.theory

/**
 * The two supported modal contexts for scale-degree resolution.
 */
sealed class Mode {
    object Major : Mode()
    object Minor : Mode()

    override fun toString(): String = when (this) {
        is Major -> "Major"
        is Minor -> "Minor"
    }
}
