@file:OptIn(ExperimentalMaterial3Api::class)
package com.lnkranch.yaga.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lnkranch.yaga.data.repository.SettingsRepository
import com.lnkranch.yaga.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(vm: SettingsViewModel, onBack: () -> Unit) {
    val playingPosition by vm.playingPosition.collectAsState()
    val correctDisplayMs by vm.correctDisplayMs.collectAsState()

    var positionSlider by remember(playingPosition) {
        mutableFloatStateOf((playingPosition - SettingsRepository.PLAYING_POSITION_MIN).toFloat())
    }

    var showDisplayTimeDialog by remember { mutableStateOf(false) }
    var dialogInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Playing position",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = (positionSlider.toInt() + SettingsRepository.PLAYING_POSITION_MIN).toString(),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Slider(
                value = positionSlider,
                onValueChange = { positionSlider = it },
                onValueChangeFinished = {
                    vm.setPlayingPosition(positionSlider.toInt() + SettingsRepository.PLAYING_POSITION_MIN)
                },
                valueRange = 0f..(SettingsRepository.PLAYING_POSITION_MAX - SettingsRepository.PLAYING_POSITION_MIN).toFloat(),
                steps = SettingsRepository.PLAYING_POSITION_MAX - SettingsRepository.PLAYING_POSITION_MIN - 1,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Correct answer display time",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { vm.setCorrectDisplayMs(correctDisplayMs - SettingsRepository.CORRECT_DISPLAY_MS_STEP) },
                        enabled = correctDisplayMs > SettingsRepository.CORRECT_DISPLAY_MS_MIN,
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                    }
                    Text(
                        text = "%.2fs".format(correctDisplayMs / 1000.0),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .clickable {
                                dialogInput = "%.2f".format(correctDisplayMs / 1000.0)
                                showDisplayTimeDialog = true
                            }
                            .padding(horizontal = 4.dp),
                    )
                    IconButton(
                        onClick = { vm.setCorrectDisplayMs(correctDisplayMs + SettingsRepository.CORRECT_DISPLAY_MS_STEP) },
                        enabled = correctDisplayMs < SettingsRepository.CORRECT_DISPLAY_MS_MAX,
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase")
                    }
                }
            }
        }
    }

    if (showDisplayTimeDialog) {
        AlertDialog(
            onDismissRequest = { showDisplayTimeDialog = false },
            title = { Text("Display time") },
            text = {
                OutlinedTextField(
                    value = dialogInput,
                    onValueChange = { dialogInput = it },
                    suffix = { Text("s") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val parsed = dialogInput.toDoubleOrNull()
                    if (parsed != null) {
                        vm.setCorrectDisplayMs((parsed * 1000).toInt())
                    }
                    showDisplayTimeDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDisplayTimeDialog = false }) { Text("Cancel") }
            },
        )
    }
}
