package com.lnkranch.yaga.theory

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object RomanChordSerializer : KSerializer<RomanChord> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("RomanChord", PrimitiveKind.STRING)

    private val byRomanNumeral: Map<String, RomanChord> =
        RomanChord.entries.associateBy { it.romanNumeral }

    private val chordByName = mapOf(
        "Maj7"     to Chord.Maj7,
        "Min7"     to Chord.Min7,
        "Dom7"     to Chord.Dom7,
        "HalfDim7" to Chord.HalfDim7,
        "FullDim7" to Chord.FullDim7,
    )

    override fun serialize(encoder: Encoder, value: RomanChord) {
        encoder.encodeString(value.romanNumeral)
    }

    override fun deserialize(decoder: Decoder): RomanChord {
        val s = decoder.decodeString()
        return byRomanNumeral[s]
            ?: throw SerializationException("Unknown roman numeral: \"$s\"")
    }
}
