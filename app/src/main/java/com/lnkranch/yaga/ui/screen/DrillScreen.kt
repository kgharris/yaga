package com.lnkranch.yaga.ui.screen

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.delay
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lnkranch.yaga.domain.DrillInputMode
import com.lnkranch.yaga.ui.component.FretboardDisplay
import com.lnkranch.yaga.ui.viewmodel.DrillUiState
import com.lnkranch.yaga.ui.viewmodel.DrillViewModel
import androidx.compose.ui.graphics.Color
import com.lnkranch.yaga.ui.viewmodel.NoteButton
import com.lnkranch.yaga.ui.viewmodel.NoteFeedback

import androidx.compose.ui.tooling.preview.Preview
import com.lnkranch.yaga.theory.Chord
import com.lnkranch.yaga.theory.ResolvedChord
import com.lnkranch.yaga.ui.theme.ChordToneDrillTheme

@Composable
private fun rememberHapticPlayer(): (NoteFeedback) -> Unit {
    val view = LocalView.current
    return remember(view) {
        { feedback ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val constant = when (feedback) {
                    NoteFeedback.Correct   -> HapticFeedbackConstants.CONFIRM
                    NoteFeedback.Incorrect -> HapticFeedbackConstants.REJECT
                }
                view.performHapticFeedback(constant)
            } else {
                // API 26-29: drive Vibrator directly
                @Suppress("DEPRECATION")
                val vib = view.context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    ?: return@remember
                if (feedback == NoteFeedback.Correct) {
                    vib.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    vib.vibrate(
                        VibrationEffect.createWaveform(longArrayOf(0, 60, 100, 60), -1)
                    )
                }
            }
        }
    }
}

@Composable
fun DrillScreen(
    vm: DrillViewModel,
    onSessionComplete: (DrillUiState.Complete) -> Unit,
) {
    val hapticPlayer = rememberHapticPlayer()
    LaunchedEffect(Unit) {
        vm.hapticEvent.collect { hapticPlayer(it) }
    }
    val uiState by vm.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is DrillUiState.Complete) {
            onSessionComplete(uiState as DrillUiState.Complete)
        }
    }

    DrillScreenContent(uiState = uiState, onTap = { sem -> vm.tap(sem) }, onFretTap = vm::tapFret, onTogglePause = vm::togglePause)
}

