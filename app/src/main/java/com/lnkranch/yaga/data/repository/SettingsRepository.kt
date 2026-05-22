package com.lnkranch.yaga.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val dataStore: DataStore<Preferences>) {
    companion object {
        val PLAYING_POSITION = intPreferencesKey("playing_position")
        val CORRECT_DISPLAY_MS = intPreferencesKey("correct_display_ms")

        const val CORRECT_DISPLAY_MS_DEFAULT = 1000
        const val CORRECT_DISPLAY_MS_MIN = 250
        const val CORRECT_DISPLAY_MS_MAX = 2000
        const val CORRECT_DISPLAY_MS_STEP = 250
    }

    val playingPosition: Flow<Int> = dataStore.data.map { prefs ->
        prefs[PLAYING_POSITION]?.coerceIn(1, 17) ?: 1
    }

    suspend fun setPlayingPosition(value: Int) {
        dataStore.edit { it[PLAYING_POSITION] = value.coerceIn(1, 17) }
    }

    val correctDisplayMs: Flow<Int> = dataStore.data.map { prefs ->
        prefs[CORRECT_DISPLAY_MS]?.coerceIn(CORRECT_DISPLAY_MS_MIN, CORRECT_DISPLAY_MS_MAX)
            ?: CORRECT_DISPLAY_MS_DEFAULT
    }

    suspend fun setCorrectDisplayMs(value: Int) {
        dataStore.edit {
            it[CORRECT_DISPLAY_MS] = value.coerceIn(CORRECT_DISPLAY_MS_MIN, CORRECT_DISPLAY_MS_MAX)
        }
    }
}
