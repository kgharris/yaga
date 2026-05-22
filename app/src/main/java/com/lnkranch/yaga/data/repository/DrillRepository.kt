package com.lnkranch.yaga.data.repository

import com.lnkranch.yaga.data.db.AppDatabase
import com.lnkranch.yaga.data.db.entity.ChordAttemptEntity
import com.lnkranch.yaga.data.db.entity.PersonalBestEntity
import com.lnkranch.yaga.data.db.entity.ProgressionEntity
import com.lnkranch.yaga.data.db.entity.SessionResultEntity
import com.lnkranch.yaga.theory.Mode
import com.lnkranch.yaga.theory.RomanChord
import com.lnkranch.yaga.theory.RomanChordSerializer
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class DrillRepository(db: AppDatabase) {
    private val progressionDao = db.progressionDao()
    private val personalBestDao = db.personalBestDao()
    private val sessionResultDao = db.sessionResultDao()
    private val chordAttemptDao = db.chordAttemptDao()

    fun progressions(): Flow<List<ProgressionEntity>> = progressionDao.getAll()

    suspend fun hasPresets(): Boolean = progressionDao.presetCount() > 0

    suspend fun insertPresets(presets: List<ProgressionEntity>) = progressionDao.insertAll(presets)

    suspend fun getProgression(id: Long): ProgressionEntity? = progressionDao.getById(id)

    suspend fun saveProgression(name: String, mode: Mode, chords: List<RomanChord>): Long =
        progressionDao.insert(
            ProgressionEntity(
                name = name,
                mode = mode.toString(),
                chordsJson = Json.encodeToString(ListSerializer(RomanChordSerializer), chords),
                isPreset = false,
            )
        )

    suspend fun deleteProgression(entity: ProgressionEntity) = progressionDao.delete(entity)

    suspend fun getPersonalBest(progressionId: Long, tonicName: String, drillMode: String): PersonalBestEntity? =
        personalBestDao.get(progressionId, tonicName, drillMode)

    suspend fun updatePersonalBestIfImproved(
        progressionId: Long,
        tonicName: String,
        drillMode: String,
        score: Double,
        elapsedMs: Long,
    ): Boolean {
        val current = personalBestDao.get(progressionId, tonicName, drillMode)
        return if (current == null || score > current.bestScore) {
            personalBestDao.upsert(
                PersonalBestEntity(
                    progressionId = progressionId,
                    tonicName = tonicName,
                    drillMode = drillMode,
                    bestScore = score,
                    bestElapsedMs = elapsedMs,
                    achievedAt = System.currentTimeMillis(),
                )
            )
            true
        } else false
    }

    suspend fun saveSession(result: SessionResultEntity): Long = sessionResultDao.insert(result)

    fun sessionsForProgression(progressionId: Long): Flow<List<SessionResultEntity>> =
        sessionResultDao.getForProgression(progressionId)

    suspend fun saveChordAttempts(attempts: List<ChordAttemptEntity>) =
        chordAttemptDao.insertAll(attempts)

    fun allAttemptsForModeAndInput(drillMode: String, inputMode: String): Flow<List<ChordAttemptEntity>> =
        chordAttemptDao.getAllForModeAndInput(drillMode, inputMode)

    fun distinctDrillModes(): Flow<List<String>> =
        chordAttemptDao.distinctDrillModes()

    fun distinctInputModesForDrillMode(drillMode: String): Flow<List<String>> =
        chordAttemptDao.distinctInputModesForDrillMode(drillMode)

    suspend fun deleteAttemptsForMode(drillMode: String, inputMode: String) =
        chordAttemptDao.deleteAttemptsForMode(drillMode, inputMode)

    suspend fun deleteAttemptsForCell(chordSymbol: String, drillMode: String, inputMode: String) =
        chordAttemptDao.deleteAttemptsForCell(chordSymbol, drillMode, inputMode)

    suspend fun deleteAllAttempts() =
        chordAttemptDao.deleteAllAttempts()
}
