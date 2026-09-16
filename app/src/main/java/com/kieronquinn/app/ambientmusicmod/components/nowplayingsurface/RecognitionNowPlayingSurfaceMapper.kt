package com.kieronquinn.app.ambientmusicmod.components.nowplayingsurface

import com.kieronquinn.app.ambientmusicmod.repositories.RecognitionRepository.RecognitionState

object RecognitionNowPlayingSurfaceMapper {

    fun map(state: RecognitionState.Recognised): NowPlayingSurfaceEvent {
        return NowPlayingSurfaceEvent(
            title = state.recognitionResult.trackName,
            artist = state.recognitionResult.artist,
            timeoutMillis = state.metadata?.remainingTime
                ?: NowPlayingSurfaceEvent.DEFAULT_TIMEOUT_MILLIS
        )
    }
}
