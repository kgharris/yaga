package com.lnkranch.yaga.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lnkranch.yaga.data.repository.DrillRepository
import com.lnkranch.yaga.domain.HeatmapScorer
import com.lnkranch.yaga.theory.Chord
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HeatmapCell(
    val chordSymbol: String,         // e.g. "Dm7", "G7", "C△7"
    val chordQuality: String,        // Chord class simple name — storage key (e.g. "Maj7", "Min7")
    val qualitySymbol: String,       // Chord.symbol — display (e.g. "△7", "ø7")
    val avgAdjustedMs: Double,       // avg(elapsedMs + misTapCount × 4000)
    val normalizedScore: Float,      // 0.0 = best (green), 1.0 = worst (red)
    val totalAttempts: Int,
)

private val qualitySymbolMap: Map<String, String> =
    Chord.all.associate { (it::class.simpleName ?: "") to it.symbol }

@OptIn(ExperimentalCoroutinesApi::class)
class HeatmapViewModel(
    private val repository: DrillRepository,
) : ViewModel() {

    val availableModes: StateFlow<List<String>> = repository.distinctDrillModes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedDrillMode = MutableStateFlow("")
    val selectedDrillMode: StateFlow<String> = _selectedDrillMode.asStateFlow()

    val availableInputModes: StateFlow<List<String>> = _selectedDrillMode
        .flatMapLatest { mode ->
            if (mode.isEmpty()) flowOf(emptyList())
            else repository.distinctInputModesForDrillMode(mode)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedInputMode = MutableStateFlow("")
    val selectedInputMode: StateFlow<String> = _selectedInputMode.asStateFlow()

    init {
        viewModelScope.launch {
            availableModes.collect { modes ->
                val current = _selectedDrillMode.value
                if (current.isEmpty() || current !in modes) {
                    _selectedDrillMode.value = modes.firstOrNull() ?: ""
                }
            }
        }
        viewModelScope.launch {
            availableInputModes.collect { modes ->
                val current = _selectedInputMode.value
                if (current.isEmpty() || current !in modes) {
                    _selectedInputMode.value = modes.firstOrNull() ?: ""
                }
            }
        }
    }

    fun selectMode(mode: String) {
        _selectedDrillMode.value = mode
    }

    fun selectInputMode(mode: String) {
        _selectedInputMode.value = mode
    }

    private val attemptsForMode = combine(_selectedDrillMode, _selectedInputMode) { drill, input ->
        drill to input
    }.flatMapLatest { (drill, input) ->
        if (drill.isEmpty() || input.isEmpty()) {
            flowOf(emptyList())
        } else {
            repository.allAttemptsForModeAndInput(drill, input)
        }
    }

    // Grouped by chordSymbol, sorted worst → best (highest normalizedScore first).
    val cells: StateFlow<List<HeatmapCell>> = attemptsForMode
        .map { attempts ->
            if (attempts.isEmpty()) return@map emptyList()

            val grouped = attempts.groupBy { it.chordSymbol }
            val rawCells = grouped.map { (symbol, group) ->
                val avgAdjustedMs = group.map { HeatmapScorer.adjustedMs(it.elapsedMs, it.misTapCount) }.average()
                val quality = group.first().chordQuality
                Triple(symbol to quality, avgAdjustedMs, group.size)
            }

            val normalizedScores = HeatmapScorer.normalize(rawCells.map { it.second })

            rawCells.zip(normalizedScores)
                .map { (raw, normalizedScore) ->
                    val (symbolAndQuality, avgMs, count) = raw
                    val (symbol, quality) = symbolAndQuality
                    HeatmapCell(
                        chordSymbol = symbol,
                        chordQuality = quality,
                        qualitySymbol = qualitySymbolMap[quality] ?: quality,
                        avgAdjustedMs = avgMs,
                        normalizedScore = normalizedScore,
                        totalAttempts = count,
                    )
                }
                .sortedByDescending { it.normalizedScore }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteCell(chordSymbol: String) {
        val mode = _selectedDrillMode.value
        val inputMode = _selectedInputMode.value
        if (mode.isEmpty() || inputMode.isEmpty()) return
        viewModelScope.launch {
            repository.deleteAttemptsForCell(chordSymbol, mode, inputMode)
        }
    }

    fun deleteMode() {
        val mode = _selectedDrillMode.value
        val inputMode = _selectedInputMode.value
        if (mode.isEmpty() || inputMode.isEmpty()) return
        viewModelScope.launch {
            repository.deleteAttemptsForMode(mode, inputMode)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            repository.deleteAllAttempts()
        }
    }

    companion object {
        fun Factory(repository: DrillRepository) =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return HeatmapViewModel(repository) as T
                }
            }
    }
}
