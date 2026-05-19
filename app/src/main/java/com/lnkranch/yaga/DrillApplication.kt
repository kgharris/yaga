package com.lnkranch.yaga

import android.app.Application
import com.lnkranch.yaga.data.db.AppDatabase
import com.lnkranch.yaga.data.db.entity.ProgressionEntity
import com.lnkranch.yaga.data.repository.DrillRepository
import com.lnkranch.yaga.ui.viewmodel.DrillUiState
import com.lnkranch.yaga.theory.RomanChord
import com.lnkranch.yaga.theory.RomanChordSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Serializable
private data class PresetJson(
    val name: String,
    val mode: String,
    val chords: List<RomanChord>,
)

class DrillApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: DrillRepository by lazy { DrillRepository(database) }
    var pendingSummary: DrillUiState.Complete? = null

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            seedPresetsIfNeeded()
        }
    }

    private suspend fun seedPresetsIfNeeded() {
        if (repository.hasPresets()) return
        val raw = assets.open("progressions.json").bufferedReader().readText()
        val presets = Json.decodeFromString<List<PresetJson>>(raw)
        repository.insertPresets(
            presets.map { p ->
                ProgressionEntity(
                    name = p.name,
                    mode = p.mode,
                    chordsJson = Json.encodeToString(ListSerializer(RomanChordSerializer), p.chords),
                    isPreset = true,
                )
            }
        )
    }
}
