@file:OptIn(ExperimentalMaterial3Api::class)
package com.lnkranch.yaga.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lnkranch.yaga.data.db.entity.ProgressionEntity
import com.lnkranch.yaga.domain.DrillInputMode
import com.lnkranch.yaga.domain.DrillMode
import com.lnkranch.yaga.ui.theme.ChordToneDrillTheme
import com.lnkranch.yaga.ui.viewmodel.SetupViewModel

@Composable
fun SetupScreen(
    vm: SetupViewModel,
    onStartDrill: (Long, String, DrillMode, DrillInputMode) -> Unit,
    onBuildProgression: () -> Unit,
) {
    val progressions by vm.progressions.collectAsState()
    val selectedTonic by vm.selectedTonic.collectAsState()
    val selectedProgressionId by vm.selectedProgressionId.collectAsState()
    val selectedDrillMode by vm.selectedDrillMode.collectAsState()
    val selectedInputMode by vm.selectedInputMode.collectAsState()
    val canStart by vm.canStartDrill.collectAsState()

    SetupScreenContent(
        tonics = vm.tonics,
        progressions = progressions,
        selectedTonic = selectedTonic,
        selectedProgressionId = selectedProgressionId,
        selectedDrillMode = selectedDrillMode,
        selectedInputMode = selectedInputMode,
        canStart = canStart,
        onTonicSelect = { vm.selectTonic(it) },
        onProgressionSelect = { vm.selectProgression(it) },
        onDrillModeSelect = { vm.selectDrillMode(it) },
        onInputModeSelect = { vm.selectInputMode(it) },
        onStartDrill = { onStartDrill(selectedProgressionId!!, selectedTonic, selectedDrillMode, selectedInputMode) },
        onBuildProgression = onBuildProgression
    )
}

@Preview(showBackground = true)
@Composable
fun SetupScreenPreview() {
    ChordToneDrillTheme {
        SetupScreenContent(
            tonics = listOf("C", "G", "D", "A", "E"),
            progressions = listOf(
                ProgressionEntity(1L, "Major 2-5-1", "Major", "ii V I", true),
                ProgressionEntity(2L, "Minor 2-5-1", "Minor", "iiø V i", true),
            ),
            selectedTonic = "C",
            selectedProgressionId = 1L,
            selectedDrillMode = DrillMode.Normal,
            selectedInputMode = DrillInputMode.Buttons,
            canStart = true,
            onTonicSelect = {},
            onProgressionSelect = { _ -> },
            onDrillModeSelect = {},
            onInputModeSelect = {},
            onStartDrill = {},
            onBuildProgression = {}
        )
    }
}

@Composable
fun SetupScreenContent(
    tonics: List<String>,
    progressions: List<ProgressionEntity>,
    selectedTonic: String,
    selectedProgressionId: Long?,
    selectedDrillMode: DrillMode,
    selectedInputMode: DrillInputMode,
    canStart: Boolean,
    onTonicSelect: (String) -> Unit,
    onProgressionSelect: (Long) -> Unit,
    onDrillModeSelect: (DrillMode) -> Unit,
    onInputModeSelect: (DrillInputMode) -> Unit,
    onStartDrill: () -> Unit,
    onBuildProgression: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("YAGA")
                        Text(
                            "Yet Another Guitar App",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onBuildProgression) { Text("Build") }
                },
            )
        },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
                Button(
                    onClick = onStartDrill,
                    enabled = canStart,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Start Drill") }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text(
                text = "Key",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(tonics) { tonic ->
                    FilterChip(
                        selected = tonic == selectedTonic,
                        onClick = { onTonicSelect(tonic) },
                        label = { Text(tonic) },
                    )
                }
            }

            Text(
                text = "Mode",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DrillMode.entries.forEach { mode ->
                    FilterChip(
                        selected = mode == selectedDrillMode,
                        onClick = { onDrillModeSelect(mode) },
                        label = { Text(mode.name) },
                    )
                }
            }

            Text(
                text = "Input",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DrillInputMode.entries.forEach { mode ->
                    FilterChip(
                        selected = mode == selectedInputMode,
                        onClick = { onInputModeSelect(mode) },
                        label = { Text(mode.name) },
                    )
                }
            }

            Text(
                text = "Progression",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(progressions, key = { it.id }) { prog ->
                    ProgressionItem(
                        progression = prog,
                        isSelected = prog.id == selectedProgressionId,
                        onClick = { onProgressionSelect(prog.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressionItem(
    progression: ProgressionEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val containerColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    ElevatedCard(
        onClick = onClick,
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(progression.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    progression.mode,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (isSelected) {
                Text(
                    "✓",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
