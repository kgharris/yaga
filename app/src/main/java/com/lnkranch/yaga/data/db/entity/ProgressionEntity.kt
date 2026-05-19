package com.lnkranch.yaga.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "progressions")
data class ProgressionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val mode: String,
    val chordsJson: String,
    val isPreset: Boolean,
)
