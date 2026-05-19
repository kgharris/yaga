package com.lnkranch.yaga.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lnkranch.yaga.DrillApplication
import com.lnkranch.yaga.domain.DrillMode

data class SummaryUiState(
    val progressionName: String,
    val tonicName: String,
    val drillMode: DrillMode,
    val formattedTime: String,
    val misTapCount: Int,
    val score: Double,
    val isNewPersonalBest: Boolean,
    val scoreDelta: Double?,
)

class SummaryViewModel(complete: DrillUiState.Complete) : ViewModel() {

    val uiState = SummaryUiState(
        progressionName = complete.progressionName,
        tonicName = complete.sessionResult.tonicName,
        drillMode = complete.drillMode,
        formattedTime = formatTime(complete.sessionResult.elapsedMs),
        misTapCount = complete.sessionResult.misTapCount,
        score = complete.sessionResult.score,
        isNewPersonalBest = complete.isNewPersonalBest,
        scoreDelta = complete.previousBestScore?.let { complete.sessionResult.score - it },
    )

    companion object {
        fun formatTime(ms: Long): String {
            val minutes = ms / 60_000
            val seconds = (ms % 60_000) / 1000
            val tenths = (ms % 1000) / 100
            return "%d:%02d.%d".format(minutes, seconds, tenths)
        }

        fun Factory(application: DrillApplication) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val complete = checkNotNull(application.pendingSummary) { "No pending summary" }
                application.pendingSummary = null
                @Suppress("UNCHECKED_CAST")
                return SummaryViewModel(complete) as T
            }
        }
    }
}
