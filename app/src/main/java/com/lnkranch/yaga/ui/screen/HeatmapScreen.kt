@file:OptIn(ExperimentalMaterial3Api::class)
package com.lnkranch.yaga.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lnkranch.yaga.domain.TONIC_NAMES
import com.lnkranch.yaga.ui.viewmodel.HeatmapCell
import com.lnkranch.yaga.ui.viewmodel.HeatmapViewModel

private val colorGreen = Color(0xFF4CAF50)
private val colorYellow = Color(0xFFFFEB3B)

private fun heatmapCellColor(normalizedScore: Float, errorColor: Color): Color {
    return if (normalizedScore <= 0.5f) {
        lerp(colorGreen, colorYellow, normalizedScore * 2f)
    } else {
        lerp(colorYellow, errorColor, (normalizedScore - 0.5f) * 2f)
    }
}

// Dialog state sealed class keeps track of which confirmation dialog is showing.
private sealed interface DialogState {
    data object None : DialogState
    data object ResetMode : DialogState
    data object ResetAll : DialogState
    data class ResetCell(val tonicName: String, val chordQuality: String, val qualitySymbol: String) : DialogState
}

@Composable
fun HeatmapScreen(vm: HeatmapViewModel) {
    val availableModes by vm.availableModes.collectAsState()
    val selectedMode by vm.selectedDrillMode.collectAsState()
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
                            "Performance by chord and key",
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
                                text = { Text("Reset ${selectedMode.ifEmpty { "current" }} mode") },
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
            // Mode selector — FilterChips, data-driven from availableModes
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

            // Grid or empty state
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
                        else
                            "No data for $selectedMode mode yet.\nComplete a session to see your heatmap.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                HeatmapGrid(
                    cells = cells,
                    errorColor = errorColor,
                    onLongPressCell = { tonicName, chordQuality, qualitySymbol ->
                        dialogState = DialogState.ResetCell(tonicName, chordQuality, qualitySymbol)
                    },
                )
            }
        }
    }

    // Confirmation dialogs
    when (val state = dialogState) {
        DialogState.None -> Unit

        DialogState.ResetMode -> AlertDialog(
            onDismissRequest = { dialogState = DialogState.None },
            title = { Text("Reset $selectedMode mode?") },
            text = { Text("This will delete all heatmap data for $selectedMode mode. This cannot be undone.") },
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
            title = { Text("Reset ${state.qualitySymbol} in ${state.tonicName}?") },
            text = { Text("This will delete all attempts for ${state.qualitySymbol} chords in the key of ${state.tonicName} for the current mode.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteCell(state.tonicName, state.chordQuality)
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
private fun HeatmapGrid(
    cells: List<HeatmapCell>,
    errorColor: Color,
    onLongPressCell: (tonicName: String, chordQuality: String, qualitySymbol: String) -> Unit,
) {
    // Build a fast-lookup map: (chordQuality, tonicName) -> HeatmapCell
    val cellMap: Map<Pair<String, String>, HeatmapCell> =
        cells.associateBy { it.chordQuality to it.tonicName }

    // Derive distinct chordQuality names in same order as yLabels
    val qualities: List<String> = cells
        .map { it.chordQuality }
        .distinct()

    // Build (qualityName -> qualitySymbol) from populated cells
    val symbolForQuality: Map<String, String> =
        cells.associate { it.chordQuality to it.qualitySymbol }

    val yLabelWidth = 40.dp
    val cellSize = 44.dp
    val headerHeight = 28.dp
    val rowHeight = cellSize

    val horizontalScrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        // X-axis header row — scrolls with the grid
        Row(modifier = Modifier.fillMaxWidth()) {
            // Spacer to align with Y-axis label column
            Spacer(modifier = Modifier.width(yLabelWidth))
            Row(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
                TONIC_NAMES.forEach { tonic ->
                    Box(
                        modifier = Modifier
                            .size(width = cellSize, height = headerHeight),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = tonic,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                        )
                    }
                }
            }
        }

        // Grid rows — LazyColumn for the Y axis
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(qualities) { quality ->
                val symbol = symbolForQuality[quality] ?: quality
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(rowHeight),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Y-axis label
                    Box(
                        modifier = Modifier
                            .width(yLabelWidth)
                            .height(rowHeight),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = symbol,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                        )
                    }

                    // 12 cells for this quality row — scrolls horizontally
                    Row(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
                        TONIC_NAMES.forEach { tonic ->
                            val cell = cellMap[quality to tonic]
                            HeatmapCellBox(
                                cell = cell,
                                tonicName = tonic,
                                qualitySymbol = symbol,
                                chordQuality = quality,
                                errorColor = errorColor,
                                cellSize = cellSize,
                                onLongPress = onLongPressCell,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatmapCellBox(
    cell: HeatmapCell?,
    tonicName: String,
    qualitySymbol: String,
    chordQuality: String,
    errorColor: Color,
    cellSize: Dp,
    onLongPress: (tonicName: String, chordQuality: String, qualitySymbol: String) -> Unit,
) {
    val backgroundColor: Color
    val contentColor: Color
    val borderColor: Color

    if (cell != null) {
        backgroundColor = heatmapCellColor(cell.normalizedScore, errorColor)
        contentColor = Color.Black.copy(alpha = 0.87f)
        borderColor = Color.Transparent
    } else {
        backgroundColor = Color.Transparent
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        borderColor = MaterialTheme.colorScheme.outlineVariant
    }

    Box(
        modifier = Modifier
            .size(cellSize)
            .padding(2.dp)
            .background(backgroundColor, shape = RoundedCornerShape(4.dp))
            .border(
                width = if (cell == null) 1.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(4.dp),
            )
            .pointerInput(tonicName, chordQuality) {
                detectTapGestures(
                    onLongPress = {
                        if (cell != null) {
                            onLongPress(tonicName, chordQuality, qualitySymbol)
                        }
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        if (cell != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = qualitySymbol,
                    fontSize = 8.sp,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    lineHeight = 9.sp,
                )
                Text(
                    text = tonicName,
                    fontSize = 8.sp,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    lineHeight = 9.sp,
                )
            }
        }
    }
}
