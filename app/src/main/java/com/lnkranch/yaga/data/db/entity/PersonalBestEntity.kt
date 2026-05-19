package com.lnkranch.yaga.data.db.entity

import androidx.room.Entity

@Entity(
    tableName = "personal_bests",
    primaryKeys = ["progressionId", "tonicName", "drillMode"],
)
data class PersonalBestEntity(
    val progressionId: Long,
    val tonicName: String,
    val drillMode: String,
    val bestScore: Double,
    val bestElapsedMs: Long,
    val achievedAt: Long,
)
