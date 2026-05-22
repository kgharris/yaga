package com.lnkranch.yaga.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.lnkranch.yaga.data.db.entity.ChordAttemptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChordAttemptDao {
    @Insert
    suspend fun insertAll(attempts: List<ChordAttemptEntity>)

    @Query("SELECT * FROM chord_attempts WHERE drillMode = :drillMode AND inputMode = :inputMode")
    fun getAllForModeAndInput(drillMode: String, inputMode: String): Flow<List<ChordAttemptEntity>>

    @Query("SELECT DISTINCT drillMode FROM chord_attempts")
    fun distinctDrillModes(): Flow<List<String>>

    @Query("SELECT DISTINCT inputMode FROM chord_attempts WHERE drillMode = :drillMode")
    fun distinctInputModesForDrillMode(drillMode: String): Flow<List<String>>

    @Query("DELETE FROM chord_attempts WHERE drillMode = :drillMode AND inputMode = :inputMode")
    suspend fun deleteAttemptsForMode(drillMode: String, inputMode: String)

    @Query("DELETE FROM chord_attempts WHERE chordSymbol = :chordSymbol AND drillMode = :drillMode AND inputMode = :inputMode")
    suspend fun deleteAttemptsForCell(chordSymbol: String, drillMode: String, inputMode: String)

    @Query("DELETE FROM chord_attempts")
    suspend fun deleteAllAttempts()
}
