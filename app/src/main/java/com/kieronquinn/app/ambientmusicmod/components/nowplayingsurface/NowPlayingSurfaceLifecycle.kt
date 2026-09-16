package com.kieronquinn.app.ambientmusicmod.components.nowplayingsurface

import com.kieronquinn.app.ambientmusicmod.repositories.RecognitionRepository.RecognitionState

class NowPlayingSurfaceLifecycle(
    private val publisher: NowPlayingSurfacePublisher
) {

    fun onRecognitionState(state: RecognitionState) {
        when (state) {
            is RecognitionState.Recognised -> {
                publisher.publish(RecognitionNowPlayingSurfaceMapper.map(state))
            }
            is RecognitionState.Failed,
            is RecognitionState.Error -> {
                publisher.clear()
            }
            is RecognitionState.Recording,
            is RecognitionState.Recognising -> Unit
        }
    }
}
