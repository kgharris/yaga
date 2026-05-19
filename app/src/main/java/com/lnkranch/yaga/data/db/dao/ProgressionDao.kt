package com.lnkranch.yaga.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.lnkranch.yaga.data.db.entity.ProgressionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressionDao {
    @Query("SELECT * FROM progressions ORDER BY isPreset DESC, name ASC")
    fun getAll(): Flow<List<ProgressionEntity>>

    @Query("SELECT * FROM progressions WHERE id = :id")
    suspend fun getById(id: Long): ProgressionEntity?

    @Insert
    suspend fun insert(entity: ProgressionEntity): Long

    @Insert
    suspend fun insertAll(entities: List<ProgressionEntity>)

    @Update
    suspend fun update(entity: ProgressionEntity)

    @Delete
    suspend fun delete(entity: ProgressionEntity)

    @Query("SELECT COUNT(*) FROM progressions WHERE isPreset = 1")
    suspend fun presetCount(): Int
}
