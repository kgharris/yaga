package com.lnkranch.yaga.ui.viewmodel

sealed class DrillPhase {
    data class AwaitingTap(val toneIndex: Int) : DrillPhase()
    data class FlashPause(val nextToneIndex: Int?) : DrillPhase()  // null = chord complete
    data class UserPaused(val resumeTo: DrillPhase) : DrillPhase()
}
