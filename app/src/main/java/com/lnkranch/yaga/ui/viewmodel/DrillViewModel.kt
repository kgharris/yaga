package com.lnkranch.yaga.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lnkranch.yaga.data.db.entity.ChordAttemptEntity
import com.lnkranch.yaga.data.db.entity.SessionResultEntity
import com.lnkranch.yaga.data.repository.DrillRepository
import com.lnkranch.yaga.data.repository.SettingsRepository
import com.lnkranch.yaga.domain.DrillInputMode
import com.lnkranch.yaga.domain.DrillMode
import com.lnkranch.yaga.domain.FretDot
import com.lnkranch.yaga.domain.FretboardNote
import com.lnkranch.yaga.domain.ScoreCalculator
import com.lnkranch.yaga.domain.findKey
import com.lnkranch.yaga.theory.Chord
import com.lnkranch.yaga.theory.FretPosition
import com.lnkranch.yaga.theory.FretboardLocator
import com.lnkranch.yaga.theory.intervalFromRoot
import com.lnkranch.yaga.theory.noteNameToSemitone
import com.lnkranch.yaga.theory.Mode
import com.lnkranch.yaga.theory.ResolvedChord
import com.lnkranch.yaga.theory.RomanChord
import com.lnkranch.yaga.theory.TheoryEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

enum class NoteFeedback { Correct, Incorrect }

