@file:OptIn(ExperimentalMaterial3Api::class)
package com.lnkranch.yaga.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lnkranch.yaga.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(vm: SettingsViewModel, onBack: () -> Unit) {
    val playingPosition by vm.playingPosition.collectAsState()

    // Local slider state so the thumb moves immediately during drag
    var sliderValue by remember(playingPosition) {
        mutableFloatStateOf((playingPosition - 1).toFloat())
    }

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
                    text = (sliderValue.toInt() + 1).toString(),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                onValueChangeFinished = { vm.setPlayingPosition(sliderValue.toInt() + 1) },
                valueRange = 0f..16f,
                steps = 15,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
