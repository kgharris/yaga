package com.lnkranch.yaga.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lnkranch.yaga.data.db.entity.ChordAttemptEntity
import com.lnkranch.yaga.data.db.entity.SessionResultEntity
import com.lnkranch.yaga.data.repository.DrillRepository
import com.lnkranch.yaga.domain.DrillMode
import com.lnkranch.yaga.domain.ScoreCalculator
import com.lnkranch.yaga.domain.findKey
import com.lnkranch.yaga.theory.Chord
import com.lnkranch.yaga.theory.Mode
import com.lnkranch.yaga.theory.noteNameToSemitone
import com.lnkranch.yaga.theory.ResolvedChord
import com.lnkranch.yaga.theory.RomanChord
import com.lnkranch.yaga.theory.TheoryEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

enum class NoteFeedback { Correct, Incorrect }

data class NoteButton(
    val label: String,
    val semitoneOffset: Int,
)

sealed interface DrillUiState {
    data object Loading : DrillUiState
    data object Error : DrillUiState
    data class Running(
        val progressionName: String,
        val currentChord: ResolvedChord,
        val currentIndex: Int,
        val totalChords: Int,
        val noteButtons: List<NoteButton>,
        val tappedThird: Boolean,
        val tappedSeventh: Boolean,
        val elapsedMs: Long,
        val misTapCount: Int,
        val feedbackNote: String? = null,
        val feedbackType: NoteFeedback? = null,
    ) : DrillUiState
    data class Complete(
        val sessionResult: SessionResultEntity,
        val isNewPersonalBest: Boolean,
        val previousBestScore: Double?,
        val progressionName: String,
        val drillMode: DrillMode,
    ) : DrillUiState
}

