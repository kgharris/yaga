package com.lnkranch.yaga.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lnkranch.yaga.data.repository.DrillRepository
import com.lnkranch.yaga.theory.ChordQuality
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HeatmapCell(
    val chordQuality: String,       // ChordQuality.name — storage key
    val qualitySymbol: String,      // ChordQuality.symbol — display (e.g. "△7", "ø7")
    val tonicName: String,
    val avgAdjustedMs: Double,      // avg(elapsedMs + misTapCount × 4000)
    val normalizedScore: Float,     // 0.0 = best (green), 1.0 = worst (red)
    val totalAttempts: Int,
)

// Map from ChordQuality.name to ChordQuality.symbol for display labeling.
// Populated from the engine enum so new qualities appear automatically.
private val qualitySymbolMap: Map<String, String> =
    ChordQuality.entries.associate { it.name to it.symbol }

@OptIn(ExperimentalCoroutinesApi::class)
class HeatmapViewModel(
    private val repository: DrillRepository,
) : ViewModel() {

    val availableModes: StateFlow<List<String>> = repository.distinctDrillModes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedDrillMode = MutableStateFlow("")
    val selectedDrillMode: StateFlow<String> = _selectedDrillMode.asStateFlow()

    init {
        // Whenever the available modes list changes, seed selectedDrillMode with the
        // first entry (if the current selection is no longer valid or was never set).
        viewModelScope.launch {
            availableModes.collect { modes ->
                val current = _selectedDrillMode.value
                if (current.isEmpty() || current !in modes) {
                    _selectedDrillMode.value = modes.firstOrNull() ?: ""
                }
            }
        }
    }

    fun selectMode(mode: String) {
        _selectedDrillMode.value = mode
    }

    // All attempts for the currently selected mode, reacting to both mode changes and
    // underlying data changes (inserts / deletes).
    private val attemptsForMode = _selectedDrillMode
        .flatMapLatest { mode ->
            if (mode.isEmpty()) {
                kotlinx.coroutines.flow.flowOf(emptyList())
            } else {
                repository.allAttemptsForMode(mode)
            }
        }

    // Distinct chord qualities for the selected mode, in data order.
    private val qualitiesForMode = _selectedDrillMode
        .flatMapLatest { mode ->
            if (mode.isEmpty()) {
                kotlinx.coroutines.flow.flowOf(emptyList())
            } else {
                repository.distinctChordQualitiesForMode(mode)
            }
        }

    // Y-axis labels: the display symbol for each distinct quality in data order.
    val yLabels: StateFlow<List<String>> = qualitiesForMode
        .map { qualities ->
            qualities.map { name -> qualitySymbolMap[name] ?: name }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val cells: StateFlow<List<HeatmapCell>> = attemptsForMode
        .map { attempts ->
            if (attempts.isEmpty()) return@map emptyList()

            // Group attempts by (chordQuality, tonicName) and compute average adjustedMs.
            data class CellKey(val chordQuality: String, val tonicName: String)

            val grouped = attempts.groupBy { CellKey(it.chordQuality, it.tonicName) }
            val rawCells = grouped.map { (key, group) ->
                val avgAdjustedMs = group.map { attempt ->
                    attempt.elapsedMs + attempt.misTapCount * 4_000L
                }.average()
                Triple(key, avgAdjustedMs, group.size)
            }

            // Normalize relative to the player's own data range.
            val minMs = rawCells.minOf { it.second }
            val maxMs = rawCells.maxOf { it.second }
            val range = maxMs - minMs

            rawCells.map { (key, avgMs, count) ->
                val normalizedScore = if (range == 0.0) 0.0f else ((avgMs - minMs) / range).toFloat()
                HeatmapCell(
                    chordQuality = key.chordQuality,
                    qualitySymbol = qualitySymbolMap[key.chordQuality] ?: key.chordQuality,
                    tonicName = key.tonicName,
                    avgAdjustedMs = avgMs,
                    normalizedScore = normalizedScore,
                    totalAttempts = count,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteCell(tonicName: String, chordQuality: String) {
        val mode = _selectedDrillMode.value
        if (mode.isEmpty()) return
        viewModelScope.launch {
            repository.deleteAttemptsForCell(tonicName, chordQuality, mode)
        }
    }

    fun deleteMode() {
        val mode = _selectedDrillMode.value
        if (mode.isEmpty()) return
        viewModelScope.launch {
            repository.deleteAttemptsForMode(mode)
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
