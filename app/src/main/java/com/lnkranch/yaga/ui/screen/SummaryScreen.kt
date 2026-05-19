package com.lnkranch.yaga.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lnkranch.yaga.ui.viewmodel.SummaryViewModel

import androidx.compose.ui.tooling.preview.Preview
import com.lnkranch.yaga.domain.DrillMode
import com.lnkranch.yaga.ui.theme.ChordToneDrillTheme
import com.lnkranch.yaga.ui.viewmodel.SummaryUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    vm: SummaryViewModel,
    onPlayAgain: () -> Unit,
    onBackToSetup: () -> Unit,
) {
    SummaryScreenContent(
        state = vm.uiState,
        onPlayAgain = onPlayAgain,
        onBackToSetup = onBackToSetup
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreenContent(
    state: SummaryUiState,
    onPlayAgain: () -> Unit,
    onBackToSetup: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Results") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.isNewPersonalBest) {
                Text(
                    "New Personal Best!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }

            Text(state.progressionName, style = MaterialTheme.typography.titleMedium)
            Text(
                "${state.tonicName} · ${state.drillMode.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HorizontalDivider()

            Text(
                "%.1f".format(state.score),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
            )
            Text("score", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            state.scoreDelta?.let { delta ->
                val prefix = if (delta >= 0) "+" else ""
                Text(
                    "$prefix%.1f vs. previous best".format(delta),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (delta >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                )
            }

            HorizontalDivider()

            StatRow(label = "Time", value = state.formattedTime)
            StatRow(label = "Errors", value = state.misTapCount.toString())

            Spacer(Modifier.weight(1f))

            Button(onClick = onPlayAgain, modifier = Modifier.fillMaxWidth()) {
                Text("Play Again")
            }
            OutlinedButton(onClick = onBackToSetup, modifier = Modifier.fillMaxWidth()) {
                Text("Back to Setup")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SummaryScreenPreview() {
    ChordToneDrillTheme {
        SummaryScreenContent(
            state = SummaryUiState(
                progressionName = "Major 2-5-1",
                tonicName = "C Major",
                drillMode = DrillMode.Normal,
                formattedTime = "0:12.5",
                misTapCount = 1,
                score = 85.5,
                isNewPersonalBest = true,
                scoreDelta = 5.2
            ),
            onPlayAgain = {},
            onBackToSetup = {}
        )
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}
