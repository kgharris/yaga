package com.lnkranch.yaga.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.lnkranch.yaga.data.db.entity.SessionResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionResultDao {
    @Insert
    suspend fun insert(entity: SessionResultEntity): Long

    @Query("SELECT * FROM session_results WHERE progressionId = :progressionId ORDER BY playedAt DESC")
    fun getForProgression(progressionId: Long): Flow<List<SessionResultEntity>>
}
