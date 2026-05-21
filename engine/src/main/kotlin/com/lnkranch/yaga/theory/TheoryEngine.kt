package com.lnkranch.yaga.theory

class TheoryEngine {

    /**
     * Resolves [degree] in [key].
     *
     * The root is spelled by the key (determining enharmonic choice: F# vs Gb).
     * All other chord tones are derived by letter-skipping: the 3rd always uses
     * the letter two steps above the root, the 5th four steps, the 7th six steps,
     * regardless of the key signature.
     */
    fun resolve(key: Key, degree: RomanChord): ResolvedChord {
        val rootSemitone = (key.tonicSemitone + key.diatonicSemitone(degree.degree) + degree.alteration).mod(12)
        val rootName     = key.spell(rootSemitone)

        val tones = Chord.Tone.entries
            .mapNotNull { tone ->
                degree.chord[tone]?.let { offset ->
                    tone to spellTone(rootName, tone.ordinal, (rootSemitone + offset).mod(12))
                }
            }
            .toMap()

        return ResolvedChord(
            romanNumeral = degree.romanNumeral,
            chord        = degree.chord,
            symbol       = rootName + degree.chord.symbol,
            tones        = tones,
        )
    }

    companion object {
        private const val LETTERS = "CDEFGAB"
        private val NATURAL_SEMITONES = intArrayOf(0, 2, 4, 5, 7, 9, 11)

        // Letter offset from root for each Chord.Tone ordinal:
        // _root=0 _3rd=1 _5th=2 _7th=3 _9th=4 _11th=5 _13th=6
        private val TONE_LETTER_OFFSETS = intArrayOf(0, 2, 4, 6, 1, 3, 5)

        private fun spellTone(rootName: String, toneIndex: Int, semitone: Int): String {
            val rootLetterIdx   = LETTERS.indexOf(rootName[0])
            val targetLetterIdx = (rootLetterIdx + TONE_LETTER_OFFSETS[toneIndex]) % 7
            val naturalSemitone = NATURAL_SEMITONES[targetLetterIdx]
            val diff            = (semitone - naturalSemitone + 12) % 12
            val sharps = if (diff <= 6) diff else 0
            val flats  = if (diff > 6) 12 - diff else 0
            return "${LETTERS[targetLetterIdx]}${"#".repeat(sharps)}${"b".repeat(flats)}"
        }
    }
}
