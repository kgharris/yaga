package com.lnkranch.yaga.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lnkranch.yaga.theory.Mode
import com.lnkranch.yaga.ui.viewmodel.ProgressionBuilderViewModel

import androidx.compose.ui.tooling.preview.Preview
import com.lnkranch.yaga.theory.RomanChord
import com.lnkranch.yaga.ui.theme.ChordToneDrillTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProgressionBuilderScreen(
    vm: ProgressionBuilderViewModel,
    onSaved: () -> Unit,
    onBack: () -> Unit,
) {
    val name by vm.name.collectAsState()
    val mode by vm.mode.collectAsState()
    val chords by vm.chords.collectAsState()
    val availableChords by vm.availableChords.collectAsState()
    val canSave by vm.canSave.collectAsState()

    ProgressionBuilderScreenContent(
        name = name,
        mode = mode,
        chords = chords,
        availableChords = availableChords,
        canSave = canSave,
        onNameChange = vm::setName,
        onModeChange = vm::setMode,
        onAddChord = vm::addChord,
        onRemoveChord = vm::removeChordAt,
        onSave = { vm.save(onSaved) },
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProgressionBuilderScreenContent(
    name: String,
    mode: Mode,
    chords: List<RomanChord>,
    availableChords: List<RomanChord>,
    canSave: Boolean,
    onNameChange: (String) -> Unit,
    onModeChange: (Mode) -> Unit,
    onAddChord: (RomanChord) -> Unit,
    onRemoveChord: (Int) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Build Progression") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Progression name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(Modifier.height(16.dp))

            Text("Mode", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = mode == Mode.Major,
                    onClick = { onModeChange(Mode.Major) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                ) { Text("Major") }
                SegmentedButton(
                    selected = mode == Mode.Minor,
                    onClick = { onModeChange(Mode.Minor) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                ) { Text("Minor") }
            }

            Spacer(Modifier.height(16.dp))

            Text("Add chords", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                availableChords.forEach { chord ->
                    SuggestionChip(
                        onClick = { onAddChord(chord) },
                        label = { Text(chord.romanNumeral) },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Progression (${chords.size} chord${if (chords.size != 1) "s" else ""})",
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(Modifier.height(4.dp))
            if (chords.isEmpty()) {
                Text(
                    "Tap chords above to add them",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyRow(
                    contentPadding = PaddingValues(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(chords) { index, chord ->
                        InputChip(
                            selected = false,
                            onClick = { onRemoveChord(index) },
                            label = { Text(chord.romanNumeral) },
                            trailingIcon = { Text("×", style = MaterialTheme.typography.labelMedium) },
                        )
                    }
                }
                Text(
                    "Tap a chord to remove it",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onSave,
                enabled = canSave,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save Progression") }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProgressionBuilderScreenPreview() {
    ChordToneDrillTheme {
        ProgressionBuilderScreenContent(
            name = "Jazz Blues",
            mode = Mode.Major,
            chords = listOf(RomanChord.I, RomanChord.IV, RomanChord.I, RomanChord.V),
            availableChords = listOf(RomanChord.I, RomanChord.II, RomanChord.III, RomanChord.IV, RomanChord.V),
            canSave = true,
            onNameChange = {},
            onModeChange = {},
            onAddChord = {},
            onRemoveChord = {},
            onSave = {},
            onBack = {}
        )
    }
}