class DrillViewModel(
    private val repository: DrillRepository,
    private val progressionId: Long,
    private val tonicName: String,
    private val drillMode: DrillMode,
) : ViewModel() {

    private val engine = TheoryEngine()

    private val _uiState = MutableStateFlow<DrillUiState>(DrillUiState.Loading)
    val uiState: StateFlow<DrillUiState> = _uiState.asStateFlow()

    private var chords: List<ResolvedChord> = emptyList()
    private var tonicSemitone = 0
    private var baseNoteButtons: List<NoteButton> = emptyList()
    private var currentNoteButtons: List<NoteButton> = emptyList()
    private var currentIndex = 0
    private var tappedThird = false
    private var tappedSeventh = false
    private var feedbackNote: String? = null
    private var feedbackType: NoteFeedback? = null
    private var feedbackClearJob: Job? = null
    private var misTapCount = 0
    private var sessionStartMs = 0L
    private var progressionName = ""
    private var timerJob: Job? = null
    private var chordStartMs: Long = 0L
    private var chordMisTapCount: Int = 0
    private val chordAttempts: MutableList<ChordAttemptEntity> = mutableListOf()

    init {
        viewModelScope.launch { loadSession() }
    }

    private suspend fun loadSession() {
        val entity = repository.getProgression(progressionId)
        if (entity == null) { _uiState.value = DrillUiState.Error; return }

        val mode = if (entity.mode == "Major") Mode.Major else Mode.Minor
        val key = findKey(tonicName, mode)
        if (key == null) { _uiState.value = DrillUiState.Error; return }

        progressionName = entity.name

        val romanChords = Json.decodeFromString<List<RomanChord>>(entity.chordsJson)
        chords = romanChords.map { rc -> engine.resolve(key, rc) }

        tonicSemitone = key.tonicSemitone
        baseNoteButtons = (0..11).map { offset ->
            NoteButton(
                label = key.spell((key.tonicSemitone + offset) % 12),
                semitoneOffset = offset,
            )
        }
        currentNoteButtons = buildShuffledButtonsForChord(chords.first())

        sessionStartMs = System.currentTimeMillis()
        chordStartMs = sessionStartMs
        emitRunningState()

        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(100)
                emitRunningState()
            }
        }
    }

    fun tap(noteName: String) {
        if (_uiState.value !is DrillUiState.Running) return
        val chord = chords[currentIndex]

        feedbackClearJob?.cancel()
        feedbackNote = null
        feedbackType = null

        val firstTarget = if (drillMode == DrillMode.Normal) chord.third else chord.seventh
        val secondTarget = if (drillMode == DrillMode.Normal) chord.seventh else chord.third
        val firstDone = if (drillMode == DrillMode.Normal) tappedThird else tappedSeventh

        when {
            !firstDone && noteName == firstTarget -> {
                if (drillMode == DrillMode.Normal) tappedThird = true else tappedSeventh = true
                showFeedback(noteName, NoteFeedback.Correct)
                emitRunningState()
            }
            firstDone && noteName == secondTarget -> {
                if (drillMode == DrillMode.Normal) tappedSeventh = true else tappedThird = true
                showFeedback(noteName, NoteFeedback.Correct)
                advanceChord()
            }
            else -> {
                misTapCount++
                chordMisTapCount++
                showFeedback(noteName, NoteFeedback.Incorrect)
                emitRunningState()
            }
        }
    }

    private fun showFeedback(note: String, type: NoteFeedback) {
        feedbackNote = note
        feedbackType = type
        feedbackClearJob = viewModelScope.launch {
            delay(300)
            feedbackNote = null
            feedbackType = null
            emitRunningState()
        }
    }

    private fun advanceChord() {
        val current = chords[currentIndex]
        chordAttempts.add(ChordAttemptEntity(
            sessionId = 0L,
            chordQuality = current.chord::class.simpleName ?: "",
            romanChord = current.romanNumeral,
            chordSymbol = current.symbol,
            tonicName = tonicName,
            drillMode = drillMode.name,
            elapsedMs = System.currentTimeMillis() - chordStartMs,
            misTapCount = chordMisTapCount,
        ))
        chordMisTapCount = 0
        chordStartMs = System.currentTimeMillis()
        currentIndex++
        if (currentIndex >= chords.size) {
            finishSession()
            return
        }
        tappedThird = false
        tappedSeventh = false
        currentNoteButtons = buildShuffledButtonsForChord(chords[currentIndex])
        emitRunningState()
    }

    private fun finishSession() {
        timerJob?.cancel()
        val elapsedMs = System.currentTimeMillis() - sessionStartMs
        val score = ScoreCalculator.compute(chords.size, elapsedMs, misTapCount)
        val result = SessionResultEntity(
            progressionId = progressionId,
            tonicName = tonicName,
            drillMode = drillMode.name,
            elapsedMs = elapsedMs,
            misTapCount = misTapCount,
            score = score,
            playedAt = System.currentTimeMillis(),
        )
        viewModelScope.launch {
            val previousBest = repository.getPersonalBest(progressionId, tonicName, drillMode.name)
            val savedId = repository.saveSession(result)
            val isNew = repository.updatePersonalBestIfImproved(progressionId, tonicName, drillMode.name, score, elapsedMs)
            val attemptsWithId = chordAttempts.map { it.copy(sessionId = savedId) }
            repository.saveChordAttempts(attemptsWithId)
            _uiState.value = DrillUiState.Complete(
                sessionResult = result.copy(id = savedId),
                isNewPersonalBest = isNew,
                previousBestScore = previousBest?.bestScore,
                progressionName = progressionName,
                drillMode = drillMode,
            )
        }
    }

    private fun emitRunningState() {
        if (currentIndex >= chords.size) return
        _uiState.value = DrillUiState.Running(
            progressionName = progressionName,
            currentChord = chords[currentIndex],
            currentIndex = currentIndex,
            totalChords = chords.size,
            noteButtons = currentNoteButtons,
            tappedThird = tappedThird,
            tappedSeventh = tappedSeventh,
            elapsedMs = if (sessionStartMs == 0L) 0L else System.currentTimeMillis() - sessionStartMs,
            misTapCount = misTapCount,
            feedbackNote = feedbackNote,
            feedbackType = feedbackType,
        )
    }

    // Applies chord-tone spelling overrides for the 3rd and 7th buttons (so e.g. "Bbb"
    // appears instead of "Ab" for Gbdim7), then shuffles to eliminate positional cues.
    private fun buildShuffledButtonsForChord(chord: ResolvedChord): List<NoteButton> {
        if (chord.third.isEmpty() || chord.seventh.isEmpty()) return baseNoteButtons.shuffled()
        val thirdSemitone = noteNameToSemitone(chord.third)
        val seventhSemitone = noteNameToSemitone(chord.seventh)
        return baseNoteButtons.map { button ->
            val semitone = (tonicSemitone + button.semitoneOffset) % 12
            val label = when (semitone) {
                thirdSemitone -> chord.third
                seventhSemitone -> chord.seventh
                else -> button.label
            }
            button.copy(label = label)
        }.shuffled()
    }

    companion object {
        fun Factory(repository: DrillRepository, progressionId: Long, tonicName: String, drillMode: DrillMode) =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return DrillViewModel(repository, progressionId, tonicName, drillMode) as T
                }
            }
    }
}
