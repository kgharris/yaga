package com.lnkranch.yaga.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_results")
data class SessionResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val progressionId: Long,
    val tonicName: String,
    val drillMode: String,
    val elapsedMs: Long,
    val misTapCount: Int,
    val score: Double,
    val playedAt: Long,
)
