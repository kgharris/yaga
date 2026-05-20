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

    @Query("SELECT * FROM chord_attempts WHERE drillMode = :drillMode")
    fun getAllForMode(drillMode: String): Flow<List<ChordAttemptEntity>>

    @Query("SELECT DISTINCT drillMode FROM chord_attempts")
    fun distinctDrillModes(): Flow<List<String>>

    @Query("DELETE FROM chord_attempts WHERE drillMode = :drillMode")
    suspend fun deleteAttemptsForMode(drillMode: String)

    @Query("DELETE FROM chord_attempts WHERE chordSymbol = :chordSymbol AND drillMode = :drillMode")
    suspend fun deleteAttemptsForCell(chordSymbol: String, drillMode: String)

    @Query("DELETE FROM chord_attempts")
    suspend fun deleteAllAttempts()
}
