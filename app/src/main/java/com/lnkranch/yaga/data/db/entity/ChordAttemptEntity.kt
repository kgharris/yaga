package com.lnkranch.yaga.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chord_attempts",
    foreignKeys = [ForeignKey(
        entity = SessionResultEntity::class,
        parentColumns = ["id"],
        childColumns = ["sessionId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("sessionId")],
)
data class ChordAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val chordQuality: String,  // e.g. "Maj7", "m7", "7", "ø7" — primary heatmap grouping axis
    val romanChord: String,    // e.g. "ii", "V", "I7" — harmonic function context, secondary grouping
    val chordSymbol: String,   // e.g. "Dm7" — display only
    val tonicName: String,     // duplicated from session for query convenience
    val drillMode: String,     // duplicated from session for query convenience
    val elapsedMs: Long,       // time to complete this chord (ms)
    val misTapCount: Int,
)
