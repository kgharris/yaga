package com.lnkranch.yaga.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lnkranch.yaga.data.repository.DrillRepository
import com.lnkranch.yaga.theory.Mode
import com.lnkranch.yaga.theory.RomanChord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProgressionBuilderViewModel(private val repository: DrillRepository) : ViewModel() {
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _mode = MutableStateFlow<Mode>(Mode.Major)
    val mode: StateFlow<Mode> = _mode

    private val _chords = MutableStateFlow<List<RomanChord>>(emptyList())
    val chords: StateFlow<List<RomanChord>> = _chords

    val availableChords: StateFlow<List<RomanChord>> = _mode.map { mode ->
        when (mode) {
            Mode.Major -> MAJOR_DIATONIC + CHROMATIC
            Mode.Minor -> MINOR_DIATONIC + CHROMATIC
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MAJOR_DIATONIC + CHROMATIC)

    val canSave: StateFlow<Boolean> = combine(_name, _chords) { n, c ->
        n.isNotBlank() && c.isNotEmpty()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setName(name: String) { _name.value = name }
    fun setMode(mode: Mode) { _mode.value = mode }
    fun addChord(chord: RomanChord) { _chords.value = _chords.value + chord }
    fun removeChordAt(index: Int) {
        _chords.value = _chords.value.toMutableList().also { it.removeAt(index) }
    }

    fun save(onSaved: () -> Unit) {
        viewModelScope.launch {
            repository.saveProgression(_name.value.trim(), _mode.value, _chords.value)
            onSaved()
        }
    }

    companion object {
        private val MAJOR_DIATONIC = listOf(
            RomanChord.I, RomanChord.II, RomanChord.III, RomanChord.IV,
            RomanChord.V, RomanChord.VI, RomanChord.VIIDIM,
        )
        private val MINOR_DIATONIC = listOf(
            RomanChord.i, RomanChord.iiDim, RomanChord.bIII, RomanChord.iv,
            RomanChord.v, RomanChord.bVI, RomanChord.bVII,
        )
        private val CHROMATIC = listOf(
            RomanChord.I7, RomanChord.IV7, RomanChord.V7,
            RomanChord.VI7, RomanChord.bVII7, RomanChord.sharpIVdim7,
        )

        fun Factory(repository: DrillRepository) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ProgressionBuilderViewModel(repository) as T
            }
        }
    }
}
