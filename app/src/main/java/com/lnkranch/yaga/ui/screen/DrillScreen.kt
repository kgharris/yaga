package com.lnkranch.yaga.ui.screen

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lnkranch.yaga.ui.viewmodel.DrillUiState
import com.lnkranch.yaga.ui.viewmodel.DrillViewModel
import androidx.compose.ui.graphics.Color
import com.lnkranch.yaga.ui.viewmodel.NoteButton
import com.lnkranch.yaga.ui.viewmodel.NoteFeedback

import androidx.compose.ui.tooling.preview.Preview
import com.lnkranch.yaga.domain.ResolvedChord
import com.lnkranch.yaga.ui.theme.ChordToneDrillTheme

@Composable
fun DrillScreen(
    vm: DrillViewModel,
    onSessionComplete: (DrillUiState.Complete) -> Unit,
) {
    val uiState by vm.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is DrillUiState.Complete) {
            onSessionComplete(uiState as DrillUiState.Complete)
        }
    }

    DrillScreenContent(uiState = uiState, onTap = vm::tap)
}

@Composable
fun DrillScreenContent(
    uiState: DrillUiState,
    onTap: (String) -> Unit,
) {
    when (val state = uiState) {
        DrillUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        DrillUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Failed to load session", style = MaterialTheme.typography.bodyLarge)
        }
        is DrillUiState.Running -> RunningContent(state = state, onTap = onTap)
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
                currentChord = ResolvedChord("ii", "Min7", "Dm7", "F", "C"),
                currentIndex = 0,
                totalChords = 3,
                noteButtons = listOf(
                    NoteButton("C", 0), NoteButton("Db", 1), NoteButton("D", 2),
                    NoteButton("Eb", 3), NoteButton("E", 4), NoteButton("F", 5),
                    NoteButton("Gb", 6), NoteButton("G", 7), NoteButton("Ab", 8),
                    NoteButton("A", 9), NoteButton("Bb", 10), NoteButton("B", 11)
                ),
                tappedThird = false,
                tappedSeventh = false,
                elapsedMs = 12500,
                misTapCount = 1,
                feedbackNote = "F",
                feedbackType = NoteFeedback.Correct,
            ),
            onTap = {}
        )
    }
}

@Composable
private fun RunningContent(state: DrillUiState.Running, onTap: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 16.dp, vertical = 24.dp),
    ) {
        // Header: timer | progress | errors
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                formatTime(state.elapsedMs),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                "${state.currentIndex + 1} / ${state.totalChords}",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                "err: ${state.misTapCount}",
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Spacer(Modifier.weight(1f))

        // Chord symbol
        Text(
            text = state.currentChord.symbol,
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.weight(1f))

        // Note grid
        NoteGrid(
            noteButtons = state.noteButtons,
            thirdName = state.currentChord.third,
            seventhName = state.currentChord.seventh,
            tappedThird = state.tappedThird,
            tappedSeventh = state.tappedSeventh,
            feedbackNote = state.feedbackNote,
            feedbackType = state.feedbackType,
            onTap = onTap,
        )
    }
}

@Composable
private fun NoteGrid(
    noteButtons: List<NoteButton>,
    thirdName: String,
    seventhName: String,
    tappedThird: Boolean,
    tappedSeventh: Boolean,
    feedbackNote: String?,
    feedbackType: NoteFeedback?,
    onTap: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Row 1: First 6 notes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            noteButtons.take(6).forEach { btn ->
                NoteKey(
                    btn = btn,
                    isTapped = isTapped(btn.label, thirdName, seventhName, tappedThird, tappedSeventh),
                    feedback = if (btn.label == feedbackNote) feedbackType else null,
                    modifier = Modifier.weight(1f),
                    onTap = onTap,
                )
            }
        }
        // Row 2: Remaining 6 notes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            noteButtons.drop(6).forEach { btn ->
                NoteKey(
                    btn = btn,
                    isTapped = isTapped(btn.label, thirdName, seventhName, tappedThird, tappedSeventh),
                    feedback = if (btn.label == feedbackNote) feedbackType else null,
                    modifier = Modifier.weight(1f),
                    onTap = onTap,
                )
            }
        }
    }
}

private fun isTapped(
    label: String,
    thirdName: String,
    seventhName: String,
    tappedThird: Boolean,
    tappedSeventh: Boolean,
): Boolean = (label == thirdName && tappedThird) || (label == seventhName && tappedSeventh)

@Composable
private fun NoteKey(
    btn: NoteButton,
    isTapped: Boolean,
    feedback: NoteFeedback?,
    modifier: Modifier = Modifier,
    onTap: (String) -> Unit,
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
        onClick = { onTap(btn.label) },
        modifier = modifier.height(60.dp),
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
