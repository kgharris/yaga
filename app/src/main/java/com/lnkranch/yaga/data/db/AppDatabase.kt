package com.lnkranch.yaga.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lnkranch.yaga.data.db.dao.ChordAttemptDao
import com.lnkranch.yaga.data.db.dao.PersonalBestDao
import com.lnkranch.yaga.data.db.dao.ProgressionDao
import com.lnkranch.yaga.data.db.dao.SessionResultDao
import com.lnkranch.yaga.data.db.entity.ChordAttemptEntity
import com.lnkranch.yaga.data.db.entity.PersonalBestEntity
import com.lnkranch.yaga.data.db.entity.ProgressionEntity
import com.lnkranch.yaga.data.db.entity.SessionResultEntity

@Database(
    entities = [ProgressionEntity::class, PersonalBestEntity::class, SessionResultEntity::class, ChordAttemptEntity::class],
    version = 3,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun progressionDao(): ProgressionDao
    abstract fun personalBestDao(): PersonalBestDao
    abstract fun sessionResultDao(): SessionResultDao
    abstract fun chordAttemptDao(): ChordAttemptDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context, AppDatabase::class.java, "guitardrill.db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}
