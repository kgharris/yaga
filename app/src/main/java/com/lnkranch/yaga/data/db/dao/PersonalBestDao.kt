package com.lnkranch.yaga.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lnkranch.yaga.data.db.entity.PersonalBestEntity

@Dao
interface PersonalBestDao {
    @Query("SELECT * FROM personal_bests WHERE progressionId = :progressionId AND tonicName = :tonicName AND drillMode = :drillMode")
    suspend fun get(progressionId: Long, tonicName: String, drillMode: String): PersonalBestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PersonalBestEntity)
}