@Composable
fun DrillScreenContent(
    uiState: DrillUiState,
    onTap: (Int) -> Unit,
    onFretTap: (Int, Int) -> Unit,
    onTogglePause: () -> Unit = {},
) {
    when (val state = uiState) {
        DrillUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        DrillUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Failed to load session", style = MaterialTheme.typography.bodyLarge)
        }
        is DrillUiState.Running -> RunningContent(state = state, onTap = onTap, onFretTap = onFretTap, onTogglePause = onTogglePause)
        is DrillUiState.Complete -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DrillScreenPreview() {
    ChordToneDrillTheme {
        DrillScreenContent(
            uiState = DrillUiState.Running(
                progressionName = "Major 2-5-1",
                currentChord = ResolvedChord(
                    romanNumeral = "ii",
                    chord = Chord.Min7,
                    symbol = "Dm7",
                    tones = mapOf(
                        Chord.Tone._root to "D",
                        Chord.Tone._3rd  to "F",
                        Chord.Tone._5th  to "A",
                        Chord.Tone._7th  to "C",
                    ),
                ),
                currentIndex = 0,
                totalChords = 3,
                noteButtons = listOf(
                    NoteButton("C", 0), NoteButton("Db", 1), NoteButton("D", 2),
                    NoteButton("Eb", 3), NoteButton("E", 4), NoteButton("F", 5),
                    NoteButton("Gb", 6), NoteButton("G", 7), NoteButton("Ab", 8),
                    NoteButton("A", 9), NoteButton("Bb", 10), NoteButton("B", 11)
                ),
                tappedSemitones = emptySet(),
                revealedSemitones = emptySet(),
                elapsedMs = 12500,
                misTapCount = 1,
                feedbackSemitone = 5,
                feedbackType = NoteFeedback.Correct,
                fretDots = emptyList(),
                playingPosition = 1,
                inputMode = DrillInputMode.Buttons,
            ),
            onTap = {},
            onFretTap = { _, _ -> },
        )
    }
}

@Composable
private fun RunningContent(
    state: DrillUiState.Running,
    onTap: (Int) -> Unit,
    onFretTap: (Int, Int) -> Unit,
    onTogglePause: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 16.dp, vertical = 24.dp),
    ) {
        // Zone 1 — Chord zone (top, natural height)
        // Header: timer | progress | errors | pause
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                formatTime(state.elapsedMs),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.weight(1f))
            Text(
                "${state.currentIndex + 1} / ${state.totalChords}",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                "  err: ${state.misTapCount}",
                style = MaterialTheme.typography.titleMedium,
            )
            IconButton(onClick = onTogglePause) {
                Icon(
                    imageVector = if (state.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (state.isPaused) "Resume" else "Pause",
                )
            }
        }

        // Chord symbol
        Text(
            text = state.currentChord.symbol,
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        // Zone 2 & 3 — Fretboard + input zone wrapped in Box for pause overlay
        Box(modifier = Modifier.weight(1f)) {
            Column {
                // Zone 2 — Fretboard zone (middle, fills remaining space)
                FretboardDisplay(
                    dots = state.fretDots,
                    playingPosition = state.playingPosition,
                    tappable = state.inputMode == DrillInputMode.Fretboard,
                    errorDot = state.errorFretPosition,
                    onFretTap = if (state.inputMode == DrillInputMode.Fretboard) onFretTap else null,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Zone 3 — Input zone (bottom, natural height)
                when (state.inputMode) {
                    DrillInputMode.Buttons -> NoteGrid(
                        noteButtons = state.noteButtons,
                        tappedSemitones = state.tappedSemitones,
                        feedbackSemitone = state.feedbackSemitone,
                        feedbackType = state.feedbackType,
                        onTap = onTap,
                    )
                    DrillInputMode.Fretboard -> FretboardNoteGrid(
                        noteButtons = state.noteButtons,
                        revealedSemitones = state.revealedSemitones,
                    )
                }
            }
            if (state.isPaused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Paused",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

private const val NoteGridShuffleDelayMs = 500L
private val NoteKeyHeight = 60.dp
private val NoteGridHeight = NoteKeyHeight * 2 + 4.dp

@Composable
private fun NoteGrid(
    noteButtons: List<NoteButton>,
    tappedSemitones: Set<Int>,
    feedbackSemitone: Int?,
    feedbackType: NoteFeedback?,
    onTap: (Int) -> Unit,
) {
    var displayedButtons by remember { mutableStateOf(noteButtons.sortedBy { it.semitone }) }
    val semitoneKey = remember(noteButtons) { noteButtons.map { it.semitone } }

    LaunchedEffect(semitoneKey) {
        displayedButtons = noteButtons.sortedBy { it.semitone }
        delay(NoteGridShuffleDelayMs)
        displayedButtons = noteButtons
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(6),
        modifier = Modifier
            .fillMaxWidth()
            .height(NoteGridHeight),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        userScrollEnabled = false,
    ) {
        items(
            items = displayedButtons,
            key = { it.semitone },
        ) { btn ->
            NoteKey(
                btn = btn,
                isTapped = btn.semitone in tappedSemitones,
                feedback = if (feedbackSemitone == btn.semitone) feedbackType else null,
                modifier = Modifier.animateItem(
                    placementSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    )
                ),
                onTap = onTap,
            )
        }
    }
}

@Composable
private fun FretboardNoteGrid(
    noteButtons: List<NoteButton>,
    revealedSemitones: Set<Int>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            noteButtons.take(6).forEach { btn ->
                FretboardNoteKey(btn = btn, revealed = btn.semitone in revealedSemitones, modifier = Modifier.weight(1f))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            noteButtons.drop(6).forEach { btn ->
                FretboardNoteKey(btn = btn, revealed = btn.semitone in revealedSemitones, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun FretboardNoteKey(
    btn: NoteButton,
    revealed: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = if (revealed) {
        ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4CAF50),
            contentColor = Color.White,
            disabledContainerColor = Color(0xFF4CAF50),
            disabledContentColor = Color.White,
        )
    } else {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurface,
        )
    }

    Button(
        onClick = {},
        enabled = false,
        modifier = modifier.height(NoteKeyHeight),
        colors = colors,
        shape = RoundedCornerShape(6.dp),
        contentPadding = PaddingValues(0.dp),
    ) {
        Text(
            text = btn.label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
        )
    }
}

@Composable
private fun NoteKey(
    btn: NoteButton,
    isTapped: Boolean,
    feedback: NoteFeedback?,
    modifier: Modifier = Modifier,
    onTap: (Int) -> Unit,
) {
    val colors = when {
        feedback == NoteFeedback.Correct -> ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4CAF50), // Green 500
            contentColor = Color.White
        )
        feedback == NoteFeedback.Incorrect -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        )
        isTapped -> ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        else -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface,
        )
    }

    Button(
        onClick = { onTap(btn.semitone) },
        modifier = modifier.height(NoteKeyHeight),
        colors = colors,
        shape = RoundedCornerShape(6.dp),
        contentPadding = PaddingValues(0.dp),
    ) {
        Text(
            text = btn.label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
        )
    }
}

private fun formatTime(ms: Long): String {
    val minutes = ms / 60_000
    val seconds = (ms % 60_000) / 1000
    val tenths = (ms % 1000) / 100
    return "%d:%02d.%d".format(minutes, seconds, tenths)
}
