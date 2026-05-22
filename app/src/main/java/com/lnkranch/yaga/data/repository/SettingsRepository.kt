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
    }

    val playingPosition: Flow<Int> = dataStore.data.map { prefs ->
        prefs[PLAYING_POSITION]?.coerceIn(1, 17) ?: 1
    }

    suspend fun setPlayingPosition(value: Int) {
        dataStore.edit { it[PLAYING_POSITION] = value.coerceIn(1, 17) }
    }
}
