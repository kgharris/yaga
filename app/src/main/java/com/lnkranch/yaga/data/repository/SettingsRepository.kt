package com.lnkranch.yaga.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lnkranch.yaga.domain.DrillInputMode
import com.lnkranch.yaga.domain.DrillMode
import com.lnkranch.yaga.domain.TONIC_NAMES
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class SetupPrefs(
    val tonic: String = TONIC_NAMES.first(),
    val progressionId: Long? = null,
    val drillMode: String = DrillMode.Normal.name,
    val inputMode: String = DrillInputMode.Buttons.name,
)

class SettingsRepository(private val dataStore: DataStore<Preferences>) {
    companion object {
        val PLAYING_POSITION = intPreferencesKey("playing_position")
        val CORRECT_DISPLAY_MS = intPreferencesKey("correct_display_ms")
        private val SETUP_PREFS = stringPreferencesKey("setup_prefs")

        const val PLAYING_POSITION_MIN = 1
        const val PLAYING_POSITION_MAX = 17

        const val CORRECT_DISPLAY_MS_DEFAULT = 1000
        const val CORRECT_DISPLAY_MS_MIN = 250
        const val CORRECT_DISPLAY_MS_STEP = 250
    }

    val playingPosition: Flow<Int> = dataStore.data.map { prefs ->
        prefs[PLAYING_POSITION]?.coerceIn(PLAYING_POSITION_MIN, PLAYING_POSITION_MAX) ?: PLAYING_POSITION_MIN
    }

    suspend fun setPlayingPosition(value: Int) {
        dataStore.edit { it[PLAYING_POSITION] = value.coerceIn(PLAYING_POSITION_MIN, PLAYING_POSITION_MAX) }
    }

    val correctDisplayMs: Flow<Int> = dataStore.data.map { prefs ->
        prefs[CORRECT_DISPLAY_MS]?.coerceAtLeast(CORRECT_DISPLAY_MS_MIN)
            ?: CORRECT_DISPLAY_MS_DEFAULT
    }

    suspend fun setCorrectDisplayMs(value: Int) {
        dataStore.edit {
            it[CORRECT_DISPLAY_MS] = value.coerceAtLeast(CORRECT_DISPLAY_MS_MIN)
        }
    }

    val setupPrefs: Flow<SetupPrefs> = dataStore.data.map { prefs ->
        prefs[SETUP_PREFS]
            ?.let { runCatching { Json.decodeFromString<SetupPrefs>(it) }.getOrNull() }
            ?: SetupPrefs()
    }

    suspend fun setSetupPrefs(prefs: SetupPrefs) {
        dataStore.edit { it[SETUP_PREFS] = Json.encodeToString(prefs) }
    }
}
