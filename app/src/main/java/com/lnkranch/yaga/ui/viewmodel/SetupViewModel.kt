package com.lnkranch.yaga.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lnkranch.yaga.data.db.entity.ProgressionEntity
import com.lnkranch.yaga.data.repository.DrillRepository
import com.lnkranch.yaga.domain.DrillMode
import com.lnkranch.yaga.domain.TONIC_NAMES
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class SetupViewModel(repository: DrillRepository) : ViewModel() {
    val progressions: StateFlow<List<ProgressionEntity>> = repository.progressions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val tonics: List<String> = TONIC_NAMES

    private val _selectedTonic = MutableStateFlow(TONIC_NAMES.first())
    val selectedTonic: StateFlow<String> = _selectedTonic

    private val _selectedProgressionId = MutableStateFlow<Long?>(null)
    val selectedProgressionId: StateFlow<Long?> = _selectedProgressionId

    private val _selectedDrillMode = MutableStateFlow(DrillMode.Normal)
    val selectedDrillMode: StateFlow<DrillMode> = _selectedDrillMode

    val canStartDrill: StateFlow<Boolean> = combine(selectedTonic, selectedProgressionId) { _, id ->
        id != null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun selectTonic(tonic: String) { _selectedTonic.value = tonic }
    fun selectProgression(id: Long) { _selectedProgressionId.value = id }
    fun selectDrillMode(mode: DrillMode) { _selectedDrillMode.value = mode }

    companion object {
        fun Factory(repository: DrillRepository) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return SetupViewModel(repository) as T
            }
        }
    }
}