data class NoteButton(
    val label: String,
    val semitone: Int,   // absolute pitch class 0–11
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
        val tappedSemitones: Set<Int>,
        val elapsedMs: Long,
        val misTapCount: Int,
        val feedbackSemitone: Int? = null,
        val feedbackType: NoteFeedback? = null,
        val fretDots: List<FretDot>,
        val revealedSemitones: Set<Int>,
        val playingPosition: Int,
        val inputMode: DrillInputMode,
        val errorFretPosition: FretPosition? = null,
        val isFlashPaused: Boolean = false,
        val isUserPaused: Boolean = false,
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
    private val inputMode: DrillInputMode,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val engine = TheoryEngine()

    private val _uiState = MutableStateFlow<DrillUiState>(DrillUiState.Loading)
    val uiState: StateFlow<DrillUiState> = _uiState.asStateFlow()

    private val _hapticEvent = MutableSharedFlow<NoteFeedback>(extraBufferCapacity = 1)
    val hapticEvent: SharedFlow<NoteFeedback> = _hapticEvent.asSharedFlow()

    private var chords: List<ResolvedChord> = emptyList()
    private var tonicSemitone = 0
    private var baseNoteButtons: List<NoteButton> = emptyList()
    private var currentNoteButtons: List<NoteButton> = emptyList()
    private var currentIndex = 0
    private var phase: DrillPhase = DrillPhase.AwaitingTap(0)
    private var orderedTones: List<Chord.Tone> = emptyList()
    private var feedbackSemitone: Int? = null
    private var feedbackType: NoteFeedback? = null
    private var feedbackClearJob: Job? = null
    private var misTapCount = 0
    private var sessionStartMs = 0L
    private var progressionName = ""
    private var timerJob: Job? = null
    private var pauseStartMs: Long = 0L
    private var totalPausedMs: Long = 0L
    private var chordStartMs: Long = 0L
    private var chordMisTapCount: Int = 0
    private val chordAttempts: MutableList<ChordAttemptEntity> = mutableListOf()
    private var drillInputMode: DrillInputMode = DrillInputMode.Buttons
    private var playingPosition: Int = 1
    private var correctDisplayMs: Long = 1000L
    private var currentFretboardNotes: List<FretboardNote> = emptyList()
    private lateinit var key: com.lnkranch.yaga.theory.Key
    private var errorFretPosition: FretPosition? = null

    init {
        viewModelScope.launch { loadSession() }
    }

    private suspend fun loadSession() {
        val entity = repository.getProgression(progressionId)
        if (entity == null) { _uiState.value = DrillUiState.Error; return }

        val mode = if (entity.mode == "Major") Mode.Major else Mode.Minor
        val key = findKey(tonicName, mode)
        if (key == null) { _uiState.value = DrillUiState.Error; return }
        this.key = key

        progressionName = entity.name

        val romanChords = Json.decodeFromString<List<RomanChord>>(entity.chordsJson)
        chords = romanChords.map { rc -> engine.resolve(key, rc) }

        tonicSemitone = key.tonicSemitone
        baseNoteButtons = (0..11).map { offset ->
            val sem = (key.tonicSemitone + offset) % 12
            NoteButton(label = key.spell(sem), semitone = sem)
        }
        drillInputMode = inputMode
        orderedTones = if (drillMode == DrillMode.Normal) listOf(Chord.Tone._3rd, Chord.Tone._7th)
                       else listOf(Chord.Tone._7th, Chord.Tone._3rd)
        playingPosition = settingsRepository.playingPosition.first()
        correctDisplayMs = settingsRepository.correctDisplayMs.first().toLong()
        currentFretboardNotes = buildFretboardNotesForChord(chords.first())
        currentNoteButtons = buildShuffledButtonsForChord(chords.first())

        sessionStartMs = System.currentTimeMillis()
        chordStartMs = sessionStartMs
        emitRunningState()

        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(TIMER_UPDATE_INTERVAL_MS)
                emitRunningState()
            }
        }
    }

    fun tap(semitone: Int, fretPosition: FretPosition? = null) {
        val currentPhase = phase as? DrillPhase.AwaitingTap ?: return
        if (currentIndex >= chords.size) return
        val chord = chords[currentIndex]

        feedbackClearJob?.cancel()
        feedbackSemitone = null
        feedbackType = null
        errorFretPosition = null

        val toneIndex = currentPhase.toneIndex
        val expectedTone = orderedTones[toneIndex]
        val expectedSemitone = chord.semitoneFor(expectedTone) ?: return

        if (semitone == expectedSemitone) {
            currentFretboardNotes = currentFretboardNotes.map {
                if (it.toneRole == expectedTone) it.copy(revealed = true) else it
            }
            showFeedback(semitone, NoteFeedback.Correct)
            phase = DrillPhase.FlashPause(if (toneIndex == orderedTones.lastIndex) null else toneIndex + 1)
            emitRunningState()
            startFlashPause()
        } else {
            misTapCount++
            chordMisTapCount++
            errorFretPosition = fretPosition
            showFeedback(semitone, NoteFeedback.Incorrect)
            emitRunningState()
        }
    }

    private fun buildFretboardNotesForChord(chord: ResolvedChord): List<FretboardNote> =
        FretboardLocator.notesForChord(chord, playingPosition)
            .map { FretboardNote(it.position, it.noteName, it.semitone, it.tone, false) }

    fun tapFret(string: Int, fret: Int) {
        tap(FretboardLocator.semitoneAt(string, fret), FretPosition(string, fret))
    }

    fun togglePause() {
        val currentPhase = phase
        if (currentPhase is DrillPhase.FlashPause) return
        if (currentPhase is DrillPhase.UserPaused) {
            totalPausedMs += System.currentTimeMillis() - pauseStartMs
            phase = currentPhase.resumeTo
            startTimer()
        } else {
            pauseStartMs = System.currentTimeMillis()
            timerJob?.cancel()
            phase = DrillPhase.UserPaused(resumeTo = currentPhase)
        }
        emitRunningState()
    }

    private fun showFeedback(semitone: Int, type: NoteFeedback) {
        _hapticEvent.tryEmit(type)
        feedbackSemitone = semitone
        feedbackType = type
        feedbackClearJob = viewModelScope.launch {
            delay(FEEDBACK_FLASH_MS)
            feedbackSemitone = null
            feedbackType = null
            emitRunningState()
        }
    }

    private fun startFlashPause() {
        timerJob?.cancel()
        val flashStart = System.currentTimeMillis()
        viewModelScope.launch {
            delay(correctDisplayMs)
            totalPausedMs += System.currentTimeMillis() - flashStart
            feedbackClearJob?.cancel()
            feedbackSemitone = null
            feedbackType = null
            errorFretPosition = null
            val flashPhase = phase as? DrillPhase.FlashPause ?: return@launch
            val nextIndex = flashPhase.nextToneIndex
            if (nextIndex != null) {
                currentFretboardNotes = currentFretboardNotes.map { it.copy(revealed = false) }
                phase = DrillPhase.AwaitingTap(nextIndex)
                startTimer()
                emitRunningState()
            } else {
                advanceChord()
            }
        }
    }

    private fun effectiveElapsedMs(): Long {
        if (sessionStartMs == 0L) return 0L
        val activeFreeze = if (phase is DrillPhase.UserPaused) System.currentTimeMillis() - pauseStartMs else 0L
        return System.currentTimeMillis() - sessionStartMs - totalPausedMs - activeFreeze
    }

    private fun advanceChord() {
        val completedIndex = currentIndex
        val current = chords[completedIndex]
        chordAttempts.add(ChordAttemptEntity(
            sessionId = 0L,
            chordQuality = current.chord::class.simpleName ?: "",
            romanChord = current.romanNumeral,
            chordSymbol = current.symbol,
            tonicName = tonicName,
            drillMode = drillMode.name,
            inputMode = inputMode.name,
            elapsedMs = System.currentTimeMillis() - chordStartMs,
            misTapCount = chordMisTapCount,
        ))
        chordMisTapCount = 0
        chordStartMs = System.currentTimeMillis()

        if (completedIndex == chords.size - 1) {
            currentIndex = chords.size  // blocks tap() while finishSession runs
            timerJob?.cancel()
            viewModelScope.launch { finishSession(effectiveElapsedMs()) }
            return
        }

        currentIndex = completedIndex + 1
        phase = DrillPhase.AwaitingTap(0)
        currentFretboardNotes = buildFretboardNotesForChord(chords[currentIndex])
        currentNoteButtons = buildShuffledButtonsForChord(chords[currentIndex])
        startTimer()
        emitRunningState()
    }

    private fun finishSession(elapsedMs: Long) {
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
        val chord = chords[currentIndex]
        val revealedNotes = currentFretboardNotes.filter { it.revealed }
        val fretDots = revealedNotes.map { note ->
            val interval = intervalFromRoot(note.semitone, chord.rootSemitone)
            FretDot(note.position, note.noteName, interval, note.semitone)
        }
        val tappedCount = when (val p = phase) {
            is DrillPhase.AwaitingTap -> p.toneIndex
            is DrillPhase.FlashPause -> p.nextToneIndex ?: orderedTones.size
            is DrillPhase.UserPaused -> (p.resumeTo as? DrillPhase.AwaitingTap)?.toneIndex ?: 0
        }
        val tappedSemitones = orderedTones.take(tappedCount).mapNotNull { chord.semitoneFor(it) }.toSet()
        val revealedSemitones = revealedNotes.map { it.semitone }.toSet()
        _uiState.value = DrillUiState.Running(
            progressionName = progressionName,
            currentChord = chords[currentIndex],
            currentIndex = currentIndex,
            totalChords = chords.size,
            noteButtons = currentNoteButtons,
            tappedSemitones = tappedSemitones,
            elapsedMs = effectiveElapsedMs(),
            misTapCount = misTapCount,
            feedbackSemitone = feedbackSemitone,
            feedbackType = feedbackType,
            fretDots = fretDots,
            revealedSemitones = revealedSemitones,
            playingPosition = playingPosition,
            inputMode = drillInputMode,
            errorFretPosition = errorFretPosition,
            isFlashPaused = phase is DrillPhase.FlashPause,
            isUserPaused = phase is DrillPhase.UserPaused,
        )
    }

    private fun ResolvedChord.semitoneFor(tone: Chord.Tone): Int? =
        this[tone]?.let { noteNameToSemitone(it) }

    // Applies chord-tone spelling overrides for the 3rd and 7th buttons (so e.g. "Bbb"
    // appears instead of "Ab" for Gbdim7), then shuffles to eliminate positional cues.
    private fun buildShuffledButtonsForChord(chord: ResolvedChord): List<NoteButton> {
        if (chord.third.isEmpty() || chord.seventh.isEmpty()) return baseNoteButtons.shuffled()
        val thirdSemitone   = chord.thirdSemitone
        val seventhSemitone = chord.seventhSemitone
        return baseNoteButtons.map { button ->
            val label = when (button.semitone) {
                thirdSemitone -> chord.third
                seventhSemitone -> chord.seventh
                else -> button.label
            }
            button.copy(label = label)
        }.let { if (drillInputMode == DrillInputMode.Fretboard) it else it.shuffled() }
    }

    companion object {
        private const val TIMER_UPDATE_INTERVAL_MS = 100L
        private const val FEEDBACK_FLASH_MS = 300L

        fun Factory(
            repository: DrillRepository,
            progressionId: Long,
            tonicName: String,
            drillMode: DrillMode,
            inputMode: DrillInputMode,
            settingsRepository: SettingsRepository,
        ) =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return DrillViewModel(repository, progressionId, tonicName, drillMode, inputMode, settingsRepository) as T
                }
            }
    }
}
