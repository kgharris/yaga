package com.lnkranch.yaga.data.db

import androidx.room.TypeConverter
import com.lnkranch.yaga.theory.RomanChord
import com.lnkranch.yaga.theory.RomanChordSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromRomanChords(chords: List<RomanChord>): String =
        Json.encodeToString(ListSerializer(RomanChordSerializer), chords)

    @TypeConverter
    fun toRomanChords(value: String): List<RomanChord> =
        Json.decodeFromString(ListSerializer(RomanChordSerializer), value)
}
