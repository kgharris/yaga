package com.lnkranch.yaga.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lnkranch.yaga.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settings: SettingsRepository) : ViewModel() {

    val playingPosition: StateFlow<Int> = settings.playingPosition
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 1)

    fun setPlayingPosition(value: Int) {
        viewModelScope.launch { settings.setPlayingPosition(value) }
    }

    companion object {
        fun Factory(settings: SettingsRepository) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(settings) as T
            }
        }
    }
}
