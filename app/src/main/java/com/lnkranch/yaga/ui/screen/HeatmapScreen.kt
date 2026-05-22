@file:OptIn(ExperimentalMaterial3Api::class)
package com.lnkranch.yaga.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lnkranch.yaga.ui.viewmodel.HeatmapCell
import com.lnkranch.yaga.ui.viewmodel.HeatmapViewModel

private val colorGreen = Color(0xFF4CAF50)
private val colorYellow = Color(0xFFFFEB3B)

private const val HEATMAP_COLOR_MIDPOINT = 0.5f
private const val HEATMAP_COLOR_SCALE = 1f / HEATMAP_COLOR_MIDPOINT

private fun heatmapColor(normalizedScore: Float, errorColor: Color): Color =
    if (normalizedScore <= HEATMAP_COLOR_MIDPOINT) {
        lerp(colorGreen, colorYellow, normalizedScore * HEATMAP_COLOR_SCALE)
    } else {
        lerp(colorYellow, errorColor, (normalizedScore - HEATMAP_COLOR_MIDPOINT) * HEATMAP_COLOR_SCALE)
    }

private sealed interface DialogState {
    data object None : DialogState
    data object ResetMode : DialogState
    data object ResetAll : DialogState
    data class ResetCell(val chordSymbol: String) : DialogState
}

@Composable
fun HeatmapScreen(vm: HeatmapViewModel) {
    val availableModes by vm.availableModes.collectAsState()
    val selectedMode by vm.selectedDrillMode.collectAsState()
    val availableInputModes by vm.availableInputModes.collectAsState()
    val selectedInputMode by vm.selectedInputMode.collectAsState()
    val cells by vm.cells.collectAsState()

    var dialogState by remember { mutableStateOf<DialogState>(DialogState.None) }
    var menuExpanded by remember { mutableStateOf(false) }

    val errorColor = MaterialTheme.colorScheme.error

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Heatmap")
                        Text(
                            "Slowest chords first",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = {
                                    val label = listOf(selectedMode, selectedInputMode)
                                        .filter { it.isNotEmpty() }
                                        .joinToString(" / ")
                                        .ifEmpty { "current" }
                                    Text("Reset $label")
                                },
                                onClick = {
                                    menuExpanded = false
                                    dialogState = DialogState.ResetMode
                                },
                                enabled = selectedMode.isNotEmpty(),
                            )
                            DropdownMenuItem(
                                text = { Text("Reset all data") },
                                onClick = {
                                    menuExpanded = false
                                    dialogState = DialogState.ResetAll
                                },
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (availableModes.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(availableModes) { mode ->
                        FilterChip(
                            selected = mode == selectedMode,
                            onClick = { vm.selectMode(mode) },
                            label = { Text(mode) },
                        )
                    }
                }
            }
            if (availableInputModes.size > 1) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(availableInputModes) { mode ->
                        FilterChip(
                            selected = mode == selectedInputMode,
                            onClick = { vm.selectInputMode(mode) },
                            label = { Text(mode) },
                        )
                    }
                }
            }

            if (cells.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (selectedMode.isEmpty())
                            "No drill data yet.\nComplete a session to see your heatmap."
                        else {
                            val label = listOf(selectedMode, selectedInputMode)
                                .filter { it.isNotEmpty() }
                                .joinToString(" / ")
                            "No data for $label yet.\nComplete a session to see your heatmap."
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(cells, key = { it.chordSymbol }) { cell ->
                        ChordRow(
                            cell = cell,
                            errorColor = errorColor,
                            onLongPress = { dialogState = DialogState.ResetCell(cell.chordSymbol) },
                        )
                    }
                }
            }
        }
    }

    when (val state = dialogState) {
        DialogState.None -> Unit

        DialogState.ResetMode -> AlertDialog(
            onDismissRequest = { dialogState = DialogState.None },
            title = { Text("Reset $selectedMode / $selectedInputMode?") },
            text = { Text("This will delete all heatmap data for $selectedMode / $selectedInputMode. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteMode()
                    dialogState = DialogState.None
                }) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { dialogState = DialogState.None }) { Text("Cancel") }
            },
        )

        DialogState.ResetAll -> AlertDialog(
            onDismissRequest = { dialogState = DialogState.None },
            title = { Text("Reset all data?") },
            text = { Text("This will delete all heatmap data across every mode. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteAll()
                    dialogState = DialogState.None
                }) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { dialogState = DialogState.None }) { Text("Cancel") }
            },
        )

        is DialogState.ResetCell -> AlertDialog(
            onDismissRequest = { dialogState = DialogState.None },
            title = { Text("Reset ${state.chordSymbol}?") },
            text = { Text("This will delete all ${state.chordSymbol} attempts for $selectedMode mode. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteCell(state.chordSymbol)
                    dialogState = DialogState.None
                }) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { dialogState = DialogState.None }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun ChordRow(
    cell: HeatmapCell,
    errorColor: Color,
    onLongPress: () -> Unit,
) {
    val barColor = heatmapColor(cell.normalizedScore, errorColor)
    val avgSec = cell.avgAdjustedMs / 1000.0
    val timeLabel = "%.1fs".format(avgSec)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .pointerInput(cell.chordSymbol) {
                detectTapGestures(onLongPress = { onLongPress() })
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Color bar
        Box(
            modifier = Modifier
                .width(8.dp)
                .height(48.dp)
                .background(barColor, shape = RoundedCornerShape(4.dp)),
        )

        // Chord symbol + quality label
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = cell.chordSymbol,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = cell.qualitySymbol,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Avg time + attempt count
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = timeLabel,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "${cell.totalAttempts} attempts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
